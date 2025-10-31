package com.delaroystudios.pizza.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.adapters.OrderItemAdapter;
import com.delaroystudios.pizza.models.OrderItem;
import com.delaroystudios.pizza.database.PizzaData;
import com.delaroystudios.pizza.models.Order;
import com.delaroystudios.pizza.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import static com.delaroystudios.pizza.database.Constants.*;

public class OrderDetailActivity extends Activity {

    private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvCustomerName;
    private TextView tvCustomerPhone, tvDeliveryAddress, tvSpecialInstructions;
    private TextView tvSubtotal, tvTax, tvTotal;
    private RecyclerView rvOrderItems;
    private Button btnUpdateStatus;

    private PizzaData database;
    private SessionManager sessionManager;
    private int orderId;
    private Order currentOrder;
    private List<OrderItem> orderItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        database = new PizzaData(this);
        sessionManager = new SessionManager(this);

        orderId = getIntent().getIntExtra("order_id", -1);
        if (orderId == -1) {
            Toast.makeText(this, "Invalid order", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadOrderDetails();
        loadOrderItems();
    }

    private void initViews() {
        tvOrderId = findViewById(R.id.tv_order_id);
        tvOrderDate = findViewById(R.id.tv_order_date);
        tvOrderStatus = findViewById(R.id.tv_order_status);
        tvCustomerName = findViewById(R.id.tv_customer_name);
        tvCustomerPhone = findViewById(R.id.tv_customer_phone);
        tvDeliveryAddress = findViewById(R.id.tv_delivery_address);
        tvSpecialInstructions = findViewById(R.id.tv_special_instructions);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvTax = findViewById(R.id.tv_tax);
        tvTotal = findViewById(R.id.tv_total);
        rvOrderItems = findViewById(R.id.rv_order_items);
        btnUpdateStatus = findViewById(R.id.btn_update_status);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Update status button click
        btnUpdateStatus.setOnClickListener(v -> showUpdateStatusDialog());
    }

    private void loadOrderDetails() {
        SQLiteDatabase db = database.getReadableDatabase();
        String selection = ORDER_ID + "=?";
        String[] selectionArgs = {String.valueOf(orderId)};

        Cursor cursor = db.query(ORDERS_TABLE, null, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            currentOrder = new Order();
            currentOrder.setOrderId(cursor.getInt(cursor.getColumnIndex(ORDER_ID)));
            currentOrder.setOrderDate(cursor.getString(cursor.getColumnIndex(ORDER_DATE)));
            currentOrder.setStatus(cursor.getString(cursor.getColumnIndex(STATUS)));
            currentOrder.setCustomerName(cursor.getString(cursor.getColumnIndex(CUSTOMER_NAME)));
            currentOrder.setCustomerPhone(cursor.getString(cursor.getColumnIndex(CUSTOMER_PHONE)));
            currentOrder.setDeliveryAddress(cursor.getString(cursor.getColumnIndex(DELIVERY_ADDRESS)));
            currentOrder.setSpecialInstructions(cursor.getString(cursor.getColumnIndex(SPECIAL_INSTRUCTIONS)));
            currentOrder.setTotalAmount(cursor.getDouble(cursor.getColumnIndex(TOTAL_AMOUNT)));

            updateUI();
        } else {
            Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
            finish();
        }
        cursor.close();
    }

    private void updateUI() {
        tvOrderId.setText("Order #" + currentOrder.getOrderId());
        tvOrderDate.setText(currentOrder.getFormattedOrderDate());
        tvOrderStatus.setText(currentOrder.getStatusDisplayName());
        tvCustomerName.setText(currentOrder.getCustomerName());
        tvCustomerPhone.setText(currentOrder.getCustomerPhone());
        tvDeliveryAddress.setText(currentOrder.getDeliveryAddress());

        String instructions = currentOrder.getSpecialInstructions();
        if (instructions != null && !instructions.isEmpty()) {
            tvSpecialInstructions.setText(instructions);
            findViewById(R.id.ll_special_instructions).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.ll_special_instructions).setVisibility(View.GONE);
        }

        // Set status color
        int statusColor = getStatusColor(currentOrder.getStatus());
        tvOrderStatus.setTextColor(statusColor);

        // Calculate and display totals
        double subtotal = currentOrder.calculateSubtotal();
        double tax = currentOrder.calculateTax();
        double total = currentOrder.getTotalAmount();

        tvSubtotal.setText(String.format("GHS %.2f", subtotal));
        tvTax.setText(String.format("GHS %.2f", tax));
        tvTotal.setText(String.format("GHS %.2f", total));

        // Update button based on status
        updateStatusButton();
    }

