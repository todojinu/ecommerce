package com.example.userservice.service;

import com.example.userservice.client.OrderServiceClient;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.vo.ResponseOrder;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service  // Bean으로 등록
public class UserServiceImpl implements UserService {

    //@Autowired
    UserRepository userRepository;
    BCryptPasswordEncoder bCryptPasswordEncoder;

    Environment env;
//    RestTemplate restTemplate;  // Feign Client 사용으로 주석처리

    OrderServiceClient orderServiceClient;  // orderService Feign Client Interface

    //생성자 주입: 생성자가 Spring Context에 의해서 만들어지면서 만들어 놓은 Bean들을 주입하여 메모리에 등록하여 사용가능한 상태로
    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder bCryptPasswordEncoder,
                           Environment env,
//                           RestTemplate restTemplate,  // Feign Client 사용으로 주석처리
                           OrderServiceClient orderServiceClient)
    {
        this.userRepository = userRepository;

        //BCryptPasswordEncoder 클래스는 Bean으로 등록되어 있지 않으므로 "No beans of 'BCryptPasswordEncoder'" 오류 발생
        //-> 가장 먼저 호출되는 Spring Application의 기동 클래스(UserServiceApplication)에서 해당하는 Bean을 등록할 수 있다.
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;

        this.env = env;

//        this.restTemplate = restTemplate;  // Feign Client 사용으로 주석처리

        this.orderServiceClient = orderServiceClient;
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

//        List<ResponseOrder> orders = new ArrayList<>();
//        userDto.setOrders(orders);

        /* User Feign Client */
        /* Feign exception handling */
//        List<ResponseOrder> orderList = null;
//        try {
//            orderList = orderServiceClient.getOrders(userId);
//        } catch (FeignException ex) {
//            log.error(ex.getMessage());
//        }
//
//        userDto.setOrders(orderList);

        // ErrorDecoder 를 사용한 예외처리
        /* ErrorDecoder */
        List<ResponseOrder> orderList = orderServiceClient.getOrders(userId);
        userDto.setOrders(orderList);

        /* Using rest template */
        /*
        // RestTemplate 사용해 order-service를 호출하여 주문정보를 가져오도록 변경
        String orderUrl = "";

        // 방법 1.호출 서비스 주소 하드코딩
        //orderUrl = "http://127.0.0.1:8000/order-service/%s/orders";

        // 방법 2.호출 서비스 주소를 Config 파일에서 관리하고, Environment 객체를 이용해 주소값 get
        orderUrl = env.getProperty("order-service.url");
        orderUrl = String.format(orderUrl, userId);

        ResponseEntity<List<ResponseOrder>> orderListResponse = restTemplate.exchange(
                orderUrl,         // 서비스 주소
                HttpMethod.GET,   // HTTP Method 방식
                null,  // 요청 파라미터 타입
                new ParameterizedTypeReference<List<ResponseOrder>>() {  // 응답파라미터타입
        });

        List<ResponseOrder> orderList = orderListResponse.getBody();
        userDto.setOrders(orderList);
         */

        return userDto;
    }

    @Override
    public Iterable<UserEntity> getUserByAll() {
        return userRepository.findAll();
    }


    //인증 처리에 필요한 정보를 load
    //-SpringSecurity의 UserDetailsService 클래스의 loadUserByUsername 메소드를 재정의
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(username);

        if (userEntity == null) {
            throw new UsernameNotFoundException(username);
        }

        return new User(userEntity.getEmail(), userEntity.getEncryptedPwd(),
                true,
                true,
                true,
                true,
                new ArrayList<>());  //로그인 이후 권한을 추가하는 작업
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);

        if (userEntity == null) {
            throw new UsernameNotFoundException("등록된 이메일이 없습니다.");
        }

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);
        //ModelMapper는 MatchingStrategies 전략(Strict, Loose, Standard 에 따라 두 객체의 매칭 수위를 조절할 수 있다.
        //-> 위의 경우는 pwd에 encryptedPwd가 들어가게됨

        return userDto;
    }

}
