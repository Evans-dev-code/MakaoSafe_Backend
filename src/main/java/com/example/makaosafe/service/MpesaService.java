package com.example.makaosafe.service;

import com.example.makaosafe.config.MpesaConfig;
import com.example.makaosafe.dto.AccessTokenResponse;
import com.example.makaosafe.dto.StkPushResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpesaService {

    private final MpesaConfig mpesaConfig;
    private final RestTemplate restTemplate;

    // 1. Get Access Token from Safaricom
    public String getAccessToken() {
        String keys = mpesaConfig.getConsumerKey() + ":" + mpesaConfig.getConsumerSecret();
        String encodedKeys = Base64.getEncoder().encodeToString(keys.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedKeys);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(
                mpesaConfig.getAuthUrl(),
                HttpMethod.GET,
                entity,
                AccessTokenResponse.class
        );

        return response.getBody().getAccessToken();
    }

    // 2. Initiate STK Push
    public StkPushResponse initiateStkPush(String phoneNumber, double amount, String accountReference) {
        String token = getAccessToken();
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        // Password = Base64(Shortcode + Passkey + Timestamp)
        String passwordStr = mpesaConfig.getBusinessShortcode() + mpesaConfig.getPasskey() + timestamp;
        String password = Base64.getEncoder().encodeToString(passwordStr.getBytes());

        // Construct the JSON Payload
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("BusinessShortCode", mpesaConfig.getBusinessShortcode());
        requestBody.put("Password", password);
        requestBody.put("Timestamp", timestamp);
        requestBody.put("TransactionType", "CustomerPayBillOnline"); // Use "CustomerBuyGoodsOnline" if using Till
        requestBody.put("Amount", (int) amount); // Sandbox only accepts whole numbers usually
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
            log.error("Error sending STK Push: ", e);
            throw new RuntimeException("Failed to initiate M-Pesa payment");
        }
    }
}