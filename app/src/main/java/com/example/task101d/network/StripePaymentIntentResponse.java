package com.example.task101d.network;

public class StripePaymentIntentResponse {
    private String id;
    private String client_secret;

    public String getId() {
        return id;
    }

    public String getClientSecret() {
        return client_secret;
    }
}
