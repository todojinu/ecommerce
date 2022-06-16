package com.example.userservice.security;

import com.example.userservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Slf4j
@Configuration  //Bean으로 등록. 다른 Bean들 보다 우선적으로 Spring Context에 등록됨
@EnableWebSecurity  //클래스를 WebSecurity 용도로 사용하기 위한 어노테이션 추가
public class WebSecurity extends WebSecurityConfigurerAdapter {
    private Environment env;  //환경설정 파일에 토큰의 유효 시간 등을 작성한뒤 값을 가져올 수 있음
    private UserService userService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    //@Configuration은 @Autowired 하지 않아도 미리 필요한 상태로 준비되어진다
    public WebSecurity(Environment env, UserService userService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.env = env;
        this.userService = userService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    //인증 방법을 정의한 configure 메소드 추가
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //select pwd from users where email=?
        //db_pwd(encrypted) == input_pwd(encrypt)
        auth.userDetailsService(userService)  //인증작업에 필요한 정보를 가져오기 위해
                                              //UserDetailsService를 상속받은 loadUserByUsername 메소드를 구현한 Service를 set
                .passwordEncoder(bCryptPasswordEncoder);  //입력한 password 값 변환 처리
    }

    //인증 작업을 위한 configure 메소드 추가
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //csrf(Cross Site Request Forgery) protection 기능 disabled
        //- REST API를 이용한 서버는 session 기반 인증과는 다르게 stateless 하기떄문에 서버에 인증정보를 보관하지 않음
        //- 따라서, 불필요한 csrf 코드 작성이 불필요
        http.csrf().disable();

        http.authorizeRequests().antMatchers("/actuator/**").permitAll();  // "/actuator/..." 요청 통과

        // TODO: 요청 ip 필터 오류 수정 필요
        http.authorizeRequests()  //사용할 수 있는 작업을 제한
//                .antMatchers("/users/**").permitAll();  // "/users/" 로 시작하는 모든 요청은 통과시킴
//                .antMatchers("/**")  //모든 작업에 대해
                .antMatchers("/users/**")  //모든 작업에 대해
                .permitAll()
//                .hasIpAddress(env.getProperty("gateway.ip"))  //gateway ip를 설정
                .and()  //추가 작업 설정시 and() 메소드 호출 후 추가
                .addFilter(getAuthenticationFilter());  //인증을 위한 필터 추가. /login 요청시 필터 동작

        // spring security 추가 후 h2-console 사용을 위한 코드
        http.headers().frameOptions().disable();  //iFrame을 사용하는 페이지에서 "'X-Frame-Options' to 'deny'" 오류를 방지.
    }

    private AuthenticationFilter getAuthenticationFilter() throws Exception {
        AuthenticationFilter authenticationFilter =
                new AuthenticationFilter(authenticationManager(), userService, env);

        //authenticationFilter.setAuthenticationManager(authenticationManager());  //실제 동작할 manager 삽입

        return authenticationFilter;
    }
}
