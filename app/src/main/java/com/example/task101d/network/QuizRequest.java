package com.example.task101d.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface QuizRequest {
    @GET("getQuiz")
    Call<QuizResponse> getQuestions(@Query("topic") String topic);
}
