package com.example.task101d.network;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

public class QuestionItem {
    @SerializedName("correct_answer") // allows for camel case to be used instead, since the fields must otherwise have the same name
    private String correctAnswer;
    private String[] options;
    private String question;

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correct_answer) { this.correctAnswer = correct_answer; }
    public String[] getOptions() { return options; }
    public void setOptions(String[] options) { this.options = options; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    // Method to get the answer in integer format, rather than a letter
    public int getCorrectAnswerInt() {
        switch (correctAnswer) {
            case "A":
                return 0;
            case "B":
                return 1;
            case "C":
                return 2;
            case "D":
                return 3;
            default:
                Log.e("MainActivity", "Failed to fetch answer");
                return -1;
        }
    }
}
