package com.delaroystudios.pizza.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.delaroystudios.pizza.utils.PasswordUtils;
import static com.delaroystudios.pizza.database.Constants.*;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvAdminLogin;
    private ProgressBar progressBar;
    private PizzaData database;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        database = new PizzaData(this);
        sessionManager = new SessionManager(this);

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            startPizzaMenuActivity();
            return;
        }

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        tvAdminLogin = findViewById(R.id.tv_admin_login);
        progressBar = findViewById(R.id.progress_bar); // Add ProgressBar to your layout
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
        tvAdminLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.btn_login) {
            attemptLogin();
        } else if (viewId == R.id.tv_register) {
            startActivity(new Intent(this, RegisterActivity.class));
        } else if (viewId == R.id.tv_admin_login) {
            startActivity(new Intent(this, AdminLoginActivity.class));
        }
    }

    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        authenticateUser(username, password);
    }

    private void authenticateUser(String username, String password) {
        try {
            SQLiteDatabase db = database.getReadableDatabase();
            String selection = "(" + USERNAME + "=? OR " + EMAIL + "=?) AND " + IS_ACTIVE + "=1";
            String[] selectionArgs = {username, username};

            Cursor cursor = db.query(USERS_TABLE, null, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int passwordIndex = cursor.getColumnIndex(PASSWORD_HASH);
                if (passwordIndex != -1) {
                    String storedPassword = cursor.getString(passwordIndex);

                    // Simple password check (improve with proper hashing later)
                    if (storedPassword.equals(password)) {
                        User user = createUserFromCursor(cursor);
                        sessionManager.createLoginSession(user);

                        showLoading(false);
                        Toast.makeText(this, "Welcome back, " + user.getFullName() + "!", Toast.LENGTH_SHORT).show();
                        startPizzaMenuActivity();
                    } else {
                        showLoading(false);
                        Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showLoading(false);
                    Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show();
                }
                cursor.close();
            } else {
                showLoading(false);
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                if (cursor != null) cursor.close();
            }
        } catch (Exception e) {
            showLoading(false);
            Toast.makeText(this, "Login error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private User createUserFromCursor(Cursor cursor) {
        User user = new User();

        int userIdIndex = cursor.getColumnIndex(USER_ID);
        int usernameIndex = cursor.getColumnIndex(USERNAME);
        int emailIndex = cursor.getColumnIndex(EMAIL);
        int fullNameIndex = cursor.getColumnIndex(FULL_NAME);
        int phoneIndex = cursor.getColumnIndex(PHONE);
        int addressIndex = cursor.getColumnIndex(ADDRESS);

        if (userIdIndex != -1) user.setUserId(cursor.getInt(userIdIndex));
        if (usernameIndex != -1) user.setUsername(cursor.getString(usernameIndex));
        if (emailIndex != -1) user.setEmail(cursor.getString(emailIndex));
        if (fullNameIndex != -1) user.setFullName(cursor.getString(fullNameIndex));
        if (phoneIndex != -1) user.setPhone(cursor.getString(phoneIndex));
        if (addressIndex != -1) user.setAddress(cursor.getString(addressIndex));

        return user;
    }

    private void startPizzaMenuActivity() {
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }
}