package com.delaroystudios.pizza.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.adapters.ImageAdapter;
import com.delaroystudios.pizza.database.PizzaData;
import com.delaroystudios.pizza.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import static com.delaroystudios.pizza.database.Constants.*;

public class PizzaCustomizeActivity extends Activity implements View.OnClickListener {

    private RadioButton wholeRadio, leftRadio, rightRadio;
    private TextView wholeText, leftText, rightText, pizzaNameText;
    private Gallery gallery;
    private ArrayList<String> wList = new ArrayList<>();
    private ArrayList<String> lList = new ArrayList<>();
    private ArrayList<String> rList = new ArrayList<>();
    private ArrayList<String> toppingList = new ArrayList<>();
    private PizzaData database;
    private SessionManager sessionManager;
    private int pizzaId;
    private String pizzaName;
    private double pizzaPrice = 15.99; // Default price

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pizza_customize);

        database = new PizzaData(this);
        sessionManager = new SessionManager(this);

        // Get pizza info from intent
        pizzaId = getIntent().getIntExtra("pizza_id", 1);
        pizzaName = getIntent().getStringExtra("pizza_name");
        if (pizzaName == null) pizzaName = "Custom Pizza";

        initViews();
        setupClickListeners();
        createToppingList();
        setupGallery();
        loadPizzaInfo();
    }

    private void initViews() {
        wholeRadio = findViewById(R.id.rb_whole);
        leftRadio = findViewById(R.id.rb_left);
        rightRadio = findViewById(R.id.rb_right);
        wholeText = findViewById(R.id.tv_whole_toppings);
        leftText = findViewById(R.id.tv_left_toppings);
        rightText = findViewById(R.id.tv_right_toppings);
        pizzaNameText = findViewById(R.id.tv_pizza_name);
        gallery = findViewById(R.id.gallery_toppings);

        pizzaNameText.setText(pizzaName);
        wholeRadio.setChecked(true); // Default to whole pizza
    }

    private void setupClickListeners() {
        findViewById(R.id.btn_add_to_cart).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
    }

    private void setupGallery() {
        gallery.setAdapter(new ImageAdapter(this));
        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleToppingSelection(position);
            }
        });
    }

    private void handleToppingSelection(int position) {
        String selectedTopping = toppingList.get(position);

        if (wholeRadio.isChecked()) {
            handleWholeSelection(selectedTopping, position);
        } else if (leftRadio.isChecked()) {
            handleLeftSelection(selectedTopping, position);
        } else {
            handleRightSelection(selectedTopping, position);
        }
    }

    private void handleWholeSelection(String topping, int position) {
        if (wList.contains(topping)) {
            wList.remove(topping);
            showMessage(topping + " removed from whole pizza");
        } else {
            // Remove from left and right if present
            lList.remove(topping);
            rList.remove(topping);
            wList.add(topping);
            showMessage(topping + " added to whole pizza");
        }
        updateToppingTexts();
    }

    private void handleLeftSelection(String topping, int position) {
        if (lList.contains(topping)) {
            lList.remove(topping);
            showMessage(topping + " removed from left side");
        } else if (rList.contains(topping)) {
            // Move from right to whole
            rList.remove(topping);
            wList.add(topping);
            showMessage(topping + " moved to whole pizza");
        } else if (wList.contains(topping)) {
            showMessage(topping + " is already on whole pizza");
        } else {
            lList.add(topping);
            showMessage(topping + " added to left side");
        }
        updateToppingTexts();
    }

    private void handleRightSelection(String topping, int position) {
        if (rList.contains(topping)) {
            rList.remove(topping);
            showMessage(topping + " removed from right side");
        } else if (lList.contains(topping)) {
            // Move from left to whole
            lList.remove(topping);
            wList.add(topping);
            showMessage(topping + " moved to whole pizza");
        } else if (wList.contains(topping)) {
            showMessage(topping + " is already on whole pizza");
        } else {
            rList.add(topping);
            showMessage(topping + " added to right side");
        }
        updateToppingTexts();
    }

    private void updateToppingTexts() {
        wholeText.setText(formatToppingList(wList));
        leftText.setText(formatToppingList(lList));
        rightText.setText(formatToppingList(rList));
    }

    private String formatToppingList(List<String> toppings) {
        if (toppings.isEmpty()) return "";

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < toppings.size(); i++) {
            result.append(toppings.get(i));
            if (i < toppings.size() - 1) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    private void createToppingList() {
        toppingList.add("Pepperoni");
        toppingList.add("Sausage");
        toppingList.add("Mushrooms");
        toppingList.add("Green Peppers");
        toppingList.add("Onions");
        toppingList.add("Black Olives");
        toppingList.add("Extra Cheese");
        toppingList.add("Bacon");
        toppingList.add("Ham");
        toppingList.add("Pineapple");
        toppingList.add("Tomatoes");
        toppingList.add("Chicken");
    }

    private void loadPizzaInfo() {
        // Load pizza details from database if available
        SQLiteDatabase db = database.getReadableDatabase();
        String selection = PIZZA_ID + "=?";
        String[] selectionArgs = {String.valueOf(pizzaId)};

        Cursor cursor = db.query(PIZZAS_TABLE, null, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            pizzaPrice = cursor.getDouble(cursor.getColumnIndex(BASE_PRICE));
            pizzaNameText.setText(cursor.getString(cursor.getColumnIndex(PIZZA_NAME)));
        }
        cursor.close();
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.btn_add_to_cart) {
            addToCart();
        } else if (viewId == R.id.btn_cancel) {
            finish();
        }
    }

    private void addToCart() {
        int userId = sessionManager.getCurrentUserId();
        if (userId == -1) {
            showMessage("Please login to add items to cart");
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        // Calculate total price with toppings
        double totalPrice = pizzaPrice + calculateToppingsPrice();

        // Add to cart
        SQLiteDatabase db = database.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_ID, userId);
        values.put(PIZZA_ID, pizzaId);
        values.put(SIZE_ID, 2); // Default to medium
        values.put(CRUST_ID, 1); // Default to thin
        values.put(QUANTITY, 1);

        long cartId = db.insert(CART_TABLE, null, values);

        if (cartId != -1) {
            // Add toppings to cart_item_toppings
            saveToppingsToCart(cartId);

            showMessage("Added to cart! Total: GHS " + String.format("%.2f", totalPrice));
            finish();
        } else {
            showMessage("Failed to add to cart");
        }
    }

    private void saveToppingsToCart(long cartId) {
        SQLiteDatabase db = database.getWritableDatabase();

        // Save whole pizza toppings
        for (String topping : wList) {
            ContentValues values = new ContentValues();
            values.put(CART_ID, cartId);
            values.put(TOPPING_ID, getToppingId(topping));
            values.put(PLACEMENT, PLACEMENT_WHOLE);
            db.insert(CART_TOPPINGS_TABLE, null, values);
        }

        // Save left side toppings
        for (String topping : lList) {
            ContentValues values = new ContentValues();
            values.put(CART_ID, cartId);
            values.put(TOPPING_ID, getToppingId(topping));
            values.put(PLACEMENT, PLACEMENT_LEFT);
            db.insert(CART_TOPPINGS_TABLE, null, values);
        }

        // Save right side toppings
        for (String topping : rList) {
            ContentValues values = new ContentValues();
            values.put(CART_ID, cartId);
            values.put(TOPPING_ID, getToppingId(topping));
            values.put(PLACEMENT, PLACEMENT_RIGHT);
            db.insert(CART_TOPPINGS_TABLE, null, values);
        }
    }

    private int getToppingId(String toppingName) {
        // Simple mapping - in a real app, query the database
        switch (toppingName) {
            case "Pepperoni": return 1;
            case "Sausage": return 2;
            case "Mushrooms": return 6;
            case "Green Peppers": return 7;
            case "Onions": return 8;
            case "Black Olives": return 9;
            case "Extra Cheese": return 11;
            case "Bacon": return 5;
            case "Ham": return 3;
            case "Pineapple": return 12;
            case "Tomatoes": return 10;
            case "Chicken": return 4;
            default: return 1;
        }
    }

    private double calculateToppingsPrice() {
        // Each topping costs GHS 2.00
        int totalToppings = wList.size() + lList.size() + rList.size();
        return totalToppings * 2.0;
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }
}