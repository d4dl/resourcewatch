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
import com.d4dl.data.OrderRepository;
import com.d4dl.model.OrderIncident;
import org.activiti.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.hateoas.EntityLinks;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 *
 */
// tag::code[]
@Component
@RepositoryEventHandler(OrderIncident.class)
public class OrderIncidentEventHandler {

	private final SimpMessagingTemplate websocket;

	private final EntityLinks entityLinks;
    private final OrderRepository orderRepository;
    private final RuntimeService runtimeService;

    @Autowired
	public OrderIncidentEventHandler(SimpMessagingTemplate websocket, EntityLinks entityLinks, OrderRepository orderRepository, RuntimeService runtimeService) {
		this.websocket = websocket;
		this.entityLinks = entityLinks;
		this.orderRepository = orderRepository;
        this.runtimeService = runtimeService;
	}

	@HandleAfterCreate
	public void newOrderIncident(OrderIncident orderIncident) {
		Logger.getLogger(this.getClass().getName()).info("Created OrderIncident " + orderIncident);
		this.websocket.convertAndSend(
				WebSocketConfiguration.MESSAGE_PREFIX + "/newOrderIncident", getPath(orderIncident));
	}

	@HandleAfterDelete
	public void deleteOrderIncident(OrderIncident orderIncident) {
		this.websocket.convertAndSend(
				WebSocketConfiguration.MESSAGE_PREFIX + "/deleteOrderIncident", getPath(orderIncident));
	}

	@HandleAfterSave
	public void updateOrderIncident(OrderIncident orderIncident) {
		this.websocket.convertAndSend(
				WebSocketConfiguration.MESSAGE_PREFIX + "/updateOrderIncident", getPath(orderIncident));
	}


	/**
	 * Take an {@link OrderIncident} and get the URI using Spring Data REST's {@link EntityLinks}.
	 *
	 * @param orderIncident
	 */
	private String getPath(OrderIncident orderIncident) {
		return this.entityLinks.linkForSingleResource(orderIncident.getClass(),
				orderIncident.getId()).toUri().getPath();
	}

}
// end::code[]
