package com.volco.creditapp.application.security.authorization;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Configuration
public class HandlerConfig {

    @Bean
    @Primary
    public MethodSecurityExpressionHandler securityExpressionHandler() {
        return new SecurityExpressionHandler();
    }
}