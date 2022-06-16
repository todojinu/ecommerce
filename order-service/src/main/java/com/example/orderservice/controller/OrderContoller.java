package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.jpa.OrderEntity;
import com.example.orderservice.messagequeue.KafkaProducer;
import com.example.orderservice.messagequeue.OrderProducer;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.vo.RequestOrder;
import com.example.orderservice.vo.ResponseOrder;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/order-service")
public class OrderContoller {
    Environment env;
    OrderService orderService;
    KafkaProducer kafkaProducer;

    OrderProducer orderProducer;

    @Autowired
    public OrderContoller(Environment env, OrderService orderService,
                          KafkaProducer kafkaProducer, OrderProducer orderProducer) {
        this.env = env;
        this.orderService = orderService;
        this.kafkaProducer = kafkaProducer;
        this.orderProducer = orderProducer;
    }

    @GetMapping("/health_check")
    public String status() {
        return String.format("It's working in Order Service on PORT %s", env.getProperty("local.server.port"));
    }

    @PostMapping("/{userId}/orders")
    public ResponseEntity<ResponseOrder> createOrder (@PathVariable("userId") String userId,
                                                      @RequestBody RequestOrder order) {
        ModelMapper mapper = new ModelMapper();

        /* jpa 작업 */
        //OrderDto orderDto = mapper.map(order, OrderDto.class);
        //orderDto.setUserId(userId);
        //orderService.createOrder(orderDto);
        //ResponseOrder responseOrder = mapper.map(orderDto, ResponseOrder.class);

        /* kafka 작업 */
        OrderDto orderDto = mapper.map(order, OrderDto.class);
        orderDto.setUserId(userId);
        orderDto.setOrderId(UUID.randomUUID().toString());
        orderDto.setTotalPrice(order.getQty() * order.getUnitPrice());

        /* send this order to kafka */
        // catalog service의 Kafka Listener에서 example-catalog-topic의 변경을 listen 한다.
        // TODO: configration server를 이용해볼 것
        kafkaProducer.sendOrder("example-catalog-topic", orderDto);

        orderProducer.sendOrder("orders", orderDto);  // topic의 이름이 테이블이 이름과 동일하다.

        ResponseOrder responseOrder = mapper.map(orderDto, ResponseOrder.class);

        return ResponseEntity.status(HttpStatus.OK).body(responseOrder);
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<ResponseOrder>> getOrders(@PathVariable("userId") String userId) {
        Iterable<OrderEntity> orderList = orderService.findOrdersByUserId(userId);

        List<ResponseOrder> result = new ArrayList<>();

        ModelMapper mapper = new ModelMapper();

        orderList.forEach(v -> {
            result.add(mapper.map(v, ResponseOrder.class));
        });

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

}
