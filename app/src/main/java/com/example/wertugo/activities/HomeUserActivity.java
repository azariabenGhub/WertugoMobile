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
import com.example.wertugo.adapters.UmkmAdapter; // Pastikan import adapter-nya
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

    // Siapkan wadah list dan Adapter
    private List<Map<String, Object>> listRekomendasi = new ArrayList<>();
    private UmkmAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tokenManager = new TokenManager(this);
        rvRecommendations = findViewById(R.id.rv_recommendations);

        // PENTING: Beri tahu RecyclerView kalau bentuk daftarnya memanjang ke bawah
        rvRecommendations.setLayoutManager(new LinearLayoutManager(this));

        // Pasang adapter kosong ke RecyclerView
        adapter = new UmkmAdapter(listRekomendasi);
        rvRecommendations.setAdapter(adapter);

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

                    // 1. Ambil objek 'data' dari JSON
                    Map<String, Object> dataObj = (Map<String, Object>) response.body().get("data");

                    if (dataObj != null && dataObj.containsKey("rekomendasi")) {
                        // 2. Ekstrak array 'rekomendasi'
                        List<Map<String, Object>> dataRekomendasi = (List<Map<String, Object>>) dataObj.get("rekomendasi");

                        // 3. Masukkan data ke dalam list adapter dan update layar
                        listRekomendasi.clear();
                        listRekomendasi.addAll(dataRekomendasi);
                        adapter.notifyDataSetChanged(); // Teriak ke UI: "Woi, ada data baru!"
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

        // Aktifkan background kotak hijau pada menu Home
        android.widget.LinearLayout navContainerHome = findViewById(R.id.nav_container_home);
        if (navContainerHome != null) {
            navContainerHome.setBackgroundResource(R.drawable.bg_nav_active);
        }

        // navHome tidak perlu diklik pindah halaman karena user sudah berada di Home

        navSearch.setOnClickListener(v ->
                Toast.makeText(this, "Fitur pencarian segera hadir", Toast.LENGTH_SHORT).show());

        // 🔽 UBAH BAGIAN INI UNTUK PINDAH KE BUCKET LIST 🔽
        navMessage.setOnClickListener(v -> {
            Intent intent = new Intent(HomeUserActivity.this, BucketListMainActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0); // Menghilangkan animasi geser
        });

        // 🔽 PERBAIKI JUGA UNTUK PINDAH KE PROFILE 🔽
        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeUserActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0); // Menghilangkan animasi geser
        });
    }
}