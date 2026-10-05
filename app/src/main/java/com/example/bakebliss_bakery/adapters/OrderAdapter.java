package com.example.bakebliss_bakery.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bakebliss_bakery.R;
import com.example.bakebliss_bakery.database.DBHelper;
import com.example.bakebliss_bakery.models.OrderModel;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    public interface OrderUpdateListener {
        void onOrderDeleted();
    }

    Context context;
    List<OrderModel> orderList;
    DBHelper dbHelper;
    String username;
    OrderUpdateListener listener;

    public OrderAdapter(Context context, List<OrderModel> orderList, DBHelper dbHelper, String username, OrderUpdateListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.dbHelper = dbHelper;
        this.username = username;
        this.listener = listener;
    }

    public OrderAdapter(Context context, List<OrderModel> orderList) {
        this(context, orderList, null, null, null);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderModel order = orderList.get(position);

        holder.tvOrderFoodName.setText(order.getFoodName());
        holder.tvOrderPriceQty.setText("Rs. " + order.getPrice() + "  |  Qty: " + order.getQuantity());

        holder.imgOrderFood.setImageResource(getImageResource(order.getFoodName()));

        holder.tvOrderStatus.setText(order.getStatus());

        if (order.getStatus().equalsIgnoreCase("Completed")) {
            holder.tvOrderStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            holder.tvOrderDate.setText(order.getOrderDate());
            holder.tvOrderDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvOrderStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
            holder.tvOrderDate.setVisibility(View.GONE);
        }

        // Delete single order button
        holder.btnDeleteOrder.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Order")
                    .setMessage("Are you sure you want to delete this order for \"" + order.getFoodName() + "\"?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (dbHelper != null && order.getOrderId() != null && !order.getOrderId().isEmpty()) {
                            dbHelper.deleteOrder(username, order.getOrderId(), success -> {});
                        }
                        int pos = holder.getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION && pos < orderList.size()) {
                            orderList.remove(pos);
                            notifyItemRemoved(pos);
                            notifyItemRangeChanged(pos, orderList.size());
                        }
                        if (listener != null) {
                            listener.onOrderDeleted();
                        }
                        Toast.makeText(context, "Order deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgOrderFood, btnDeleteOrder;
        TextView tvOrderFoodName, tvOrderPriceQty, tvOrderStatus, tvOrderDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgOrderFood = itemView.findViewById(R.id.imgOrderFood);
            btnDeleteOrder = itemView.findViewById(R.id.btnDeleteOrder);
            tvOrderFoodName = itemView.findViewById(R.id.tvOrderFoodName);
            tvOrderPriceQty = itemView.findViewById(R.id.tvOrderPriceQty);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
        }
    }

    private int getImageResource(String foodName) {
        if (foodName == null) return R.mipmap.ic_launcher;
        String cleanName = foodName.replace(" (50% OFF)", "");
        String imageName = "food_" + cleanName.toLowerCase()
                .replace(" ", "_")
                .replace("(", "")
                .replace(")", "")
                .replace("&", "and");
        int resId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        if (resId != 0) {
            return resId;
        }
        return R.mipmap.ic_launcher;
    }
}
