package com.d4dl.controller.tasks;

import com.d4dl.data.OrderIncidentRepository;
import com.d4dl.data.OrderRepository;
import com.d4dl.model.CartOrder;
import com.d4dl.model.HardCodedDrupalStuff;
import com.d4dl.model.OrderIncident;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.*;
import org.activiti.engine.impl.el.FixedValue;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URISyntaxException;
import java.util.logging.Logger;

/**
 */
public class UpdateOrderStatusTask implements JavaDelegate, TaskListener {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderIncidentRepository orderIncidentRepository;
    private FixedValue status;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        CartOrder order = delegateExecution.getVariable(CartOrder.CART_ORDER, CartOrder.class);
        setStatus(delegateExecution, order);
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        CartOrder order = delegateTask.getVariable(CartOrder.CART_ORDER, CartOrder.class);
        try {
            setStatus(delegateTask, order);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setStatus(VariableScope scope, CartOrder order) throws Exception {
        order = orderRepository.findOne(order.getId());
        Logger.getLogger(this.getClass().getName()).info("Automagically updating order status!!!:");
        if(order.getShoppingCartType().equals("drupal_commerce_rest")) {
            String status = this.getStatus().getValue(scope).toString();
            order.setStatus(status);
            order.setLog("Auto processed by Resource Watcher");
            OrderIncident orderIncident = new HardCodedDrupalStuff().updateOrder(order);
            orderIncident = orderIncidentRepository.save(orderIncident);
            order.addOrderIncident(orderIncident);

            orderRepository.save(order);
        }
    }

    public FixedValue getStatus() {
        return status;
    }

    public void setStatus(FixedValue status) {
        this.status = status;
    }

}
