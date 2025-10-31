package com.delaroystudios.pizza.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.database.PizzaData;
import com.delaroystudios.pizza.models.CartItem;

import java.util.List;

import static com.delaroystudios.pizza.database.Constants.*;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private static final String TAG = "CartAdapter";
    private List<CartItem> cartItems;
    private Context context;
    private OnCartItemClickListener listener;
    private PizzaData database;

    public interface OnCartItemClickListener {
        void onUpdateQuantity(CartItem item, int newQuantity);
        void onRemoveItem(CartItem item);
    }

    public CartAdapter(List<CartItem> cartItems, Context context, OnCartItemClickListener listener) {
        this.cartItems = cartItems;
        this.context = context;
        this.listener = listener;
        this.database = new PizzaData(context);
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        // Fetch pizza details from database INCLUDING image_resource
        SQLiteDatabase db = database.getReadableDatabase();

        // Updated query to include image_resource from pizzas table
        String query = "SELECT p." + PIZZA_NAME + ", p." + BASE_PRICE + ", p.image_resource, " +
                "s." + SIZE_NAME + ", s." + PRICE_MULTIPLIER + ", " +
                "c." + CRUST_NAME + ", c." + ADDITIONAL_PRICE +
                " FROM " + PIZZAS_TABLE + " p " +
                "INNER JOIN " + SIZES_TABLE + " s ON s." + SIZE_ID + " = ? " +
                "INNER JOIN " + CRUST_TABLE + " c ON c." + CRUST_ID + " = ? " +
                "WHERE p." + PIZZA_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(item.getSizeId()),
                String.valueOf(item.getCrustId()),
                String.valueOf(item.getPizzaId())
        });

        if (cursor != null && cursor.moveToFirst()) {
            try {
                // Get data from cursor
                String pizzaName = cursor.getString(0);
                double basePrice = cursor.getDouble(1);
                int imageResource = cursor.getInt(2);
                String sizeName = cursor.getString(3);
                double sizeMultiplier = cursor.getDouble(4);
                String crustName = cursor.getString(5);
                double crustPrice = cursor.getDouble(6);

                // Calculate final price
                double itemPrice = (basePrice * sizeMultiplier) + crustPrice;

                // Set pizza name
                holder.tvPizzaName.setText(pizzaName);

                // Set size and crust details
                holder.tvPizzaDetails.setText(sizeName + " • " + crustName + " Crust");

                // Set price
                holder.tvPizzaPrice.setText("GHS " + String.format("%.2f", itemPrice));

                // Set the ACTUAL pizza image from database
                if (imageResource != 0) {
                    holder.ivPizza.setImageResource(imageResource);
                } else {
                    // Fallback only if no image is stored
                    holder.ivPizza.setImageResource(R.drawable.mozzarella);
                    Log.w(TAG, "No image resource found for pizza: " + pizzaName);
                }

                // Set quantity
                holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

                // Only set up click listeners if listener is not null (i.e., not in checkout)
                if (listener != null) {
                    // Increase quantity button
                    holder.btnIncrease.setOnClickListener(v -> {
                        int currentPos = holder.getAdapterPosition();
                        if (currentPos != RecyclerView.NO_POSITION) {
                            CartItem currentItem = cartItems.get(currentPos);
                            listener.onUpdateQuantity(currentItem, currentItem.getQuantity() + 1);
                        }
                    });

                    // Decrease quantity button
                    holder.btnDecrease.setOnClickListener(v -> {
                        int currentPos = holder.getAdapterPosition();
                        if (currentPos != RecyclerView.NO_POSITION) {
                            CartItem currentItem = cartItems.get(currentPos);
                            if (currentItem.getQuantity() > 1) {
                                listener.onUpdateQuantity(currentItem, currentItem.getQuantity() - 1);
                            } else {
                                listener.onRemoveItem(currentItem);
                            }
                        }
                    });

                    // Remove button
                    holder.btnRemove.setOnClickListener(v -> {
                        int currentPos = holder.getAdapterPosition();
                        if (currentPos != RecyclerView.NO_POSITION) {
                            CartItem currentItem = cartItems.get(currentPos);
                            listener.onRemoveItem(currentItem);
                        }
                    });

                    // Make buttons visible and enabled
                    holder.btnIncrease.setVisibility(View.VISIBLE);
                    holder.btnDecrease.setVisibility(View.VISIBLE);
                    holder.btnRemove.setVisibility(View.VISIBLE);
                } else {
                    // Hide buttons in checkout view
                    holder.btnIncrease.setVisibility(View.GONE);
                    holder.btnDecrease.setVisibility(View.GONE);
                    holder.btnRemove.setVisibility(View.GONE);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error binding cart item: " + e.getMessage());
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        } else {
            if (cursor != null) {
                cursor.close();
            }
            Log.e(TAG, "Failed to load cart item details for pizza ID: " + item.getPizzaId());
        }
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPizza;
        TextView tvPizzaName, tvPizzaDetails, tvPizzaPrice, tvQuantity;
        Button btnIncrease, btnDecrease, btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPizza = itemView.findViewById(R.id.iv_pizza);
            tvPizzaName = itemView.findViewById(R.id.tv_pizza_name);
            tvPizzaDetails = itemView.findViewById(R.id.tv_pizza_details);
            tvPizzaPrice = itemView.findViewById(R.id.tv_pizza_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);

            // CRITICAL FIX: Initialize the buttons!
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}