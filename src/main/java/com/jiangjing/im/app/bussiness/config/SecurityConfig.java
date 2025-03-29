package com.jiangjing.im.app.bussiness.config;

import com.jiangjing.im.app.bussiness.security.filter.JwtAuthenticationFilter;
import com.jiangjing.im.app.bussiness.security.handler.JwtAccessDeniedHandler;
import com.jiangjing.im.app.bussiness.security.handler.JwtAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    UserDetailsService userService;

    @Autowired
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and()
                .authorizeRequests()
                // 允许匿名访问
                .antMatchers("/v1/register", "/v1/login", "/oauth2/**").permitAll()
                // 其他接口需要认证
                .anyRequest().authenticated()
                .and()
                // 禁用默认的登录页面，使用自定义的登录逻辑
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf().disable();

        http.logout(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);
        http.addFilterBefore(jwtAuthenticationFilter, FilterSecurityInterceptor.class);
        http.exceptionHandling(handler -> {
            handler.authenticationEntryPoint(new JwtAuthenticationEntryPoint());
            handler.accessDeniedHandler(new JwtAccessDeniedHandler());
        });
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
