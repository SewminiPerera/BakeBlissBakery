package com.example.bakebliss_bakery.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bakebliss_bakery.R;
import com.example.bakebliss_bakery.models.OrderModel;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    Context context;
    List<OrderModel> orderList;

    public OrderAdapter(Context context, List<OrderModel> orderList) {
        this.context = context;
        this.orderList = orderList;
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
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgOrderFood;
        TextView tvOrderFoodName, tvOrderPriceQty, tvOrderStatus, tvOrderDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgOrderFood = itemView.findViewById(R.id.imgOrderFood);
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
