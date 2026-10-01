package com.example.bakebliss_bakery.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bakebliss_bakery.R;
import com.example.bakebliss_bakery.database.DBHelper;

public class AdminAddEditFoodActivity extends AppCompatActivity {

    EditText etFoodName, etFoodDesc, etFoodPrice;
    Spinner spinnerCategory;
    Button btnSaveFood;
    TextView tvFormTitle;

    DBHelper dbHelper;

    boolean isEditMode = false;
    String foodDocId = "";

    private static final String[] CATEGORIES = {"Burger", "Pastry", "Cake", "Bun", "Beverage"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_add_edit_food);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        dbHelper = new DBHelper(this);

        tvFormTitle    = findViewById(R.id.tvFormTitle);
        etFoodName     = findViewById(R.id.etFoodName);
        etFoodDesc     = findViewById(R.id.etFoodDesc);
        etFoodPrice    = findViewById(R.id.etFoodPrice);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSaveFood    = findViewById(R.id.btnSaveFood);

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

            etFoodName.setText(name != null ? name : "");
            etFoodDesc.setText(desc != null ? desc : "");
            etFoodPrice.setText(price > 0 ? String.format("%.2f", price) : "");

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

    private void saveFood() {
        String name     = etFoodName.getText().toString().trim();
        String desc     = etFoodDesc.getText().toString().trim();
        String priceStr = etFoodPrice.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

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

        btnSaveFood.setEnabled(false);

        if (isEditMode) {
            // UPDATE existing food item
            dbHelper.updateFoodItem(foodDocId, name, desc, price, category, success -> {
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
            dbHelper.addFoodItem(name, desc, price, category, success -> {
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
