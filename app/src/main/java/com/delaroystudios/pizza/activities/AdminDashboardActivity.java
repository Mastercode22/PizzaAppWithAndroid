
package com.delaroystudios.pizza.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.adapters.OrderAdapter;
import com.delaroystudios.pizza.database.DatabaseHelper;
import com.delaroystudios.pizza.models.Order;

import java.util.List;

public class AdminDashboardActivity extends Activity implements View.OnClickListener, OrderAdapter.OnOrderClickListener {

    private TextView tvTotalOrders, tvTotalRevenue, tvPendingOrders;
    private RecyclerView rvRecentOrders;
    private DatabaseHelper databaseHelper;
    private OrderAdapter recentOrdersAdapter;
    private List<Order> recentOrdersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupClickListeners();
        setupRecentOrdersRecyclerView();
        loadDashboardData();
        loadRecentOrders();
    }

    private void initViews() {
        tvTotalOrders = findViewById(R.id.tv_total_orders);
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);
        tvPendingOrders = findViewById(R.id.tv_pending_orders);
        rvRecentOrders = findViewById(R.id.rv_recent_orders);

        // Logout button in toolbar
        findViewById(R.id.btn_logout).setOnClickListener(this);
    }

    private void setupClickListeners() {
        // Quick action cards
        findViewById(R.id.card_view_orders).setOnClickListener(this);
        findViewById(R.id.card_manage_pizzas).setOnClickListener(this);
        findViewById(R.id.card_view_customers).setOnClickListener(this);
        findViewById(R.id.card_settings).setOnClickListener(this);

        // Statistics cards (make them clickable for detailed views)
        findViewById(R.id.card_total_orders).setOnClickListener(this);
        findViewById(R.id.card_revenue).setOnClickListener(this);
        findViewById(R.id.card_pending_orders).setOnClickListener(this);
    }

    private void setupRecentOrdersRecyclerView() {
        rvRecentOrders.setLayoutManager(new LinearLayoutManager(this));
        rvRecentOrders.setNestedScrollingEnabled(false); // Since it's inside a ScrollView
    }

    private void loadDashboardData() {
        // Load today's statistics
        int todayOrders = databaseHelper.getTodayOrderCount();
        double todayRevenue = databaseHelper.getTodayRevenue();
        int pendingOrders = databaseHelper.getPendingOrderCount();

        // Update UI
        tvTotalOrders.setText(String.valueOf(todayOrders));
        tvTotalRevenue.setText(String.format("GHS %.2f", todayRevenue));
        tvPendingOrders.setText(String.valueOf(pendingOrders));
    }

    private void loadRecentOrders() {
        // Load recent orders (last 10)
        recentOrdersList = databaseHelper.getAllOrders();

        // Limit to recent 10 orders for dashboard
        if (recentOrdersList.size() > 10) {
            recentOrdersList = recentOrdersList.subList(0, 10);
        }

        recentOrdersAdapter = new OrderAdapter(recentOrdersList, this, this, true);
        rvRecentOrders.setAdapter(recentOrdersAdapter);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        if (viewId == R.id.card_view_orders || viewId == R.id.card_total_orders || viewId == R.id.card_pending_orders) {
            startActivity(new Intent(this, AdminOrdersActivity.class));
        } else if (viewId == R.id.card_manage_pizzas) {
            // TODO: Create AdminPizzaManagementActivity
            showMessage("Pizza management coming soon");
        } else if (viewId == R.id.card_view_customers) {
            // TODO: Create AdminCustomersActivity
            showMessage("Customer management coming soon");
        } else if (viewId == R.id.card_settings) {
            // TODO: Create AdminSettingsActivity
            showMessage("Admin settings coming soon");
        } else if (viewId == R.id.card_revenue) {
            // TODO: Create RevenueReportActivity
            showMessage("Revenue reports coming soon");
        } else if (viewId == R.id.btn_logout) {
            logout();
        }
    }

    @Override
    public void onOrderClick(Order order) {
        // Navigate to order details
        Intent intent = new Intent(this, AdminOrderDetailActivity.class);
        intent.putExtra("order_id", order.getOrderId());
        startActivity(intent);
    }

    @Override
    public void onUpdateStatus(Order order, String newStatus) {
        // Update order status
        boolean success = databaseHelper.updateOrderStatus(order.getOrderId(), newStatus);

        if (success) {
            order.setStatus(newStatus);
            recentOrdersAdapter.notifyDataSetChanged();
            loadDashboardData(); // Refresh statistics
            showMessage("Order status updated successfully");
        } else {
            showMessage("Failed to update order status");
        }
    }

    @Override
    public void onViewDetails(Order order) {
        onOrderClick(order); // Same as clicking on the order
    }

    private void logout() {
        // Clear admin session (you'll need to implement admin session management)
        // For now, just go back to login
        Intent intent = new Intent(this, AdminLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showMessage(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to dashboard
        loadDashboardData();
        loadRecentOrders();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
