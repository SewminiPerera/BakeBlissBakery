package com.example.bakebliss_bakery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bakebliss_bakery.MainActivity;
import com.example.bakebliss_bakery.R;
import com.example.bakebliss_bakery.utils.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;

    EditText etEmail, etPass;
    Button btnLogin;
    TextView tvRegister, tvForgotPassword;
    LinearLayout btnGoogleSignIn;

    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        mAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(getApplicationContext());

        // If already logged in, go straight to MainActivity
        if (sessionManager.isLoggedIn()) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Configure Google Sign-In
        int defaultClientIdRes = getResources().getIdentifier("default_web_client_id", "string", getPackageName());
        String webClientId = defaultClientIdRes != 0 ? getString(defaultClientIdRes) : "";

        GoogleSignInOptions.Builder gsoBuilder = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail();
        if (!TextUtils.isEmpty(webClientId)) {
            gsoBuilder.requestIdToken(webClientId);
        }
        mGoogleSignInClient = GoogleSignIn.getClient(this, gsoBuilder.build());

        // Bind views
        etEmail  = findViewById(R.id.etLoginUsername);
        etPass   = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister      = findViewById(R.id.tvRegisterLink);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnGoogleSignIn  = findViewById(R.id.btnGoogleSignIn);

        // Google Sign-In click
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

        // Email / Password login
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass  = etPass.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
            } else {
                signInWithEmail(email, pass);
            }
        });

        // Forgot password
        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Enter your email above to reset password", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Reset link sent to " + email, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Navigate to Register
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_login_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // ─── Email / Password Sign-In ───────────────────────────────────────────
    private void signInWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        sessionManager.createLoginSession(user != null && user.getDisplayName() != null
                                ? user.getDisplayName()
                                : email.split("@")[0]);
                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        goToMain();
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ─── Google Sign-In Flow ────────────────────────────────────────────────
    private void signInWithGoogle() {
        int defaultClientIdRes = getResources().getIdentifier("default_web_client_id", "string", getPackageName());
        if (defaultClientIdRes == 0) {
            Toast.makeText(this, "Google Sign-In is not configured yet. Please enable Google in Firebase Console and update google-services.json.", Toast.LENGTH_LONG).show();
            return;
        }
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String name = (user != null && user.getDisplayName() != null)
                                ? user.getDisplayName()
                                : "User";
                        sessionManager.createLoginSession(name);
                        Toast.makeText(this, "Welcome, " + name + "!", Toast.LENGTH_SHORT).show();
                        goToMain();
                    } else {
                        Toast.makeText(this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ─── Helper ─────────────────────────────────────────────────────────────
    private void goToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
