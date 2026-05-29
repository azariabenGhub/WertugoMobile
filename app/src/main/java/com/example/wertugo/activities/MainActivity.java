package com.example.wertugo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.wertugo.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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