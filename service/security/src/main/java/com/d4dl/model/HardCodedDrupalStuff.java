package com.d4dl.model;

import org.activiti.engine.impl.util.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jdeford on 8/3/16.
 */
public class HardCodedDrupalStuff {

    public static final String X_CSRF_TOKEN = "X-CSRF-Token";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    public static void main(String[] args) throws Exception {
        String orderId = "29";
        String status = "TESTING22";
        String revisionId = "134";


        new HardCodedDrupalStuff().updateOrder(new CartOrder(orderId, revisionId), status);
    }

    public OrderIncident updateOrder(CartOrder order, String status) throws URISyntaxException {
        String cookie = fetchCooke();
        String token = fetchToken(cookie);
        RestTemplate restTemplate = new RestTemplate();
        updateStatus(order.getCartOrderSystemId(), order.getCartOrderSystemQualifier(), status, cookie, token, restTemplate);
        OrderIncident incident = new OrderIncident();
        incident.setIncidentType(OrderIncident.IncidentType.AUTO_PROCESS_STATE_CHANGE);
        incident.setCartOrder(order);
        incident.setAction("UPDATE_STATUS");
        return incident;
    }

    private void updateStatus(String orderId, String revisionId, String status, String cookie, String token, RestTemplate restTemplate) {
        MultiValueMap<String, String> headers = initHeaders(cookie);
        headers.add(X_CSRF_TOKEN, token);

        HttpEntity<Map<String, String>> entity = new HttpEntity(getUpdateOrderPayload(orderId, revisionId, status), headers);
        String updateOrderURL = getUpdateOrderURL(orderId);
        restTemplate.put(updateOrderURL, entity);
    }

    private String fetchToken(String cookie) throws URISyntaxException {
        RestTemplate tokenTemplate = new RestTemplate();
        MultiValueMap<String, String> headers = initHeaders(cookie);
        HttpEntity<Map<String, String>> tokenEntity = new HttpEntity(headers);
        ResponseEntity<HashMap> tokenResponse = tokenTemplate.postForEntity(new URI(XSRF_TOKEN_URL), tokenEntity, HashMap.class);

        return (String) tokenResponse.getBody().get("token");
    }

    private String fetchCooke() throws URISyntaxException {
        RestTemplate loginTemplate = new RestTemplate();
        HashMap loginParams = new HashMap();
        loginParams.put(USERNAME, DRUPAL_USERNAME);
        loginParams.put(PASSWORD, DRUPAL_PASSWORD);
        ResponseEntity<HashMap> loginResponse = loginTemplate.postForEntity(new URI(LOGIN_URL), loginParams, HashMap.class);
        return loginResponse.getHeaders().get("Set-Cookie").get(0);
    }

    private static MultiValueMap<String, String> initHeaders(String cookie) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        //headers.add("X-CSRF-Token", "oSLOQJK6I00ORgX8z3Xyg7LeKJk4V5mnCWhlmQ2lWR0");
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
        headers.add("Cookie", cookie);
        return headers;
    }

    private static Map<String, String> getXsrfPayload(String cookie) {
        Map<String, String> payload = new HashMap();

        payload.put("username", DRUPAL_USERNAME);
        payload.put("password", DRUPAL_PASSWORD);
        payload.put("Cookie", cookie);
        return payload;
    }

    HttpHeaders createHeaders(String username, String password) {
        return new HttpHeaders() {
            {
                String auth = username + ":" + password;
                byte[] encodedAuth = java.util.Base64.getEncoder().encode(auth.getBytes((Charset.forName("US-ASCII"))));
                String authHeader = "Basic " + new String(encodedAuth);
                set("Authorization", authHeader);
            }
        };
    }

    public static String UPDATE_ORDER = "http://remanplanet.com/drupal/api/1.0/order/ORDER_ID.json";
    public static String XSRF_TOKEN_URL = "http://remanplanet.com/drupal/api/1.0/user/token.json";
    public static String LOGIN_URL = "http://remanplanet.com/drupal/api/1.0/user/login.json";
    public static String TOKEN_URL = "http://admin:Quicks@nd1@remanplanet.com/drupal/api/1.0/services_token/generate";
    public static String DRUPAL_USERNAME = "admin";
    public static String DRUPAL_PASSWORD = "admin";
    //public static String DRUPAL_PASSWORD = "Quicks@nd1";


    public Map<String, String> getUpdateOrderPayload(String orderId, String revision, String status) {
        Map<String, String> payload = new HashMap();
        payload.put("revision_id", revision);
        payload.put("status", status);
        payload.put("order_id", orderId);
        payload.put("order_id", orderId);
        payload.put("type", "commerce_order");
        //payload.put("username", DRUPAL_USERNAME);
        //payload.put("password", DRUPAL_PASSWORD);
        return payload;
    }

    public String getUpdateOrderPayloadString(String orderId, String revision, String status) {
        Map<String, String> payload = new HashMap();
        payload.put("revision_id", revision);
        payload.put("status", status);
        payload.put("order_id", orderId);
        payload.put("type", "commerce_order");
        return new JSONObject(payload).toString();
    }

    public String getUpdateOrderURL(String orderId) {
        return UPDATE_ORDER.replace("ORDER_ID", orderId);
    }
}
