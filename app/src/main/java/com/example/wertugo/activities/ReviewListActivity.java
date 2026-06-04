package com.example.wertugo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wertugo.R;
import com.example.wertugo.adapters.ReviewAdapter;
import com.example.wertugo.api.ApiClient;
import com.example.wertugo.api.ApiService;
import com.example.wertugo.api.models.Review;
import com.example.wertugo.utils.TokenManager;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewListActivity extends AppCompatActivity {

    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList = new ArrayList<>();
    private List<Review> originalReviewList = new ArrayList<>();
    private TokenManager tokenManager;
    private ApiService apiService;
    private String placeId;
    private boolean isUmkm = false;

    // View Components for Filter
    private android.widget.LinearLayout btnFilterRating, btnSortBy;
    private android.widget.TextView tvFilterRating, tvSortBy;
    
    // Filter State
    private int currentRatingFilter = 0; // 0 = Semua
    private boolean isSortLatest = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_list);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getApiService();

        // Terima data dari Intent
        placeId = getIntent().getStringExtra("place_id");
        isUmkm = getIntent().getBooleanExtra("is_umkm", false);

        if (placeId == null) {
            Toast.makeText(this, "ID tempat tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rvReviews = findViewById(R.id.rv_reviews);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(reviewList, isUmkm, (review, position) -> {
            showReplyDialog(review, position);
        });
        rvReviews.setAdapter(reviewAdapter);

        // Setup Filter Buttons
        btnFilterRating = findViewById(R.id.btn_filter_rating);
        btnSortBy = findViewById(R.id.btn_sort_by);
        tvFilterRating = findViewById(R.id.tv_filter_rating);
        tvSortBy = findViewById(R.id.tv_sort_by);

        btnFilterRating.setOnClickListener(v -> showRatingFilterMenu());
        btnSortBy.setOnClickListener(v -> showSortMenu());

        loadReviews();

        // Setup bottom navigation
        setupBottomNav();
    }

    private void loadReviews() {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Sesi habis, silakan login ulang", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Call<Map<String, Object>> call;
        if (isUmkm) {
            call = apiService.getUmkmComments("Bearer " + token, placeId);
        } else {
            call = apiService.getPlaceReviews("Bearer " + token, placeId);
        }

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    parseReviews(response.body());
                } else {
                    Toast.makeText(ReviewListActivity.this, "Gagal memuat ulasan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ReviewListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showReplyDialog(Review review, int position) {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Ketik balasan Anda...");
        input.setPadding(32, 32, 32, 32);
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Balas Ulasan")
                .setView(input)
                .setPositiveButton("Kirim", (dialog, which) -> {
                    String replyText = input.getText().toString().trim();
                    if (!replyText.isEmpty()) {
                        sendReply(review, position, replyText);
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void sendReply(Review review, int position, String replyText) {
        String token = tokenManager.getToken();
        apiService.replyComment("Bearer " + token, review.getId(), replyText)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(ReviewListActivity.this, "Balasan terkirim", Toast.LENGTH_SHORT).show();
                            review.setReply(replyText);
                            reviewAdapter.notifyItemChanged(position);
                        } else {
                            Toast.makeText(ReviewListActivity.this, "Gagal mengirim balasan", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(ReviewListActivity.this, "Error Jaringan", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void parseReviews(Map<String, Object> responseBody) {
        try {
            Object dataObj = responseBody.get("data");
            List<?> dataList = null;
            if (dataObj instanceof List) {
                dataList = (List<?>) dataObj;
            } else if (dataObj instanceof Map) {
                Map<String, Object> dataMap = (Map<String, Object>) dataObj;
                if (dataMap.containsKey("comments")) {
                    Object commentsObj = dataMap.get("comments");
                    if (commentsObj instanceof List) {
                        dataList = (List<?>) commentsObj;
                    }
                }
            }

            if (dataList != null) {
                originalReviewList.clear();
                for (Object item : dataList) {
                    if (item instanceof Map) {
                        Map<String, Object> map = (Map<String, Object>) item;
                        Review review = new Review();
                        review.setId(map.containsKey("id") ? String.valueOf(map.get("id")) : "");
                        
                        // Isi komentar di database ada di field "content"
                        if (map.containsKey("content")) {
                            review.setComment((String) map.get("content"));
                        } else if (map.containsKey("comment")) {
                            review.setComment((String) map.get("comment"));
                        } else {
                            review.setComment("");
                        }
                        
                        if (map.containsKey("rating") && map.get("rating") != null) {
                            review.setRating(((Number) map.get("rating")).floatValue());
                        }
                        review.setCreated_at(map.containsKey("created_at") ? (String) map.get("created_at") : "");
                        review.setReply(map.containsKey("reply") ? (String) map.get("reply") : null);

                        if (map.containsKey("user")) {
                            Map<String, Object> userMap = (Map<String, Object>) map.get("user");
                            review.setUser_name(userMap.containsKey("username") ? (String) userMap.get("username") : "User");
                            review.setUser_avatar(userMap.containsKey("foto_profil") ? (String) userMap.get("foto_profil") : null);
                        } else {
                            review.setUser_name(map.containsKey("user_name") ? (String) map.get("user_name") : "User");
                            review.setUser_avatar(map.containsKey("user_avatar") ? (String) map.get("user_avatar") : null);
                        }
                        originalReviewList.add(review);
                    }
                }
                applyFilters();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal parsing data", Toast.LENGTH_SHORT).show();
        }
    }

    private void showRatingFilterMenu() {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, btnFilterRating);
        popup.getMenu().add(0, 0, 0, "Semua Rating");
        popup.getMenu().add(0, 5, 1, "Bintang 5");
        popup.getMenu().add(0, 4, 2, "Bintang 4");
        popup.getMenu().add(0, 3, 3, "Bintang 3");
        popup.getMenu().add(0, 2, 4, "Bintang 2");
        popup.getMenu().add(0, 1, 5, "Bintang 1");
        
        popup.setOnMenuItemClickListener(item -> {
            currentRatingFilter = item.getItemId();
            tvFilterRating.setText(item.getTitle());
            applyFilters();
            return true;
        });
        popup.show();
    }

    private void showSortMenu() {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, btnSortBy);
        popup.getMenu().add(0, 0, 0, "Terbaru");
        popup.getMenu().add(0, 1, 1, "Terlama");
        
        popup.setOnMenuItemClickListener(item -> {
            isSortLatest = (item.getItemId() == 0);
            tvSortBy.setText(item.getTitle());
            applyFilters();
            return true;
        });
        popup.show();
    }

    private void applyFilters() {
        reviewList.clear();
        for (Review r : originalReviewList) {
            if (currentRatingFilter == 0 || Math.round(r.getRating()) == currentRatingFilter) {
                reviewList.add(r);
            }
        }
        
        java.util.Collections.sort(reviewList, (r1, r2) -> {
            String date1 = r1.getCreated_at() != null ? r1.getCreated_at() : "";
            String date2 = r2.getCreated_at() != null ? r2.getCreated_at() : "";
            return isSortLatest ? date2.compareTo(date1) : date1.compareTo(date2);
        });
        
        reviewAdapter.notifyDataSetChanged();
    }

    private void setupBottomNav() {
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_view);
        bottomNav.setSelectedItemId(R.id.nav_star);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home_umkm) {
                Intent intent = new Intent(this, isUmkm ? UmkmHomeActivity.class : HomeUserActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_star) {
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(this, isUmkm ? UmkmProfileActivity.class : ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }
}