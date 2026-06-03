package com.example.wertugo.api.models;

public class RegisterRequest {
    private String email;
    private String username;
    private String password;
    private String role;
    private String country;
    private String account_status; // Tambahan field status

    // Constructor
    public RegisterRequest(String email, String username, String password, String role, String country) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
        this.country = country;
        this.account_status = "active"; // Otomatis diset ke active
    }

    // Getter
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getCountry() { return country; }
    public String getAccountStatus() { return account_status; }
}