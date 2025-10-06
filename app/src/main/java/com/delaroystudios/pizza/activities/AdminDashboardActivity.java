package com.delaroystudios.pizza.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.adapters.OrderAdapter;
import com.delaroystudios.pizza.database.DatabaseHelper;
import com.delaroystudios.pizza.database.PizzaData;
import com.delaroystudios.pizza.models.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.delaroystudios.pizza.database.Constants.*;

public class AdminDashboardActivity extends Activity implements View.OnClickListener, OrderAdapter.OnOrderClickListener {

    private TextView tvTotalOrders, tvTotalRevenue, tvPendingOrders;
    private TextView tvTodayOrders, tvWeekRevenue, tvTotalCustomers;
    private RecyclerView rvRecentOrders;
    private DatabaseHelper databaseHelper;
    private PizzaData pizzaData;
    private OrderAdapter recentOrdersAdapter;
    private List<Order> recentOrdersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        databaseHelper = new DatabaseHelper(this);
        pizzaData = new PizzaData(this);

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
        tvTodayOrders = findViewById(R.id.tv_today_orders);
        tvWeekRevenue = findViewById(R.id.tv_week_revenue);
        tvTotalCustomers = findViewById(R.id.tv_total_customers);
        rvRecentOrders = findViewById(R.id.rv_recent_orders);

        findViewById(R.id.btn_logout).setOnClickListener(this);
    }

    private void setupClickListeners() {
        findViewById(R.id.card_view_orders).setOnClickListener(this);
        findViewById(R.id.card_manage_pizzas).setOnClickListener(this);
        findViewById(R.id.card_view_customers).setOnClickListener(this);
        findViewById(R.id.card_settings).setOnClickListener(this);
        findViewById(R.id.card_total_orders).setOnClickListener(this);
        findViewById(R.id.card_revenue).setOnClickListener(this);
        findViewById(R.id.card_pending_orders).setOnClickListener(this);
    }

    private void setupRecentOrdersRecyclerView() {
        rvRecentOrders.setLayoutManager(new LinearLayoutManager(this));
        rvRecentOrders.setNestedScrollingEnabled(false);
    }

    private void loadDashboardData() {
        SQLiteDatabase db = pizzaData.getReadableDatabase();

        // Get today's date
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Total orders (all time)
        Cursor totalOrdersCursor = db.rawQuery("SELECT COUNT(*) FROM " + ORDERS_TABLE, null);
        int totalOrders = 0;
        if (totalOrdersCursor.moveToFirst()) {
            totalOrders = totalOrdersCursor.getInt(0);
        }
        totalOrdersCursor.close();

        // Today's orders
        Cursor todayOrdersCursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + ORDERS_TABLE +
                        " WHERE DATE(" + ORDER_DATE + ") = ?",
                new String[]{today}
        );
        int todayOrders = 0;
        if (todayOrdersCursor.moveToFirst()) {
            todayOrders = todayOrdersCursor.getInt(0);
        }
        todayOrdersCursor.close();

        // Total revenue (all time)
        Cursor totalRevenueCursor = db.rawQuery(
                "SELECT SUM(" + TOTAL_AMOUNT + ") FROM " + ORDERS_TABLE +
                        " WHERE " + STATUS + " != '" + STATUS_CANCELLED + "'",
                null
        );
        double totalRevenue = 0.0;
        if (totalRevenueCursor.moveToFirst()) {
            totalRevenue = totalRevenueCursor.getDouble(0);
        }
        totalRevenueCursor.close();

        // This week's revenue
        Cursor weekRevenueCursor = db.rawQuery(
                "SELECT SUM(" + TOTAL_AMOUNT + ") FROM " + ORDERS_TABLE +
                        " WHERE DATE(" + ORDER_DATE + ") >= DATE('now', '-7 days')" +
                        " AND " + STATUS + " != '" + STATUS_CANCELLED + "'",
                null
        );
        double weekRevenue = 0.0;
        if (weekRevenueCursor.moveToFirst()) {
            weekRevenue = weekRevenueCursor.getDouble(0);
        }
        weekRevenueCursor.close();

        // Pending orders
        int pendingOrders = databaseHelper.getPendingOrderCount();

        // Total customers
        Cursor customersCursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + USERS_TABLE +
                        " WHERE " + IS_ACTIVE + " = 1",
                null
        );
        int totalCustomers = 0;
        if (customersCursor.moveToFirst()) {
            totalCustomers = customersCursor.getInt(0);
        }
        customersCursor.close();

        // Update UI
        tvTotalOrders.setText(String.valueOf(totalOrders));
        tvTodayOrders.setText(String.valueOf(todayOrders));
        tvTotalRevenue.setText(String.format("GHS %.2f", totalRevenue));
        tvWeekRevenue.setText(String.format("GHS %.2f", weekRevenue));
        tvPendingOrders.setText(String.valueOf(pendingOrders));
        tvTotalCustomers.setText(String.valueOf(totalCustomers));
    }

    private void loadRecentOrders() {
        recentOrdersList = databaseHelper.getAllOrders();

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
            startActivity(new Intent(this, AdminPizzaManagementActivity.class));
        } else if (viewId == R.id.card_view_customers) {
            startActivity(new Intent(this, AdminCustomersActivity.class));
        } else if (viewId == R.id.card_settings) {
            startActivity(new Intent(this, AdminSettingsActivity.class));
        } else if (viewId == R.id.card_revenue) {
            startActivity(new Intent(this, ActivitydminRevenueA.class));
        } else if (viewId == R.id.btn_logout) {
            logout();
        }
    }

    @Override
    public void onOrderClick(Order order) {
        Intent intent = new Intent(this, AdminOrderDetailActivity.class);
        intent.putExtra("order_id", order.getOrderId());
        startActivity(intent);
    }

    @Override
    public void onUpdateStatus(Order order, String newStatus) {
        boolean success = databaseHelper.updateOrderStatus(order.getOrderId(), newStatus);

        if (success) {
            order.setStatus(newStatus);
            recentOrdersAdapter.notifyDataSetChanged();
            loadDashboardData();
            showMessage("Order status updated successfully");
        } else {
            showMessage("Failed to update order status");
        }
    }

    @Override
    public void onViewDetails(Order order) {
        onOrderClick(order);
    }

    private void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
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
        loadDashboardData();
        loadRecentOrders();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        if (pizzaData != null) {
            pizzaData.close();
        }
    }
}