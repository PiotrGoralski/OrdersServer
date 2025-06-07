package goralski.piotr.com.orders.service;

import goralski.piotr.com.orders.kafka.KafkaMessageProducer;
import goralski.piotr.com.orders.model.Order;
import goralski.piotr.com.orders.model.User;
import goralski.piotr.com.orders.model.dto.OrderCreationRequestDTO;
import goralski.piotr.com.orders.model.dto.OrderDTO;
import goralski.piotr.com.orders.model.enums.OrderStatus;
import goralski.piotr.com.orders.repository.OrderRepository;
import goralski.piotr.com.orders.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static goralski.piotr.com.orders.kafka.KafkaMessageProducer.CLOSED_ORDERS_TOPIC;
import static goralski.piotr.com.orders.kafka.KafkaMessageProducer.CREATED_ORDERS_TOPIC;

@Service
@AllArgsConstructor
public class OrderService {

    private final KafkaMessageProducer kafkaMessageProducer;

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;

    public Order createOrder(OrderCreationRequestDTO orderData) {
        User user = userRepository.findById(orderData.getUserId()).orElseThrow();

        Order order = new Order(orderData.getDescription(), user);
        order = orderRepository.save(order);

        kafkaMessageProducer.sendMessage(
            CREATED_ORDERS_TOPIC,
            order.getId().toString(),
            kafkaMessageProducer.parseOrderToStringMessage(new OrderDTO(order))
        );

        return order;
    }

    public Order closeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        if(order.getStatus().equals(OrderStatus.CLOSED)) {
            throw new RuntimeException("Order " + order.getId() + " is already closed");
        }

        order.setStatus(OrderStatus.CLOSED);
        order = orderRepository.save(order);

        kafkaMessageProducer.sendMessage(
            CLOSED_ORDERS_TOPIC,
            orderId.toString(),
            kafkaMessageProducer.parseOrderToStringMessage(new OrderDTO(order))
        );

        return order;
    }

    public List<Order> fetchAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> fetchActiveOrders() {
        return orderRepository.findAllByStatus(OrderStatus.CREATED);
    }

    public List<Order> fetchOrdersCreatedBetween(LocalDateTime dateFrom, LocalDateTime dateTo) {
        return orderRepository.findAllByCreationDateBetween(dateFrom, dateTo);
    }
}
