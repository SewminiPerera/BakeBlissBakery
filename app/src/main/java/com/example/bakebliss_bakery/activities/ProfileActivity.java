package com.example.bakebliss_bakery.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bakebliss_bakery.MainActivity;
import com.example.bakebliss_bakery.R;
import com.example.bakebliss_bakery.database.DBHelper;
import com.example.bakebliss_bakery.utils.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class ProfileActivity extends AppCompatActivity {

    TextView tvProfileName, tvShowEmail, tvShowPhone, tvShowAddress;
    ImageView imgProfilePic;
    Button btnLogout, btnEditProfile;
    DBHelper dbHelper;
    SessionManager sessionManager;
    GoogleSignInClient mGoogleSignInClient;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);

        // Init Google Sign-In client for proper sign-out
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        tvProfileName = findViewById(R.id.tvProfileName);
        tvShowEmail = findViewById(R.id.tvShowEmail);
        tvShowPhone = findViewById(R.id.tvShowPhone);
        tvShowAddress = findViewById(R.id.tvShowAddress);
        imgProfilePic = findViewById(R.id.imgProfilePic);
        btnLogout = findViewById(R.id.btnLogout);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        String username = sessionManager.getUsername();

        loadUserProfile(username);

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(ProfileActivity.this)
                    .setTitle("Log Out")
                    .setMessage("Are you sure want to Log Out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Sign out from Firebase + Google
                        sessionManager.logoutUser();
                        mGoogleSignInClient.signOut();

                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_cart) {
                    Intent intent = new Intent(ProfileActivity.this, CartActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (id == R.id.nav_orders) {
                    Intent intent = new Intent(ProfileActivity.this, MyOrderActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (id == R.id.nav_profile) {
                    return true;
                }
                return false;
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_profile_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            bottomNavigationView.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile(sessionManager.getUsername());
    }

    private void loadUserProfile(String username) {
        Cursor cursor = dbHelper.getUserDetails(username);

        if (cursor != null && cursor.moveToFirst()) {
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));

            int addressIndex = cursor.getColumnIndex("address");
            String address = (addressIndex != -1) ? cursor.getString(addressIndex) : "Not Set";

            int imgIndex = cursor.getColumnIndex("profile_image");
            String imgPath = (imgIndex != -1) ? cursor.getString(imgIndex) : "";

            tvProfileName.setText(username);
            tvShowEmail.setText(email);
            tvShowPhone.setText(phone);
            tvShowAddress.setText(address);

            if(imgPath != null && !imgPath.isEmpty()){
                try {
                    imgProfilePic.setImageURI(Uri.parse(imgPath));
                } catch (Exception e) {
                    imgProfilePic.setImageResource(R.mipmap.ic_launcher);
                }
            } else {
                imgProfilePic.setImageResource(R.mipmap.ic_launcher);
            }
        } else {
            Toast.makeText(this, "Error: No details found", Toast.LENGTH_SHORT).show();
        }

        if (cursor != null) {
            cursor.close();
        }
    }
}
