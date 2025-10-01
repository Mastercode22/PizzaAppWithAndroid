package com.delaroystudios.pizza.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.models.Order;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private Context context;
    private OnOrderClickListener onOrderClickListener;
    private boolean isAdminView;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
        void onUpdateStatus(Order order, String newStatus);
        void onViewDetails(Order order);
    }

    public OrderAdapter(List<Order> orderList, Context context, boolean isAdminView) {
        this.orderList = orderList;
        this.context = context;
        this.isAdminView = isAdminView;
        if (context instanceof OnOrderClickListener) {
            this.onOrderClickListener = (OnOrderClickListener) context;
        }
    }

    public OrderAdapter(List<Order> orderList, Context context, OnOrderClickListener listener, boolean isAdminView) {
        this.orderList = orderList;
        this.context = context;
        this.onOrderClickListener = listener;
        this.isAdminView = isAdminView;
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (isAdminView) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_order, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_history, parent, false);
        }
        return new OrderViewHolder(view, isAdminView);
    }

    @Override
    public void onBindViewHolder(OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvOrderId.setText("Order #" + order.getOrderId());
        holder.tvCustomerName.setText(order.getCustomerName());
        holder.tvOrderDate.setText(order.getFormattedOrderDate());
        holder.tvOrderTotal.setText(order.getFormattedTotal());
        holder.tvOrderStatus.setText(order.getStatusDisplayName());

        // Set status color
        int statusColor = getStatusColor(order.getStatus());
        holder.tvOrderStatus.setTextColor(statusColor);

        // Set background color based on status for admin view
        if (isAdminView) {
            setStatusBackground(holder.itemView, order.getStatus());
        }

        // Handle click events
        holder.itemView.setOnClickListener(v -> {
            if (onOrderClickListener != null) {
                onOrderClickListener.onOrderClick(order);
            }
        });

        // Admin-specific functionality
        if (isAdminView && holder.btnUpdateStatus != null) {
            setupStatusButton(holder.btnUpdateStatus, order);
        }

        if (holder.btnViewDetails != null) {
            holder.btnViewDetails.setOnClickListener(v -> {
                if (onOrderClickListener != null) {
                    onOrderClickListener.onViewDetails(order);
                }
            });
        }

        // Show customer phone for admin view
        if (isAdminView && holder.tvCustomerPhone != null) {
            holder.tvCustomerPhone.setText(order.getCustomerPhone());
        }

        // Show item count if available
        if (holder.tvItemCount != null && order.getItemCount() > 0) {
            holder.tvItemCount.setText(order.getItemCount() + " items");
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    private void setupStatusButton(Button btnUpdateStatus, Order order) {
        String nextStatus = getNextStatus(order.getStatus());
        if (nextStatus != null) {
            btnUpdateStatus.setText(getStatusButtonText(nextStatus));
            btnUpdateStatus.setVisibility(View.VISIBLE);
            btnUpdateStatus.setOnClickListener(v -> {
                if (onOrderClickListener != null) {
                    onOrderClickListener.onUpdateStatus(order, nextStatus);
                }
            });
        } else {
            btnUpdateStatus.setVisibility(View.GONE);
        }
    }

    private String getNextStatus(String currentStatus) {
        switch (currentStatus) {
            case Order.STATUS_PENDING:
                return Order.STATUS_IN_PROGRESS;
            case Order.STATUS_IN_PROGRESS:
                return Order.STATUS_COMPLETED;
            default:
                return null; // No next status available
        }
    }

    private String getStatusButtonText(String status) {
        switch (status) {
            case Order.STATUS_IN_PROGRESS:
                return "Start Preparation";
            case Order.STATUS_COMPLETED:
                return "Mark Complete";
            default:
                return "Update Status";
        }
    }

    private int getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case Order.STATUS_PENDING:
                return context.getResources().getColor(R.color.status_pending);
            case Order.STATUS_IN_PROGRESS:
                return context.getResources().getColor(R.color.status_in_progress);
            case Order.STATUS_COMPLETED:
                return context.getResources().getColor(R.color.status_completed);
            case Order.STATUS_CANCELLED:
                return context.getResources().getColor(R.color.status_cancelled);
            default:
                return context.getResources().getColor(R.color.secondary_text);
        }
    }

    private void setStatusBackground(View itemView, String status) {
        int backgroundColor;
        switch (status.toLowerCase()) {
            case Order.STATUS_PENDING:
                backgroundColor = context.getResources().getColor(R.color.status_pending_bg);
                break;
            case Order.STATUS_IN_PROGRESS:
                backgroundColor = context.getResources().getColor(R.color.status_in_progress_bg);
                break;
            case Order.STATUS_COMPLETED:
                backgroundColor = context.getResources().getColor(R.color.status_completed_bg);
                break;
            case Order.STATUS_CANCELLED:
                backgroundColor = context.getResources().getColor(R.color.status_cancelled_bg);
                break;
            default:
                backgroundColor = context.getResources().getColor(R.color.white);
                break;
        }
        // You can set a subtle background tint here if desired
        // itemView.setBackgroundColor(backgroundColor);
    }

    // Filter methods
    public void filterByStatus(String status) {
        // Implement filtering logic if needed
        notifyDataSetChanged();
    }

    public void updateOrderList(List<Order> newOrderList) {
        this.orderList = newOrderList;
        notifyDataSetChanged();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvCustomerName, tvOrderDate, tvOrderTotal, tvOrderStatus;
        TextView tvCustomerPhone, tvItemCount, tvDeliveryAddress;
        Button btnUpdateStatus, btnViewDetails;

        public OrderViewHolder(View itemView, boolean isAdminView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvOrderTotal = itemView.findViewById(R.id.tv_order_total);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);

            // Optional fields that may not exist in all layouts
            tvCustomerPhone = itemView.findViewById(R.id.tv_customer_phone);
            tvItemCount = itemView.findViewById(R.id.tv_item_count);
            tvDeliveryAddress = itemView.findViewById(R.id.tv_delivery_address);
            btnUpdateStatus = itemView.findViewById(R.id.btn_update_status);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
        }
    }
}

