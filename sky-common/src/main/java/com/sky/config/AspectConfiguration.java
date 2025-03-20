package com.sky.config;

import com.sky.aspect.AutoFillAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AspectConfiguration {

    // 注册切面的Bean
    @Bean
    public AutoFillAspect autoFillAspect() {
        return new AutoFillAspect();
    }
}
