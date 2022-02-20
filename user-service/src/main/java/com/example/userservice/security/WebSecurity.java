package com.example.userservice.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration  //Bean으로 등록. 다른 Bean들 보다 우선적으로 Spring Context에 등록됨
@EnableWebSecurity  //클래스를 WebSecurity 용도로 사용하기 위한 어노테이션 추가
public class WebSecurity extends WebSecurityConfigurerAdapter {

    //권한 작업을 위해 재정의 해야하는 configure 메소드
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeRequests()  //사용할 수 있는 작업을 제한
                .antMatchers("/users/**").permitAll();  // "/users/" 로 시작하는 모든 요청은 통과시킴

        // spring security 추가 후 h2-console 사용을 위한 코드
        http.headers().frameOptions().disable();  //iFrame을 사용하는 페이지에서 'X-Frame-Options' to 'deny' 오류를 방지.
    }
}
