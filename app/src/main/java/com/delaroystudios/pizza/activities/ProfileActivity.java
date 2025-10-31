package com.delaroystudios.pizza.activities;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.database.PizzaData;
import com.delaroystudios.pizza.models.User;
import com.delaroystudios.pizza.utils.SessionManager;

import static com.delaroystudios.pizza.database.Constants.*;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ProfileActivity";

    private TextView tvUsername, tvEmail;
    private EditText etFullName, etEmail, etPhone, etAddress;
    private Button btnUpdateProfile, btnChangePassword, btnLogout;
    private LinearLayout llViewOrderHistory;
    private SwitchCompat switchDarkMode;
    private ImageButton btnBack;

    private PizzaData database;
    private SessionManager sessionManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme preference before calling super.onCreate()
        sessionManager = new SessionManager(this); // Initialize sessionManager early to check preference

        // ** THEME CONTROL: Apply user's saved preference (Dark Mode or Light Mode) **
        if (sessionManager.isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        database = new PizzaData(this);

        currentUser = sessionManager.getCurrentUser();

        if (currentUser == null) {
            // User not logged in, redirect to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupClickListeners();
        loadUserData();
        setupDarkModeSwitch();
    }

    private void initViews() {
        tvUsername = findViewById(R.id.tv_username);
        tvEmail = findViewById(R.id.tv_email);

        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);

        btnUpdateProfile = findViewById(R.id.btn_update_profile);
        btnChangePassword = findViewById(R.id.btn_change_password);
        llViewOrderHistory = findViewById(R.id.btn_orders);
        btnLogout = findViewById(R.id.btn_logout);

        btnBack = findViewById(R.id.btn_back);

        switchDarkMode = findViewById(R.id.switch_dark_mode);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(this);

        btnUpdateProfile.setOnClickListener(this);
        btnChangePassword.setOnClickListener(this);
        llViewOrderHistory.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
    }

    private void loadUserData() {
        tvUsername.setText(sessionManager.getUserName());
        tvEmail.setText(currentUser.getEmail());

        etFullName.setText(currentUser.getFullName());
        etEmail.setText(currentUser.getEmail());
        etPhone.setText(currentUser.getPhone());
        etAddress.setText(currentUser.getAddress());
    }

    private void setupDarkModeSwitch() {
        // 1. Load the saved dark mode preference
        boolean isDarkMode = sessionManager.isDarkModeEnabled();
        switchDarkMode.setChecked(isDarkMode);

        // 2. Set the listener for when the switch state changes
        switchDarkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 3. Save the new preference
                sessionManager.setDarkModeEnabled(isChecked);

                // 4. Apply the new theme
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }

                // 5. Restart the current activity
                recreate();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        if (viewId == R.id.btn_back) {
            finish();
        } else if (viewId == R.id.btn_update_profile) {
            updateProfile();
        } else if (viewId == R.id.btn_change_password) {
            showChangePasswordDialog();
        } else if (viewId == R.id.btn_orders) {
            startActivity(new Intent(this, OrderHistoryActivity.class));
        } else if (viewId == R.id.btn_logout) {
            showLogoutConfirmation();
        }
    }

    private void updateProfile() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name cannot be empty");
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone number cannot be empty");
            etPhone.requestFocus();
            return;
        }

        SQLiteDatabase db = database.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FULL_NAME, fullName);
        values.put(EMAIL, email);
        values.put(PHONE, phone);
        values.put(ADDRESS, address);

        String whereClause = USER_ID + "=?";
        String[] whereArgs = {String.valueOf(currentUser.getUserId())};

        int rowsUpdated = db.update(USERS_TABLE, values, whereClause, whereArgs);

        if (rowsUpdated > 0) {
            currentUser.setFullName(fullName);
            currentUser.setEmail(email);
            currentUser.setPhone(phone);
            currentUser.setAddress(address);

            sessionManager.updateUserProfile(currentUser);

            tvUsername.setText(fullName);
            tvEmail.setText(email);

            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);

        EditText etCurrentPassword = dialogView.findViewById(R.id.et_current_password);
        EditText etNewPassword = dialogView.findViewById(R.id.et_new_password);
        EditText etConfirmNewPassword = dialogView.findViewById(R.id.et_confirm_password);

        builder.setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String currentPassword = etCurrentPassword.getText().toString();
                        String newPassword = etNewPassword.getText().toString();
                        String confirmPassword = etConfirmNewPassword.getText().toString();

                        changePassword(currentPassword, newPassword, confirmPassword);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void changePassword(String currentPassword, String newPassword, String confirmPassword) {
        if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "All password fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "New passwords don't match", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = database.getReadableDatabase();
        String selection = USER_ID + "=?";
        String[] selectionArgs = {String.valueOf(currentUser.getUserId())};

        Cursor cursor = db.query(USERS_TABLE, new String[]{PASSWORD_HASH},
                selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String storedPassword = cursor.getString(cursor.getColumnIndexOrThrow(PASSWORD_HASH));
            cursor.close();

            if (!storedPassword.equals(currentPassword)) {
                Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                return;
            }

            ContentValues values = new ContentValues();
            values.put(PASSWORD_HASH, newPassword);

            int rowsUpdated = db.update(USERS_TABLE, values, selection, selectionArgs);

            if (rowsUpdated > 0) {
                Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to change password", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (cursor != null) cursor.close();
            Toast.makeText(this, "Error verifying current password", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logout();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void logout() {
        sessionManager.logoutUser();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to login. LoginActivity is responsible for ensuring it starts in Light Mode.
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }
}