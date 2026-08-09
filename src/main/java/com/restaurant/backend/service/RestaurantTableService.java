package com.restaurant.backend.service;

import com.restaurant.backend.dto.request.TableDto;
import com.restaurant.backend.entity.RestaurantTable;
import com.restaurant.backend.enums.TableStatus;
import com.restaurant.backend.exception.BadRequestException;
import com.restaurant.backend.exception.ResourceNotFoundException;
import com.restaurant.backend.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantTableService {

    private final RestaurantTableRepository tableRepository;

    @Transactional
    public TableDto.TableResponse createTable(TableDto.TableRequest request) {
        if (tableRepository.existsByTableNumber(request.getTableNumber())) {
            throw new BadRequestException("Table with number '" + request.getTableNumber() + "' already exists");
        }

        RestaurantTable table = RestaurantTable.builder()
                .tableNumber(request.getTableNumber())
                .capacity(request.getCapacity())
                .status(request.getStatus() != null ? request.getStatus() : TableStatus.AVAILABLE)
                .locationSection(request.getLocationSection())
                .build();

        return mapToResponse(tableRepository.save(table));
    }

    @Transactional
    public TableDto.TableResponse updateTable(Long id, TableDto.TableRequest request) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RestaurantTable", "id", id));
        
        table.setTableNumber(request.getTableNumber());
        table.setCapacity(request.getCapacity());
        if (request.getStatus() != null) {
            table.setStatus(request.getStatus());
        }
        table.setLocationSection(request.getLocationSection());
        if (request.getAssignedWaiter() != null) {
            table.setAssignedWaiter(request.getAssignedWaiter());
        }

        return mapToResponse(tableRepository.save(table));
    }

    @Transactional
    public TableDto.TableResponse updateTableStatus(Long id, TableStatus status) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RestaurantTable", "id", id));
        table.setStatus(status);
        return mapToResponse(tableRepository.save(table));
    }

    @Transactional(readOnly = true)
    public List<TableDto.TableResponse> getAllTables() {
        return tableRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TableDto.TableResponse> getTablesByStatus(TableStatus status) {
        return tableRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TableDto.TableResponse getTableById(Long id) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RestaurantTable", "id", id));
        return mapToResponse(table);
    }

    @Transactional
    public void deleteTable(Long id) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RestaurantTable", "id", id));
        tableRepository.delete(table);
    }

    private TableDto.TableResponse mapToResponse(RestaurantTable table) {
        TableDto.TableResponse res = new TableDto.TableResponse();
        res.setId(table.getId());
        res.setTableNumber(table.getTableNumber());
        res.setCapacity(table.getCapacity());
        res.setStatus(table.getStatus());
        res.setLocationSection(table.getLocationSection());
        res.setAssignedWaiter(table.getAssignedWaiter());
        return res;
    }
}
