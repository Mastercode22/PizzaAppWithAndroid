package com.delaroystudios.pizza.activities;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

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
    private WebView webViewBarChart, webViewPieChart, webViewLineChart, webViewDoughnutChart;
    private PizzaData database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_revenue);

        database = new PizzaData(this);
        initViews();
        loadRevenueData();
        setupCharts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh all data when activity becomes visible
        Log.d("REVENUE_REFRESH", "onResume() called - Refreshing data");
        loadRevenueData();
        setupCharts();

        // Show toast to confirm refresh
        Toast.makeText(this, "Revenue data refreshed", Toast.LENGTH_SHORT).show();
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

        webViewBarChart = findViewById(R.id.webview_bar_chart);
        webViewPieChart = findViewById(R.id.webview_pie_chart);
        webViewLineChart = findViewById(R.id.webview_line_chart);
        webViewDoughnutChart = findViewById(R.id.webview_doughnut_chart);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void loadRevenueData() {
        Log.d("REVENUE_REFRESH", "loadRevenueData() called");

        SQLiteDatabase db = database.getReadableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());

        // Debug: Check all orders and their status
        debugOrders(db);

        // Today's Revenue (ONLY COMPLETED ORDERS)
        double todayRevenue = getRevenue(db, today, today);
        tvTodayRevenue.setText(String.format(Locale.getDefault(), "GHS %.2f", todayRevenue));
        Log.d("REVENUE_REFRESH", "Today's Revenue: " + todayRevenue);

        // Week's Revenue (ONLY COMPLETED ORDERS)
        String weekStart = getDateDaysAgo(7);
        double weekRevenue = getRevenue(db, weekStart, today);
        tvWeekRevenue.setText(String.format(Locale.getDefault(), "GHS %.2f", weekRevenue));
        Log.d("REVENUE_REFRESH", "Week's Revenue: " + weekRevenue);

        // Month's Revenue (ONLY COMPLETED ORDERS)
        String monthStart = getDateDaysAgo(30);
        double monthRevenue = getRevenue(db, monthStart, today);
        tvMonthRevenue.setText(String.format(Locale.getDefault(), "GHS %.2f", monthRevenue));
        Log.d("REVENUE_REFRESH", "Month's Revenue: " + monthRevenue);

        // Total Revenue (ONLY COMPLETED ORDERS)
        double totalRevenue = getTotalRevenue(db);
        tvTotalRevenue.setText(String.format(Locale.getDefault(), "GHS %.2f", totalRevenue));
        Log.d("REVENUE_REFRESH", "Total Revenue: " + totalRevenue);

        // Today's Orders (ONLY COMPLETED ORDERS)
        int todayOrders = getOrderCount(db, today, today);
        tvTodayOrders.setText(String.valueOf(todayOrders));

        // Week's Orders (ONLY COMPLETED ORDERS)
        int weekOrders = getOrderCount(db, weekStart, today);
        tvWeekOrders.setText(String.valueOf(weekOrders));

        // Month's Orders (ONLY COMPLETED ORDERS)
        int monthOrders = getOrderCount(db, monthStart, today);
        tvMonthOrders.setText(String.valueOf(monthOrders));

        // Total Orders (ONLY COMPLETED ORDERS)
        int totalOrders = getTotalOrderCount(db);
        tvTotalOrders.setText(String.valueOf(totalOrders));

        // Average Order Value (ONLY COMPLETED ORDERS)
        double avgOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;
        tvAvgOrderValue.setText(String.format(Locale.getDefault(), "GHS %.2f", avgOrderValue));

        // Top Pizza (FROM COMPLETED ORDERS)
        String topPizza = getTopPizza(db);
        tvTopPizza.setText(topPizza);

        // Total Customers
        int totalCustomers = getTotalCustomers(db);
        tvTotalCustomers.setText(String.valueOf(totalCustomers));

        Log.d("REVENUE_REFRESH", "Data load completed successfully");
    }

    private void debugOrders(SQLiteDatabase db) {
        Cursor debugCursor = db.rawQuery(
                "SELECT " + ORDER_ID + ", " + STATUS + ", " + TOTAL_AMOUNT + ", " + ORDER_DATE +
                        " FROM " + ORDERS_TABLE + " ORDER BY " + ORDER_DATE + " DESC LIMIT 10", null);

        Log.d("REVENUE_DEBUG", "=== RECENT ORDERS ===");
        while (debugCursor.moveToNext()) {
            String orderId = debugCursor.getString(0);
            String status = debugCursor.getString(1);
            double amount = debugCursor.getDouble(2);
            String date = debugCursor.getString(3);
            Log.d("REVENUE_DEBUG", "Order: " + orderId + ", Status: " + status + ", Amount: " + amount + ", Date: " + date);
        }
        debugCursor.close();

        // Check completed orders revenue
        Cursor completedCursor = db.rawQuery(
                "SELECT SUM(" + TOTAL_AMOUNT + ") FROM " + ORDERS_TABLE +
                        " WHERE " + STATUS + " = '" + STATUS_COMPLETED + "'", null);

        if (completedCursor.moveToFirst()) {
            double completedRevenue = completedCursor.getDouble(0);
            Log.d("REVENUE_DEBUG", "TOTAL COMPLETED REVENUE: " + completedRevenue);
        }
        completedCursor.close();

        // Check order count by status
        String[] statuses = {STATUS_PENDING, STATUS_COMPLETED, STATUS_CANCELLED};
        for (String status : statuses) {
            Cursor statusCursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + ORDERS_TABLE + " WHERE " + STATUS + " = ?",
                    new String[]{status});
            if (statusCursor.moveToFirst()) {
                int count = statusCursor.getInt(0);
                Log.d("REVENUE_DEBUG", status + " orders: " + count);
            }
            statusCursor.close();
        }
    }

    private double getRevenue(SQLiteDatabase db, String startDate, String endDate) {
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + TOTAL_AMOUNT + ") FROM " + ORDERS_TABLE +
                        " WHERE DATE(" + ORDER_DATE + ") >= ? AND DATE(" + ORDER_DATE + ") <= ?" +
                        " AND " + STATUS + " = '" + STATUS_COMPLETED + "'", // ONLY COMPLETED ORDERS
                new String[]{startDate, endDate}
        );

        double revenue = 0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            revenue = cursor.getDouble(0);
        }
        cursor.close();
        return revenue;
    }

    private double getTotalRevenue(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + TOTAL_AMOUNT + ") FROM " + ORDERS_TABLE +
                        " WHERE " + STATUS + " = '" + STATUS_COMPLETED + "'", // ONLY COMPLETED ORDERS
                null
        );

        double revenue = 0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            revenue = cursor.getDouble(0);
        }
        cursor.close();
        return revenue;
    }

    private int getOrderCount(SQLiteDatabase db, String startDate, String endDate) {
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + ORDERS_TABLE +
                        " WHERE DATE(" + ORDER_DATE + ") >= ? AND DATE(" + ORDER_DATE + ") <= ?" +
                        " AND " + STATUS + " = '" + STATUS_COMPLETED + "'", // ONLY COMPLETED ORDERS
                new String[]{startDate, endDate}
        );

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    private int getTotalOrderCount(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + ORDERS_TABLE +
                        " WHERE " + STATUS + " = '" + STATUS_COMPLETED + "'", // ONLY COMPLETED ORDERS
                null
        );

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    private String getTopPizza(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery(
                "SELECT p." + PIZZA_NAME + ", COUNT(*) as order_count FROM " + ORDER_ITEMS_TABLE + " oi " +
                        "INNER JOIN " + PIZZAS_TABLE + " p ON oi." + PIZZA_ID + " = p." + PIZZA_ID +
                        " INNER JOIN " + ORDERS_TABLE + " o ON oi." + ORDER_ID + " = o." + ORDER_ID +
                        " WHERE o." + STATUS + " = '" + STATUS_COMPLETED + "'" + // ONLY FROM COMPLETED ORDERS
                        " GROUP BY p." + PIZZA_NAME +
                        " ORDER BY order_count DESC LIMIT 1", null
        );

        String topPizza = "N/A";
        if (cursor.moveToFirst()) {
            topPizza = cursor.getString(0);
        }
        cursor.close();
        return topPizza;
    }

    private int getTotalCustomers(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(DISTINCT " + USER_ID + ") FROM " + ORDERS_TABLE +
                        " WHERE " + STATUS + " = '" + STATUS_COMPLETED + "'", // ONLY CUSTOMERS WITH COMPLETED ORDERS
                null
        );

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    private String getDateDaysAgo(int days) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = new Date();
        date.setTime(date.getTime() - (days * 24 * 60 * 60 * 1000L));
        return sdf.format(date);
    }

    private void setupCharts() {
        Log.d("REVENUE_REFRESH", "setupCharts() called - Refreshing all charts");

        // Enable JavaScript for all WebViews
        webViewBarChart.getSettings().setJavaScriptEnabled(true);
        webViewPieChart.getSettings().setJavaScriptEnabled(true);
        webViewLineChart.getSettings().setJavaScriptEnabled(true);
        webViewDoughnutChart.getSettings().setJavaScriptEnabled(true);

        webViewBarChart.setBackgroundColor(Color.TRANSPARENT);
        webViewPieChart.setBackgroundColor(Color.TRANSPARENT);
        webViewLineChart.setBackgroundColor(Color.TRANSPARENT);
        webViewDoughnutChart.setBackgroundColor(Color.TRANSPARENT);

        // Load charts with fresh data
        int[] monthlyData = getMonthlyRevenueData();
        Log.d("REVENUE_REFRESH", "Monthly data refreshed");
        webViewBarChart.loadDataWithBaseURL(null, generateBarChartHTML(monthlyData), "text/html", "UTF-8", null);

        int[] categoryData = getCategoryData();
        Log.d("REVENUE_REFRESH", "Category data refreshed");
        webViewPieChart.loadDataWithBaseURL(null, generatePieChartHTML(categoryData), "text/html", "UTF-8", null);

        webViewLineChart.loadDataWithBaseURL(null, generateLineChartHTML(monthlyData), "text/html", "UTF-8", null);

        int[] statusData = getOrderStatusData();
        Log.d("REVENUE_REFRESH", "Status data refreshed");
        webViewDoughnutChart.loadDataWithBaseURL(null, generateDoughnutChartHTML(statusData), "text/html", "UTF-8", null);
    }

    private int[] getMonthlyRevenueData() {
        int[] data = new int[12];
        SQLiteDatabase db = database.getReadableDatabase();

        String[] months = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
        String currentYear = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());

        for (int i = 0; i < 12; i++) {
            String monthStart = currentYear + "-" + months[i] + "-01";
            String monthEnd = currentYear + "-" + months[i] + "-31";

            Cursor cursor = db.rawQuery(
                    "SELECT SUM(" + TOTAL_AMOUNT + ") FROM " + ORDERS_TABLE +
                            " WHERE DATE(" + ORDER_DATE + ") >= ? AND DATE(" + ORDER_DATE + ") <= ?" +
                            " AND " + STATUS + " = '" + STATUS_COMPLETED + "'", // ONLY COMPLETED ORDERS
                    new String[]{monthStart, monthEnd}
            );

            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                data[i] = (int) cursor.getDouble(0);
            } else {
                data[i] = 0;
            }
            cursor.close();
        }

        return data;
    }

    private int[] getCategoryData() {
        int[] data = new int[3]; // Classic, Specialty, Vegetarian
        SQLiteDatabase db = database.getReadableDatabase();

        for (int i = 1; i <= 3; i++) {
            Cursor cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + ORDER_ITEMS_TABLE + " oi " +
                            "INNER JOIN " + PIZZAS_TABLE + " p ON oi." + PIZZA_ID + " = p." + PIZZA_ID +
                            " INNER JOIN " + ORDERS_TABLE + " o ON oi." + ORDER_ID + " = o." + ORDER_ID +
                            " WHERE p." + CATEGORY_ID + " = ?" +
                            " AND o." + STATUS + " = '" + STATUS_COMPLETED + "'", // ONLY FROM COMPLETED ORDERS
                    new String[]{String.valueOf(i)}
            );

            if (cursor.moveToFirst()) {
                data[i-1] = cursor.getInt(0);
            } else {
                data[i-1] = 0;
            }
            cursor.close();
        }

        return data;
    }

    private int[] getOrderStatusData() {
        int[] data = new int[3]; // Pending, Completed, Cancelled
        SQLiteDatabase db = database.getReadableDatabase();
        String[] statuses = {STATUS_PENDING, STATUS_COMPLETED, STATUS_CANCELLED};

        for (int i = 0; i < 3; i++) {
            Cursor cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + ORDERS_TABLE + " WHERE " + STATUS + " = ?",
                    new String[]{statuses[i]}
            );

            if (cursor.moveToFirst()) {
                data[i] = cursor.getInt(0);
            } else {
                data[i] = 0;
            }
            cursor.close();
        }

        return data;
    }

    private String generateBarChartHTML(int[] data) {
        StringBuilder dataStr = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            dataStr.append(data[i]);
            if (i < data.length - 1) dataStr.append(", ");
        }

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "    <script src='https://cdn.jsdelivr.net/npm/chart.js@3.9.1/dist/chart.min.js'></script>\n" +
                "    <style>\n" +
                "        body { margin: 0; padding: 10px; background: transparent; }\n" +
                "        #chartContainer { position: relative; height: 300px; width: 100%; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id='chartContainer'><canvas id='myChart'></canvas></div>\n" +
                "    <script>\n" +
                "        const ctx = document.getElementById('myChart').getContext('2d');\n" +
                "        const gradient = ctx.createLinearGradient(0, 0, 0, 300);\n" +
                "        gradient.addColorStop(0, 'rgba(54, 162, 235, 0.9)');\n" +
                "        gradient.addColorStop(1, 'rgba(54, 162, 235, 0.3)');\n" +
                "        \n" +
                "        new Chart(ctx, {\n" +
                "            type: 'bar',\n" +
                "            data: {\n" +
                "                labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],\n" +
                "                datasets: [{\n" +
                "                    label: 'Monthly Revenue (GHS)',\n" +
                "                    data: [" + dataStr + "],\n" +
                "                    backgroundColor: gradient,\n" +
                "                    borderColor: 'rgba(54, 162, 235, 1)',\n" +
                "                    borderWidth: 2,\n" +
                "                    borderRadius: 10,\n" +
                "                    borderSkipped: false\n" +
                "                }]\n" +
                "            },\n" +
                "            options: {\n" +
                "                responsive: true,\n" +
                "                maintainAspectRatio: false,\n" +
                "                plugins: {\n" +
                "                    legend: { display: true, position: 'bottom', labels: { color: '#666', padding: 15, font: { size: 13, weight: '500' } } },\n" +
                "                    tooltip: { backgroundColor: 'rgba(0, 0, 0, 0.8)', padding: 12, cornerRadius: 8 }\n" +
                "                },\n" +
                "                scales: {\n" +
                "                    y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' }, ticks: { color: '#666', font: { size: 11 } } },\n" +
                "                    x: { grid: { display: false }, ticks: { color: '#666', font: { size: 11 } } }\n" +
                "                },\n" +
                "                animation: { duration: 1200, easing: 'easeInOutQuart' }\n" +
                "            }\n" +
                "        });\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }

    private String generateDoughnutChartHTML(int[] data) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "    <script src='https://cdn.jsdelivr.net/npm/chart.js@3.9.1/dist/chart.min.js'></script>\n" +
                "    <style>\n" +
                "        body { margin: 0; padding: 10px; background: transparent; }\n" +
                "        #chartContainer { position: relative; height: 280px; width: 100%; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id='chartContainer'><canvas id='myChart'></canvas></div>\n" +
                "    <script>\n" +
                "        const ctx = document.getElementById('myChart').getContext('2d');\n" +
                "        new Chart(ctx, {\n" +
                "            type: 'doughnut',\n" +
                "            data: {\n" +
                "                labels: ['Pending', 'Completed', 'Cancelled'],\n" +
                "                datasets: [{\n" +
                "                    data: [" + data[0] + ", " + data[1] + ", " + data[2] + "],\n" +
                "                    backgroundColor: [\n" +
                "                        'rgba(255, 206, 86, 0.8)',\n" +
                "                        'rgba(75, 192, 192, 0.8)',\n" +
                "                        'rgba(255, 99, 132, 0.8)'\n" +
                "                    ],\n" +
                "                    borderColor: [\n" +
                "                        'rgba(255, 206, 86, 1)',\n" +
                "                        'rgba(75, 192, 192, 1)',\n" +
                "                        'rgba(255, 99, 132, 1)'\n" +
                "                    ],\n" +
                "                    borderWidth: 2,\n" +
                "                    hoverOffset: 15\n" +
                "                }]\n" +
                "            },\n" +
                "            options: {\n" +
                "                responsive: true,\n" +
                "                maintainAspectRatio: false,\n" +
                "                cutout: '65%',\n" +
                "                plugins: {\n" +
                "                    legend: {\n" +
                "                        display: true,\n" +
                "                        position: 'bottom',\n" +
                "                        labels: {\n" +
                "                            color: '#666',\n" +
                "                            padding: 15,\n" +
                "                            font: { size: 12 },\n" +
                "                            usePointStyle: true,\n" +
                "                            pointStyle: 'circle'\n" +
                "                        }\n" +
                "                    },\n" +
                "                    tooltip: {\n" +
                "                        backgroundColor: 'rgba(0, 0, 0, 0.8)',\n" +
                "                        padding: 12,\n" +
                "                        cornerRadius: 8,\n" +
                "                        callbacks: {\n" +
                "                            label: function(context) {\n" +
                "                                let label = context.label || '';\n" +
                "                                let value = context.parsed || 0;\n" +
                "                                let total = context.dataset.data.reduce((a, b) => a + b, 0);\n" +
                "                                let percentage = ((value / total) * 100).toFixed(1);\n" +
                "                                return label + ': ' + value + ' (' + percentage + '%)';\n" +
                "                            }\n" +
                "                        }\n" +
                "                    }\n" +
                "                },\n" +
                "                animation: {\n" +
                "                    animateRotate: true,\n" +
                "                    animateScale: true,\n" +
                "                    duration: 1000\n" +
                "                }\n" +
                "            }\n" +
                "        });\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }

    private String generateLineChartHTML(int[] data) {
        StringBuilder dataStr = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            dataStr.append(data[i]);
            if (i < data.length - 1) dataStr.append(", ");
        }

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "    <script src='https://cdn.jsdelivr.net/npm/chart.js@3.9.1/dist/chart.min.js'></script>\n" +
                "    <style>\n" +
                "        body { margin: 0; padding: 10px; background: transparent; }\n" +
                "        #chartContainer { position: relative; height: 250px; width: 100%; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id='chartContainer'><canvas id='myChart'></canvas></div>\n" +
                "    <script>\n" +
                "        const ctx = document.getElementById('myChart').getContext('2d');\n" +
                "        const gradient = ctx.createLinearGradient(0, 0, 0, 250);\n" +
                "        gradient.addColorStop(0, 'rgba(75, 192, 192, 0.4)');\n" +
                "        gradient.addColorStop(1, 'rgba(75, 192, 192, 0.0)');\n" +
                "        \n" +
                "        new Chart(ctx, {\n" +
                "            type: 'line',\n" +
                "            data: {\n" +
                "                labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],\n" +
                "                datasets: [{\n" +
                "                    label: 'Revenue Trend',\n" +
                "                    data: [" + dataStr + "],\n" +
                "                    backgroundColor: gradient,\n" +
                "                    borderColor: 'rgba(75, 192, 192, 1)',\n" +
                "                    borderWidth: 3,\n" +
                "                    fill: true,\n" +
                "                    tension: 0.4,\n" +
                "                    pointBackgroundColor: 'rgba(75, 192, 192, 1)',\n" +
                "                    pointBorderColor: '#fff',\n" +
                "                    pointBorderWidth: 2,\n" +
                "                    pointRadius: 5,\n" +
                "                    pointHoverRadius: 7\n" +
                "                }]\n" +
                "            },\n" +
                "            options: {\n" +
                "                responsive: true,\n" +
                "                maintainAspectRatio: false,\n" +
                "                plugins: {\n" +
                "                    legend: { display: true, position: 'bottom', labels: { color: '#666', padding: 15 } }\n" +
                "                },\n" +
                "                scales: {\n" +
                "                    y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' }, ticks: { color: '#666' } },\n" +
                "                    x: { grid: { display: false }, ticks: { color: '#666' } }\n" +
                "                }\n" +
                "            }\n" +
                "        });\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }

    private String generatePieChartHTML(int[] data) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "    <script src='https://cdn.jsdelivr.net/npm/chart.js@3.9.1/dist/chart.min.js'></script>\n" +
                "    <style>\n" +
                "        body { margin: 0; padding: 10px; background: transparent; }\n" +
                "        #chartContainer { position: relative; height: 280px; width: 100%; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id='chartContainer'><canvas id='myChart'></canvas></div>\n" +
                "    <script>\n" +
                "        const ctx = document.getElementById('myChart').getContext('2d');\n" +
                "        new Chart(ctx, {\n" +
                "            type: 'pie',\n" +
                "            data: {\n" +
                "                labels: ['Classic', 'Specialty', 'Vegetarian'],\n" +
                "                datasets: [{\n" +
                "                    data: [" + data[0] + ", " + data[1] + ", " + data[2] + "],\n" +
                "                    backgroundColor: [\n" +
                "                        'rgba(255, 99, 132, 0.8)',\n" +
                "                        'rgba(54, 162, 235, 0.8)',\n" +
                "                        'rgba(75, 192, 192, 0.8)'\n" +
                "                    ],\n" +
                "                    borderColor: [\n" +
                "                        'rgba(255, 99, 132, 1)',\n" +
                "                        'rgba(54, 162, 235, 1)',\n" +
                "                        'rgba(75, 192, 192, 1)'\n" +
                "                    ],\n" +
                "                    borderWidth: 2,\n" +
                "                    hoverOffset: 15\n" +
                "                }]\n" +
                "            },\n" +
                "            options: {\n" +
                "                responsive: true,\n" +
                "                maintainAspectRatio: false,\n" +
                "                plugins: {\n" +
                "                    legend: {\n" +
                "                        display: true,\n" +
                "                        position: 'bottom',\n" +
                "                        labels: {\n" +
                "                            color: '#666',\n" +
                "                            padding: 15,\n" +
                "                            font: { size: 12 },\n" +
                "                            usePointStyle: true,\n" +
                "                            pointStyle: 'circle'\n" +
                "                        }\n" +
                "                    },\n" +
                "                    tooltip: {\n" +
                "                        backgroundColor: 'rgba(0, 0, 0, 0.8)',\n" +
                "                        padding: 12,\n" +
                "                        cornerRadius: 8,\n" +
                "                        callbacks: {\n" +
                "                            label: function(context) {\n" +
                "                                let label = context.label || '';\n" +
                "                                let value = context.parsed || 0;\n" +
                "                                let total = context.dataset.data.reduce((a, b) => a + b, 0);\n" +
                "                                let percentage = ((value / total) * 100).toFixed(1);\n" +
                "                                return label + ': ' + value + ' orders (' + percentage + '%)';\n" +
                "                            }\n" +
                "                        }\n" +
                "                    }\n" +
                "                },\n" +
                "                animation: {\n" +
                "                    animateRotate: true,\n" +
                "                    animateScale: true,\n" +
                "                    duration: 1000\n" +
                "                }\n" +
                "            }\n" +
                "        });\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }
}
