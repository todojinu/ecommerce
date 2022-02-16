package com.example.userservice.controller;

import com.example.userservice.dto.UserDto;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.Greeting;
import com.example.userservice.vo.RequestUser;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class UserController {

    private Environment env;  //Environment를 활용한 환경변수 사용
    private UserService userService;

    @Autowired
    public UserController(Environment env, UserService userService) {
        this.env = env;
        this.userService = userService;  //생성자주입
    }

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

    @PostMapping("/users")
    public ResponseEntity<RequestUser> createUser(@RequestBody RequestUser user) {  //POST 방식은 RequestBody 형태로 받는다
        //ModelMapper를 사용한 객체 매핑
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto userDto = mapper.map(user, UserDto.class);  //RequestUser -> UserDto

        userService.createUser(userDto);

        RequestUser requestUser = mapper.map(userDto, RequestUser.class);  //UserDto -> RequestUser

        return ResponseEntity.status(HttpStatus.CREATED)  //ResponseEntity에 CREATED(201) 성공 코드를 반환. *RestAPI를 개발할 때에는 적절한 상태코드를 반환하는 것이 좋음*
                .body(requestUser);  //requestUser를 responseBody로 전달
    }

}
