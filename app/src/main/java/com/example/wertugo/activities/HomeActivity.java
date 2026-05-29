package com.example.wertugo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.wertugo.R;
import com.example.wertugo.utils.TokenManager;

public class HomeActivity extends AppCompatActivity {

    private TextView tvMessage;
    private Button btnLogout;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tokenManager = new TokenManager(this);
        tvMessage = findViewById(R.id.tv_message);
        btnLogout = findViewById(R.id.btn_logout);

        tvMessage.setText("Anda telah login");

        btnLogout.setOnClickListener(v -> {
            tokenManager.clearToken();
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
