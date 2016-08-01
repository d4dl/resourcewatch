package com.d4dl.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by joshuadeford on 7/31/16.
 */
@Data
@Entity
public class WhitelistAttribute extends BaseEntity {
    @ManyToOne
    private Customer customer;
    private String name;
    private String value;

    public WhitelistAttribute() {

    }

    public WhitelistAttribute(Customer customer, String name, String value) {
        this.customer = customer;
        this.name = name;
        this.value = value;
    }
}
