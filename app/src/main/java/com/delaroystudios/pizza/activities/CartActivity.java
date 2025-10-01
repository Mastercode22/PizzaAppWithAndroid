package com.delaroystudios.pizza.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.adapters.CartAdapter;
import com.delaroystudios.pizza.database.PizzaData;
import com.delaroystudios.pizza.models.CartItem;
import com.delaroystudios.pizza.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import static com.delaroystudios.pizza.database.Constants.*;

public class CartActivity extends Activity implements CartAdapter.OnCartItemClickListener {

    private static final String TAG = "CartActivity";

    private RecyclerView rvCart;
    private LinearLayout llEmptyCart;  // Changed from TextView to LinearLayout
    private TextView tvSubtotal, tvTotal;
    private Button btnCheckout;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    private PizzaData database;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        database = new PizzaData(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupRecyclerView();
        loadCartItems();
        updateTotals();
    }

    private void initViews() {
        rvCart = findViewById(R.id.rv_cart);
        llEmptyCart = findViewById(R.id.tv_empty_cart);  // Now correctly cast to LinearLayout
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvTotal = findViewById(R.id.tv_total);
        btnCheckout = findViewById(R.id.btn_checkout);

        btnCheckout.setOnClickListener(v -> proceedToCheckout());
    }

    private void setupRecyclerView() {
        cartItems = new ArrayList<>();
        cartAdapter = new CartAdapter(cartItems, this, this);
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(cartAdapter);
    }

    private void loadCartItems() {
        int userId = sessionManager.getCurrentUserId();
        if (userId == -1) {
            Log.e(TAG, "User not logged in");
            updateUI();
            return;
        }

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = database.getReadableDatabase();
            String selection = USER_ID + "=?";
            String[] selectionArgs = {String.valueOf(userId)};

            cursor = db.query(CART_TABLE, null, selection, selectionArgs, null, null, ADDED_AT + " DESC");

            cartItems.clear();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        CartItem item = new CartItem();
                        item.setCartId(cursor.getInt(cursor.getColumnIndexOrThrow(CART_ID)));
                        item.setPizzaId(cursor.getInt(cursor.getColumnIndexOrThrow(PIZZA_ID)));
                        item.setSizeId(cursor.getInt(cursor.getColumnIndexOrThrow(SIZE_ID)));
                        item.setCrustId(cursor.getInt(cursor.getColumnIndexOrThrow(CRUST_ID)));
                        item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(QUANTITY)));
                        cartItems.add(item);
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "Column not found: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }

            updateUI();

        } catch (Exception e) {
            Log.e(TAG, "Error loading cart items: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void updateUI() {
        if (cartItems.isEmpty()) {
            rvCart.setVisibility(View.GONE);
            llEmptyCart.setVisibility(View.VISIBLE);
            btnCheckout.setEnabled(false);
        } else {
            rvCart.setVisibility(View.VISIBLE);
            llEmptyCart.setVisibility(View.GONE);
            btnCheckout.setEnabled(true);
            cartAdapter.notifyDataSetChanged();
        }
    }

    private void updateTotals() {
        // Calculate totals
        double subtotal = calculateSubtotal();
        double tax = subtotal * 0.1; // 10% tax
        double total = subtotal + tax;

        tvSubtotal.setText("GHS " + String.format("%.2f", subtotal));
        tvTotal.setText("GHS " + String.format("%.2f", total));
    }

    private double calculateSubtotal() {
        // TODO: Calculate actual subtotal from cart items with real prices
        // This is a placeholder calculation
        double subtotal = 0.0;
        for (CartItem item : cartItems) {
            // You should fetch the actual price from database based on pizza, size, and crust
            subtotal += 15.99 * item.getQuantity();
        }
        return subtotal;
    }

    @Override
    public void onUpdateQuantity(CartItem item, int newQuantity) {
        if (newQuantity <= 0) {
            onRemoveItem(item);
            return;
        }

        // Update quantity in database
        SQLiteDatabase db = database.getWritableDatabase();
        String whereClause = CART_ID + "=?";
        String[] whereArgs = {String.valueOf(item.getCartId())};

        android.content.ContentValues values = new android.content.ContentValues();
        values.put(QUANTITY, newQuantity);

        db.update(CART_TABLE, values, whereClause, whereArgs);

        item.setQuantity(newQuantity);
        cartAdapter.notifyDataSetChanged();
        updateTotals();
    }

    @Override
    public void onRemoveItem(CartItem item) {
        // Remove item from database
        SQLiteDatabase db = database.getWritableDatabase();
        String whereClause = CART_ID + "=?";
        String[] whereArgs = {String.valueOf(item.getCartId())};

        db.delete(CART_TABLE, whereClause, whereArgs);

        // Remove from list
        cartItems.remove(item);
        cartAdapter.notifyDataSetChanged();
        updateUI();
        updateTotals();
    }

    private void proceedToCheckout() {
        if (cartItems.isEmpty()) {
            return;
        }

        Intent intent = new Intent(this, CheckoutActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload cart when returning to this activity
        loadCartItems();
        updateTotals();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }
}