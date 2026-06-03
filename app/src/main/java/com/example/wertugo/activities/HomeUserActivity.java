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
import com.example.wertugo.api.models.BerandaResponse;
import com.example.wertugo.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeUserActivity extends AppCompatActivity {

    private RecyclerView rvUmkmList;
    private UmkmAdapter umkmAdapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // PERBAIKAN 1: Sesuaikan dengan nama layout XML milikmu
        setContentView(R.layout.activity_home);

        // ==========================================
        // INISIALISASI NAVBAR
        // ==========================================
        ImageView navHome = findViewById(R.id.nav_home);
        ImageView navSearch = findViewById(R.id.nav_search);
        ImageView navMessage = findViewById(R.id.nav_message);
        ImageView navProfile = findViewById(R.id.nav_profile);

        navHome.setOnClickListener(v -> {
            // Sudah berada di halaman home
        });

        navSearch.setOnClickListener(v -> {
            Toast.makeText(this, "Fitur pencarian belum tersedia", Toast.LENGTH_SHORT).show();
        });

        navMessage.setOnClickListener(v -> {
            Toast.makeText(this, "Fitur pesan belum tersedia", Toast.LENGTH_SHORT).show();
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeUserActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // ==========================================
        // INISIALISASI RECYCLERVIEW
        // ==========================================
        rvUmkmList = findViewById(R.id.rv_umkm_list);

        // Agar list tidak bentrok scrollnya dengan NestedScrollView
        rvUmkmList.setNestedScrollingEnabled(false);
        rvUmkmList.setLayoutManager(new LinearLayoutManager(this));

        umkmAdapter = new UmkmAdapter();
        rvUmkmList.setAdapter(umkmAdapter);

        // ==========================================
        // PERBAIKAN 2: SETUP TOKEN MANAGER & API
        // ==========================================
        TokenManager tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();

        // Panggil ApiService dengan token (bisa null jika token belum ada)
        apiService = ApiClient.getApiService(token);

        // Panggil Data dari API
        if (apiService != null) {
            fetchDataBeranda();
        }
    }

    private void fetchDataBeranda() {
        // Memanggil halaman 1 untuk data beranda
        apiService.getUmkmList(1).enqueue(new Callback<BerandaResponse>() {
            @Override
            public void onResponse(Call<BerandaResponse> call, Response<BerandaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Masukkan data dari API ke Adapter
                    umkmAdapter.setUmkmList(response.body().getData());
                } else {
                    Toast.makeText(HomeUserActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BerandaResponse> call, Throwable t) {
                Log.e("API_ERROR", "Koneksi Error: " + t.getMessage());
                Toast.makeText(HomeUserActivity.this, "Koneksi Error. Pastikan server lokal menyala.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}