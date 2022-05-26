package com.example.userservice.error;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/* ErrorDecoder 를 이용한 예외처리를 위해 ErrorDecoder 인터페이스를 구현 */

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    Environment env;

    @Autowired
    FeignErrorDecoder(Environment env) {
        this.env = env;
    }

    // 메소드키와 상태코드를 가지고 예외 처리가 가능하다.
    @Override
    public Exception decode(String methodKey, Response response) {

        switch (response.status()) {
            case 400:
                break;
            case 404 :
                if (methodKey.contains("getOrders")) {
                    return new ResponseStatusException(HttpStatus.valueOf(response.status()),
                            env.getProperty("order-service.exception.order_is_empty"));  // 메시지를 설정파일로 관리
//                    return new ResponseStatusException(HttpStatus.valueOf(response.status()),
//                            "User's orders is empty.");
                }
                break;
            default:
                return new Exception(response.reason());
        }

        return null;
    }

}
