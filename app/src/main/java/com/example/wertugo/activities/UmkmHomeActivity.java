package com.example.wertugo.activities;

import android.app.AlertDialog;
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

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UmkmHomeActivity extends AppCompatActivity {

    private MaterialCardView cardStatusBuka, cardStatusTutup;
    private TextView tvUbahBuka, tvUbahTutup, tvJamOperasionalBuka, tvJamOperasionalTutup;
    private TextView tvRatingValue, tvRatingCount, tvWishlistCount;
    private ImageView navHome, navStar, navProfile;

    private TokenManager tokenManager;
    private ApiService apiService;
    private boolean isOpen = false;  // status saat ini dari server
    private String openTime = "08:00";
    private String closeTime = "17:00";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_umkm_home);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getApiService();

        initViews();
        setupBottomNav();
        loadUmkmData();

        // Event listener untuk ubah status
        tvUbahBuka.setOnClickListener(v -> showChangeStatusDialog(true));
        tvUbahTutup.setOnClickListener(v -> showChangeStatusDialog(false));
    }

    private void initViews() {
        cardStatusBuka = findViewById(R.id.card_status_buka);
        cardStatusTutup = findViewById(R.id.card_status_tutup);
        tvUbahBuka = findViewById(R.id.tv_click_ubah_buka);
        tvUbahTutup = findViewById(R.id.tv_click_ubah_tutup);
        tvJamOperasionalBuka = findViewById(R.id.tv_jam_buka);
        tvJamOperasionalTutup = findViewById(R.id.tv_jam_tutup);
        tvRatingValue = findViewById(R.id.tv_rating_value); // tidak ada di layout asli? layout activity_umkm_home.xml tidak memiliki id untuk rating, tapi ada TextView "8,3/10". Kita perlu tambahkan id di layout. Sementara kita asumsikan sudah ditambahkan id.
        tvRatingCount = findViewById(R.id.tv_rating_count); // "16,3 rb Rating"
        tvWishlistCount = findViewById(R.id.tv_wishlist_count); // "15,3 rb orang ..."
        // Jika tidak ada id di layout, gunakan findViewById dengan teks. Lebih baik modifikasi layout.
        // Untuk keperluan demo, kita skip update rating & wishlist.
        navHome = findViewById(R.id.nav_home);
        navStar = findViewById(R.id.nav_star);
        navProfile = findViewById(R.id.nav_profile);
    }

    private void setupBottomNav() {
        navHome.setOnClickListener(v -> {
            // sudah di home
        });
        navStar.setOnClickListener(v -> {
            // Arahkan ke halaman Wishlist / Bucketlist (belum ada, bisa toast)
            Toast.makeText(this, "Fitur Wishlist segera hadir", Toast.LENGTH_SHORT).show();
        });
        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(UmkmHomeActivity.this, ProfileActivity.class));
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
                        isOpen = (boolean) data.get("is_open");
                        List<Map<String, String>> jadwal = (List<Map<String, String>>) data.get("jadwal_operasional");
                        parseSchedule(jadwal);

                        // Update tampilan status
                        updateStatusUI(isOpen);

                        // (Opsional) Ambil rating dan wishlist dari endpoint lain
                        loadStats();
                    }
                } else if (response.code() == 404) {
                    // UMKM belum terdaftar, arahkan ke halaman registrasi
                    Toast.makeText(UmkmHomeActivity.this, "Silakan daftarkan usaha Anda terlebih dahulu", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(UmkmHomeActivity.this, RegisterUmkmActivity.class));
                    finish();
                } else {
                    Toast.makeText(UmkmHomeActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(UmkmHomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void parseSchedule(List<Map<String, String>> jadwal) {
        if (jadwal == null || jadwal.isEmpty()) return;
        // Ambil jadwal untuk hari ini (misal Monday, Tuesday...)
        String today = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(new Date());
        for (Map<String, String> schedule : jadwal) {
            if (schedule.get("day").equalsIgnoreCase(today)) {
                openTime = schedule.get("open");
                closeTime = schedule.get("close");
                break;
            }
        }
    }

    private void updateStatusUI(boolean isOpenNow) {
        if (isOpenNow) {
            cardStatusBuka.setVisibility(View.VISIBLE);
            cardStatusTutup.setVisibility(View.GONE);
            // Tampilkan jam operasional
            // tvJamOperasionalBuka.setText("Buka hingga " + closeTime);
        } else {
            cardStatusBuka.setVisibility(View.GONE);
            cardStatusTutup.setVisibility(View.VISIBLE);
            // tvJamOperasionalTutup.setText("Otomatis Terbuka pada " + openTime);
        }
    }

    private void showChangeStatusDialog(boolean currentIsOpen) {
        String[] options = currentIsOpen ? new String[]{"Tutup", "Batal"} : new String[]{"Buka", "Batal"};
        new AlertDialog.Builder(this)
                .setTitle(currentIsOpen ? "Ubah status menjadi Tutup?" : "Ubah status menjadi Buka?")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        changeStatus(!currentIsOpen);
                    }
                })
                .show();
    }

    private void changeStatus(boolean newStatus) {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Sesi habis, silakan login ulang", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Buat body request
        Map<String, Boolean> body = new HashMap<>();
        body.put("is_open", newStatus);

        Call<Map<String, Object>> call = apiService.updateUmkmStatus("Bearer " + token, body);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    isOpen = newStatus;
                    updateStatusUI(isOpen);
                    Toast.makeText(UmkmHomeActivity.this, "Status berhasil diubah", Toast.LENGTH_SHORT).show();
                } else {
                    // Parsing error dari server
                    try {
                        String errorBody = response.errorBody().string();
                        JSONObject json = new JSONObject(errorBody);
                        String message = json.optString("message", "Gagal mengubah status");
                        Toast.makeText(UmkmHomeActivity.this, message, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(UmkmHomeActivity.this, "Gagal mengubah status", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(UmkmHomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadStats() {
        // Contoh dummy, nanti ganti dengan panggilan API yang menyediakan total rating dan wishlist count
        // Misal: Call<Map<String, Object>> call = apiService.getUmkmStats("Bearer " + token);
        // Untuk sementara set data statis
        // tvRatingValue.setText("8,3/10");
        // tvRatingCount.setText("16,3 rb Rating");
        // tvWishlistCount.setText("15,3 rb orang menambahkan ke bucket list");
    }

    private void showIncompleteDocsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_incomplete_docs, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();
        dialogView.findViewById(R.id.btn_dialog_batal).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_dialog_lanjut).setOnClickListener(v -> {
            dialog.dismiss();
            // Arahkan ke halaman upload dokumen (jika ada)
            Toast.makeText(this, "Fitur upload dokumen segera hadir", Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }

    // Cek status verifikasi UMKM saat activity dimulai (opsional)
    private void checkVerificationStatus() {
        String token = tokenManager.getToken();
        if (token == null) return;
        Call<Map<String, Object>> call = apiService.getVerificationStatus("Bearer " + token);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String status = (String) response.body().get("verification_status");
                    if (!"verified".equals(status)) {
                        showIncompleteDocsDialog();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                // ignore
            }
        });
    }
}