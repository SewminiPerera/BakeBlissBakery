package com.example.bakebliss_bakery.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bakebliss_bakery.R;
import com.example.bakebliss_bakery.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    EditText etUsername, etEmail, etPhone, etPassword, etConfirmPass;
    Button btnRegister;
    TextView tvLoginLink;

    FirebaseAuth mAuth;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(getApplicationContext());

        etUsername   = findViewById(R.id.etUsername);
        etEmail      = findViewById(R.id.etEmail);
        etPhone      = findViewById(R.id.etPhone);
        etPassword   = findViewById(R.id.etPassword);
        etConfirmPass = findViewById(R.id.etConfirmPassword);
        btnRegister  = findViewById(R.id.btnRegister);
        tvLoginLink  = findViewById(R.id.tvLoginLink);

        View btnBack = findViewById(R.id.btnBackRegister);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnRegister.setOnClickListener(view -> {
            String username     = etUsername.getText().toString().trim();
            String email        = etEmail.getText().toString().trim();
            String phone        = etPhone.getText().toString().trim();
            String pass         = etPassword.getText().toString().trim();
            String confirmPass  = etConfirmPass.getText().toString().trim();

            // ── Validation ──────────────────────────────────────────────────
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email)
                    || TextUtils.isEmpty(pass) || TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Please enter a valid email address");
                etEmail.requestFocus();
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            } else if (phone.length() != 10 || !phone.matches("^\\d{10}$")) {
                etPhone.setError("10 digits must be entered");
                etPhone.requestFocus();
                Toast.makeText(this, "10 digits must be entered", Toast.LENGTH_SHORT).show();
            } else if (pass.length() < 8) {
                etPassword.setError("Password must be at least 8 characters");
                etPassword.requestFocus();
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            } else if (!pass.equals(confirmPass)) {
                etConfirmPass.setError("Passwords do not match");
                etConfirmPass.requestFocus();
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                registerWithFirebase(username, email, phone, pass);
            }
        });

        tvLoginLink.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_register_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void registerWithFirebase(String username, String email, String phone, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Set display name to the chosen username
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                .build();

                        if (mAuth.getCurrentUser() != null) {
                            mAuth.getCurrentUser().updateProfile(profileUpdates);
                        }

                        // Save user profile details to Firestore
                        com.example.bakebliss_bakery.database.DBHelper dbHelper =
                                new com.example.bakebliss_bakery.database.DBHelper(RegisterActivity.this);
                        dbHelper.saveUserProfile(username, email, phone, "Not Set", "", success -> {
                            sessionManager.createLoginSession(username);
                            Toast.makeText(this, "Registration Successful! Please log in.", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                            finish();
                        });
                    } else {
                        String errorMsg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration failed";
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
