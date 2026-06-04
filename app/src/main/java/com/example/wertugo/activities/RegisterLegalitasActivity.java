package com.example.wertugo.activities;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wertugo.R;
import com.example.wertugo.api.ApiClient;
import com.example.wertugo.api.ApiService;
import com.example.wertugo.utils.TokenManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterLegalitasActivity extends AppCompatActivity {

    private static final int REQUEST_NIB = 1;
    private static final int REQUEST_NPWP = 2;
    private static final int REQUEST_BANGUNAN = 3;
    private static final int REQUEST_HALAL = 4;
    private static final int REQUEST_PARIWISATA = 5;

    // View Components: Cards
    private MaterialCardView cardNib, cardNpwp, cardBangunan, cardHalal, cardPariwisata;

    // View Components: TextViews
    private TextView tvNib, tvNpwp, tvBangunan, tvHalal, tvPariwisata;

    // View Components: Icons
    private ImageView iconNib, iconNpwp, iconBangunan, iconHalal, iconPariwisata;

    private MaterialButton btnSubmit, btnSkip;
    private TokenManager tokenManager;
    private ApiService apiService;

    // URIs untuk menyimpan file sementara
    private Uri nibUri, npwpUri, bangunanUri, halalUri, pariwisataUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_legalitas);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getApiService(tokenManager.getToken());

        // Inisialisasi TextView
        tvNib = findViewById(R.id.tv_nib_filename);
        tvNpwp = findViewById(R.id.tv_npwp_filename);
        tvBangunan = findViewById(R.id.tv_bangunan_filename);
        tvHalal = findViewById(R.id.tv_halal_filename);
        tvPariwisata = findViewById(R.id.tv_pariwisata_filename);

        // Inisialisasi Cards
        cardNib = findViewById(R.id.card_nib);
        cardNpwp = findViewById(R.id.card_npwp);
        cardBangunan = findViewById(R.id.card_bangunan);
        cardHalal = findViewById(R.id.card_halal);
        cardPariwisata = findViewById(R.id.card_pariwisata);

        // Inisialisasi Icons
        iconNib = findViewById(R.id.icon_nib);
        iconNpwp = findViewById(R.id.icon_npwp);
        iconBangunan = findViewById(R.id.icon_bangunan);
        iconHalal = findViewById(R.id.icon_halal);
        iconPariwisata = findViewById(R.id.icon_pariwisata);

        btnSubmit = findViewById(R.id.btn_submit);
        btnSkip = findViewById(R.id.btn_skip);

        // Pasang klik listener ke Card (bukan ke button terpisah)
        cardNib.setOnClickListener(v -> openFilePicker(REQUEST_NIB));
        cardNpwp.setOnClickListener(v -> openFilePicker(REQUEST_NPWP));
        cardBangunan.setOnClickListener(v -> openFilePicker(REQUEST_BANGUNAN));
        cardHalal.setOnClickListener(v -> openFilePicker(REQUEST_HALAL));
        cardPariwisata.setOnClickListener(v -> openFilePicker(REQUEST_PARIWISATA));

        btnSubmit.setOnClickListener(v -> uploadDocuments());

        btnSkip.setOnClickListener(v -> {
            startActivity(new Intent(RegisterLegalitasActivity.this, UmkmHomeActivity.class));
            finish();
        });

        checkExistingDocuments();
    }

    private void checkExistingDocuments() {
        apiService.checkVerificationStatus("Bearer " + tokenManager.getToken())
            .enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Map<String, Object> body = response.body();
                        if (body.containsKey("uploaded_docs")) {
                            Map<String, Object> docs = (Map<String, Object>) body.get("uploaded_docs");
                            if (docs != null) {
                                if (isDocUploaded(docs.get("nib"))) updateCardToSuccess(cardNib, tvNib, iconNib, "Telah Diunggah");
                                if (isDocUploaded(docs.get("npwp"))) updateCardToSuccess(cardNpwp, tvNpwp, iconNpwp, "Telah Diunggah");
                                if (isDocUploaded(docs.get("bangunan"))) updateCardToSuccess(cardBangunan, tvBangunan, iconBangunan, "Telah Diunggah");
                                if (isDocUploaded(docs.get("halal"))) updateCardToSuccess(cardHalal, tvHalal, iconHalal, "Telah Diunggah");
                                if (isDocUploaded(docs.get("pariwisata"))) updateCardToSuccess(cardPariwisata, tvPariwisata, iconPariwisata, "Telah Diunggah");
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
            });
    }

    private boolean isDocUploaded(Object val) {
        return val != null && val.toString().equals("true");
    }

    private void openFilePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // Bisa disesuaikan jadi "image/*" jika hanya menerima gambar
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Pilih file"), requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            String fileName = getFileName(uri);

            switch (requestCode) {
                case REQUEST_NIB:
                    nibUri = uri;
                    updateCardToSuccess(cardNib, tvNib, iconNib, fileName);
                    break;
                case REQUEST_NPWP:
                    npwpUri = uri;
                    updateCardToSuccess(cardNpwp, tvNpwp, iconNpwp, fileName);
                    break;
                case REQUEST_BANGUNAN:
                    bangunanUri = uri;
                    updateCardToSuccess(cardBangunan, tvBangunan, iconBangunan, fileName);
                    break;
                case REQUEST_HALAL:
                    halalUri = uri;
                    updateCardToSuccess(cardHalal, tvHalal, iconHalal, fileName);
                    break;
                case REQUEST_PARIWISATA:
                    pariwisataUri = uri;
                    updateCardToSuccess(cardPariwisata, tvPariwisata, iconPariwisata, fileName);
                    break;
            }
        }
    }

    // ==========================================
    // FUNGSI ANIMASI: UBAH KARTU JADI HIJAU
    // ==========================================
    private void updateCardToSuccess(MaterialCardView card, TextView tv, ImageView icon, String fileName) {
        // Ubah Background jadi Hijau
        card.setCardBackgroundColor(Color.parseColor("#008B09"));
        // Hilangkan garis pinggir
        card.setStrokeWidth(0);
        // Ubah teks dan warna teks
        tv.setText(fileName);
        tv.setTextColor(Color.WHITE);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        // Ubah ikon jadi centang
        icon.setImageResource(android.R.drawable.checkbox_on_background);
        icon.setColorFilter(Color.WHITE);
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result != null ? result : "Dokumen Terpilih";
    }

    private void uploadDocuments() {
        if (nibUri == null && npwpUri == null && bangunanUri == null && halalUri == null && pariwisataUri == null) {
            Toast.makeText(this, "Pilih setidaknya 1 dokumen untuk diunggah", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Sesi habis", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Mengunggah...");

        // File yang kosong (null) akan otomatis diabaikan karena logika if(fileUri == null) return null;
        java.util.List<MultipartBody.Part> parts = new java.util.ArrayList<>();
        
        MultipartBody.Part nibPart = createFilePart("nib_dokumen", nibUri);
        if (nibPart != null) parts.add(nibPart);
        
        MultipartBody.Part npwpPart = createFilePart("npwp_dokumen", npwpUri);
        if (npwpPart != null) parts.add(npwpPart);
        
        MultipartBody.Part bangunanPart = createFilePart("legalitas_bangunan_dokumen", bangunanUri);
        if (bangunanPart != null) parts.add(bangunanPart);
        
        MultipartBody.Part halalPart = createFilePart("sertifikat_halal_dokumen", halalUri);
        if (halalPart != null) parts.add(halalPart);
        
        MultipartBody.Part pariwisataPart = createFilePart("sertifikat_usaha_pariwisata_dokumen", pariwisataUri);
        if (pariwisataPart != null) parts.add(pariwisataPart);

        Call<Map<String, Object>> call = apiService.uploadLegalitasDocuments(
                "Bearer " + token,
                parts
        );

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterLegalitasActivity.this, "Dokumen berhasil disimpan!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(RegisterLegalitasActivity.this, UmkmHomeActivity.class));
                    finish();
                } else {
                    Toast.makeText(RegisterLegalitasActivity.this, "Gagal mengunggah: " + response.code(), Toast.LENGTH_SHORT).show();
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Simpan Dokumen");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(RegisterLegalitasActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Simpan Dokumen");
            }
        });
    }

    private MultipartBody.Part createFilePart(String fieldName, Uri fileUri) {
        if (fileUri == null) return null; // Kunci agar dokumen opsional/yang belum ada tidak menyebabkan crash
        try {
            // Ambil tipe MIME otomatis (agar backend tidak menolak file)
            String mimeType = getContentResolver().getType(fileUri);
            if (mimeType == null) mimeType = "application/octet-stream";

            String extension = ".tmp";
            if (mimeType.contains("pdf")) extension = ".pdf";
            else if (mimeType.contains("jpg") || mimeType.contains("jpeg")) extension = ".jpg";
            else if (mimeType.contains("png")) extension = ".png";

            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            File tempFile = File.createTempFile("doc_", extension, getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), tempFile);
            return MultipartBody.Part.createFormData(fieldName, tempFile.getName(), requestFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}