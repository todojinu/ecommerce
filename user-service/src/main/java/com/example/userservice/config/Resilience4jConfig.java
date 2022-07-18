package com.example.userservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;


@Configuration
public class Resilience4jConfig {

    // 커스터마이징을 하기 위해 Customizer를 이용해 사용하고자 하는 Factory를 감싸서 반환
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> globalCustomConfiguration() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(4)  // CircuitBreaker를 열지 결정하는 실패율: 4%
                // CircuitBreaker open 상태 지속시간: 1초 -> 1초후 half-open 상태로 변경됨
                .waitDurationInOpenState(Duration.ofMillis(1000))
                // CircuitBreaker가 닫힐때 통화결과를 기록하는데 사용되는 슬라이딩창의 유형. 카운트 혹은 시간기반
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                // CircuitBreaker가 닫힐때 호출결과를 기록하는데 사용되는 슬라이딩창 크기
                .slidingWindowSize(2)
                .build();

        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(4))  // future supplier의 time limit을 정하는 API
                .build();

        // 생성할 수 있는 파라미터 지정하여 최종적인 Factory를 생성해 반환
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(timeLimiterConfig)
                .circuitBreakerConfig(circuitBreakerConfig)
                .build()
        );
    }
}
