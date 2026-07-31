package com.restaurant.backend.service;

import com.restaurant.backend.dto.response.ReportDto;
import com.restaurant.backend.enums.PaymentMethod;
import com.restaurant.backend.repository.BillRepository;
import com.restaurant.backend.repository.OrderItemRepository;
import com.restaurant.backend.repository.OrderRepository;
import com.restaurant.backend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public ReportDto generateSalesReport(LocalDateTime startDate, LocalDateTime endDate) {
        long totalOrders = orderRepository.countOrdersBetween(startDate, endDate);
        Double totalRevenueDouble = orderRepository.sumTotalRevenueBetween(startDate, endDate);
        BigDecimal totalRevenue = totalRevenueDouble != null ? BigDecimal.valueOf(totalRevenueDouble) : BigDecimal.ZERO;

        Double totalGstDouble = billRepository.sumGstCollectedBetween(startDate, endDate);
        BigDecimal totalGst = totalGstDouble != null ? BigDecimal.valueOf(totalGstDouble) : BigDecimal.ZERO;

        List<Object[]> popularItemsData = orderItemRepository.findPopularItems(startDate, endDate);
        List<ReportDto.PopularItemDto> popularItems = popularItemsData.stream()
                .map(data -> new ReportDto.PopularItemDto((String) data[0], ((Number) data[1]).longValue()))
                .limit(10) // Top 10
                .collect(Collectors.toList());

        List<Object[]> revenueByMethodData = paymentRepository.sumRevenueByPaymentMethod(startDate, endDate);
        Map<String, BigDecimal> revenueByMethod = new HashMap<>();
        for (Object[] row : revenueByMethodData) {
            PaymentMethod method = (PaymentMethod) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            revenueByMethod.put(method.name(), amount);
        }

        return ReportDto.builder()
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .totalGstCollected(totalGst)
                .popularItems(popularItems)
                .revenueByPaymentMethod(revenueByMethod)
                .build();
    }
}
