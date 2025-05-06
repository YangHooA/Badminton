package com.example.Badminton.service;

import com.example.Badminton.dto.OrderDTO;
import com.example.Badminton.entity.Order;
import com.example.Badminton.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Đơn hàng không tìm thấy"));
        return convertToDTO(order);
    }

    public Page<OrderDTO> getOrdersByStatus(String status, String keyword, Pageable pageable) {
        Page<Order> orders;
        if (status != null && !status.isEmpty()) {
            try {
                // Chuẩn hóa trạng thái: pending → Pending
                String normalizedStatus = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
                Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(normalizedStatus);
                orders = orderRepository.searchOrders(keyword, orderStatus, pageable);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái không hợp lệ: " + status);
            }
        } else {
            orders = orderRepository.searchOrders(keyword, null, pageable);
        }
        return orders.map(this::convertToDTO);
    }

    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Đơn hàng không tìm thấy");
        }
        orderRepository.deleteById(id);
    }

    private OrderDTO convertToDTO(Order order) {
        String customerName = order.getCustomer() != null ? order.getCustomer().getName() : (order.getGuestName() != null ? order.getGuestName() : "N/A");
        return new OrderDTO(
                order.getId(),
                customerName,
                order.getGuestName() != null ? order.getGuestName() : "N/A",
                order.getCreatedAt(),
                order.getTotalPrice(),
                order.getStatus() != null ? order.getStatus().toString() : "N/A"
        );
    }
}