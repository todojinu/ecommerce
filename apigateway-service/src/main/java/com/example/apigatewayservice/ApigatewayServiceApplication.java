package com.example.apigatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApigatewayServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApigatewayServiceApplication.class, args);
	}

	// HttpTraceRepository 를 Bean 으로 등록
	// -> 직접 제어가 불가능한 라이브러리 등을 Bean으로 등록하려면 해당 라이브러리 객체를 반환하는 메소드를 만들고 @Bean 어노테이션을 추가
	@Bean
	public HttpTraceRepository httpTraceRepository() {
		// HttpTraceRepository를 메모리 타입으로 return
		// ->  클라이언트 요청에 대한 trace 정보가 memory에 로드되어 필요시 '/httptrace' end point로 내용을 확인할 수 있음
		return new InMemoryHttpTraceRepository();
	}

}
