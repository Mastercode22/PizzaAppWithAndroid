package com.delaroystudios.pizza.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

public class CheckoutActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView rvOrderSummary;
    private EditText etDeliveryAddress, etSpecialInstructions, etPhoneNumber;
    private TextView tvSubtotal, tvTax, tvTotal, tvCustomerName;
    private Button btnPlaceOrder, btnCancel;
    private ImageButton btnBack;

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
        btnBack = findViewById(R.id.btn_back);
    }

    private void setupClickListeners() {
        btnPlaceOrder.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnBack.setOnClickListener(this);
    }

    private void loadCartItems() {
        int userId = sessionManager.getCurrentUserId();
        cartItems = new ArrayList<>();

        SQLiteDatabase db = database.getReadableDatabase();
        String selection = USER_ID + "=?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(CART_TABLE, null, selection, selectionArgs, null, null, ADDED_AT + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                CartItem item = new CartItem();
                item.setCartId(cursor.getInt(cursor.getColumnIndexOrThrow(CART_ID)));
                item.setPizzaId(cursor.getInt(cursor.getColumnIndexOrThrow(PIZZA_ID)));
                item.setSizeId(cursor.getInt(cursor.getColumnIndexOrThrow(SIZE_ID)));
                item.setCrustId(cursor.getInt(cursor.getColumnIndexOrThrow(CRUST_ID)));
                item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(QUANTITY)));
                cartItems.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }

        // Setup RecyclerView (read-only, so pass null for listener)
        CartAdapter adapter = new CartAdapter(cartItems, this, null);
        rvOrderSummary.setLayoutManager(new LinearLayoutManager(this));
        rvOrderSummary.setAdapter(adapter);
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
        subtotal = calculateSubtotalFromDatabase();
        tax = subtotal * 0.1; // 10% tax
        total = subtotal + tax;

        tvSubtotal.setText(String.format("GHS %.2f", subtotal));
        tvTax.setText(String.format("GHS %.2f", tax));
        tvTotal.setText(String.format("GHS %.2f", total));
    }

    private double calculateSubtotalFromDatabase() {
        double calculatedSubtotal = 0.0;
        SQLiteDatabase db = database.getReadableDatabase();

        for (CartItem item : cartItems) {
            // Query to get pizza base price, size multiplier, and crust additional price
            String query = "SELECT p." + BASE_PRICE + ", s." + PRICE_MULTIPLIER + ", c." + ADDITIONAL_PRICE +
                    " FROM " + PIZZAS_TABLE + " p " +
                    "INNER JOIN " + SIZES_TABLE + " s ON s." + SIZE_ID + " = ? " +
                    "INNER JOIN " + CRUST_TABLE + " c ON c." + CRUST_ID + " = ? " +
                    "WHERE p." + PIZZA_ID + " = ?";

            Cursor cursor = db.rawQuery(query, new String[]{
                    String.valueOf(item.getSizeId()),
                    String.valueOf(item.getCrustId()),
                    String.valueOf(item.getPizzaId())
            });

            if (cursor != null && cursor.moveToFirst()) {
                try {
                    double basePrice = cursor.getDouble(0);
                    double sizeMultiplier = cursor.getDouble(1);
                    double crustPrice = cursor.getDouble(2);

                    // Calculate item price: (base price * size multiplier) + crust price
                    double itemPrice = (basePrice * sizeMultiplier) + crustPrice;
                    calculatedSubtotal += itemPrice * item.getQuantity();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            }
        }

        return calculatedSubtotal;
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.btn_place_order) {
            placeOrder();
        } else if (viewId == R.id.btn_cancel) {
            finish();
        } else if (viewId == R.id.btn_back) {
            // Navigate back to CartActivity
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
                // Insert order items with calculated prices from database
                for (CartItem cartItem : cartItems) {
                    // Get the actual price for this item
                    double itemPrice = getItemPrice(cartItem);

                    ContentValues itemValues = new ContentValues();
                    itemValues.put(ORDER_ID, orderId);
                    itemValues.put(PIZZA_ID, cartItem.getPizzaId());
                    itemValues.put(SIZE_ID, cartItem.getSizeId());
                    itemValues.put(CRUST_ID, cartItem.getCrustId());
                    itemValues.put(QUANTITY, cartItem.getQuantity());
                    itemValues.put(ITEM_PRICE, itemPrice);

                    db.insert(ORDER_ITEMS_TABLE, null, itemValues);
                }

                // Clear cart
                String whereClause = USER_ID + "=?";
                String[] whereArgs = {String.valueOf(userId)};
                db.delete(CART_TABLE, whereClause, whereArgs);

                db.setTransactionSuccessful();

                // Show detailed success message
                String pizzaCount = cartItems.size() == 1 ? "1 pizza" : cartItems.size() + " pizzas";
                Toast.makeText(this,
                        "Order #" + orderId + " placed successfully!\n" +
                                pizzaCount + " - Total: GHS " + String.format("%.2f", total),
                        Toast.LENGTH_LONG).show();

                // Go to main menu
                Intent intent = new Intent(this, PizzaMenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

            } else {
                Toast.makeText(this, "Failed to place order. Please try again.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error placing order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
        }
    }

    private double getItemPrice(CartItem item) {
        SQLiteDatabase db = database.getReadableDatabase();
        double itemPrice = 0.0;

        String query = "SELECT p." + BASE_PRICE + ", s." + PRICE_MULTIPLIER + ", c." + ADDITIONAL_PRICE +
                " FROM " + PIZZAS_TABLE + " p " +
                "INNER JOIN " + SIZES_TABLE + " s ON s." + SIZE_ID + " = ? " +
                "INNER JOIN " + CRUST_TABLE + " c ON c." + CRUST_ID + " = ? " +
                "WHERE p." + PIZZA_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(item.getSizeId()),
                String.valueOf(item.getCrustId()),
                String.valueOf(item.getPizzaId())
        });

        if (cursor != null && cursor.moveToFirst()) {
            try {
                double basePrice = cursor.getDouble(0);
                double sizeMultiplier = cursor.getDouble(1);
                double crustPrice = cursor.getDouble(2);
                itemPrice = (basePrice * sizeMultiplier) + crustPrice;
            } catch (Exception e) {
                e.printStackTrace();
            }
            cursor.close();
        }

        return itemPrice;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }
}