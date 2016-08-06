package com.d4dl.model;

import com.d4dl.data.WhitelistAttributeRepository;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by joshuadeford on 7/31/16.
 */
@Data
@Entity
public class CartOrder extends BaseEntity {

    public static final String CART_ORDER = "cartOrder";
    private boolean isManuallyApproved;
    private String processInstanceId;
    private String baseEndpoint;
    private String orderTag;

    private String action;
    private String transactionId;
    private String processDefinitionKey;//The process definition that handles these kinds of incidents per the client request
    private String cartOrderSystemId;
    private String status;;
    private String shoppingCartType;
    private String shoppingCartId;
    private String shoppingCartName;
    private String siteName;
    private String tenantId;
    private BigDecimal amount;

    @Autowired
    @Transient
    @JsonIgnore
    private WhitelistAttributeRepository whitelistAttributeRepository;

    @OneToMany(cascade = CascadeType.MERGE, mappedBy = CART_ORDER, fetch = FetchType.EAGER)
    private List<OrderIncident> orderIncidents = new ArrayList();

    private String ccLastFour;
    private String email;

    @ManyToOne
    private Customer customer;
    private String cartEndpoint;

    public CartOrder() {
    }

    public CartOrder(String orderId) {
        this.cartOrderSystemId = orderId;;
    }

    public boolean determineCCAndEmailWhitelisting() {
        String orderEmail = getOrderEmail();
        Logger.getLogger(this.getClass().getName()).info("Checking if " + orderEmail + " and " + ccLastFour + " is whitelisted.");
        if(ccLastFour == null || orderEmail == null) {
            return false;
        }

        Map<String, String> attrMap = fetchCCEmailWhitelistMap(orderEmail);
        return (ccLastFour.equals(attrMap.get("ccLastFour")) && orderEmail.equals(attrMap.get("email")));
    }

    @JsonIgnore
    public void whiteListCCAndEmail() {
        whiteListCCAndEmail(whitelistAttributeRepository);
    }

    @JsonIgnore
    public void whiteListCCAndEmail(WhitelistAttributeRepository whitelistAttributeRepository) {
        String orderEmail = getOrderEmail();
        if(ccLastFour == null || orderEmail == null) {
            return;
        }

        Map<String, String> attrMap = fetchCCEmailWhitelistMap(orderEmail, whitelistAttributeRepository);
        if(!ccLastFour.equals(attrMap.get("ccLastFour"))) {
            whitelistAttributeRepository.save(new WhitelistAttribute(customer, "ccLastFour", ccLastFour));
        }
        if(orderEmail.equals(attrMap.get("email"))) {
            whitelistAttributeRepository.save(new WhitelistAttribute(customer, "email", email));
        }
    }

    @JsonIgnore
    private String getOrderEmail() {
        return (this.email == null && customer != null) ? customer.getEmail() : this.email;
    }


    private Map<String, String> fetchCCEmailWhitelistMap(String email) {
        return fetchCCEmailWhitelistMap(email, this.whitelistAttributeRepository);
    }

    private Map<String, String> fetchCCEmailWhitelistMap(String email, WhitelistAttributeRepository whitelistAttributeRepository) {
        List<WhitelistAttribute> whitelistAttributes = whitelistAttributeRepository.findByCustomerAndNameInAndValueIn(
                customer,
                new String[]{"ccLastFour", "email"},
                new String[]{ccLastFour, email});
        Map<String, String> attrMap = new HashMap();

        for (WhitelistAttribute attr : whitelistAttributes) attrMap.put(attr.getName(),attr.getValue());
        return attrMap;
    }

    public void addOrderIncident(OrderIncident orderIncident) {
        orderIncident.setCartOrder(this);
        this.orderIncidents.add(orderIncident);
    }

    public String getCartEndpoint() {
        return baseEndpoint + "/" + cartOrderSystemId;
    }

    public boolean getIsManuallyApproved() {
        return isManuallyApproved;
    }

    public Object getOrderIncident(String status) {
        for(OrderIncident orderIncident : orderIncidents) {
            if(status.equals(orderIncident.getStatus())) {
                return orderIncident;
            }
        }
        return null;
    }

    public void removeOrderIncident(OrderIncident orderIncident) {
        orderIncidents.remove(orderIncident);
    }
}
