package com.example.bakebliss_bakery.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bakebliss_bakery.R;
import com.example.bakebliss_bakery.adapters.AdminFoodAdapter;
import com.example.bakebliss_bakery.database.DBHelper;
import com.example.bakebliss_bakery.models.FoodModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity implements AdminFoodAdapter.OnItemActionListener {

    RecyclerView rvAdminFood;
    EditText etAdminSearch;
    LinearLayout layoutAdminEmpty;
    FloatingActionButton fabAddFood;
    TextView tvAdminItemCount;

    DBHelper dbHelper;
    AdminFoodAdapter adminFoodAdapter;
    List<FoodModel> foodList;
    List<FoodModel> originalFoodList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        dbHelper       = new DBHelper(this);
        foodList       = new ArrayList<>();
        originalFoodList = new ArrayList<>();

        rvAdminFood     = findViewById(R.id.rvAdminFood);
        etAdminSearch   = findViewById(R.id.etAdminSearch);
        layoutAdminEmpty = findViewById(R.id.layoutAdminEmpty);
        fabAddFood      = findViewById(R.id.fabAddFood);
        tvAdminItemCount = findViewById(R.id.tvAdminItemCount);

        rvAdminFood.setLayoutManager(new LinearLayoutManager(this));
        adminFoodAdapter = new AdminFoodAdapter(this, foodList, this);
        rvAdminFood.setAdapter(adminFoodAdapter);

        loadAllFood();

        fabAddFood.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminAddEditFoodActivity.class);
            startActivity(intent);
        });

        // Real-time search inside admin panel
        etAdminSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAdminSearch(s.toString());
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_admin_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllFood();
    }

    private void loadAllFood() {
        dbHelper.getAllFoodItems(list -> {
            foodList.clear();
            originalFoodList.clear();
            if (list != null) {
                foodList.addAll(list);
                originalFoodList.addAll(list);
            }
            adminFoodAdapter.notifyDataSetChanged();
            updateCountAndEmptyState();
        });
    }

    private void filterAdminSearch(String query) {
        List<FoodModel> filtered = new ArrayList<>();
        for (FoodModel food : originalFoodList) {
            if (food.getName().toLowerCase().contains(query.toLowerCase()) ||
                    food.getCategory().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(food);
            }
        }
        adminFoodAdapter.updateList(filtered);
        tvAdminItemCount.setText(filtered.size() + " items");
        layoutAdminEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        rvAdminFood.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void updateCountAndEmptyState() {
        tvAdminItemCount.setText(foodList.size() + " items");
        if (foodList.isEmpty()) {
            rvAdminFood.setVisibility(View.GONE);
            layoutAdminEmpty.setVisibility(View.VISIBLE);
        } else {
            rvAdminFood.setVisibility(View.VISIBLE);
            layoutAdminEmpty.setVisibility(View.GONE);
        }
    }

    // ── Edit action ──────────────────────────────────────────────────────────
    @Override
    public void onEdit(FoodModel food) {
        Intent intent = new Intent(AdminActivity.this, AdminAddEditFoodActivity.class);
        intent.putExtra("EDIT_MODE", true);
        intent.putExtra("FOOD_DOC_ID", food.getDocumentId());
        intent.putExtra("FOOD_NAME", food.getName());
        intent.putExtra("FOOD_DESC", food.getDescription());
        intent.putExtra("FOOD_PRICE", food.getPrice());
        intent.putExtra("FOOD_CATEGORY", food.getCategory());
        startActivity(intent);
    }

    // ── Delete action with confirmation dialog ────────────────────────────────
    @Override
    public void onDelete(FoodModel food) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Food Item")
                .setMessage("Are you sure you want to delete \"" + food.getName() + "\"?\nThis action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (food.getDocumentId() == null || food.getDocumentId().isEmpty()) {
                        Toast.makeText(this, "Cannot delete: missing document ID", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dbHelper.deleteFoodItem(food.getDocumentId(), success -> {
                        if (success) {
                            Toast.makeText(this, "\"" + food.getName() + "\" deleted!", Toast.LENGTH_SHORT).show();
                            loadAllFood();
                        } else {
                            Toast.makeText(this, "Failed to delete item. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
