package com.d4dl.model;

import org.activiti.engine.impl.util.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jdeford on 8/3/16.
 */
public class HardCodedMagentoStuff {


    public static String UPDATE_ORDER = "http://remanplanet.com/drupal/d4dl_resource_watch/update";
    public static String LOGIN_URL = "http://remanplanet.com/drupal/api/1.0/user/login.json";
    public static String ORDERS_URL = "http://remanplanet.com/drupal/users/admin#overlay=admin/commerce/orders/";
    public static String DRUPAL_USERNAME = "admin";
    public static String DRUPAL_PASSWORD = "admin";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    /**
     * Drupal order
     * 1) They come in as 'Paid Awaiting Shipping X'
     * 2) Orders that need review are 'Pending'
     * 3) Order that are good to ship are 'Pending' with a Checkmark.
     *
     *
     * Things looked at billing matches shipping
     * AVS Response matches
     * Is it express? Express orders on strive tend to always be fraud
     * Logs
     */

    public static void main(String[] args) throws Exception {
        String orderId = "29";
        CartOrder order = new CartOrder(orderId);
        order.setStatus("OK To Ship");
        new HardCodedMagentoStuff().updateOrder(order);
    }

    public OrderIncident updateOrder(CartOrder order) throws URISyntaxException {
        String cookie = fetchCooke();
        RestTemplate restTemplate = new RestTemplate();
        updateStatus(order, cookie, restTemplate);
        OrderIncident incident = new OrderIncident();
        incident.setIncidentType(OrderIncident.IncidentType.AUTO_PROCESS_STATE_CHANGE);
        incident.setCartOrder(order);
        return incident;
    }

    private void updateStatus(CartOrder order, String cookie, RestTemplate restTemplate) {
        MultiValueMap<String, String> headers = initHeaders(cookie);

        String repairsUniverseToken = "9c3dbb5567f646cb9a2fe284b26e53c8b11fb04c62374122b5626d20067f6025";
        HttpEntity<Map<String, String>> entity = new HttpEntity(order, headers);
        String updateOrderURL = getUpdateOrderURL();
        restTemplate.put(updateOrderURL, entity);
    }

    private String fetchCooke() throws URISyntaxException {
        RestTemplate loginTemplate = new RestTemplate();
        loginTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                //Even if there's an error the header may still have the necessary cookie which
                //is the only point of this service call.
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {

            }
        });
        HashMap loginParams = new HashMap();
        loginParams.put(USERNAME, DRUPAL_USERNAME);
        loginParams.put(PASSWORD, DRUPAL_PASSWORD);
        ResponseEntity<HashMap> loginResponse = loginTemplate.postForEntity(new URI(LOGIN_URL), loginParams, HashMap.class);
        return loginResponse.getHeaders().get("Set-Cookie").get(0);
    }

    private static MultiValueMap<String, String> initHeaders(String cookie) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        // Authorization: Basic amRlZm9yZDpPcGVuU2VzYW1l
        //headers.add("X-CSRF-Token", "oSLOQJK6I00ORgX8z3Xyg7LeKJk4V5mnCWhlmQ2lWR0");
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
        headers.add("Cookie", cookie);
        return headers;
    }

    public String getUpdateOrderPayloadString(String orderId, String revision, String status) {
        Map<String, String> payload = new HashMap();
        payload.put("revision_id", revision);
        payload.put("status", status);
        payload.put("order_id", orderId);
        payload.put("type", "commerce_order");
        return new JSONObject(payload).toString();
    }

    public String getUpdateOrderURL() {
        return UPDATE_ORDER;
    }
}