    private void updateStatusButton() {
        String status = currentOrder.getStatus().toLowerCase().trim();

        // Hide button for completed or cancelled orders
        if (status.equals("completed") || status.equals("cancelled")) {
            btnUpdateStatus.setVisibility(View.GONE);
        } else {
            btnUpdateStatus.setVisibility(View.VISIBLE);
        }
    }

    private void showUpdateStatusDialog() {
        String currentStatus = currentOrder.getStatus().toLowerCase().trim();

        // Options based on current status
        final String[] options;
        final String[] values;

        if (currentStatus.equals("pending")) {
            options = new String[]{"Mark as In Progress", "Cancel Order"};
            values = new String[]{"in_progress", "cancelled"};
        } else if (currentStatus.equals("in_progress")) {
            options = new String[]{"Mark as Completed", "Cancel Order"};
            values = new String[]{"completed", "cancelled"};
        } else {
            Toast.makeText(this, "Cannot update this order", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Order Status");
        builder.setItems(options, (dialog, which) -> {
            updateStatus(values[which]);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateStatus(String newStatus) {
        try {
            boolean success = database.updateOrderStatus(orderId, newStatus);

            if (success) {
                currentOrder.setStatus(newStatus);
                updateUI();
                Toast.makeText(this, "Order updated to " + getStatusName(newStatus),
                        Toast.LENGTH_SHORT).show();

                // Notify previous activity
                setResult(RESULT_OK);
            } else {
                Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private String getStatusName(String status) {
        switch (status.toLowerCase()) {
            case "pending": return "Pending";
            case "in_progress": return "In Progress";
            case "completed": return "Completed";
            case "cancelled": return "Cancelled";
            default: return status;
        }
    }

    private void loadOrderItems() {
        orderItems = new ArrayList<>();
        SQLiteDatabase db = database.getReadableDatabase();

        String query = "SELECT oi.*, p." + PIZZA_NAME + ", s." + SIZE_NAME + ", c." + CRUST_NAME +
                " FROM " + ORDER_ITEMS_TABLE + " oi" +
                " LEFT JOIN " + PIZZAS_TABLE + " p ON oi." + PIZZA_ID + " = p." + PIZZA_ID +
                " LEFT JOIN " + SIZES_TABLE + " s ON oi." + SIZE_ID + " = s." + SIZE_ID +
                " LEFT JOIN " + CRUST_TABLE + " c ON oi." + CRUST_ID + " = c." + CRUST_ID +
                " WHERE oi." + ORDER_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(orderId)});

        while (cursor.moveToNext()) {
            OrderItem item = new OrderItem();
            item.setItemId(cursor.getInt(cursor.getColumnIndex(ITEM_ID)));
            item.setPizzaId(cursor.getInt(cursor.getColumnIndex(PIZZA_ID)));
            item.setQuantity(cursor.getInt(cursor.getColumnIndex(QUANTITY)));
            item.setItemPrice(cursor.getDouble(cursor.getColumnIndex(ITEM_PRICE)));
            item.setPizzaName(cursor.getString(cursor.getColumnIndex(PIZZA_NAME)));
            item.setSizeName(cursor.getString(cursor.getColumnIndex(SIZE_NAME)));
            item.setCrustName(cursor.getString(cursor.getColumnIndex(CRUST_NAME)));
            item.setSpecialInstructions(cursor.getString(cursor.getColumnIndex(SPECIAL_INSTRUCTIONS)));

            orderItems.add(item);
        }
        cursor.close();

        // Setup RecyclerView
        OrderItemAdapter adapter = new OrderItemAdapter(orderItems, this);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        rvOrderItems.setAdapter(adapter);
    }

    private int getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "pending":
                return getResources().getColor(R.color.status_pending);
            case "in_progress":
                return getResources().getColor(R.color.status_in_progress);
            case "completed":
                return getResources().getColor(R.color.status_completed);
            case "cancelled":
                return getResources().getColor(R.color.status_cancelled);
            default:
                return getResources().getColor(R.color.success_light);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }
}