package com.example.wertugo.api.models;

public class Review {
    private String id;
    private String user_id;
    private String user_name;
    private String user_avatar;
    private float rating;
    private String comment;
    private String created_at;

    // Constructor, getters, and setters
    public Review() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUser_id() { return user_id; }
    public void setUser_id(String user_id) { this.user_id = user_id; }

    public String getUser_name() { return user_name; }
    public void setUser_name(String user_name) { this.user_name = user_name; }

    public String getUser_avatar() { return user_avatar; }
    public void setUser_avatar(String user_avatar) { this.user_avatar = user_avatar; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
}