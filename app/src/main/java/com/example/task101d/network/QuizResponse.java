package com.example.task101d.network;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QuizResponse {
    @SerializedName("quiz") // Tells Gson to map the JSON field "quiz" to the Java field "questions";
                            // Without this, the below field must match the field in the JSON!
    private List<QuestionItem> questions;

    public List<QuestionItem> getQuestions() { return questions; }
    public void setQuestions(List<QuestionItem> questions) { this.questions = questions; }
}
