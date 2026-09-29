package com.example.bakebliss_bakery.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bakebliss_bakery.R;
import com.example.bakebliss_bakery.database.DBHelper;
import com.example.bakebliss_bakery.utils.SessionManager;

public class FoodDetailActivity extends AppCompatActivity {

    TextView tvName, tvPrice, tvDesc, tvQuantity;
    TextView btnMinus, btnPlus;
    TextView btnHeart;
    ImageView imgDetailFood;
    Button btnAddToCart;

    int quantity = 1;
    double basePrice = 0.0;
    boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_food_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        tvName = findViewById(R.id.tvDetailName);
        tvPrice = findViewById(R.id.tvDetailPrice);
        tvDesc = findViewById(R.id.tvDetailDesc);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        tvQuantity = findViewById(R.id.tvQuantity);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        imgDetailFood = findViewById(R.id.imgDetailFood);
        btnHeart = findViewById(R.id.btnHeart);

        Intent intent = getIntent();
        String name = intent.getStringExtra("FOOD_NAME");
        String descFromIntent = intent.getStringExtra("FOOD_DESC");
        basePrice = intent.getDoubleExtra("FOOD_PRICE", 0.0);

        tvName.setText(name);

        tvPrice.setText(String.format("Rs. %.2f", basePrice));

        if(name != null) {
            imgDetailFood.setImageResource(getImageResource(name));
        }

        String finalDescription;

        if (descFromIntent != null && descFromIntent.contains("Hot Deal Promo Applied!")) {
            finalDescription = descFromIntent;
        } else if (descFromIntent != null && !descFromIntent.isEmpty()) {
            finalDescription = descFromIntent;
        } else {
            finalDescription = "A perfect blend of fresh, high-quality ingredients.\n" +
                    "Prepared with love for a unique and rich taste experience.\n" +
                    "Perfect for any meal, this dish guarantees satisfaction.";
        }

        tvDesc.setText(finalDescription);

        updateButtonPriceDisplay();

        btnHeart.setOnClickListener(v -> {
            isFavorite = !isFavorite;

            if(isFavorite) {
                btnHeart.setText("❤");
                btnHeart.setTextColor(Color.parseColor("#FF0000"));
                Toast.makeText(FoodDetailActivity.this, "Added to favorites!", Toast.LENGTH_SHORT).show();
            } else {
                btnHeart.setText("♡");
                btnHeart.setTextColor(Color.parseColor("#000000"));
                Toast.makeText(FoodDetailActivity.this, "Removed from favorites.", Toast.LENGTH_SHORT).show();
            }
        });

        btnPlus.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
            updateButtonPriceDisplay();
        });

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
                updateButtonPriceDisplay();
            }
        });

        btnAddToCart.setOnClickListener(v -> {
            SessionManager sessionManager = new SessionManager(FoodDetailActivity.this);
            DBHelper dbHelper = new DBHelper(FoodDetailActivity.this);

            String username = sessionManager.getUsername();

            if(username != null) {
                boolean isAdded = dbHelper.addToCart(username, name, basePrice, quantity);

                if(isAdded) {
                    Toast.makeText(FoodDetailActivity.this, quantity + "x " + name + " added to cart!", Toast.LENGTH_SHORT).show();

                    Intent cartIntent = new Intent(FoodDetailActivity.this, CartActivity.class);
                    startActivity(cartIntent);

                    finish();
                } else {
                    Toast.makeText(FoodDetailActivity.this, "Failed to add to cart.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(FoodDetailActivity.this, "Please login first!", Toast.LENGTH_SHORT).show();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_food_detail_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);

            View bottomLayout = findViewById(R.id.bottom_cart_layout);
            int originalPaddingDp = (int) (15 * getResources().getDisplayMetrics().density);
            bottomLayout.setPadding(originalPaddingDp, originalPaddingDp, originalPaddingDp, originalPaddingDp + systemBars.bottom);

            return insets;
        });
    }

    private void updateButtonPriceDisplay() {
        double total = basePrice * quantity;
        btnAddToCart.setText(String.format("Add to Cart - Rs. %.2f", total));
    }

    private int getImageResource(String foodName) {
        if (foodName == null) return R.mipmap.ic_launcher;
        String cleanName = foodName.replace(" (50% OFF)", "");
        String imageName = "food_" + cleanName.toLowerCase()
                .replace(" ", "_")
                .replace("(", "")
                .replace(")", "")
                .replace("&", "and");
        int resId = getResources().getIdentifier(imageName, "drawable", getPackageName());
        if (resId != 0) {
            return resId;
        }
        return R.mipmap.ic_launcher;
    }
}
