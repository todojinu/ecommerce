package com.example.userservice;

import com.example.userservice.error.FeignErrorDecoder;
import feign.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient  // Eureka Server 에 Client 로 등록해 사용하기 위한 어노테이션 추가
@EnableFeignClients     // Feign Client 를 사용하기 위한 어노테이션 추가
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

	// BCryptPasswordEncoder를 Bean으로 등록
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// RestTemplate 을 Bean으로 등록하기 위한 메소드 생성 -> Feign Client 사용으로 주석처리
//	@Bean
//	@LoadBalanced  // ip 주소가 아닌 Eureka Server에 등록된 Application Name으로 서비스를 찾아갈 수 있다.
//	public RestTemplate getRestTemplate() {
//		return new RestTemplate();
//	}

	// Feign Client 의 로그 출력을 위해 Logger.Level 을 Bean 으로 등록하기 위한 메소드 추가
	@Bean
	public Logger.Level feignLoggerLevel() {
		return Logger.Level.FULL;
	}

	// ErrorDecoder 인터페이스를 구현한 FeignErrorDecoder 사용을 위해 Bean으로 등록
	// -> FeignErrorDecoder 에서 @Component로 등록 했으므로 주석처리
//	@Bean
//	public FeignErrorDecoder feignErrorDecoder() {
//		return new FeignErrorDecoder();
//	}

}
