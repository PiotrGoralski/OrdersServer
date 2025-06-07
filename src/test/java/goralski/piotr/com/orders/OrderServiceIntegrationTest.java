package goralski.piotr.com.orders;

import goralski.piotr.com.orders.kafka.KafkaMessageProducer;
import goralski.piotr.com.orders.model.User;
import goralski.piotr.com.orders.repository.OrderRepository;
import goralski.piotr.com.orders.repository.UserRepository;
import goralski.piotr.com.orders.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import goralski.piotr.com.orders.model.dto.*;
import goralski.piotr.com.orders.model.enums.*;
import goralski.piotr.com.orders.model.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private KafkaMessageProducer kafkaMessageProducer;

    private User savedUser;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        userRepository.deleteAll();

        savedUser = new User();
        savedUser.setUsername("test user name");
        savedUser = userRepository.save(savedUser);

        when(kafkaMessageProducer.parseOrderToStringMessage(any(OrderDTO.class))).thenReturn("test json data");
    }

    @Test
    @DisplayName("Should create an order and store it in the DB")
    void shouldCreateOrderAndPersist() {
        // Given
        OrderCreationRequestDTO orderData = new OrderCreationRequestDTO("Zamówienie Testowe Integracyjne", savedUser.getId());

        // When
        Order createdOrder = orderService.createOrder(orderData);

        // Then
        assertNotNull(createdOrder.getId());
        assertEquals(orderData.getDescription(), createdOrder.getDescription());
        assertEquals(savedUser.getId(), createdOrder.getUser().getId());
        assertEquals(OrderStatus.CREATED, createdOrder.getStatus());
        assertNotNull(createdOrder.getCreationDate());

        Optional<Order> foundOrder = orderRepository.findById(createdOrder.getId());
        assertTrue(foundOrder.isPresent());
        assertEquals(createdOrder.getDescription(), foundOrder.get().getDescription());
        assertNotNull(foundOrder.get().getCreationDate());

        verify(kafkaMessageProducer, times(1)).sendMessage(
                eq("created-orders"),
                eq(createdOrder.getId().toString()),
                anyString()
        );
        verify(kafkaMessageProducer, times(1)).parseOrderToStringMessage(any(OrderDTO.class));
    }

    @Test
    @DisplayName("Should throw exception when user was not found")
    void shouldThrowExceptionWhenUserNotFoundDuringCreation() {
        // Given
        OrderCreationRequestDTO orderData = new OrderCreationRequestDTO("order description", 0L);

        // When
        assertThrows(RuntimeException.class, () -> orderService.createOrder(orderData));

        // Then
        assertEquals(0, orderRepository.count());
        verify(kafkaMessageProducer, times(0)).sendMessage(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should close order and update DB")
    void shouldCloseOrderAndUpdateStatus() {
        // Given
        Order orderToClose = new Order("order to closed description", savedUser);
        orderToClose = orderRepository.save(orderToClose);

        // When
        Order closedOrder = orderService.closeOrder(orderToClose.getId());

        // Then
        assertNotNull(closedOrder);
        assertEquals(orderToClose.getId(), closedOrder.getId());
        assertEquals(OrderStatus.CLOSED, closedOrder.getStatus());

        Optional<Order> dbOrder = orderRepository.findById(orderToClose.getId());
        assertTrue(dbOrder.isPresent());
        assertEquals(OrderStatus.CLOSED, dbOrder.get().getStatus());

        verify(kafkaMessageProducer, times(1)).sendMessage(
                eq("closed-orders"),
                eq(orderToClose.getId().toString()),
                anyString()
        );
        verify(kafkaMessageProducer, times(1)).parseOrderToStringMessage(any(OrderDTO.class));
    }

    @Test
    @DisplayName("Should throw exception if order is already closed")
    void shouldThrowExceptionWhenOrderAlreadyClosed() {
        // Given
        Order alreadyClosedOrder = new Order("closed order description", savedUser);
        alreadyClosedOrder.setStatus(OrderStatus.CLOSED);
        alreadyClosedOrder = orderRepository.save(alreadyClosedOrder);

        // When
        Order finalAlreadyClosedOrder = alreadyClosedOrder;
        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.closeOrder(finalAlreadyClosedOrder.getId()));

        // Then
        assertEquals("Order " + alreadyClosedOrder.getId() + " is already closed", exception.getMessage());

        verify(kafkaMessageProducer, times(0)).sendMessage(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception if order was not found")
    void shouldThrowExceptionWhenOrderNotFoundDuringClosing() {
        // Given
        Long nonExistentOrderId = 0L;

        // When
        assertThrows(RuntimeException.class, () -> orderService.closeOrder(nonExistentOrderId));

        // Then
        verify(kafkaMessageProducer, times(0)).sendMessage(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should fetch all orders from DB")
    void shouldFetchAllOrdersFromDB() {
        // Given
        orderRepository.save(new Order("Order 1", savedUser));
        orderRepository.save(new Order("Order 2", savedUser));

        // When
        List<Order> orders = orderService.fetchAllOrders();

        // Then
        assertNotNull(orders);
        assertEquals(2, orders.size());
    }

    @Test
    @DisplayName("Should fetch all active orders from DB")
    void shouldFetchActiveOrdersFromDB() {
        // Given
        orderRepository.save(new Order("Active order 1", savedUser));
        orderRepository.save(new Order("Active order 2", savedUser));

        Order closedOrder = new Order("closed order 1", savedUser);
        closedOrder.setStatus(OrderStatus.CLOSED);
        orderRepository.save(closedOrder);

        // When
        List<Order> activeOrders = orderService.fetchActiveOrders();

        // Then
        assertNotNull(activeOrders);
        assertEquals(2, activeOrders.size());
        assertTrue(activeOrders.stream().allMatch(o -> o.getStatus() == OrderStatus.CREATED));
    }

    @Test
    @DisplayName("Should fetch orders created between")
    void shouldFetchOrdersCreatedBetweenDatesFromDB() {
        // Given
        Order orderBeforeRange = new Order("Order before proper range", savedUser);
        orderBeforeRange.setCreationDate(LocalDateTime.now().minusDays(1));
        orderRepository.save(orderBeforeRange);

        Order orderInRange1 = new Order("Order 1 in proper range", savedUser);
        orderInRange1.setCreationDate(LocalDateTime.now());
        orderRepository.save(orderInRange1);

        Order orderInRange2 = new Order("Order 2 in proper range", savedUser);
        orderInRange2.setCreationDate(LocalDateTime.now());
        orderRepository.save(orderInRange2);

        Order orderAfterRange = new Order("Order after proper range", savedUser);
        orderAfterRange.setCreationDate(LocalDateTime.now().plusDays(1));
        orderRepository.save(orderAfterRange);

        LocalDateTime dateFrom = LocalDateTime.now().minusHours(1);
        LocalDateTime dateTo = LocalDateTime.now().plusHours(1);

        // When
        List<Order> ordersInRange = orderService.fetchOrdersCreatedBetween(dateFrom, dateTo);

        // Then
        assertNotNull(ordersInRange);
        assertEquals(2, ordersInRange.size());
        assertTrue(ordersInRange.stream().anyMatch(o -> o.getDescription().equals("Order 1 in proper range")));
        assertTrue(ordersInRange.stream().anyMatch(o -> o.getDescription().equals("Order 2 in proper range")));
    }
}