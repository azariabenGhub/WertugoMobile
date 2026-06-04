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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UmkmProfileActivity extends AppCompatActivity {

    private ShapeableImageView ivUmkmAvatar;
    private TextView tvUmkmName, tvUmkmId;

    private com.google.android.material.switchmaterial.SwitchMaterial switchStatusToko;
    private TextView tvStatusToko;

    // TAMBAHAN: btnMenuGaleri
    private MaterialCardView btnMenuJamBuka, btnMenuInfoTempat, btnMenuVerifikasi, btnMenuInfoAkun, btnMenuGaleri;
    private ImageView navHome, navStar, navProfile;

    private TokenManager tokenManager;
    private ApiService apiService;
    private String umkmId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_umkm_profile);

            tokenManager = new TokenManager(this);
            apiService = ApiClient.getApiService(tokenManager.getToken()); // Pastikan masukin token!

            initViews();
            loadUmkmData();

            MaterialButton btnLogout = findViewById(R.id.btn_logout_umkm);
            btnLogout.setOnClickListener(v -> performLogout());

            btnMenuJamBuka.setOnClickListener(v -> {
            Intent intent = new Intent(this, JadwalOperasionalActivity.class);
            startActivity(intent);
        });
            btnMenuInfoTempat.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminInfoActivity.class);
                startActivity(intent);
            });
            btnMenuVerifikasi.setOnClickListener(v -> startActivity(new Intent(this, RegisterLegalitasActivity.class)));
            btnMenuInfoAkun.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("from_umkm", true);
                startActivity(intent);
            });

            // AKSI KLIK UNTUK MENU GALERI
            btnMenuGaleri.setOnClickListener(v -> {
                if (umkmId == null || umkmId.isEmpty()) {
                    Toast.makeText(this, "Data UMKM belum termuat, mohon tunggu", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(this, UploadGaleriActivity.class);
                intent.putExtra("UMKM_ID", umkmId);
                startActivity(intent);
            });

            switchStatusToko.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed()) {
                    updateStatusToko(isChecked);
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "CRASH DITEMUKAN: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void initViews() {
        ivUmkmAvatar = findViewById(R.id.iv_umkm_avatar);
        tvUmkmName = findViewById(R.id.tv_umkm_name);
        tvUmkmId = findViewById(R.id.tv_umkm_id);

        switchStatusToko = findViewById(R.id.switch_status_toko);
        tvStatusToko = findViewById(R.id.tv_status_toko);

        btnMenuJamBuka = findViewById(R.id.btn_menu_jam_buka);
        btnMenuInfoTempat = findViewById(R.id.btn_menu_info_tempat);
        btnMenuVerifikasi = findViewById(R.id.btn_menu_verifikasi);
        btnMenuInfoAkun = findViewById(R.id.btn_menu_info_akun);
        btnMenuGaleri = findViewById(R.id.btn_menu_galeri); // INISIALISASI BARU

        // PERBAIKAN: Ambil referensi melalui layout include
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_view);
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home_umkm) {
                startActivity(new Intent(UmkmProfileActivity.this, UmkmHomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_star) {
                Intent intent = new Intent(UmkmProfileActivity.this, ReviewListActivity.class);
                intent.putExtra("place_id", umkmId);
                intent.putExtra("is_umkm", true);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            return false;
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

                        if (data.containsKey("is_open") && data.get("is_open") != null) {
                            boolean isOpen = false;
                            // MongoDB boolean sometimes parses as Double (0.0/1.0) or String ("1", "true")
                            String openStr = String.valueOf(data.get("is_open")).toLowerCase();
                            if (openStr.equals("true") || openStr.equals("1") || openStr.equals("1.0")) {
                                isOpen = true;
                            }
                            switchStatusToko.setChecked(isOpen);
                            tvStatusToko.setText(isOpen ? "Toko Buka" : "Toko Tutup");
                            tvStatusToko.setTextColor(isOpen ? android.graphics.Color.parseColor("#008B09") : android.graphics.Color.RED);
                        }
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
        });
    }

    private void updateStatusToko(boolean isOpen) {
        String token = tokenManager.getToken();
        if (token == null) return;

        Map<String, Boolean> body = new java.util.HashMap<>();
        body.put("is_open", isOpen);

        tvStatusToko.setText("Menyimpan...");
        switchStatusToko.setEnabled(false);

        apiService.updateUmkmStatus("Bearer " + token, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                switchStatusToko.setEnabled(true);
                if (response.isSuccessful()) {
                    tvStatusToko.setText(isOpen ? "Toko Buka" : "Toko Tutup");
                    tvStatusToko.setTextColor(isOpen ? android.graphics.Color.parseColor("#008B09") : android.graphics.Color.RED);
                    Toast.makeText(UmkmProfileActivity.this, "Status toko diperbarui", Toast.LENGTH_SHORT).show();
                } else {
                    switchStatusToko.setChecked(!isOpen);
                    tvStatusToko.setText(!isOpen ? "Toko Buka" : "Toko Tutup");
                    tvStatusToko.setTextColor(!isOpen ? android.graphics.Color.parseColor("#008B09") : android.graphics.Color.RED);
                    Toast.makeText(UmkmProfileActivity.this, "Gagal mengubah status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                switchStatusToko.setEnabled(true);
                switchStatusToko.setChecked(!isOpen);
                tvStatusToko.setText(!isOpen ? "Toko Buka" : "Toko Tutup");
                tvStatusToko.setTextColor(!isOpen ? android.graphics.Color.parseColor("#008B09") : android.graphics.Color.RED);
                Toast.makeText(UmkmProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

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