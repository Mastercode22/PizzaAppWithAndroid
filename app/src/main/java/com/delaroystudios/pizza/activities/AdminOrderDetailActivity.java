package com.delaroystudios.pizza.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.adapters.OrderItemAdapter;
import com.delaroystudios.pizza.adapters.OrderAdapter;
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

    private void loadOrderDetails() {
        currentOrder = databaseHelper.getOrderById(orderId);
        if (currentOrder != null) {
            tvOrderId.setText("Order #" + currentOrder.getOrderId());
            tvOrderDate.setText(currentOrder.getFormattedOrderDate());
            tvCustomerInfo.setText(currentOrder.getCustomerName() + "\n" + currentOrder.getCustomerPhone());
            tvTotal.setText(String.format("GHS %.2f", currentOrder.getTotalAmount()));
            tvStatus.setText(currentOrder.getStatusDisplayName());
        }
    }

    private void loadOrderItems() {
        List<OrderItem> items = databaseHelper.getOrderItems(orderId);
        itemAdapter = new OrderItemAdapter(items, this);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        rvOrderItems.setAdapter(itemAdapter);
    }

    private void updateOrderStatus(View view) {
        String newStatus = spinnerStatus.getSelectedItem().toString();
        boolean success = databaseHelper.updateOrderStatus(orderId, newStatus);

        if (success) {
            currentOrder.setStatus(newStatus);
            tvStatus.setText(currentOrder.getStatusDisplayName());
            android.widget.Toast.makeText(this, "Status updated successfully", android.widget.Toast.LENGTH_SHORT).show();
        } else {
            android.widget.Toast.makeText(this, "Failed to update status", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}