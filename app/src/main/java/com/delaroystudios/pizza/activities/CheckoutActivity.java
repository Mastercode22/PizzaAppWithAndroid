package com.delaroystudios.pizza.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.adapters.CartAdapter;
import com.delaroystudios.pizza.database.PizzaData;
import com.delaroystudios.pizza.models.CartItem;
import com.delaroystudios.pizza.models.User;
import com.delaroystudios.pizza.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import static com.delaroystudios.pizza.database.Constants.*;

public class CheckoutActivity extends Activity implements View.OnClickListener {

    private RecyclerView rvOrderSummary;
    private EditText etDeliveryAddress, etSpecialInstructions, etPhoneNumber;
    private TextView tvSubtotal, tvTax, tvTotal, tvCustomerName;
    private Button btnPlaceOrder, btnCancel;

    private PizzaData database;
    private SessionManager sessionManager;
    private List<CartItem> cartItems;
    private double subtotal = 0.0;
    private double tax = 0.0;
    private double total = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        database = new PizzaData(this);
        sessionManager = new SessionManager(this);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupClickListeners();
        loadCartItems();
        populateCustomerInfo();
        calculateTotals();
    }

    private void initViews() {
        rvOrderSummary = findViewById(R.id.rv_order_summary);
        etDeliveryAddress = findViewById(R.id.et_delivery_address);
        etSpecialInstructions = findViewById(R.id.et_special_instructions);
        etPhoneNumber = findViewById(R.id.et_phone_number);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvTax = findViewById(R.id.tv_tax);
        tvTotal = findViewById(R.id.tv_total);
        tvCustomerName = findViewById(R.id.tv_customer_name);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    private void setupClickListeners() {
        btnPlaceOrder.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    private void loadCartItems() {
        int userId = sessionManager.getCurrentUserId();
        cartItems = new ArrayList<>();

        SQLiteDatabase db = database.getReadableDatabase();
        String selection = USER_ID + "=?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(CART_TABLE, null, selection, selectionArgs, null, null, ADDED_AT + " DESC");

        while (cursor.moveToNext()) {
            CartItem item = new CartItem();
            item.setCartId(cursor.getInt(cursor.getColumnIndex(CART_ID)));
            item.setPizzaId(cursor.getInt(cursor.getColumnIndex(PIZZA_ID)));
            item.setQuantity(cursor.getInt(cursor.getColumnIndex(QUANTITY)));

            // Get pizza details
            loadPizzaDetails(item);
            cartItems.add(item);
        }
        cursor.close();

        // Setup RecyclerView
        CartAdapter adapter = new CartAdapter(cartItems, this, null);
        rvOrderSummary.setLayoutManager(new LinearLayoutManager(this));
        rvOrderSummary.setAdapter(adapter);
    }

    private void loadPizzaDetails(CartItem item) {
        SQLiteDatabase db = database.getReadableDatabase();
        String selection = PIZZA_ID + "=?";
        String[] selectionArgs = {String.valueOf(item.getPizzaId())};

        Cursor cursor = db.query(PIZZAS_TABLE, null, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            item.setPizzaName(cursor.getString(cursor.getColumnIndex(PIZZA_NAME)));
            item.setBasePrice(cursor.getDouble(cursor.getColumnIndex(BASE_PRICE)));
        }
        cursor.close();
    }

    private void populateCustomerInfo() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null) {
            tvCustomerName.setText(currentUser.getFullName());
            etPhoneNumber.setText(currentUser.getPhone());
            etDeliveryAddress.setText(currentUser.getAddress());
        }
    }

    private void calculateTotals() {
        subtotal = 0.0;
        for (CartItem item : cartItems) {
            subtotal += item.getTotalPrice();
        }

        tax = subtotal * 0.1; // 10% tax
        total = subtotal + tax;

        tvSubtotal.setText(String.format("GHS %.2f", subtotal));
        tvTax.setText(String.format("GHS %.2f", tax));
        tvTotal.setText(String.format("GHS %.2f", total));
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.btn_place_order) {
            placeOrder();
        } else if (viewId == R.id.btn_cancel) {
            finish();
        }
    }

    private void placeOrder() {
        // Validate input
        String deliveryAddress = etDeliveryAddress.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();

        if (TextUtils.isEmpty(deliveryAddress)) {
            etDeliveryAddress.setError("Delivery address is required");
            return;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            etPhoneNumber.setError("Phone number is required");
            return;
        }

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create order
        int userId = sessionManager.getCurrentUserId();
        User currentUser = sessionManager.getCurrentUser();

        SQLiteDatabase db = database.getWritableDatabase();
        db.beginTransaction();

        try {
            // Insert order
            ContentValues orderValues = new ContentValues();
            orderValues.put(USER_ID, userId);
            orderValues.put(CUSTOMER_NAME, currentUser.getFullName());
            orderValues.put(CUSTOMER_PHONE, phoneNumber);
            orderValues.put(DELIVERY_ADDRESS, deliveryAddress);
            orderValues.put(SPECIAL_INSTRUCTIONS, etSpecialInstructions.getText().toString().trim());
            orderValues.put(TOTAL_AMOUNT, total);
            orderValues.put(STATUS, STATUS_PENDING);

            long orderId = db.insert(ORDERS_TABLE, null, orderValues);

            if (orderId != -1) {
                // Insert order items
                for (CartItem cartItem : cartItems) {
                    ContentValues itemValues = new ContentValues();
                    itemValues.put(ORDER_ID, orderId);
                    itemValues.put(PIZZA_ID, cartItem.getPizzaId());
                    itemValues.put(SIZE_ID, cartItem.getSizeId());
                    itemValues.put(CRUST_ID, cartItem.getCrustId());
                    itemValues.put(QUANTITY, cartItem.getQuantity());
                    itemValues.put(ITEM_PRICE, cartItem.getBasePrice());

                    db.insert(ORDER_ITEMS_TABLE, null, itemValues);
                }

                // Clear cart
                String whereClause = USER_ID + "=?";
                String[] whereArgs = {String.valueOf(userId)};
                db.delete(CART_TABLE, whereClause, whereArgs);

                db.setTransactionSuccessful();

                // Show success message
                Toast.makeText(this, "Order placed successfully! Order ID: " + orderId, Toast.LENGTH_LONG).show();

                // Go to order confirmation or main menu
                Intent intent = new Intent(this, PizzaMenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

            } else {
                Toast.makeText(this, "Failed to place order", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error placing order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
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