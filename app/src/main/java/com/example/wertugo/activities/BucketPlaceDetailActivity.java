package com.example.wertugo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wertugo.R;
import com.example.wertugo.adapters.MemberCommentAdapter;
import com.example.wertugo.api.ApiClient;
import com.example.wertugo.api.ApiService;
import com.example.wertugo.api.models.MemberCommentModel;
import com.example.wertugo.utils.TokenManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BucketPlaceDetailActivity extends AppCompatActivity {

    private String bucketId, placeId, placeName, placeLocation, placeDesc, placeCategory;

    private TextView tvName, tvAddress, tvDesc, tvCategory, tvOverallScore;
    private MaterialButton btnRate, btnEdit;
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

        // 1. Tangkap data dari Intent yang dikirim oleh BucketListPlaceAdapter
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

        // 2. Setup RecyclerView Komentar Anggota
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new MemberCommentAdapter(commentList);
        rvComments.setAdapter(commentAdapter);

        // Ambil data detail (termasuk list komentar dari backend)
        fetchPlaceDetails();

        // 3. Tombol Rate diklik -> Munculkan Popup Dialog
        btnRate.setOnClickListener(v -> showRateDialog());

        btnEdit.setOnClickListener(v -> {
            Toast.makeText(this, "Fitur Edit Tempat menyusul!", Toast.LENGTH_SHORT).show();
        });
    }

    private void initViews() {
        tvName = findViewById(R.id.tv_detail_place_name);
        tvAddress = findViewById(R.id.tv_detail_place_address);
        tvDesc = findViewById(R.id.tv_detail_place_desc);
        tvCategory = findViewById(R.id.tv_detail_place_category);
        tvOverallScore = findViewById(R.id.tv_overall_rating_score);
        btnRate = findViewById(R.id.btn_action_rate);
        btnEdit = findViewById(R.id.btn_action_edit);
        rvComments = findViewById(R.id.rv_member_comments);
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

            // Kirim rating ke API Laravel
            apiService.ratePlace("Bearer " + tokenManager.getToken(), bucketId, placeId, ratingValue, commentText)
                    .enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(BucketPlaceDetailActivity.this, "Rating berhasil disimpan!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                fetchPlaceDetails(); // Refresh list komentar
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
        // Panggil API ke Laravel kamu
        apiService.getBucketListDetail("Bearer " + tokenManager.getToken(), bucketId)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // 1. Ambil data dari response
                            Map<String, Object> data = (Map<String, Object>) response.body().get("data");
                            List<Map<String, Object>> places = (List<Map<String, Object>>) data.get("places");

                            // 2. Cari tempat yang sesuai dengan placeId
                            for (Map<String, Object> place : places) {
                                if (String.valueOf(place.get("id")).equals(placeId)) {
                                    // 3. Ekstrak komentar
                                    List<Map<String, Object>> commentsRaw = (List<Map<String, Object>>) place.get("comments");
                                    commentList.clear();

                                    if (commentsRaw != null) {
                                        for (Map<String, Object> c : commentsRaw) {
                                            commentList.add(new MemberCommentModel(
                                                    (String) c.get("user_name"),
                                                    ((Double) c.get("rating")).floatValue(),
                                                    (String) c.get("comment")
                                            ));
                                        }
                                    }
                                    commentAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                    // ... (onFailure)
                });
    }

    private void setupNavbar() {
        ImageView navHome = findViewById(R.id.nav_home);
        ImageView navSearch = findViewById(R.id.nav_search);
        ImageView navMessage = findViewById(R.id.nav_message);
        ImageView navProfile = findViewById(R.id.nav_profile);

        // Cara aman mewarnai background kotak menu Bucket List
        if (navMessage != null && navMessage.getParent() instanceof android.widget.LinearLayout) {
            ((android.widget.LinearLayout) navMessage.getParent()).setBackgroundResource(R.drawable.bg_nav_active);
        }

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                startActivity(new Intent(this, HomeUserActivity.class));
                finish();
                overridePendingTransition(0, 0);
            });
        }

        if (navSearch != null) {
            navSearch.setOnClickListener(v ->
                    Toast.makeText(this, "Fitur pencarian belum tersedia", Toast.LENGTH_SHORT).show()
            );
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                overridePendingTransition(0, 0);
            });
        }
    }
}