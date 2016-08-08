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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

import javax.persistence.*;

/**
 *
 */
@Data
@Entity
@ToString(exclude="cartOrder")
public class OrderIncident extends BaseEntity {
    private boolean isManuallyApproved;

    private IncidentType type;
    private String status;
    private String description;
    private String cartOrderSystemQualifier;

    public enum IncidentType {
        CART_STATE_CHANGE,
        AUTO_PROCESS_STATE_CHANGE,
        MANUAL_STATE_CHANGE
    }

    @Enumerated(EnumType.STRING)
    private IncidentType incidentType;

	@NonNull
	@ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
	private CartOrder cartOrder;

	public OrderIncident() {}


	public OrderIncident(CartOrder cartOrder, IncidentType type, String status) {
		this.cartOrder = cartOrder;
        this.type = type;
        this.status = status;
	}
}
