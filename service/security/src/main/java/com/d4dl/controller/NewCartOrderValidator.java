package com.d4dl.controller;

import com.d4dl.data.OrderIncidentRepository;
import com.d4dl.data.OrderRepository;
import com.d4dl.model.CartOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class NewCartOrderValidator implements Validator {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderIncidentRepository orderIncidentRepository;

    public boolean supports(Class clazz) {
        return CartOrder.class.isAssignableFrom(clazz);
    }

    public void validate(Object target, Errors errors) {
        CartOrder incomingOrder = (CartOrder) target;
        if(incomingOrder.getRestClientId() == null ||
           incomingOrder.getShoppingCartId() == null ||
           incomingOrder.getCartOrderSystemId() == null ||
           incomingOrder.getCartOrderSystemQualifier() == null) {
            if(incomingOrder.getOrderIncidents().size() == 0) {
                errors.reject("ALL_ATTRIBUTES_NOT_SET", "restClientId, shoppingCartId, orderSystemId and orderSystemQualifier must all be set. " + incomingOrder);
            }
        }
    }
}
