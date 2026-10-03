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

import com.example.bakebliss_bakery.MainActivity;
import com.example.bakebliss_bakery.R;
import com.example.bakebliss_bakery.adapters.AdminFoodAdapter;
import com.example.bakebliss_bakery.database.DBHelper;
import com.example.bakebliss_bakery.models.FoodModel;
import com.example.bakebliss_bakery.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
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

        View btnBack = findViewById(R.id.btnBackAdmin);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Intent intent = new Intent(AdminActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }

        View btnAdminStore = findViewById(R.id.btnAdminStore);
        if (btnAdminStore != null) {
            btnAdminStore.setOnClickListener(v -> {
                Intent intent = new Intent(AdminActivity.this, MainActivity.class);
                startActivity(intent);
            });
        }

        View btnAdminLogout = findViewById(R.id.btnAdminLogout);
        if (btnAdminLogout != null) {
            btnAdminLogout.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Logout", (d, w) -> {
                            new SessionManager(getApplicationContext()).logoutUser();
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

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
        String cleanQuery = (query == null) ? "" : query.trim().toLowerCase();
        List<FoodModel> filtered = new ArrayList<>();

        if (cleanQuery.isEmpty()) {
            filtered.addAll(originalFoodList);
        } else {
            String[] queryTokens = cleanQuery.split("\\s+");

            class ScoredAdminFood {
                FoodModel food;
                int score;
                ScoredAdminFood(FoodModel food, int score) {
                    this.food = food;
                    this.score = score;
                }
            }

            List<ScoredAdminFood> scored = new ArrayList<>();
            for (FoodModel food : originalFoodList) {
                if (food == null || food.getName() == null) continue;
                String name = food.getName().trim().toLowerCase();
                String category = food.getCategory() != null ? food.getCategory().trim().toLowerCase() : "";
                String desc = food.getDescription() != null ? food.getDescription().trim().toLowerCase() : "";

                int score = 0;
                if (name.startsWith(cleanQuery)) {
                    score = 1000 + (100 - Math.min(name.length(), 100));
                } else {
                    String[] words = name.split("[\\s\\-_,.]+");
                    for (String word : words) {
                        if (word.startsWith(cleanQuery)) {
                            score = 600;
                            break;
                        }
                    }
                    if (score == 0 && queryTokens.length > 1) {
                        boolean allTokensMatch = true;
                        for (String token : queryTokens) {
                            boolean tokenFound = false;
                            for (String word : words) {
                                if (word.startsWith(token) || word.contains(token)) {
                                    tokenFound = true;
                                    break;
                                }
                            }
                            if (!tokenFound && (category.contains(token) || desc.contains(token))) {
                                tokenFound = true;
                            }
                            if (!tokenFound) {
                                allTokensMatch = false;
                                break;
                            }
                        }
                        if (allTokensMatch) score = 400;
                    }
                    if (score == 0 && name.contains(cleanQuery)) {
                        score = 300;
                    } else if (score == 0 && category.startsWith(cleanQuery)) {
                        score = 150;
                    } else if (score == 0 && category.contains(cleanQuery)) {
                        score = 100;
                    } else if (score == 0 && cleanQuery.length() >= 3 && desc.contains(cleanQuery)) {
                        score = 20;
                    }
                }

                if (score > 0) {
                    scored.add(new ScoredAdminFood(food, score));
                }
            }

            Collections.sort(scored, (a, b) -> Integer.compare(b.score, a.score));
            for (ScoredAdminFood sf : scored) {
                filtered.add(sf.food);
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
        intent.putExtra("FOOD_IMAGE", food.getImageUrl());
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
