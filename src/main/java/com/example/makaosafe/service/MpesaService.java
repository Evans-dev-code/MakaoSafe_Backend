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
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpesaService {

    private final MpesaConfig mpesaConfig;
    private final RestTemplate restTemplate;

    public String getAccessToken() {
        // Construct the Base64 string: ConsumerKey:ConsumerSecret
        String auth = mpesaConfig.getConsumerKey() + ":" + mpesaConfig.getConsumerSecret();
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII));
        String authHeader = "Basic " + new String(encodedAuth);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // Safaricom Sandbox requires the ?grant_type=client_credentials query param
            String url = mpesaConfig.getAuthUrl() + "?grant_type=client_credentials";

            ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    AccessTokenResponse.class
            );

            if (response.getBody() != null) {
                return response.getBody().getAccessToken();
            }
            throw new RuntimeException("Empty response from Safaricom");
        } catch (Exception e) {
            log.error("Failed to get M-Pesa Access Token. Check your Consumer Key/Secret: {}", e.getMessage());
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
            log.error("STK Push Failed. Detailed Error: ", e);
            throw new RuntimeException("M-Pesa STK Push Initiation failed");
        }
    }
}