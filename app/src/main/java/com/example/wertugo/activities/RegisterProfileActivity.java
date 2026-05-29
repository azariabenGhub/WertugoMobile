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
import com.example.wertugo.api.models.RegisterRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterProfileActivity extends AppCompatActivity {

    private EditText etUsername, etCountry;
    private Button btnLanjutkan;
    private String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_profile);

        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");

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

            RegisterRequest request = new RegisterRequest(email, username, password, "user", country);
            ApiService api = ApiClient.getApiService();
            Call<ApiResponse> call = api.register(request);
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(RegisterProfileActivity.this, "Registrasi berhasil, silakan login", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterProfileActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterProfileActivity.this, "Registrasi gagal: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(RegisterProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
