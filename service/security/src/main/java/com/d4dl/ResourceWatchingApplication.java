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

import org.activiti.spring.SpringAsyncExecutor;
import org.activiti.spring.SpringCallerRunsRejectedJobsHandler;
import org.activiti.spring.SpringRejectedJobsHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

/**
 *
 */
@SpringBootApplication
@Configuration
@EnableSpringConfigured
@ComponentScan("com.d4dl.model, com.d4dl.data, com.d4dl.controller")
public class ResourceWatchingApplication {
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
}
