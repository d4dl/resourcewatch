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
package com.d4dl;

import com.d4dl.controller.tasks.UpdateOrderStatusTask;
import com.d4dl.data.OrderRepository;
import com.d4dl.model.CartOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.spring.SpringAsyncExecutor;
import org.activiti.spring.SpringCallerRunsRejectedJobsHandler;
import org.activiti.spring.SpringRejectedJobsHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import java.util.List;
import java.util.logging.Logger;

/**
 *
 */
@SpringBootApplication
@Configuration
@EnableSpringConfigured
@ComponentScan("com.d4dl.model, com.d4dl.data, com.d4dl.controller, com.d4dl.config")
public class ResourceWatchingApplication implements RepositoryRestConfigurer {

	@Autowired
	private OrderRepository orderRepository;

	public static void main(String[] args) {
		SpringApplication.run(ResourceWatchingApplication.class, args);
	}


	@Bean
	@Primary
	public SpringAsyncExecutor springAsyncExecutor() {
		return new SpringAsyncExecutor(new SimpleAsyncTaskExecutor(), springRejectedJobsHandler());
	}

	@Bean
	@Primary
	public SpringRejectedJobsHandler springRejectedJobsHandler() {
		return new SpringCallerRunsRejectedJobsHandler();
	}

	@Bean
	public UpdateOrderStatusTask updateOrderStatusTask() {
		return new UpdateOrderStatusTask();
	}

	@Override
	public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {

	}

	@Override
	public void configureConversionService(ConfigurableConversionService conversionService) {

	}

	@Override
	public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {
		validatingListener.addValidator("beforeCreate", new Validator() {
			public boolean supports(Class clazz) {
				return CartOrder.class.isAssignableFrom(clazz);
			}

			public void validate(Object target, Errors errors) {
				Logger.getLogger(getClass().getName()).info("Validating cartOrder " + target);
				CartOrder incomingOrder = (CartOrder) target;
				CartOrder existingOrder = orderRepository.findByTenantIdAndShoppingCartIdAndCartOrderSystemId(
						incomingOrder.getTenantId(),
						incomingOrder.getShoppingCartId(),
						incomingOrder.getCartOrderSystemId());
				if(existingOrder != null) {
					Logger.getLogger(getClass().getName()).info("cartOrder found" + target);
					errors.reject("ORDER_ALREADY_EXISTS", "The order already exists. No big deal. Not creating it." + incomingOrder);
				} else {
					Logger.getLogger(getClass().getName()).info("cartOrder not found" + target);
				}

				if(incomingOrder.getTenantId() == null ||
						incomingOrder.getShoppingCartId() == null ||
						incomingOrder.getCartOrderSystemId() == null) {
					if(incomingOrder.getOrderIncidents().size() == 0) {
						errors.reject("ALL_ATTRIBUTES_NOT_SET", "tenantId, shoppingCartId, orderSystemId must all be set. " + incomingOrder);
					}
				}
			}
		});
	}

	@Override
	public void configureExceptionHandlerExceptionResolver(ExceptionHandlerExceptionResolver exceptionResolver) {

	}

	@Override
	public void configureHttpMessageConverters(List<HttpMessageConverter<?>> messageConverters) {

	}

	@Override
	public void configureJacksonObjectMapper(ObjectMapper objectMapper) {

	}

}
