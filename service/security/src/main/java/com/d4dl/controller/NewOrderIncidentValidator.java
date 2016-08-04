package com.d4dl.controller;

import com.d4dl.data.OrderIncidentRepository;
import com.d4dl.model.OrderIncident;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Component
public class NewOrderIncidentValidator implements Validator {

    @Autowired
    private OrderIncidentRepository orderIncidentRepository;

    public boolean supports(Class clazz) {
        return OrderIncident.class.isAssignableFrom(clazz);
    }

    public void validate(Object target, Errors errors) {
        OrderIncident orderIncident = (OrderIncident) target;

        List<OrderIncident> incidentList = orderIncident.getCartOrder().getId() == 0 ? new ArrayList() : orderIncidentRepository.findByCartOrderAndStatus(orderIncident.getCartOrder(), orderIncident.getStatus());
        Logger.getLogger(this.getClass().getName()).info("Validator Found " + incidentList.size() + " incidences for order " + orderIncident.getCartOrder().getCartSystemId() + " with status " + orderIncident.getStatus());
        if(incidentList.size() > 0) {
            errors.reject("ORDER_EXISTS", "There is already an order incident with status '" + orderIncident.getStatus());
        }
    }
}
