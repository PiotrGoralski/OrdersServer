package goralski.piotr.com.orders.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import goralski.piotr.com.orders.model.dto.OrderDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaMessageProducer {

    public static final String CREATED_ORDERS_TOPIC = "created-orders";
    public static final String CLOSED_ORDERS_TOPIC = "closed-orders";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public void sendMessage(String topic, String key, String message) {
        kafkaTemplate.send(topic, key, message);
        System.out.println("Message sent: Key = " + key + ", Value = " + message + ", Topic = " + topic);
    }

    public String parseOrderToStringMessage(OrderDTO value) {
        String message;
        try {
            message = objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot parse order with ID " + value.getId());
        }
        return message;
    }
}