package com.example.wertugo.activities;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wertugo.R;
import com.example.wertugo.adapters.BucketListPlaceAdapter;
import com.example.wertugo.api.ApiClient;
import com.example.wertugo.api.ApiService;
import com.example.wertugo.api.models.BucketListPlaceModel;
import com.example.wertugo.utils.TokenManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
    private LinearLayout layoutEmpty;

    private BucketListPlaceAdapter placeAdapter;
    private List<BucketListPlaceModel> placeList = new ArrayList<>();

    private TokenManager tokenManager;
    private ApiService apiService;

    // ==========================================
    // VARIABEL GLOBAL UNTUK UPLOAD GAMBAR
    // ==========================================
    private Uri selectedImageUri = null;
    private TextView tvDialogImageName;

    // Peluncur untuk membuka Galeri HP
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (tvDialogImageName != null && selectedImageUri != null) {
                        tvDialogImageName.setText("Gambar siap diupload! ✓");
                        tvDialogImageName.setTextColor(android.graphics.Color.parseColor("#008B09"));
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bucket_list_detail);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getApiService(tokenManager.getToken());

        if (getIntent() != null) {
            bucketId = getIntent().getStringExtra("BUCKET_ID");
            bucketName = getIntent().getStringExtra("BUCKET_NAME");
        }

        initViews();
        setupNavbar();

        // Rem Darurat Anti-Crash
        if (bucketId == null || bucketId.trim().isEmpty()) {
            Toast.makeText(this, "Gagal membuka: ID Bucket List tidak ditemukan!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (tvBucketTitle != null && bucketName != null) {
            tvBucketTitle.setText(bucketName);
        }

        if (rvBucketItems != null) {
            rvBucketItems.setLayoutManager(new LinearLayoutManager(this));
            placeAdapter = new BucketListPlaceAdapter(placeList, bucketId, (place, position) -> {
                deletePlace(place, position);
            });
            rvBucketItems.setAdapter(placeAdapter);
        }

        fetchDetailBucketList();

        if (fabAddItem != null) {
            fabAddItem.setOnClickListener(v -> showAddPlaceDialog());
        }

        if (btnOptions != null) {
            btnOptions.setOnClickListener(v -> showInviteDialog());
        }
    }

    private void initViews() {
        tvBucketTitle = findViewById(R.id.tv_bucket_title);
        btnOptions = findViewById(R.id.btn_bucket_options);
        rvBucketItems = findViewById(R.id.rv_bucket_items);
        fabAddItem = findViewById(R.id.fab_add_item);
        layoutEmpty = findViewById(R.id.layout_empty_bucket_items);

        ImageView btnBack = findViewById(R.id.btn_back_to_main);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupNavbar() {
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_traveller);
        bottomNav.setSelectedItemId(R.id.nav_bucketlist);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, HomeUserActivity.class));
                finish();
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_search) {
                Toast.makeText(this, "Fitur pencarian", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_bucketlist) {
                // Return to main bucket list
                startActivity(new Intent(this, BucketListMainActivity.class));
                finish();
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
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

                            if (responseData != null) {
                                // 1. Set Judul Bucket dari API
                                if (responseData.get("info") != null) {
                                    Map<String, Object> info = (Map<String, Object>) responseData.get("info");
                                    if (info.get("title") != null && tvBucketTitle != null) {
                                        tvBucketTitle.setText(String.valueOf(info.get("title")));
                                    }
                                }

                                // 2. Proses list places
                                if (responseData.get("places") != null) {
                                    List<Map<String, Object>> places = (List<Map<String, Object>>) responseData.get("places");
                                    placeList.clear();

                                    for (Map<String, Object> item : places) {
                                        String id = item.get("id") != null ? String.valueOf(item.get("id")) :
                                                (item.get("_id") != null ? String.valueOf(item.get("_id")) : "");

                                        String nama = item.get("nama_tempat") != null ? (String) item.get("nama_tempat") : "Tanpa Nama";
                                        String lokasi = item.get("lokasi") != null ? (String) item.get("lokasi") : "-";
                                        String kategori = item.get("kategori") != null ? (String) item.get("kategori") : "Umum";

                                        placeList.add(new BucketListPlaceModel(id, nama, lokasi, kategori));
                                    }

                                    if (placeAdapter != null) {
                                        placeAdapter.notifyDataSetChanged();
                                    }

                                    if (layoutEmpty != null && rvBucketItems != null) {
                                        if (placeList.isEmpty()) {
                                            layoutEmpty.setVisibility(View.VISIBLE);
                                            rvBucketItems.setVisibility(View.GONE);
                                        } else {
                                            layoutEmpty.setVisibility(View.GONE);
                                            rvBucketItems.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(BucketListDetailActivity.this, "Gagal memuat tempat", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ================= FITUR TAMBAH TEMPAT & GAMBAR ================= //

    private void showAddPlaceDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_add_place_manual);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        EditText etNama = dialog.findViewById(R.id.et_manual_nama_tempat);
        EditText etLokasi = dialog.findViewById(R.id.et_manual_lokasi);
        EditText etDeskripsi = dialog.findViewById(R.id.et_manual_deskripsi);
        android.widget.AutoCompleteTextView dropdownKategori = dialog.findViewById(R.id.dropdown_manual_kategori);

        MaterialCardView btnUpload = dialog.findViewById(R.id.btn_manual_upload_gambar);
        tvDialogImageName = dialog.findViewById(R.id.tv_manual_image_filename); // Inisialisasi Teks Gambar

        com.google.android.material.button.MaterialButton btnTambah = dialog.findViewById(R.id.btn_manual_submit_tambah);
        com.google.android.material.button.MaterialButton btnBatal = dialog.findViewById(R.id.btn_manual_submit_batal);

        String[] listKategori = {"Wisata Alam", "Kuliner & Cafe", "Theme Park", "Sejarah & Budaya", "Lainnya"};
        android.widget.ArrayAdapter<String> dropdownAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listKategori);
        dropdownKategori.setAdapter(dropdownAdapter);

        // Aksi klik buka galeri
        if (btnUpload != null) {
            btnUpload.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imagePickerLauncher.launch(intent);
            });
        }

        if (btnBatal != null) {
            btnBatal.setOnClickListener(v -> {
                selectedImageUri = null; // Reset gambar saat batal
                dialog.dismiss();
            });
        }

        if (btnTambah != null) {
            btnTambah.setOnClickListener(v -> {
                String namaTempat = etNama != null ? etNama.getText().toString().trim() : "";
                String lokasi = etLokasi != null ? etLokasi.getText().toString().trim() : "";
                String deskripsi = etDeskripsi != null ? etDeskripsi.getText().toString().trim() : "";
                String kategori = dropdownKategori != null ? dropdownKategori.getText().toString().trim() : "";

                if (namaTempat.isEmpty()) {
                    Toast.makeText(this, "Nama tempat wajib diisi!", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnTambah.setEnabled(false);
                btnTambah.setText("Menyimpan...");

                sendPlaceToApi(namaTempat, lokasi, deskripsi, kategori, dialog, btnTambah);
            });
        }

        dialog.show();
    }

    private void sendPlaceToApi(String namaTempat, String lokasi, String deskripsi, String kategori, Dialog dialog, MaterialButton btnTambah) {
    }

    private void showInviteDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_invite_bucket);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        EditText etEmail = dialog.findViewById(R.id.et_invite_email);
        TextView btnCancel = dialog.findViewById(R.id.btn_cancel_invite);
        com.google.android.material.button.MaterialButton btnSend = dialog.findViewById(R.id.btn_send_invite);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSend.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Email tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSend.setEnabled(false);
            btnSend.setText("Mengundang...");

            Map<String, String> body = new java.util.HashMap<>();
            body.put("email", email);

            apiService.inviteUser("Bearer " + tokenManager.getToken(), bucketId, body)
                    .enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(BucketListDetailActivity.this, "Undangan berhasil dikirim!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(BucketListDetailActivity.this, "Gagal mengirim undangan", Toast.LENGTH_SHORT).show();
                                btnSend.setEnabled(true);
                                btnSend.setText("Undang");
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            Toast.makeText(BucketListDetailActivity.this, "Error Jaringan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            btnSend.setEnabled(true);
                            btnSend.setText("Undang");
                        }
                    });
        });

        dialog.show();
    }

    private void deletePlace(BucketListPlaceModel place, int position) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hapus Tempat")
                .setMessage("Yakin ingin menghapus " + place.getNamaTempat() + " dari Bucket List?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    apiService.removePlace("Bearer " + tokenManager.getToken(), bucketId, place.getId())
                            .enqueue(new Callback<Map<String, Object>>() {
                                @Override
                                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(BucketListDetailActivity.this, "Tempat berhasil dihapus", Toast.LENGTH_SHORT).show();
                                        placeList.remove(position);
                                        placeAdapter.notifyItemRemoved(position);
                                        if (placeList.isEmpty()) {
                                            layoutEmpty.setVisibility(View.VISIBLE);
                                            rvBucketItems.setVisibility(View.GONE);
                                        }
                                    } else {
                                        Toast.makeText(BucketListDetailActivity.this, "Gagal menghapus tempat", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                                    Toast.makeText(BucketListDetailActivity.this, "Error Jaringan", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void getMimeTypeAndUpload(Uri uri, String namaTempat, String lokasi, String deskripsi, String kategori, android.app.Dialog dialog, com.google.android.material.button.MaterialButton btnTambah) {

        // 1. Bungkus teks biasa menjadi RequestBody agar bisa dikirim via Multipart
        RequestBody rbPlaceId = RequestBody.create("", MediaType.parse("text/plain"));
        RequestBody rbNama = RequestBody.create(namaTempat, MediaType.parse("text/plain"));
        RequestBody rbLokasi = RequestBody.create(lokasi, MediaType.parse("text/plain"));
        RequestBody rbDeskripsi = RequestBody.create(deskripsi, MediaType.parse("text/plain"));
        RequestBody rbKategori = RequestBody.create(kategori, MediaType.parse("text/plain"));

        // 2. Siapkan File Gambar (Jika Ada)
        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                File tempFile = File.createTempFile("upload", ".jpg", getCacheDir());
                FileOutputStream out = new FileOutputStream(tempFile);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.close();
                inputStream.close();

                RequestBody requestFile = RequestBody.create(tempFile, MediaType.parse("image/jpeg"));
                imagePart = MultipartBody.Part.createFormData("gambar", tempFile.getName(), requestFile);

            } catch (Exception e) {
                Toast.makeText(this, "Gagal memproses gambar", Toast.LENGTH_SHORT).show();
            }
        }

        // 3. Tembak API Multipart
        apiService.addPlaceToBucketList("Bearer " + tokenManager.getToken(), bucketId, rbPlaceId, rbNama, rbLokasi, rbDeskripsi, rbKategori, imagePart)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(BucketListDetailActivity.this, "Berhasil masuk daftar!", Toast.LENGTH_SHORT).show();
                            selectedImageUri = null; // Reset setelah sukses
                            dialog.dismiss();
                            fetchDetailBucketList();
                        } else {
                            Toast.makeText(BucketListDetailActivity.this, "Gagal menambahkan tempat", Toast.LENGTH_SHORT).show();
                            if (btnTambah != null) {
                                btnTambah.setEnabled(true);
                                btnTambah.setText("Tambah");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(BucketListDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        if (btnTambah != null) {
                            btnTambah.setEnabled(true);
                            btnTambah.setText("Tambah");
                        }
                    }
                });
    }
}