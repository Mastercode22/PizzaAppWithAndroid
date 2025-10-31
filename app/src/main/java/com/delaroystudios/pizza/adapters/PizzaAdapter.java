package com.delaroystudios.pizza.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.models.Pizza;

import java.util.List;

public class PizzaAdapter extends RecyclerView.Adapter<PizzaAdapter.PizzaViewHolder> {

    private List<Pizza> pizzaList;
    private OnPizzaClickListener listener;

    public interface OnPizzaClickListener {
        void onAddToCart(Pizza pizza, String size);
        void onPizzaClick(Pizza pizza);
    }

    public PizzaAdapter(List<Pizza> pizzaList, OnPizzaClickListener listener) {
        this.pizzaList = pizzaList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PizzaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pizza, parent, false);
        return new PizzaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PizzaViewHolder holder, int position) {
        Pizza pizza = pizzaList.get(position);

        holder.tvPizzaName.setText(pizza.getName());
        holder.tvPizzaDescription.setText(pizza.getDescription());
        holder.tvPizzaPrice.setText("GHS " + String.format("%.2f", pizza.getBasePrice()));

        // CRITICAL FIX: Use the image resource directly from the Pizza object
        // The Pizza object already has the correct image set from the database
        try {
            if (pizza.getImageResource() != 0) {
                holder.ivPizza.setImageResource(pizza.getImageResource());
            } else {
                // Only use fallback if no image is set at all
                holder.ivPizza.setImageResource(R.drawable.mozzarella);
            }
        } catch (Exception e) {
            // If image resource is invalid, use default
            holder.ivPizza.setImageResource(R.drawable.mozzarella);
        }

        // Click on entire card to view pizza details
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPizzaClick(pizza);
            }
        });

        // Add to Cart button - defaults to Medium size
        holder.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCart(pizza, "M");
            }
        });
    }

    @Override
    public int getItemCount() {
        return pizzaList.size();
    }

    /**
     * Update the pizza list and refresh the adapter
     */
    public void updatePizzaList(List<Pizza> newPizzaList) {
        this.pizzaList = newPizzaList;
        notifyDataSetChanged();
    }

    /**
     * Clear all pizzas from the list
     */
    public void clearPizzas() {
        this.pizzaList.clear();
        notifyDataSetChanged();
    }

    public static class PizzaViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPizza;
        TextView tvPizzaName, tvPizzaDescription, tvPizzaPrice;
        Button btnAddToCart;

        public PizzaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPizza = itemView.findViewById(R.id.iv_pizza);
            tvPizzaName = itemView.findViewById(R.id.tv_pizza_name);
            tvPizzaDescription = itemView.findViewById(R.id.tv_pizza_description);
            tvPizzaPrice = itemView.findViewById(R.id.tv_pizza_price);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
        }
    }
}