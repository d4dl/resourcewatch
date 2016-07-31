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
package com.d4dl.data;

import com.d4dl.model.Employee;
import com.d4dl.model.Manager;
import com.d4dl.model.OrderIncident;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 *
 */
// tag::code[]
@Component
public class DatabaseLoader implements CommandLineRunner {

	private final EmployeeRepository employees;
	private final ManagerRepository managers;
	private final OrderIncidentRepository orderIncidents;

	@Autowired
	public DatabaseLoader(OrderIncidentRepository orderIncidentRepository,
			              EmployeeRepository employeeRepository,
						  ManagerRepository managerRepository) {

		this.employees = employeeRepository;
		this.orderIncidents = orderIncidentRepository;
		this.managers = managerRepository;
	}

	@Override
	public void run(String... strings) throws Exception {

		Manager greg = this.managers.save(new Manager("jdeford", "OpenSesame",
							"ROLE_MANAGER"));
		//Manager oliver = this.managers.save(new Manager("oliver", "gierke",
							//"ROLE_MANAGER"));

		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken("jdeford", "doesn't matter",
				AuthorityUtils.createAuthorityList("ROLE_MANAGER")));

		this.employees.save(new Employee("Frodo", "Baggins", "ring bearer", greg));
		this.employees.save(new Employee("Bilbo", "Baggins", "burglar", greg));
		this.employees.save(new Employee("Gandalf", "the Grey", "wizard", greg));

		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken("oliver", "doesn't matter",
				AuthorityUtils.createAuthorityList("ROLE_MANAGER")));

		OrderIncident orderIncident = new OrderIncident();
		orderIncident.setAction("Init System");
		orderIncident.setCustomerEmail("jdeford@gmail.com");
		orderIncident.setAmount(new BigDecimal(0));
		orderIncident.setTransactionId("653064af-8c9d-496f");
		orderIncident.setShoppingCartName("Resource Matcher");
		orderIncident.setDescription("Resource Matcher Processing Orders");
		this.orderIncidents.save(orderIncident);
		//this.employees.save(new Employee("Samwise", "Gamgee", "gardener", oliver));
		//this.employees.save(new Employee("Merry", "Brandybuck", "pony rider", oliver));
		//this.employees.save(new Employee("Peregrin", "Took", "pipe smoker", oliver));

		SecurityContextHolder.clearContext();
	}
}
// end::code[]