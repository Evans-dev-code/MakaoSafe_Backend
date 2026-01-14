package com.example.makaosafe.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "daraja")
public class MpesaConfig {
    private String consumerKey;
    private String consumerSecret;
    private String businessShortcode;
    private String passkey;
    private String callbackUrl;
    private String apiUrl;
    private String authUrl;
}