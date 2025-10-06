package com.delaroystudios.pizza.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
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

import static com.delaroystudios.pizza.database.Constants.*;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvAdminLogin;
    private PizzaData database;
    private SessionManager sessionManager;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        database = new PizzaData(this);
        sessionManager = new SessionManager(this);

        // Check if already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToMainMenu();
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

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);
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

        progressDialog.show();
        authenticateUser(username, password);
    }

    private void authenticateUser(String username, String password) {
        SQLiteDatabase db = database.getReadableDatabase();

        // First check if user is admin
        String adminSelection = "(" + USERNAME + "=? OR " + EMAIL + "=?) AND " + IS_ACTIVE + "=1";
        String[] adminSelectionArgs = {username, username};

        Cursor adminCursor = db.query(ADMINS_TABLE, null, adminSelection, adminSelectionArgs, null, null, null);

        if (adminCursor != null && adminCursor.moveToFirst()) {
            String storedPassword = adminCursor.getString(adminCursor.getColumnIndex(PASSWORD_HASH));

            if (storedPassword.equals(password)) {
                // Admin login successful
                String adminName = adminCursor.getString(adminCursor.getColumnIndex(FULL_NAME));
                adminCursor.close();
                progressDialog.dismiss();

                Toast.makeText(this, "Welcome Admin, " + adminName + "!", Toast.LENGTH_SHORT).show();

                // Navigate to Admin Dashboard
                Intent intent = new Intent(this, AdminDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }
            adminCursor.close();
        }
        if (adminCursor != null) adminCursor.close();

        // If not admin, check regular users table
        String userSelection = "(" + USERNAME + "=? OR " + EMAIL + "=?) AND " + IS_ACTIVE + "=1";
        String[] userSelectionArgs = {username, username};

        Cursor userCursor = db.query(USERS_TABLE, null, userSelection, userSelectionArgs, null, null, null);

        if (userCursor != null && userCursor.moveToFirst()) {
            String storedPassword = userCursor.getString(userCursor.getColumnIndex(PASSWORD_HASH));

            if (storedPassword.equals(password)) {
                // Regular user login successful
                User user = new User();
                user.setUserId(userCursor.getInt(userCursor.getColumnIndex(USER_ID)));
                user.setUsername(userCursor.getString(userCursor.getColumnIndex(USERNAME)));
                user.setEmail(userCursor.getString(userCursor.getColumnIndex(EMAIL)));
                user.setFullName(userCursor.getString(userCursor.getColumnIndex(FULL_NAME)));
                user.setPhone(userCursor.getString(userCursor.getColumnIndex(PHONE)));
                user.setAddress(userCursor.getString(userCursor.getColumnIndex(ADDRESS)));

                sessionManager.createLoginSession(user);
                userCursor.close();
                progressDialog.dismiss();

                Toast.makeText(this, "Welcome, " + user.getFullName() + "!", Toast.LENGTH_SHORT).show();
                navigateToMainMenu();
            } else {
                userCursor.close();
                progressDialog.dismiss();
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (userCursor != null) userCursor.close();
            progressDialog.dismiss();
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToMainMenu() {
        Intent intent = new Intent(this, PizzaMenuActivity.class);
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