package goralski.piotr.com.orders.model;

import goralski.piotr.com.orders.model.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="app_order")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime creationDate;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String description;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "idUser")
    private User user;

    public Order(String description, User user) {
        super();
        this.creationDate = LocalDateTime.now();
        this.status = OrderStatus.CREATED;
        this.description = description;
        this.user = user;
    }
}
