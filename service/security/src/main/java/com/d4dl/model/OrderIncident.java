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
package com.d4dl.model;

import lombok.Data;
import lombok.NonNull;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;

/**
 *
 */
@Data
@Entity
public class OrderIncident extends BaseEntity {

	private String action;
	private String transactionId;
	private String shoppingCartId;
	private String shoppingCartName;
	private String siteName;
	private String restClientId;
	private String customerId;
	private String customerName;
	private String description;
	private BigDecimal amount;

	// @NonNull
	// @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
	// private CartOrder cartOrder;

	public OrderIncident() {}


	/**
	public OrderIncident(CartOrder cartOrder) {
		this.cartOrder = cartOrder;
	}
	 **/
}
