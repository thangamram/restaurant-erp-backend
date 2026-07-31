package com.restaurant.backend.service;

import com.restaurant.backend.dto.request.BillDto;
import com.restaurant.backend.entity.*;
import com.restaurant.backend.enums.BillStatus;
import com.restaurant.backend.enums.OrderStatus;
import com.restaurant.backend.exception.BadRequestException;
import com.restaurant.backend.exception.ResourceNotFoundException;
import com.restaurant.backend.repository.*;
import com.restaurant.backend.util.PdfInvoiceGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final PdfInvoiceGenerator pdfInvoiceGenerator;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Value("${app.restaurant.default-gst-percentage:5.0}")
    private double defaultGstPercentage;

    @Value("${app.restaurant.default-service-charge-percentage:5.0}")
    private double defaultServiceChargePercentage;

    @Value("${app.restaurant.name:Restaurant ERP}")
    private String restaurantName;

    @Value("${app.restaurant.address:}")
    private String restaurantAddress;

    @Value("${app.restaurant.tax-number:}")
    private String taxNumber;

    @Transactional
    public BillDto.BillResponse generateBill(BillDto.GenerateBillRequest request, Long cashierId, String ipAddress) {
        Order order = orderRepository.findByIdWithItems(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("Order must be in DELIVERED status before billing. Current: " + order.getStatus());
        }

        if (billRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new BadRequestException("Bill already generated for Order #" + order.getOrderNumber());
        }

        User cashier = userRepository.findById(cashierId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", cashierId));

        // Calculate item total
        BigDecimal itemTotal = order.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate GST (use menu item-level GST)
        BigDecimal gstAmount = order.getItems().stream()
                .map(item -> {
                    BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    BigDecimal gstPct = item.getGstPercentage() != null ? item.getGstPercentage()
                            : BigDecimal.valueOf(defaultGstPercentage);
                    return lineTotal.multiply(gstPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Service Charge
        BigDecimal serviceChargePct = request.getServiceChargePercentage() != null
                ? request.getServiceChargePercentage() : BigDecimal.valueOf(defaultServiceChargePercentage);
        BigDecimal serviceCharge = itemTotal.multiply(serviceChargePct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Discount from coupon
        BigDecimal discountAmount = BigDecimal.ZERO;
        String couponCode = null;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            Coupon coupon = couponRepository.findByCode(request.getCouponCode())
                    .orElseThrow(() -> new BadRequestException("Invalid coupon code: " + request.getCouponCode()));
            if (!coupon.isActive() || LocalDateTime.now().isAfter(coupon.getExpiryDate())) {
                throw new BadRequestException("Coupon '" + request.getCouponCode() + "' is expired or inactive.");
            }
            if (itemTotal.compareTo(coupon.getMinOrderAmount()) < 0) {
                throw new BadRequestException("Minimum order amount for coupon is " + coupon.getMinOrderAmount());
            }
            discountAmount = itemTotal.multiply(coupon.getDiscountPercentage()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (coupon.getMaxDiscountAmount() != null && discountAmount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                discountAmount = coupon.getMaxDiscountAmount();
            }
            couponCode = coupon.getCode();
        }

        // Custom Discount
        if (request.getCustomDiscount() != null && request.getCustomDiscount().compareTo(BigDecimal.ZERO) > 0) {
            discountAmount = discountAmount.add(request.getCustomDiscount());
        }

        // Grand total before round-off
        BigDecimal beforeRoundOff = itemTotal.add(gstAmount).add(serviceCharge).subtract(discountAmount);
        BigDecimal grandTotal = beforeRoundOff.setScale(0, RoundingMode.HALF_UP);
        BigDecimal roundOff = grandTotal.subtract(beforeRoundOff);

        String invoiceNumber = "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Bill bill = Bill.builder()
                .invoiceNumber(invoiceNumber)
                .order(order)
                .customer(order.getCustomer())
                .cashier(cashier)
                .itemTotal(itemTotal)
                .gstAmount(gstAmount)
                .serviceCharge(serviceCharge)
                .discountAmount(discountAmount)
                .couponCode(couponCode)
                .roundOff(roundOff)
                .grandTotal(grandTotal)
                .status(BillStatus.UNPAID)
                .generatedAt(LocalDateTime.now())
                .build();

        // Generate PDF
        try {
            byte[] pdfBytes = pdfInvoiceGenerator.generateInvoicePdf(bill);
            // Send invoice PDF via email asynchronously
            notificationService.sendInvoiceEmail(
                    order.getCustomer().getEmail(),
                    order.getCustomer().getFullName(),
                    invoiceNumber,
                    pdfBytes
            );
        } catch (Exception e) {
            log.error("Failed to generate PDF for invoice {}: {}", invoiceNumber, e.getMessage());
        }

        // Update order status to BILLED
        order.setStatus(OrderStatus.BILLED);
        orderRepository.save(order);

        Bill saved = billRepository.save(bill);

        auditService.logAction(cashier.getUsername(), "ROLE_CASHIER", "BILL_GENERATED", "BILLING",
                "Bill #" + invoiceNumber + " generated for Order #" + order.getOrderNumber()
                        + ". Grand Total: " + grandTotal, ipAddress);

        return mapToBillResponse(saved);
    }

    @Transactional(readOnly = true)
    public BillDto.BillResponse getBillById(Long billId) {
        return mapToBillResponse(billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "id", billId)));
    }

    @Transactional(readOnly = true)
    public BillDto.BillResponse getBillByOrderId(Long orderId) {
        return mapToBillResponse(billRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "orderId", orderId)));
    }

    @Transactional(readOnly = true)
    public Page<BillDto.BillResponse> getAllBills(Pageable pageable) {
        return billRepository.findAll(pageable).map(this::mapToBillResponse);
    }

    @Transactional(readOnly = true)
    public Page<BillDto.BillResponse> getBillsByCustomer(Long customerId, Pageable pageable) {
        return billRepository.findByCustomerId(customerId, pageable).map(this::mapToBillResponse);
    }

    @Transactional
    public BillDto.BillResponse markBillAsPaid(Long billId, String performedBy, String ipAddress) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "id", billId));
        if (bill.getStatus() == BillStatus.PAID) {
            throw new BadRequestException("Bill is already paid.");
        }
        bill.setStatus(BillStatus.PAID);
        bill.getOrder().setStatus(OrderStatus.PAID);
        orderRepository.save(bill.getOrder());
        Bill saved = billRepository.save(bill);

        auditService.logAction(performedBy, "ROLE_CASHIER", "BILL_PAID", "BILLING",
                "Bill #" + bill.getInvoiceNumber() + " marked as PAID.", ipAddress);
        return mapToBillResponse(saved);
    }

    private BillDto.BillResponse mapToBillResponse(Bill bill) {
        BillDto.BillResponse res = new BillDto.BillResponse();
        res.setId(bill.getId());
        res.setInvoiceNumber(bill.getInvoiceNumber());
        res.setOrderId(bill.getOrder().getId());
        res.setOrderNumber(bill.getOrder().getOrderNumber());
        res.setCustomerId(bill.getCustomer().getId());
        res.setCustomerName(bill.getCustomer().getFullName());
        res.setCustomerMobile(bill.getCustomer().getMobileNumber());
        if (bill.getCashier() != null) {
            res.setCashierId(bill.getCashier().getId());
            res.setCashierName(bill.getCashier().getFullName());
        }
        res.setItemTotal(bill.getItemTotal());
        res.setGstAmount(bill.getGstAmount());
        res.setServiceCharge(bill.getServiceCharge());
        res.setDiscountAmount(bill.getDiscountAmount());
        res.setCouponCode(bill.getCouponCode());
        res.setRoundOff(bill.getRoundOff());
        res.setGrandTotal(bill.getGrandTotal());
        res.setStatus(bill.getStatus());
        res.setPdfFilePath(bill.getPdfFilePath());
        res.setGeneratedAt(bill.getGeneratedAt());
        return res;
    }
}
