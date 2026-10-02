package com.example.bakebliss_bakery.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bakebliss_bakery.R;
import com.example.bakebliss_bakery.database.DBHelper;
import com.example.bakebliss_bakery.models.CartModel;
import com.example.bakebliss_bakery.utils.NotificationHelper;
import com.example.bakebliss_bakery.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {

    // Views
    ImageView btnBackPayment;
    TextView tvPaySubtotal, tvPayDelivery, tvPayTotal;
    TextView tabCard, tabCash;
    LinearLayout layoutCardFields, layoutCashNote;
    EditText etCardNumber, etCardHolder, etExpiry, etCvv, etDeliveryAddress;
    Button btnConfirmPayment;

    // Data
    DBHelper dbHelper;
    SessionManager sessionManager;
    List<CartModel> cartList;
    double subtotal = 0.0;
    double deliveryFee = 300.00;
    double grandTotal = 0.0;
    String username;
    boolean isCardPayment = true;

    // Notification permission launcher for Android 13+
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {});

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Create notification channel early
        NotificationHelper.createNotificationChannel(this);

        // Request POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // Bind views
        btnBackPayment    = findViewById(R.id.btnBackPayment);
        tvPaySubtotal     = findViewById(R.id.tvPaySubtotal);
        tvPayDelivery     = findViewById(R.id.tvPayDelivery);
        tvPayTotal        = findViewById(R.id.tvPayTotal);
        tabCard           = findViewById(R.id.tabCard);
        tabCash           = findViewById(R.id.tabCash);
        layoutCardFields  = findViewById(R.id.layoutCardFields);
        layoutCashNote    = findViewById(R.id.layoutCashNote);
        etCardNumber      = findViewById(R.id.etCardNumber);
        etCardHolder      = findViewById(R.id.etCardHolder);
        etExpiry          = findViewById(R.id.etExpiry);
        etCvv             = findViewById(R.id.etCvv);
        etDeliveryAddress = findViewById(R.id.etDeliveryAddress);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);

        dbHelper       = new DBHelper(this);
        sessionManager = new SessionManager(this);
        username       = sessionManager.getUsername();

        // Receive totals passed from CartActivity
        subtotal    = getIntent().getDoubleExtra("SUBTOTAL", 0.0);
        deliveryFee = getIntent().getDoubleExtra("DELIVERY_FEE", 300.0);
        grandTotal  = getIntent().getDoubleExtra("GRAND_TOTAL", 0.0);

        // Receive cart list
        cartList = new ArrayList<>();
        Object extra = getIntent().getSerializableExtra("CART_LIST");
        if (extra instanceof List) {
            //noinspection unchecked
            cartList = (List<CartModel>) extra;
        }

        updateSummaryDisplay();

        // Pre-fill delivery address from saved profile
        if (dbHelper != null && username != null) {
            dbHelper.getUserProfile(username, user -> runOnUiThread(() -> {
                if (user != null && user.getAddress() != null && !user.getAddress().equals("Not Set")) {
                    etDeliveryAddress.setText(user.getAddress());
                }
            }));
        }

        btnBackPayment.setOnClickListener(v -> finish());
        tabCard.setOnClickListener(v -> selectCardTab());
        tabCash.setOnClickListener(v -> selectCashTab());
        btnConfirmPayment.setOnClickListener(v -> handlePlaceOrder());

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main_payment_layout), (v, insets) -> {
                    Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                });
    }

    // ── Tab helpers ──────────────────────────────────────────────────────────

    private void updateSummaryDisplay() {
        tvPaySubtotal.setText(String.format(Locale.getDefault(), "Rs. %.2f", subtotal));
        tvPayDelivery.setText(String.format(Locale.getDefault(), "Rs. %.2f", subtotal > 0 ? deliveryFee : 0.0));
        tvPayTotal.setText(String.format(Locale.getDefault(), "Rs. %.2f", grandTotal));
        updateButtonLabel();
    }

    private void updateButtonLabel() {
        if (isCardPayment) {
            btnConfirmPayment.setText(String.format(Locale.getDefault(), "Pay Now  •  Rs. %.2f", grandTotal));
        } else {
            btnConfirmPayment.setText("Pay with Cash on Delivery");
        }
    }

    private void selectCardTab() {
        isCardPayment = true;
        tabCard.setBackgroundResource(R.drawable.bg_tab_selected);
        tabCard.setTextColor(Color.WHITE);
        tabCash.setBackgroundColor(Color.TRANSPARENT);
        tabCash.setTextColor(Color.parseColor("#757575"));
        layoutCardFields.setVisibility(View.VISIBLE);
        layoutCashNote.setVisibility(View.GONE);
        updateButtonLabel();
    }

    private void selectCashTab() {
        isCardPayment = false;
        tabCash.setBackgroundResource(R.drawable.bg_tab_selected);
        tabCash.setTextColor(Color.WHITE);
        tabCard.setBackgroundColor(Color.TRANSPARENT);
        tabCard.setTextColor(Color.parseColor("#757575"));
        layoutCashNote.setVisibility(View.VISIBLE);
        layoutCardFields.setVisibility(View.GONE);
        updateButtonLabel();
    }

    // ── Order placement ──────────────────────────────────────────────────────

    private void handlePlaceOrder() {
        // Validate delivery address
        String address = etDeliveryAddress.getText().toString().trim();
        if (TextUtils.isEmpty(address)) {
            etDeliveryAddress.setError("Please enter a delivery address");
            etDeliveryAddress.requestFocus();
            return;
        }

        // Validate card fields only when card payment is selected
        String cardNum = "";
        if (isCardPayment) {
            cardNum = etCardNumber.getText().toString().trim().replaceAll("\\s+", "");
            String cardName = etCardHolder.getText().toString().trim();
            String expiry   = etExpiry.getText().toString().trim();
            String cvv      = etCvv.getText().toString().trim();

            if (cardNum.length() < 16) {
                etCardNumber.setError("Enter a valid 16-digit card number");
                etCardNumber.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(cardName)) {
                etCardHolder.setError("Enter cardholder name");
                etCardHolder.requestFocus();
                return;
            }
            if (expiry.length() < 4) {
                etExpiry.setError("Enter valid expiry (MM/YY)");
                etExpiry.requestFocus();
                return;
            }
            if (cvv.length() < 3) {
                etCvv.setError("Enter valid CVV");
                etCvv.requestFocus();
                return;
            }
        }

        // Disable button while processing
        btnConfirmPayment.setEnabled(false);
        btnConfirmPayment.setText("Processing…");

        // Build order metadata
        final String finalPaymentMethod;
        if (isCardPayment) {
            String last4 = cardNum.length() >= 4 ? cardNum.substring(cardNum.length() - 4) : cardNum;
            finalPaymentMethod = "Credit / Debit Card (•••• " + last4 + ")";
        } else {
            finalPaymentMethod = "Cash on Delivery";
        }

        final String orderId        = "BB-" + (100000 + (int) (Math.random() * 900000));
        final String orderDate      = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date());
        final String estimatedTime  = "20–30 Minutes";
        final String deliveryAddr   = address;
        final String finalCardNum   = cardNum;

        // ── Try to save order to Firestore, but ALWAYS navigate to confirm ──
        Runnable navigateToConfirm = () -> runOnUiThread(() -> {
            btnConfirmPayment.setEnabled(true);
            updateButtonLabel();

            // Notification 1: Payment Successful
            NotificationHelper.sendPaymentSuccessNotification(
                    PaymentActivity.this, orderId, grandTotal, "20-30 minutes");

            // Notification 2: Order is processing
            NotificationHelper.sendOrderProcessingNotification(
                    PaymentActivity.this, orderId);

            // Go to receipt / confirmation page
            Intent intent = new Intent(PaymentActivity.this, OrderConfirmActivity.class);
            intent.putExtra("ORDER_ID",        orderId);
            intent.putExtra("ORDER_DATE",      orderDate);
            intent.putExtra("SUBTOTAL",        subtotal);
            intent.putExtra("DELIVERY_FEE",    subtotal > 0 ? deliveryFee : 0.0);
            intent.putExtra("GRAND_TOTAL",     grandTotal);
            intent.putExtra("DELIVERY_ADDRESS", deliveryAddr);
            intent.putExtra("PAYMENT_METHOD",  finalPaymentMethod);
            intent.putExtra("ESTIMATED_TIME",  estimatedTime);
            intent.putExtra("CART_LIST",       (java.io.Serializable) new ArrayList<>(cartList));
            // Clear the back stack so the user cannot go back to payment page
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Attempt Firestore save (fire-and-forget — we navigate regardless)
        if (cartList != null && !cartList.isEmpty() && username != null) {
            dbHelper.placeOrders(username, cartList, deliveryFee, null);
        }
        
        // Navigate immediately so the UI never hangs
        navigateToConfirm.run();
    }
}
