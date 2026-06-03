package com.example.wertugo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wertugo.R;
import com.example.wertugo.adapters.BucketListPlaceAdapter;
import com.example.wertugo.api.ApiClient;
import com.example.wertugo.api.ApiService;
import com.example.wertugo.api.models.BucketListPlaceModel;
import com.example.wertugo.utils.TokenManager;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BucketListDetailActivity extends AppCompatActivity {

    private String bucketId;
    private String bucketName;

    private TextView tvBucketTitle;
    private ImageView btnOptions;
    private RecyclerView rvBucketItems;
    private MaterialCardView fabAddItem;

    private BucketListPlaceAdapter placeAdapter;
    private List<BucketListPlaceModel> placeList = new ArrayList<>();

    private TokenManager tokenManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bucket_list_detail);

        // Siapkan API
        tokenManager = new TokenManager(this);
        apiService = ApiClient.getApiService(tokenManager.getToken());

        // Tangkap data dari Intent
        if (getIntent() != null) {
            bucketId = getIntent().getStringExtra("BUCKET_ID");
            bucketName = getIntent().getStringExtra("BUCKET_NAME");
        }

        initViews();
        setupNavbar();

        if (bucketName != null) {
            tvBucketTitle.setText(bucketName);
        }

        rvBucketItems.setLayoutManager(new LinearLayoutManager(this));

        // Pasang Adapter ke RecyclerView
        placeAdapter = new BucketListPlaceAdapter(placeList, bucketId);
        rvBucketItems.setAdapter(placeAdapter);

        // Panggil data dari API untuk menampilkan list tempat
        fetchDetailBucketList();

        // Tombol Tambah Tempat (+) diklik
        fabAddItem.setOnClickListener(v -> showAddPlaceDialog());

        btnOptions.setOnClickListener(v -> {
            Toast.makeText(this, "Menu opsi (Invite Teman / Hapus)", Toast.LENGTH_SHORT).show();
        });
    }

    private void initViews() {
        tvBucketTitle = findViewById(R.id.tv_bucket_title);
        btnOptions = findViewById(R.id.btn_bucket_options);
        rvBucketItems = findViewById(R.id.rv_bucket_items);
        fabAddItem = findViewById(R.id.fab_add_item);
    }

    private void setupNavbar() {
        ImageView navHome = findViewById(R.id.nav_home);
        ImageView navSearch = findViewById(R.id.nav_search);
        ImageView navProfile = findViewById(R.id.nav_profile);

        // Aktifkan background kotak hijau pada menu Bucket List
        android.widget.LinearLayout navContainerMessage = findViewById(R.id.nav_message).getParent() instanceof android.widget.LinearLayout ? (android.widget.LinearLayout) findViewById(R.id.nav_message).getParent() : null;
        if (navContainerMessage != null) {
            navContainerMessage.setBackgroundResource(R.drawable.bg_nav_active);
        }

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeUserActivity.class));
            finish();
            overridePendingTransition(0, 0);
        });

        navSearch.setOnClickListener(v ->
                Toast.makeText(this, "Fitur pencarian belum tersedia", Toast.LENGTH_SHORT).show());

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
            overridePendingTransition(0, 0);
        });
    }

    // ================= FITUR TARIK DATA TEMPAT ================= //

    private void fetchDetailBucketList() {
        apiService.getBucketListDetail("Bearer " + tokenManager.getToken(), bucketId)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Map<String, Object> responseData = (Map<String, Object>) response.body().get("data");

                            if (responseData != null && responseData.get("places") != null) {
                                List<Map<String, Object>> places = (List<Map<String, Object>>) responseData.get("places");
                                placeList.clear();

                                for (Map<String, Object> item : places) {
                                    // Ambil ID secara aman
                                    String id = item.get("id") != null ? String.valueOf(item.get("id")) :
                                            (item.get("_id") != null ? String.valueOf(item.get("_id")) : "");

                                    String nama = item.get("nama_tempat") != null ? (String) item.get("nama_tempat") : "Tanpa Nama";
                                    String lokasi = item.get("lokasi") != null ? (String) item.get("lokasi") : "-";
                                    String kategori = item.get("kategori") != null ? (String) item.get("kategori") : "Umum";

                                    placeList.add(new BucketListPlaceModel(id, nama, lokasi, kategori));
                                }
                                placeAdapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(BucketListDetailActivity.this, "Gagal memuat tempat", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ================= FITUR TAMBAH TEMPAT ================= //

    private void showAddPlaceDialog() {
        // 1. Siapkan Dialog menggunakan XML buatanmu
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_add_place_manual);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        // 2. Kenalkan elemen di dalam XML
        EditText etNama = dialog.findViewById(R.id.et_manual_nama_tempat);
        EditText etLokasi = dialog.findViewById(R.id.et_manual_lokasi);
        EditText etDeskripsi = dialog.findViewById(R.id.et_manual_deskripsi);
        android.widget.AutoCompleteTextView dropdownKategori = dialog.findViewById(R.id.dropdown_manual_kategori);
        MaterialCardView btnUpload = dialog.findViewById(R.id.btn_manual_upload_gambar);
        com.google.android.material.button.MaterialButton btnTambah = dialog.findViewById(R.id.btn_manual_submit_tambah);
        com.google.android.material.button.MaterialButton btnBatal = dialog.findViewById(R.id.btn_manual_submit_batal);

        // 3. Setup Dropdown Kategori
        String[] listKategori = {"Wisata Alam", "Kuliner & Cafe", "Theme Park", "Sejarah & Budaya", "Lainnya"};
        android.widget.ArrayAdapter<String> dropdownAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listKategori);
        dropdownKategori.setAdapter(dropdownAdapter);

        // 4. Tombol Upload Gambar (Sementara pakai Toast dulu)
        btnUpload.setOnClickListener(v -> {
            Toast.makeText(this, "Fitur pilih gambar galeri menyusul!", Toast.LENGTH_SHORT).show();
        });

        // 5. Tombol Batal
        btnBatal.setOnClickListener(v -> dialog.dismiss());

        // 6. Tombol Tambah
        btnTambah.setOnClickListener(v -> {
            String namaTempat = etNama.getText().toString().trim();
            String lokasi = etLokasi.getText().toString().trim();
            String deskripsi = etDeskripsi.getText().toString().trim();
            String kategori = dropdownKategori.getText().toString().trim();

            if (namaTempat.isEmpty()) {
                Toast.makeText(this, "Nama tempat wajib diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kunci tombol biar gak spam klik
            btnTambah.setEnabled(false);
            btnTambah.setText("Menyimpan...");

            // Kirim semua data ke API
            sendPlaceToApi(namaTempat, lokasi, deskripsi, kategori, dialog, btnTambah);
        });

        // 7. Tampilkan
        dialog.show();
    }

    private void sendPlaceToApi(String namaTempat, String lokasi, String deskripsi, String kategori, android.app.Dialog dialog, com.google.android.material.button.MaterialButton btnTambah) {

        // Kita kirim placeId kosong ("") karena ini tempat manual buatan user
        String placeId = "";

        apiService.addPlaceToBucketList("Bearer " + tokenManager.getToken(), bucketId, placeId, namaTempat, lokasi, deskripsi, kategori)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(BucketListDetailActivity.this, "Berhasil masuk daftar!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();

                            // Refresh otomatis daftar tempat setelah berhasil ditambahkan
                            fetchDetailBucketList();
                        } else {
                            // Menampilkan pesan error dari backend
                            try {
                                String errorBody = response.errorBody().string();
                                Toast.makeText(BucketListDetailActivity.this, "Gagal: " + errorBody, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(BucketListDetailActivity.this, "Gagal menambahkan tempat", Toast.LENGTH_SHORT).show();
                            }
                            btnTambah.setEnabled(true);
                            btnTambah.setText("Tambah");
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(BucketListDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        btnTambah.setEnabled(true);
                        btnTambah.setText("Tambah");
                    }
                });
    }
}