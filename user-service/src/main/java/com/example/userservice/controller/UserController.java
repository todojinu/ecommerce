package com.example.userservice.controller;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.Greeting;
import com.example.userservice.vo.RequestUser;
import com.example.userservice.vo.ResponseUser;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class UserController {

    private Environment env;  //Environment를 활용한 환경변수 사용
    private UserService userService;

    //생성자주입
    @Autowired
    public UserController(Environment env, UserService userService) {
        this.env = env;
        this.userService = userService;
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
        return String.format("It's Working in User Service"
                + ", port(local.server.port)=" + env.getProperty("local.server.port")  //할당된 random port 번호
                + ", port(server.port)=" + env.getProperty("server.port")
                + ", token secret= " + env.getProperty("token.secret")
                + ", token expiration time=" + env.getProperty("token.expiration_time"));
    }

    @PostMapping("/users")
    public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser user) {  //POST 방식은 RequestBody 형태로 받는다
        //ModelMapper를 사용한 객체 매핑
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto userDto = mapper.map(user, UserDto.class);  //RequestUser -> UserDto

        userService.createUser(userDto);

        ResponseUser responseUser = mapper.map(userDto, ResponseUser.class);  //UserDto -> RequestUser

        return ResponseEntity.status(HttpStatus.CREATED)  //ResponseEntity에 CREATED(201) 성공 코드를 반환. *RestAPI를 개발할 때에는 적절한 상태코드를 반환하는 것이 좋음*
                .body(responseUser);  //requestUser를 responseBody로 전달
    }

    @GetMapping("/users")
    public ResponseEntity<List<ResponseUser>> getUsers() {
        Iterable<UserEntity> userList = userService.getUserByAll();

        List<ResponseUser> result = new ArrayList<>();
        ModelMapper modelMapper = new ModelMapper();

        userList.forEach(v -> {
            result.add(modelMapper.map(v, ResponseUser.class));
        });

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseUser> getUserByUserId(@PathVariable("userId") String userId) {
        UserDto userDto = userService.getUserByUserId(userId);

        ResponseUser returnValue = new ModelMapper().map(userDto, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.OK).body(returnValue);
    }

}
