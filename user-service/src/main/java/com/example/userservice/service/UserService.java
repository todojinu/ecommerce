package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {  //SpringSecurity의 UserDetailsService를 상속받아
                                                           //impl 클래스에서 loadUserByUsername 메소드 구현
                                                           //-> 인증처리를 위한 메소드(configure) 에서 사용
    UserDto createUser(UserDto userDto);
    UserDto getUserByUserId(String userId);
    Iterable<UserEntity> getUserByAll();

    UserDto getUserDetailsByEmail(String userName);
}
