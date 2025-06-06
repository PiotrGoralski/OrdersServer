package goralski.piotr.com.orders.repository;

import goralski.piotr.com.orders.model.Order;
import goralski.piotr.com.orders.model.enums.OrderStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends CrudRepository<Order, UUID> {

    List<Order> findAll();
    List<Order> findAllByStatus(OrderStatus status);
    List<Order> findAllByCreationDateBetween(LocalDateTime dateFrom, LocalDateTime dateTo);

}