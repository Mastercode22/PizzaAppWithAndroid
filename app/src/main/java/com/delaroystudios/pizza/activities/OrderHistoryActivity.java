package com.delaroystudios.pizza.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.adapters.OrderHistoryAdapter;
import com.delaroystudios.pizza.database.PizzaData;
import com.delaroystudios.pizza.models.Order;
import com.delaroystudios.pizza.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import static com.delaroystudios.pizza.database.Constants.*;

public class OrderHistoryActivity extends AppCompatActivity {

    private static final String TAG = "OrderHistoryActivity";
    private RecyclerView rvOrderHistory;
    private LinearLayout llEmptyOrders;
    private Button btnOrderNow;
    private OrderHistoryAdapter orderAdapter;
    private List<Order> orderList;
    private PizzaData database;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        database = new PizzaData(this);
        sessionManager = new SessionManager(this);

        setupToolbar();
        initViews();
        setupRecyclerView();
        loadOrderHistory();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        rvOrderHistory = findViewById(R.id.rv_order_history);
        llEmptyOrders = findViewById(R.id.tv_empty_orders);
        btnOrderNow = findViewById(R.id.btn_order_now);

        btnOrderNow.setOnClickListener(v -> {
            // Navigate to main activity or menu
            finish(); // Or start your main/menu activity
        });
    }

    private void setupRecyclerView() {
        orderList = new ArrayList<>();
        orderAdapter = new OrderHistoryAdapter(orderList, this);
        rvOrderHistory.setLayoutManager(new LinearLayoutManager(this));
        rvOrderHistory.setAdapter(orderAdapter);
    }

    private void loadOrderHistory() {
        int userId = sessionManager.getCurrentUserId();
        if (userId == -1) {
            Log.e(TAG, "User not logged in");
            showEmptyState();
            return;
        }

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = database.getReadableDatabase();
            String selection = USER_ID + "=?";
            String[] selectionArgs = {String.valueOf(userId)};
            String orderBy = ORDER_DATE + " DESC";

            cursor = db.query(ORDERS_TABLE, null, selection, selectionArgs, null, null, orderBy);

            orderList.clear();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        Order order = new Order();
                        order.setOrderId(cursor.getInt(cursor.getColumnIndexOrThrow(ORDER_ID)));
                        order.setOrderDate(cursor.getString(cursor.getColumnIndexOrThrow(ORDER_DATE)));
                        order.setTotalAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(TOTAL_AMOUNT)));
                        order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(STATUS)));
                        order.setCustomerName(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_NAME)));
                        orderList.add(order);
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "Column not found: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }

            updateUI();

        } catch (Exception e) {
            Log.e(TAG, "Error loading order history: " + e.getMessage());
            showEmptyState();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void updateUI() {
        if (orderList.isEmpty()) {
            showEmptyState();
        } else {
            llEmptyOrders.setVisibility(View.GONE);
            rvOrderHistory.setVisibility(View.VISIBLE);
            orderAdapter.notifyDataSetChanged();
        }
    }

    private void showEmptyState() {
        llEmptyOrders.setVisibility(View.VISIBLE);
        rvOrderHistory.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}