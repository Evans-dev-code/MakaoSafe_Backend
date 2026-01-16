package com.example.makaosafe.service;

import com.example.makaosafe.config.MpesaConfig;
import com.example.makaosafe.dto.AccessTokenResponse;
import com.example.makaosafe.dto.StkPushResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpesaService {

    private final MpesaConfig mpesaConfig;
    private final RestTemplate restTemplate;

    public String getAccessToken() {
        String auth = mpesaConfig.getConsumerKey() + ":" + mpesaConfig.getConsumerSecret();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.ISO_8859_1));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // We use a simple string URL to avoid double-appending the query param
            String url = mpesaConfig.getAuthUrl().trim();
            if (!url.contains("?")) {
                url += "?grant_type=client_credentials";
            }

            log.info("Requesting token from: {}", url);

            ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    AccessTokenResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody().getAccessToken();
            }
            throw new RuntimeException("Unexpected response status: " + response.getStatusCode());
        } catch (Exception e) {
            log.error("M-Pesa Auth Error: {}", e.getMessage());
            throw new RuntimeException("M-Pesa Auth failed: " + e.getMessage());
        }
    }

    public StkPushResponse initiateStkPush(String phoneNumber, double amount, String accountReference) {
        String token = getAccessToken();
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        String passwordStr = mpesaConfig.getBusinessShortcode() + mpesaConfig.getPasskey() + timestamp;
        String password = Base64.getEncoder().encodeToString(passwordStr.getBytes());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("BusinessShortCode", mpesaConfig.getBusinessShortcode());
        requestBody.put("Password", password);
        requestBody.put("Timestamp", timestamp);
        requestBody.put("TransactionType", "CustomerPayBillOnline");
        requestBody.put("Amount", (int) Math.round(amount));
        requestBody.put("PartyA", phoneNumber);
        requestBody.put("PartyB", mpesaConfig.getBusinessShortcode());
        requestBody.put("PhoneNumber", phoneNumber);
        requestBody.put("CallBackURL", mpesaConfig.getCallbackUrl());
        requestBody.put("AccountReference", accountReference);
        requestBody.put("TransactionDesc", "Booking Payment");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Content-Type", "application/json");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<StkPushResponse> response = restTemplate.postForEntity(
                    mpesaConfig.getApiUrl(),
                    entity,
                    StkPushResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("STK Push Failed: {}", e.getMessage());
            throw new RuntimeException("M-Pesa payment initiation failed");
        }
    }
}