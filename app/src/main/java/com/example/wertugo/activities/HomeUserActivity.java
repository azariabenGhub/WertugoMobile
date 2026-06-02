package com.example.wertugo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.wertugo.R;

public class HomeUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inisialisasi navbar
        ImageView navHome = findViewById(R.id.nav_home);
        ImageView navSearch = findViewById(R.id.nav_search);
        ImageView navMessage = findViewById(R.id.nav_message);
        ImageView navProfile = findViewById(R.id.nav_profile);

        // Home aktif (tidak perlu aksi)
        navHome.setOnClickListener(v -> {
            // sudah berada di halaman home
        });

        navSearch.setOnClickListener(v -> {
            Toast.makeText(this, "Fitur pencarian belum tersedia", Toast.LENGTH_SHORT).show();
        });

        navMessage.setOnClickListener(v -> {
            Toast.makeText(this, "Fitur pesan belum tersedia", Toast.LENGTH_SHORT).show();
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeUserActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }
}