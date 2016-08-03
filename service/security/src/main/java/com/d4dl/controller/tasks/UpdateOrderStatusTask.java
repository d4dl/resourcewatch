package com.d4dl.controller.tasks;

import com.d4dl.data.OrderIncidentRepository;
import com.d4dl.data.OrderRepository;
import com.d4dl.model.CartOrder;
import com.d4dl.model.OrderIncident;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.FixedValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class UpdateOrderStatusTask implements JavaDelegate {
;
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderIncidentRepository orderIncidentRepository;
    private FixedValue action;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        CartOrder order = delegateExecution.getVariable(CartOrder.CART_ORDER, CartOrder.class);
        order = orderRepository.findOne(order.getId());

        /**
        OrderIncident orderIncident = new OrderIncident();
        orderIncident.setIncidentType(OrderIncident.IncidentType.AUTO_PROCESS_STATE_CHANGE);
        orderIncident = orderIncidentRepository.save(orderIncident);
        order.addOrderIncident(orderIncident);
         **/
        Map params = new HashMap();
        params.put("action", this.getAction().getValue(delegateExecution).toString());

        String cartURL = order.getCartEndpoint();
        RestTemplate restTemplate = new RestTemplate();
        OrderIncident response = restTemplate.postForObject(cartURL, params, OrderIncident.class);
        response = orderIncidentRepository.save(response);
        order.addOrderIncident(response);

        orderRepository.save(order);
    }

    public FixedValue getAction() {
        return action;
    }

    public void setAction(FixedValue action) {
        this.action = action;
    }
}
