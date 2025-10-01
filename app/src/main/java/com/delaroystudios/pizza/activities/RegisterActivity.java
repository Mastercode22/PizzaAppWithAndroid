package com.delaroystudios.pizza.activities;

import android.content.ContentValues;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.database.PizzaData;
import com.delaroystudios.pizza.models.User;
import com.delaroystudios.pizza.utils.SessionManager;
import static com.delaroystudios.pizza.database.Constants.*;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";

    private EditText etFullName, etUsername, etEmail, etPhone, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;
    private PizzaData database;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        database = new PizzaData(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etFullName = findViewById(R.id.et_full_name);
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(this);
        tvLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.btn_register) {
            attemptRegister();
        } else if (viewId == R.id.tv_login) {
            // Go back to login
            finish();
        }
    }

    private void attemptRegister() {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Validation
        if (!validateInput(fullName, username, email, phone, password, confirmPassword)) {
            return;
        }

        showLoading(true);
        registerUser(fullName, username, email, phone, password);
    }

    private boolean validateInput(String fullName, String username, String email,
                                  String phone, String password, String confirmPassword) {
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return false;
        }

        if (username.length() < 3) {
            etUsername.setError("Username must be at least 3 characters");
            etUsername.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords don't match");
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void registerUser(String fullName, String username, String email, String phone, String password) {
        try {
            SQLiteDatabase db = database.getWritableDatabase();

            // Check if username or email already exists
            if (isUsernameOrEmailExists(username, email)) {
                showLoading(false);
                Toast.makeText(this, "Username or email already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            ContentValues values = new ContentValues();
            values.put(FULL_NAME, fullName);
            values.put(USERNAME, username);
            values.put(EMAIL, email);
            values.put(PHONE, phone);
            values.put(PASSWORD_HASH, password); // TODO: Add proper password hashing
            values.put(IS_ACTIVE, 1);

            long userId = db.insert(USERS_TABLE, null, values);

            if (userId != -1) {
                Log.d(TAG, "User registered successfully with ID: " + userId);

                // Create user object and login session
                User user = new User();
                user.setUserId((int) userId);
                user.setUsername(username);
                user.setEmail(email);
                user.setFullName(fullName);
                user.setPhone(phone);

                // Create login session immediately after registration
                sessionManager.createLoginSession(user);

                showLoading(false);
                Toast.makeText(this, "Account created successfully! Welcome " + fullName + "!", Toast.LENGTH_SHORT).show();

                // Go directly to PizzaMenuActivity
                Intent intent = new Intent(this, PizzaMenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                showLoading(false);
                Toast.makeText(this, "Failed to create account. Please try again.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to insert user into database");
            }
        } catch (Exception e) {
            showLoading(false);
            Toast.makeText(this, "Error creating account: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Registration error", e);
        }
    }

    private boolean isUsernameOrEmailExists(String username, String email) {
        SQLiteDatabase db = database.getReadableDatabase();
        String selection = USERNAME + "=? OR " + EMAIL + "=?";
        String[] selectionArgs = {username, email};

        Cursor cursor = db.query(USERS_TABLE, new String[]{USER_ID}, selection, selectionArgs, null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        btnRegister.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }
}