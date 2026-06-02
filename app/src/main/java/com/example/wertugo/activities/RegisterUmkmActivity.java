package com.example.wertugo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wertugo.R;
import com.example.wertugo.api.ApiClient;
import com.example.wertugo.api.ApiService;
import com.example.wertugo.utils.TokenManager;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterUmkmActivity extends AppCompatActivity {

    private EditText etNamaTempat, etLokasi, etDeskripsi, etTelepon;
    private AutoCompleteTextView etCountry, etJenis;
    private MaterialButton btnDaftar;

    private TokenManager tokenManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_umkm);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getApiService();

        initViews();
        setupDropdowns();

        btnDaftar.setOnClickListener(v -> registerUmkm());
    }

    private void initViews() {
        etNamaTempat = findViewById(R.id.et_nama_tempat);
        etLokasi = findViewById(R.id.et_lokasi);
        etDeskripsi = findViewById(R.id.et_deskripsi);
        etCountry = findViewById(R.id.et_country);
        etJenis = findViewById(R.id.et_jenis);
        etTelepon = findViewById(R.id.et_telepon);
        btnDaftar = findViewById(R.id.btn_lanjutkan);
    }

    private void setupDropdowns() {
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.country_list));
        etCountry.setAdapter(countryAdapter);
        etCountry.setOnClickListener(v -> etCountry.showDropDown());

        ArrayAdapter<String> jenisAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.jenis_usaha_list));
        etJenis.setAdapter(jenisAdapter);
        etJenis.setOnClickListener(v -> etJenis.showDropDown());
    }

    private void registerUmkm() {
        String namaTempat = etNamaTempat.getText().toString().trim();
        String lokasi = etLokasi.getText().toString().trim();
        String deskripsi = etDeskripsi.getText().toString().trim();

        if (namaTempat.isEmpty()) { etNamaTempat.setError("Wajib diisi"); return; }
        if (lokasi.isEmpty()) { etLokasi.setError("Wajib diisi"); return; }
        if (deskripsi.isEmpty()) { etDeskripsi.setError("Wajib diisi"); return; }

        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Sesi habis, silakan login ulang", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Map<String, String> fields = new HashMap<>();
        fields.put("nama_usaha", namaTempat);
        fields.put("lokasi", lokasi);
        fields.put("deskripsi", deskripsi);

        Call<Map<String, Object>> call = apiService.registerUmkm("Bearer " + token, fields);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Map<String, Object> responseBody = response.body();
                    if (responseBody != null && responseBody.containsKey("data")) {
                        Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                        String umkmId = (String) data.get("_id"); // atau "id" tergantung response
                        Intent intent = new Intent(RegisterUmkmActivity.this, RegisterLegalitasActivity.class);
                        intent.putExtra("umkm_id", umkmId);
                        startActivity(intent);
                    } else {
                        // fallback jika tidak ada id
                        startActivity(new Intent(RegisterUmkmActivity.this, RegisterLegalitasActivity.class));
                    }
                    finish();
                } else {
                    parseError(response);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(RegisterUmkmActivity.this, "Gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void parseError(Response<?> response) {
        try {
            String errorBody = response.errorBody().string();
            JSONObject json = new JSONObject(errorBody);
            String msg = json.optString("message", "Terjadi kesalahan");
            if (json.has("errors")) {
                JSONObject errors = json.getJSONObject("errors");
                msg = errors.toString();
            }
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
        }
    }
}