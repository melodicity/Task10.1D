package com.example.task101d.network;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface StripeApiService {
    @FormUrlEncoded
    @POST("v1/payment_intents")
    Call<StripePaymentIntentResponse> createPaymentIntent(
            @Field("amount") int amount,
            @Field("currency") String currency,
            @Field("payment_method_types[]") String paymentMethodType
    );
}
