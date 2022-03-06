package com.example.userservice.security;

import com.example.userservice.dto.UserDto;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.RequestLogin;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private UserService userService;
    private Environment env;

    public AuthenticationFilter(AuthenticationManager authenticationManager, UserService userService, Environment env) {
        super.setAuthenticationManager(authenticationManager);
        this.userService = userService;
        this.env = env;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        try {
            //request 로 전달되어지는 input stream을 지정된 클래스 타입으로 변경
            //- POST로 전달되는 값은 Request Parameter로 받을 수 없으므로 inputStream으로 데이터를 처리할 수 있음
            RequestLogin creds = new ObjectMapper().readValue(request.getInputStream(), RequestLogin.class);

            //인증정보 생성
            //- 사용자가 입력한 email, password를 SpringSecurity에서 사용할 수 있는 UsernamePasswordAuthenticationToken으로 변경
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    creds.getEmail(),
                    creds.getPassword(),
                    new ArrayList<>());  //권한에 대한 내용

            return getAuthenticationManager().authenticate(token);  //AuthenticationManager로 인증 작업을 요청

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //인증 성공후 호출됨
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {

        //인증 성공 후 처리할 작업(토큰생성, 만료시간, 반환값 등) 작성

        //토큰 생성시 사용할 userId 조회
        //log.debug(((User)authResult.getPrincipal()).getUsername());
        String userName = ((User)authResult.getPrincipal()).getUsername();
        UserDto userDetails = userService.getUserDetailsByEmail(userName);

        //JWT 토큰 생성
        String token = Jwts.builder()
                .setSubject(userDetails.getUserId())  //userId를 사용해 토큰 생성
                //만료시간설정 -> 현재시간+만료기간
                .setExpiration(new Date(System.currentTimeMillis() +
                        Long.parseLong(env.getProperty("token.expiration_time"))))  //yml 파일에서 가져오는 값은 모두 String
                .signWith(SignatureAlgorithm.HS512, env.getProperty("token.secret"))  //token.secret의 값을 첨가하여 암호화
                .compact();

        response.addHeader("token", token);
        response.addHeader("userId", userDetails.getUserId());
    }
}
