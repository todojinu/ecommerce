package com.example.catalogservice.messagequeue;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@EnableKafka  // Kafka 에서 사용할 수 있도록 어노테이션 추가
@Configuration  // 설정정보로 등록
public class KafkaConsumerConfig {

    Environment env;

    @Autowired
    public KafkaConsumerConfig(Environment env) {
        this.env = env;
    }

    // 접속할 kafka 정보를 Bean 으로 등록
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {  // <KEY, VALUE> 타입
        Map<String, Object> properties = new HashMap<>();

        // TODO: config 서버를 사용해 주소를 관리해볼 것
//        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");  // kafka 서버 주소
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, env.getProperty("kafka.server.uri"));  // kafka 서버 주소
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "consumerGroupId");  // group id로 Consumer 들의 grouping 가능
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(properties);
    }

    // topic의 변경사항을 catch할 Listener를 Bean으로 등록
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory
                = new ConcurrentKafkaListenerContainerFactory<>();

        kafkaListenerContainerFactory.setConsumerFactory(consumerFactory());  // kafka 접속을 위한 설정정보 등록

        return kafkaListenerContainerFactory;
    }

}
