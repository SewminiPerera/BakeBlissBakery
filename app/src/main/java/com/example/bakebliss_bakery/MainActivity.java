package com.example.bakebliss_bakery;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bakebliss_bakery.activities.CartActivity;
import com.example.bakebliss_bakery.activities.FoodDetailActivity;
import com.example.bakebliss_bakery.activities.LoginActivity;
import com.example.bakebliss_bakery.activities.MyOrderActivity;
import com.example.bakebliss_bakery.activities.ProfileActivity;
import com.example.bakebliss_bakery.adapters.FoodAdapter;
import com.example.bakebliss_bakery.database.DBHelper;
import com.example.bakebliss_bakery.models.FoodModel;
import com.example.bakebliss_bakery.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Component Declarations
    SessionManager sessionManager;
    RecyclerView recyclerView;
    DBHelper dbHelper;
    List<FoodModel> foodList;
    List<FoodModel> originalFoodList;
    FoodAdapter adapter;
    BottomNavigationView bottomNavigationView;

    // UI Components for Search & Filtering
    EditText etSearchFood;
    TextView tvNoFoodFound;
    TextView tvCatAll, tvCatBurger, tvCatPastry, tvCatCake, tvCatBun, tvCatBeverage, tvSeeAll;
    ImageView imgTopProfile; // NEW

    // Flag to prevent searching conflicts when clicking categories
    boolean isCategoryFiltering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // --- SESSION VERIFICATION ---
        sessionManager = new SessionManager(getApplicationContext());
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        dbHelper = new DBHelper(this); // Initialize DB First
        dbHelper.seedFoodItemsIfNeeded(); // Seed initial food items to Firestore if collection is empty

        // --- SET DYNAMIC USERNAME AND PROFILE PIC IN HEADER ---
        TextView tvWelcomeName = findViewById(R.id.tvWelcomeName);
        imgTopProfile = findViewById(R.id.imgTopProfile);

        String currentUsername = sessionManager.getUsername();
        if(currentUsername != null) {
            tvWelcomeName.setText(currentUsername);
            loadProfilePicture(currentUsername); // NEW: Load Custom Image!
        }

        // --- INIT TOP BUTTONS & SEARCH BAR ---
        imgTopProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Notification Button to show a popup with Promo Codes
        ImageView imgNotification = findViewById(R.id.imgNotification);
        imgNotification.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Offers & Promotions 🎉")
                    .setMessage("1. 50% OFF on Chicken Burger!\nUse Promo Code: BURGER50\n\n" +
                            "2. Free Delivery on orders over Rs. 2000.\nUse Promo Code: FREEDEL\n\n" +
                            "3. 10% OFF on your first order.\nUse Promo Code: WELCOME10")
                    .setPositiveButton("Got it!", null)
                    .show();
        });

        // Shop Now Button navigates directly to FoodDetailActivity with 50% discount
        Button btnShopNow = findViewById(R.id.btnShopNow);
        btnShopNow.setOnClickListener(v -> {
            FoodModel promoFood = null;

            // Find the Chicken Burger in the original list
            for (FoodModel food : originalFoodList) {
                if (food.getName().equals("Chicken Burger")) {
                    promoFood = food;
                    break;
                }
            }

            if (promoFood != null) {
                Intent intent = new Intent(MainActivity.this, FoodDetailActivity.class);
                intent.putExtra("FOOD_ID", promoFood.getId());
                intent.putExtra("FOOD_NAME", promoFood.getName() + " (50% OFF)");
                intent.putExtra("FOOD_DESC", promoFood.getDescription() + "\n\n🔥 Hot Deal Promo Applied!");
                intent.putExtra("FOOD_PRICE", promoFood.getPrice() / 2.0);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "Promo item not found!", Toast.LENGTH_SHORT).show();
            }
        });

        etSearchFood = findViewById(R.id.etSearchFood);
        tvNoFoodFound = findViewById(R.id.tvNoFoodFound);

        // --- RECYCLER VIEW SETUP ---
        recyclerView = findViewById(R.id.recycler_view_food);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        foodList = new ArrayList<>();
        originalFoodList = new ArrayList<>();

        loadFoodData();

        adapter = new FoodAdapter(this, foodList);
        recyclerView.setAdapter(adapter);

        // --- SETUP CATEGORIES & SEARCH ---
        initializeCategoryViews();
        setupCategoryClickListeners();
        setupSearchListener();

        // --- BOTTOM NAVIGATION BAR SETUP ---
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    return true;
                } else if (id == R.id.nav_cart) {
                    Intent intent = new Intent(MainActivity.this, CartActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_orders) {
                    Intent intent = new Intent(MainActivity.this, MyOrderActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_profile) {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });

        // --- Updated Edge-To-Edge Window Insets Logic ---
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_home_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply padding to Top, Left, and Right for the root layout (Do NOT apply Bottom padding here)
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);

            // Apply the Bottom padding ONLY to the BottomNavigationView so it fills the white space
            bottomNavigationView.setPadding(0, 0, 0, systemBars.bottom);

            return insets;
        });
    }

    // Refresh Profile picture if changed in ProfileActivity
    @Override
    protected void onResume() {
        super.onResume();
        if(sessionManager != null && sessionManager.isLoggedIn()) {
            loadProfilePicture(sessionManager.getUsername());
        }
    }

    // --- Fetch Image from Firestore and Set in Home Screen Header ---
    private void loadProfilePicture(String username) {
        dbHelper.getUserProfile(username, user -> {
            if (user != null && user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                try {
                    imgTopProfile.setImageURI(Uri.parse(user.getProfileImage()));
                } catch (Exception e) {
                    imgTopProfile.setImageResource(R.mipmap.ic_launcher);
                }
            } else {
                imgTopProfile.setImageResource(R.mipmap.ic_launcher);
            }
        });
    }

    private void loadFoodData() {
        dbHelper.getAllFoodItems(list -> {
            foodList.clear();
            originalFoodList.clear();
            if (list != null) {
                foodList.addAll(list);
                originalFoodList.addAll(list);
            }
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        });
    }

    // Real-time Search Listener
    private void setupSearchListener() {
        etSearchFood.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isCategoryFiltering) {
                    filterSearch(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    // Filter the list based on Search Bar input
    private void filterSearch(String query) {
        updateCategoryStyles(tvCatAll);

        List<FoodModel> filteredList = new ArrayList<>();
        for (FoodModel food : originalFoodList) {
            if (food.getName().toLowerCase().contains(query.toLowerCase()) ||
                    food.getDescription().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(food);
            }
        }

        adapter.updateList(filteredList);

        if (filteredList.isEmpty()) {
            tvNoFoodFound.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoFoodFound.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void initializeCategoryViews() {
        tvCatAll = findViewById(R.id.tvCatAll);
        tvCatBurger = findViewById(R.id.tvCatBurger);
        tvCatPastry = findViewById(R.id.tvCatPastry);
        tvCatCake = findViewById(R.id.tvCatCake);
        tvCatBun = findViewById(R.id.tvCatBun);
        tvCatBeverage = findViewById(R.id.tvCatBeverage);
        tvSeeAll = findViewById(R.id.tvSeeAll);
    }

    private void setupCategoryClickListeners() {
        tvCatAll.setOnClickListener(v -> filterCategory("All", tvCatAll));
        tvSeeAll.setOnClickListener(v -> filterCategory("All", tvCatAll));

        tvCatBurger.setOnClickListener(v -> filterCategory("Burger", tvCatBurger));
        tvCatPastry.setOnClickListener(v -> filterCategory("Pastry", tvCatPastry));
        tvCatCake.setOnClickListener(v -> filterCategory("Cake", tvCatCake));
        tvCatBun.setOnClickListener(v -> filterCategory("Bun", tvCatBun));
        tvCatBeverage.setOnClickListener(v -> filterCategory("Beverage", tvCatBeverage));
    }

    private void filterCategory(String categoryName, TextView selectedView) {
        updateCategoryStyles(selectedView);

        isCategoryFiltering = true;
        etSearchFood.setText("");
        etSearchFood.clearFocus();
        isCategoryFiltering = false;

        List<FoodModel> filteredList = new ArrayList<>();

        if (categoryName.equals("All")) {
            filteredList.addAll(originalFoodList);
        } else {
            for (FoodModel food : originalFoodList) {
                if (categoryName.equalsIgnoreCase(food.getCategory())) {
                    filteredList.add(food);
                }
            }
        }

        adapter.updateList(filteredList);

        if (filteredList.isEmpty()) {
            tvNoFoodFound.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoFoodFound.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void updateCategoryStyles(TextView selectedView) {
        TextView[] allCategories = {tvCatAll, tvCatBurger, tvCatPastry, tvCatCake, tvCatBun, tvCatBeverage};
        for (TextView tv : allCategories) {
            tv.setBackgroundTintList(null);
            tv.setTextColor(Color.parseColor("#000000"));
        }

        selectedView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF5722")));
        selectedView.setTextColor(Color.parseColor("#FFFFFF"));
    }
}
