package com.example.wertugo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.wertugo.R;
import com.example.wertugo.api.ApiService;
import com.example.wertugo.api.ApiClient;
import com.example.wertugo.api.models.LoginRequest;
import com.example.wertugo.api.models.LoginResponse;
import com.example.wertugo.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tokenManager = new TokenManager(this);
        // Jika sudah login, langsung ke Home
        if (tokenManager.isLoggedIn()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        TextView tvRegisterLink = findViewById(R.id.tv_register_link);

        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterRoleActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            LoginRequest request = new LoginRequest(email, password);
            ApiService api = ApiClient.getApiService();
            Call<LoginResponse> call = api.login(request);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String token = response.body().getAccessToken();
                        tokenManager.saveToken(token);
                        Toast.makeText(LoginActivity.this, "Login berhasil", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Login gagal: email atau password salah", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
