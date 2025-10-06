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

        // Fetch pizza details from database
        SQLiteDatabase db = database.getReadableDatabase();

        // Query to get all necessary information
        String query = "SELECT p." + PIZZA_NAME + ", p." + BASE_PRICE + ", " +
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
                String sizeName = cursor.getString(2);
                double sizeMultiplier = cursor.getDouble(3);
                String crustName = cursor.getString(4);
                double crustPrice = cursor.getDouble(5);

                // Calculate final price
                double itemPrice = (basePrice * sizeMultiplier) + crustPrice;

                // Set pizza name
                holder.tvPizzaName.setText(pizzaName);

                // Set size and crust details
                holder.tvPizzaDetails.setText(sizeName + " • " + crustName + " Crust");

                // Set price
                holder.tvPizzaPrice.setText("GHS " + String.format("%.2f", itemPrice));

                // Set pizza image based on name
                holder.ivPizza.setImageResource(getPizzaImageResource(pizzaName));

                // Set quantity
                holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

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

            } catch (Exception e) {
                Log.e(TAG, "Error binding cart item: " + e.getMessage());
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

    private int getPizzaImageResource(String pizzaName) {
        String name = pizzaName.toLowerCase();

        if (name.contains("margherita")) return R.drawable.mozzarella;
        if (name.contains("pepperoni")) return R.drawable.pepperoni;
        if (name.contains("hawaiian") || name.contains("pineapple") || name.contains("ocean"))
            return R.drawable.pineapple;
        if (name.contains("chicken")) return R.drawable.chicken;
        if (name.contains("veggie") || name.contains("vegetarian")) return R.drawable.greenpeppers;
        if (name.contains("mushroom")) return R.drawable.mushrooms;
        if (name.contains("bbq") || name.contains("bacon")) return R.drawable.bacon;
        if (name.contains("sausage") || name.contains("meat")) return R.drawable.sausage;
        if (name.contains("chilli") || name.contains("supreme")) return R.drawable.greenpeppers;

        return R.drawable.mozzarella; // Default image
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
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}