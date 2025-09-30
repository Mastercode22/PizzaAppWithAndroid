package com.delaroystudios.pizza.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.models.OrderItem;

import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    private List<OrderItem> orderItems;
    private Context context;

    public OrderItemAdapter(List<OrderItem> orderItems, Context context) {
        this.orderItems = orderItems;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_item, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderItem item = orderItems.get(position);

        holder.tvPizzaName.setText(item.getPizzaName());
        holder.tvQuantity.setText("Qty: " + item.getQuantity());
        holder.tvPrice.setText(String.format("GHS %.2f", item.getItemPrice()));

        // Build description with size and crust if available
        StringBuilder description = new StringBuilder();
        if (item.getSizeName() != null && !item.getSizeName().isEmpty()) {
            description.append("Size: ").append(item.getSizeName());
        }
        if (item.getCrustName() != null && !item.getCrustName().isEmpty()) {
            if (description.length() > 0) description.append(" • ");
            description.append("Crust: ").append(item.getCrustName());
        }

        if (description.length() > 0) {
            holder.tvDescription.setText(description.toString());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // Special instructions if available
        if (item.getSpecialInstructions() != null && !item.getSpecialInstructions().isEmpty()) {
            holder.tvSpecialInstructions.setText("Note: " + item.getSpecialInstructions());
            holder.tvSpecialInstructions.setVisibility(View.VISIBLE);
        } else {
            holder.tvSpecialInstructions.setVisibility(View.GONE);
        }

        // Calculate total for this item
        double totalPrice = item.getItemPrice() * item.getQuantity();
        holder.tvTotalPrice.setText(String.format("Total: GHS %.2f", totalPrice));
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    public static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvPizzaName, tvQuantity, tvPrice, tvDescription, tvSpecialInstructions, tvTotalPrice;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPizzaName = itemView.findViewById(R.id.tv_pizza_name);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvSpecialInstructions = itemView.findViewById(R.id.tv_special_instructions);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
        }
    }
}