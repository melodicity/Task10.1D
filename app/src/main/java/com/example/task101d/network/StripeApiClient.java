package com.example.task101d.network;

import com.example.task101d.BuildConfig;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StripeApiClient {

    private static Retrofit retrofit = null;

    public static StripeApiService getStripeApiService() {

        if (retrofit == null) {
            // Create an interceptor to add the authorization header
            Interceptor interceptor = chain -> {
                Request originalRequest = chain.request();
                Request newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer " + BuildConfig.STRIPE_SECRET_KEY)
                        .build();
                return chain.proceed(newRequest);
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.stripe.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }

        return retrofit.create(StripeApiService.class);
    }
}
