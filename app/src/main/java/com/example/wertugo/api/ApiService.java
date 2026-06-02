package com.example.wertugo.api;

import com.example.wertugo.api.models.ApiResponse;
import com.example.wertugo.api.models.LoginRequest;
import com.example.wertugo.api.models.LoginResponse;
import com.example.wertugo.api.models.RegisterRequest;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public interface ApiService {
    @POST("register")
    Call<ApiResponse> register(@Body RegisterRequest request);

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("profile")
    Call<Map<String, Object>> getProfile(@Header("Authorization") String auth);

    @PUT("profile/update")
    @Multipart
    Call<Map<String, Object>> updateProfileWithImage(
            @Header("Authorization") String auth,
            @PartMap Map<String, RequestBody> textFields,
            @Part MultipartBody.Part foto
    );

    @POST("logout")
    Call<Void> logout();
}