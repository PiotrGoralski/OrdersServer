package goralski.piotr.com.orders.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static goralski.piotr.com.orders.kafka.KafkaMessageProducer.CLOSED_ORDERS_TOPIC;
import static goralski.piotr.com.orders.kafka.KafkaMessageProducer.CREATED_ORDERS_TOPIC;

@Component
public class KafkaMessageConsumer {

    @KafkaListener(topics = CREATED_ORDERS_TOPIC, groupId = "my-consumer-group-id")
    public void consumeCreatedOrdersMessage(String message) {
        System.out.println("Create message was received: " + message);
    }

    @KafkaListener(topics = CLOSED_ORDERS_TOPIC, groupId = "my-consumer-group-id")
    public void consumeClosedOrdersMessage(String message) {
        System.out.println("Close message was received: " + message);
    }

}