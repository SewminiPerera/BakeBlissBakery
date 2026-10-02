package com.example.bakebliss_bakery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge; // NEW
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets; // NEW
import androidx.core.view.ViewCompat; // NEW
import androidx.core.view.WindowInsetsCompat; // NEW
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bakebliss_bakery.MainActivity;
import com.example.bakebliss_bakery.R;
import com.example.bakebliss_bakery.adapters.CartAdapter;
import com.example.bakebliss_bakery.database.DBHelper;
import com.example.bakebliss_bakery.models.CartModel;
import com.example.bakebliss_bakery.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    RecyclerView rvCartItems;
    TextView tvSubTotal, tvDeliveryFee, tvGrandTotal;
    Button btnPlaceOrder;
    LinearLayout layoutEmptyCart;
    CardView bottomBillLayout;
    BottomNavigationView bottomNavigationView;

    DBHelper dbHelper;
    SessionManager sessionManager;
    CartAdapter cartAdapter;
    List<CartModel> cartList;

    double deliveryFee = 300.00;
    double grandTotal = 0.0;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        rvCartItems = findViewById(R.id.rvCartItems);
        tvSubTotal = findViewById(R.id.tvSubTotal);
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
        tvGrandTotal = findViewById(R.id.tvGrandTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        layoutEmptyCart = findViewById(R.id.layoutEmptyCart);
        bottomBillLayout = findViewById(R.id.bottom_bill_layout);

        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);
        username = sessionManager.getUsername();

        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        cartList = new ArrayList<>();

        // Pass 'this' as an interface listener to update bill when quantity changes
        cartAdapter = new CartAdapter(this, cartList, dbHelper, this::calculateTotal);
        rvCartItems.setAdapter(cartAdapter);

        loadCartData();

        btnPlaceOrder.setOnClickListener(v -> {
            if (cartList.isEmpty()) {
                Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Navigate to Payment page with order totals and cart items
            double subTotal = 0.0;
            for (CartModel item : cartList) {
                subTotal += (item.getPrice() * item.getQuantity());
            }
            double total = subTotal > 0 ? subTotal + deliveryFee : 0.0;

            Intent payIntent = new Intent(CartActivity.this, PaymentActivity.class);
            payIntent.putExtra("SUBTOTAL", subTotal);
            payIntent.putExtra("DELIVERY_FEE", subTotal > 0 ? deliveryFee : 0.0);
            payIntent.putExtra("GRAND_TOTAL", total);
            payIntent.putExtra("CART_LIST", (java.io.Serializable) new java.util.ArrayList<>(cartList));
            startActivity(payIntent);
        });

        // BOTTOM NAVIGATION BAR SETUP
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_cart);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    Intent intent = new Intent(CartActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_cart) {
                    return true;
                } else if (id == R.id.nav_orders) {
                    Intent intent = new Intent(CartActivity.this, MyOrderActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (id == R.id.nav_profile) {
                    Intent intent = new Intent(CartActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }
                return false;
            }
        });

        // Window Insets Logic to remove bottom white space
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_cart_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply padding to Top, Left, Right for root
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            // Apply Bottom padding ONLY to BottomNavigationView
            bottomNavigationView.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });
    }

    // Load Data from Firestore Cart
    private void loadCartData() {
        dbHelper.getCartItems(username, items -> {
            cartList.clear();
            if (items != null) {
                cartList.addAll(items);
            }
            cartAdapter.notifyDataSetChanged();
            calculateTotal();
            checkEmptyState();
        });
    }

    // Calculate Total Bill dynamically
    public void calculateTotal() {
        double subTotal = 0.0;
        for (CartModel item : cartList) {
            subTotal += (item.getPrice() * item.getQuantity());
        }

        tvSubTotal.setText(String.format("Rs. %.2f", subTotal));

        if (subTotal > 0) {
            grandTotal = subTotal + deliveryFee;
            tvDeliveryFee.setText(String.format("Rs. %.2f", deliveryFee));
        } else {
            grandTotal = 0.0;
            tvDeliveryFee.setText("Rs. 0.00");
        }

        tvGrandTotal.setText(String.format("Rs. %.2f", grandTotal));
        checkEmptyState();
    }

    // Show/Hide empty state
    private void checkEmptyState() {
        if(cartList.isEmpty()){
            layoutEmptyCart.setVisibility(View.VISIBLE);
            rvCartItems.setVisibility(View.GONE);
            btnPlaceOrder.setEnabled(false);
            btnPlaceOrder.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.GRAY));
        } else {
            layoutEmptyCart.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.VISIBLE);
            btnPlaceOrder.setEnabled(true);
            btnPlaceOrder.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FF5722")));
        }
    }
}