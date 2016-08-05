/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.d4dl.controller;

import com.d4dl.config.WebSocketConfiguration;
import com.d4dl.data.OrderIncidentRepository;
import com.d4dl.data.OrderRepository;
import com.d4dl.model.CartOrder;
import com.d4dl.model.OrderIncident;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.hateoas.EntityLinks;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 */
// tag::code[]
@Component
@RepositoryEventHandler(CartOrder.class)
public class OrderEventHandler {

	private final SimpMessagingTemplate websocket;

	private final EntityLinks entityLinks;
    private final OrderRepository orderRepository;
    private final OrderIncidentRepository orderIncidentRepository;
    private final RuntimeService runtimeService;

    @Autowired
	public OrderEventHandler(SimpMessagingTemplate websocket, EntityLinks entityLinks, OrderRepository orderRepository, OrderIncidentRepository incidentRepository, RuntimeService runtimeService) {
		this.websocket = websocket;
		this.entityLinks = entityLinks;
		this.orderRepository = orderRepository;
        this.orderIncidentRepository = incidentRepository;
        this.runtimeService = runtimeService;
	}

	@HandleAfterCreate
	public void newCartOrder(CartOrder incomingOrder) {
		Logger.getLogger(this.getClass().getName()).info("Received CartOrder " + incomingOrder);
		Logger.getLogger(this.getClass().getName()).info("Starting process instance");

		Map<String, Object> startVariables = new HashMap();
		startVariables.put(CartOrder.CART_ORDER, incomingOrder);
		CartOrder newOrder = orderRepository.save(incomingOrder);

		Logger.getLogger(this.getClass().getName()).info("Starting process instance for new order: " + incomingOrder);
		ProcessInstance instance = runtimeService.startProcessInstanceByKey(newOrder.getProcessDefinitionKey(), startVariables);
		newOrder.setProcessInstanceId(instance.getProcessInstanceId());
		orderRepository.save(newOrder);
		this.websocket.convertAndSend(WebSocketConfiguration.MESSAGE_PREFIX + "/newCartOrder", getPath(newOrder));
	}

	@HandleAfterDelete
	public void deleteCartOrder(CartOrder cartOrder) {
		this.websocket.convertAndSend(
				WebSocketConfiguration.MESSAGE_PREFIX + "/deleteCartOrder", getPath(cartOrder));
	}

	@HandleBeforeSave
	public void saveCartOrder(CartOrder incomingOrder) {
		CartOrder existingOrder = orderRepository.findByRestClientIdAndShoppingCartIdAndCartOrderSystemIdAndCartOrderSystemQualifier(
				incomingOrder.getRestClientId(),
				incomingOrder.getShoppingCartId(),
				incomingOrder.getCartOrderSystemId(),
				incomingOrder.getCartOrderSystemQualifier());
        if(existingOrder != null) {
            if(!existingOrder.getStatus().equals(incomingOrder.getStatus())) {
                boolean foundOrderWithStatus = false;
                Logger.getLogger(this.getClass().getName()).info("Found " + existingOrder + " for id " + incomingOrder.getCartOrderSystemId());
                incomingOrder.setId(incomingOrder.getId());
                for(OrderIncident orderIncident : incomingOrder.getOrderIncidents()) {
                    if(orderIncident.getStatus().equals(incomingOrder.getStatus())) {
                        foundOrderWithStatus = true;
                    }
                }
                if(!foundOrderWithStatus) {
                    OrderIncident orderIncident = new OrderIncident(existingOrder, incomingOrder.getOrderTag().equalsIgnoreCase("cart") ? OrderIncident.IncidentType.CART_STATE_CHANGE : OrderIncident.IncidentType.AUTO_PROCESS_STATE_CHANGE, incomingOrder.getStatus());
                    this.orderIncidentRepository.save(orderIncident);
                }
            }
        }
	}

	@HandleAfterSave
	public void updateCartOrder(CartOrder cartOrder) {
		this.websocket.convertAndSend(
				WebSocketConfiguration.MESSAGE_PREFIX + "/updateCartOrder", getPath(cartOrder));
	}


	/**
	 * Take an {@link CartOrder} and get the URI using Spring Data REST's {@link EntityLinks}.
	 *
	 * @param cartOrder
	 */
	private String getPath(CartOrder cartOrder) {
		return this.entityLinks.linkForSingleResource(cartOrder.getClass(),
				cartOrder.getId()).toUri().getPath();
	}

}
// end::code[]
