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
import com.d4dl.data.WhitelistAttributeRepository;
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
	public static final String HARD_CODED_STATUS = "pending";

	private final EntityLinks entityLinks;
    private final OrderRepository orderRepository;
    private final OrderIncidentRepository orderIncidentRepository;
    private final RuntimeService runtimeService;

    private WhitelistAttributeRepository whitelistAttributeRepository;

    @Autowired
	public OrderEventHandler(SimpMessagingTemplate websocket, EntityLinks entityLinks, OrderRepository orderRepository, OrderIncidentRepository incidentRepository, RuntimeService runtimeService, WhitelistAttributeRepository whitelistAttributeRepository) {
		this.websocket = websocket;
		this.entityLinks = entityLinks;
		this.orderRepository = orderRepository;
        this.orderIncidentRepository = incidentRepository;
        this.runtimeService = runtimeService;
        this.whitelistAttributeRepository = whitelistAttributeRepository;
	}

	@HandleAfterCreate
	public void newCartOrder(CartOrder incomingOrder) {
		Logger.getLogger(this.getClass().getName()).info("Starting process instance for " + incomingOrder);

		CartOrder newOrder = orderRepository.save(incomingOrder);

		this.websocket.convertAndSend(WebSocketConfiguration.MESSAGE_PREFIX + "/newCartOrder", getPath(newOrder));
	}

	@HandleAfterDelete
	public void deleteCartOrder(CartOrder cartOrder) {
		this.websocket.convertAndSend(
				WebSocketConfiguration.MESSAGE_PREFIX + "/deleteCartOrder", getPath(cartOrder));
	}

	@HandleBeforeSave
    @HandleBeforeCreate
	public void saveCartOrder(CartOrder incomingOrder) {
        Logger.getLogger(this.getClass().getName()).info("Saving cart order: " + incomingOrder);
		CartOrder existingOrder = orderRepository.findByTenantIdAndShoppingCartIdAndCartOrderSystemId(
				incomingOrder.getTenantId(),
				incomingOrder.getShoppingCartId(),
				incomingOrder.getCartOrderSystemId());
        boolean createIncident = false;
        if(existingOrder != null) {
            if(existingOrder.getStatus() == null || incomingOrder.getStatus() == null || !existingOrder.getStatus().equals(incomingOrder.getStatus())) {
                boolean foundOrderWithStatus = false;
                Logger.getLogger(this.getClass().getName()).info("Found " + existingOrder + " for id " + incomingOrder.getCartOrderSystemId());
                incomingOrder.setId(incomingOrder.getId());
                for(OrderIncident orderIncident : incomingOrder.getOrderIncidents()) {
                    String status = orderIncident.getStatus();
                    if(status != null && status.equals(incomingOrder.getStatus())) {
                        foundOrderWithStatus = true;
                        Logger.getLogger(this.getClass().getName()).info("Found  order with status " + status);
                    }
                }
                if(!foundOrderWithStatus) {
                    createIncident = true;
                } else {
                    Logger.getLogger(this.getClass().getName()).info("Didn't find order with status of " + incomingOrder);
                }
            } else {
                createIncident = true;
				Logger.getLogger(this.getClass().getName()).info(
                        "Could not find order for ->\n " +
				        "\ntenantId: " + incomingOrder.getTenantId() +
                        "\nshoppingCartId: " + incomingOrder.getShoppingCartId() +
                        "\ncartOrderSystemId: " +  incomingOrder.getCartOrderSystemId() +
                        "\n status: " + incomingOrder.getStatus());
			}
            if(incomingOrder.isShouldWhitelist()) {
                existingOrder.whiteListCCAndEmail(whitelistAttributeRepository);
                incomingOrder.setWhitelisted(true);
            }
            if(!incomingOrder.isWhitelisted() && incomingOrder.determineCCAndEmailWhitelisting(whitelistAttributeRepository)) {
                existingOrder.setWhitelisted(true);
                incomingOrder.setWhitelisted(true);
                this.orderRepository.save(existingOrder);
            }
        }
        if(HARD_CODED_STATUS.equalsIgnoreCase(incomingOrder.getStatus())) {
            Map<String, Object> startVariables = new HashMap();
            startVariables.put(CartOrder.CART_ORDER, existingOrder);
            ProcessInstance instance = runtimeService.startProcessInstanceByKey(existingOrder.getProcessDefinitionKey(), startVariables);
            //runtimeService.sid/d(instance.getProcessInstanceId());
            incomingOrder.setProcessInstanceId(instance.getProcessInstanceId());
            Logger.getLogger(this.getClass().getName()).info("Started process instance for new order: " + instance.getProcessInstanceId());
        }
        if(createIncident && incomingOrder.getOrderTag() != null) {
            OrderIncident.IncidentType incidentType = incomingOrder.getOrderTag().equalsIgnoreCase("cart") ? OrderIncident.IncidentType.CART_STATE_CHANGE : OrderIncident.IncidentType.AUTO_PROCESS_STATE_CHANGE;
            OrderIncident orderIncident = new OrderIncident(existingOrder, incidentType, incomingOrder.getStatus());
            orderIncident.setLog(incomingOrder.getLog());
            this.orderIncidentRepository.save(orderIncident);
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
