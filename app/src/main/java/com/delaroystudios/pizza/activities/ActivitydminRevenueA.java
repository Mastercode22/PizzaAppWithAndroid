// ActivitydminRevenueA.java - Updated Implementation with Revenue Goal Tracker

package com.delaroystudios.pizza.activities;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.database.PizzaData;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.delaroystudios.pizza.database.Constants.*;

public class ActivitydminRevenueA extends Activity {

    private TextView tvTodayRevenue, tvWeekRevenue, tvMonthRevenue, tvTotalRevenue;
    private TextView tvTodayOrders, tvWeekOrders, tvMonthOrders, tvTotalOrders;
    private TextView tvAvgOrderValue, tvTopPizza, tvTotalCustomers;

    // Revenue Goal Tracker Components
    private TextView tvActualRevenue, tvTargetRevenue, tvProgressPercentage;
    private ProgressBar progressBarGoal;

    private WebView webViewPieChart, webViewLineChart, webViewDoughnutChart;
    private PizzaData database;

    // Revenue Goal Constants
    private static final float MONTHLY_TARGET = 5000f;

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

        // Initialize Revenue Goal Tracker Components
        tvActualRevenue = findViewById(R.id.tv_actual_revenue);
        tvTargetRevenue = findViewById(R.id.tv_target_revenue);
        tvProgressPercentage = findViewById(R.id.tv_progress_percentage);
        progressBarGoal = findViewById(R.id.progress_bar_goal);

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
        String monthStart = getFirstDayOfCurrentMonth();
        double monthRevenue = getRevenue(db, monthStart, today);
        tvMonthRevenue.setText(String.format(Locale.getDefault(), "GHS %.2f", monthRevenue));

        // Total Revenue (ONLY COMPLETED ORDERS)
        double totalRevenue = getTotalRevenue(db);
        tvTotalRevenue.setText(String.format(Locale.getDefault(), "GHS %.2f", totalRevenue));

        // Today's Orders (ONLY COMPLETED ORDERS)
        int todayOrders = getOrderCount(db, today, today);
        tvTodayOrders.setText(String.valueOf(todayOrders) + " orders");

        // Week's Orders (ONLY COMPLETED ORDERS)
        int weekOrders = getOrderCount(db, weekStart, today);
        tvWeekOrders.setText(String.valueOf(weekOrders) + " orders");

        // Month's Orders (ONLY COMPLETED ORDERS)
        int monthOrders = getOrderCount(db, monthStart, today);
        tvMonthOrders.setText(String.valueOf(monthOrders) + " orders");

        // Total Orders (ONLY COMPLETED ORDERS)
        int totalOrders = getTotalOrderCount(db);
        tvTotalOrders.setText(String.valueOf(totalOrders) + " orders");

        // Average Order Value (ONLY COMPLETED ORDERS)
        double avgOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;
        tvAvgOrderValue.setText(String.format(Locale.getDefault(), "GHS %.2f", avgOrderValue));

        // Top Pizza (FROM COMPLETED ORDERS)
        String topPizza = getTopPizza(db);
        tvTopPizza.setText(topPizza);

        // Total Customers
        int totalCustomers = getTotalCustomers(db);
        tvTotalCustomers.setText(String.valueOf(totalCustomers));

