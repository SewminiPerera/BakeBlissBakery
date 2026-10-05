package com.example.bakebliss_bakery.adapters;

import android.content.Context;
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
import com.example.bakebliss_bakery.models.CartModel;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    Context context;
    List<CartModel> cartList;
    DBHelper dbHelper;
    CartUpdateListener listener;

    public interface CartUpdateListener {
        void onCartUpdated();
    }

    public CartAdapter(Context context, List<CartModel> cartList, DBHelper dbHelper, CartUpdateListener listener) {
        this.context = context;
        this.cartList = cartList;
        this.dbHelper = dbHelper;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartModel cartItem = cartList.get(position);

        holder.tvCartFoodName.setText(cartItem.getFoodName());
        holder.tvCartFoodPrice.setText(String.format("Rs. %.2f", cartItem.getPrice()));
        holder.tvCartQuantity.setText(String.valueOf(cartItem.getQuantity()));

        holder.imgCartFood.setImageResource(getImageResource(cartItem.getFoodName()));

        // Increase Quantity Button
        holder.btnCartPlus.setOnClickListener(v -> {
            int newQty = cartItem.getQuantity() + 1;
            cartItem.setQuantity(newQty);
            dbHelper.updateCartQuantity(cartItem.getCartId(), newQty);
            holder.tvCartQuantity.setText(String.valueOf(newQty));
            if (listener != null) listener.onCartUpdated();
        });

        // Decrease Quantity Button
        holder.btnCartMinus.setOnClickListener(v -> {
            int currentQty = cartItem.getQuantity();
            if (currentQty > 1) {
                int newQty = currentQty - 1;
                cartItem.setQuantity(newQty);
                dbHelper.updateCartQuantity(cartItem.getCartId(), newQty);
                holder.tvCartQuantity.setText(String.valueOf(newQty));
                if (listener != null) listener.onCartUpdated();
            } else {
                dbHelper.removeFromCart(cartItem.getCartId());
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && pos < cartList.size()) {
                    cartList.remove(pos);
                    notifyItemRemoved(pos);
                    notifyItemRangeChanged(pos, cartList.size());
                }
                if (listener != null) listener.onCartUpdated();
                Toast.makeText(context, "Item removed from cart", Toast.LENGTH_SHORT).show();
            }
        });

        // Direct Delete Button
        holder.btnDeleteCartItem.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(context)
                    .setTitle("Remove Item")
                    .setMessage("Remove \"" + cartItem.getFoodName() + "\" from your cart?")
                    .setPositiveButton("Remove", (dialog, which) -> {
                        dbHelper.removeFromCart(cartItem.getCartId());
                        int pos = holder.getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION && pos < cartList.size()) {
                            cartList.remove(pos);
                            notifyItemRemoved(pos);
                            notifyItemRangeChanged(pos, cartList.size());
                        }
                        if (listener != null) listener.onCartUpdated();
                        Toast.makeText(context, "Item removed from cart", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCartFood, btnDeleteCartItem;
        TextView tvCartFoodName, tvCartFoodPrice, tvCartQuantity, btnCartMinus, btnCartPlus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCartFood = itemView.findViewById(R.id.imgCartFood);
            btnDeleteCartItem = itemView.findViewById(R.id.btnDeleteCartItem);
            tvCartFoodName = itemView.findViewById(R.id.tvCartFoodName);
            tvCartFoodPrice = itemView.findViewById(R.id.tvCartFoodPrice);
            tvCartQuantity = itemView.findViewById(R.id.tvCartQuantity);
            btnCartMinus = itemView.findViewById(R.id.btnCartMinus);
            btnCartPlus = itemView.findViewById(R.id.btnCartPlus);
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
