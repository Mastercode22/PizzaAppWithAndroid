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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.database.PizzaData;
import com.delaroystudios.pizza.models.User;
import com.delaroystudios.pizza.utils.SessionManager;

// ADD THESE IMPORTS
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
// END NEW IMPORTS

import static com.delaroystudios.pizza.database.Constants.*;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ProfileActivity";

    private TextView tvUsername, tvJoinedDate;
    private EditText etFullName, etEmail, etPhone, etAddress;
    private Button btnUpdateProfile, btnChangePassword, btnViewOrderHistory, btnLogout;
    private PizzaData database;
    private SessionManager sessionManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        database = new PizzaData(this);
        sessionManager = new SessionManager(this);
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
    }

    private void initViews() {
        tvUsername = findViewById(R.id.tv_username);
        tvJoinedDate = findViewById(R.id.tv_joined_date);
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        btnUpdateProfile = findViewById(R.id.btn_update_profile);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnViewOrderHistory = findViewById(R.id.btn_view_order_history);
        btnLogout = findViewById(R.id.btn_logout);
    }

    private void setupClickListeners() {
        btnUpdateProfile.setOnClickListener(this);
        btnChangePassword.setOnClickListener(this);
        btnViewOrderHistory.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
    }

    private void loadUserData() {
        tvUsername.setText("@" + currentUser.getUsername());
        etFullName.setText(currentUser.getFullName());
        etEmail.setText(currentUser.getEmail());
        etPhone.setText(currentUser.getPhone());
        etAddress.setText(currentUser.getAddress());

        // MODIFIED: Replace hardcoded placeholder with current date logic

        // 1. Get the current date
        Date currentDate = new Date();

        // 2. Define the desired format (e.g., "dd MMM , yyyy" -> "Sep 30, 2025")
        // We use Locale.getDefault() for internationalization
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM , yyyy", Locale.getDefault());

        // 3. Format the date into a string
        String joinedDateString = "Member since " + sdf.format(currentDate);

        tvJoinedDate.setText(joinedDateString);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.btn_update_profile) {
            updateProfile();
        } else if (viewId == R.id.btn_change_password) {
            showChangePasswordDialog();
        } else if (viewId == R.id.btn_view_order_history) {
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

        // Validation
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

        // Update database
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
            // Update current user object
            currentUser.setFullName(fullName);
            currentUser.setEmail(email);
            currentUser.setPhone(phone);
            currentUser.setAddress(address);

            // Update session
            sessionManager.updateUserProfile(currentUser);

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
        EditText etConfirmNewPassword = dialogView.findViewById(R.id.et_confirm_new_password);

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
        // Validate passwords
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

        // Verify current password
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

            // Update password
            ContentValues values = new ContentValues();
            values.put(PASSWORD_HASH, newPassword); // TODO: Add proper password hashing

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
        // Clear session
        sessionManager.logoutUser();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to login and clear activity stack
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