package com.example.wertugo.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2; // IMPORT VIEWPAGER2

import com.example.wertugo.R;
import com.example.wertugo.adapters.BannerCarouselAdapter; // IMPORT ADAPTER CAROUSEL
import com.example.wertugo.adapters.CommentAdapter;
import com.example.wertugo.api.ApiClient;
import com.example.wertugo.api.ApiService;
import com.example.wertugo.utils.TokenManager;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.wertugo.adapters.GalleryAdapter; // IMPORT ADAPTER GALERI
import com.bumptech.glide.Glide;

public class DetailTempatActivity extends AppCompatActivity {

    private TextView tvName, tvLocation, tvDesc, tvJadwal, tvStatusBuka, tvGalleryTitle;
    private ImageView ivVerifiedBadge, ivThumbnail;
    private RecyclerView rvGallery;
    private MaterialCardView btnBack, btnReportPlace;
    private com.google.android.material.button.MaterialButton btnAddBucketlist;
    private TokenManager tokenManager;
    private ApiService apiService;
    private String placeId;
    private String placeOwnerId;
    private String placeName = "";
    private String placeLocation = "";
    private String placeCategory = "";
    private String placeDesc = "";

    // Variabel untuk Komentar & Rating
    private RatingBar rbInputRating;
    private EditText etCommentInput;
    private ImageView btnSendComment;

