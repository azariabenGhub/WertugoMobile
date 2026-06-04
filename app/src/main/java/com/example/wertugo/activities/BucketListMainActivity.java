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
    private ImageView btnInvitations;

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        // AUTO-REFRESH: Setiap kali halaman utama ini dibuka kembali (setelah Back),
        // kita paksa mengambil data terupdate dari server Laravel
        fetchBucketLists();
    }

    private void initViews() {
        layoutEmptyBucket = findViewById(R.id.layout_empty_bucket);
        rvBucketRooms = findViewById(R.id.rv_bucket_rooms);
        fabCreateBucket = findViewById(R.id.fab_create_bucket);
        viewDimOverlay = findViewById(R.id.view_dim_overlay_bucket);
        etSearchBucket = findViewById(R.id.et_search_bucket);
        btnInvitations = findViewById(R.id.btn_invitations);

        btnInvitations.setOnClickListener(v -> showInvitationsDialog());
    }

    private void setupRecyclerView() {
        rvBucketRooms.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BucketListAdapter(bucketList);
        rvBucketRooms.setAdapter(adapter);
    }

    private void setupNavbar() {
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_traveller);
        bottomNav.setSelectedItemId(R.id.nav_bucketlist);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(this, HomeUserActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_search) {
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_bucketlist) {
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
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

                                    // Mengecek "id" atau "_id" secara dinamis dan aman
                                    String id = "";
                                    if (item.get("id") != null) {
                                        id = String.valueOf(item.get("id"));
                                    } else if (item.get("_id") != null) {
                                        id = String.valueOf(item.get("_id"));
                                    }

                                    String nama = (String) item.get("title"); // Sesuai kolom di Laravel

                                    // SINKRONISASI FIX: Mengambil data hitungan asli dari variabel response Laravel
                                    int totalMember = 1;
                                    if (item.get("member_count") != null) {
                                        totalMember = (int) Double.parseDouble(String.valueOf(item.get("member_count")));
                                    }

                                    int totalTempat = 0;
                                    if (item.get("tempat_count") != null) {
                                        totalTempat = (int) Double.parseDouble(String.valueOf(item.get("tempat_count")));
                                    }

                                    // ==========================================
                                    // FIX: TANGKAP GAMBAR DARI API
                                    // ==========================================
                                    String gambar = item.get("gambar") != null ? String.valueOf(item.get("gambar")) : "";

                                    // FIX: MASUKKAN GAMBAR SEBAGAI PARAMETER KE-5
                                    bucketList.add(new BucketListModel(id, nama, totalMember, totalTempat, gambar));
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
        viewDimOverlay.setVisibility(View.VISIBLE);

        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_create_bucket);

        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        EditText etNewName = dialog.findViewById(R.id.et_new_bucket_name);
        TextView btnCancel = dialog.findViewById(R.id.btn_cancel_dialog);
        com.google.android.material.button.MaterialButton btnCreate = dialog.findViewById(R.id.btn_create_dialog);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String bucketName = etNewName.getText().toString().trim();
            if (bucketName.isEmpty()) {
                Toast.makeText(this, "Nama tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                return;
            }

            btnCreate.setEnabled(false);
            btnCreate.setText("Membuat...");

            apiService.createBucketList("Bearer " + tokenManager.getToken(), bucketName)
                    .enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(BucketListMainActivity.this, "Berhasil dibuat!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
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

        dialog.setOnDismissListener(dialogInterface -> viewDimOverlay.setVisibility(View.GONE));
        dialog.show();
    }

    private void showInvitationsDialog() {
        viewDimOverlay.setVisibility(View.VISIBLE);

        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_create_bucket); // Kita recycle dulu layoutnya dan ubah secara dinamis untuk menghemat waktu
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView tvTitle = dialog.findViewById(R.id.tv_dialog_title);
        if(tvTitle != null) tvTitle.setText("Undangan Kolaborasi");

        EditText etNewName = dialog.findViewById(R.id.et_new_bucket_name);
        if(etNewName != null) etNewName.setVisibility(View.GONE);

        TextView btnCancel = dialog.findViewById(R.id.btn_cancel_dialog);
        if(btnCancel != null) btnCancel.setText("Tutup");

        com.google.android.material.button.MaterialButton btnCreate = dialog.findViewById(R.id.btn_create_dialog);
        if(btnCreate != null) btnCreate.setVisibility(View.GONE);

        // Tambahkan ScrollView dan LinearLayout untuk daftar undangan
        LinearLayout parentLayout = (LinearLayout) etNewName.getParent();
        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        LinearLayout listLayout = new LinearLayout(this);
        listLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(listLayout);
        parentLayout.addView(scrollView, 1);

        TextView tvLoading = new TextView(this);
        tvLoading.setText("Memuat undangan...");
        tvLoading.setPadding(0, 16, 0, 16);
        listLayout.addView(tvLoading);

        apiService.getInvitations("Bearer " + tokenManager.getToken())
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        listLayout.removeAllViews();
                        if (response.isSuccessful() && response.body() != null) {
                            List<Map<String, Object>> data = (List<Map<String, Object>>) response.body().get("data");
                            if (data == null || data.isEmpty()) {
                                TextView tvEmpty = new TextView(BucketListMainActivity.this);
                                tvEmpty.setText("Tidak ada undangan baru.");
                                tvEmpty.setPadding(0, 16, 0, 16);
                                listLayout.addView(tvEmpty);
                            } else {
                                for (Map<String, Object> inv : data) {
                                    String bucketId = (String) inv.get("bucketlist_id");
                                    String inviterId = (String) inv.get("inviter_id");
                                    
                                    LinearLayout itemLayout = new LinearLayout(BucketListMainActivity.this);
                                    itemLayout.setOrientation(LinearLayout.VERTICAL);
                                    itemLayout.setPadding(0, 16, 0, 24);
                                    
                                    String inviterName = (String) inv.get("inviter_name");
                                    if(inviterName == null) inviterName = "Seseorang";
                                    String bucketTitle = (String) inv.get("bucket_title");
                                    if(bucketTitle == null) bucketTitle = "Bucket List";
                                    
                                    TextView tvInfo = new TextView(BucketListMainActivity.this);
                                    tvInfo.setText(inviterName + " mengundang Anda ke " + bucketTitle + "!");
                                    tvInfo.setTextColor(android.graphics.Color.BLACK);
                                    itemLayout.addView(tvInfo);

                                    LinearLayout btnLayout = new LinearLayout(BucketListMainActivity.this);
                                    btnLayout.setOrientation(LinearLayout.HORIZONTAL);
                                    btnLayout.setPadding(0, 8, 0, 0);

                                    com.google.android.material.button.MaterialButton btnTerima = new com.google.android.material.button.MaterialButton(BucketListMainActivity.this);
                                    btnTerima.setText("Terima");
                                    btnTerima.setBackgroundColor(android.graphics.Color.parseColor("#008B09"));
                                    btnTerima.setTextColor(android.graphics.Color.WHITE);
                                    btnTerima.setOnClickListener(v -> respondToInvitation(bucketId, true, dialog));

                                    com.google.android.material.button.MaterialButton btnTolak = new com.google.android.material.button.MaterialButton(BucketListMainActivity.this);
                                    btnTolak.setText("Tolak");
                                    btnTolak.setBackgroundColor(android.graphics.Color.parseColor("#D32F2F"));
                                    btnTolak.setTextColor(android.graphics.Color.WHITE);
                                    btnTolak.setOnClickListener(v -> respondToInvitation(bucketId, false, dialog));

                                    btnLayout.addView(btnTerima);
                                    
                                    View space = new View(BucketListMainActivity.this);
                                    space.setLayoutParams(new LinearLayout.LayoutParams(16, 1));
                                    btnLayout.addView(space);
                                    
                                    btnLayout.addView(btnTolak);
                                    itemLayout.addView(btnLayout);

                                    listLayout.addView(itemLayout);
                                }
                            }
                        } else {
                            TextView tvError = new TextView(BucketListMainActivity.this);
                            tvError.setText("Gagal memuat undangan.");
                            listLayout.addView(tvError);
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        listLayout.removeAllViews();
                        TextView tvError = new TextView(BucketListMainActivity.this);
                        tvError.setText("Error jaringan.");
                        listLayout.addView(tvError);
                    }
                });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.setOnDismissListener(dialogInterface -> viewDimOverlay.setVisibility(View.GONE));
        dialog.show();
    }

    private void respondToInvitation(String bucketId, boolean accept, android.app.Dialog parentDialog) {
        Map<String, Boolean> body = new java.util.HashMap<>();
        body.put("accept", accept);

        apiService.joinSharedList("Bearer " + tokenManager.getToken(), bucketId, body)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(BucketListMainActivity.this, accept ? "Berhasil bergabung!" : "Undangan ditolak", Toast.LENGTH_SHORT).show();
                            parentDialog.dismiss();
                            fetchBucketLists();
                        } else {
                            Toast.makeText(BucketListMainActivity.this, "Gagal memproses undangan", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(BucketListMainActivity.this, "Error Jaringan", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}