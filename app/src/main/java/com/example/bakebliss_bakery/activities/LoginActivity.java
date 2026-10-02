package com.example.bakebliss_bakery.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bakebliss_bakery.MainActivity;
import com.example.bakebliss_bakery.R;
import com.example.bakebliss_bakery.activities.AdminActivity;
import com.example.bakebliss_bakery.database.DBHelper;
import com.example.bakebliss_bakery.utils.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    // ── Views ────────────────────────────────────────────────────────────────
    EditText etEmail, etPass;
    Button btnLogin;
    TextView tvRegister, tvForgotPassword;
    LinearLayout btnGoogleSignIn;
    ProgressBar progressBar;

    // ── Firebase / Auth ───────────────────────────────────────────────────────
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    SessionManager sessionManager;

    // ── Modern ActivityResultLauncher (replaces deprecated onActivityResult) ──
    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Task<GoogleSignInAccount> task =
                                GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null && account.getIdToken() != null) {
                                firebaseAuthWithGoogle(account.getIdToken());
                            } else {
                                hideLoading();
                                Toast.makeText(this,
                                        "Google Sign-In failed: No account data received.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (ApiException e) {
                            hideLoading();
                            String errorMsg = getGoogleSignInErrorMessage(e.getStatusCode());
                            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        mAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(getApplicationContext());

        // If a Firebase user is already signed in, skip login
        if (sessionManager.isLoggedIn()) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null && DBHelper.isAdminEmail(currentUser.getEmail())) {
                goToAdmin();
            } else {
                goToMain();
            }
            return;
        }

        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // ── Bind views ──────────────────────────────────────────────────────
        etEmail          = findViewById(R.id.etLoginUsername);
        etPass           = findViewById(R.id.etLoginPassword);
        btnLogin         = findViewById(R.id.btnLogin);
        tvRegister       = findViewById(R.id.tvRegisterLink);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnGoogleSignIn  = findViewById(R.id.btnGoogleSignIn);
        progressBar      = findViewById(R.id.progressBarLogin);

        // ── Configure Google Sign-In ────────────────────────────────────────
        setupGoogleSignIn();

        View btnBack = findViewById(R.id.btnBackLogin);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // ── Email / Password Login ──────────────────────────────────────────
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass  = etPass.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email is required");
                etEmail.requestFocus();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Enter a valid email address");
                etEmail.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(pass)) {
                etPass.setError("Password is required");
                etPass.requestFocus();
                return;
            }
            if (pass.length() < 6) {
                etPass.setError("Password must be at least 6 characters");
                etPass.requestFocus();
                return;
            }
            signInWithEmail(email, pass);
        });

        // ── Forgot Password ─────────────────────────────────────────────────
        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());

        // ── Navigate to Register ────────────────────────────────────────────
        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        // ── Window insets ───────────────────────────────────────────────────
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main_login_layout), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });
    }

    // =========================================================================
    //  Google Sign-In Setup
    // =========================================================================

    private void setupGoogleSignIn() {
        // Look up the web client ID
        int clientIdRes = getResources().getIdentifier(
                "default_web_client_id", "string", getPackageName());

        String webClientId = (clientIdRes != 0) ? getString(clientIdRes) : "";

        // Check if it's still the placeholder or missing entirely
        boolean isPlaceholder = webClientId.isEmpty() || webClientId.contains("REPLACE_WITH_YOUR_WEB_CLIENT_ID");

        if (isPlaceholder) {
            // Google Sign-In not properly configured — show setup instructions
            btnGoogleSignIn.setEnabled(true);
            btnGoogleSignIn.setAlpha(1.0f);
            btnGoogleSignIn.setOnClickListener(v ->
                    new AlertDialog.Builder(this)
                            .setTitle("⚙️ Google Sign-In Setup Required")
                            .setMessage(
                                    "To enable Google Sign-In, complete these steps:\n\n" +
                                    "Step 1 — Add SHA-1 to Firebase:\n" +
                                    "• Open Firebase Console → Project Settings → Your Apps\n" +
                                    "• Click 'Add fingerprint' and enter your SHA-1:\n" +
                                    "  31:49:DC:86:35:5F:AC:3F:2E:7F:E6:B1:53:17:90:C1:5E:F7:88:CF\n\n" +
                                    "Step 2 — Enable Google Sign-In:\n" +
                                    "• Firebase Console → Authentication → Sign-in method\n" +
                                    "• Enable 'Google'\n\n" +
                                    "Step 3 — Get Web Client ID:\n" +
                                    "• Authentication → Sign-in method → Google → Web SDK config\n" +
                                    "• Copy the Web Client ID\n\n" +
                                    "Step 4 — Paste into res/values/strings.xml:\n" +
                                    "• Replace the value of 'default_web_client_id'\n" +
                                    "  with the copied Web Client ID\n\n" +
                                    "Step 5 — Rebuild and run the app")
                            .setPositiveButton("Copy SHA-1", (d, w) -> {
                                android.content.ClipboardManager cb =
                                        (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                                if (cb != null) {
                                    cb.setPrimaryClip(android.content.ClipData.newPlainText("SHA-1",
                                            "31:49:DC:86:35:5F:AC:3F:2E:7F:E6:B1:53:17:90:C1:5E:F7:88:CF"));
                                    Toast.makeText(this, "SHA-1 copied!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Close", null)
                            .show());
            return;
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogleSignIn.setEnabled(true);
        btnGoogleSignIn.setAlpha(1.0f);
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
    }

    private void signInWithGoogle() {
        if (mGoogleSignInClient == null) {
            Toast.makeText(this,
                    "Google Sign-In is not available. Please check Firebase configuration.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        showLoading();
        // Force account picker every time so users can switch accounts
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    hideLoading();
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String name = (user != null && user.getDisplayName() != null)
                                ? user.getDisplayName() : "User";
                        String email = (user != null && user.getEmail() != null)
                                ? user.getEmail() : "";
                        sessionManager.createLoginSession(name);
                        Toast.makeText(this, "Welcome, " + name + "!", Toast.LENGTH_SHORT).show();
                        if (DBHelper.isAdminEmail(email)) {
                            goToAdmin();
                        } else {
                            goToMain();
                        }
                    } else {
                        String errMsg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Authentication failed";
                        Toast.makeText(this, "Google login failed: " + errMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // =========================================================================
    //  Email / Password Sign-In
    // =========================================================================

    private void signInWithEmail(String email, String password) {
        showLoading();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    hideLoading();
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String displayName = (user != null && user.getDisplayName() != null
                                && !user.getDisplayName().isEmpty())
                                ? user.getDisplayName()
                                : email.split("@")[0];
                        sessionManager.createLoginSession(displayName);
                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        if (DBHelper.isAdminEmail(email)) {
                            goToAdmin();
                        } else {
                            goToMain();
                        }
                    } else {
                        String errorMsg = getFriendlyAuthError(task.getException());
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // =========================================================================
    //  Forgot Password — Fixed Implementation
    // =========================================================================

    private void handleForgotPassword() {
        String currentEmail = etEmail.getText().toString().trim();

        final EditText input = new EditText(this);
        input.setHint("Enter your registered email");
        input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        if (!TextUtils.isEmpty(currentEmail)) {
            input.setText(currentEmail);
            input.setSelection(currentEmail.length());
        }

        int paddingPx = (int) (20 * getResources().getDisplayMetrics().density);
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = paddingPx;
        params.rightMargin = paddingPx;
        input.setLayoutParams(params);
        container.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("Enter your registered email address to receive a password reset link:")
                .setView(container)
                .setPositiveButton("Send Reset Link", (dialog, which) -> {
                    String email = input.getText().toString().trim();
                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    etEmail.setText(email);
                    sendPasswordReset(email);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendPasswordReset(String email) {
        showLoading();
        btnLogin.setEnabled(false);
        tvForgotPassword.setEnabled(false);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    hideLoading();
                    btnLogin.setEnabled(true);
                    tvForgotPassword.setEnabled(true);

                    if (task.isSuccessful()) {
                        // Show a clear success dialog with instructions
                        new AlertDialog.Builder(LoginActivity.this)
                                .setTitle("✅ Reset Email Sent")
                                .setMessage(
                                        "A password reset link has been sent to:\n\n" + email +
                                        "\n\n📌 Steps:\n" +
                                        "1. Open your email inbox\n" +
                                        "2. Check Spam/Junk folder if not found\n" +
                                        "3. Click the reset link\n" +
                                        "4. Set your new password\n" +
                                        "5. Return here and log in")
                                .setPositiveButton("OK", null)
                                .show();
                    } else {
                        // Show the exact Firebase error — common cause is email not registered
                        String errorMsg;
                        Exception ex = task.getException();
                        if (ex instanceof FirebaseAuthInvalidUserException) {
                            errorMsg = "No account found with this email address.\n\nPlease check the email or register a new account.";
                        } else if (ex != null) {
                            errorMsg = "Failed to send reset email.\n\nReason: " + ex.getMessage();
                        } else {
                            errorMsg = "Failed to send reset email. Please try again.";
                        }
                        new AlertDialog.Builder(LoginActivity.this)
                                .setTitle("❌ Reset Failed")
                                .setMessage(errorMsg)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                });
    }

    // =========================================================================
    //  Helpers
    // =========================================================================

    private void showLoading() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        btnGoogleSignIn.setEnabled(false);
    }

    private void hideLoading() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        btnLogin.setEnabled(true);
        btnGoogleSignIn.setEnabled(true);
    }

    private void goToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToAdmin() {
        Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Returns a user-friendly error message for Firebase Auth exceptions.
     */
    private String getFriendlyAuthError(Exception exception) {
        if (exception == null) return "Login failed. Please try again.";
        String msg = exception.getMessage();
        if (msg == null) return "Login failed. Please try again.";
        if (msg.contains("no user record") || msg.contains("user may have been deleted")) {
            return "No account found with this email. Please register first.";
        }
        if (msg.contains("password is invalid") || msg.contains("wrong-password")) {
            return "Incorrect password. Please try again.";
        }
        if (msg.contains("badly formatted") || msg.contains("invalid-email")) {
            return "Invalid email format. Please check and try again.";
        }
        if (msg.contains("too-many-requests") || msg.contains("too many")) {
            return "Too many failed attempts. Please wait a few minutes and try again.";
        }
        if (msg.contains("network") || msg.contains("NETWORK_ERROR")) {
            return "Network error. Please check your internet connection.";
        }
        return "Login failed: " + msg;
    }

    /**
     * Returns a user-friendly error message for Google Sign-In status codes.
     * Status code reference: https://developers.google.com/android/reference/com/google/android/gms/common/api/CommonStatusCodes
     */
    private String getGoogleSignInErrorMessage(int statusCode) {
        switch (statusCode) {
            case 7:   return "Network error. Please check your internet connection and try again.";
            case 10:  return "Google Sign-In configuration error (code 10).\n\nFix: Add your SHA-1 fingerprint in Firebase Console → Project Settings → Your App → SHA certificate fingerprints.";
            case 12501: return "Google Sign-In was cancelled.";
            case 12502: return "Google Sign-In is currently in progress. Please wait.";
            default:  return "Google Sign-In failed (code " + statusCode + "). Please try again.";
        }
    }
}
