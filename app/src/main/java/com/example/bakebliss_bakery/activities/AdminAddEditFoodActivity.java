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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

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

    DBHelper dbHelper;

    boolean isEditMode = false;
    String foodDocId = "";
    String selectedImageUriString = "";

    private static final String[] CATEGORIES = {"Burger", "Pastry", "Cake", "Bun", "Beverage"};

    // Modern Android Photo Picker
    private final ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    String localUri = saveImageToInternalStorage(uri);
                    selectedImageUriString = localUri;
                    displayFoodImage(localUri);
                }
            });

    // Fallback Gallery Picker
    private final ActivityResultLauncher<Intent> fallbackGalleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedUri = result.getData().getData();
                    if (selectedUri != null) {
                        String localUri = saveImageToInternalStorage(selectedUri);
                        selectedImageUriString = localUri;
                        displayFoodImage(localUri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_add_edit_food);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

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

        // Show/hide discount field based on Super Deal switch
        switchSuperDeal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutDiscountField.setVisibility(isChecked ? android.view.View.VISIBLE : android.view.View.GONE);
        });

        View btnBack = findViewById(R.id.btnBackAdminForm);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Tap to choose image
        if (frameAdminFoodPhoto != null) {
            frameAdminFoodPhoto.setOnClickListener(v -> choosePhoto());
        }

        // Populate category spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, CATEGORIES);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        // Check if we are editing an existing item
        isEditMode = getIntent().getBooleanExtra("EDIT_MODE", false);

        if (isEditMode) {
            tvFormTitle.setText("✏️ Edit Food Item");
            btnSaveFood.setText("Update Item");

            foodDocId = getIntent().getStringExtra("FOOD_DOC_ID");
            String name     = getIntent().getStringExtra("FOOD_NAME");
            String desc     = getIntent().getStringExtra("FOOD_DESC");
            double price    = getIntent().getDoubleExtra("FOOD_PRICE", 0.0);
            String category = getIntent().getStringExtra("FOOD_CATEGORY");
            String image    = getIntent().getStringExtra("FOOD_IMAGE");
            boolean isSuperDeal = getIntent().getBooleanExtra("IS_SUPER_DEAL", false);
            double discountPct  = getIntent().getDoubleExtra("DISCOUNT_PERCENT", 0.0);

            etFoodName.setText(name != null ? name : "");
            etFoodDesc.setText(desc != null ? desc : "");
            etFoodPrice.setText(price > 0 ? String.format("%.2f", price) : "");

            switchSuperDeal.setChecked(isSuperDeal);
            if (isSuperDeal) {
                layoutDiscountField.setVisibility(android.view.View.VISIBLE);
                etDiscountPercent.setText(discountPct > 0 ? String.valueOf((int) discountPct) : "");
            }

            if (image != null && !image.isEmpty()) {
                selectedImageUriString = image;
                displayFoodImage(image);
            }

            // Pre-select the category in the spinner
            if (category != null) {
                for (int i = 0; i < CATEGORIES.length; i++) {
                    if (CATEGORIES[i].equalsIgnoreCase(category)) {
                        spinnerCategory.setSelection(i);
                        break;
                    }
                }
            }
        } else {
            tvFormTitle.setText("➕ Add New Food Item");
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
                            .build()
            );
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            fallbackGalleryLauncher.launch(Intent.createChooser(intent, "Select Food Photo"));
        }
    }

    private void displayFoodImage(String uriString) {
        if (uriString != null && !uriString.trim().isEmpty()) {
            try {
                imgAdminFoodPreview.setImageURI(Uri.parse(uriString));
                imgAdminFoodPreview.setVisibility(View.VISIBLE);
                if (layoutPhotoPlaceholder != null) {
                    layoutPhotoPlaceholder.setVisibility(View.GONE);
                }
                return;
            } catch (Exception e) {
                Log.e("AdminAddEditFood", "Error loading preview image: " + e.getMessage());
            }
        }
        if (imgAdminFoodPreview != null) {
            imgAdminFoodPreview.setVisibility(View.GONE);
        }
        if (layoutPhotoPlaceholder != null) {
            layoutPhotoPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    private String saveImageToInternalStorage(Uri sourceUri) {
        if (sourceUri == null) return "";
        try {
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            if (inputStream == null) return sourceUri.toString();
            File dir = new File(getFilesDir(), "food_images");
            if (!dir.exists()) dir.mkdirs();
            File destFile = new File(dir, "food_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(destFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            return Uri.fromFile(destFile).toString();
        } catch (Exception e) {
            Log.e("AdminAddEditFood", "Error copying image to internal storage: " + e.getMessage());
            return sourceUri.toString();
        }
    }

    private void saveFood() {
        String name     = etFoodName.getText().toString().trim();
        String desc     = etFoodDesc.getText().toString().trim();
        String priceStr = etFoodPrice.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        boolean isSuperDeal = switchSuperDeal.isChecked();

        // Validation
        if (TextUtils.isEmpty(name)) {
            etFoodName.setError("Food name is required");
            etFoodName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(desc)) {
            etFoodDesc.setError("Description is required");
            etFoodDesc.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(priceStr)) {
            etFoodPrice.setError("Price is required");
            etFoodPrice.requestFocus();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                etFoodPrice.setError("Price must be greater than 0");
                return;
            }
        } catch (NumberFormatException e) {
            etFoodPrice.setError("Enter a valid price");
            return;
        }

        double discountPercent = 0.0;
        if (isSuperDeal) {
            String discountStr = etDiscountPercent.getText().toString().trim();
            if (TextUtils.isEmpty(discountStr)) {
                etDiscountPercent.setError("Enter discount % for Super Deal");
                etDiscountPercent.requestFocus();
                return;
            }
            try {
                discountPercent = Double.parseDouble(discountStr);
                if (discountPercent <= 0 || discountPercent >= 100) {
                    etDiscountPercent.setError("Discount must be between 1 and 99");
                    return;
                }
            } catch (NumberFormatException e) {
                etDiscountPercent.setError("Enter a valid discount percentage");
                return;
            }
        }

        btnSaveFood.setEnabled(false);
        final double finalDiscount = discountPercent;

        if (isEditMode) {
            // UPDATE existing food item
            dbHelper.updateFoodItem(foodDocId, name, desc, price, category, selectedImageUriString, isSuperDeal, finalDiscount, success -> {
                btnSaveFood.setEnabled(true);
                if (success) {
                    Toast.makeText(this, "\"" + name + "\" updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Update failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // CREATE new food item
            dbHelper.addFoodItem(name, desc, price, category, selectedImageUriString, isSuperDeal, finalDiscount, success -> {
                btnSaveFood.setEnabled(true);
                if (success) {
                    Toast.makeText(this, "\"" + name + "\" added successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to add item. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
