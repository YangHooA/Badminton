package com.example.Badminton.service;

import com.example.Badminton.entity.Order;
import com.example.Badminton.entity.OrderDetail;
import com.example.Badminton.repository.OrderDetailRepository;
import com.example.Badminton.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    public Map<String, Object> generateReport(String range, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start;
        LocalDateTime end = LocalDateTime.now();

        // Xác định khoảng thời gian
        switch (range) {
            case "day":
                start = end.toLocalDate().atStartOfDay();
                break;
            case "week":
                start = end.toLocalDate().atStartOfDay().minusDays(6);
                break;
            case "month":
                start = end.toLocalDate().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
                break;
            case "custom":
                if (startDate == null || endDate == null) {
                    throw new IllegalArgumentException("Vui lòng cung cấp ngày bắt đầu và kết thúc");
                }
                start = startDate.atStartOfDay();
                end = endDate.atTime(23, 59, 59);
                break;
            default:
                throw new IllegalArgumentException("Khoảng thời gian không hợp lệ");
        }

        // Lấy dữ liệu đơn hàng
        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);

        // Tính doanh thu theo ngày
        Map<LocalDate, BigDecimal> revenueByDate = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getCreatedAt().toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO, Order::getTotalPrice, BigDecimal::add)
                ));

        // Chuẩn bị dữ liệu cho biểu đồ doanh thu
        List<String> labels = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();
        LocalDate current = start.toLocalDate();
        while (!current.isAfter(end.toLocalDate())) {
            labels.add(current.toString());
            values.add(revenueByDate.getOrDefault(current, BigDecimal.ZERO));
            current = current.plusDays(1);
        }

        // Thống kê trạng thái đơn hàng
        List<Map<String, Object>> orderStatus = Arrays.stream(Order.OrderStatus.values())
                .map(status -> {
                    List<Order> statusOrders = orders.stream()
                            .filter(order -> order.getStatus() == status)
                            .collect(Collectors.toList());
                    Map<String, Object> statusData = new HashMap<>();
                    statusData.put("status", status.toString());
                    statusData.put("count", (long) statusOrders.size());
                    statusData.put("total", statusOrders.stream()
                            .map(Order::getTotalPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    return statusData;
                })
                .collect(Collectors.toList());

        // Thống kê sản phẩm bán chạy
        List<Map<String, Object>> topProducts = orderDetailRepository.findAllByOrderCreatedAtBetween(start, end)
                .stream()
                .collect(Collectors.groupingBy(
                        OrderDetail::getProduct,
                        Collectors.summingInt(OrderDetail::getQuantity)
                ))
                .entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", entry.getKey().getId());
                    map.put("name", entry.getKey().getName());
                    map.put("quantity", entry.getValue());
                    map.put("revenue", BigDecimal.valueOf(entry.getKey().getPrice())
                            .multiply(BigDecimal.valueOf(entry.getValue())));
                    return map;
                })
                .sorted((a, b) -> Integer.compare((Integer) b.get("quantity"), (Integer) a.get("quantity")))
                .limit(5)
                .collect(Collectors.toList());

        return Map.of(
                "success", true,
                "revenueData", Map.of("labels", labels, "values", values),
                "orderStatus", orderStatus,
                "topProducts", topProducts
        );
    }
}