        // ===== UPDATE REVENUE GOAL TRACKER =====
        updateRevenueGoalTracker(monthRevenue);
    }

    /**
     * Updates the Revenue Goal Tracker with current month's revenue
     * @param actualRevenue The actual revenue for the current month
     */
    private void updateRevenueGoalTracker(double actualRevenue) {
        // Convert to float for calculations
        float actualRevenueFloat = (float) actualRevenue;

        // Calculate percentage
        float percentageComplete = (actualRevenueFloat / MONTHLY_TARGET) * 100f;
        int progressInt = Math.min((int) percentageComplete, 100); // Cap at 100%

        // --- FIX: Change method call to pass float values for animation ---
        // We'll start the animation from 0.0f for simplicity in this example.
        float startValue = 0.0f;
        animateTextChange(tvActualRevenue, startValue, actualRevenueFloat);

        // Update Target Revenue TextView
        tvTargetRevenue.setText(String.format(java.util.Locale.getDefault(), "Target: GHS %.2f", MONTHLY_TARGET));

        // Update ProgressBar with MAX value
        progressBarGoal.setMax(100);

        // DYNAMIC COLOR LOGIC - Change progress bar color based on percentage
        int progressColor;
        String statusMessage;
        int statusColor;

        if (percentageComplete >= 100) {
            // GREEN - Target Achieved
            statusMessage = String.format(java.util.Locale.getDefault(), "%.1f%% Complete - Target Achieved! 🎉", percentageComplete);
            statusColor = android.graphics.Color.parseColor("#4CAF50"); // Green
            progressColor = android.graphics.Color.parseColor("#4CAF50"); // Green
        } else if (percentageComplete >= 50) {
            // BLUE - Good Progress / On Track
            statusMessage = String.format(java.util.Locale.getDefault(), "%.1f%% Complete - On Track ✓", percentageComplete);
            statusColor = android.graphics.Color.parseColor("#2196F3"); // Blue
            progressColor = android.graphics.Color.parseColor("#2196F3"); // Blue
        } else {
            // RED - Needs Attention
            statusMessage = String.format(java.util.Locale.getDefault(), "%.1f%% Complete - Needs Attention", percentageComplete);
            statusColor = android.graphics.Color.parseColor("#F44336"); // Red
            progressColor = android.graphics.Color.parseColor("#F44336"); // Red
        }

        // Apply dynamic color to progress bar
        setProgressBarColor(progressColor);

        // Update status text and color
        tvProgressPercentage.setText(statusMessage);
        tvProgressPercentage.setTextColor(statusColor);

        // Animate the progress bar
        animateProgressBar(progressInt);
    }

    // --------------------------------------------------------------------------------------------------

    /**
     * Animates the change of the TextView value, counting up to the final number.
     * THIS METHOD IS ADDED TO RESOLVE THE ERROR.
     * @param textView The TextView to update (e.g., tvActualRevenue)
     * @param startValue The starting numerical value (the previous revenue or 0)
     * @param endValue The final numerical value (the new actualRevenueFloat)
     */
    private void animateTextChange(final android.widget.TextView textView, float startValue, float endValue) {
        android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofFloat(startValue, endValue);
        animator.setDuration(800);

        animator.addUpdateListener(new android.animation.ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(android.animation.ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                textView.setText(String.format(java.util.Locale.getDefault(), "GHS %.2f", animatedValue));
            }
        });
        animator.start();
    }

    // --------------------------------------------------------------------------------------------------

    /**
     * Sets the progress bar color dynamically
     * @param color The color to apply to the progress bar
     */
    private void setProgressBarColor(int color) {
        // Create a layer drawable for the progress bar
        android.graphics.drawable.LayerDrawable layerDrawable =
                (android.graphics.drawable.LayerDrawable) progressBarGoal.getProgressDrawable();

        if (layerDrawable != null) {
            // Get the progress drawable (index 1 in the layer list)
            android.graphics.drawable.Drawable progressDrawable = layerDrawable.findDrawableByLayerId(android.R.id.progress);

            if (progressDrawable != null) {
                // Apply color filter to change the color
                progressDrawable.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
            }
        } else {
            // Fallback: If no layer drawable, apply color directly
            progressBarGoal.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    // --------------------------------------------------------------------------------------------------

    /**
     * Animates the progress bar from 0 to target value
     * @param targetProgress The target progress value
     */
    private void animateProgressBar(int targetProgress) {
        progressBarGoal.setProgress(0);

        android.os.Handler handler = new android.os.Handler();
        final int[] currentProgress = {0};
        final int step = Math.max(1, targetProgress / 50); // Animate in ~50 steps

        java.lang.Runnable runnable = new java.lang.Runnable() {
            @Override
            public void run() {
                if (currentProgress[0] < targetProgress) {
                    currentProgress[0] = Math.min(currentProgress[0] + step, targetProgress);
                    progressBarGoal.setProgress(currentProgress[0]);
                    handler.postDelayed(this, 20); // Update every 20ms
                }
            }
        };

        handler.postDelayed(runnable, 100); // Start after 100ms delay
    }
    /**
     * Gets the first day of the current month in yyyy-MM-dd format
     */
    private String getFirstDayOfCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(calendar.getTime());
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
        WebView[] webViews = {webViewPieChart, webViewLineChart, webViewDoughnutChart};
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
            webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
            webView.clearCache(true);
            webView.clearHistory();
        }

        // Load charts with delay to ensure WebView is ready
        new Handler().postDelayed(() -> {
            int[] categoryData = getCategoryData();
            webViewPieChart.loadDataWithBaseURL("https://example.com/",
                    generateEnhancedPieChartHTML(categoryData), "text/html", "UTF-8", null);

            int[] monthlyData = getMonthlyRevenueData();
            webViewLineChart.loadDataWithBaseURL("https://example.com/",
                    generateEnhancedLineChartHTML(monthlyData), "text/html", "UTF-8", null);

            int[] statusData = getOrderStatusData();
            webViewDoughnutChart.loadDataWithBaseURL("https://example.com/",
                    generateEnhancedDoughnutChartHTML(statusData), "text/html", "UTF-8", null);
        }, 300);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }
}