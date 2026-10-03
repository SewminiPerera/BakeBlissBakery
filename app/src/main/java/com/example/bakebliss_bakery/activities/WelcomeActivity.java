package com.example.bakebliss_bakery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bakebliss_bakery.MainActivity;
import com.example.bakebliss_bakery.R;
import com.example.bakebliss_bakery.database.DBHelper;
import com.example.bakebliss_bakery.utils.SessionManager;

public class WelcomeActivity extends AppCompatActivity {

    SessionManager sessionManager;
    Button btnGetStarted;
    TextView tvWelcomeSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        sessionManager = new SessionManager(this);

        btnGetStarted = findViewById(R.id.btnGetStarted);
        tvWelcomeSubtitle = findViewById(R.id.tvWelcomeSubtitle);

        if (sessionManager.isLoggedIn()) {
            String username = sessionManager.getUsername();
            if (username != null && !username.trim().isEmpty()) {
                tvWelcomeSubtitle.setText("Welcome back, " + username + "!\nYour Ultimate Bakery for Every Craving.\nFresh Bakes, Anytime.");
            }
        }

        btnGetStarted.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                String email = sessionManager.getUserEmail();
                if (DBHelper.isAdminEmail(email)) {
                    Intent intent = new Intent(WelcomeActivity.this, AdminActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            } else {
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
            }
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_welcome_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
