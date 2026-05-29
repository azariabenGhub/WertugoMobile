package com.example.wertugo.api.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("token_type")
    private String tokenType;
    private User user;

    public String getAccessToken() { return accessToken; }
    public User getUser() { return user; }

    public static class User {
        private String id;
        private String name;
        private String email;
        private String role;
        // getters
        public String getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
    }
}
