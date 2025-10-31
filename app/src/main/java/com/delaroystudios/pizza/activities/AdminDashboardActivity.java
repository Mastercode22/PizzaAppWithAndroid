package com.delaroystudios.pizza.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.adapters.OrderAdapter;
import com.delaroystudios.pizza.database.DatabaseHelper;
import com.delaroystudios.pizza.database.PizzaData;
import com.delaroystudios.pizza.models.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.delaroystudios.pizza.database.Constants.*;

public class AdminDashboardActivity extends AppCompatActivity implements View.OnClickListener, OrderAdapter.OnOrderClickListener {

    private TextView tvTotalOrders, tvTotalRevenue, tvPendingOrders;
    private TextView tvTodayOrders, tvWeekRevenue, tvTotalCustomers;
    private RecyclerView rvRecentOrders;
    private WebView webViewChart;
    private DatabaseHelper databaseHelper;
    private PizzaData pizzaData;
    private OrderAdapter recentOrdersAdapter;
    private List<Order> recentOrdersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Force Light Mode for the Admin Dashboard
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        databaseHelper = new DatabaseHelper(this);
        pizzaData = new PizzaData(this);

        initViews();
        setupClickListeners();
        setupRecentOrdersRecyclerView();
        loadDashboardData();
        loadRecentOrders();
        setupChart();
    }

    private void initViews() {
        tvTotalOrders = findViewById(R.id.tv_total_orders);
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);
        tvPendingOrders = findViewById(R.id.tv_pending_orders);
        tvTodayOrders = findViewById(R.id.tv_today_orders);
        tvWeekRevenue = findViewById(R.id.tv_week_revenue);
        tvTotalCustomers = findViewById(R.id.tv_total_customers);
        rvRecentOrders = findViewById(R.id.rv_recent_orders);
        webViewChart = findViewById(R.id.webview_chart);

        // Enable JavaScript for chart
        webViewChart.getSettings().setJavaScriptEnabled(true);
        webViewChart.getSettings().setDomStorageEnabled(true);
        webViewChart.setBackgroundColor(Color.TRANSPARENT);

        findViewById(R.id.btn_logout).setOnClickListener(this);
    }

    private void setupClickListeners() {
        findViewById(R.id.card_view_orders).setOnClickListener(this);
        findViewById(R.id.card_manage_pizzas).setOnClickListener(this);
        findViewById(R.id.card_view_customers).setOnClickListener(this);
        findViewById(R.id.card_settings).setOnClickListener(this);
        findViewById(R.id.card_total_orders).setOnClickListener(this);
        findViewById(R.id.card_revenue).setOnClickListener(this);
        findViewById(R.id.card_pending_orders).setOnClickListener(this);
    }

    private void setupRecentOrdersRecyclerView() {
        rvRecentOrders.setLayoutManager(new LinearLayoutManager(this));
        rvRecentOrders.setNestedScrollingEnabled(false);
    }

    private void setupChart() {
        int[] currentYearData = getMonthlyRevenueData(0);
        int[] previousYearData = getMonthlyRevenueData(-1);

        String htmlData = generateChartHTML(currentYearData, previousYearData);
        webViewChart.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null);
    }

    /**
     * Gets monthly revenue data - ONLY COMPLETED ORDERS
     */
    private int[] getMonthlyRevenueData(int yearOffset) {
        int[] data = new int[12];
        SQLiteDatabase db = pizzaData.getReadableDatabase();

        String[] months = {"01","02","03","04","05","06","07","08","09","10","11","12"};
        String currYearStr = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());
        int targetYear = Integer.parseInt(currYearStr) + yearOffset;

        for (int i = 0; i < 12; i++) {
            String monthStart = targetYear + "-" + months[i] + "-01";
            String monthEnd = targetYear + "-" + months[i] + "-31";

            Cursor cursor = db.rawQuery(
                    "SELECT SUM(" + TOTAL_AMOUNT + ") FROM " + ORDERS_TABLE +
                            " WHERE DATE(" + ORDER_DATE + ") >= ? AND DATE(" + ORDER_DATE + ") <= ?" +
                            " AND " + STATUS + " = '" + STATUS_COMPLETED + "'",
                    new String[]{monthStart, monthEnd}
            );

            double val = 0.0;
            if (cursor.moveToFirst()) {
                if (!cursor.isNull(0)) {
                    val = cursor.getDouble(0);
                }
            }
            data[i] = (int) Math.round(val);
            cursor.close();
        }
        return data;
    }

    private String generateChartHTML(int[] currYear, int[] prevYear) {
        StringBuilder sbCurr = new StringBuilder();
        StringBuilder sbPrev = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sbCurr.append(currYear[i]);
            sbPrev.append(prevYear[i]);
            if (i < 11) {
                sbCurr.append(", ");
                sbPrev.append(", ");
            }
        }

        return "<!doctype html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta name='viewport' content='width=device-width, initial-scale=1'>\n" +
                "  <link href='https://fonts.googleapis.com/css2?family=Montserrat:wght@600;700;800&family=Roboto:wght@300;400&display=swap' rel='stylesheet'>\n" +
                "  <script src='https://cdn.jsdelivr.net/npm/chart.js'></script>\n" +
                "  <style>\n" +
                "    body { margin:0; padding:20px; padding-bottom:80px; background:#ffffff; font-family:Roboto,sans-serif; }\n" +
                "    .title { text-align:center; color:#E04843; font-family:'Montserrat',sans-serif; font-weight:700; letter-spacing:3px; font-size:24px; }\n" +
                "    #chart-wrap { width:100%; height:280px; padding-bottom:60px; margin-bottom:20px; }\n" +
                "    canvas { width:100% !important; height:100% !important; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div id='chart-wrap'><canvas id='myChart'></canvas></div>\n" +
                "  <script>\n" +
                "    const ctx = document.getElementById('myChart').getContext('2d');\n" +
                "    const gradientCurr = ctx.createLinearGradient(0,0,0,400);\n" +
                "    gradientCurr.addColorStop(0, 'rgba(46, 204, 113, 0.95)');\n" +
                "    gradientCurr.addColorStop(1, 'rgba(34, 139, 34, 0.95)');\n" +
                "    const gradientPrev = ctx.createLinearGradient(0,0,0,400);\n" +
                "    gradientPrev.addColorStop(0, 'rgba(175, 230, 150, 0.95)');\n" +
                "    gradientPrev.addColorStop(1, 'rgba(120, 200, 100, 0.95)');\n" +
                "    \n" +
                "    let tooltipTimeout;\n" +
                "    \n" +
                "    const data = {\n" +
                "      labels: ['JAN','FEB','MAR','APR','MAY','JUN','JUL','AUG','SEP','OCT','NOV','DEC'],\n" +
                "      datasets: [\n" +
                "        { label: 'Previous Year', data: [" + sbPrev + "], backgroundColor: gradientPrev, borderRadius:8 },\n" +
                "        { label: 'Current Year', data: [" + sbCurr + "], backgroundColor: gradientCurr, borderRadius:8 }\n" +
                "      ]\n" +
                "    };\n" +
                "    const config = {\n" +
                "      type: 'bar',\n" +
                "      data: data,\n" +
                "      options: {\n" +
                "        responsive: true,\n" +
                "        maintainAspectRatio: false,\n" +
                "        interaction: {\n" +
                "          mode: 'index',\n" +
                "          intersect: false\n" +
                "        },\n" +
                "        plugins: {\n" +
                "          legend: { \n" +
                "            display: true,\n" +
                "            position: 'bottom',\n" +
                "            labels: {\n" +
                "              color: '#333',\n" +
                "              font: { size: 11, weight: 'bold' },\n" +
                "              padding: 15,\n" +
                "              usePointStyle: true,\n" +
                "              pointStyle: 'circle'\n" +
                "            }\n" +
                "          },\n" +
                "          tooltip: {\n" +
                "            enabled: true,\n" +
                "            backgroundColor: 'rgba(0,0,0,0.85)',\n" +
                "            titleFont: { size: 14, weight: 'bold', family: 'Montserrat' },\n" +
                "            bodyFont: { size: 13, family: 'Roboto' },\n" +
                "            padding: 12,\n" +
                "            cornerRadius: 8,\n" +
                "            displayColors: true,\n" +
                "            boxWidth: 10,\n" +
                "            boxHeight: 10,\n" +
                "            boxPadding: 5,\n" +
                "            multiKeyBackground: '#fff',\n" +
                "            callbacks: {\n" +
                "              title: function(tooltipItems) {\n" +
                "                return tooltipItems[0].label + ' Revenue';\n" +
                "              },\n" +
                "              label: function(context) {\n" +
                "                let label = context.dataset.label || '';\n" +
                "                let value = context.parsed.y !== null ? context.parsed.y : 0;\n" +
                "                return label + ': GHS ' + value.toLocaleString('en-US', { \n" +
                "                  minimumFractionDigits: 2,\n" +
                "                  maximumFractionDigits: 2 \n" +
                "                });\n" +
                "              },\n" +
                "              afterBody: function(tooltipItems) {\n" +
                "                let total = 0;\n" +
                "                tooltipItems.forEach(function(tooltipItem) {\n" +
                "                  total += tooltipItem.parsed.y || 0;\n" +
                "                });\n" +
                "                return '\\nTotal: GHS ' + total.toLocaleString('en-US', { \n" +
                "                  minimumFractionDigits: 2,\n" +
                "                  maximumFractionDigits: 2 \n" +
                "                });\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        scales: {\n" +
                "          x: {\n" +
                "            grid: { display: false },\n" +
                "            ticks: {\n" +
                "              autoSkip: false,\n" +
                "              color: '#333',\n" +
                "              font: { weight: '700', size: 12 },\n" +
                "              callback: function(value, index, ticks) {\n" +
                "                const width = window.innerWidth;\n" +
                "                this.minRotation = width < 400 ? 45 : 0;\n" +
                "                this.maxRotation = width < 400 ? 45 : 0;\n" +
                "                return this.getLabelForValue(value);\n" +
                "              }\n" +
                "            }\n" +
                "          },\n" +
                "          y: {\n" +
                "            beginAtZero: true,\n" +
                "            grid: { color: 'rgba(0,0,0,0.06)' },\n" +
                "            ticks: { \n" +
                "              color: '#333', \n" +
                "              font: { size: 11 },\n" +
                "              callback: function(value) {\n" +
                "                return 'GHS ' + value.toLocaleString();\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        onHover: function(event, activeElements) {\n" +
                "          event.native.target.style.cursor = activeElements.length > 0 ? 'pointer' : 'default';\n" +
                "          \n" +
                "          if (activeElements.length > 0) {\n" +
                "            clearTimeout(tooltipTimeout);\n" +
                "            tooltipTimeout = setTimeout(() => {\n" +
                "              this.tooltip.setActiveElements([], {x: 0, y: 0});\n" +
                "              this.update();\n" +
                "            }, 2000);\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    };\n" +
                "    new Chart(ctx, config);\n" +
                "  </script>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * Load dashboard data with COMPLETED orders for revenue
     */
    private void loadDashboardData() {
        SQLiteDatabase db = pizzaData.getReadableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Total Orders (ALL orders)
        Cursor totalOrdersCursor = db.rawQuery("SELECT COUNT(*) FROM " + ORDERS_TABLE, null);
        int totalOrders = totalOrdersCursor.moveToFirst() ? totalOrdersCursor.getInt(0) : 0;
        totalOrdersCursor.close();

        // Today's Orders (ALL orders for today)
        Cursor todayOrdersCursor = db.rawQuery("SELECT COUNT(*) FROM " + ORDERS_TABLE +
                " WHERE DATE(" + ORDER_DATE + ") = ?", new String[]{today});
        int todayOrders = todayOrdersCursor.moveToFirst() ? todayOrdersCursor.getInt(0) : 0;
        todayOrdersCursor.close();

        // Total Revenue (ONLY COMPLETED ORDERS)
        Cursor totalRevenueCursor = db.rawQuery("SELECT SUM(" + TOTAL_AMOUNT + ") FROM " + ORDERS_TABLE +
                " WHERE " + STATUS + " = '" + STATUS_COMPLETED + "'", null);
        double totalRevenue = (totalRevenueCursor.moveToFirst() && !totalRevenueCursor.isNull(0)) ? totalRevenueCursor.getDouble(0) : 0.0;
        totalRevenueCursor.close();

        // Week Revenue (ONLY COMPLETED ORDERS)
        Cursor weekRevenueCursor = db.rawQuery("SELECT SUM(" + TOTAL_AMOUNT + ") FROM " + ORDERS_TABLE +
                " WHERE DATE(" + ORDER_DATE + ") >= DATE('now', '-7 days')" +
                " AND " + STATUS + " = '" + STATUS_COMPLETED + "'", null);
        double weekRevenue = (weekRevenueCursor.moveToFirst() && !weekRevenueCursor.isNull(0)) ? weekRevenueCursor.getDouble(0) : 0.0;
        weekRevenueCursor.close();

        // Pending Orders
        int pendingOrders = databaseHelper.getPendingOrderCount();

        // Total Customers
        Cursor customersCursor = db.rawQuery("SELECT COUNT(*) FROM " + USERS_TABLE + " WHERE " + IS_ACTIVE + " = 1", null);
        int totalCustomers = customersCursor.moveToFirst() ? customersCursor.getInt(0) : 0;
        customersCursor.close();

        // Update UI
        tvTotalOrders.setText(String.valueOf(totalOrders));
        tvTodayOrders.setText(String.valueOf(todayOrders));
        tvTotalRevenue.setText(String.format("GHS %.2f", totalRevenue));
        tvWeekRevenue.setText(String.format("GHS %.2f", weekRevenue));
        tvPendingOrders.setText(String.valueOf(pendingOrders));
        tvTotalCustomers.setText(String.valueOf(totalCustomers));
    }

    private void loadRecentOrders() {
        recentOrdersList = databaseHelper.getAllOrders();
        if (recentOrdersList.size() > 10) recentOrdersList = recentOrdersList.subList(0, 10);
        recentOrdersAdapter = new OrderAdapter(recentOrdersList, this, this, true);
        rvRecentOrders.setAdapter(recentOrdersAdapter);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.card_view_orders || viewId == R.id.card_total_orders || viewId == R.id.card_pending_orders) {
            startActivity(new Intent(this, AdminOrdersActivity.class));
        } else if (viewId == R.id.card_manage_pizzas) {
            startActivity(new Intent(this, AdminPizzaManagementActivity.class));
        } else if (viewId == R.id.card_view_customers) {
            startActivity(new Intent(this, AdminCustomersActivity.class));
        } else if (viewId == R.id.card_settings) {
            startActivity(new Intent(this, AdminSettingsActivity.class));
        } else if (viewId == R.id.card_revenue) {
            startActivity(new Intent(this, ActivitydminRevenueA.class));
        } else if (viewId == R.id.btn_logout) {
            logout();
        }
    }

    @Override
    public void onOrderClick(Order order) {
        Intent intent = new Intent(this, AdminOrderDetailActivity.class);
        intent.putExtra("order_id", order.getOrderId());
        startActivity(intent);
    }

    @Override
    public void onUpdateStatus(Order order, String newStatus) {
        boolean success = databaseHelper.updateOrderStatus(order.getOrderId(), newStatus);
        if (success) {
            order.setStatus(newStatus);
            recentOrdersAdapter.notifyDataSetChanged();
            loadDashboardData();
            showMessage("Order status updated successfully");
        } else {
            showMessage("Failed to update order status");
        }
    }

    @Override
    public void onViewDetails(Order order) {
        onOrderClick(order);
    }

    private void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
        loadRecentOrders();
        setupChart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) databaseHelper.close();
        if (pizzaData != null) pizzaData.close();
    }
}