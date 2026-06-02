package com.example.wertugo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wertugo.R;
import com.example.wertugo.api.ApiClient;
import com.example.wertugo.api.ApiService;
import com.example.wertugo.utils.TokenManager;
import com.google.android.material.button.MaterialButton;
import retrofit2.Callback;
import retrofit2.Call;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tokenManager = new TokenManager(this);

        // Tombol Logout
        MaterialButton btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> logout());

        // Navbar
        ImageView navHome = findViewById(R.id.nav_home);
        ImageView navSearch = findViewById(R.id.nav_search);
        ImageView navMessage = findViewById(R.id.nav_message);
        ImageView navProfile = findViewById(R.id.nav_profile);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
            finish();
        });

        navSearch.setOnClickListener(v -> {
            Toast.makeText(this, "Fitur pencarian belum tersedia", Toast.LENGTH_SHORT).show();
        });

        navMessage.setOnClickListener(v -> {
            Toast.makeText(this, "Fitur pesan belum tersedia", Toast.LENGTH_SHORT).show();
        });

        // Profile aktif, tidak perlu pindah ke dirinya sendiri
        navProfile.setOnClickListener(v -> {
            // sudah di halaman profile
        });
    }

    private void logout() {
        String token = tokenManager.getToken();
        if (token == null) {
            // Token sudah tidak ada, langsung navigasi
            navigateToLogin();
            return;
        }

        ApiService apiService = ApiClient.getApiService(token);
        Call<Void> call = apiService.logout();
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // Hapus token lokal terlepas dari sukses/gagal (kecuali server error 5xx?)
                tokenManager.clearToken();
                navigateToLogin();
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Logout berhasil", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Logout server gagal, sesi lokal tetap dihapus", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Gagal network: tetap logout lokal
                tokenManager.clearToken();
                navigateToLogin();
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage() + ", logout lokal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}