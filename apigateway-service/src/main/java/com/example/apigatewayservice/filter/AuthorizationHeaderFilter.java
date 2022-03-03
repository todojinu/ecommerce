package com.example.apigatewayservice.filter;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {
    private Environment env;

    public AuthorizationHeaderFilter(Environment env) {
        super(Config.class);
        this.env = env;
    }

    //설정에 관련된 작업을 위한 inner class 작성
    public static class Config {

    }

    // login -> token -> /users (with token) -> header(include token)
    @Override
    public GatewayFilter apply(Config config) {

        //PreFilter
        return ((exchange, chain) -> {
            // exchange 로 부터 서버객체의 Request 와 Response 를 얻는다
            ServerHttpRequest request = exchange.getRequest();
            //ServerHttpResponse response = exchange.getResponse();

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "No authorization header", HttpStatus.UNAUTHORIZED);
            }

            //get token
            String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String jwt = authorizationHeader.replace("Bearer", "");

            if (!isJwtValid(jwt)) {
                return onError(exchange, "JWT token is not valid", HttpStatus.UNAUTHORIZED);
            }

            //PostFilter
            return chain.filter(exchange);
        });

    }

    // JWT로 부터 subject를 추출한 후 정상적인 값인지를 체크
    private boolean isJwtValid(String jwt) {
        boolean returnValue = true;

        String subject = null;

        try {
            subject = Jwts.parser()
                    .setSigningKey(env.getProperty("token.secret"))  // 복호화를 위해 암호화시 사용한 키를 set
                    .parseClaimsJws(jwt)  // 토큰을 Jws(JSON Web Signature)*로 파싱
                                          // *Jws: 서버 인증을 근거로 서버의 private key로 서명한 것을 토큰화 한 것
                    .getBody()  // 데이터가 담긴 claim들을 get
                    .getSubject();  // subject get -> userId
        } catch (Exception e) {
            returnValue = false;
        }

        if (subject == null || subject.isEmpty()) {
            returnValue = false;
        }

        return returnValue;
    }

    // Mono, Flux -> Spring WebFlux 에서 클라이언트로 반환시켜주는 데이터 타입
    private Mono<Void> onError(ServerWebExchange exchange, String errMsg, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();

        response.setStatusCode(httpStatus);
        log.error(errMsg);

        return response.setComplete();  // setComplete()로 response 를 Mono 타입으로 반환
    }
}
