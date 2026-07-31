package com.restaurant.backend.service;

import com.restaurant.backend.dto.request.PaymentDto;
import com.restaurant.backend.entity.*;
import com.restaurant.backend.enums.BillStatus;
import com.restaurant.backend.enums.OrderStatus;
import com.restaurant.backend.enums.PaymentStatus;
import com.restaurant.backend.exception.BadRequestException;
import com.restaurant.backend.exception.ResourceNotFoundException;
import com.restaurant.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public PaymentDto.PaymentResponse processPayment(PaymentDto.ProcessPaymentRequest request, Long cashierId, String ipAddress) {
        Bill bill = billRepository.findById(request.getBillId())
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "id", request.getBillId()));

        if (bill.getStatus() == BillStatus.PAID) {
            throw new BadRequestException("Bill is already paid.");
        }
        if (bill.getStatus() == BillStatus.CANCELLED) {
            throw new BadRequestException("Bill is cancelled and cannot be paid.");
        }

        User cashier = null;
        if (cashierId != null) {
            cashier = userRepository.findById(cashierId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", cashierId));
        }

        Payment payment = Payment.builder()
                .bill(bill)
                .order(bill.getOrder())
                .cashier(cashier)
                .paymentMethod(request.getPaymentMethod())
                .transactionId(request.getTransactionId())
                .referenceNumber(request.getReferenceNumber())
                .amount(bill.getGrandTotal())
                .status(PaymentStatus.SUCCESS)
                .paymentTime(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);

        // Update bill status
        bill.setStatus(BillStatus.PAID);
        billRepository.save(bill);

        // Update order status to PAID
        Order order = bill.getOrder();
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        auditService.logAction(cashier != null ? cashier.getUsername() : "SYSTEM", "ROLE_CASHIER",
                "PAYMENT_PROCESSED", "PAYMENT",
                "Payment of " + bill.getGrandTotal() + " via " + request.getPaymentMethod()
                        + " for Invoice #" + bill.getInvoiceNumber(), ipAddress);

        return mapToPaymentResponse(saved);
    }

    @Transactional(readOnly = true)
    public PaymentDto.PaymentResponse getPaymentById(Long paymentId) {
        return mapToPaymentResponse(paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId)));
    }

    @Transactional(readOnly = true)
    public PaymentDto.PaymentResponse getPaymentByBillId(Long billId) {
        return mapToPaymentResponse(paymentRepository.findByBillId(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "billId", billId)));
    }

    @Transactional(readOnly = true)
    public Page<PaymentDto.PaymentResponse> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(this::mapToPaymentResponse);
    }

    private PaymentDto.PaymentResponse mapToPaymentResponse(Payment payment) {
        PaymentDto.PaymentResponse res = new PaymentDto.PaymentResponse();
        res.setId(payment.getId());
        res.setBillId(payment.getBill().getId());
        res.setInvoiceNumber(payment.getBill().getInvoiceNumber());
        res.setOrderId(payment.getOrder().getId());
        if (payment.getCashier() != null) {
            res.setCashierName(payment.getCashier().getFullName());
        }
        res.setPaymentMethod(payment.getPaymentMethod());
        res.setTransactionId(payment.getTransactionId());
        res.setReferenceNumber(payment.getReferenceNumber());
        res.setAmount(payment.getAmount());
        res.setStatus(payment.getStatus());
        res.setPaymentTime(payment.getPaymentTime());
        return res;
    }
}
