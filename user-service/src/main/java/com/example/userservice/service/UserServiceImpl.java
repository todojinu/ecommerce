package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service  // Bean으로 등록
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {

        /* UUID
         * -UUID는 네트워크 상에서 고유성이 보장되는 id를 만들기 위한 표준 규약이다.
         * -UUID는 128비트의 숫자이며, 32자리의 16진수로 표현된다.
         * -분산 환경에서 개별 시스템이 id를 발급하더라도 유일성이 보장될 수 있도록 한다.
         */
        userDto.setUserId(UUID.randomUUID().toString());  // UUID 클래스를 UUID를 가져올 수 있다.

        //ModelMapper를 사용해 Dto -> Entity 변환
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()                                 //매칭할수 있는 설정정보를 setting
                .setMatchingStrategy(MatchingStrategies.STRICT);  //매칭전략설정
        UserEntity userEntity = mapper.map(userDto, UserEntity.class);  //UserDto를 UserEntity로 변환
        userEntity.setEncryptedPwd("encrypted_password");  //TODO: 추후 암호화 기능 구현

        userRepository.save(userEntity);

        UserDto returnUserDto = mapper.map(userEntity, UserDto.class);

        return returnUserDto;
    }
}