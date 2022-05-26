package com.example.orderservice.messagequeue;

import com.netflix.discovery.converters.Auto;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@EnableKafka  // kafka 에서 사용할 수 있도록 변경
@Configuration
public class KafkaProducerConfig {

    Environment env;

    @Autowired
    public KafkaProducerConfig(Environment env) {
        this.env = env;
    }

    @Bean
    ProducerFactory<String, String> producerFactory() {

        Map<String, Object> properties = new HashMap<>();
//        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, env.getProperty("kafka.server.uri"));
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new DefaultKafkaProducerFactory<>(properties);
    }

    // Data를 kafka로 전달하는 인스턴스인 KafkaTemplate를 Bean으로 등록하기 위한 메소드 생성
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

}
