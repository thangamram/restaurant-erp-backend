package com.restaurant.backend.controller;

import com.restaurant.backend.dto.request.TableDto;
import com.restaurant.backend.dto.response.ApiResponse;
import com.restaurant.backend.enums.TableStatus;
import com.restaurant.backend.service.RestaurantTableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
public class TableController {

    private final RestaurantTableService tableService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TableDto.TableResponse>>> getAllTables() {
        return ResponseEntity.ok(ApiResponse.success("Tables fetched successfully", tableService.getAllTables()));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<TableDto.TableResponse>>> getTablesByStatus(@PathVariable TableStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Tables fetched successfully", tableService.getTablesByStatus(status)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TableDto.TableResponse>> getTableById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Table fetched successfully", tableService.getTableById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<TableDto.TableResponse>> createTable(@Valid @RequestBody TableDto.TableRequest request) {
        return new ResponseEntity<>(ApiResponse.success("Table created successfully", tableService.createTable(request)), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<TableDto.TableResponse>> updateTable(
            @PathVariable Long id, @Valid @RequestBody TableDto.TableRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Table updated successfully", tableService.updateTable(id, request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_WAITER', 'ROLE_CASHIER', 'ROLE_CUSTOMER')")
    public ResponseEntity<ApiResponse<TableDto.TableResponse>> updateTableStatus(
            @PathVariable Long id, @RequestParam TableStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Table status updated", tableService.updateTableStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTable(@PathVariable Long id) {
        tableService.deleteTable(id);
        return ResponseEntity.ok(ApiResponse.success("Table deleted successfully"));
    }
}
