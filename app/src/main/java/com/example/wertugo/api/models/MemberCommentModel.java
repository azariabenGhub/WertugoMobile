package com.example.wertugo.api.models;

public class MemberCommentModel {
    private String userName;
    private float rating;
    private String comment;

    // Konstruktor
    public MemberCommentModel(String userName, float rating, String comment) {
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
    }

    // Getter dan Setter untuk Nama
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    // Getter dan Setter untuk Rating
    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    // Getter dan Setter untuk Komentar
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}