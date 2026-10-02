package com.example.bakebliss_bakery.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bakebliss_bakery.MainActivity;
import com.example.bakebliss_bakery.R;
import com.example.bakebliss_bakery.models.CartModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderConfirmActivity extends AppCompatActivity {

    ImageView btnBackOrderConfirm;
    TextView tvEstimatedDeliveryTime;
    TextView tvPaymentStatusBadge;
    TextView tvReceiptOrderId;
    TextView tvReceiptDate;
    TextView tvReceiptPaymentMethod;
    TextView tvReceiptAddress;
    LinearLayout layoutReceiptItems;
    TextView tvReceiptSubtotal;
    TextView tvReceiptDelivery;
    TextView tvReceiptTotal;
    Button btnViewOrders;
    Button btnGoHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_confirm);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Bind views
        btnBackOrderConfirm      = findViewById(R.id.btnBackOrderConfirm);
        tvEstimatedDeliveryTime = findViewById(R.id.tvEstimatedDeliveryTime);
        tvPaymentStatusBadge    = findViewById(R.id.tvPaymentStatusBadge);
        tvReceiptOrderId        = findViewById(R.id.tvReceiptOrderId);
        tvReceiptDate           = findViewById(R.id.tvReceiptDate);
        tvReceiptPaymentMethod  = findViewById(R.id.tvReceiptPaymentMethod);
        tvReceiptAddress        = findViewById(R.id.tvReceiptAddress);
        layoutReceiptItems      = findViewById(R.id.layoutReceiptItems);
        tvReceiptSubtotal       = findViewById(R.id.tvReceiptSubtotal);
        tvReceiptDelivery       = findViewById(R.id.tvReceiptDelivery);
        tvReceiptTotal          = findViewById(R.id.tvReceiptTotal);
        btnViewOrders           = findViewById(R.id.btnViewOrders);
        btnGoHome               = findViewById(R.id.btnGoHome);

        // Receive intent extras with safe defaults
        Intent intent = getIntent();

        String orderId = intent.getStringExtra("ORDER_ID");
        if (orderId == null || orderId.isEmpty()) {
            orderId = "BB-" + (100000 + (int) (Math.random() * 900000));
        }

        String orderDate = intent.getStringExtra("ORDER_DATE");
        if (orderDate == null || orderDate.isEmpty()) {
            orderDate = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date());
        }

        String estimatedTime = intent.getStringExtra("ESTIMATED_TIME");
        if (estimatedTime == null || estimatedTime.isEmpty()) {
            estimatedTime = "20 – 30 Minutes";
        }

        String paymentMethod = intent.getStringExtra("PAYMENT_METHOD");
        if (paymentMethod == null || paymentMethod.isEmpty()) {
            paymentMethod = "Credit / Debit Card";
        }

        String address = intent.getStringExtra("DELIVERY_ADDRESS");
        if (address == null || address.isEmpty()) {
            address = "Standard Delivery Address";
        }

        double subtotal = intent.getDoubleExtra("SUBTOTAL", 0.0);
        double deliveryFee = intent.getDoubleExtra("DELIVERY_FEE", 300.0);
        double grandTotal = intent.getDoubleExtra("GRAND_TOTAL", subtotal + deliveryFee);

        @SuppressWarnings("unchecked")
        List<CartModel> cartList = (List<CartModel>) intent.getSerializableExtra("CART_LIST");
        if (cartList == null) {
            cartList = new ArrayList<>();
        }

        // Populate receipt UI
        tvReceiptOrderId.setText("#" + orderId);
        tvReceiptDate.setText(orderDate);
        tvReceiptPaymentMethod.setText(paymentMethod);
        tvReceiptAddress.setText(address);
        tvEstimatedDeliveryTime.setText(estimatedTime);

        boolean isCod = paymentMethod.toLowerCase().contains("cash");
        if (isCod) {
            tvPaymentStatusBadge.setText("● CASH ON DELIVERY");
            tvPaymentStatusBadge.setTextColor(Color.parseColor("#E65100"));
            tvPaymentStatusBadge.setBackgroundColor(Color.parseColor("#FFF3E0"));
        } else {
            tvPaymentStatusBadge.setText("● PAID");
            tvPaymentStatusBadge.setTextColor(Color.parseColor("#2E7D32"));
            tvPaymentStatusBadge.setBackgroundResource(R.drawable.bg_paid_badge);
        }

        tvReceiptSubtotal.setText(String.format(Locale.getDefault(), "Rs. %.2f", subtotal));
        tvReceiptDelivery.setText(String.format(Locale.getDefault(), "Rs. %.2f", subtotal > 0 ? deliveryFee : 0.0));
        tvReceiptTotal.setText(String.format(Locale.getDefault(), "Rs. %.2f", grandTotal));

        // Dynamically add purchased items to the bill
        populateReceiptItems(cartList);

        // Buttons listeners
        if (btnBackOrderConfirm != null) {
            btnBackOrderConfirm.setOnClickListener(v -> navigateToHome());
        }

        btnGoHome.setOnClickListener(v -> navigateToHome());

        btnViewOrders.setOnClickListener(v -> {
            Intent ordersIntent = new Intent(OrderConfirmActivity.this, MyOrderActivity.class);
            ordersIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(ordersIntent);
            finish();
        });

        // Window Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_order_confirm_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void populateReceiptItems(List<CartModel> cartList) {
        layoutReceiptItems.removeAllViews();

        if (cartList.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Bakery order items");
            tvEmpty.setTextColor(Color.parseColor("#757575"));
            tvEmpty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            layoutReceiptItems.addView(tvEmpty);
            return;
        }

        for (CartModel item : cartList) {
            LinearLayout itemRow = new LinearLayout(this);
            itemRow.setOrientation(LinearLayout.HORIZONTAL);
            itemRow.setPadding(0, dpToPx(5), 0, dpToPx(5));

            TextView tvItemName = new TextView(this);
            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            tvItemName.setLayoutParams(nameParams);
            tvItemName.setText(item.getFoodName());
            tvItemName.setTextColor(Color.parseColor("#212121"));
            tvItemName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);

            TextView tvItemQty = new TextView(this);
            LinearLayout.LayoutParams qtyParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvItemQty.setLayoutParams(qtyParams);
            tvItemQty.setText("x " + item.getQuantity() + "   ");
            tvItemQty.setTextColor(Color.parseColor("#757575"));
            tvItemQty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);

            TextView tvItemPrice = new TextView(this);
            LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvItemPrice.setLayoutParams(priceParams);
            tvItemPrice.setText(String.format(Locale.getDefault(), "Rs. %.2f", item.getPrice() * item.getQuantity()));
            tvItemPrice.setTextColor(Color.parseColor("#212121"));
            tvItemPrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            tvItemPrice.setTypeface(null, Typeface.BOLD);

            itemRow.addView(tvItemName);
            itemRow.addView(tvItemQty);
            itemRow.addView(tvItemPrice);
            layoutReceiptItems.addView(itemRow);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void navigateToHome() {
        Intent intent = new Intent(OrderConfirmActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
