package com.restaurant.backend.controller;

import com.restaurant.backend.dto.request.PaymentDto;
import com.restaurant.backend.dto.response.ApiResponse;
import com.restaurant.backend.dto.response.PageResponse;
import com.restaurant.backend.security.UserPrincipal;
import com.restaurant.backend.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    public ResponseEntity<ApiResponse<PaymentDto.PaymentResponse>> processPayment(
            @Valid @RequestBody PaymentDto.ProcessPaymentRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        return new ResponseEntity<>(ApiResponse.success("Payment processed successfully", 
                paymentService.processPayment(request, currentUser.getId(), ipAddress)), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    public ResponseEntity<ApiResponse<PaymentDto.PaymentResponse>> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Payment fetched successfully", paymentService.getPaymentById(id)));
    }

    @GetMapping("/bill/{billId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    public ResponseEntity<ApiResponse<PaymentDto.PaymentResponse>> getPaymentByBillId(@PathVariable Long billId) {
        return ResponseEntity.ok(ApiResponse.success("Payment fetched successfully", paymentService.getPaymentByBillId(billId)));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    public ResponseEntity<ApiResponse<PageResponse<PaymentDto.PaymentResponse>>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "paymentTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Page<PaymentDto.PaymentResponse> paymentPage = paymentService.getAllPayments(PageRequest.of(page, size, sort));

        PageResponse<PaymentDto.PaymentResponse> response = new PageResponse<>(
                paymentPage.getContent(), paymentPage.getNumber(), paymentPage.getSize(),
                paymentPage.getTotalElements(), paymentPage.getTotalPages(), paymentPage.isLast()
        );
        return ResponseEntity.ok(ApiResponse.success("Payments fetched successfully", response));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
