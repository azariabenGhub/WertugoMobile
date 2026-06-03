package com.example.wertugo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wertugo.R;
import com.example.wertugo.api.ApiClient;
import com.example.wertugo.api.ApiService;
import com.example.wertugo.utils.TokenManager;
import com.google.android.material.button.MaterialButton; // IMPORT INI WAJIB ADA
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UmkmProfileActivity extends AppCompatActivity {

    private ShapeableImageView ivUmkmAvatar;
    private TextView tvUmkmName, tvUmkmId;
    private MaterialCardView btnMenuJamBuka, btnMenuInfoTempat, btnMenuVerifikasi, btnMenuInfoAkun;
    private ImageView navHome, navStar, navProfile;

    private TokenManager tokenManager;
    private ApiService apiService;
    private String umkmId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // 1. GAMBAR LAYARNYA DULU
            setContentView(R.layout.activity_umkm_profile);

            tokenManager = new TokenManager(this);
            apiService = ApiClient.getApiService();

            // 2. INISIALISASI VIEW & API
            initViews();
            setupBottomNav();
            loadUmkmData();

            // 3. BARU CARI TOMBOL LOGOUT-NYA
            MaterialButton btnLogout = findViewById(R.id.btn_logout_umkm);
            btnLogout.setOnClickListener(v -> performLogout());

            btnMenuJamBuka.setOnClickListener(v -> Toast.makeText(this, "Segera hadir", Toast.LENGTH_SHORT).show());
            btnMenuInfoTempat.setOnClickListener(v -> Toast.makeText(this, "Segera hadir", Toast.LENGTH_SHORT).show());
            btnMenuVerifikasi.setOnClickListener(v -> startActivity(new Intent(this, RegisterLegalitasActivity.class)));
            btnMenuInfoAkun.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        } catch (Exception e) {
            Toast.makeText(this, "CRASH DITEMUKAN: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void initViews() {
        ivUmkmAvatar = findViewById(R.id.iv_umkm_avatar);
        tvUmkmName = findViewById(R.id.tv_umkm_name);
        tvUmkmId = findViewById(R.id.tv_umkm_id);
        btnMenuJamBuka = findViewById(R.id.btn_menu_jam_buka);
        btnMenuInfoTempat = findViewById(R.id.btn_menu_info_tempat);
        btnMenuVerifikasi = findViewById(R.id.btn_menu_verifikasi);
        btnMenuInfoAkun = findViewById(R.id.btn_menu_info_akun);

        navHome = findViewById(R.id.nav_home_umkm);
        navStar = findViewById(R.id.nav_star);
        navProfile = findViewById(R.id.nav_profile);
    }

    private void setupBottomNav() {
        navHome.setOnClickListener(v -> {
            startActivity(new Intent(UmkmProfileActivity.this, UmkmHomeActivity.class));
            finish();
        });
        navStar.setOnClickListener(v -> {
            if (umkmId == null) {
                Toast.makeText(this, "Data belum siap", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(UmkmProfileActivity.this, ReviewListActivity.class);
            intent.putExtra("place_id", umkmId);
            intent.putExtra("is_umkm", true);
            startActivity(intent);
        });
        navProfile.setOnClickListener(v -> {
            // sudah di halaman profile ini
        });
    }

    private void loadUmkmData() {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Sesi habis, silakan login ulang", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Call<Map<String, Object>> call = apiService.getUmkmPlace("Bearer " + token);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = (Map<String, Object>) response.body().get("data");

                    if (data != null) {
                        Object rawId = data.containsKey("_id") ? data.get("_id") : data.get("id");

                        if (rawId != null) {
                            umkmId = String.valueOf(rawId);
                            String displayId = umkmId.length() > 6 ? umkmId.substring(0, 6) : umkmId;
                            tvUmkmId.setText("ID: " + displayId.toUpperCase());
                        } else {
                            umkmId = "";
                            tvUmkmId.setText("ID: Tidak diketahui");
                        }

                        Object rawNama = data.get("nama_usaha");
                        String namaUsaha = rawNama != null ? String.valueOf(rawNama) : "Nama belum diatur";
                        tvUmkmName.setText(namaUsaha);
                    }
                } else if (response.code() == 404) {
                    Toast.makeText(UmkmProfileActivity.this, "Data UMKM belum lengkap", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UmkmProfileActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(UmkmProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }); // PENUTUP CALLBACK API HARUS DISINI
    }

    // ==========================================
    // METHOD LOGOUT HARUS DI LUAR SINI
    // ==========================================
    private void performLogout() {
        String token = tokenManager.getToken();

        if (token != null) {
            apiService.logout("Bearer " + token).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    executeLocalLogout();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    executeLocalLogout();
                }
            });
        } else {
            executeLocalLogout();
        }
    }

    private void executeLocalLogout() {
        tokenManager.clearToken();
        Toast.makeText(this, "Berhasil Logout dari UMKM", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}