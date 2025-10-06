package com.delaroystudios.pizza.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.ImageButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.adapters.OrderAdapter;
import com.delaroystudios.pizza.database.PizzaData;
import com.delaroystudios.pizza.models.Order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminOrdersActivity extends Activity implements OrderAdapter.OnOrderClickListener {

    private RecyclerView rvOrders;
    private EditText etSearch;
    private Spinner spinnerStatus;
    private TextView tvTotalOrders;
    private LinearLayout tvEmptyState; // The container
    private TextView tvEmptyTitle;     // The empty state title (new)
    private TextView tvEmptyCaption;   // The empty state caption (new)

    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private List<Order> filteredOrderList;
    private PizzaData database;
    private String currentStatusFilter = "all";

    // Status options for the spinner
    private final String[] statusOptions = {"All", "Pending", "In Progress", "Completed", "Cancelled"};
    private final String[] statusValues = {"all", "pending", "in_progress", "completed", "cancelled"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        database = new PizzaData(this);

        initViews();
        setupRecyclerView();
        setupSearchAndFilters();
        loadOrders();
    }

    private void initViews() {
        rvOrders = findViewById(R.id.rv_orders);
        etSearch = findViewById(R.id.et_search);
        spinnerStatus = findViewById(R.id.spinner_status);
        tvTotalOrders = findViewById(R.id.tv_total_orders);
        tvEmptyState = findViewById(R.id.tv_empty_state);

        // Find the inner TextViews using the IDs added to the XML
        tvEmptyTitle = findViewById(R.id.empty_state_title);
        tvEmptyCaption = findViewById(R.id.empty_state_caption);

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Setup status spinner
        setupStatusSpinner();
    }

    private void setupStatusSpinner() {
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                statusOptions
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
    }

    private void setupRecyclerView() {
        orderList = new ArrayList<>();
        filteredOrderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(filteredOrderList, this, this, true);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(orderAdapter);
    }

    private void setupSearchAndFilters() {
        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterOrders();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Status filter spinner
        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentStatusFilter = statusValues[position];
                filterOrders();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void loadOrders() {
        // Now call the actual database method
        orderList = getAllOrdersFromDatabase();
        filterOrders();
        updateUI();
    }

    private List<Order> getAllOrdersFromDatabase() {
        List<Order> orders = new ArrayList<>();

        try {
            // FIX 1: Use the actual implemented database method.
            orders = database.getAllOrders();

        } catch (Exception e) {
            // Only show a message on a genuine error
            showMessage("Error loading orders: " + e.getMessage());
        }

        return orders;
    }

    private void filterOrders() {
        filteredOrderList.clear();
        String searchQuery = etSearch.getText().toString().toLowerCase().trim();

        for (Order order : orderList) {
            boolean matchesSearch = searchQuery.isEmpty() ||
                    order.getCustomerName().toLowerCase().contains(searchQuery) ||
                    order.getCustomerPhone().contains(searchQuery) ||
                    String.valueOf(order.getOrderId()).contains(searchQuery);

            boolean matchesStatus = currentStatusFilter.equals("all") ||
                    order.getStatus().equals(currentStatusFilter);

            if (matchesSearch && matchesStatus) {
                filteredOrderList.add(order);
            }
        }

        orderAdapter.notifyDataSetChanged();
        updateUI();
    }

    private void updateUI() {
        tvTotalOrders.setText("Total: " + filteredOrderList.size() + " orders");

        if (filteredOrderList.isEmpty()) {
            rvOrders.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);

            // Logic to set dynamic text using the TextView fields
            if (orderList.isEmpty()) {
                tvEmptyTitle.setText("No orders found yet");
                tvEmptyCaption.setText("Start accepting orders to see them here.");
            } else {
                tvEmptyTitle.setText("No orders match your filters");
                tvEmptyCaption.setText("Try adjusting the search or status filter.");
            }
        } else {
            rvOrders.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onOrderClick(Order order) {
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("order_id", order.getOrderId());
        startActivity(intent);
    }

    @Override
    public void onUpdateStatus(Order order, String newStatus) {
        // Call the actual database method
        boolean success = updateOrderStatusInDatabase(order.getOrderId(), newStatus);

        if (success) {
            order.setStatus(newStatus);
            // Re-run filter and updateUI to apply status change and potential filter changes
            loadOrders();
            showMessage("Order #" + order.getOrderId() + " status updated to " + getStatusDisplayName(newStatus));
        } else {
            showMessage("Failed to update order status");
        }
    }

    private boolean updateOrderStatusInDatabase(int orderId, String newStatus) {
        try {
            // FIX 2: Use the actual implemented database method.
            return database.updateOrderStatus(orderId, newStatus);

        } catch (Exception e) {
            showMessage("Error updating order status: " + e.getMessage());
            return false;
        }
    }

    private String getStatusDisplayName(String status) {
        switch (status.toLowerCase()) {
            case "pending": return "Pending";
            case "in_progress": return "In Progress";
            case "completed": return "Completed";
            case "cancelled": return "Cancelled";
            default: return status;
        }
    }

    @Override
    public void onViewDetails(Order order) {
        onOrderClick(order);
    }

    public void onStatusFilterChanged(String status) {
        currentStatusFilter = status;
        filterOrders();
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders(); // Refresh orders when returning to this activity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }
}