package com.example.task101d.model;

public class QuizItem {
    private int id;
    private String topic;

    public QuizItem(int id, String topic) {
        this.id = id;
        this.topic = topic;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTopic() { return topic; }
    public void setTopic(String todo) { this.topic = todo; }

    public String getTitle() { return "Generated Task " + id; }
    public String getDescription() { return "A quiz about " + topic; }
}
