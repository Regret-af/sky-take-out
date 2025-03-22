package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.utils.WeChatPayUtil;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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

    @Bean
    @ConfigurationProperties(prefix = "sky.wechat")
    public WeChatProperties weChatProperties() {
        return new WeChatProperties();
    }

    @Bean
    public WeChatPayUtil weChatPayUtil(WeChatProperties weChatProperties) {
        return new WeChatPayUtil(weChatProperties);
    }
}
