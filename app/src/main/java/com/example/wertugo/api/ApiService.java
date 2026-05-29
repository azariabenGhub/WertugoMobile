package com.example.wertugo.api;

import com.example.wertugo.api.models.ApiResponse;
import com.example.wertugo.api.models.LoginRequest;
import com.example.wertugo.api.models.LoginResponse;
import com.example.wertugo.api.models.RegisterRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("register")
    Call<ApiResponse> register(@Body RegisterRequest request);

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);
}