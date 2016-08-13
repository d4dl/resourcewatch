package com.d4dl.model;

import com.d4dl.data.WhitelistAttributeRepository;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.annotation.PostConstruct;
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
    private String paymentMethod;
    private String baseEndpoint;
    private String orderTag;
    private String log;
    private String url;

    private String action;
    private String transactionId;
    private String processDefinitionKey;//The process definition that handles these kinds of incidents per the client request
    private String cartOrderSystemId;
    private String status;
    private String shoppingCartType;
    private String shoppingCartId;
    private String shoppingCartName;
    private String siteName;
    private String tenantId;
    private boolean shouldWhitelist;
    private boolean isWhitelisted;
    private String shippingCarrier;
    private String customerName;
    private String shippingAddress;
    private String billingAddress;
    private boolean workflowComplete;
    private BigDecimal amount;


    @OneToMany(cascade = CascadeType.MERGE, mappedBy = CART_ORDER, fetch = FetchType.EAGER)
    private List<OrderIncident> orderIncidents = new ArrayList();

    private String ccLastFour;
    private String email;

    @ManyToOne
    private Customer customer;
    private String cartEndpoint;

    public CartOrder() {
    }

    public boolean determineCCAndEmailWhitelisting(WhitelistAttributeRepository whitelistAttributeRepository) {
        String orderEmail = getOrderEmail();
        Logger.getLogger(this.getClass().getName()).info("Checking if " + orderEmail + " and " + ccLastFour + " is whitelisted.");
        if (ccLastFour == null || orderEmail == null) {
            return false;
        }

        Map<String, String> attrMap = fetchCCEmailWhitelistMap(orderEmail, whitelistAttributeRepository);
        return (ccLastFour.equals(attrMap.get("ccLastFour")) && orderEmail.equals(attrMap.get("email")));
    }



    @PostConstruct
    public void postCreate() {
        if (shoppingCartType.equals("drupal_commerce_rest")) {
            url = HardCodedDrupalStuff.ORDERS_URL + cartOrderSystemId;
        } else if (shoppingCartType.equals("magento_commerce_rest")) {
            url = HardCodedMagentoStuff.ORDERS_URL + cartOrderSystemId;
        }
        Logger.getLogger(this.getClass().getName()).info("Postprocessed order: " + url);
    }

    public CartOrder(String orderId, String url) {
        this.cartOrderSystemId = orderId;
    }

    @JsonIgnore
    public void whiteListCCAndEmail(WhitelistAttributeRepository whitelistAttributeRepository) {
        if (!couldWhitelist()) {
            return;
        }
        whitelistAttributeRepository.save(new WhitelistAttribute(customer, "ccLastFour", ccLastFour));
        whitelistAttributeRepository.save(new WhitelistAttribute(customer, "email", getOrderEmail()));
    }

    public boolean couldWhitelist() {
        return getOrderEmail() != null && ccLastFour != null;
    }

    @JsonIgnore
    private String getOrderEmail() {
        return (this.email == null && customer != null) ? customer.getEmail() : this.email;
    }


    private Map<String, String> fetchCCEmailWhitelistMap(String email, WhitelistAttributeRepository whitelistAttributeRepository) {
        List<WhitelistAttribute> whitelistAttributes = whitelistAttributeRepository.findByCustomerAndNameInAndValueIn(
                customer,
                new String[]{"ccLastFour", "email"},
                new String[]{ccLastFour, email});
        Map<String, String> attrMap = new HashMap();

        for (WhitelistAttribute attr : whitelistAttributes) attrMap.put(attr.getName(), attr.getValue());
        return attrMap;
    }

    public void addOrderIncident(OrderIncident orderIncident) {
        orderIncident.setCartOrder(this);
        this.orderIncidents.add(orderIncident);
    }

    public String getCartEndpoint() {
        if (shoppingCartType.equals("drupal_commerce_rest")) {
            url = HardCodedDrupalStuff.ORDERS_URL + cartOrderSystemId;
        } else if (shoppingCartType.equals("magento_commerce_rest")) {
            url = HardCodedMagentoStuff.ORDERS_URL + cartOrderSystemId;
        }
        return baseEndpoint + "/" + cartOrderSystemId;
    }

    public boolean getIsManuallyApproved() {
        return isManuallyApproved;
    }

    public Object getOrderIncident(String status) {
        for (OrderIncident orderIncident : orderIncidents) {
            if (status.equals(orderIncident.getStatus())) {
                return orderIncident;
            }
        }
        return null;
    }

    public void removeOrderIncident(OrderIncident orderIncident) {
        orderIncidents.remove(orderIncident);
    }
}
