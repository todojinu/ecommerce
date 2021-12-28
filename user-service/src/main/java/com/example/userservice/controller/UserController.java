package com.example.userservice.controller;

import com.example.userservice.vo.Greeting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class UserController {

      //Environment를 활용한 환경변수 사용
//    private Environment env;
//
//    @Autowired
//    public UserController(Environment env) {
//        this.env = env;
//    }
//
//    @GetMapping("/welcome")
//    public String welcome() {
//        return env.getProperty("greeting.message");
//    }

    @Autowired
    private Greeting greeting;

    @GetMapping("/welcome")
    public String welcome() {
        return greeting.getMessage();
    }


    @GetMapping("/health_check")
    public String status() {
        return "It's Working in User Service";
    }

}
