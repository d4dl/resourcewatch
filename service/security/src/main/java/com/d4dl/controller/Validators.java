package com.d4dl.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;

/**
 */
@Configuration
public class Validators {

    @Bean
    public Validator beforeCreateOrderIncidentValidator() {
        return new NewOrderIncidentValidator();
    }
}
