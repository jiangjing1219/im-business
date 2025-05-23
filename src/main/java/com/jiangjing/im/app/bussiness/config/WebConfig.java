package com.jiangjing.im.app.bussiness.config;

import com.jiangjing.im.app.bussiness.utils.SnowflakeIdWorker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author: Chackylee
 * @description:
 **/
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600)
                .allowedHeaders("*");
    }

    /***
     * 雪花算法
     *
     * @return
     * @throws Exception
     */
    @Bean
    public SnowflakeIdWorker buildSnowflakeSeq() throws Exception {
        return new SnowflakeIdWorker(0);
    }
}
