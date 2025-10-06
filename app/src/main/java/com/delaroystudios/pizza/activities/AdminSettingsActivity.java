package com.delaroystudios.pizza.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.database.PizzaData;

import static com.delaroystudios.pizza.database.Constants.*;

public class AdminSettingsActivity extends Activity implements View.OnClickListener {

    private PizzaData database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_settings);

        database = new PizzaData(this);
        initViews();
    }

    private void initViews() {
        findViewById(R.id.btn_back).setOnClickListener(this);
        findViewById(R.id.card_manage_sizes).setOnClickListener(this);
        findViewById(R.id.card_manage_crusts).setOnClickListener(this);
        findViewById(R.id.card_manage_toppings).setOnClickListener(this);
        findViewById(R.id.card_manage_categories).setOnClickListener(this);
        findViewById(R.id.card_change_password).setOnClickListener(this);
        findViewById(R.id.card_backup_database).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        if (viewId == R.id.btn_back) {
            finish();
        } else if (viewId == R.id.card_manage_sizes) {
            showManageSizesDialog();
        } else if (viewId == R.id.card_manage_crusts) {
            showManageCrustsDialog();
        } else if (viewId == R.id.card_manage_toppings) {
            showManageToppingsDialog();
        } else if (viewId == R.id.card_manage_categories) {
            showManageCategoriesDialog();
        } else if (viewId == R.id.card_change_password) {
            showChangePasswordDialog();
        } else if (viewId == R.id.card_backup_database) {
            showMessage("Database backup feature coming soon");
        }
    }

    private void showManageSizesDialog() {
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.query(SIZES_TABLE, null, null, null, null, null, SIZE_NAME);

        StringBuilder sizes = new StringBuilder();
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(SIZE_NAME));
            double multiplier = cursor.getDouble(cursor.getColumnIndexOrThrow(PRICE_MULTIPLIER));
            sizes.append(name).append(" (").append(multiplier).append("x)\n");
        }
        cursor.close();

        new AlertDialog.Builder(this)
                .setTitle("Pizza Sizes")
                .setMessage(sizes.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showManageCrustsDialog() {
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.query(CRUST_TABLE, null, null, null, null, null, CRUST_NAME);

        StringBuilder crusts = new StringBuilder();
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(CRUST_NAME));
            double price = cursor.getDouble(cursor.getColumnIndexOrThrow(ADDITIONAL_PRICE));
            crusts.append(name).append(" (+GHS ").append(String.format("%.2f", price)).append(")\n");
        }
        cursor.close();

        new AlertDialog.Builder(this)
                .setTitle("Crust Types")
                .setMessage(crusts.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showManageToppingsDialog() {
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.query(TOPPINGS_TABLE, null, null, null, null, null, TOPPING_NAME);

        StringBuilder toppings = new StringBuilder();
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(TOPPING_NAME));
            double price = cursor.getDouble(cursor.getColumnIndexOrThrow(TOPPING_PRICE));
            toppings.append(name).append(" (GHS ").append(String.format("%.2f", price)).append(")\n");
        }
        cursor.close();

        new AlertDialog.Builder(this)
                .setTitle("Available Toppings")
                .setMessage(toppings.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showManageCategoriesDialog() {
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.query(CATEGORIES_TABLE, null, null, null, null, null, CATEGORY_NAME);

        StringBuilder categories = new StringBuilder();
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY_NAME));
            String desc = cursor.getString(cursor.getColumnIndexOrThrow(DESCRIPTION));
            categories.append(name).append(": ").append(desc).append("\n\n");
        }
        cursor.close();

        new AlertDialog.Builder(this)
                .setTitle("Pizza Categories")
                .setMessage(categories.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        EditText etCurrentPassword = dialogView.findViewById(R.id.et_current_password);
        EditText etNewPassword = dialogView.findViewById(R.id.et_new_password);
        EditText etConfirmPassword = dialogView.findViewById(R.id.et_confirm_password);

        new AlertDialog.Builder(this)
                .setTitle("Change Admin Password")
                .setView(dialogView)
                .setPositiveButton("Change", (dialog, which) -> {
                    String currentPassword = etCurrentPassword.getText().toString();
                    String newPassword = etNewPassword.getText().toString();
                    String confirmPassword = etConfirmPassword.getText().toString();

                    if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword)) {
                        showMessage("Please fill all fields");
                        return;
                    }

                    if (!newPassword.equals(confirmPassword)) {
                        showMessage("New passwords don't match");
                        return;
                    }

                    if (newPassword.length() < 6) {
                        showMessage("Password must be at least 6 characters");
                        return;
                    }

                    changeAdminPassword(currentPassword, newPassword);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void changeAdminPassword(String currentPassword, String newPassword) {
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.query(ADMINS_TABLE,
                new String[]{PASSWORD_HASH},
                USERNAME + "=?",
                new String[]{"admin"},
                null, null, null);

        if (cursor.moveToFirst()) {
            String storedPassword = cursor.getString(0);

            if (storedPassword.equals(currentPassword)) {
                ContentValues values = new ContentValues();
                values.put(PASSWORD_HASH, newPassword);

                int rowsUpdated = db.update(ADMINS_TABLE, values, USERNAME + "=?", new String[]{"admin"});

                if (rowsUpdated > 0) {
                    showMessage("Password changed successfully");
                } else {
                    showMessage("Failed to change password");
                }
            } else {
                showMessage("Current password is incorrect");
            }
        }
        cursor.close();
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