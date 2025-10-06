package com.delaroystudios.pizza.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.models.User;

import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {

    private List<User> customerList;

    public CustomerAdapter(List<User> customerList) {
        this.customerList = customerList;
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        User customer = customerList.get(position);

        holder.tvCustomerName.setText(customer.getFullName());
        holder.tvEmail.setText(customer.getEmail());
        holder.tvPhone.setText(customer.getPhone() != null ? customer.getPhone() : "No phone");
        holder.tvUsername.setText("@" + customer.getUsername());
        holder.tvOrderCount.setText(customer.getOrderCount() + " orders");
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    public static class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerName, tvEmail, tvPhone, tvUsername, tvOrderCount;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvOrderCount = itemView.findViewById(R.id.tv_order_count);
        }
    }
}