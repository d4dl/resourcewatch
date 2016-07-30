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
package com.greglturnquist.payroll;

import org.activiti.spring.SpringAsyncExecutor;
import org.activiti.spring.SpringCallerRunsRejectedJobsHandler;
import org.activiti.spring.SpringRejectedJobsHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author Greg Turnquist
 */
// tag::code[]
@SpringBootApplication
@EnableAutoConfiguration(exclude = {
		org.activiti.spring.boot.RestApiAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration.class,
		org.activiti.spring.boot.SecurityAutoConfiguration.class,
		org.activiti.spring.boot.JpaProcessEngineAutoConfiguration.class,
		ThreadPoolTaskExecutor.class
})
@Configuration
@EnableSpringConfigured
public class ReactAndSpringDataRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactAndSpringDataRestApplication.class, args);
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
}
// end::code[]