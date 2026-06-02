package com.example.wertugo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.wertugo.R;

public class RegisterRoleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_role);

        Button btnTraveller = findViewById(R.id.btn_traveller);
        Button btnPemilikWisata = findViewById(R.id.btn_pemilik_wisata);
        TextView tvLoginLink = findViewById(R.id.tv_login_link);

        btnTraveller.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterRoleActivity.this, RegisterCredentialsActivity.class);
            startActivity(intent);
        });

        btnPemilikWisata.setOnClickListener(v -> {
            Toast.makeText(this, "Fitur Pemilik Wisata belum tersedia", Toast.LENGTH_SHORT).show();
        });

        btnPemilikWisata.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterRoleActivity.this, RegisterCredentialsActivity.class);
            intent.putExtra("role", "umkm"); // tambahkan extra role
            startActivity(intent);
        });

        btnTraveller.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterRoleActivity.this, RegisterCredentialsActivity.class);
            intent.putExtra("role", "user"); // tambahkan extra role
            startActivity(intent);
        });

        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterRoleActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
