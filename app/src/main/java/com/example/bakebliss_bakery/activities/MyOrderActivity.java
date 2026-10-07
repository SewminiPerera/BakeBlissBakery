package com.example.bakebliss_bakery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bakebliss_bakery.MainActivity;
import com.example.bakebliss_bakery.R;
import com.example.bakebliss_bakery.adapters.OrderAdapter;
import com.example.bakebliss_bakery.database.DBHelper;
import com.example.bakebliss_bakery.models.OrderModel;
import com.example.bakebliss_bakery.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class MyOrderActivity extends AppCompatActivity {

    RecyclerView rvOrders;
    LinearLayout layoutEmptyOrders;
    OrderAdapter orderAdapter;
    List<OrderModel> orderList;

    DBHelper dbHelper;
    SessionManager sessionManager;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_myorder);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);
        String username = sessionManager.getUsername();

        rvOrders = findViewById(R.id.rvOrders);
        layoutEmptyOrders = findViewById(R.id.layoutEmptyOrders);

        View btnBack = findViewById(R.id.btnBackOrders);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        View btnClearOrders = findViewById(R.id.btnClearOrders);
        if (btnClearOrders != null) {
            btnClearOrders.setOnClickListener(v -> {
                if (orderList == null || orderList.isEmpty()) {
                    android.widget.Toast.makeText(this, "No orders to clear", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                new android.app.AlertDialog.Builder(MyOrderActivity.this)
                        .setTitle("Clear All Orders")
                        .setMessage("Are you sure you want to clear your entire order history?")
                        .setPositiveButton("Clear All", (dialog, which) -> {
                            dbHelper.clearAllOrders(username, success -> {
                                runOnUiThread(() -> {
                                    if (success) {
                                        orderList.clear();
                                        if (orderAdapter != null) {
                                            orderAdapter.notifyDataSetChanged();
                                        }
                                        rvOrders.setVisibility(View.GONE);
                                        layoutEmptyOrders.setVisibility(View.VISIBLE);
                                        android.widget.Toast.makeText(MyOrderActivity.this, "Order history cleared!", android.widget.Toast.LENGTH_SHORT).show();
                                    } else {
                                        android.widget.Toast.makeText(MyOrderActivity.this, "Failed to clear orders. Please try again.", android.widget.Toast.LENGTH_SHORT).show();
                                    }
                                });
                            });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(this, orderList, dbHelper, username, () -> {
            if (orderList.isEmpty()) {
                rvOrders.setVisibility(View.GONE);
                layoutEmptyOrders.setVisibility(View.VISIBLE);
            }
        });
        rvOrders.setAdapter(orderAdapter);

        loadAllOrders(username);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_orders);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    Intent intent = new Intent(MyOrderActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_cart) {
                    Intent intent = new Intent(MyOrderActivity.this, CartActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (id == R.id.nav_orders) {
                    return true;
                } else if (id == R.id.nav_profile) {
                    Intent intent = new Intent(MyOrderActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }
                return false;
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_orders_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            bottomNavigationView.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });
    }

    private void loadAllOrders(String username) {
        // Show a loading state while fetching
        runOnUiThread(() -> {
            rvOrders.setVisibility(View.GONE);
            layoutEmptyOrders.setVisibility(View.GONE);
        });

        dbHelper.getUserOrders(username, orders -> {
            runOnUiThread(() -> {
                orderList.clear();
                if (orders != null && !orders.isEmpty()) {
                    orderList.addAll(orders);
                }

                if (orderList.isEmpty()) {
                    rvOrders.setVisibility(View.GONE);
                    layoutEmptyOrders.setVisibility(View.VISIBLE);
                } else {
                    layoutEmptyOrders.setVisibility(View.GONE);
                    rvOrders.setVisibility(View.VISIBLE);
                    orderAdapter.notifyDataSetChanged();
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String username = sessionManager.getUsername();
        loadAllOrders(username);
    }
}
