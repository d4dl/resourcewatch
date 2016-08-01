package com.d4dl.controller;

import com.d4dl.data.OrderIncidentRepository;
import com.d4dl.model.OrderIncident;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 */
@Configuration
public class Validators {

    @Autowired
    private OrderIncidentRepository orderIncidentRepository;

    @Bean
    public Validator beforeCreateOrderIncidentValidator() {
        return new Validator() {
            @Override
            public boolean supports(Class<?> clazz) {
                return OrderIncident.class.isAssignableFrom(clazz);
            }

            @Override
            public void validate(Object target, Errors errors) {
                OrderIncident orderIncident = (OrderIncident)target;

            }
        };
    }
}
