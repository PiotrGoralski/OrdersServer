package goralski.piotr.com.orders.controller;

import goralski.piotr.com.orders.model.dto.OrderCreationRequestDTO;
import goralski.piotr.com.orders.model.dto.OrderDTO;
import goralski.piotr.com.orders.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/active")
    public ResponseEntity<List<OrderDTO>> getActiveOrders() {
        return ResponseEntity.ok(orderService.fetchActiveOrders().stream().map(OrderDTO::new).toList());
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody @Valid OrderCreationRequestDTO requestData) {
        return ResponseEntity.ok(new OrderDTO(orderService.createOrder(requestData)));
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getOrders(@RequestParam(required = false) LocalDateTime dateFrom, @RequestParam(required = false) LocalDateTime dateTo) {
        if(Objects.isNull(dateFrom) && Objects.isNull(dateTo)) {
            return ResponseEntity.ok(orderService.fetchAllOrders().stream().map(OrderDTO::new).toList());
        }

        return ResponseEntity.ok(orderService.fetchOrdersCreatedBetween(dateFrom, dateTo).stream().map(OrderDTO::new).toList());
    }

    @PutMapping("/{orderId}/close")
    public ResponseEntity<OrderDTO> closeOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(new OrderDTO(orderService.closeOrder(orderId)));
    }
}