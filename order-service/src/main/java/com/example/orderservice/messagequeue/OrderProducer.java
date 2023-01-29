package com.example.orderservice.messagequeue;

import com.example.orderservice.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class OrderProducer {

    private KafkaTemplate<String, String> kafkaTemplate;

    // kafka 메시지 Field 정보 생성
    List<Field> fields = Arrays.asList(
            new Field("string", true, "user_id"),
            new Field("string", true, "product_id"),
            new Field("string", true, "order_id"),
            new Field("int32", true, "qty"),
            new Field("int32", true, "unit_price"),
            new Field("int32", true, "total_price"));

    //  kafka 메시지 Schema 정보 생성
    Schema schema = Schema.builder()
            .type("struct")
            .fields(fields)
            .optional(false)
            .name("orders")
            .build();

    @Autowired
    public OrderProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;  // Config 파일에서 Bean으로 등록한 KafkaTemplate을 주입
    }

    public OrderDto sendOrder(String topic, OrderDto orderDto) {
        Payload payload = Payload.builder()
                .order_id(orderDto.getOrderId())
                .user_id(orderDto.getUserId())
                .product_id(orderDto.getProductId())
                .qty(orderDto.getQty())
                .unit_price(orderDto.getUnitPrice())
                .total_price(orderDto.getTotalPrice())
                .build();

        KafkaOrderDto kafkaOrderDto = new KafkaOrderDto(schema, payload);

        /* Object(OrderDto) -> Json(String) */
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try {
            //jsonInString = mapper.writeValueAsString(orderDto);
            jsonInString = mapper.writeValueAsString(kafkaOrderDto);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }

        // kafkaTemplate의
        kafkaTemplate.send(topic, jsonInString);
        //log.info("Kafka Producer sent data from the Order microservice: " + orderDto);
        log.info("Kafka Producer sent data from the Order microservice: " + kafkaOrderDto);

        return orderDto;
    }
}
