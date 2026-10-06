package com.example.bakebliss_bakery.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bakebliss_bakery.R;
import com.example.bakebliss_bakery.activities.FoodDetailActivity;
import com.example.bakebliss_bakery.models.FoodModel;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private Context context;
    private List<FoodModel> foodList;

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;
        ImageView imgFood;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_food_name);
            tvPrice = itemView.findViewById(R.id.tv_food_price);
            imgFood = itemView.findViewById(R.id.img_food);
        }
    }

    public FoodAdapter(Context context, List<FoodModel> foodList) {
        this.context = context;
        this.foodList = foodList;
    }

    public void updateList(List<FoodModel> filteredList) {
        this.foodList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodModel food = foodList.get(position);

        holder.tvName.setText(food.getName());
        holder.tvPrice.setText(String.format("Rs. %.2f", food.getPrice()));

        String imageUrl = food.getImageUrl();
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                // Load remote Firebase Storage image on background thread
                holder.imgFood.setImageResource(getImageResource(food.getName())); // placeholder
                final String urlToLoad = imageUrl;
                new Thread(() -> {
                    try {
                        java.net.URL url = new java.net.URL(urlToLoad);
                        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                        conn.setDoInput(true);
                        conn.connect();
                        android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeStream(conn.getInputStream());
                        if (bmp != null) {
                            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                            mainHandler.post(() -> holder.imgFood.setImageBitmap(bmp));
                        }
                    } catch (Exception e) {
                        android.util.Log.e("FoodAdapter", "Error loading image: " + e.getMessage());
                    }
                }).start();
            } else {
                try {
                    holder.imgFood.setImageURI(android.net.Uri.parse(imageUrl));
                } catch (Exception e) {
                    holder.imgFood.setImageResource(getImageResource(food.getName()));
                }
            }
        } else {
            holder.imgFood.setImageResource(getImageResource(food.getName()));
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FoodDetailActivity.class);
            intent.putExtra("FOOD_ID", food.getId());
            intent.putExtra("FOOD_NAME", food.getName());
            intent.putExtra("FOOD_DESC", food.getDescription());
            intent.putExtra("FOOD_PRICE", food.getPrice());
            intent.putExtra("FOOD_IMAGE", food.getImageUrl());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
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
