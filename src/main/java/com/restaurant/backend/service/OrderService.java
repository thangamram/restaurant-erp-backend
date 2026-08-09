package com.restaurant.backend.service;

import com.restaurant.backend.dto.request.OrderDto;
import com.restaurant.backend.entity.*;
import com.restaurant.backend.enums.OrderItemStatus;
import com.restaurant.backend.enums.OrderStatus;
import com.restaurant.backend.enums.TableStatus;
import com.restaurant.backend.exception.BadRequestException;
import com.restaurant.backend.exception.ResourceNotFoundException;
import com.restaurant.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantTableRepository tableRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final InventoryService inventoryService;

    @Transactional
    public OrderDto.OrderResponse placeOrder(Long customerId, OrderDto.CreateOrderRequest request, String ipAddress) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", customerId));

        RestaurantTable table = null;
        if (request.getTableId() != null) {
            table = tableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Table", "id", request.getTableId()));
            if (table.getStatus() == TableStatus.OCCUPIED) {
                // Allow re-ordering on occupied table
            }
            table.setStatus(TableStatus.OCCUPIED);
            tableRepository.save(table);
        }

        Order order = Order.builder()
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customer(customer)
                .table(table)
                .status(OrderStatus.NEW)
                .orderType(request.getOrderType())
                .remarks(request.getRemarks())
                .items(new ArrayList<>())
                .placedAt(LocalDateTime.now())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderDto.OrderItemRequest itemReq : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", itemReq.getMenuItemId()));

            if (!menuItem.isAvailable() || menuItem.isDeleted()) {
                throw new BadRequestException("Menu item '" + menuItem.getName() + "' is not available.");
            }

            BigDecimal itemTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .itemName(menuItem.getName())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(menuItem.getPrice())
                    .subtotal(itemTotal)
                    .gstPercentage(menuItem.getGstPercentage())
                    .specialInstructions(itemReq.getSpecialInstructions())
                    .status(OrderItemStatus.PENDING)
                    .build();
            order.getItems().add(orderItem);
        }

        order.setTotalAmount(totalAmount);
        Order saved = orderRepository.save(order);

        // Clear customer's cart
        cartRepository.findByCustomerId(customerId).ifPresent(cart -> {
            cartItemRepository.deleteByCartId(cart.getId());
            cart.getItems().clear();
            cartRepository.save(cart);
        });

        OrderDto.OrderResponse response = mapToOrderResponse(saved);
        notificationService.notifyKitchen(response);
        notificationService.notifyCustomer(customerId, response);
        auditService.logAction(customer.getUsername(), "ROLE_CUSTOMER", "ORDER_PLACED", "ORDER",
                "Order #" + saved.getOrderNumber() + " placed with " + request.getItems().size() + " items.", ipAddress);

        return response;
    }

    @Transactional
    public OrderDto.OrderResponse updateOrderStatus(Long orderId, OrderDto.UpdateOrderStatusRequest request, String performedBy, String ipAddress) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        OrderStatus newStatus = request.getStatus();
        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);

        if (request.getRemarks() != null) {
            order.setRemarks(request.getRemarks());
        }

        if (request.getWaiterId() != null && newStatus == OrderStatus.DELIVERED) {
            userRepository.findById(request.getWaiterId()).ifPresent(order::setWaiter);
            order.setDeliveredAt(LocalDateTime.now());
        }

        if (newStatus == OrderStatus.CLOSED) {
            order.setClosedAt(LocalDateTime.now());
            // Auto-deduct inventory
            inventoryService.deductInventoryForOrder(order);
        }

        // Update order items status in bulk
        if (newStatus == OrderStatus.PREPARING) {
            order.getItems().forEach(item -> {
                if (item.getStatus() == OrderItemStatus.PENDING) {
                    item.setStatus(OrderItemStatus.PREPARING);
                }
            });
        } else if (newStatus == OrderStatus.READY) {
            order.getItems().forEach(item -> {
                if (item.getStatus() == OrderItemStatus.PREPARING) {
                    item.setStatus(OrderItemStatus.READY);
                }
            });
        } else if (newStatus == OrderStatus.DELIVERED) {
            order.getItems().forEach(item -> {
                if (item.getStatus() == OrderItemStatus.READY) {
                    item.setStatus(OrderItemStatus.DELIVERED);
                }
            });
        }

        // Free the table if order is closed/cancelled
        if ((newStatus == OrderStatus.CLOSED || newStatus == OrderStatus.CANCELLED) && order.getTable() != null) {
            RestaurantTable table = order.getTable();
            boolean hasActiveOrders = orderRepository.existsByTableIdAndStatusNotIn(
                    table.getId(), List.of(OrderStatus.CLOSED, OrderStatus.CANCELLED, OrderStatus.PAID));
            if (!hasActiveOrders) {
                table.setStatus(TableStatus.CLEANING);
                tableRepository.save(table);
            }
        }

        Order updated = orderRepository.save(order);
        OrderDto.OrderResponse response = mapToOrderResponse(updated);

        // WebSocket Notifications
        switch (newStatus) {
            case RECEIVED, PREPARING, READY -> notificationService.notifyKitchen(response);
            case DELIVERED -> notificationService.notifyWaiter(response);
            case BILLED, PAID -> notificationService.notifyCashier(response);
            default -> {}
        }
        notificationService.notifyCustomer(order.getCustomer().getId(), response);

        auditService.logAction(performedBy, "STAFF", "ORDER_STATUS_UPDATE", "ORDER",
                "Order #" + order.getOrderNumber() + " status changed to " + newStatus, ipAddress);

        return response;
    }

    @Transactional(readOnly = true)
    public OrderDto.OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return mapToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDto.OrderResponse> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByPlacedAtDesc(customerId).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<OrderDto.OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::mapToOrderResponse);
    }

    @Transactional(readOnly = true)
    public List<OrderDto.OrderResponse> getActiveOrders() {
        return orderRepository.findByStatusNotIn(List.of(OrderStatus.CLOSED, OrderStatus.CANCELLED, OrderStatus.PAID)).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderDto.OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatusOrderByPlacedAtDesc(status).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDto.OrderResponse cancelOrder(Long orderId, String performedBy, String ipAddress) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.BILLED
                || order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.CLOSED) {
            throw new BadRequestException("Cannot cancel order that has been delivered or paid.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.getItems().forEach(item -> item.setStatus(OrderItemStatus.CANCELLED));

        if (order.getTable() != null) {
            order.getTable().setStatus(TableStatus.CLEANING);
            tableRepository.save(order.getTable());
        }

        Order updated = orderRepository.save(order);
        OrderDto.OrderResponse response = mapToOrderResponse(updated);
        notificationService.notifyCustomer(order.getCustomer().getId(), response);
        auditService.logAction(performedBy, "STAFF", "ORDER_CANCELLED", "ORDER",
                "Order #" + order.getOrderNumber() + " cancelled.", ipAddress);

        return response;
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case NEW -> next == OrderStatus.RECEIVED || next == OrderStatus.PREPARING || next == OrderStatus.CANCELLED;
            case RECEIVED -> next == OrderStatus.PREPARING || next == OrderStatus.CANCELLED;
            case PREPARING -> next == OrderStatus.READY || next == OrderStatus.CANCELLED;
            case READY -> next == OrderStatus.DELIVERED || next == OrderStatus.SERVED;
            case SERVED -> next == OrderStatus.DELIVERED || next == OrderStatus.BILLED;
            case DELIVERED -> next == OrderStatus.BILLED;
            case BILLED -> next == OrderStatus.PAID;
            case PAID -> next == OrderStatus.CLOSED;
            default -> false;
        };
        if (!valid) {
            throw new BadRequestException("Invalid status transition from " + current + " to " + next);
        }
    }

    public OrderDto.OrderResponse mapToOrderResponse(Order order) {
        OrderDto.OrderResponse res = new OrderDto.OrderResponse();
        res.setId(order.getId());
        res.setOrderNumber(order.getOrderNumber());
        res.setCustomerId(order.getCustomer().getId());
        res.setCustomerName(order.getCustomer().getFullName());
        res.setCustomerMobile(order.getCustomer().getMobileNumber());
        res.setStatus(order.getStatus());
        res.setOrderType(order.getOrderType());
        res.setTotalAmount(order.getTotalAmount());
        res.setRemarks(order.getRemarks());
        res.setPlacedAt(order.getPlacedAt());
        res.setDeliveredAt(order.getDeliveredAt());
        res.setClosedAt(order.getClosedAt());

        if (order.getTable() != null) {
            res.setTableId(order.getTable().getId());
            res.setTableNumber(order.getTable().getTableNumber());
        }
        if (order.getWaiter() != null) {
            res.setWaiterId(order.getWaiter().getId());
            res.setWaiterName(order.getWaiter().getFullName());
        }

        if (order.getItems() != null) {
            List<OrderDto.OrderItemResponse> itemResponses = order.getItems().stream().map(item -> {
                OrderDto.OrderItemResponse ir = new OrderDto.OrderItemResponse();
                ir.setId(item.getId());
                ir.setMenuItemId(item.getMenuItem().getId());
                ir.setItemName(item.getMenuItem().getName());
                ir.setQuantity(item.getQuantity());
                ir.setUnitPrice(item.getUnitPrice());
                ir.setSubtotal(item.getSubtotal());
                ir.setSpecialInstructions(item.getSpecialInstructions());
                ir.setStatus(item.getStatus());
                return ir;
            }).collect(Collectors.toList());
            res.setItems(itemResponses);
        }
        return res;
    }
}
/ /   T r i g g e r   R a i l w a y   D e p l o y  
 