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
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.GridLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.adapters.AdminPizzaAdapter;
import com.delaroystudios.pizza.database.PizzaData;
import com.delaroystudios.pizza.models.Pizza;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import static com.delaroystudios.pizza.database.Constants.*;

public class AdminPizzaManagementActivity extends AppCompatActivity implements AdminPizzaAdapter.OnAdminPizzaActionListener {

    private RecyclerView rvPizzas;
    private Button btnAddPizza;
    private ImageButton btnBack;

    private AdminPizzaAdapter pizzaAdapter;
    private List<Pizza> pizzaList;
    private PizzaData database;

    // Extended pizza image resources - matching your ImageAdapter
    private final int[] pizzaImages = {
            R.drawable.anchovies,
            R.drawable.bacon,
            R.drawable.lbismarkpizza,
            R.drawable.lcaprese,
            R.drawable.bananapepper,
            R.drawable.blackolives,
            R.drawable.chicken,
            R.drawable.lcalifornia,
            R.drawable.greenpeppers,
            R.drawable.ham,
            R.drawable.jalapenopeppers,
            R.drawable.mozzarella,
            R.drawable.mushrooms,
            R.drawable.onion,
            R.drawable.pepperoni,
            R.drawable.pineapple,
            R.drawable.sausage,
            R.drawable.tomatoes
    };

    private final String[] pizzaImageNames = {
            "Anchovies",
            "Bacon",
            "Bismark Pizza",
            "Caprese",
            "Banana Pepper",
            "Black Olives",
            "Chicken",
            "California",
            "Green Peppers",
            "Ham",
            "Jalapeño Peppers",
            "Mozzarella",
            "Mushrooms",
            "Onion",
            "Pepperoni",
            "Pineapple",
            "Sausage",
            "Tomatoes"
    };

    private int selectedImageResource = R.drawable.mozzarella; // Default image

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
        btnAddPizza = findViewById(R.id.btn_add_pizza);
        btnBack = findViewById(R.id.btn_back);

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
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = database.getReadableDatabase();
            cursor = db.query(PIZZAS_TABLE, null, null, null, null, null, PIZZA_NAME + " ASC");

