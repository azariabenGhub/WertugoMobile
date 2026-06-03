package com.example.wertugo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.wertugo.R;
import com.example.wertugo.utils.TokenManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ==========================================
        // 1. CEK STATUS LOGIN & ROLE SEBELUM MENGGAMBAR LAYAR
        // ==========================================
        TokenManager tokenManager = new TokenManager(this);
        if (tokenManager.isLoggedIn()) {
            String role = tokenManager.getRole();

            Intent intent;
            if ("umkm".equalsIgnoreCase(role)) {
                // Jika UMKM, lempar ke Beranda UMKM
                intent = new Intent(MainActivity.this, UmkmHomeActivity.class);
            } else {
                // Jika User biasa, lempar ke Beranda User
                intent = new Intent(MainActivity.this, HomeUserActivity.class);
            }

            startActivity(intent);
            finish();
            return; // Hentikan proses agar layar Welcome tidak perlu di-load
        }

        // ==========================================
        // 2. JIKA BELUM LOGIN, BARU TAMPILKAN HALAMAN WELCOME/MAIN
        // ==========================================
        setContentView(R.layout.activity_main);

        Button btnDaftar = findViewById(R.id.btn_daftar);
        TextView tvLoginLink = findViewById(R.id.tv_login_link);

        btnDaftar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterRoleActivity.class);
            startActivity(intent);
        });

        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}