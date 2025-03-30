package com.jiangjing.im.app.bussiness.config;

import com.jiangjing.im.app.bussiness.security.filter.JwtAuthenticationFilter;
import com.jiangjing.im.app.bussiness.security.handler.JwtAccessDeniedHandler;
import com.jiangjing.im.app.bussiness.security.handler.JwtAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    OAuth2UserService userService;

    @Autowired
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(
                        // 注册接口
                        "/v1/register",
                        // 登录接口
                        "/v1/login",
                        // 授权回调
                        "/login/oauth2/code/gitee",
                        // 触发第三方登录
                        "/oauth2/authorization/gitee",
                        // 测试接口
                        "/v1/login/oauth2/success",
                        "/v1/login/oauth2/failure"
                ).permitAll()
                // 其他接口需要认证
                .anyRequest().authenticated();
        http.logout(AbstractHttpConfigurer::disable);
        http.formLogin().disable();
        http.httpBasic().disable();
        http.csrf().disable();
        http.addFilterBefore(jwtAuthenticationFilter, FilterSecurityInterceptor.class);
        http.exceptionHandling(handler -> {
            handler.authenticationEntryPoint(new JwtAuthenticationEntryPoint());
            handler.accessDeniedHandler(new JwtAccessDeniedHandler());
        });

        /**
         * oauth2 聚焦于资源共享，只要持有正确的令牌就可向资源服务器请求资源
         * 如： https://gitee.com/api/v5/user?access_token=xxxxx（前置流程都是为了获取 access_token）
         *
         * CIDC是OpenIDConnect的简写，它是由OpenID基金会开发的、以OAuth2.0为基础一种身份层协议。
         * OIDC则既处理授权也处理认证，它确保应用不仅可以获取访问权限，还能确认用户的真实身份。OIDC引入了
         * Openld的概念，这是一种包含用户身份信息的Jwt，使得应用可以确信"谁"正在访问。
         *
         */
        http.oauth2Login(oauth2Login -> oauth2Login.authorizationEndpoint(authorization -> {
                            // 项目启动时调用，授权端点， oauth2 客户端和资源所有者交互并获取一个授权许可
                            System.out.println("1111 :" + authorization);
                        })
                        .tokenEndpoint(token -> {
                            // 项目启动时调用，令牌端点，oauth2 客户端使用授权许可来获取访问令牌
                            System.out.println("2222:" + token);
                        })
                        .redirectionEndpoint(redirection -> {
                            // 项目启动时调用，重定向端点，oauth2服务器通过重定向到此端点，将授权码传递给 oauth2 客户端
                            System.out.println("333:" + redirection);
                        })
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig.userService(userService))
                        .successHandler((request, response, authentication) -> {
                            // 登录成功后的处理逻辑
                            response.sendRedirect("/v1/login/oauth2/success");
                        })
                        .failureHandler((request, response, authentication) -> {
                            // 登录成功后的处理逻辑
                            response.sendRedirect("/v1/login/oauth2/failure");
                        })
        );
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
