package com.example.wertugo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.wertugo.R;
import com.example.wertugo.api.ApiService;
import com.example.wertugo.api.ApiClient;
import com.example.wertugo.api.models.ApiResponse;
import com.example.wertugo.api.models.LoginRequest;
import com.example.wertugo.api.models.LoginResponse;
import com.example.wertugo.api.models.RegisterRequest;
import com.example.wertugo.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterProfileActivity extends AppCompatActivity {

    private EditText etUsername, etCountry;
    private Button btnLanjutkan;
    private String email, password, role;
    private TokenManager tokenManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_profile);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getApiService(); // Pastikan tidak butuh parameter token

        // Ambil data dari Intent sebelumnya
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");
        role = getIntent().getStringExtra("role");

        etUsername = findViewById(R.id.et_username);
        etCountry = findViewById(R.id.et_country);
        btnLanjutkan = findViewById(R.id.btn_lanjutkan);

        btnLanjutkan.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String country = etCountry.getText().toString().trim();

            if (username.isEmpty() || country.isEmpty()) {
                Toast.makeText(this, "Nama dan negara harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            btnLanjutkan.setEnabled(false); // Cegah double klik saat loading

            // Menggunakan RegisterRequest yang sudah otomatis mengisi account_status = "active"
            RegisterRequest request = new RegisterRequest(email, username, password, role, country);

            Call<ApiResponse> call = apiService.register(request);

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(RegisterProfileActivity.this, "Registrasi berhasil", Toast.LENGTH_SHORT).show();
                        loginAutomatically(email, password);
                    } else {
                        btnLanjutkan.setEnabled(true);
                        Toast.makeText(RegisterProfileActivity.this, "Registrasi gagal: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    btnLanjutkan.setEnabled(true);
                    Toast.makeText(RegisterProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void loginAutomatically(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);
        Call<LoginResponse> loginCall = apiService.login(loginRequest);

        loginCall.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getAccessToken();
                    tokenManager.saveToken(token, role);

                    String userRole = response.body().getUser().getRole();
                    if ("umkm".equals(userRole)) {
                        startActivity(new Intent(RegisterProfileActivity.this, RegisterUmkmActivity.class));
                    } else {
                        startActivity(new Intent(RegisterProfileActivity.this, HomeUserActivity.class));
                    }
                    finish();
                } else {
                    btnLanjutkan.setEnabled(true);
                    Toast.makeText(RegisterProfileActivity.this, "Auto login gagal, silakan login manual", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterProfileActivity.this, LoginActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnLanjutkan.setEnabled(true);
                Toast.makeText(RegisterProfileActivity.this, "Auto login error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterProfileActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}