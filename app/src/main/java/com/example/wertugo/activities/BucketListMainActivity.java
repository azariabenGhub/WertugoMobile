package com.example.wertugo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wertugo.R;
import com.example.wertugo.adapters.BucketListAdapter;
import com.example.wertugo.api.ApiClient;
import com.example.wertugo.api.ApiService;
import com.example.wertugo.api.models.BucketListModel;
import com.example.wertugo.utils.TokenManager;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BucketListMainActivity extends AppCompatActivity {

    private LinearLayout layoutEmptyBucket;
    private RecyclerView rvBucketRooms;
    private MaterialCardView fabCreateBucket;
    private View viewDimOverlay;
    private EditText etSearchBucket;

    private TokenManager tokenManager;
    private ApiService apiService;

    // Adapter dan Model
    private BucketListAdapter adapter;
    private List<BucketListModel> bucketList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bucket_list_main);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getApiService(tokenManager.getToken());

        initViews();
        setupNavbar();
        setupRecyclerView();

        // Tombol FAB (+) diklik
        fabCreateBucket.setOnClickListener(v -> showCreateBucketDialog());

        // Ambil data asli dari server
        fetchBucketLists();
    }

    private void initViews() {
        layoutEmptyBucket = findViewById(R.id.layout_empty_bucket);
        rvBucketRooms = findViewById(R.id.rv_bucket_rooms);
        fabCreateBucket = findViewById(R.id.fab_create_bucket);
        viewDimOverlay = findViewById(R.id.view_dim_overlay_bucket);
        etSearchBucket = findViewById(R.id.et_search_bucket);
    }

    private void setupRecyclerView() {
        rvBucketRooms.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BucketListAdapter(bucketList);
        rvBucketRooms.setAdapter(adapter);
    }

    private void setupNavbar() {
        ImageView navHome = findViewById(R.id.nav_home);
        ImageView navSearch = findViewById(R.id.nav_search);
        ImageView navMessage = findViewById(R.id.nav_message);
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

        // navMessage sudah tidak usah di-klik karena sedang di halaman ini

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
            overridePendingTransition(0, 0);
        });
    }

    private void fetchBucketLists() {
        apiService.getBucketLists("Bearer " + tokenManager.getToken())
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Map<String, Object>> dataList = (List<Map<String, Object>>) response.body().get("data");
                            bucketList.clear();

                            if (dataList != null && !dataList.isEmpty()) {
                                for (Map<String, Object> item : dataList) {

                                    // FIX: Mengecek "id" atau "_id" secara dinamis dan aman dari tipe data angka/string
                                    String id = "";
                                    if (item.get("id") != null) {
                                        id = String.valueOf(item.get("id"));
                                    } else if (item.get("_id") != null) {
                                        id = String.valueOf(item.get("_id"));
                                    }

                                    String nama = (String) item.get("title"); // Sesuai kolom di Laravel

                                    // Mengonversi nilai Double dari GSON secara aman ke Integer
                                    int totalMember = item.get("total_member") != null ? ((Double) item.get("total_member")).intValue() : 1;
                                    int totalTempat = item.get("total_tempat") != null ? ((Double) item.get("total_tempat")).intValue() : 0;

                                    bucketList.add(new BucketListModel(id, nama, totalMember, totalTempat));
                                }

                                layoutEmptyBucket.setVisibility(View.GONE);
                                rvBucketRooms.setVisibility(View.VISIBLE);
                            } else {
                                layoutEmptyBucket.setVisibility(View.VISIBLE);
                                rvBucketRooms.setVisibility(View.GONE);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(BucketListMainActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showCreateBucketDialog() {
        // 1. Gelapkan background utama
        viewDimOverlay.setVisibility(View.VISIBLE);

        // 2. Siapkan Dialog
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_create_bucket);

        // Bikin background asli dialog jadi transparan agar sudut melengkung (radius) dari CardView terlihat
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        // Lebarkan dialog ke samping
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        // 3. Kenalkan elemen di dalam XML Dialog
        EditText etNewName = dialog.findViewById(R.id.et_new_bucket_name);
        TextView btnCancel = dialog.findViewById(R.id.btn_cancel_dialog);
        com.google.android.material.button.MaterialButton btnCreate = dialog.findViewById(R.id.btn_create_dialog);

        // 4. Pasang tombol Batal
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // 5. Pasang tombol Buat (Kirim ke API)
        btnCreate.setOnClickListener(v -> {
            String bucketName = etNewName.getText().toString().trim();
            if (bucketName.isEmpty()) {
                Toast.makeText(this, "Nama tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kunci tombol biar user tidak spam klik
            btnCreate.setEnabled(false);
            btnCreate.setText("Membuat...");

            apiService.createBucketList("Bearer " + tokenManager.getToken(), bucketName)
                    .enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(BucketListMainActivity.this, "Berhasil dibuat!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();

                                // Refresh daftar otomatis setelah berhasil buat
                                fetchBucketLists();
                            } else {
                                Toast.makeText(BucketListMainActivity.this, "Gagal membuat Bucket List", Toast.LENGTH_SHORT).show();
                                btnCreate.setEnabled(true);
                                btnCreate.setText("Buat");
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            Toast.makeText(BucketListMainActivity.this, "Error Jaringan", Toast.LENGTH_SHORT).show();
                            btnCreate.setEnabled(true);
                            btnCreate.setText("Buat");
                        }
                    });
        });

        // 6. Kalau dialog ditutup (entah klik batal, atau klik luar kotak), hilangkan layar gelap
        dialog.setOnDismissListener(dialogInterface -> viewDimOverlay.setVisibility(View.GONE));

        // 7. Tampilkan
        dialog.show();
    }
}