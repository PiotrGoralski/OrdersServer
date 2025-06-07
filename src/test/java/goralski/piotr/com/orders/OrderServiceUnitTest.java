package goralski.piotr.com.orders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import goralski.piotr.com.orders.kafka.KafkaMessageProducer;
import goralski.piotr.com.orders.model.Order;
import goralski.piotr.com.orders.model.User;
import goralski.piotr.com.orders.model.dto.OrderCreationRequestDTO;
import goralski.piotr.com.orders.model.dto.OrderDTO;
import goralski.piotr.com.orders.model.enums.OrderStatus;
import goralski.piotr.com.orders.repository.OrderRepository;
import goralski.piotr.com.orders.repository.UserRepository;
import goralski.piotr.com.orders.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {

    private static LocalDateTime NOW = LocalDateTime.now();

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private KafkaMessageProducer kafkaMessageProducer;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "testuser", List.of());
        testOrder = new Order(1L, NOW, OrderStatus.CREATED, "Test Description", testUser);

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Test
    @DisplayName("Should create an order successfully")
    void shouldCreateOrderSuccessfully() throws JsonProcessingException {
        // Given
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(kafkaMessageProducer.parseOrderToStringMessage(any(OrderDTO.class))).thenReturn(objectMapper.writeValueAsString(testOrder));

        // When
        Order createdOrder = orderService.createOrder(new OrderCreationRequestDTO(testOrder.getDescription(), testOrder.getUser().getId()));

        // Then
        assertNotNull(createdOrder);
        assertEquals(testOrder.getId(), createdOrder.getId());
        assertEquals(testOrder.getDescription(), createdOrder.getDescription());
        assertEquals(testUser, createdOrder.getUser());
        assertEquals(OrderStatus.CREATED, createdOrder.getStatus());
        assertEquals(NOW, createdOrder.getCreationDate());

        // Weryfikacja interakcji z mockami
        verify(userRepository, times(1)).findById(eq(1L));
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(kafkaMessageProducer, times(1)).sendMessage(
                eq("created-orders"),
                eq(testOrder.getId().toString()),
                anyString()
        );
        verify(kafkaMessageProducer, times(1)).parseOrderToStringMessage(any(OrderDTO.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found during order creation")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        assertThrows(RuntimeException.class, () -> orderService.createOrder(
            new OrderCreationRequestDTO("New order description", 1L)
        ));

        // Then
        verify(userRepository, times(1)).findById(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
        verify(kafkaMessageProducer, never()).sendMessage(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should close an order successfully")
    void shouldCloseOrderSuccessfully() throws JsonProcessingException {
        // Given
        Long orderId = testOrder.getId();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(kafkaMessageProducer.parseOrderToStringMessage(any(OrderDTO.class))).thenReturn(objectMapper.writeValueAsString(testOrder));

        // When
        Order closedOrder = orderService.closeOrder(orderId);

        // Then
        assertNotNull(closedOrder);
        assertEquals(orderId, closedOrder.getId());
        assertEquals(OrderStatus.CLOSED, closedOrder.getStatus());

        verify(orderRepository, times(1)).findById(eq(orderId));
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(kafkaMessageProducer, times(1)).sendMessage(
                eq("closed-orders"),
                eq(orderId.toString()),
                anyString()
        );
        verify(kafkaMessageProducer, times(1)).parseOrderToStringMessage(any(OrderDTO.class));
    }

    @Test
    @DisplayName("Should throw exception when trying to close an already closed order")
    void shouldThrowExceptionWhenOrderAlreadyClosed() {
        // Given
        Long orderId = testOrder.getId();
        testOrder.setStatus(OrderStatus.CLOSED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // When
        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.closeOrder(orderId));

        // Then
        assertEquals("Order " + orderId + " is already closed", exception.getMessage());

        verify(orderRepository, times(1)).findById(eq(orderId));
        verify(orderRepository, never()).save(any(Order.class));
        verify(kafkaMessageProducer, never()).sendMessage(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when order not found during closing")
    void shouldThrowExceptionWhenOrderNotFoundDuringClosing() {
        // Given
        when(orderRepository.findById(0L)).thenReturn(Optional.empty());

        // When
        assertThrows(RuntimeException.class, () -> orderService.closeOrder(0L));

        // Then
        verify(orderRepository, times(1)).findById(eq(0L));
        verify(orderRepository, never()).save(any(Order.class));
        verify(kafkaMessageProducer, never()).sendMessage(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should fetch all orders")
    void shouldFetchAllOrders() {
        // Given
        List<Order> orders = Arrays.asList(testOrder, new Order(2L, NOW, OrderStatus.CLOSED, "second order description", testUser));
        when(orderRepository.findAll()).thenReturn(orders);

        // When
        List<Order> fetchedOrders = orderService.fetchAllOrders();

        // Then
        assertNotNull(fetchedOrders);
        assertFalse(fetchedOrders.isEmpty());
        assertEquals(2, fetchedOrders.size());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should fetch active orders")
    void shouldFetchActiveOrders() {
        // Given
        List<Order> activeOrders = List.of(testOrder);
        when(orderRepository.findAllByStatus(OrderStatus.CREATED)).thenReturn(activeOrders);

        // When
        List<Order> fetchedActiveOrders = orderService.fetchActiveOrders();

        // Then
        assertNotNull(fetchedActiveOrders);
        assertFalse(fetchedActiveOrders.isEmpty());
        assertEquals(1, fetchedActiveOrders.size());
        assertEquals(OrderStatus.CREATED, fetchedActiveOrders.get(0).getStatus());
        verify(orderRepository, times(1)).findAllByStatus(eq(OrderStatus.CREATED));
    }

    @Test
    @DisplayName("Should fetch orders created between two dates")
    void shouldFetchOrdersCreatedBetweenDates() {
        // Given
        LocalDateTime from = NOW.minusMinutes(1);
        LocalDateTime to = NOW.plusMinutes(1);
        when(orderRepository.findAllByCreationDateBetween(from, to)).thenReturn(List.of(testOrder));

        // When
        List<Order> fetchedOrders = orderService.fetchOrdersCreatedBetween(from, to);

        // Then
        assertNotNull(fetchedOrders);
        assertFalse(fetchedOrders.isEmpty());
        assertEquals(1, fetchedOrders.size());
        verify(orderRepository, times(1)).findAllByCreationDateBetween(eq(from), eq(to));
    }
}