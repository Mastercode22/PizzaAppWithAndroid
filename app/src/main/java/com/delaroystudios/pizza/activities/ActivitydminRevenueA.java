package com.delaroystudios.pizza.activities;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.database.PizzaData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.delaroystudios.pizza.database.Constants.*;

public class ActivitydminRevenueA extends Activity {

    private TextView tvTodayRevenue, tvWeekRevenue, tvMonthRevenue, tvTotalRevenue;
    private TextView tvTodayOrders, tvWeekOrders, tvMonthOrders, tvTotalOrders;
    private TextView tvAvgOrderValue, tvTopPizza, tvTotalCustomers;
    private PizzaData database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_revenue);

        database = new PizzaData(this);
        initViews();
        loadRevenueData();
    }

    private void initViews() {
        tvTodayRevenue = findViewById(R.id.tv_today_revenue);
        tvWeekRevenue = findViewById(R.id.tv_week_revenue);
        tvMonthRevenue = findViewById(R.id.tv_month_revenue);
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);
        tvTodayOrders = findViewById(R.id.tv_today_orders);
        tvWeekOrders = findViewById(R.id.tv_week_orders);
        tvMonthOrders = findViewById(R.id.tv_month_orders);
        tvTotalOrders = findViewById(R.id.tv_total_orders);
        tvAvgOrderValue = findViewById(R.id.tv_avg_order_value);
        tvTopPizza = findViewById(R.id.tv_top_pizza);
        tvTotalCustomers = findViewById(R.id.tv_total_customers);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void loadRevenueData() {
        SQLiteDatabase db = database.getReadableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Today's revenue and orders
        Cursor todayCursor = db.rawQuery(
                "SELECT COUNT(*), SUM(" + TOTAL_AMOUNT + ") FROM " + ORDERS_TABLE +
                        " WHERE DATE(" + ORDER_DATE + ") = ?" +
                        " AND " + STATUS + " != '" + STATUS_CANCELLED + "'",
                new String[]{today}
        );
        if (todayCursor.moveToFirst()) {
            int todayOrders = todayCursor.getInt(0);
            double todayRevenue = todayCursor.getDouble(1);
            tvTodayOrders.setText(String.valueOf(todayOrders));
            tvTodayRevenue.setText(String.format("GHS %.2f", todayRevenue));
        }
        todayCursor.close();

        // Week's revenue and orders
        Cursor weekCursor = db.rawQuery(
                "SELECT COUNT(*), SUM(" + TOTAL_AMOUNT + ") FROM " + ORDERS_TABLE +
                        " WHERE DATE(" + ORDER_DATE + ") >= DATE('now', '-7 days')" +
                        " AND " + STATUS + " != '" + STATUS_CANCELLED + "'",
                null
        );
        if (weekCursor.moveToFirst()) {
            int weekOrders = weekCursor.getInt(0);
            double weekRevenue = weekCursor.getDouble(1);
            tvWeekOrders.setText(String.valueOf(weekOrders));
            tvWeekRevenue.setText(String.format("GHS %.2f", weekRevenue));
        }
        weekCursor.close();

        // Month's revenue and orders
        Cursor monthCursor = db.rawQuery(
                "SELECT COUNT(*), SUM(" + TOTAL_AMOUNT + ") FROM " + ORDERS_TABLE +
                        " WHERE DATE(" + ORDER_DATE + ") >= DATE('now', '-30 days')" +
                        " AND " + STATUS + " != '" + STATUS_CANCELLED + "'",
                null
        );
        if (monthCursor.moveToFirst()) {
            int monthOrders = monthCursor.getInt(0);
            double monthRevenue = monthCursor.getDouble(1);
            tvMonthOrders.setText(String.valueOf(monthOrders));
            tvMonthRevenue.setText(String.format("GHS %.2f", monthRevenue));
        }
        monthCursor.close();

        // Total revenue and orders
        Cursor totalCursor = db.rawQuery(
                "SELECT COUNT(*), SUM(" + TOTAL_AMOUNT + ") FROM " + ORDERS_TABLE +
                        " WHERE " + STATUS + " != '" + STATUS_CANCELLED + "'",
                null
        );
        if (totalCursor.moveToFirst()) {
            int totalOrders = totalCursor.getInt(0);
            double totalRevenue = totalCursor.getDouble(1);
            tvTotalOrders.setText(String.valueOf(totalOrders));
            tvTotalRevenue.setText(String.format("GHS %.2f", totalRevenue));

            // Calculate average order value
            double avgOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;
            tvAvgOrderValue.setText(String.format("GHS %.2f", avgOrderValue));
        }
        totalCursor.close();

        // Top selling pizza
        Cursor topPizzaCursor = db.rawQuery(
                "SELECT p." + PIZZA_NAME + ", SUM(oi." + QUANTITY + ") as total " +
                        "FROM " + ORDER_ITEMS_TABLE + " oi " +
                        "INNER JOIN " + PIZZAS_TABLE + " p ON oi." + PIZZA_ID + " = p." + PIZZA_ID + " " +
                        "GROUP BY p." + PIZZA_NAME + " " +
                        "ORDER BY total DESC LIMIT 1",
                null
        );
        if (topPizzaCursor.moveToFirst()) {
            String topPizza = topPizzaCursor.getString(0);
            int quantity = topPizzaCursor.getInt(1);
            tvTopPizza.setText(topPizza + " (" + quantity + " sold)");
        } else {
            tvTopPizza.setText("No sales yet");
        }
        topPizzaCursor.close();

        // Total customers
        Cursor customersCursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + USERS_TABLE + " WHERE " + IS_ACTIVE + " = 1",
                null
        );
        if (customersCursor.moveToFirst()) {
            int totalCustomers = customersCursor.getInt(0);
            tvTotalCustomers.setText(String.valueOf(totalCustomers));
        }
        customersCursor.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }
}