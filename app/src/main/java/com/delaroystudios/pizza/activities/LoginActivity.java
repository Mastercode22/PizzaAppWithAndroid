package com.delaroystudios.pizza.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.database.PizzaData;
import com.delaroystudios.pizza.models.User;
import com.delaroystudios.pizza.utils.SessionManager;

import static com.delaroystudios.pizza.database.Constants.*;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword, tvAdminLogin;
    private ProgressBar progressBar;

    private PizzaData database;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Always force Light Mode for login screen (matches your RegisterActivity)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);

        // Initialize SessionManager first
        sessionManager = new SessionManager(this);

        // ** AUTO-LOGIN: Check if user is already logged in **
        if (sessionManager.isLoggedIn()) {
            Log.d(TAG, "User already logged in, redirecting to main menu");
            navigateToPizzaMenu();
            return; // Don't load login UI
        }

        setContentView(R.layout.activity_login);

        database = new PizzaData(this);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        tvAdminLogin = findViewById(R.id.tv_admin_login);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
        tvAdminLogin.setOnClickListener(this);

        // Optional: Handle forgot password if you have this feature
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        if (viewId == R.id.btn_login) {
            attemptLogin();
        } else if (viewId == R.id.tv_register) {
            // Navigate to RegisterActivity
            startActivity(new Intent(this, RegisterActivity.class));
        } else if (viewId == R.id.tv_admin_login) {
            // Navigate to AdminLoginActivity
            startActivity(new Intent(this, AdminLoginActivity.class));
        }
    }

    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();

        // Validation
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        showLoading(true);
        loginUser(username, password);
    }

    private void loginUser(String username, String password) {
        try {
            SQLiteDatabase db = database.getReadableDatabase();

            // Query for user with matching username and password
            String selection = USERNAME + "=? AND " + PASSWORD_HASH + "=? AND " + IS_ACTIVE + "=?";
            String[] selectionArgs = {username, password, "1"};

            Cursor cursor = db.query(
                    USERS_TABLE,
                    null, // Select all columns
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                // User found - create User object
                User user = new User();
                user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(USER_ID)));
                user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(USERNAME)));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(EMAIL)));
                user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(FULL_NAME)));
                user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(PHONE)));

                // Address might be null for new users
                int addressIndex = cursor.getColumnIndex(ADDRESS);
                if (addressIndex != -1 && !cursor.isNull(addressIndex)) {
                    user.setAddress(cursor.getString(addressIndex));
                }

                cursor.close();

                // ** SAVE SESSION - This keeps user logged in **
                sessionManager.createLoginSession(user);

                Log.d(TAG, "Login successful for user: " + username);

                showLoading(false);
                Toast.makeText(this, "Welcome back, " + user.getFullName() + "!", Toast.LENGTH_SHORT).show();

                // Navigate to main menu
                navigateToPizzaMenu();

            } else {
                // Login failed
                if (cursor != null) {
                    cursor.close();
                }

                showLoading(false);
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Login failed for username: " + username);

                // Clear password field for security
                etPassword.setText("");
                etPassword.requestFocus();
            }

        } catch (Exception e) {
            showLoading(false);
            Toast.makeText(this, "Login error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Login error", e);
        }
    }

    private void navigateToPizzaMenu() {
        Intent intent = new Intent(this, PizzaMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        btnLogin.setEnabled(!show);
        etUsername.setEnabled(!show);
        etPassword.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }

    // ** IMPORTANT: Handle back button to prevent going back after login **
    @Override
    public void onBackPressed() {
        // Exit app instead of going back
        finishAffinity();
    }
}