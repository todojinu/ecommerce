package com.example.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient  // Eureka Server 에 Client 로 등록해 사용하기 위한 어노테이션 추가
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

	// BCryptPasswordEncoder를 Bean으로 등록
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// RestTemplate 을 Bean으로 등록하기 위한 메소드 생성
	@Bean
	@LoadBalanced  // ip 주소가 아닌 Eureka Server에 등록된 Application Name으로 서비스를 찾아갈 수 있다.
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}
}
