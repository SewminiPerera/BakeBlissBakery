package com.example.bakebliss_bakery.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class AdminAddEditFoodActivity extends AppCompatActivity {

    EditText etFoodName, etFoodDesc, etFoodPrice;
    Spinner spinnerCategory;
    Button btnSaveFood;
    TextView tvFormTitle;
    FrameLayout frameAdminFoodPhoto;
    ImageView imgAdminFoodPreview;
    LinearLayout layoutPhotoPlaceholder, layoutDiscountField;
    android.widget.Switch switchSuperDeal;
    EditText etDiscountPercent;
    ProgressBar progressUpload;

    DBHelper dbHelper;

    boolean isEditMode = false;
    String foodDocId = "";
    String selectedImageUriString = "";
    Uri pendingLocalUri = null;

    private static final String[] CATEGORIES = {"Burger", "Pastry", "Cake", "Bun", "Beverage"};

    private ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher;
    private ActivityResultLauncher<Intent> fallbackGalleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_add_edit_food);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        pendingLocalUri = uri;
                        displayLocalImage(uri);
                    }
                });

        fallbackGalleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedUri = result.getData().getData();
                        if (selectedUri != null) {
                            pendingLocalUri = selectedUri;
                            displayLocalImage(selectedUri);
                        }
                    }
                });

        dbHelper = new DBHelper(this);

        tvFormTitle            = findViewById(R.id.tvFormTitle);
        etFoodName             = findViewById(R.id.etFoodName);
        etFoodDesc             = findViewById(R.id.etFoodDesc);
        etFoodPrice            = findViewById(R.id.etFoodPrice);
        spinnerCategory        = findViewById(R.id.spinnerCategory);
        btnSaveFood            = findViewById(R.id.btnSaveFood);
        frameAdminFoodPhoto    = findViewById(R.id.frameAdminFoodPhoto);
        imgAdminFoodPreview    = findViewById(R.id.imgAdminFoodPreview);
        layoutPhotoPlaceholder = findViewById(R.id.layoutPhotoPlaceholder);
        switchSuperDeal        = findViewById(R.id.switchSuperDeal);
        etDiscountPercent      = findViewById(R.id.etDiscountPercent);
        layoutDiscountField    = findViewById(R.id.layoutDiscountField);
        progressUpload         = findViewById(R.id.progressUpload);

        switchSuperDeal.setOnCheckedChangeListener((buttonView, isChecked) ->
                layoutDiscountField.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        View btnBack = findViewById(R.id.btnBackAdminForm);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (frameAdminFoodPhoto != null)
            frameAdminFoodPhoto.setOnClickListener(v -> choosePhoto());

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, CATEGORIES);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        isEditMode = getIntent().getBooleanExtra("EDIT_MODE", false);

        if (isEditMode) {
            tvFormTitle.setText("Edit Food Item");
            btnSaveFood.setText("Update Item");

            foodDocId           = getIntent().getStringExtra("FOOD_DOC_ID");
            String name         = getIntent().getStringExtra("FOOD_NAME");
            String desc         = getIntent().getStringExtra("FOOD_DESC");
            double price        = getIntent().getDoubleExtra("FOOD_PRICE", 0.0);
            String category     = getIntent().getStringExtra("FOOD_CATEGORY");
            String image        = getIntent().getStringExtra("FOOD_IMAGE");
            boolean isSuperDeal = getIntent().getBooleanExtra("IS_SUPER_DEAL", false);
            double discountPct  = getIntent().getDoubleExtra("DISCOUNT_PERCENT", 0.0);

            etFoodName.setText(name != null ? name : "");
            etFoodDesc.setText(desc != null ? desc : "");
            etFoodPrice.setText(price > 0 ? String.format("%.2f", price) : "");

            switchSuperDeal.setChecked(isSuperDeal);
            if (isSuperDeal) {
                layoutDiscountField.setVisibility(View.VISIBLE);
                etDiscountPercent.setText(discountPct > 0 ? String.valueOf((int) discountPct) : "");
            }

            if (image != null && !image.isEmpty()) {
                selectedImageUriString = image;
                displayImageFromUrl(image);
            }

            if (category != null) {
                for (int i = 0; i < CATEGORIES.length; i++) {
                    if (CATEGORIES[i].equalsIgnoreCase(category)) {
                        spinnerCategory.setSelection(i);
                        break;
                    }
                }
            }
        } else {
            tvFormTitle.setText("Add New Food Item");
            btnSaveFood.setText("Add Item");
        }

        btnSaveFood.setOnClickListener(v -> saveFood());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_add_edit_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void choosePhoto() {
        if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(this)) {
            photoPickerLauncher.launch(
                    new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build());
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            fallbackGalleryLauncher.launch(Intent.createChooser(intent, "Select Food Photo"));
        }
    }

    private void displayLocalImage(Uri localUri) {
        try {
            imgAdminFoodPreview.setImageURI(null);
            imgAdminFoodPreview.setImageURI(localUri);
            imgAdminFoodPreview.setVisibility(View.VISIBLE);
            if (layoutPhotoPlaceholder != null) layoutPhotoPlaceholder.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.e("AdminAddEditFood", "Error showing local preview: " + e.getMessage());
        }
    }

    private void displayImageFromUrl(String url) {
        if (url == null || url.trim().isEmpty()) return;
        if (url.startsWith("http://") || url.startsWith("https://")) {
            imgAdminFoodPreview.setVisibility(View.VISIBLE);
            if (layoutPhotoPlaceholder != null) layoutPhotoPlaceholder.setVisibility(View.GONE);
            new Thread(() -> {
                try {
                    java.net.URL imgUrl = new java.net.URL(url);
                    java.net.HttpURLConnection connection = (java.net.HttpURLConnection) imgUrl.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeStream(connection.getInputStream());
                    runOnUiThread(() -> { if (bmp != null) imgAdminFoodPreview.setImageBitmap(bmp); });
                } catch (Exception ex) {
                    Log.e("AdminAddEditFood", "Error loading remote image: " + ex.getMessage());
                }
            }).start();
        } else {
            try {
                imgAdminFoodPreview.setImageURI(Uri.parse(url));
                imgAdminFoodPreview.setVisibility(View.VISIBLE);
                if (layoutPhotoPlaceholder != null) layoutPhotoPlaceholder.setVisibility(View.GONE);
            } catch (Exception e) {
                Log.e("AdminAddEditFood", "Error loading local image: " + e.getMessage());
            }
        }
    }

    private void saveFood() {
        String name     = etFoodName.getText().toString().trim();
        String desc     = etFoodDesc.getText().toString().trim();
        String priceStr = etFoodPrice.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        boolean isSuperDeal = switchSuperDeal.isChecked();

        if (TextUtils.isEmpty(name)) { etFoodName.setError("Food name is required"); etFoodName.requestFocus(); return; }
        if (TextUtils.isEmpty(desc)) { etFoodDesc.setError("Description is required"); etFoodDesc.requestFocus(); return; }
        if (TextUtils.isEmpty(priceStr)) { etFoodPrice.setError("Price is required"); etFoodPrice.requestFocus(); return; }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) { etFoodPrice.setError("Price must be greater than 0"); return; }
        } catch (NumberFormatException e) {
            etFoodPrice.setError("Enter a valid price"); return;
        }

        double discountPercent = 0.0;
        if (isSuperDeal) {
            String discountStr = etDiscountPercent.getText().toString().trim();
            if (TextUtils.isEmpty(discountStr)) { etDiscountPercent.setError("Enter discount % for Super Deal"); etDiscountPercent.requestFocus(); return; }
            try {
                discountPercent = Double.parseDouble(discountStr);
                if (discountPercent <= 0 || discountPercent >= 100) { etDiscountPercent.setError("Discount must be between 1 and 99"); return; }
            } catch (NumberFormatException e) {
                etDiscountPercent.setError("Enter a valid discount percentage"); return;
            }
        }

        btnSaveFood.setEnabled(false);
        final double finalPrice    = price;
        final double finalDiscount = discountPercent;

        if (pendingLocalUri != null) {
            uploadImageThenSave(name, desc, finalPrice, category, isSuperDeal, finalDiscount);
        } else {
            persistFood(name, desc, finalPrice, category, selectedImageUriString, isSuperDeal, finalDiscount);
        }
    }

    private void uploadImageThenSave(String name, String desc, double price,
                                     String category, boolean isSuperDeal, double discountPercent) {
        if (progressUpload != null) progressUpload.setVisibility(View.VISIBLE);

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("food_images/" + UUID.randomUUID().toString() + ".jpg");

        storageRef.putFile(pendingLocalUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl()
                                .addOnSuccessListener(downloadUri -> {
                                    String imageUrl = downloadUri.toString();
                                    Log.d("AdminAddEditFood", "Image uploaded: " + imageUrl);
                                    if (progressUpload != null) progressUpload.setVisibility(View.GONE);
                                    persistFood(name, desc, price, category, imageUrl, isSuperDeal, discountPercent);
                                })
                                .addOnFailureListener(e -> {
                                    if (progressUpload != null) progressUpload.setVisibility(View.GONE);
                                    Log.e("AdminAddEditFood", "Failed to get download URL: " + e.getMessage());
                                    Toast.makeText(this, "Image upload failed. Saving without image.", Toast.LENGTH_SHORT).show();
                                    persistFood(name, desc, price, category, selectedImageUriString, isSuperDeal, discountPercent);
                                }))
                .addOnFailureListener(e -> {
                    if (progressUpload != null) progressUpload.setVisibility(View.GONE);
                    Log.e("AdminAddEditFood", "Image upload failed: " + e.getMessage());
                    Toast.makeText(this, "Image upload failed. Saving without image.", Toast.LENGTH_SHORT).show();
                    btnSaveFood.setEnabled(true);
                });
    }

    private void persistFood(String name, String desc, double price, String category,
                              String imageUrl, boolean isSuperDeal, double discountPercent) {
        if (isEditMode) {
            dbHelper.updateFoodItem(foodDocId, name, desc, price, category, imageUrl,
                    isSuperDeal, discountPercent, success -> {
                        btnSaveFood.setEnabled(true);
                        if (success) { Toast.makeText(this, "\"" + name + "\" updated!", Toast.LENGTH_SHORT).show(); finish(); }
                        else Toast.makeText(this, "Update failed. Please try again.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            dbHelper.addFoodItem(name, desc, price, category, imageUrl,
                    isSuperDeal, discountPercent, success -> {
                        btnSaveFood.setEnabled(true);
                        if (success) { Toast.makeText(this, "\"" + name + "\" added!", Toast.LENGTH_SHORT).show(); finish(); }
                        else Toast.makeText(this, "Failed to add item. Please try again.", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
