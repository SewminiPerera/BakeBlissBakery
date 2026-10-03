package com.example.bakebliss_bakery.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bakebliss_bakery.R;
import com.example.bakebliss_bakery.database.DBHelper;
import com.example.bakebliss_bakery.utils.SessionManager;

public class EditProfileActivity extends AppCompatActivity {

    FrameLayout frameProfilePic;
    ImageView imgEditProfile;
    EditText etEditName, etEditEmail, etEditPhone, etEditAddress;
    Button btnSaveProfile;

    DBHelper dbHelper;
    SessionManager sessionManager;

    String currentUsername;
    String selectedImageUriString = "";

    // URI for camera photo (needs to be stored before capturing)
    Uri cameraImageUri;

    // ── Modern Photo Picker (Android 11+ / backport on older) ──
    ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    // Grant persistent read permission
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception ignored) {}
                    selectedImageUriString = uri.toString();
                    imgEditProfile.setImageURI(uri);
                }
            });

    // ── Camera capture launcher ──
    ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraImageUri != null) {
                    selectedImageUriString = cameraImageUri.toString();
                    imgEditProfile.setImageURI(cameraImageUri);
                }
            });

    // ── Fallback gallery picker (for devices where PickVisualMedia is unavailable) ──
    ActivityResultLauncher<Intent> fallbackGalleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedUri = result.getData().getData();
                    if (selectedUri != null) {
                        try {
                            getContentResolver().takePersistableUriPermission(
                                    selectedUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception ignored) {}
                        selectedImageUriString = selectedUri.toString();
                        imgEditProfile.setImageURI(selectedUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);
        currentUsername = sessionManager.getUsername();

        frameProfilePic = findViewById(R.id.frameProfilePic);
        imgEditProfile  = findViewById(R.id.imgEditProfile);
        etEditName      = findViewById(R.id.etEditName);
        etEditEmail     = findViewById(R.id.etEditEmail);
        etEditPhone     = findViewById(R.id.etEditPhone);
        etEditAddress   = findViewById(R.id.etEditAddress);
        btnSaveProfile  = findViewById(R.id.btnSaveProfile);

        ImageView btnBack = findViewById(R.id.btnBackEditProfile);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        loadExistingData();

        // Tap profile photo → show options dialog
        android.view.View.OnClickListener pickImageListener = v -> showImagePickerDialog();
        frameProfilePic.setOnClickListener(pickImageListener);
        imgEditProfile.setOnClickListener(pickImageListener);

        btnSaveProfile.setOnClickListener(v -> {
            String newName    = etEditName.getText().toString().trim();
            String newEmail   = etEditEmail.getText().toString().trim();
            String newPhone   = etEditPhone.getText().toString().trim();
            String newAddress = etEditAddress.getText().toString().trim();

            if (newEmail.isEmpty() || newPhone.isEmpty()) {
                Toast.makeText(this, "Email and Phone cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                etEditEmail.setError("Please enter a valid email address");
                etEditEmail.requestFocus();
                return;
            }
            if (newPhone.length() != 10 || !newPhone.matches("^\\d{10}$")) {
                etEditPhone.setError("10 digits must be entered");
                etEditPhone.requestFocus();
                return;
            }

            // Show confirmation dialog before saving
            new AlertDialog.Builder(this)
                    .setTitle("Save Changes")
                    .setMessage("Do you want to save the changes to your profile?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        btnSaveProfile.setEnabled(false);
                        String finalName = newName.isEmpty() ? currentUsername : newName;
                        dbHelper.updateUserProfile(currentUsername, finalName, newEmail, newPhone, newAddress,
                                selectedImageUriString, isUpdated -> runOnUiThread(() -> {
                                    btnSaveProfile.setEnabled(true);
                                    if (isUpdated) {
                                        Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
                                    }
                                }));
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_edit_profile_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /** Shows a simple dialog: Choose from Gallery OR Take a Photo */
    private void showImagePickerDialog() {
        String[] options = {"📷  Take Photo", "🖼️  Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Change Profile Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Opens the device camera to take a new photo */
    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "profile_photo");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Profile photo taken from camera");
        cameraImageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (cameraImageUri != null) {
            cameraLauncher.launch(cameraImageUri);
        } else {
            Toast.makeText(this, "Cannot open camera", Toast.LENGTH_SHORT).show();
        }
    }

    /** Opens the modern Android Photo Picker (falls back to Intent-based on older devices) */
    private void openGallery() {
        if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(this)) {
            // Modern photo picker — shows a nice grid of all photos
            photoPickerLauncher.launch(
                    new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build()
            );
        } else {
            // Fallback for older Android versions
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            fallbackGalleryLauncher.launch(Intent.createChooser(intent, "Select Profile Photo"));
        }
    }

    private void loadExistingData() {
        dbHelper.getUserProfile(currentUsername, user -> runOnUiThread(() -> {
            if (user != null) {
                String name    = user.getUsername();
                String email   = user.getEmail();
                String phone   = user.getPhone();
                String address = user.getAddress();
                selectedImageUriString = user.getProfileImage();

                etEditName.setText(name != null ? name : "");
                etEditEmail.setText(email != null ? email : "");
                etEditPhone.setText(phone != null ? phone : "");
                if (address != null && !address.equals("Not Set")) {
                    etEditAddress.setText(address);
                }

                if (selectedImageUriString != null && !selectedImageUriString.isEmpty()) {
                    try {
                        imgEditProfile.setImageURI(Uri.parse(selectedImageUriString));
                    } catch (Exception e) {
                        imgEditProfile.setImageResource(R.mipmap.ic_launcher);
                    }
                }
            }
        }));
    }
}
