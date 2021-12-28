package com.example.userservice.vo;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component  //Spring에 일반적인 Bean으로 등록하기위해 @Component 사용
@Data
//@AllArgsConstructor  //모든 argument를 가지고 있는 생성자 생성
//@NoArgsConstructor  //argument가 없는 default 생성자 생성
public class Greeting {

    @Value("${greeting.message}")
    private String message;

}

