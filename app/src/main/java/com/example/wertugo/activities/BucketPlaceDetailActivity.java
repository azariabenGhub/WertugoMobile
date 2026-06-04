package com.example.wertugo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.wertugo.R;
import com.example.wertugo.adapters.MemberCommentAdapter;
import com.example.wertugo.api.ApiClient;
import com.example.wertugo.api.ApiService;
import com.example.wertugo.api.models.MemberCommentModel;
import com.example.wertugo.utils.TokenManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BucketPlaceDetailActivity extends AppCompatActivity {

    private String bucketId, placeId, placeName, placeLocation, placeDesc, placeCategory;

    private TextView tvName, tvAddress, tvDesc, tvCategory, tvOverallScore, tvEmptyComments;
    private ImageView ivPlaceImage;
    private MaterialButton btnRate, btnEdit;
    private MaterialCardView btnBack;
    private RecyclerView rvComments;

    private MemberCommentAdapter commentAdapter;
    private List<MemberCommentModel> commentList = new ArrayList<>();

    private TokenManager tokenManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bucket_place_detail);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getApiService(tokenManager.getToken());

        if (getIntent() != null) {
            bucketId = getIntent().getStringExtra("BUCKET_ID");
            placeId = getIntent().getStringExtra("PLACE_ID");
            placeName = getIntent().getStringExtra("PLACE_NAME");
            placeLocation = getIntent().getStringExtra("PLACE_LOCATION");
            placeDesc = getIntent().getStringExtra("PLACE_DESC");
            placeCategory = getIntent().getStringExtra("PLACE_CATEGORY");
        }

        initViews();
        setupDataData();
        setupNavbar();

        btnBack.setOnClickListener(v -> finish());

        rvComments.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new MemberCommentAdapter(commentList);
        rvComments.setAdapter(commentAdapter);

        fetchPlaceDetails();

        btnRate.setOnClickListener(v -> showRateDialog());

        btnEdit.setOnClickListener(v -> showEditDialog());
    }

    private void showEditDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_place);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        EditText etNama = dialog.findViewById(R.id.et_edit_nama_tempat);
        EditText etLokasi = dialog.findViewById(R.id.et_edit_lokasi);
        EditText etDeskripsi = dialog.findViewById(R.id.et_edit_deskripsi);
        android.widget.AutoCompleteTextView dropdownKategori = dialog.findViewById(R.id.dropdown_edit_kategori);

        MaterialButton btnSubmit = dialog.findViewById(R.id.btn_submit_edit);
        MaterialButton btnCancel = dialog.findViewById(R.id.btn_cancel_edit);

        String[] listKategori = {"Wisata Alam", "Kuliner & Cafe", "Theme Park", "Sejarah & Budaya", "Lainnya"};
        android.widget.ArrayAdapter<String> dropdownAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listKategori);
        dropdownKategori.setAdapter(dropdownAdapter);

        etNama.setText(placeName);
        etLokasi.setText(placeLocation);
        etDeskripsi.setText(placeDesc);
        dropdownKategori.setText(placeCategory, false);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String newNama = etNama.getText().toString().trim();
            String newLokasi = etLokasi.getText().toString().trim();
            String newDesc = etDeskripsi.getText().toString().trim();
            String newKategori = dropdownKategori.getText().toString().trim();

            if (newNama.isEmpty()) {
                Toast.makeText(this, "Nama tempat wajib diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSubmit.setEnabled(false);
            btnSubmit.setText("Menyimpan...");

            apiService.updatePlace("Bearer " + tokenManager.getToken(), bucketId, placeId, newNama, newLokasi, newDesc, newKategori)
                    .enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(BucketPlaceDetailActivity.this, "Berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();

                                placeName = newNama;
                                placeLocation = newLokasi;
                                placeDesc = newDesc;
                                placeCategory = newKategori;
                                setupDataData();

                            } else {
                                Toast.makeText(BucketPlaceDetailActivity.this, "Gagal mengubah tempat", Toast.LENGTH_SHORT).show();
                                btnSubmit.setEnabled(true);
                                btnSubmit.setText("Simpan");
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            Toast.makeText(BucketPlaceDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            btnSubmit.setEnabled(true);
                            btnSubmit.setText("Simpan");
                        }
                    });
        });

        dialog.show();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvEmptyComments = findViewById(R.id.tv_empty_comments);

        tvName = findViewById(R.id.tv_detail_place_name);
        tvAddress = findViewById(R.id.tv_detail_place_address);
        tvDesc = findViewById(R.id.tv_detail_place_desc);
        tvCategory = findViewById(R.id.tv_detail_place_category);
        tvOverallScore = findViewById(R.id.tv_overall_rating_score);
        btnRate = findViewById(R.id.btn_action_rate);
        btnEdit = findViewById(R.id.btn_action_edit);
        rvComments = findViewById(R.id.rv_member_comments);

        ivPlaceImage = findViewById(R.id.iv_detail_place_image);
    }

    private void setupDataData() {
        if (placeName != null) tvName.setText(placeName);
        if (placeLocation != null) tvAddress.setText(placeLocation);
        if (placeDesc != null) tvDesc.setText(placeDesc);
        if (placeCategory != null) tvCategory.setText(placeCategory);
    }

    private void showRateDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_rate_place);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView tvPopupTitle = dialog.findViewById(R.id.tv_popup_place_name);
        RatingBar ratingInput = dialog.findViewById(R.id.rating_bar_input);
        EditText etComment = dialog.findViewById(R.id.et_rating_comment);
        MaterialButton btnConfirm = dialog.findViewById(R.id.btn_submit_rating);
        MaterialButton btnCancel = dialog.findViewById(R.id.btn_cancel_rating);

        if (placeName != null) {
            tvPopupTitle.setText(placeName);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            int ratingValue = (int) ratingInput.getRating();
            String commentText = etComment.getText().toString().trim();

            if (ratingValue == 0) {
                Toast.makeText(this, "Harap pilih bintang terlebih dahulu!", Toast.LENGTH_SHORT).show();
                return;
            }

            btnConfirm.setEnabled(false);
            btnConfirm.setText("Mengirim...");

            apiService.ratePlace("Bearer " + tokenManager.getToken(), bucketId, placeId, ratingValue, commentText)
                    .enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(BucketPlaceDetailActivity.this, "Rating berhasil disimpan!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                fetchPlaceDetails();
                            } else {
                                Toast.makeText(BucketPlaceDetailActivity.this, "Gagal memberi penilaian", Toast.LENGTH_SHORT).show();
                                btnConfirm.setEnabled(true);
                                btnConfirm.setText("Konfirmasi");
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            Toast.makeText(BucketPlaceDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            btnConfirm.setEnabled(true);
                            btnConfirm.setText("Konfirmasi");
                        }
                    });
        });

        dialog.show();
    }

    private void fetchPlaceDetails() {
        apiService.getBucketListDetail("Bearer " + tokenManager.getToken(), bucketId)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Map<String, Object> body = response.body();
                            Map<String, Object> data = (Map<String, Object>) body.get("data");
                            List<Map<String, Object>> places = (List<Map<String, Object>>) data.get("places");

                            if (places != null) {
                                for (Map<String, Object> place : places) {
                                    String currentPlaceId = "";
                                    if (place.get("id") != null) {
                                        currentPlaceId = String.valueOf(place.get("id"));
                                    } else if (place.get("_id") != null) {
                                        currentPlaceId = String.valueOf(place.get("_id"));
                                    }

                                    if (currentPlaceId.equals(placeId)) {

                                        // ==========================================
                                        // LOGIKA GLIDE DENGAN DEBUG & ERROR ICON
                                        // ==========================================
                                        String gambarPath = place.get("gambar") != null ? String.valueOf(place.get("gambar")) : "";

                                        if (!gambarPath.isEmpty() && !gambarPath.equals("null")) {
                                            // PENTING: Jika API Client kamu pakai IP Wifi (192.168.x.x),
                                            // ubah 10.0.2.2 di bawah ini menjadi IP Wifi tersebut!
                                            String imageUrl = ApiClient.IMG_BASE_URL + gambarPath;

                                            // Menampilkan log URL di terminal Android Studio
                                            Log.d("CEK_GAMBAR", "Mencoba load URL: " + imageUrl);

                                            Glide.with(BucketPlaceDetailActivity.this)
                                                    .load(imageUrl)
                                                    .centerCrop()
                                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                                    .error(android.R.drawable.stat_notify_error) // 👈 Ganti ikon tanda seru/silang kalau error
                                                    .into(ivPlaceImage);
                                        } else {
                                            if (ivPlaceImage != null) {
                                                ivPlaceImage.setImageResource(android.R.drawable.ic_menu_gallery);
                                            }
                                        }
                                        // ==========================================

                                        List<Map<String, Object>> commentsRaw = (List<Map<String, Object>>) place.get("comments");
                                        commentList.clear();

                                        float totalRating = 0f;
                                        int jumlahKomentar = 0;

                                        if (commentsRaw != null && !commentsRaw.isEmpty()) {
                                            for (Map<String, Object> c : commentsRaw) {
                                                float rating = 0f;
                                                Object rawRating = c.get("rating");
                                                if (rawRating != null) {
                                                    try {
                                                        rating = Float.parseFloat(String.valueOf(rawRating));
                                                    } catch (NumberFormatException e) {
                                                        rating = 0f;
                                                    }
                                                }
                                                totalRating += rating;
                                                jumlahKomentar++;

                                                commentList.add(new MemberCommentModel(
                                                        (String) c.get("user_name"),
                                                        rating,
                                                        (String) c.get("comment")
                                                ));
                                            }
                                        }
                                        commentAdapter.notifyDataSetChanged();

                                        if (commentList.isEmpty()) {
                                            tvEmptyComments.setVisibility(View.VISIBLE);
                                            rvComments.setVisibility(View.GONE);
                                        } else {
                                            tvEmptyComments.setVisibility(View.GONE);
                                            rvComments.setVisibility(View.VISIBLE);
                                        }

                                        TextView tvRatingScore = findViewById(R.id.tv_overall_rating_score);
                                        TextView tvRatingCount = findViewById(R.id.tv_overall_rating_count);

                                        if (jumlahKomentar > 0) {
                                            float rataRata = totalRating / jumlahKomentar;
                                            tvRatingScore.setText(String.format(java.util.Locale.getDefault(), "%.1f/5", rataRata));
                                            tvRatingCount.setText("Dirating sebanyak " + jumlahKomentar + " kali");
                                        } else {
                                            tvRatingScore.setText("0/5");
                                            tvRatingCount.setText("Belum ada rating");
                                        }

                                        break;
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(BucketPlaceDetailActivity.this, "Error memuat data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
                Toast.makeText(this, "Fitur pencarian belum tersedia", Toast.LENGTH_SHORT).show();
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
}