            pizzaList.clear();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Pizza pizza = new Pizza();
                    pizza.setPizzaId(cursor.getInt(cursor.getColumnIndexOrThrow(PIZZA_ID)));
                    pizza.setName(cursor.getString(cursor.getColumnIndexOrThrow(PIZZA_NAME)));
                    pizza.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(PIZZA_DESCRIPTION)));
                    pizza.setBasePrice(cursor.getDouble(cursor.getColumnIndexOrThrow(BASE_PRICE)));
                    pizza.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow(CATEGORY_ID)));
                    pizza.setAvailable(cursor.getInt(cursor.getColumnIndexOrThrow(IS_AVAILABLE)) == 1);

                    // Load image resource if exists in database
                    int imageColIndex = cursor.getColumnIndex("image_resource");
                    if (imageColIndex != -1) {
                        pizza.setImageResource(cursor.getInt(imageColIndex));
                    }

                    pizzaList.add(pizza);
                } while (cursor.moveToNext());
            }
            pizzaAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading pizzas: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void showAddPizzaDialog() {
        try {
            selectedImageResource = R.drawable.mozzarella; // Reset to default

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_pizza, null);

            TextInputEditText etName = dialogView.findViewById(R.id.et_pizza_name);
            TextInputEditText etDescription = dialogView.findViewById(R.id.et_pizza_description);
            TextInputEditText etPrice = dialogView.findViewById(R.id.et_pizza_price);
            Spinner spinnerCategory = dialogView.findViewById(R.id.spinner_category);
            SwitchCompat switchAvailable = dialogView.findViewById(R.id.switch_available);
            ImageView ivSelectedImage = dialogView.findViewById(R.id.iv_selected_pizza_image);
            Button btnSelectImage = dialogView.findViewById(R.id.btn_select_image);

            // Verify all views are found
            if (etName == null || etDescription == null || etPrice == null ||
                    spinnerCategory == null || switchAvailable == null) {
                Toast.makeText(this, "Error: Dialog layout views not found. Check view IDs.", Toast.LENGTH_LONG).show();
                return;
            }

            // Setup category spinner
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.pizza_categories, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategory.setAdapter(adapter);

            // Set default values
            switchAvailable.setChecked(true);
            if (ivSelectedImage != null) {
                ivSelectedImage.setImageResource(selectedImageResource);
            }

            // Image selection button
            if (btnSelectImage != null) {
                btnSelectImage.setOnClickListener(v -> {
                    showImageSelectionDialog(ivSelectedImage);
                });
            }

            builder.setView(dialogView)
                    .setTitle("Add New Pizza")
                    .setPositiveButton("Add", null)
                    .setNegativeButton("Cancel", null);

            AlertDialog dialog = builder.create();
            dialog.show();

            // Override positive button to prevent auto-dismiss on validation errors
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
                String priceStr = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";
                int categoryId = spinnerCategory.getSelectedItemPosition() + 1;
                boolean isAvailable = switchAvailable.isChecked();

                // Validation
                if (TextUtils.isEmpty(name)) {
                    etName.setError("Pizza name is required");
                    etName.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(description)) {
                    description = "Delicious pizza"; // Default description
                }

                if (TextUtils.isEmpty(priceStr)) {
                    etPrice.setError("Price is required");
                    etPrice.requestFocus();
                    return;
                }

                try {
                    double price = Double.parseDouble(priceStr);
                    if (price <= 0) {
                        etPrice.setError("Price must be greater than 0");
                        etPrice.requestFocus();
                        return;
                    }

                    if (addPizza(name, description, price, categoryId, isAvailable, selectedImageResource)) {
                        dialog.dismiss();
                    }
                } catch (NumberFormatException e) {
                    etPrice.setError("Invalid price format");
                    etPrice.requestFocus();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error opening dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showImageSelectionDialog(ImageView targetImageView) {
        AlertDialog.Builder imageBuilder = new AlertDialog.Builder(this);
        View imageDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_image, null);

        GridLayout gridImages = imageDialogView.findViewById(R.id.grid_images);

        if (gridImages != null) {
            gridImages.removeAllViews();

            for (int i = 0; i < pizzaImages.length; i++) {
                final int imageRes = pizzaImages[i];
                final String imageName = pizzaImageNames[i];

                View imageItem = LayoutInflater.from(this).inflate(R.layout.item_image_selector, gridImages, false);
                ImageView ivImage = imageItem.findViewById(R.id.iv_image);

                ivImage.setImageResource(imageRes);
                ivImage.setContentDescription(imageName);

                // Highlight selected image
                if (imageRes == selectedImageResource) {
                    imageItem.setBackgroundResource(R.drawable.selected_image_border);
                }

                imageItem.setOnClickListener(v -> {
                    selectedImageResource = imageRes;
                    if (targetImageView != null) {
                        targetImageView.setImageResource(imageRes);
                    }
                    Toast.makeText(this, imageName + " selected", Toast.LENGTH_SHORT).show();
                });

                gridImages.addView(imageItem);
            }
        }

        imageBuilder.setView(imageDialogView)
                .setTitle("Select Pizza Image")
                .setPositiveButton("Done", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private boolean addPizza(String name, String description, double price, int categoryId, boolean isAvailable, int imageResource) {
        SQLiteDatabase db = null;
        try {
            db = database.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(PIZZA_NAME, name);
            values.put(PIZZA_DESCRIPTION, description);
            values.put(BASE_PRICE, price);
            values.put(CATEGORY_ID, categoryId);
            values.put(IS_AVAILABLE, isAvailable ? 1 : 0);
            values.put("image_resource", imageResource);

            long result = db.insert(PIZZAS_TABLE, null, values);
            if (result != -1) {
                Toast.makeText(this, "Pizza added successfully", Toast.LENGTH_SHORT).show();
                loadPizzas();
                broadcastPizzaUpdate();
                return true;
            } else {
                Toast.makeText(this, "Failed to add pizza", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error adding pizza: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    public void onEditPizza(Pizza pizza) {
        try {
            selectedImageResource = pizza.getImageResource() != 0 ? pizza.getImageResource() : R.drawable.mozzarella;

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_pizza, null);

            TextInputEditText etName = dialogView.findViewById(R.id.et_pizza_name);
            TextInputEditText etDescription = dialogView.findViewById(R.id.et_pizza_description);
            TextInputEditText etPrice = dialogView.findViewById(R.id.et_pizza_price);
            Spinner spinnerCategory = dialogView.findViewById(R.id.spinner_category);
            SwitchCompat switchAvailable = dialogView.findViewById(R.id.switch_available);
            ImageView ivSelectedImage = dialogView.findViewById(R.id.iv_selected_pizza_image);
            Button btnSelectImage = dialogView.findViewById(R.id.btn_select_image);

            // Verify all views are found
            if (etName == null || etDescription == null || etPrice == null ||
                    spinnerCategory == null || switchAvailable == null) {
                Toast.makeText(this, "Error: Dialog layout views not found. Check view IDs.", Toast.LENGTH_LONG).show();
                return;
            }

            // Pre-fill existing data
            etName.setText(pizza.getName());
            etDescription.setText(pizza.getDescription());
            etPrice.setText(String.format("%.2f", pizza.getBasePrice()));
            switchAvailable.setChecked(pizza.isAvailable());

            if (ivSelectedImage != null) {
                ivSelectedImage.setImageResource(selectedImageResource);
            }

            // Setup category spinner
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.pizza_categories, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategory.setAdapter(adapter);

            // Set the correct category (categoryId is 1-based, spinner is 0-based)
            if (pizza.getCategoryId() > 0 && pizza.getCategoryId() <= adapter.getCount()) {
                spinnerCategory.setSelection(pizza.getCategoryId() - 1);
            }

            // Image selection button
            if (btnSelectImage != null) {
                btnSelectImage.setOnClickListener(v -> {
                    showImageSelectionDialog(ivSelectedImage);
                });
            }

            builder.setView(dialogView)
                    .setTitle("Edit Pizza")
                    .setPositiveButton("Update", null)
                    .setNegativeButton("Cancel", null);

            AlertDialog dialog = builder.create();
            dialog.show();

            // Override positive button
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
                String priceStr = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";
                int categoryId = spinnerCategory.getSelectedItemPosition() + 1;
                boolean isAvailable = switchAvailable.isChecked();

                if (TextUtils.isEmpty(name)) {
                    etName.setError("Pizza name is required");
                    etName.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(description)) {
                    description = "Delicious pizza";
                }

                if (TextUtils.isEmpty(priceStr)) {
                    etPrice.setError("Price is required");
                    etPrice.requestFocus();
                    return;
                }

                try {
                    double price = Double.parseDouble(priceStr);
                    if (price <= 0) {
                        etPrice.setError("Price must be greater than 0");
                        etPrice.requestFocus();
                        return;
                    }

                    if (updatePizza(pizza.getPizzaId(), name, description, price, categoryId, isAvailable, selectedImageResource)) {
                        dialog.dismiss();
                    }
                } catch (NumberFormatException e) {
                    etPrice.setError("Invalid price format");
                    etPrice.requestFocus();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error opening edit dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean updatePizza(int pizzaId, String name, String description, double price, int categoryId, boolean isAvailable, int imageResource) {
        SQLiteDatabase db = null;
        try {
            db = database.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(PIZZA_NAME, name);
            values.put(PIZZA_DESCRIPTION, description);
            values.put(BASE_PRICE, price);
            values.put(CATEGORY_ID, categoryId);
            values.put(IS_AVAILABLE, isAvailable ? 1 : 0);
            values.put("image_resource", imageResource);

            int rowsAffected = db.update(PIZZAS_TABLE, values, PIZZA_ID + "=?",
                    new String[]{String.valueOf(pizzaId)});

            if (rowsAffected > 0) {
                Toast.makeText(this, "Pizza updated successfully", Toast.LENGTH_SHORT).show();
                loadPizzas();
                broadcastPizzaUpdate();
                return true;
            } else {
                Toast.makeText(this, "Failed to update pizza", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error updating pizza: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    public void onDeletePizza(Pizza pizza) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Pizza")
                .setMessage("Are you sure you want to delete " + pizza.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    SQLiteDatabase db = null;
                    try {
                        db = database.getWritableDatabase();
                        int rowsDeleted = db.delete(PIZZAS_TABLE, PIZZA_ID + "=?",
                                new String[]{String.valueOf(pizza.getPizzaId())});

                        if (rowsDeleted > 0) {
                            Toast.makeText(this, "Pizza deleted successfully", Toast.LENGTH_SHORT).show();
                            loadPizzas();
                            broadcastPizzaUpdate();
                        } else {
                            Toast.makeText(this, "Failed to delete pizza", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error deleting pizza: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onToggleAvailability(Pizza pizza) {
        SQLiteDatabase db = null;
        try {
            db = database.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(IS_AVAILABLE, pizza.isAvailable() ? 0 : 1);

            int rowsAffected = db.update(PIZZAS_TABLE, values, PIZZA_ID + "=?",
                    new String[]{String.valueOf(pizza.getPizzaId())});

            if (rowsAffected > 0) {
                pizza.setAvailable(!pizza.isAvailable());
                pizzaAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Availability updated", Toast.LENGTH_SHORT).show();
                broadcastPizzaUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error updating availability: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void broadcastPizzaUpdate() {
        try {
            Intent intent = new Intent("com.delaroystudios.pizza.PIZZA_UPDATED");
            sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
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