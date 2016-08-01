package com.d4dl.controller;

import com.d4dl.model.OrderIncident;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class NewOrderIncidentValidator implements Validator {

    public boolean supports(Class clazz) {
        return OrderIncident.class.isAssignableFrom(clazz);
    }

    public void validate(Object target, Errors errors) {
    }
}
