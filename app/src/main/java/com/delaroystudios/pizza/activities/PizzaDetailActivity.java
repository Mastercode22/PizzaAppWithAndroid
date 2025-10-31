package com.delaroystudios.pizza.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.database.PizzaData;
import com.delaroystudios.pizza.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.delaroystudios.pizza.database.Constants.*;

public class PizzaDetailActivity extends AppCompatActivity {

    private static final String TAG = "PizzaDetailActivity";

    private ImageView ivPizzaImage;
    private TextView tvPizzaName, tvPizzaDescription, tvPizzaPrice, tvTotalPrice;
    private RadioGroup rgSize, rgCrust;
    private Button btnAddToCart, btnIncreaseQty, btnDecreaseQty;
    private TextView tvQuantity;
    private ImageButton btnBack;
    private CardView cardIngredients;
    private TextView tvIngredients;

    private int pizzaId;
    private String pizzaName;
    private String pizzaDescription;
    private double basePrice;
    private int categoryId;
    private int imageResource;

    private int quantity = 1;
    private int selectedSizeId = 2; // Default Medium
    private int selectedCrustId = 1; // Default Thin Crust
    private double selectedSizeMultiplier = 1.0;
    private double selectedCrustPrice = 0.0;

    private PizzaData database;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pizza_detail);

        database = new PizzaData(this);
        sessionManager = new SessionManager(this);

        getIntentData();
        initViews();
        loadSizeAndCrustFromDatabase();
        setupListeners();
        displayPizzaDetails();
        updateTotalPrice();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        pizzaId = intent.getIntExtra("pizza_id", -1);
        pizzaName = intent.getStringExtra("pizza_name");
        pizzaDescription = intent.getStringExtra("pizza_description");
        basePrice = intent.getDoubleExtra("pizza_price", 0.0);
        categoryId = intent.getIntExtra("pizza_category", 1);
        imageResource = intent.getIntExtra("pizza_image", R.drawable.mozzarella);
    }

    private void initViews() {
        ivPizzaImage = findViewById(R.id.iv_pizza_detail);
        tvPizzaName = findViewById(R.id.tv_pizza_name_detail);
        tvPizzaDescription = findViewById(R.id.tv_pizza_description_detail);
        tvPizzaPrice = findViewById(R.id.tv_pizza_price_detail);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        rgSize = findViewById(R.id.rg_size);
        rgCrust = findViewById(R.id.rg_crust);
        btnAddToCart = findViewById(R.id.btn_add_to_cart_detail);
        btnIncreaseQty = findViewById(R.id.btn_increase_qty);
        btnDecreaseQty = findViewById(R.id.btn_decrease_qty);
        tvQuantity = findViewById(R.id.tv_quantity);
        btnBack = findViewById(R.id.btn_back_detail);
        cardIngredients = findViewById(R.id.card_ingredients);
        tvIngredients = findViewById(R.id.tv_ingredients);
    }

    private void loadSizeAndCrustFromDatabase() {
        SQLiteDatabase db = database.getReadableDatabase();

        // Load size multipliers from database and set default
        Cursor sizeCursor = db.query(SIZES_TABLE,
                new String[]{SIZE_ID, PRICE_MULTIPLIER},
                SIZE_ID + "=?",
                new String[]{"2"}, // Medium is default (ID 2)
                null, null, null);

        if (sizeCursor != null && sizeCursor.moveToFirst()) {
            selectedSizeMultiplier = sizeCursor.getDouble(1);
            sizeCursor.close();
        }

        // Load crust price from database and set default
        Cursor crustCursor = db.query(CRUST_TABLE,
                new String[]{CRUST_ID, ADDITIONAL_PRICE},
                CRUST_ID + "=?",
                new String[]{"1"}, // Thin Crust is default (ID 1)
                null, null, null);

        if (crustCursor != null && crustCursor.moveToFirst()) {
            selectedCrustPrice = crustCursor.getDouble(1);
            crustCursor.close();
        }

        Log.d(TAG, "Loaded defaults - Size Multiplier: " + selectedSizeMultiplier + ", Crust Price: " + selectedCrustPrice);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnIncreaseQty.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
            updateTotalPrice();
        });

        btnDecreaseQty.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
                updateTotalPrice();
            }
        });

        rgSize.setOnCheckedChangeListener((group, checkedId) -> {
            // Update size ID and multiplier from database
            if (checkedId == R.id.rb_small) {
                selectedSizeId = 1;
            } else if (checkedId == R.id.rb_medium) {
                selectedSizeId = 2;
            } else if (checkedId == R.id.rb_large) {
                selectedSizeId = 3;
            }

            // Get actual multiplier from database
            SQLiteDatabase db = database.getReadableDatabase();
            Cursor cursor = db.query(SIZES_TABLE,
                    new String[]{PRICE_MULTIPLIER},
                    SIZE_ID + "=?",
                    new String[]{String.valueOf(selectedSizeId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                selectedSizeMultiplier = cursor.getDouble(0);
                cursor.close();
                Log.d(TAG, "Size changed to ID: " + selectedSizeId + ", Multiplier: " + selectedSizeMultiplier);
            }

            updateTotalPrice();
        });

        rgCrust.setOnCheckedChangeListener((group, checkedId) -> {
            // Update crust ID and price from database
            if (checkedId == R.id.rb_thin) {
                selectedCrustId = 1;
            } else if (checkedId == R.id.rb_thick) {
                selectedCrustId = 2;
            } else if (checkedId == R.id.rb_stuffed) {
                selectedCrustId = 3;
            }

            // Get actual additional price from database
            SQLiteDatabase db = database.getReadableDatabase();
            Cursor cursor = db.query(CRUST_TABLE,
                    new String[]{ADDITIONAL_PRICE},
                    CRUST_ID + "=?",
                    new String[]{String.valueOf(selectedCrustId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                selectedCrustPrice = cursor.getDouble(0);
                cursor.close();
                Log.d(TAG, "Crust changed to ID: " + selectedCrustId + ", Additional Price: " + selectedCrustPrice);
            }

            updateTotalPrice();
        });

        btnAddToCart.setOnClickListener(v -> addToCart());
    }

    private void displayPizzaDetails() {
        ivPizzaImage.setImageResource(imageResource);
        tvPizzaName.setText(pizzaName);
        tvPizzaDescription.setText(pizzaDescription);
        tvPizzaPrice.setText("GHS " + String.format("%.2f", basePrice));
        tvQuantity.setText(String.valueOf(quantity));

        // Display ingredients based on pizza name
        String ingredients = getIngredientsForPizza(pizzaName);
        tvIngredients.setText(ingredients);
    }

    private String getIngredientsForPizza(String name) {
        String lowerName = name.toLowerCase();

        if (lowerName.contains("margherita")) {
            return "• Fresh Mozzarella Cheese\n• Tomato Sauce\n• Fresh Basil\n• Olive Oil\n• Italian Herbs";
        } else if (lowerName.contains("pepperoni")) {
            return "• Pepperoni Slices\n• Mozzarella Cheese\n• Tomato Sauce\n• Italian Seasoning\n• Oregano";
        } else if (lowerName.contains("hawaiian") || lowerName.contains("ocean")) {
            return "• Ham\n• Pineapple Chunks\n• Mozzarella Cheese\n• Tomato Sauce\n• Red Onions";
        } else if (lowerName.contains("chicken")) {
            return "• Grilled Chicken\n• Bell Peppers\n• Onions\n• Mozzarella Cheese\n• BBQ/Tomato Sauce\n• Mushrooms";
        } else if (lowerName.contains("veggie") || lowerName.contains("vegetarian")) {
            return "• Bell Peppers\n• Mushrooms\n• Onions\n• Tomatoes\n• Olives\n• Mozzarella Cheese\n• Spinach";
        } else if (lowerName.contains("mushroom")) {
            return "• Premium Mushrooms\n• Truffle Oil\n• Mozzarella Cheese\n• Parmesan\n• Fresh Herbs\n• Garlic";
        } else if (lowerName.contains("bbq")) {
            return "• BBQ Chicken\n• Red Onions\n• Bacon\n• Mozzarella Cheese\n• BBQ Sauce\n• Cilantro";
        } else if (lowerName.contains("supreme") || lowerName.contains("special")) {
            return "• Pepperoni\n• Sausage\n• Bell Peppers\n• Onions\n• Mushrooms\n• Olives\n• Mozzarella Cheese";
        } else if (lowerName.contains("cheese")) {
            return "• Mozzarella\n• Cheddar\n• Parmesan\n• Provolone\n• Tomato Sauce\n• Italian Herbs";
        } else if (lowerName.contains("mediterranean")) {
            return "• Feta Cheese\n• Kalamata Olives\n• Sun-dried Tomatoes\n• Red Onions\n• Spinach\n• Mozzarella";
        } else if (lowerName.contains("chilli")) {
            return "• Spicy Peppers\n• Red Chili Flakes\n• Onions\n• Jalapeños\n• Mozzarella Cheese\n• Hot Sauce";
        } else if (lowerName.contains("moonlight")) {
            return "• Special Night Edition Blend\n• Premium Cheese\n• Secret Sauce\n• Fresh Herbs\n• Gourmet Toppings";
        } else {
            return "• Premium Cheese Blend\n• Fresh Tomato Sauce\n• Select Toppings\n• Italian Herbs\n• Quality Ingredients";
        }
    }

    private void updateTotalPrice() {
        // Calculate price using database values: (base price × size multiplier) + crust price
        double itemPrice = (basePrice * selectedSizeMultiplier) + selectedCrustPrice;
        double totalPrice = itemPrice * quantity;

        tvTotalPrice.setText("GHS " + String.format("%.2f", totalPrice));

        Log.d(TAG, String.format("Price calculation - Base: %.2f, Multiplier: %.2f, Crust: %.2f, Qty: %d, Total: %.2f",
                basePrice, selectedSizeMultiplier, selectedCrustPrice, quantity, totalPrice));
    }

    private void addToCart() {
        int userId = sessionManager.getCurrentUserId();
        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        SQLiteDatabase db = database.getWritableDatabase();

        // Check if same pizza with same size and crust exists in cart
        String selection = USER_ID + "=? AND " + PIZZA_ID + "=? AND " + SIZE_ID + "=? AND " + CRUST_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(userId),
                String.valueOf(pizzaId),
                String.valueOf(selectedSizeId),
                String.valueOf(selectedCrustId)
        };

        Cursor cursor = db.query(CART_TABLE, null, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            // Update quantity
            int currentQty = cursor.getInt(cursor.getColumnIndexOrThrow(QUANTITY));
            int cartId = cursor.getInt(cursor.getColumnIndexOrThrow(CART_ID));

            ContentValues values = new ContentValues();
            values.put(QUANTITY, currentQty + quantity);

            db.update(CART_TABLE, values, CART_ID + "=?", new String[]{String.valueOf(cartId)});
            cursor.close();

            Toast.makeText(this, quantity + " more added to cart!", Toast.LENGTH_SHORT).show();
        } else {
            if (cursor != null) cursor.close();

            // Insert new cart item
            ContentValues values = new ContentValues();
            values.put(USER_ID, userId);
            values.put(PIZZA_ID, pizzaId);
            values.put(SIZE_ID, selectedSizeId);
            values.put(CRUST_ID, selectedCrustId);
            values.put(QUANTITY, quantity);
            values.put(ADDED_AT, getCurrentDateTime());

            long result = db.insert(CART_TABLE, null, values);
            if (result != -1) {
                // Get size and crust names for confirmation
                String sizeName = getSizeName(selectedSizeId);
                String crustName = getCrustName(selectedCrustId);

                Toast.makeText(this,
                        quantity + " " + pizzaName + "\n" + sizeName + " • " + crustName + "\nAdded to cart!",
                        Toast.LENGTH_LONG).show();

                Log.d(TAG, String.format("Added to cart - Pizza: %s, Size ID: %d, Crust ID: %d, Qty: %d",
                        pizzaName, selectedSizeId, selectedCrustId, quantity));
            } else {
                Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
            }
        }

        // Reset quantity to 1 after adding
        quantity = 1;
        tvQuantity.setText(String.valueOf(quantity));
        updateTotalPrice();
    }

    private String getSizeName(int sizeId) {
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.query(SIZES_TABLE,
                new String[]{SIZE_NAME},
                SIZE_ID + "=?",
                new String[]{String.valueOf(sizeId)},
                null, null, null);

        String sizeName = "Medium";
        if (cursor != null && cursor.moveToFirst()) {
            sizeName = cursor.getString(0);
            cursor.close();
        }
        return sizeName;
    }

    private String getCrustName(int crustId) {
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.query(CRUST_TABLE,
                new String[]{CRUST_NAME},
                CRUST_ID + "=?",
                new String[]{String.valueOf(crustId)},
                null, null, null);

        String crustName = "Thin Crust";
        if (cursor != null && cursor.moveToFirst()) {
            crustName = cursor.getString(0);
            cursor.close();
        }
        return crustName;
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }
}