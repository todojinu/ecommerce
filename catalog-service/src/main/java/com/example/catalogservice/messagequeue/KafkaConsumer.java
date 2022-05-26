package com.example.catalogservice.messagequeue;

/*
 * Consumer 에서는
 * - Listener 를 이용해 데이터를 가지고 오고,
 * - 가지고 온 데이터를 DB에 반영하는 처리를 함.
 */

import com.example.catalogservice.jpa.CatalogEntity;
import com.example.catalogservice.jpa.CatalogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class KafkaConsumer {

    CatalogRepository repository;

    @Autowired
    public KafkaConsumer(CatalogRepository repository) {
        this.repository = repository;
    }

    /* Listener 연결 */
    // example-catalog-topic 라는 topic에 데이터가 전달이 되면,
    // updateQty 메소드가 실행되게 된다.
    @KafkaListener(topics = "example-catalog-topic")
    public void updateQty(String kafkaMessage) {
        log.info("Kafka Message: -> {}", kafkaMessage);

        Map<Object, Object> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            // Message에 전달된 데이터를 변환하고자 하는 타입으로 변경
            map = mapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }

        CatalogEntity entity = repository.findByProductId((String)map.get("productId"));

        if (entity != null) {
            // Catalog 의 수량을 변경
            entity.setStock(entity.getStock() - (Integer)map.get("qty"));
            repository.save(entity);
        }
    }

}
