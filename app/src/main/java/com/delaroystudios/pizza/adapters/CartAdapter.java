package com.delaroystudios.pizza.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.models.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private Context context;
    private OnCartItemClickListener listener;

    public interface OnCartItemClickListener {
        void onUpdateQuantity(CartItem item, int newQuantity);
        void onRemoveItem(CartItem item);
    }

    public CartAdapter(List<CartItem> cartItems, Context context, OnCartItemClickListener listener) {
        this.cartItems = cartItems;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        // You'll need to fetch pizza details using pizza_id
        holder.tvPizzaName.setText("Pizza Name"); // TODO: Fetch from database
        holder.tvPizzaPrice.setText("GHS 15.99"); // TODO: Calculate actual price
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        holder.btnIncrease.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() + 1;
            listener.onUpdateQuantity(item, newQuantity);
        });

        holder.btnDecrease.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() - 1;
            if (newQuantity > 0) {
                listener.onUpdateQuantity(item, newQuantity);
            } else {
                listener.onRemoveItem(item);
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            listener.onRemoveItem(item);
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPizza;
        TextView tvPizzaName, tvPizzaPrice, tvQuantity;
        Button btnDecrease, btnIncrease, btnRemove;

        public CartViewHolder(View itemView) {
            super(itemView);
            ivPizza = itemView.findViewById(R.id.iv_pizza);
            tvPizzaName = itemView.findViewById(R.id.tv_pizza_name);
            tvPizzaPrice = itemView.findViewById(R.id.tv_pizza_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}