package goralski.piotr.com.orders.model.dto;

import goralski.piotr.com.orders.model.Order;
import goralski.piotr.com.orders.model.enums.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class OrderDTO implements Serializable {

    private UUID id;
    private LocalDateTime creationDate;
    private OrderStatus status;
    private String description;
    private UUID userId;

    public OrderDTO(Order order) {
        super();
        this.id = order.getId();
        this.creationDate = order.getCreationDate();
        this.status = order.getStatus();
        this.description = order.getDescription();
        this.userId = order.getUser().getId();
    }

}
