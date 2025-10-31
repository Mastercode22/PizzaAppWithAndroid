package com.delaroystudios.pizza.activities;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate; // Import for theme control
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.adapters.PizzaAdapter;
import com.delaroystudios.pizza.database.PizzaData;
import com.delaroystudios.pizza.models.Pizza;
import com.delaroystudios.pizza.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.delaroystudios.pizza.database.Constants.*;

public class PizzaMenuActivity extends AppCompatActivity implements PizzaAdapter.OnPizzaClickListener {

    private static final String TAG = "PizzaMenuActivity";

    private RecyclerView rvPizzas;
    private EditText etSearch;
    private ImageButton btnCart, btnMenu;
    private LinearLayout llCartSummary, llSearchContainer;
    private TextView tvCartCount, tvCartTotal;

    private PizzaAdapter pizzaAdapter;
    private List<Pizza> pizzaList;
    private List<Pizza> filteredPizzaList;
    private PizzaData database;
    private SessionManager sessionManager;
    private String currentCategory = "all";

    private BroadcastReceiver pizzaUpdateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);

        // ** THEME CONTROL: Apply user's saved preference (Dark Mode or Light Mode) **
        if (sessionManager.isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            // Force Light Mode if Dark Mode is not enabled by the user
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pizza_menu);

        database = new PizzaData(this);

        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupSearchFunctionality();
        setupCategoryButtons();
        setupBroadcastReceiver();

        initializeSamplePizzas();
        loadPizzas();
        updateCartSummary();
    }

    private void setupBroadcastReceiver() {
        pizzaUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadPizzas();
                Toast.makeText(PizzaMenuActivity.this, "Menu updated!", Toast.LENGTH_SHORT).show();
            }
        };

        IntentFilter filter = new IntentFilter("com.delaroystudios.pizza.PIZZA_UPDATED");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(pizzaUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(pizzaUpdateReceiver, filter);
        }
    }

    private void initViews() {
        rvPizzas = findViewById(R.id.rv_pizzas);
        etSearch = findViewById(R.id.et_search);
        btnCart = findViewById(R.id.btn_cart);
        btnMenu = findViewById(R.id.btn_menu);
        llCartSummary = findViewById(R.id.ll_cart_summary);
        llSearchContainer = findViewById(R.id.ll_search_container);
        tvCartCount = findViewById(R.id.tv_cart_count);
        tvCartTotal = findViewById(R.id.tv_cart_total);

        btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));

        btnMenu.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                startActivity(new Intent(this, ProfileActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
        });

        findViewById(R.id.btn_view_cart).setOnClickListener(v ->
                startActivity(new Intent(this, CartActivity.class)));
    }

    private void setupRecyclerView() {
        pizzaList = new ArrayList<>();
        filteredPizzaList = new ArrayList<>();
        pizzaAdapter = new PizzaAdapter(filteredPizzaList, this);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvPizzas.setLayoutManager(gridLayoutManager);
        rvPizzas.setAdapter(pizzaAdapter);
    }

    private void setupSearchFunctionality() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPizzas(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupCategoryButtons() {
        findViewById(R.id.btn_all).setOnClickListener(v -> filterByCategory("all"));
        findViewById(R.id.btn_classic).setOnClickListener(v -> filterByCategory("classic"));
        findViewById(R.id.btn_specialty).setOnClickListener(v -> filterByCategory("specialty"));
        findViewById(R.id.btn_vegetarian).setOnClickListener(v -> filterByCategory("vegetarian"));
    }

    private void initializeSamplePizzas() {
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.query(PIZZAS_TABLE, new String[]{PIZZA_ID}, null, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();

        if (count < 10) {
            addSamplePizzas();
        }
    }

    private void addSamplePizzas() {
        SQLiteDatabase db = database.getWritableDatabase();

        // Sample pizzas with unique images assigned
        String[][] pizzas = {
                {"Supreme Special", "Loaded with premium toppings", "18.34", "1", String.valueOf(R.drawable.sausage)},
                {"Moonlight", "Special night edition pizza", "15.28", "2", String.valueOf(R.drawable.lbismarkpizza)},
                {"Chicken Supreme", "Grilled chicken with veggies", "17.23", "2", String.valueOf(R.drawable.chicken)},
                {"Veggie Delight", "Fresh garden vegetables", "10.29", "3", String.valueOf(R.drawable.greenpeppers)},
                {"Chilli Fresh", "Spicy peppers and onions", "14.99", "2", String.valueOf(R.drawable.jalapenopeppers)},
                {"Ocean Hawaiian", "Ham and pineapple classic", "16.50", "2", String.valueOf(R.drawable.pineapple)},
                {"Margherita Classic", "Fresh mozzarella and basil", "12.99", "1", String.valueOf(R.drawable.mozzarella)},
                {"Pepperoni Deluxe", "Double pepperoni lovers", "19.99", "1", String.valueOf(R.drawable.pepperoni)},
                {"BBQ Chicken", "Tangy BBQ sauce with chicken", "18.99", "2", String.valueOf(R.drawable.bacon)},
                {"Mushroom Truffle", "Premium mushrooms with truffle", "22.99", "2", String.valueOf(R.drawable.mushrooms)},
                {"Four Cheese", "Blend of four cheeses", "17.99", "1", String.valueOf(R.drawable.lcaprese)},
                {"Mediterranean", "Feta, olives, and tomatoes", "16.99", "3", String.valueOf(R.drawable.blackolives)}
        };

        for (String[] pizza : pizzas) {
            ContentValues values = new ContentValues();
            values.put(PIZZA_NAME, pizza[0]);
            values.put(PIZZA_DESCRIPTION, pizza[1]);
            values.put(BASE_PRICE, Double.parseDouble(pizza[2]));
            values.put(CATEGORY_ID, Integer.parseInt(pizza[3]));
            values.put("image_resource", Integer.parseInt(pizza[4])); // Store the unique image
            values.put(IS_AVAILABLE, 1);

            db.insert(PIZZAS_TABLE, null, values);
        }

        Log.d(TAG, "Sample pizzas added to database with unique images");
    }

    private void loadPizzas() {
        SQLiteDatabase db = database.getReadableDatabase();
        String selection = IS_AVAILABLE + "=1";
        Cursor cursor = db.query(PIZZAS_TABLE, null, selection, null, null, null, PIZZA_NAME + " ASC");

        pizzaList.clear();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    Pizza pizza = new Pizza();
                    pizza.setPizzaId(cursor.getInt(cursor.getColumnIndexOrThrow(PIZZA_ID)));
                    pizza.setName(cursor.getString(cursor.getColumnIndexOrThrow(PIZZA_NAME)));
                    pizza.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(PIZZA_DESCRIPTION)));
                    pizza.setBasePrice(cursor.getDouble(cursor.getColumnIndexOrThrow(BASE_PRICE)));
                    pizza.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow(CATEGORY_ID)));

                    // CRITICAL FIX: Read image_resource from database instead of generating it
                    int imageColIndex = cursor.getColumnIndex("image_resource");
                    if (imageColIndex != -1) {
                        int imageResource = cursor.getInt(imageColIndex);
                        // Only use stored image if it's valid (non-zero)
                        if (imageResource != 0) {
                            pizza.setImageResource(imageResource);
                        } else {
                            // Fallback to generated image only if no image is stored
                            pizza.setImageResource(getPizzaImageResourceFallback(pizza.getName()));
                        }
                    } else {
                        // If column doesn't exist (old database), use fallback
                        pizza.setImageResource(getPizzaImageResourceFallback(pizza.getName()));
                    }

                    pizzaList.add(pizza);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Column not found: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        filteredPizzaList.clear();
        filteredPizzaList.addAll(pizzaList);
        pizzaAdapter.notifyDataSetChanged();

        Log.d(TAG, "Loaded " + pizzaList.size() + " pizzas");
    }

    /**
     * Fallback method - only used when database doesn't have image_resource stored
     * This ensures backward compatibility with old pizzas
     */
    private int getPizzaImageResourceFallback(String pizzaName) {
        String name = pizzaName.toLowerCase();

        if (name.contains("margherita")) return R.drawable.mozzarella;
        if (name.contains("pepperoni")) return R.drawable.pepperoni;
        if (name.contains("hawaiian") || name.contains("pineapple") || name.contains("ocean"))
            return R.drawable.pineapple;
        if (name.contains("chicken")) return R.drawable.chicken;
        if (name.contains("veggie") || name.contains("vegetarian")) return R.drawable.greenpeppers;
        if (name.contains("mushroom")) return R.drawable.mushrooms;
        if (name.contains("bbq") || name.contains("bacon")) return R.drawable.bacon;
        if (name.contains("sausage") || name.contains("meat")) return R.drawable.sausage;
        if (name.contains("chilli") || name.contains("supreme")) return R.drawable.greenpeppers;

        return R.drawable.mozzarella;
    }

    private void filterPizzas(String query) {
        filteredPizzaList.clear();

        if (query.isEmpty()) {
            filteredPizzaList.addAll(pizzaList);
        } else {
            for (Pizza pizza : pizzaList) {
                if (pizza.getName().toLowerCase().contains(query.toLowerCase()) ||
                        pizza.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    filteredPizzaList.add(pizza);
                }
            }
        }
        pizzaAdapter.notifyDataSetChanged();
    }

    private void filterByCategory(String category) {
        currentCategory = category;
        filteredPizzaList.clear();

        if (category.equals("all")) {
            filteredPizzaList.addAll(pizzaList);
        } else {
            int categoryId = getCategoryId(category);
            for (Pizza pizza : pizzaList) {
                if (pizza.getCategoryId() == categoryId) {
                    filteredPizzaList.add(pizza);
                }
            }
        }
        pizzaAdapter.notifyDataSetChanged();
    }

    private int getCategoryId(String categoryName) {
        switch (categoryName.toLowerCase()) {
            case "specialty":
                return 2;
            case "vegetarian":
                return 3;
            default:
                return 1;
        }
    }

    @Override
    public void onPizzaClick(Pizza pizza) {
        Intent intent = new Intent(this, PizzaDetailActivity.class);
        intent.putExtra("pizza_id", pizza.getPizzaId());
        intent.putExtra("pizza_name", pizza.getName());
        intent.putExtra("pizza_description", pizza.getDescription());
        intent.putExtra("pizza_price", pizza.getBasePrice());
        intent.putExtra("pizza_category", pizza.getCategoryId());
        intent.putExtra("pizza_image", pizza.getImageResource());
        startActivity(intent);
    }

    @Override
    public void onAddToCart(Pizza pizza, String size) {
        int userId = sessionManager.getCurrentUserId();
        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        int sizeId = size.equals("L") ? 3 : 2;

        SQLiteDatabase db = database.getWritableDatabase();
        String selection = USER_ID + "=? AND " + PIZZA_ID + "=? AND " + SIZE_ID + "=?";
        String[] selectionArgs = {String.valueOf(userId), String.valueOf(pizza.getPizzaId()), String.valueOf(sizeId)};
        Cursor cursor = db.query(CART_TABLE, null, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int currentQty = cursor.getInt(cursor.getColumnIndexOrThrow(QUANTITY));
            int cartId = cursor.getInt(cursor.getColumnIndexOrThrow(CART_ID));

            ContentValues values = new ContentValues();
            values.put(QUANTITY, currentQty + 1);

            db.update(CART_TABLE, values, CART_ID + "=?", new String[]{String.valueOf(cartId)});
            cursor.close();
            Toast.makeText(this, "Updated cart!", Toast.LENGTH_SHORT).show();
        } else {
            if (cursor != null) cursor.close();

            ContentValues values = new ContentValues();
            values.put(USER_ID, userId);
            values.put(PIZZA_ID, pizza.getPizzaId());
            values.put(SIZE_ID, sizeId);
            values.put(CRUST_ID, 1);
            values.put(QUANTITY, 1);
            values.put(ADDED_AT, getCurrentDateTime());

            long result = db.insert(CART_TABLE, null, values);
            if (result != -1) {
                Toast.makeText(this, pizza.getName() + " (Size " + size + ") added!", Toast.LENGTH_SHORT).show();
            }
        }
        updateCartSummary();
    }

    private void updateCartSummary() {
        int cartCount = getCartItemCount();
        double cartTotal = getCartTotal();

        if (cartCount > 0) {
            tvCartCount.setText(cartCount + (cartCount == 1 ? " item" : " items"));
            tvCartTotal.setText("GHS " + String.format("%.2f", cartTotal));
            llCartSummary.setVisibility(View.VISIBLE);
        } else {
            llCartSummary.setVisibility(View.GONE);
        }
    }

    private int getCartItemCount() {
        int userId = sessionManager.getCurrentUserId();
        if (userId == -1) return 0;

        SQLiteDatabase db = database.getReadableDatabase();
        String selection = USER_ID + "=?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(CART_TABLE, new String[]{"SUM(" + QUANTITY + ")"},
                selection, selectionArgs, null, null, null);

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    private double getCartTotal() {
        int userId = sessionManager.getCurrentUserId();
        if (userId == -1) return 0.0;

        SQLiteDatabase db = database.getReadableDatabase();
        String query = "SELECT c." + QUANTITY + ", p." + BASE_PRICE + " FROM " + CART_TABLE + " c " +
                "INNER JOIN " + PIZZAS_TABLE + " p ON c." + PIZZA_ID + " = p." + PIZZA_ID +
                " WHERE c." + USER_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        double total = 0.0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int qty = cursor.getInt(0);
                double price = cursor.getDouble(1);
                total += qty * price;
            } while (cursor.moveToNext());
            cursor.close();
        }
        return total;
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartSummary();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pizzaUpdateReceiver != null) {
            unregisterReceiver(pizzaUpdateReceiver);
        }
        if (database != null) {
            database.close();
        }
    }
}