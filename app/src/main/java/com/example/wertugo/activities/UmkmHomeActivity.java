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
import com.google.android.material.card.MaterialCardView; // Pastikan ini ter-import

import java.util.List; // Pastikan ini ter-import
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UmkmHomeActivity extends AppCompatActivity {

    // ==========================================================
    // DEKLARASI VARIABEL (Wajib di luar fungsi / di tingkat kelas)
    // ==========================================================
    private TextView tvRatingValue, tvRatingCount, tvWishlistCount;
    private ImageView navHome, navStar, navProfile;
    private String umkmId;

    private TokenManager tokenManager;
    private ApiService apiService;

    // Variabel Verifikasi
    private View layoutUnverified, layoutVerified;
    private MaterialButton btnLengkapiDokumen;

    // Variabel Tip Galeri (Baru)
    private MaterialCardView cardTipGaleri;
    // ==========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_umkm_home);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getApiService(tokenManager.getToken());

        initViews();
//        setupBottomNav();
        loadUmkmData();
    }

    private void initViews() {
        tvRatingValue = findViewById(R.id.tv_rating_value);
        tvRatingCount = findViewById(R.id.tv_rating_count);
        tvWishlistCount = findViewById(R.id.tv_wishlist_count);

        // Setup Bottom Navigation
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_view);
        bottomNav.setSelectedItemId(R.id.nav_home_umkm);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home_umkm) {
                return true;
            } else if (itemId == R.id.nav_star) {
                if (umkmId != null) {
                    Intent intent = new Intent(UmkmHomeActivity.this, ReviewListActivity.class);
                    intent.putExtra("place_id", umkmId);
                    intent.putExtra("is_umkm", true);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Data UMKM belum siap", Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(UmkmHomeActivity.this, UmkmProfileActivity.class));
                return true;
            }
            return false;
        });

        // Inisialisasi Layout Verifikasi
        layoutUnverified = findViewById(R.id.layout_unverified);
        layoutVerified = findViewById(R.id.layout_verified);
        btnLengkapiDokumen = findViewById(R.id.btn_lengkapi_dokumen);

        // Inisialisasi Tip Galeri
        cardTipGaleri = findViewById(R.id.card_tip_galeri);

        // Aksi Tombol Lengkapi Dokumen
        btnLengkapiDokumen.setOnClickListener(v -> {
            startActivity(new Intent(UmkmHomeActivity.this, RegisterLegalitasActivity.class));
        });

        // Aksi Tombol Tip Galeri (Ngarah ke halaman upload)
        cardTipGaleri.setOnClickListener(v -> {
            Intent intent = new Intent(UmkmHomeActivity.this, UploadGaleriActivity.class);
            intent.putExtra("UMKM_ID", umkmId);
            startActivity(intent);
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
                        umkmId = (String) data.get("id");
                        if(umkmId == null && data.get("_id") != null) {
                            umkmId = String.valueOf(data.get("_id"));
                        }

                        updateStatsUI(data);
                        checkVerificationStatus(data);
                        checkGaleriStatus(data); // Panggil fungsi cek galeri
                        loadUmkmRating(umkmId); // Mengambil rating dari koleksi komentar yang sebenarnya
                    }
                } else if (response.code() == 404) {
                    Toast.makeText(UmkmHomeActivity.this, "Silakan daftarkan usaha Anda terlebih dahulu", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(UmkmHomeActivity.this, RegisterUmkmActivity.class));
                    finish();
                } else {
                    Toast.makeText(UmkmHomeActivity.this, "Gagal memuat data dashboard", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(UmkmHomeActivity.this, "Error Jaringan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatsUI(Map<String, Object> data) {
        try {
            // Coba ambil dari "rating_avg" (karena nama field di API mu biasanya pakai ini)
            Object rawRating = data.containsKey("rating_avg") ? data.get("rating_avg") : data.get("rating");
            if (rawRating != null) {
                // Konversi aman untuk menghindari crash jika formatnya double/float
                double ratingVal = Double.parseDouble(String.valueOf(rawRating));
                tvRatingValue.setText(String.format("%.1f / 5.0", ratingVal));
            }

            // Coba ambil dari "total_ulasan"
            Object rawTotal = data.containsKey("total_ulasan") ? data.get("total_ulasan") : data.get("total_reviews");
            if (rawTotal != null) {
                tvRatingCount.setText(String.valueOf(rawTotal) + " Ulasan");
            }

            // Wishlist
            if (data.containsKey("wishlist_count") && data.get("wishlist_count") != null) {
                String wishlist = String.valueOf(data.get("wishlist_count"));
                tvWishlistCount.setText(wishlist + "x Disimpan");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkVerificationStatus(Map<String, Object> data) {
        String status = "";

        if (data.containsKey("verification_status") && data.get("verification_status") != null) {
            // Trim (buang spasi) dan ubah ke string dengan aman
            status = String.valueOf(data.get("verification_status")).trim();
        }

        // Gunakan equalsIgnoreCase agar "Verified", "VERIFIED", atau "verified" semua diterima
        if (status.equalsIgnoreCase("verified")) {
            layoutVerified.setVisibility(View.VISIBLE);
            layoutUnverified.setVisibility(View.GONE);
        } else {
            layoutVerified.setVisibility(View.GONE);
            layoutUnverified.setVisibility(View.VISIBLE);
        }
    }

    // ==============================================
    // FUNGSI BARU UNTUK MENGECEK GALERI KOSONG
    // ==============================================
    private void checkGaleriStatus(Map<String, Object> data) {
        boolean isGaleriEmpty = true; // Anggap kosong dari awal

        if (data.containsKey("katalog_galeri") && data.get("katalog_galeri") != null) {
            Object rawGaleri = data.get("katalog_galeri");

            if (rawGaleri instanceof List) {
                List<?> galeriList = (List<?>) rawGaleri;
                if (!galeriList.isEmpty()) {
                    isGaleriEmpty = false; // Ada isinya!
                }
            }
        }

        // Tampilkan kartu tip kalau benar-benar kosong
        if (isGaleriEmpty) {
            cardTipGaleri.setVisibility(View.VISIBLE);
        } else {
            cardTipGaleri.setVisibility(View.GONE);
        }
    }

    private void loadUmkmRating(String umkmId) {
        String token = tokenManager.getToken();
        if (token == null || umkmId == null) return;

        Call<Map<String, Object>> call = apiService.getUmkmComments("Bearer " + token, umkmId);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Object dataObj = response.body().get("data");
                        if (dataObj instanceof List) {
                            List<?> dataList = (List<?>) dataObj;
                            int totalReviews = dataList.size();
                            if (totalReviews > 0) {
                                float totalRating = 0;
                                for (Object item : dataList) {
                                    if (item instanceof Map) {
                                        Map<String, Object> map = (Map<String, Object>) item;
                                        if (map.containsKey("rating") && map.get("rating") != null) {
                                            try {
                                                totalRating += Float.parseFloat(String.valueOf(map.get("rating")));
                                            } catch (NumberFormatException ignored) {}
                                        }
                                    }
                                }
                                float avgRating = totalRating / totalReviews;
                                tvRatingValue.setText(String.format(java.util.Locale.US, "%.1f / 5.0", avgRating));
                                tvRatingCount.setText(totalReviews + " Ulasan");
                            }
                        } else if (dataObj instanceof Map) {
                            Map<String, Object> map = (Map<String, Object>) dataObj;
                            float rating = 0;
                            if (map.containsKey("rating") && map.get("rating") != null) {
                                try {
                                    rating = Float.parseFloat(String.valueOf(map.get("rating")));
                                } catch (NumberFormatException ignored) {}
                            }
                            tvRatingValue.setText(String.format(java.util.Locale.US, "%.1f / 5.0", rating));
                            tvRatingCount.setText("1 Ulasan");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                // Abaikan kesalahan, biarkan fallback dari updateStatsUI yang tampil
            }
        });
    }
}