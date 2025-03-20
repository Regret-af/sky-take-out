package com.sky.config;

import com.sky.aspect.AutoFillAspect;
import com.sky.properties.AliOssProperties;
import com.sky.properties.JwtProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PropertiesConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "sky.jwt")
    public JwtProperties jwtProperties() {
        return new JwtProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "sky.alioss")
    public AliOssProperties aliOssProperties() {
        return new AliOssProperties();
    }
}
