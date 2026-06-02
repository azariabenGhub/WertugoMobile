package com.example.wertugo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.wertugo.R;

public class RegisterCredentialsActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword;
    private Button btnDaftar;
    private String role; // <-- tambahkan variabel untuk menyimpan role

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_credentials);

        // Terima role dari intent sebelumnya (RegisterRoleActivity)
        role = getIntent().getStringExtra("role");
        if (role == null) {
            role = "user"; // default jika tidak dikirim (misal dari tombol Traveller)
        }

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnDaftar = findViewById(R.id.btn_daftar);
        TextView tvLoginLink = findViewById(R.id.tv_login_link);

        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterCredentialsActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnDaftar.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirm)) {
                Toast.makeText(this, "Password dan konfirmasi password tidak sama", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 8) {
                Toast.makeText(this, "Password minimal 8 karakter", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(RegisterCredentialsActivity.this, RegisterProfileActivity.class);
            intent.putExtra("email", email);
            intent.putExtra("password", password);
            intent.putExtra("role", role); // <-- kirim role ke RegisterProfileActivity
            startActivity(intent);
        });
    }
}