package com.jiangjing.im.app.bussiness.config;

import com.jiangjing.im.app.bussiness.security.filter.JwtAuthenticationFilter;
import com.jiangjing.im.app.bussiness.security.handler.JwtAccessDeniedHandler;
import com.jiangjing.im.app.bussiness.security.handler.JwtAuthenticationEntryPoint;
import com.jiangjing.im.app.bussiness.security.oauth2.OAuth2LoginSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Value("${spring.security.oauth2.client.registration.gitee.redirect-uri}")
    private String redirectUri;

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
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.addFilterBefore(jwtAuthenticationFilter, FilterSecurityInterceptor.class);
        http.exceptionHandling(handler -> {
            handler.authenticationEntryPoint(new JwtAuthenticationEntryPoint());
            handler.accessDeniedHandler(new JwtAccessDeniedHandler());
        });

        // 配置OAuth2登录
        http.oauth2Login()
            .authorizationEndpoint()
                .baseUri("/oauth2/authorization")
                .and()
            .redirectionEndpoint()
                .baseUri("/oauth2/callback/*")
                .and()
            .userInfoEndpoint()
                .userService(giteeOAuth2UserService)
                .and()
            .successHandler(oAuth2LoginSuccessHandler);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
