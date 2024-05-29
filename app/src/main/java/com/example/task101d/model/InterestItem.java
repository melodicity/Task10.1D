package com.example.task101d.model;

public class InterestItem {
    private String interest;
    private boolean selected;

    public InterestItem(String topic) {
        this.interest = topic;
        this.selected = false;
    }

    public String getTopic() { return interest; }
    public void setTopic(String todo) { this.interest = todo; }
    public Boolean getSelected() { return selected; }
    public void setSelected(Boolean selected) { this.selected = selected; }

    public Boolean toggleSelected() {
        this.selected = !this.selected;
        return this.selected;
    }
}
