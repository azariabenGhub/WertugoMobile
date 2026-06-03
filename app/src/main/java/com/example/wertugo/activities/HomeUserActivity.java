package com.example.wertugo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wertugo.R;
import com.example.wertugo.adapters.UmkmAdapter;
import com.example.wertugo.api.ApiClient;
import com.example.wertugo.api.ApiService;
import com.example.wertugo.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeUserActivity extends AppCompatActivity {

    private RecyclerView rvRecommendations;
    private TokenManager tokenManager;

    private List<Map<String, Object>> listRekomendasi = new ArrayList<>();
    private UmkmAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tokenManager = new TokenManager(this);
        rvRecommendations = findViewById(R.id.rv_recommendations);

        // Pengamanan 1: Pastikan RecyclerView ada di XML
        if (rvRecommendations != null) {
            rvRecommendations.setLayoutManager(new LinearLayoutManager(this));
            adapter = new UmkmAdapter(listRekomendasi);
            rvRecommendations.setAdapter(adapter);
        } else {
            Toast.makeText(this, "Error: rv_recommendations tidak ditemukan di XML!", Toast.LENGTH_LONG).show();
        }

        setupNavbar();
        fetchUmkmData();
    }

    private void fetchUmkmData() {
        String token = tokenManager.getToken();
        ApiService apiService = ApiClient.getApiService(token);

        apiService.getBerandaUser("Bearer " + token).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // Pengamanan 2: Bungkus proses ekstrak data API pakai try-catch
                    try {
                        Object dataRaw = response.body().get("data");

                        // Cek apakah data dibungkus sebagai Map { rekomendasi: [...] }
                        if (dataRaw instanceof Map) {
                            Map<String, Object> dataObj = (Map<String, Object>) dataRaw;
                            if (dataObj.containsKey("rekomendasi")) {
                                List<Map<String, Object>> dataRekomendasi = (List<Map<String, Object>>) dataObj.get("rekomendasi");

                                // Cegah error addAll(null)
                                if (dataRekomendasi != null) {
                                    listRekomendasi.clear();
                                    listRekomendasi.addAll(dataRekomendasi);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                        // Alternatif: Kalau API ternyata langsung melempar array [ {umkm1}, {umkm2} ]
                        else if (dataRaw instanceof List) {
                            List<Map<String, Object>> dataRekomendasi = (List<Map<String, Object>>) dataRaw;
                            listRekomendasi.clear();
                            listRekomendasi.addAll(dataRekomendasi);
                            adapter.notifyDataSetChanged();
                        }

                    } catch (Exception e) {
                        Log.e("API_ERROR", "Format JSON API tidak sesuai ekspektasi: " + e.getMessage());
                        Toast.makeText(HomeUserActivity.this, "Error struktur data dari server", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(HomeUserActivity.this, "Gagal mengambil data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(HomeUserActivity.this, "Error Jaringan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNavbar() {
        ImageView navHome = findViewById(R.id.nav_home);
        ImageView navSearch = findViewById(R.id.nav_search);
        ImageView navMessage = findViewById(R.id.nav_message);
        ImageView navProfile = findViewById(R.id.nav_profile);

        // Pengamanan 3: Cek null sebelum memasang Click Listener
        if (navSearch != null) {
            navSearch.setOnClickListener(v -> Toast.makeText(this, "Fitur pencarian segera hadir", Toast.LENGTH_SHORT).show());
        }

        if (navMessage != null) {
            navMessage.setOnClickListener(v -> {
                Intent intent = new Intent(HomeUserActivity.this, BucketListMainActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(HomeUserActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
            });
        }

        // Aktifkan background kotak hijau dengan cara aman dari getParent()
        if (navHome != null && navHome.getParent() instanceof android.widget.LinearLayout) {
            ((android.widget.LinearLayout) navHome.getParent()).setBackgroundResource(R.drawable.bg_nav_active);
        }
    }
}