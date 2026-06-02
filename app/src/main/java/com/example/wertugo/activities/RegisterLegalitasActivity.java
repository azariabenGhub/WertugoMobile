package com.example.wertugo.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wertugo.R;
import com.example.wertugo.api.ApiClient;
import com.example.wertugo.api.ApiService;
import com.example.wertugo.utils.TokenManager;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
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

    private TextView tvNib, tvNpwp, tvBangunan, tvHalal, tvPariwisata;
    private MaterialButton btnSubmit;
    private TokenManager tokenManager;
    private ApiService apiService;

    private Uri nibUri, npwpUri, bangunanUri, halalUri, pariwisataUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_legalitas);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getApiService();

        tvNib = findViewById(R.id.tv_nib_filename);
        tvNpwp = findViewById(R.id.tv_npwp_filename);
        tvBangunan = findViewById(R.id.tv_bangunan_filename);
        tvHalal = findViewById(R.id.tv_halal_filename);
        tvPariwisata = findViewById(R.id.tv_pariwisata_filename);
        btnSubmit = findViewById(R.id.btn_submit);

        findViewById(R.id.btn_upload_nib).setOnClickListener(v -> openFilePicker(REQUEST_NIB));
        findViewById(R.id.btn_upload_npwp).setOnClickListener(v -> openFilePicker(REQUEST_NPWP));
        findViewById(R.id.btn_upload_bangunan).setOnClickListener(v -> openFilePicker(REQUEST_BANGUNAN));
        findViewById(R.id.btn_upload_halal).setOnClickListener(v -> openFilePicker(REQUEST_HALAL));
        findViewById(R.id.btn_upload_pariwisata).setOnClickListener(v -> openFilePicker(REQUEST_PARIWISATA));

        btnSubmit.setOnClickListener(v -> uploadDocuments());
    }

    private void openFilePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
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
                    tvNib.setText(fileName);
                    break;
                case REQUEST_NPWP:
                    npwpUri = uri;
                    tvNpwp.setText(fileName);
                    break;
                case REQUEST_BANGUNAN:
                    bangunanUri = uri;
                    tvBangunan.setText(fileName);
                    break;
                case REQUEST_HALAL:
                    halalUri = uri;
                    tvHalal.setText(fileName);
                    break;
                case REQUEST_PARIWISATA:
                    pariwisataUri = uri;
                    tvPariwisata.setText(fileName);
                    break;
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(nameIndex);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }

    private void uploadDocuments() {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Sesi habis", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Siapkan MultipartBody
        MultipartBody.Part nibPart = createFilePart("nib_dokumen", nibUri);
        MultipartBody.Part npwpPart = createFilePart("npwp_dokumen", npwpUri);
        MultipartBody.Part bangunanPart = createFilePart("legalitas_bangunan_dokumen", bangunanUri);
        MultipartBody.Part halalPart = createFilePart("sertifikat_halal_dokumen", halalUri);
        MultipartBody.Part pariwisataPart = createFilePart("sertifikat_usaha_pariwisata_dokumen", pariwisataUri);

        // Kirim request
        Call<Map<String, Object>> call = apiService.uploadLegalitasDocuments(
                "Bearer " + token,
                nibPart, npwpPart, bangunanPart, halalPart, pariwisataPart
        );
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterLegalitasActivity.this, "Dokumen berhasil dikirim. Menunggu verifikasi admin.", Toast.LENGTH_LONG).show();
                    // Arahkan ke halaman UMKM Home
                    startActivity(new Intent(RegisterLegalitasActivity.this, UmkmHomeActivity.class));
                    finish();
                } else {
                    Toast.makeText(RegisterLegalitasActivity.this, "Gagal: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(RegisterLegalitasActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private MultipartBody.Part createFilePart(String fieldName, Uri fileUri) {
        if (fileUri == null) return null;
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            File tempFile = File.createTempFile("upload_", ".tmp", getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse("application/octet-stream"), tempFile);
            return MultipartBody.Part.createFormData(fieldName, tempFile.getName(), requestFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}