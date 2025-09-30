package com.delaroystudios.pizza.activities;

import android.app.Activity;
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

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.database.PizzaData;
import static com.delaroystudios.pizza.database.Constants.*;

public class AdminLoginActivity extends Activity implements View.OnClickListener {

    private EditText etAdminUsername, etAdminPassword;
    private Button btnAdminLogin;
    private TextView tvBackToLogin;
    private PizzaData database;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        database = new PizzaData(this);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etAdminUsername = findViewById(R.id.et_admin_username);
        etAdminPassword = findViewById(R.id.et_admin_password);
        btnAdminLogin = findViewById(R.id.btn_admin_login);
        tvBackToLogin = findViewById(R.id.tv_back_to_login);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);
    }

    private void setupClickListeners() {
        btnAdminLogin.setOnClickListener(this);
        tvBackToLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.btn_admin_login) {
            attemptAdminLogin();
        } else if (viewId == R.id.tv_back_to_login) {
            finish(); // Go back to main login
        }
    }

    private void attemptAdminLogin() {
        String username = etAdminUsername.getText().toString().trim();
        String password = etAdminPassword.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();
        authenticateAdmin(username, password);
    }

    private void authenticateAdmin(String username, String password) {
        SQLiteDatabase db = database.getReadableDatabase();
        String selection = "(" + USERNAME + "=? OR " + EMAIL + "=?) AND " + IS_ACTIVE + "=1";
        String[] selectionArgs = {username, username};

        Cursor cursor = db.query(ADMINS_TABLE, null, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            String storedPassword = cursor.getString(cursor.getColumnIndex(PASSWORD_HASH));

            // Simple password check (in production, use proper hashing)
            if (storedPassword.equals(password)) {
                String adminName = cursor.getString(cursor.getColumnIndex(FULL_NAME));

                progressDialog.dismiss();
                Toast.makeText(this, "Welcome, " + adminName + "!", Toast.LENGTH_SHORT).show();

                // Navigate to admin dashboard
                Intent intent = new Intent(this, AdminDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                progressDialog.dismiss();
                Toast.makeText(this, "Invalid admin credentials", Toast.LENGTH_SHORT).show();
            }
        } else {
            progressDialog.dismiss();
            Toast.makeText(this, "Invalid admin credentials", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }
}
