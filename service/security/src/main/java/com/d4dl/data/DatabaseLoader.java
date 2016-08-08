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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 *
 */
// tag::code[]
@Component
public class DatabaseLoader implements CommandLineRunner {

	private final EmployeeRepository employees;
	private final WhitelistAttributeRepository whitelistAttributeRepository;
	private final ManagerRepository managers;
	private final OrderRepository orderRepository;
	private final CustomerRepository customerRepository;

	@Autowired
	public DatabaseLoader(OrderRepository orderRepository,
						  EmployeeRepository employeeRepository,
						  ManagerRepository managerRepository,
						  CustomerRepository customerRepository,
						  WhitelistAttributeRepository whitelistAttributeRepository) {

		this.employees = employeeRepository;
		this.orderRepository = orderRepository;
		this.managers = managerRepository;
		this.customerRepository = customerRepository;
		this.whitelistAttributeRepository = whitelistAttributeRepository;
	}

	@Override
	public void run(String... strings) throws Exception {

		Manager lenny = new Manager("lenny", "repair", "ROLE_MANAGER");
		lenny.setRoles(new String[]{"ROLE_MANAGER", "ORDER_PROCESSOR"});
		Manager lenny1 = this.managers.save(lenny);

        Manager manager = new Manager("jdeford", "OpenSesame", "ROLE_MANAGER");
        manager.setRoles(new String[]{"ROLE_MANAGER", "ORDER_PROCESSOR"});
        Manager myself = this.managers.save(manager);

		//Manager oliver = this.managers.save(new Manager("oliver", "gierke",
							//"ROLE_MANAGER"));

		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken("jdeford", "doesn't matter",
				AuthorityUtils.createAuthorityList("ROLE_MANAGER")));

		this.employees.save(new Employee("Frodo", "Baggins", "ring bearer", myself));
		this.employees.save(new Employee("Bilbo", "Baggins", "burglar", myself));
		this.employees.save(new Employee("Gandalf", "the Grey", "wizard", myself));

		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken("oliver", "doesn't matter",
				AuthorityUtils.createAuthorityList("ROLE_MANAGER")));
		//this.employees.save(new Employee("Samwise", "Gamgee", "gardener", oliver));
		//this.employees.save(new Employee("Merry", "Brandybuck", "pony rider", oliver));
		//this.employees.save(new Employee("Peregrin", "Took", "pipe smoker", oliver));

		SecurityContextHolder.clearContext();
	}
}
// end::code[]