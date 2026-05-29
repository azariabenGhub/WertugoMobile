package com.example.wertugo.api.models;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    private String email;
    private String username;
    private String password;
    private String role;
    private String country;

    public RegisterRequest(String email, String username, String password, String role, String country) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
        this.country = country;
    }
}
