package com.delaroystudios.pizza.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.models.Pizza;

import java.util.List;

public class AdminPizzaAdapter extends RecyclerView.Adapter<AdminPizzaAdapter.AdminPizzaViewHolder> {

    private List<Pizza> pizzaList;
    private OnAdminPizzaActionListener listener;

    public interface OnAdminPizzaActionListener {
        void onEditPizza(Pizza pizza);
        void onDeletePizza(Pizza pizza);
        void onToggleAvailability(Pizza pizza);
    }

    public AdminPizzaAdapter(List<Pizza> pizzaList, OnAdminPizzaActionListener listener) {
        this.pizzaList = pizzaList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminPizzaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_pizza, parent, false);
        return new AdminPizzaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminPizzaViewHolder holder, int position) {
        Pizza pizza = pizzaList.get(position);

        holder.tvPizzaName.setText(pizza.getName());
        holder.tvPizzaDescription.setText(pizza.getDescription());
        holder.tvPizzaPrice.setText("GHS " + String.format("%.2f", pizza.getBasePrice()));
        holder.tvCategory.setText(getCategoryName(pizza.getCategoryId()));
        holder.switchAvailable.setChecked(pizza.isAvailable());

        // Edit button
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditPizza(pizza);
            }
        });

        // Delete button
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeletePizza(pizza);
            }
        });

        // Availability switch
        holder.switchAvailable.setOnClickListener(v -> {
            if (listener != null) {
                listener.onToggleAvailability(pizza);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pizzaList.size();
    }

    private String getCategoryName(int categoryId) {
        switch (categoryId) {
            case 1: return "Classic";
            case 2: return "Specialty";
            case 3: return "Vegetarian";
            default: return "Unknown";
        }
    }

    public static class AdminPizzaViewHolder extends RecyclerView.ViewHolder {
        TextView tvPizzaName, tvPizzaDescription, tvPizzaPrice, tvCategory;
        Switch switchAvailable;
        Button btnEdit, btnDelete;

        public AdminPizzaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPizzaName = itemView.findViewById(R.id.tv_pizza_name);
            tvPizzaDescription = itemView.findViewById(R.id.tv_pizza_description);
            tvPizzaPrice = itemView.findViewById(R.id.tv_pizza_price);
            tvCategory = itemView.findViewById(R.id.tv_category);
            switchAvailable = itemView.findViewById(R.id.switch_available);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}