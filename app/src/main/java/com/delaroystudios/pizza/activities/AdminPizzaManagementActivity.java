package com.delaroystudios.pizza.activities;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ImageButton; // <--- NEW IMPORT: Required for btnBack

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.adapters.AdminPizzaAdapter;
import com.delaroystudios.pizza.database.PizzaData;
import com.delaroystudios.pizza.models.Pizza;

import java.util.ArrayList;
import java.util.List;

import static com.delaroystudios.pizza.database.Constants.*;

public class AdminPizzaManagementActivity extends AppCompatActivity implements AdminPizzaAdapter.OnAdminPizzaActionListener {

    private RecyclerView rvPizzas;
    // CRASH FIX: btnBack must be an ImageButton
    private Button btnAddPizza;
    private ImageButton btnBack; // <--- CORRECTED TYPE

    private AdminPizzaAdapter pizzaAdapter;
    private List<Pizza> pizzaList;
    private PizzaData database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_pizza_management);

        database = new PizzaData(this);
        initViews();
        setupRecyclerView();
        loadPizzas();
    }

    private void initViews() {
        rvPizzas = findViewById(R.id.rv_admin_pizzas);

        // This line is correct as per XML: <Button android:id="@+id/btn_add_pizza" ... />
        btnAddPizza = findViewById(R.id.btn_add_pizza);

        // CRASH FIX: This line must cast to ImageButton, not Button (Line 55 fix)
        btnBack = findViewById(R.id.btn_back); // The implicit cast is now correct because the field type is ImageButton

        btnAddPizza.setOnClickListener(v -> showAddPizzaDialog());
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        pizzaList = new ArrayList<>();
        pizzaAdapter = new AdminPizzaAdapter(pizzaList, this);
        rvPizzas.setLayoutManager(new LinearLayoutManager(this));
        rvPizzas.setAdapter(pizzaAdapter);
    }

    private void loadPizzas() {
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.query(PIZZAS_TABLE, null, null, null, null, null, PIZZA_NAME + " ASC");

        pizzaList.clear();
        if (cursor != null && cursor.moveToFirst()) {
            try {
                do {
                    Pizza pizza = new Pizza();
                    pizza.setPizzaId(cursor.getInt(cursor.getColumnIndexOrThrow(PIZZA_ID)));
                    pizza.setName(cursor.getString(cursor.getColumnIndexOrThrow(PIZZA_NAME)));
                    pizza.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(PIZZA_DESCRIPTION)));
                    pizza.setBasePrice(cursor.getDouble(cursor.getColumnIndexOrThrow(BASE_PRICE)));
                    pizza.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow(CATEGORY_ID)));
                    pizza.setAvailable(cursor.getInt(cursor.getColumnIndexOrThrow(IS_AVAILABLE)) == 1);
                    pizzaList.add(pizza);
                } while (cursor.moveToNext());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                Toast.makeText(this, "Database schema error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                cursor.close();
            }
        }
        pizzaAdapter.notifyDataSetChanged();
    }

    private void showAddPizzaDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_pizza, null);

        EditText etName = dialogView.findViewById(R.id.et_pizza_name);
        EditText etDescription = dialogView.findViewById(R.id.et_pizza_description);
        EditText etPrice = dialogView.findViewById(R.id.et_pizza_price);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinner_category);
        Switch switchAvailable = dialogView.findViewById(R.id.switch_available);

        // Setup category spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.pizza_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        builder.setView(dialogView)
                .setTitle("Add New Pizza")
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();
                    int categoryId = spinnerCategory.getSelectedItemPosition() + 1;
                    boolean isAvailable = switchAvailable.isChecked();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr)) {
                        Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double price = Double.parseDouble(priceStr);
                        addPizza(name, description, price, categoryId, isAvailable);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addPizza(String name, String description, double price, int categoryId, boolean isAvailable) {
        SQLiteDatabase db = database.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PIZZA_NAME, name);
        values.put(PIZZA_DESCRIPTION, description);
        values.put(BASE_PRICE, price);
        values.put(CATEGORY_ID, categoryId);
        values.put(IS_AVAILABLE, isAvailable ? 1 : 0);

        long result = db.insert(PIZZAS_TABLE, null, values);
        if (result != -1) {
            Toast.makeText(this, "Pizza added successfully", Toast.LENGTH_SHORT).show();
            loadPizzas();
        } else {
            Toast.makeText(this, "Failed to add pizza", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEditPizza(Pizza pizza) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_pizza, null);

        EditText etName = dialogView.findViewById(R.id.et_pizza_name);
        EditText etDescription = dialogView.findViewById(R.id.et_pizza_description);
        EditText etPrice = dialogView.findViewById(R.id.et_pizza_price);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinner_category);
        Switch switchAvailable = dialogView.findViewById(R.id.switch_available);

        // Pre-fill existing data
        etName.setText(pizza.getName());
        etDescription.setText(pizza.getDescription());
        etPrice.setText(String.valueOf(pizza.getBasePrice()));
        switchAvailable.setChecked(pizza.isAvailable());

        // Setup category spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.pizza_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Assuming category IDs are 1-based
        if (pizza.getCategoryId() > 0 && pizza.getCategoryId() <= adapter.getCount()) {
            spinnerCategory.setSelection(pizza.getCategoryId() - 1);
        }

        builder.setView(dialogView)
                .setTitle("Edit Pizza")
                .setPositiveButton("Update", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();
                    int categoryId = spinnerCategory.getSelectedItemPosition() + 1;
                    boolean isAvailable = switchAvailable.isChecked();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr)) {
                        Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double price = Double.parseDouble(priceStr);
                        updatePizza(pizza.getPizzaId(), name, description, price, categoryId, isAvailable);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updatePizza(int pizzaId, String name, String description, double price, int categoryId, boolean isAvailable) {
        SQLiteDatabase db = database.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PIZZA_NAME, name);
        values.put(PIZZA_DESCRIPTION, description);
        values.put(BASE_PRICE, price);
        values.put(CATEGORY_ID, categoryId);
        values.put(IS_AVAILABLE, isAvailable ? 1 : 0);

        int rowsAffected = db.update(PIZZAS_TABLE, values, PIZZA_ID + "=?", new String[]{String.valueOf(pizzaId)});
        if (rowsAffected > 0) {
            Toast.makeText(this, "Pizza updated successfully", Toast.LENGTH_SHORT).show();
            loadPizzas();
        } else {
            Toast.makeText(this, "Failed to update pizza", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeletePizza(Pizza pizza) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Pizza")
                .setMessage("Are you sure you want to delete " + pizza.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    SQLiteDatabase db = database.getWritableDatabase();
                    int rowsDeleted = db.delete(PIZZAS_TABLE, PIZZA_ID + "=?",
                            new String[]{String.valueOf(pizza.getPizzaId())});

                    if (rowsDeleted > 0) {
                        Toast.makeText(this, "Pizza deleted successfully", Toast.LENGTH_SHORT).show();
                        loadPizzas();
                    } else {
                        Toast.makeText(this, "Failed to delete pizza", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onToggleAvailability(Pizza pizza) {
        SQLiteDatabase db = database.getWritableDatabase();
        ContentValues values = new ContentValues();
        // Toggle the value
        values.put(IS_AVAILABLE, pizza.isAvailable() ? 0 : 1);

        int rowsAffected = db.update(PIZZAS_TABLE, values, PIZZA_ID + "=?",
                new String[]{String.valueOf(pizza.getPizzaId())});

        if (rowsAffected > 0) {
            pizza.setAvailable(!pizza.isAvailable());
            pizzaAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Availability updated", Toast.LENGTH_SHORT).show();
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