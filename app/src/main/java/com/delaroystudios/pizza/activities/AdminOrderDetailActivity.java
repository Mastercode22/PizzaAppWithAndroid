package com.delaroystudios.pizza.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.adapters.OrderItemAdapter;
import com.delaroystudios.pizza.database.DatabaseHelper;
import com.delaroystudios.pizza.models.Order;
import com.delaroystudios.pizza.models.OrderItem;

import java.util.List;

public class AdminOrderDetailActivity extends Activity {

    private TextView tvOrderId, tvOrderDate, tvCustomerInfo, tvTotal, tvStatus;
    private Spinner spinnerStatus;
    private Button btnUpdateStatus, btnBack;
    private RecyclerView rvOrderItems;

    private DatabaseHelper databaseHelper;
    private int orderId;
    private Order currentOrder;
    private OrderItemAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_detail);

        orderId = getIntent().getIntExtra("order_id", -1);
        if (orderId == -1) {
            finish();
            return;
        }

        databaseHelper = new DatabaseHelper(this);
        initViews();
        loadOrderDetails();
        loadOrderItems();
        setupStatusSpinner();
    }

    private void initViews() {
        tvOrderId = findViewById(R.id.tv_order_id);
        tvOrderDate = findViewById(R.id.tv_order_date);
        tvCustomerInfo = findViewById(R.id.tv_customer_info);
        tvTotal = findViewById(R.id.tv_total);
        tvStatus = findViewById(R.id.tv_status);
        spinnerStatus = findViewById(R.id.spinner_status);
        btnUpdateStatus = findViewById(R.id.btn_update_status);
        btnBack = findViewById(R.id.btn_back);
        rvOrderItems = findViewById(R.id.rv_order_items);

        btnBack.setOnClickListener(v -> finish());
        btnUpdateStatus.setOnClickListener(this::updateOrderStatus);
    }

    private void setupStatusSpinner() {
        String[] statusOptions = {"Pending", "In Progress", "Completed", "Cancelled"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);
    }

    private void loadOrderDetails() {
        currentOrder = databaseHelper.getOrderById(orderId);
        if (currentOrder != null) {
            tvOrderId.setText("Order #" + currentOrder.getOrderId());
            tvOrderDate.setText(formatOrderDate(currentOrder.getOrderDate()));

            // Build customer info
            String customerInfo = currentOrder.getCustomerName();
            if (currentOrder.getCustomerPhone() != null && !currentOrder.getCustomerPhone().isEmpty()) {
                customerInfo += "\n📞 " + currentOrder.getCustomerPhone();
            }
            if (currentOrder.getDeliveryAddress() != null && !currentOrder.getDeliveryAddress().isEmpty()) {
                customerInfo += "\n📍 " + currentOrder.getDeliveryAddress();
            }
            tvCustomerInfo.setText(customerInfo);

            tvTotal.setText(String.format("GHS %.2f", currentOrder.getTotalAmount()));
            tvStatus.setText(getStatusDisplayName(currentOrder.getStatus()));

            // Set current status in spinner
            setSpinnerToCurrentStatus(currentOrder.getStatus());
        }
    }

    private String formatOrderDate(String dateString) {
        try {
            // Simple formatting - you can enhance this with proper date parsing
            if (dateString != null && !dateString.isEmpty()) {
                return "📋 " + dateString.replace("T", " at ").replace("-", "/");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "📋 Date not available";
    }

    private String getStatusDisplayName(String status) {
        switch (status) {
            case Order.STATUS_PENDING:
                return "⏳ Pending";
            case Order.STATUS_IN_PROGRESS:
                return "👨‍🍳 In Progress";
            case Order.STATUS_COMPLETED:
                return "✅ Completed";
            case Order.STATUS_CANCELLED:
                return "❌ Cancelled";
            default:
                return status;
        }
    }

    private String getStatusConstant(String displayName) {
        // FIX: Use the exact constants from Order model
        switch (displayName) {
            case "Pending":
                return Order.STATUS_PENDING;  // "pending"
            case "In Progress":
                return Order.STATUS_IN_PROGRESS; // "in_progress"
            case "Completed":
                return Order.STATUS_COMPLETED; // "completed" - This matches revenue queries!
            case "Cancelled":
                return Order.STATUS_CANCELLED; // "cancelled"
            default:
                return Order.STATUS_PENDING;
        }
    }

    private void setSpinnerToCurrentStatus(String status) {
        // FIX: Use the exact constants from Order model
        switch (status) {
            case Order.STATUS_PENDING:
                spinnerStatus.setSelection(0);
                break;
            case Order.STATUS_IN_PROGRESS:
                spinnerStatus.setSelection(1);
                break;
            case Order.STATUS_COMPLETED:
                spinnerStatus.setSelection(2);
                break;
            case Order.STATUS_CANCELLED:
                spinnerStatus.setSelection(3);
                break;
            default:
                spinnerStatus.setSelection(0);
        }
    }

    private void loadOrderItems() {
        List<OrderItem> items = databaseHelper.getOrderItems(orderId);
        if (items != null && !items.isEmpty()) {
            itemAdapter = new OrderItemAdapter(items, this);
            rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
            rvOrderItems.setAdapter(itemAdapter);

            // Update item count display
            updateItemCountDisplay(items.size());
        } else {
            // Show no items message
            TextView noItemsText = new TextView(this);
            noItemsText.setText("No items found for this order");
            noItemsText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            noItemsText.setPadding(0, 50, 0, 0);
            rvOrderItems.addView(noItemsText);
        }
    }

    private void updateItemCountDisplay(int itemCount) {
        // You can add this to your layout if needed
        String itemText = itemCount + " item" + (itemCount != 1 ? "s" : "");
        // If you have a TextView for item count, set it here
    }

    private void updateOrderStatus(View view) {
        String selectedStatus = spinnerStatus.getSelectedItem().toString();
        String newStatus = getStatusConstant(selectedStatus);

        boolean success = databaseHelper.updateOrderStatus(orderId, newStatus);

        if (success) {
            currentOrder.setStatus(newStatus);
            tvStatus.setText(getStatusDisplayName(newStatus));

            // Send broadcast to refresh dashboard
            sendStatusUpdateBroadcast();

            android.widget.Toast.makeText(this, "Status updated to: " + selectedStatus, android.widget.Toast.LENGTH_SHORT).show();
        } else {
            android.widget.Toast.makeText(this, "Failed to update status", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void sendStatusUpdateBroadcast() {
        // Send broadcast to refresh dashboard data
        Intent refreshIntent = new Intent("ORDER_STATUS_UPDATED");
        refreshIntent.putExtra("order_id", orderId);
        refreshIntent.putExtra("new_status", currentOrder.getStatus());
        sendBroadcast(refreshIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}