package com.example.bakebliss_bakery.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bakebliss_bakery.R;
import com.example.bakebliss_bakery.models.FoodModel;

import java.util.List;

public class AdminFoodAdapter extends RecyclerView.Adapter<AdminFoodAdapter.AdminFoodViewHolder> {

    public interface OnItemActionListener {
        void onEdit(FoodModel food);
        void onDelete(FoodModel food);
    }

    private final Context context;
    private final List<FoodModel> foodList;
    private final OnItemActionListener listener;

    public AdminFoodAdapter(Context context, List<FoodModel> foodList, OnItemActionListener listener) {
        this.context = context;
        this.foodList = foodList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminFoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_food, parent, false);
        return new AdminFoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminFoodViewHolder holder, int position) {
        FoodModel food = foodList.get(position);

        holder.tvAdminFoodName.setText(food.getName());
        holder.tvAdminFoodCategory.setText("📂 " + food.getCategory());
        holder.tvAdminFoodPrice.setText(String.format("Rs. %.2f", food.getPrice()));
        holder.tvAdminFoodDesc.setText(food.getDescription());

        if (food.getImageUrl() != null && !food.getImageUrl().trim().isEmpty()) {
            try {
                holder.imgAdminFoodThumb.setImageURI(Uri.parse(food.getImageUrl()));
            } catch (Exception e) {
                holder.imgAdminFoodThumb.setImageResource(getImageResource(food.getName()));
            }
        } else {
            holder.imgAdminFoodThumb.setImageResource(getImageResource(food.getName()));
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(food);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(food);
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public void updateList(List<FoodModel> newList) {
        foodList.clear();
        foodList.addAll(newList);
        notifyDataSetChanged();
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

    static class AdminFoodViewHolder extends RecyclerView.ViewHolder {
        TextView tvAdminFoodName, tvAdminFoodCategory, tvAdminFoodPrice, tvAdminFoodDesc;
        ImageView imgAdminFoodThumb;
        ImageButton btnEdit, btnDelete;

        public AdminFoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAdminFoodName      = itemView.findViewById(R.id.tvAdminFoodName);
            tvAdminFoodCategory  = itemView.findViewById(R.id.tvAdminFoodCategory);
            tvAdminFoodPrice     = itemView.findViewById(R.id.tvAdminFoodPrice);
            tvAdminFoodDesc      = itemView.findViewById(R.id.tvAdminFoodDesc);
            imgAdminFoodThumb    = itemView.findViewById(R.id.imgAdminFoodThumb);
            btnEdit              = itemView.findViewById(R.id.btnAdminEdit);
            btnDelete            = itemView.findViewById(R.id.btnAdminDelete);
        }
    }
}
