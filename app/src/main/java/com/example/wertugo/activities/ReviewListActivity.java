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
    private TokenManager tokenManager;
    private ApiService apiService;
    private String placeId;
    private boolean isUmkm = false;

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
        reviewAdapter = new ReviewAdapter(reviewList);
        rvReviews.setAdapter(reviewAdapter);

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

    private void parseReviews(Map<String, Object> responseBody) {
        try {
            Object dataObj = responseBody.get("data");
            if (dataObj instanceof List) {
                List<?> dataList = (List<?>) dataObj;
                reviewList.clear();
                for (Object item : dataList) {
                    if (item instanceof Map) {
                        Map<String, Object> map = (Map<String, Object>) item;
                        Review review = new Review();
                        review.setId((String) map.get("id"));
                        review.setUser_name((String) map.get("user_name"));
                        review.setComment((String) map.get("comment"));
                        review.setRating(((Number) map.get("rating")).floatValue());
                        review.setCreated_at((String) map.get("created_at"));
                        review.setUser_avatar((String) map.get("user_avatar"));
                        reviewList.add(review);
                    }
                }
                reviewAdapter.updateList(reviewList);
            } else if (dataObj instanceof Map) {
                // jika response berupa single object (tidak array), tetap treat sebagai list
                Map<String, Object> map = (Map<String, Object>) dataObj;
                Review review = new Review();
                review.setUser_name((String) map.get("user_name"));
                review.setComment((String) map.get("comment"));
                review.setRating(((Number) map.get("rating")).floatValue());
                review.setCreated_at((String) map.get("created_at"));
                reviewList.add(review);
                reviewAdapter.updateList(reviewList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal parsing data", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNav() {
        // Ambil view hasil include
        View bottomNavView = findViewById(R.id.bottom_nav_view);

        ImageView navHome = bottomNavView.findViewById(R.id.nav_home);
        ImageView navStar = bottomNavView.findViewById(R.id.nav_star);
        ImageView navProfile = bottomNavView.findViewById(R.id.nav_profile);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeUserActivity.class));
            finish();
        });
        navStar.setOnClickListener(v -> {
            Toast.makeText(this, "Fitur Wishlist segera hadir", Toast.LENGTH_SHORT).show();
        });
        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
    }
}