    private RecyclerView rvComments;
    private CommentAdapter commentAdapter;
    private List<Map<String, Object>> commentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_detail_tempat);

            // 1. Inisialisasi View Detail (PASTIKAN ID SESUAI XML)
            tvName = findViewById(R.id.tv_detail_name);
            tvLocation = findViewById(R.id.tv_detail_location);
            tvDesc = findViewById(R.id.tv_detail_desc);
            tvJadwal = findViewById(R.id.tv_detail_jadwal);
            ivVerifiedBadge = findViewById(R.id.iv_verified_badge);
            tvStatusBuka = findViewById(R.id.tv_detail_status_buka);

            ivThumbnail = findViewById(R.id.iv_detail_thumbnail);
            rvGallery = findViewById(R.id.rv_detail_gallery);
            tvGalleryTitle = findViewById(R.id.tv_gallery_title);
            btnBack = findViewById(R.id.btn_back_detail);
            btnReportPlace = findViewById(R.id.btn_report_place);
            btnAddBucketlist = findViewById(R.id.btn_add_bucketlist);

            // 2. Inisialisasi View Komentar & Rating
            rbInputRating = findViewById(R.id.rb_input_rating);
            etCommentInput = findViewById(R.id.et_comment_input);
            btnSendComment = findViewById(R.id.btn_send_comment);
            rvComments = findViewById(R.id.rv_detail_comments);

            tokenManager = new TokenManager(this);
            apiService = ApiClient.getApiService(tokenManager.getToken());
            placeId = getIntent().getStringExtra("PLACE_ID");

            if (placeId == null || placeId.isEmpty()) {
                Toast.makeText(this, "ID Tempat tidak ditemukan!", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            btnBack.setOnClickListener(v -> finish());
            btnReportPlace.setOnClickListener(v -> {
                if (placeOwnerId != null) {
                    showReportDialog("umkm", null, placeOwnerId, tvName.getText().toString());
                } else {
                    Toast.makeText(this, "Sedang memuat data pemilik tempat...", Toast.LENGTH_SHORT).show();
                }
            });
            btnAddBucketlist.setOnClickListener(v -> showBucketListSelectionDialog());

            // 3. Setup RecyclerView Komentar
            rvComments.setLayoutManager(new LinearLayoutManager(this));
            String myUserId = tokenManager.getUserId();

            commentAdapter = new CommentAdapter(commentList, myUserId, new CommentAdapter.OnReportClickListener() {
                @Override
                public void onReportClick(String commentId, String reportedUserId, String username) {
                    showReportDialog("comment", commentId, reportedUserId, username);
                }
            });
            rvComments.setAdapter(commentAdapter);

            // 4. Tarik Data
            fetchDetailTempat();
            fetchComments();

            // 5. Kirim Komentar
            btnSendComment.setOnClickListener(v -> submitComment());

        } catch (Exception e) {
            Toast.makeText(this, "CRASH SETUP: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void fetchDetailTempat() {
        String token = tokenManager.getToken();
        apiService.getDetailTempat("Bearer " + token, placeId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        Map<String, Object> dataObj = (Map<String, Object>) response.body().get("data");

                        if (dataObj != null) {
                            placeName = dataObj.containsKey("nama_usaha") ? String.valueOf(dataObj.get("nama_usaha")) : "Nama tidak tersedia";
                            placeLocation = dataObj.containsKey("lokasi") ? String.valueOf(dataObj.get("lokasi")) : "Lokasi tidak tersedia";
                            placeCategory = dataObj.containsKey("kategori") ? String.valueOf(dataObj.get("kategori")) : "";
                            placeDesc = dataObj.containsKey("deskripsi") ? String.valueOf(dataObj.get("deskripsi")) : "Tidak ada deskripsi";

                            tvName.setText(placeName);
                            tvLocation.setText(placeLocation);
                            tvDesc.setText(placeDesc);
                            if (dataObj.containsKey("user_id")) {
                                placeOwnerId = String.valueOf(dataObj.get("user_id"));
                            }

                            // ==========================================
                            // 1. FITUR GAMBAR (THUMBNAIL & GALERI)
                            // ==========================================
                            List<String> imageList = new ArrayList<>();

                            Object rawGambar = dataObj.get("gambar");
                            if (rawGambar != null && !String.valueOf(rawGambar).isEmpty() && !String.valueOf(rawGambar).equals("null")) {
                                String imgStr = String.valueOf(rawGambar);
                                Glide.with(DetailTempatActivity.this)
                                        .load(ApiClient.IMG_BASE_URL + imgStr)
                                        .placeholder(R.drawable.bg_placeholder)
                                        .into(ivThumbnail);
                            }

                            Object rawKatalog = dataObj.get("katalog_galeri");
                            if (rawKatalog instanceof List) {
                                List<?> katalogList = (List<?>) rawKatalog;
                                for (Object item : katalogList) {
                                    if (item != null && !String.valueOf(item).isEmpty()) {
                                        String itemStr = String.valueOf(item);
                                        imageList.add(ApiClient.IMG_BASE_URL + itemStr);
                                    }
                                }
                            }

                            if (!imageList.isEmpty()) {
                                tvGalleryTitle.setVisibility(View.VISIBLE);
                                rvGallery.setVisibility(View.VISIBLE);
                                rvGallery.setLayoutManager(new LinearLayoutManager(DetailTempatActivity.this, LinearLayoutManager.HORIZONTAL, false));
                                GalleryAdapter galleryAdapter = new GalleryAdapter(imageList);
                                rvGallery.setAdapter(galleryAdapter);
                            }

                            // ==========================================
                            // 1.5 STATUS BUKA/TUTUP MANUAL
                            // ==========================================
                            Object rawIsOpen = dataObj.get("is_open");
                            if (rawIsOpen != null) {
                                String openStr = String.valueOf(rawIsOpen).toLowerCase();
                                boolean isOpen = openStr.equals("true") || openStr.equals("1") || openStr.equals("1.0");
                                if (isOpen) {
                                    tvStatusBuka.setText(" BUKA ");
                                    tvStatusBuka.setBackgroundResource(R.drawable.bg_green_rounded_filled);
                                } else {
                                    tvStatusBuka.setText(" TUTUP ");
                                    tvStatusBuka.setBackgroundColor(Color.parseColor("#D32F2F"));
                                }
                            } else {
                                tvStatusBuka.setVisibility(View.GONE);
                            }

                            // ==========================================
                            // 2. FITUR BADGE VERIFIKASI (CENTANG HIJAU)
                            // ==========================================
                            Object rawVerified = dataObj.get("verification_status");
                            if (rawVerified != null && rawVerified.equals("verified")) {
                                ivVerifiedBadge.setVisibility(View.VISIBLE);
                            } else {
                                ivVerifiedBadge.setVisibility(View.GONE);
                            }

                            // ==========================================
                            // 3. FITUR JADWAL OPERASIONAL (VERSI OBJECT/MAP)
                            // ==========================================
                            Object rawJadwal = dataObj.get("jadwal_operasional");
                            if (rawJadwal instanceof Map) {
                                Map<String, String> jadwalMap = (Map<String, String>) rawJadwal;

                                java.util.Locale localeID = new java.util.Locale("id", "ID");
                                String hariIni = new java.text.SimpleDateFormat("EEEE", localeID).format(new java.util.Date());

                                String jadwalHariIni = null;
                                for (Map.Entry<String, String> entry : jadwalMap.entrySet()) {
                                    if (entry.getKey().equalsIgnoreCase(hariIni)) {
                                        jadwalHariIni = entry.getValue();
                                        break;
                                    }
                                }

                                if (jadwalHariIni != null && !jadwalHariIni.equalsIgnoreCase("Tutup")) {
                                    tvJadwal.setText("Buka Hari Ini: " + jadwalHariIni);
                                } else if (jadwalHariIni != null && jadwalHariIni.equalsIgnoreCase("Tutup")) {
                                    tvJadwal.setText("Jadwal Hari Ini: TUTUP");
                                } else {
                                    tvJadwal.setText("Jadwal: Hubungi pihak tempat");
                                }
                            } else {
                                tvJadwal.setText("Jadwal: Belum Diatur");
                            }
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(DetailTempatActivity.this, "Error Proses: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(DetailTempatActivity.this, "Gagal koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchComments() {
        String token = tokenManager.getToken();
        ApiService apiService = ApiClient.getApiService(token);
        apiService.getPlaceReviews("Bearer " + token, placeId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Object dataObj = response.body().get("data");
                    // Response: { "data": { "summary": {...}, "comments": [...], "ratings": [...] } }
                    if (dataObj instanceof Map) {
                        Map<String, Object> dataMap = (Map<String, Object>) dataObj;
                        Object commentsObj = dataMap.get("comments");
                        if (commentsObj instanceof List) {
                            commentList.clear();
                            commentList.addAll((List<Map<String, Object>>) commentsObj);
                            commentAdapter.notifyDataSetChanged();
                        }
                    } else if (dataObj instanceof List) {
                        // Fallback: jika API langsung return list
                        commentList.clear();
                        commentList.addAll((List<Map<String, Object>>) dataObj);
                        commentAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
        });
    }

    private void submitComment() {
        String commentText = etCommentInput.getText().toString().trim();
        float ratingValue = rbInputRating.getRating();
        if (commentText.isEmpty()) return;

        String token = tokenManager.getToken();
        ApiService apiService = ApiClient.getApiService(token);

        Map<String, Object> body = new HashMap<>();
        body.put("content", commentText);
        body.put("rating", ratingValue);

        apiService.postComment("Bearer " + token, placeId, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    etCommentInput.setText("");
                    fetchComments();
                }
            }
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
        });
    }

    private void showReportDialog(String reportType, String commentId, String reportedUserId, String titleName) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_report, null);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();

        Spinner spinnerCategory = dialogView.findViewById(R.id.spinner_report_category);
        String[] categories = {"Spam", "SARA", "Kasar", "Penipuan", "Lainnya"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(spinnerAdapter);

        EditText etMessage = dialogView.findViewById(R.id.et_report_message);

        dialogView.findViewById(R.id.btn_cancel_report).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_submit_report).setOnClickListener(v -> {
            submitReportToApi(reportType, commentId, reportedUserId, spinnerCategory.getSelectedItem().toString(), etMessage.getText().toString(), dialog);
        });
        dialog.show();
    }

    private void submitReportToApi(String reportType, String commentId, String reportedUserId, String category, String message, android.app.AlertDialog dialog) {
        String token = tokenManager.getToken();
        ApiService apiService = ApiClient.getApiService(token);
        Map<String, Object> body = new HashMap<>();
        body.put("report_type", reportType);
        body.put("reported_user_id", reportedUserId);
        if (commentId != null) {
            body.put("comment_id", commentId);
        }
        body.put("report_category", category);
        body.put("report_message", message);

        apiService.postReport("Bearer " + token, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DetailTempatActivity.this, "Laporan terkirim", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
        });
    }

    private void showBucketListSelectionDialog() {
        btnAddBucketlist.setEnabled(false);
        btnAddBucketlist.setText("Memuat...");

        apiService.getBucketLists("Bearer " + tokenManager.getToken())
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        btnAddBucketlist.setEnabled(true);
                        btnAddBucketlist.setText("Add to Bucket List!");

                        if (response.isSuccessful() && response.body() != null) {
                            List<Map<String, Object>> data = (List<Map<String, Object>>) response.body().get("data");
                            if (data == null || data.isEmpty()) {
                                Toast.makeText(DetailTempatActivity.this, "Anda belum memiliki Bucket List", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String[] listTitles = new String[data.size()];
                            String[] listIds = new String[data.size()];

                            for (int i = 0; i < data.size(); i++) {
                                Map<String, Object> b = data.get(i);
                                listTitles[i] = (String) b.get("title");
                                listIds[i] = String.valueOf(b.get("_id") != null ? b.get("_id") : b.get("id"));
                            }

                            android.app.Dialog dialog = new android.app.Dialog(DetailTempatActivity.this);
                            dialog.setContentView(R.layout.dialog_select_bucketlist);
                            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                            dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

                            android.widget.ListView lvBucketlists = dialog.findViewById(R.id.lv_bucketlists);
                            android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(DetailTempatActivity.this, android.R.layout.simple_list_item_1, listTitles);
                            lvBucketlists.setAdapter(adapter);

                            lvBucketlists.setOnItemClickListener((parent, view, position, id) -> {
                                dialog.dismiss();
                                addToBucketList(listIds[position]);
                            });

                            dialog.findViewById(R.id.btn_cancel_select).setOnClickListener(v -> dialog.dismiss());
                            dialog.show();
                        } else {
                            Toast.makeText(DetailTempatActivity.this, "Gagal memuat Bucket List", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        btnAddBucketlist.setEnabled(true);
                        btnAddBucketlist.setText("Add to Bucket List!");
                        Toast.makeText(DetailTempatActivity.this, "Error Jaringan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addToBucketList(String bucketId) {
        btnAddBucketlist.setEnabled(false);
        btnAddBucketlist.setText("Menambahkan...");

        apiService.addPlaceToBucketList("Bearer " + tokenManager.getToken(), bucketId, placeId, placeName, placeLocation, placeDesc, placeCategory)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        btnAddBucketlist.setEnabled(true);
                        btnAddBucketlist.setText("Add to Bucket List!");
                        if (response.isSuccessful()) {
                            Toast.makeText(DetailTempatActivity.this, "Berhasil ditambahkan ke Bucket List!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(DetailTempatActivity.this, "Gagal menambahkan ke Bucket List", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        btnAddBucketlist.setEnabled(true);
                        btnAddBucketlist.setText("Add to Bucket List!");
                        Toast.makeText(DetailTempatActivity.this, "Error Jaringan", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}