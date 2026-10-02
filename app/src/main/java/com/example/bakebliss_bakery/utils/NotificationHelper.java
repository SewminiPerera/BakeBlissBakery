package com.example.bakebliss_bakery.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.bakebliss_bakery.R;
import com.example.bakebliss_bakery.activities.MyOrderActivity;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";
    public static final String CHANNEL_ID = "bakebliss_order_channel";
    public static final String CHANNEL_NAME = "Bake Bliss Order Updates";
    public static final String CHANNEL_DESC = "Notifications for order confirmation and delivery updates";

    /**
     * Ensure the notification channel is registered on Android 8.0+ (API 26+).
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            channel.enableLights(true);
            channel.setLightColor(Color.parseColor("#FF5722"));
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 300, 200, 300});

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Send a rich payment successful notification to the user.
     *
     * @param context       Context
     * @param orderId       Generated order ID (e.g. #BB-123456)
     * @param grandTotal    Total amount paid
     * @param estimatedTime Estimated delivery duration (e.g. "20-30 minutes")
     */
    public static void sendPaymentSuccessNotification(Context context, String orderId, double grandTotal, String estimatedTime) {
        if (context == null) return;

        createNotificationChannel(context);

        // Intent to open MyOrderActivity when user taps the notification
        Intent intent = new Intent(context, MyOrderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) System.currentTimeMillis(),
                intent,
                pendingIntentFlags
        );

        String title = "🎉 Payment Successful!";
        String shortMessage = String.format("Payment was successful! Your order will be received in %s.", estimatedTime);

        String bigTextMessage = String.format(
                "Payment of Rs. %.2f was successful!\n" +
                "Order #%s is confirmed and being freshly prepared.\n" +
                "⏱️ Your order will be received in %s.\n" +
                "Thank you for choosing Bake Bliss Bakery!",
                grandTotal, orderId, estimatedTime
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(title)
                .setContentText(shortMessage)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigTextMessage))
                .setColor(Color.parseColor("#FF5722"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Check POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Cannot post notification.");
                return;
            }
        }

        try {
            int notificationId = (int) (System.currentTimeMillis() % 100000);
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Payment success notification posted successfully: ID " + notificationId);
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException while posting notification: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Failed to post notification: " + e.getMessage());
        }
    }
}
