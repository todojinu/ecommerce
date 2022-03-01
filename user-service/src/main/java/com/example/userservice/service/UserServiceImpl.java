package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.vo.ResponseOrder;
import org.bouncycastle.math.raw.Mod;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service  // Bean으로 등록
public class UserServiceImpl implements UserService {

    //@Autowired
    UserRepository userRepository;
    BCryptPasswordEncoder bCryptPasswordEncoder;

    //생성사 주입: 생성자가 Spring Context에 의해서 만들어지면서 만들어 놓은 Bean들을 주입하여 메모리에 등록하여 사용가능한 상태로
    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;

        //BCryptPasswordEncoder 클래스는 Bean으로 등록되어 있지 않으므로 "No beans of 'BCryptPasswordEncoder'" 오류 발생
        //-> 가장 먼저 호출되는 Spring Application의 기동 클래스(UserServiceApplication)에서 해당하는 Bean을 등록할 수 있다.
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

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
        userEntity.setEncryptedPwd(bCryptPasswordEncoder.encode(userDto.getPwd()));

        userRepository.save(userEntity);

        UserDto returnUserDto = mapper.map(userEntity, UserDto.class);

        return returnUserDto;
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);

        if (userEntity == null) {
            throw new UsernameNotFoundException("User not found");
        }

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);

        List<ResponseOrder> orders = new ArrayList<>();
        userDto.setOrders(orders);

        return userDto;
    }

    @Override
    public Iterable<UserEntity> getUserByAll() {
        return userRepository.findAll();
    }


}
