package com.example.wertugo.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wertugo.R;
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

public class DetailTempatActivity extends AppCompatActivity {

    private TextView tvName, tvLocation, tvDesc, tvStatus;
    private TokenManager tokenManager;
    private String placeId;

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

            // 1. Inisialisasi View Detail Utama
            tvName = findViewById(R.id.tv_detail_name);
            tvLocation = findViewById(R.id.tv_detail_location);
            tvDesc = findViewById(R.id.tv_detail_desc);
            tvStatus = findViewById(R.id.tv_detail_status);

            // 2. Inisialisasi View Komentar & Rating
            rbInputRating = findViewById(R.id.rb_input_rating);
            etCommentInput = findViewById(R.id.et_comment_input);
            btnSendComment = findViewById(R.id.btn_send_comment);
            rvComments = findViewById(R.id.rv_detail_comments);

            // Fungsi Tombol Kembali
            MaterialCardView btnBack = findViewById(R.id.btn_back_detail);
            btnBack.setOnClickListener(v -> finish());

            tokenManager = new TokenManager(this);
            placeId = getIntent().getStringExtra("PLACE_ID");

            if (placeId == null || placeId.isEmpty()) {
                Toast.makeText(this, "ID Tempat tidak ditemukan!", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // 3. Setup RecyclerView Komentar & Pasang Interface Report
            rvComments.setLayoutManager(new LinearLayoutManager(this));

            // ==========================================
            // PERBAIKAN: Masukkan ID User yang sedang login!
            // ==========================================
            String myUserId = tokenManager.getUserId();

            commentAdapter = new CommentAdapter(commentList, myUserId, new CommentAdapter.OnReportClickListener() {
                @Override
                public void onReportClick(String commentId, String reportedUserId, String username) {
                    showReportDialog(commentId, reportedUserId, username);
                }
            });
            rvComments.setAdapter(commentAdapter);

            // 4. Tarik Data dari Server
            fetchDetailTempat();
            fetchComments();

            // 5. Aksi Tombol Kirim Komentar
            btnSendComment.setOnClickListener(v -> submitComment());

        } catch (Exception e) {
            Toast.makeText(this, "CRASH TAMPILAN: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void fetchDetailTempat() {
        String token = tokenManager.getToken();
        ApiService apiService = ApiClient.getApiService(token);

        apiService.getDetailTempat("Bearer " + token, placeId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        Map<String, Object> dataObj = (Map<String, Object>) response.body().get("data");

                        if (dataObj != null) {
                            Object rawNama = dataObj.get("nama_usaha");
                            Object rawLokasi = dataObj.get("lokasi");
                            Object rawDeskripsi = dataObj.get("deskripsi");
                            Object rawIsOpen = dataObj.get("is_open");

                            tvName.setText(rawNama != null ? String.valueOf(rawNama) : "Nama Tidak Diketahui");
                            tvLocation.setText(rawLokasi != null ? String.valueOf(rawLokasi) : "Lokasi Tidak Diketahui");
                            tvDesc.setText(rawDeskripsi != null ? String.valueOf(rawDeskripsi) : "Tidak ada deskripsi disediakan.");

                            boolean isOpen = false;
                            if (rawIsOpen instanceof Boolean) {
                                isOpen = (Boolean) rawIsOpen;
                            } else if (rawIsOpen instanceof Number) {
                                isOpen = ((Number) rawIsOpen).intValue() == 1;
                            } else if (rawIsOpen instanceof String) {
                                isOpen = rawIsOpen.equals("true") || rawIsOpen.equals("1");
                            }

                            if (isOpen) {
                                tvStatus.setText("Status: BUKA");
                                tvStatus.setTextColor(Color.parseColor("#008B09"));
                            } else {
                                tvStatus.setText("Status: TUTUP");
                                tvStatus.setTextColor(Color.parseColor("#D32F2F"));
                            }
                        }
                    }
                } catch (Exception e) {
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

        apiService.getUmkmComments("Bearer " + token, placeId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Object dataObj = response.body().get("data");

                    if (dataObj instanceof List) {
                        commentList.clear();
                        commentList.addAll((List<Map<String, Object>>) dataObj);
                        commentAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
            }
        });
    }

     private void submitComment() {
        String commentText = etCommentInput.getText().toString().trim();
        // RatingBar mengembalikan float, kita kirimkan sebagai angka ke Laravel
        float ratingValue = rbInputRating.getRating();

        if (commentText.isEmpty()) {
            Toast.makeText(this, "Komentar tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = tokenManager.getToken();

        // Cek apakah token ada
        if (token == null) {
            Toast.makeText(this, "Token tidak ditemukan, silakan login ulang", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getApiService(token);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("content", commentText); // Key harus sesuai dengan Laravel
        requestBody.put("rating", ratingValue);  // Key harus sesuai dengan Laravel

        btnSendComment.setEnabled(false);

        apiService.postComment("Bearer " + token, placeId, requestBody).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                btnSendComment.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(DetailTempatActivity.this, "Komentar berhasil dikirim!", Toast.LENGTH_SHORT).show();
                    etCommentInput.setText(""); // Kosongkan input
                    rbInputRating.setRating(5.0f); // Reset bintang ke 5
                    fetchComments(); // Refresh daftar komentar
                } // Ganti bagian else di dalam onResponse dengan ini:
                else {
                try {
                    String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown Error";

                    // KIRIM KE LOGCAT (bisa lihat pesan panjang di sini)
                    android.util.Log.e("DEBUG_ERROR", "Error " + response.code() + ": " + errorBody);

                    // Cukup tampilkan kode errornya saja di Toast supaya tidak kepotong
                    Toast.makeText(DetailTempatActivity.this, "Gagal (Code " + response.code() + "). Cek Logcat!", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                btnSendComment.setEnabled(true);
                Toast.makeText(DetailTempatActivity.this, "Jaringan bermasalah: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showReportDialog(String commentId, String reportedUserId, String username) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_report, null);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();

        Spinner spinnerCategory = dialogView.findViewById(R.id.spinner_report_category);
        String[] categories = {"Spam", "Ujaran Kebencian", "Kata-kata Kasar / Pelecehan", "Penipuan", "Lainnya"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(spinnerAdapter);

        EditText etMessage = dialogView.findViewById(R.id.et_report_message);

        dialogView.findViewById(R.id.btn_cancel_report).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_submit_report).setOnClickListener(v -> {
            String category = spinnerCategory.getSelectedItem().toString();
            String message = etMessage.getText().toString().trim();

            if (message.isEmpty()) {
                Toast.makeText(this, "Alasan harus diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            submitReportToApi(commentId, reportedUserId, category, message, dialog);
        });

        dialog.show();
    }

    private void submitReportToApi(String commentId, String reportedUserId, String category, String message, android.app.AlertDialog dialog) {
        String token = tokenManager.getToken();
        ApiService apiService = ApiClient.getApiService(token);

        Map<String, Object> body = new HashMap<>();
        body.put("reported_user_id", String.valueOf(reportedUserId)); // Pastikan String
        body.put("comment_id", String.valueOf(commentId));             // Pastikan String
        body.put("report_category", category);
        body.put("report_message", message);

        apiService.postReport("Bearer " + token, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DetailTempatActivity.this, "Laporan terkirim!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    // Di sini akan terlihat error aslinya (misal: "report_status" field missing)
                    String error = "Code: " + response.code();
                    try { error = response.errorBody().string(); } catch (Exception e) {}
                    Toast.makeText(DetailTempatActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(DetailTempatActivity.this, "Jaringan Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}