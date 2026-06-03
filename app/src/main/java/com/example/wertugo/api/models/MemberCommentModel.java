package com.example.wertugo.api.models;

public class MemberCommentModel {
    private String userName;
    private float rating;
    private String commentText;

    public MemberCommentModel(String userName, float rating, String commentText) {
        this.userName = userName;
        this.rating = rating;
        this.commentText = commentText;
    }

    public String getUserName() { return userName; }
    public float getRating() { return rating; }
    public String getCommentText() { return commentText; }
}