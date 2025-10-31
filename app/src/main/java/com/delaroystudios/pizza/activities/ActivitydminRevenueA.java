package com.delaroystudios.pizza.activities;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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

        // Delay chart setup to ensure WebView is fully initialized
        new Handler().postDelayed(() -> {
            setupChartsWithDebug();
        }, 500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh all data when activity becomes visible
        Log.d("REVENUE_REFRESH", "onResume() called - Refreshing data");
        loadRevenueData();

        new Handler().postDelayed(() -> {
            setupChartsWithDebug();
        }, 500);

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

        // Today's Revenue (ONLY COMPLETED ORDERS)
        double todayRevenue = getRevenue(db, today, today);
        tvTodayRevenue.setText(String.format(Locale.getDefault(), "GHS %.2f", todayRevenue));

        // Week's Revenue (ONLY COMPLETED ORDERS)
        String weekStart = getDateDaysAgo(7);
        double weekRevenue = getRevenue(db, weekStart, today);
        tvWeekRevenue.setText(String.format(Locale.getDefault(), "GHS %.2f", weekRevenue));

        // Month's Revenue (ONLY COMPLETED ORDERS)
        String monthStart = getDateDaysAgo(30);
        double monthRevenue = getRevenue(db, monthStart, today);
        tvMonthRevenue.setText(String.format(Locale.getDefault(), "GHS %.2f", monthRevenue));

        // Total Revenue (ONLY COMPLETED ORDERS)
        double totalRevenue = getTotalRevenue(db);
        tvTotalRevenue.setText(String.format(Locale.getDefault(), "GHS %.2f", totalRevenue));

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
    }

    private double getRevenue(SQLiteDatabase db, String startDate, String endDate) {
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + TOTAL_AMOUNT + ") FROM " + ORDERS_TABLE +
                        " WHERE DATE(" + ORDER_DATE + ") >= ? AND DATE(" + ORDER_DATE + ") <= ?" +
                        " AND " + STATUS + " = '" + STATUS_COMPLETED + "'",
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
                        " WHERE " + STATUS + " = '" + STATUS_COMPLETED + "'",
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
                        " AND " + STATUS + " = '" + STATUS_COMPLETED + "'",
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
                        " WHERE " + STATUS + " = '" + STATUS_COMPLETED + "'",
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
                        " WHERE o." + STATUS + " = '" + STATUS_COMPLETED + "'" +
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
                        " WHERE " + STATUS + " = '" + STATUS_COMPLETED + "'",
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

    private void setupChartsWithDebug() {
        Log.d("CHART_DEBUG", "Starting enhanced chart setup...");

        // Enhanced WebView settings for physical devices
        WebView[] webViews = {webViewBarChart, webViewPieChart, webViewLineChart, webViewDoughnutChart};
        for (WebView webView : webViews) {
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setDatabaseEnabled(true);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setBuiltInZoomControls(false);
            webView.getSettings().setDisplayZoomControls(false);
            webView.getSettings().setSupportZoom(false);
            webView.getSettings().setAllowFileAccess(true);
            webView.setBackgroundColor(Color.TRANSPARENT);

            // Try software rendering first for better compatibility
            webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);

            // Clear cache to avoid stale content
            webView.clearCache(true);
            webView.clearHistory();
        }

        // Check WebView dimensions
        webViewBarChart.post(() -> {
            Log.d("CHART_DEBUG", "Bar Chart WebView dimensions: " +
                    webViewBarChart.getWidth() + "x" + webViewBarChart.getHeight());
            Log.d("CHART_DEBUG", "Bar Chart WebView isShown: " + webViewBarChart.isShown());
        });

        // Load charts with delay to ensure WebView is ready
        new Handler().postDelayed(() -> {
            int[] monthlyData = getMonthlyRevenueData();
            Log.d("CHART_DEBUG", "Monthly data: " + java.util.Arrays.toString(monthlyData));

            webViewBarChart.loadDataWithBaseURL("https://example.com/",
                    generateEnhancedBarChartHTML(monthlyData), "text/html", "UTF-8", null);

            int[] categoryData = getCategoryData();
            webViewPieChart.loadDataWithBaseURL("https://example.com/",
                    generateEnhancedPieChartHTML(categoryData), "text/html", "UTF-8", null);

            webViewLineChart.loadDataWithBaseURL("https://example.com/",
                    generateEnhancedLineChartHTML(monthlyData), "text/html", "UTF-8", null);

            int[] statusData = getOrderStatusData();
            webViewDoughnutChart.loadDataWithBaseURL("https://example.com/",
                    generateEnhancedDoughnutChartHTML(statusData), "text/html", "UTF-8", null);
        }, 300);
    }

    private void setupCharts() {
        Log.d("REVENUE_REFRESH", "setupCharts() called - Refreshing all charts");

        // Enhanced WebView settings
        WebView[] webViews = {webViewBarChart, webViewPieChart, webViewLineChart, webViewDoughnutChart};
        for (WebView webView : webViews) {
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setBuiltInZoomControls(false);
            webView.getSettings().setDisplayZoomControls(false);
            webView.setBackgroundColor(Color.TRANSPARENT);
            webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        }

        // Load charts with fresh data
        int[] monthlyData = getMonthlyRevenueData();
        webViewBarChart.loadDataWithBaseURL("https://example.com/",
                generateEnhancedBarChartHTML(monthlyData), "text/html", "UTF-8", null);

        int[] categoryData = getCategoryData();
        webViewPieChart.loadDataWithBaseURL("https://example.com/",
                generateEnhancedPieChartHTML(categoryData), "text/html", "UTF-8", null);

        webViewLineChart.loadDataWithBaseURL("https://example.com/",
                generateEnhancedLineChartHTML(monthlyData), "text/html", "UTF-8", null);

        int[] statusData = getOrderStatusData();
        webViewDoughnutChart.loadDataWithBaseURL("https://example.com/",
                generateEnhancedDoughnutChartHTML(statusData), "text/html", "UTF-8", null);
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
                            " AND " + STATUS + " = '" + STATUS_COMPLETED + "'",
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
                            " AND o." + STATUS + " = '" + STATUS_COMPLETED + "'",
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

    private String generateEnhancedBarChartHTML(int[] data) {
        StringBuilder dataStr = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            dataStr.append(data[i]);
            if (i < data.length - 1) dataStr.append(", ");
        }

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0, user-scalable=no'>\n" +
                "    <script src='https://cdn.jsdelivr.net/npm/chart.js'></script>\n" +
                "    <style>\n" +
                "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "        html, body { width: 100%; height: 100%; background: transparent; overflow: hidden; }\n" +
                "        .chart-wrapper { \n" +
                "            width: 100%; \n" +
                "            height: 100%; \n" +
                "            padding: 15px 10px 60px 10px;\n" +
                "            background: transparent;\n" +
                "        }\n" +
                "        .chart-container { \n" +
                "            width: 100%; \n" +
                "            height: 100%; \n" +
                "            position: relative;\n" +
                "            background: transparent;\n" +
                "        }\n" +
                "        canvas { \n" +
                "            display: block !important; \n" +
                "            width: 100% !important; \n" +
                "            height: 100% !important; \n" +
                "            background: transparent;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='chart-wrapper'>\n" +
                "        <div class='chart-container'>\n" +
                "            <canvas id='barChart'></canvas>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    <script>\n" +
                "        function initializeChart() {\n" +
                "            console.log('Initializing chart...');\n" +
                "            \n" +
                "            // Check if canvas element exists\n" +
                "            var canvas = document.getElementById('barChart');\n" +
                "            if (!canvas) {\n" +
                "                console.error('Canvas element not found!');\n" +
                "                return;\n" +
                "            }\n" +
                "            \n" +
                "            var ctx = canvas.getContext('2d');\n" +
                "            \n" +
                "            // Force canvas dimensions\n" +
                "            var container = canvas.parentElement.parentElement;\n" +
                "            canvas.width = container.offsetWidth;\n" +
                "            canvas.height = container.offsetHeight;\n" +
                "            \n" +
                "            console.log('Canvas dimensions:', canvas.width, 'x', canvas.height);\n" +
                "            \n" +
                "            try {\n" +
                "                var chart = new Chart(ctx, {\n" +
                "                    type: 'bar',\n" +
                "                    data: {\n" +
                "                        labels: ['JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG', 'SEP', 'OCT', 'NOV', 'DEC'],\n" +
                "                        datasets: [{\n" +
                "                            label: 'Monthly Revenue (GHS)',\n" +
                "                            data: [" + dataStr + "],\n" +
                "                            backgroundColor: '#4F46E5',\n" +
                "                            borderColor: '#3730A3',\n" +
                "                            borderWidth: 1,\n" +
                "                            borderRadius: 4,\n" +
                "                            barPercentage: 0.7\n" +
                "                        }]\n" +
                "                    },\n" +
                "                    options: {\n" +
                "                        responsive: true,\n" +
                "                        maintainAspectRatio: false,\n" +
                "                        layout: {\n" +
                "                            padding: {\n" +
                "                                top: 10,\n" +
                "                                right: 10,\n" +
                "                                bottom: 50,\n" +
                "                                left: 10\n" +
                "                            }\n" +
                "                        },\n" +
                "                        plugins: {\n" +
                "                            legend: {\n" +
                "                                display: true,\n" +
                "                                position: 'top',\n" +
                "                                labels: {\n" +
                "                                    color: '#374151',\n" +
                "                                    font: { size: 14, weight: 'bold' },\n" +
                "                                    padding: 20\n" +
                "                                }\n" +
                "                            },\n" +
                "                            tooltip: {\n" +
                "                                backgroundColor: 'rgba(0,0,0,0.8)',\n" +
                "                                padding: 12,\n" +
                "                                cornerRadius: 6,\n" +
                "                                titleFont: { size: 14 },\n" +
                "                                bodyFont: { size: 14 }\n" +
                "                            }\n" +
                "                        },\n" +
                "                        scales: {\n" +
                "                            y: {\n" +
                "                                beginAtZero: true,\n" +
                "                                grid: {\n" +
                "                                    color: 'rgba(0,0,0,0.1)',\n" +
                "                                    drawBorder: false\n" +
                "                                },\n" +
                "                                ticks: {\n" +
                "                                    color: '#6B7280',\n" +
                "                                    font: { size: 12 },\n" +
                "                                    padding: 8,\n" +
                "                                    callback: function(value) {\n" +
                "                                        return 'GHS ' + value.toLocaleString();\n" +
                "                                    }\n" +
                "                                }\n" +
                "                            },\n" +
                "                            x: {\n" +
                "                                grid: {\n" +
                "                                    display: false,\n" +
                "                                    drawBorder: false\n" +
                "                                },\n" +
                "                                ticks: {\n" +
                "                                    color: '#374151',\n" +
                "                                    font: { size: 12, weight: 'bold' },\n" +
                "                                    padding: 10,\n" +
                "                                    maxRotation: 0,\n" +
                "                                    minRotation: 0\n" +
                "                                }\n" +
                "                            }\n" +
                "                        },\n" +
                "                        animation: {\n" +
                "                            duration: 1000,\n" +
                "                            easing: 'easeOutQuart'\n" +
                "                        }\n" +
                "                    }\n" +
                "                });\n" +
                "                \n" +
                "                console.log('Chart created successfully');\n" +
                "                \n" +
                "            } catch (error) {\n" +
                "                console.error('Chart creation failed:', error);\n" +
                "            }\n" +
                "        }\n" +
                "        \n" +
                "        // Initialize when DOM is ready\n" +
                "        if (document.readyState === 'loading') {\n" +
                "            document.addEventListener('DOMContentLoaded', initializeChart);\n" +
                "        } else {\n" +
                "            initializeChart();\n" +
                "        }\n" +
                "        \n" +
                "        // Also try initializing after a short delay\n" +
                "        setTimeout(initializeChart, 100);\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }

    private String generateEnhancedPieChartHTML(int[] data) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0, user-scalable=no'>\n" +
                "    <script src='https://cdn.jsdelivr.net/npm/chart.js'></script>\n" +
                "    <style>\n" +
                "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "        html, body { width: 100%; height: 100%; background: transparent; overflow: hidden; }\n" +
                "        .chart-wrapper { width: 100%; height: 100%; padding: 20px; background: transparent; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='chart-wrapper'><canvas id='pieChart'></canvas></div>\n" +
                "    <script>\n" +
                "        function initializeChart() {\n" +
                "            var canvas = document.getElementById('pieChart');\n" +
                "            if (!canvas) return;\n" +
                "            \n" +
                "            var ctx = canvas.getContext('2d');\n" +
                "            var container = canvas.parentElement;\n" +
                "            canvas.width = container.offsetWidth;\n" +
                "            canvas.height = container.offsetHeight;\n" +
                "            \n" +
                "            new Chart(ctx, {\n" +
                "                type: 'pie',\n" +
                "                data: {\n" +
                "                    labels: ['Classic', 'Specialty', 'Vegetarian'],\n" +
                "                    datasets: [{\n" +
                "                        data: [" + data[0] + ", " + data[1] + ", " + data[2] + "],\n" +
                "                        backgroundColor: ['#FF6384', '#36A2EB', '#4BC0C0'],\n" +
                "                        borderWidth: 2\n" +
                "                    }]\n" +
                "                },\n" +
                "                options: {\n" +
                "                    responsive: true,\n" +
                "                    maintainAspectRatio: false,\n" +
                "                    plugins: {\n" +
                "                        legend: { position: 'bottom' }\n" +
                "                    }\n" +
                "                }\n" +
                "            });\n" +
                "        }\n" +
                "        \n" +
                "        if (document.readyState === 'loading') {\n" +
                "            document.addEventListener('DOMContentLoaded', initializeChart);\n" +
                "        } else {\n" +
                "            initializeChart();\n" +
                "        }\n" +
                "        setTimeout(initializeChart, 100);\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }

    private String generateEnhancedLineChartHTML(int[] data) {
        StringBuilder dataStr = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            dataStr.append(data[i]);
            if (i < data.length - 1) dataStr.append(", ");
        }

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0, user-scalable=no'>\n" +
                "    <script src='https://cdn.jsdelivr.net/npm/chart.js'></script>\n" +
                "    <style>\n" +
                "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "        html, body { width: 100%; height: 100%; background: transparent; overflow: hidden; }\n" +
                "        .chart-wrapper { width: 100%; height: 100%; padding: 20px; background: transparent; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='chart-wrapper'><canvas id='lineChart'></canvas></div>\n" +
                "    <script>\n" +
                "        function initializeChart() {\n" +
                "            var canvas = document.getElementById('lineChart');\n" +
                "            if (!canvas) return;\n" +
                "            \n" +
                "            var ctx = canvas.getContext('2d');\n" +
                "            var container = canvas.parentElement;\n" +
                "            canvas.width = container.offsetWidth;\n" +
                "            canvas.height = container.offsetHeight;\n" +
                "            \n" +
                "            new Chart(ctx, {\n" +
                "                type: 'line',\n" +
                "                data: {\n" +
                "                    labels: ['JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG', 'SEP', 'OCT', 'NOV', 'DEC'],\n" +
                "                    datasets: [{\n" +
                "                        label: 'Revenue Trend',\n" +
                "                        data: [" + dataStr + "],\n" +
                "                        borderColor: '#4BC0C0',\n" +
                "                        backgroundColor: 'rgba(75, 192, 192, 0.1)',\n" +
                "                        borderWidth: 3,\n" +
                "                        fill: true,\n" +
                "                        tension: 0.4\n" +
                "                    }]\n" +
                "                },\n" +
                "                options: {\n" +
                "                    responsive: true,\n" +
                "                    maintainAspectRatio: false,\n" +
                "                    plugins: {\n" +
                "                        legend: { position: 'top' }\n" +
                "                    },\n" +
                "                    scales: {\n" +
                "                        x: {\n" +
                "                            ticks: {\n" +
                "                                maxRotation: 0,\n" +
                "                                minRotation: 0\n" +
                "                            }\n" +
                "                        }\n" +
                "                    }\n" +
                "                }\n" +
                "            });\n" +
                "        }\n" +
                "        \n" +
                "        if (document.readyState === 'loading') {\n" +
                "            document.addEventListener('DOMContentLoaded', initializeChart);\n" +
                "        } else {\n" +
                "            initializeChart();\n" +
                "        }\n" +
                "        setTimeout(initializeChart, 100);\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }

    private String generateEnhancedDoughnutChartHTML(int[] data) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0, user-scalable=no'>\n" +
                "    <script src='https://cdn.jsdelivr.net/npm/chart.js'></script>\n" +
                "    <style>\n" +
                "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "        html, body { width: 100%; height: 100%; background: transparent; overflow: hidden; }\n" +
                "        .chart-wrapper { width: 100%; height: 100%; padding: 20px; background: transparent; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='chart-wrapper'><canvas id='doughnutChart'></canvas></div>\n" +
                "    <script>\n" +
                "        function initializeChart() {\n" +
                "            var canvas = document.getElementById('doughnutChart');\n" +
                "            if (!canvas) return;\n" +
                "            \n" +
                "            var ctx = canvas.getContext('2d');\n" +
                "            var container = canvas.parentElement;\n" +
                "            canvas.width = container.offsetWidth;\n" +
                "            canvas.height = container.offsetHeight;\n" +
                "            \n" +
                "            new Chart(ctx, {\n" +
                "                type: 'doughnut',\n" +
                "                data: {\n" +
                "                    labels: ['Pending', 'Completed', 'Cancelled'],\n" +
                "                    datasets: [{\n" +
                "                        data: [" + data[0] + ", " + data[1] + ", " + data[2] + "],\n" +
                "                        backgroundColor: ['#FFCE56', '#4BC0C0', '#FF6384'],\n" +
                "                        borderWidth: 2\n" +
                "                    }]\n" +
                "                },\n" +
                "                options: {\n" +
                "                    responsive: true,\n" +
                "                    maintainAspectRatio: false,\n" +
                "                    cutout: '60%',\n" +
                "                    plugins: {\n" +
                "                        legend: { position: 'bottom' }\n" +
                "                    }\n" +
                "                }\n" +
                "            });\n" +
                "        }\n" +
                "        \n" +
                "        if (document.readyState === 'loading') {\n" +
                "            document.addEventListener('DOMContentLoaded', initializeChart);\n" +
                "        } else {\n" +
                "            initializeChart();\n" +
                "        }\n" +
                "        setTimeout(initializeChart, 100);\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }

    // Force refresh method
    private void forceRefreshCharts() {
        Log.d("CHART_DEBUG", "Forcing chart refresh...");

        // Clear WebView cache
        webViewBarChart.clearCache(true);
        webViewPieChart.clearCache(true);
        webViewLineChart.clearCache(true);
        webViewDoughnutChart.clearCache(true);

        // Reload charts with delay
        new Handler().postDelayed(() -> {
            setupChartsWithDebug();
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }
}