package com.delaroystudios.pizza.activities;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.adapters.CustomerAdapter;
import com.delaroystudios.pizza.database.PizzaData;
import com.delaroystudios.pizza.models.User;

import java.util.ArrayList;
import java.util.List;

import static com.delaroystudios.pizza.database.Constants.*;

public class AdminCustomersActivity extends Activity {

    private RecyclerView rvCustomers;
    private EditText etSearch;
    private TextView tvTotalCustomers, tvEmptyState;
    private CustomerAdapter customerAdapter;
    private List<User> customerList;
    private List<User> filteredCustomerList;
    private PizzaData database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_customers);

        database = new PizzaData(this);
        initViews();
        setupRecyclerView();
        setupSearch();
        loadCustomers();
    }

    private void initViews() {
        rvCustomers = findViewById(R.id.rv_customers);
        etSearch = findViewById(R.id.et_search);
        tvTotalCustomers = findViewById(R.id.tv_total_customers);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        customerList = new ArrayList<>();
        filteredCustomerList = new ArrayList<>();
        customerAdapter = new CustomerAdapter(filteredCustomerList);
        rvCustomers.setLayoutManager(new LinearLayoutManager(this));
        rvCustomers.setAdapter(customerAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCustomers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadCustomers() {
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.query(USERS_TABLE, null, null, null, null, null, FULL_NAME + " ASC");

        customerList.clear();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(USER_ID)));
                user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(USERNAME)));
                user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(FULL_NAME)));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(EMAIL)));
                user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(PHONE)));
                user.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS)));

                int orderCount = getCustomerOrderCount(user.getUserId());
                user.setOrderCount(orderCount);

                customerList.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();

        filteredCustomerList.clear();
        filteredCustomerList.addAll(customerList);
        customerAdapter.notifyDataSetChanged();
        updateUI();
    }

    private int getCustomerOrderCount(int userId) {
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + ORDERS_TABLE + " WHERE " + USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    private void filterCustomers(String query) {
        filteredCustomerList.clear();
        if (query.isEmpty()) {
            filteredCustomerList.addAll(customerList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (User user : customerList) {
                if (user.getFullName().toLowerCase().contains(lowerQuery) ||
                        user.getEmail().toLowerCase().contains(lowerQuery) ||
                        user.getUsername().toLowerCase().contains(lowerQuery) ||
                        (user.getPhone() != null && user.getPhone().contains(query))) {
                    filteredCustomerList.add(user);
                }
            }
        }
        customerAdapter.notifyDataSetChanged();
        updateUI();
    }

    private void updateUI() {
        tvTotalCustomers.setText("Total: " + filteredCustomerList.size() + " customers");

        if (filteredCustomerList.isEmpty()) {
            rvCustomers.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvCustomers.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
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