package com.restaurant.backend.controller;

import com.restaurant.backend.dto.request.BillDto;
import com.restaurant.backend.dto.response.ApiResponse;
import com.restaurant.backend.dto.response.PageResponse;
import com.restaurant.backend.security.UserPrincipal;
import com.restaurant.backend.service.BillService;
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
@RequestMapping("/api/v1/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    public ResponseEntity<ApiResponse<BillDto.BillResponse>> generateBill(
            @Valid @RequestBody BillDto.GenerateBillRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        return new ResponseEntity<>(ApiResponse.success("Bill generated successfully", 
                billService.generateBill(request, currentUser.getId(), ipAddress)), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER', 'ROLE_CUSTOMER')")
    public ResponseEntity<ApiResponse<BillDto.BillResponse>> getBillById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Bill fetched successfully", billService.getBillById(id)));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER', 'ROLE_CUSTOMER')")
    public ResponseEntity<ApiResponse<BillDto.BillResponse>> getBillByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success("Bill fetched successfully", billService.getBillByOrderId(orderId)));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    public ResponseEntity<ApiResponse<PageResponse<BillDto.BillResponse>>> getAllBills(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "generatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Page<BillDto.BillResponse> billPage = billService.getAllBills(PageRequest.of(page, size, sort));

        PageResponse<BillDto.BillResponse> response = new PageResponse<>(
                billPage.getContent(), billPage.getNumber(), billPage.getSize(),
                billPage.getTotalElements(), billPage.getTotalPages(), billPage.isLast()
        );
        return ResponseEntity.ok(ApiResponse.success("Bills fetched successfully", response));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER') or @securityService.isCurrentUser(#customerId)")
    public ResponseEntity<ApiResponse<PageResponse<BillDto.BillResponse>>> getBillsByCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<BillDto.BillResponse> billPage = billService.getBillsByCustomer(customerId, PageRequest.of(page, size, Sort.by("generatedAt").descending()));
        PageResponse<BillDto.BillResponse> response = new PageResponse<>(
                billPage.getContent(), billPage.getNumber(), billPage.getSize(),
                billPage.getTotalElements(), billPage.getTotalPages(), billPage.isLast()
        );
        return ResponseEntity.ok(ApiResponse.success("Bills fetched successfully", response));
    }

    @PostMapping("/{id}/mark-paid")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    public ResponseEntity<ApiResponse<BillDto.BillResponse>> markBillAsPaid(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Bill marked as paid", 
                billService.markBillAsPaid(id, currentUser.getUsername(), ipAddress)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    public ResponseEntity<ApiResponse<Void>> deleteBill(@PathVariable Long id) {
        billService.deleteBill(id);
        return ResponseEntity.ok(ApiResponse.success("Bill deleted successfully", (Void) null));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
