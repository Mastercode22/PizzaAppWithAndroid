package com.delaroystudios.pizza.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.adapters.OrderAdapter;
import com.delaroystudios.pizza.database.DatabaseHelper;
import com.delaroystudios.pizza.database.PizzaData;
import com.delaroystudios.pizza.models.Order;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.delaroystudios.pizza.database.Constants.*;

public class AdminDashboardActivity extends AppCompatActivity implements
        View.OnClickListener,
        OrderAdapter.OnOrderClickListener,
        NavigationView.OnNavigationItemSelectedListener {

    private TextView tvTotalOrders, tvTotalRevenue, tvPendingOrders;
    private TextView tvTodayOrders, tvWeekRevenue, tvTotalCustomers;
    private TextView tvViewAllReports, tvViewAllOrders;
    private RecyclerView rvRecentOrders;
    private WebView webViewChart;
    private ImageButton btnRefresh, btnMenu;
    private DatabaseHelper databaseHelper;
    private PizzaData pizzaData;
    private OrderAdapter recentOrdersAdapter;
    private List<Order> recentOrdersList;

    // Sidebar components
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Force Light Mode for the Admin Dashboard
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        databaseHelper = new DatabaseHelper(this);
        pizzaData = new PizzaData(this);

        initViews();
        setupSidebar();
        setupClickListeners();
        setupRecentOrdersRecyclerView();
        loadDashboardData();
        loadRecentOrders();
        setupChart();
    }

    private void initViews() {
        // Initialize main statistics TextViews
        tvTotalOrders = findViewById(R.id.tv_total_orders);
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);
        tvPendingOrders = findViewById(R.id.tv_pending_orders);
        tvTodayOrders = findViewById(R.id.tv_today_orders);
        tvWeekRevenue = findViewById(R.id.tv_week_revenue);
        tvTotalCustomers = findViewById(R.id.tv_total_customers);

        // Initialize new views
        tvViewAllReports = findViewById(R.id.tv_view_all_reports);
        tvViewAllOrders = findViewById(R.id.tv_view_all_orders);
        btnRefresh = findViewById(R.id.btn_refresh);
        btnMenu = findViewById(R.id.btn_menu);

        // Initialize RecyclerView and WebView
        rvRecentOrders = findViewById(R.id.rv_recent_orders);
        webViewChart = findViewById(R.id.webview_chart);

        // Initialize sidebar components
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // WebView configuration for charts
        webViewChart.getSettings().setJavaScriptEnabled(true);
        webViewChart.getSettings().setDomStorageEnabled(true);
        webViewChart.getSettings().setLoadWithOverviewMode(true);
        webViewChart.getSettings().setUseWideViewPort(true);
        webViewChart.setBackgroundColor(Color.TRANSPARENT);

        // Set click listeners for buttons
        findViewById(R.id.btn_logout).setOnClickListener(this);
        btnRefresh.setOnClickListener(this);
        btnMenu.setOnClickListener(this);
        tvViewAllReports.setOnClickListener(this);
        tvViewAllOrders.setOnClickListener(this);
    }

    private void setupSidebar() {
        // Set navigation item selected listener
        navigationView.setNavigationItemSelectedListener(this);

        // Set up header view if needed
        View headerView = navigationView.getHeaderView(0);
        TextView tvAdminName = headerView.findViewById(R.id.tv_admin_name);
        TextView tvAdminEmail = headerView.findViewById(R.id.tv_admin_email);

        // Set admin info (you can get this from shared preferences or database)
        tvAdminName.setText("Admin User");
        tvAdminEmail.setText("admin@pizzastore.com");
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
        rvRecentOrders.setHasFixedSize(true);
    }

    private void setupChart() {
        int[] currentYearData = getMonthlyRevenueData(0);
        int[] previousYearData = getMonthlyRevenueData(-1);

        String htmlData = generateModernChartHTML(currentYearData, previousYearData);
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

    private String generateModernChartHTML(int[] currYear, int[] prevYear) {
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
                "  <link href='https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap' rel='stylesheet'>\n" +
                "  <script src='https://cdn.jsdelivr.net/npm/chart.js'></script>\n" +
                "  <style>\n" +
                "    * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "    body { margin:0; padding:20px; background:transparent; font-family:'Inter',sans-serif; }\n" +
                "    .chart-container { position:relative; width:100%; height:300px; }\n" +
                "    canvas { width:100% !important; height:100% !important; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class='chart-container'><canvas id='revenueChart'></canvas></div>\n" +
                "  <script>\n" +
                "    const ctx = document.getElementById('revenueChart').getContext('2d');\n" +
                "    \n" +
                "    // Create modern gradients\n" +
                "    const gradientCurrent = ctx.createLinearGradient(0, 0, 0, 400);\n" +
                "    gradientCurrent.addColorStop(0, 'rgba(79, 70, 229, 0.8)');\n" +
                "    gradientCurrent.addColorStop(1, 'rgba(79, 70, 229, 0.1)');\n" +
                "    \n" +
                "    const gradientPrevious = ctx.createLinearGradient(0, 0, 0, 400);\n" +
                "    gradientPrevious.addColorStop(0, 'rgba(16, 185, 129, 0.6)');\n" +
                "    gradientPrevious.addColorStop(1, 'rgba(16, 185, 129, 0.1)');\n" +
                "    \n" +
                "    const data = {\n" +
                "      labels: ['JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG', 'SEP', 'OCT', 'NOV', 'DEC'],\n" +
                "      datasets: [\n" +
                "        {\n" +
                "          label: 'Current Year',\n" +
                "          data: [" + sbCurr + "],\n" +
                "          backgroundColor: gradientCurrent,\n" +
                "          borderColor: '#4F46E5',\n" +
                "          borderWidth: 2,\n" +
                "          borderRadius: 12,\n" +
                "          borderSkipped: false,\n" +
                "          barPercentage: 0.6,\n" +
                "          categoryPercentage: 0.7\n" +
                "        },\n" +
                "        {\n" +
                "          label: 'Previous Year',\n" +
                "          data: [" + sbPrev + "],\n" +
                "          backgroundColor: gradientPrevious,\n" +
                "          borderColor: '#10B981',\n" +
                "          borderWidth: 2,\n" +
                "          borderRadius: 12,\n" +
                "          borderSkipped: false,\n" +
                "          barPercentage: 0.6,\n" +
                "          categoryPercentage: 0.7\n" +
                "        }\n" +
                "      ]\n" +
                "    };\n" +
                "    \n" +
                "    const config = {\n" +
                "      type: 'bar',\n" +
                "      data: data,\n" +
                "      options: {\n" +
                "        responsive: true,\n" +
                "        maintainAspectRatio: false,\n" +
                "        interaction: { mode: 'index', intersect: false },\n" +
                "        plugins: {\n" +
                "          legend: { \n" +
                "            display: true,\n" +
                "            position: 'top',\n" +
                "            labels: {\n" +
                "              color: '#374151',\n" +
                "              font: { size: 12, weight: '500', family: 'Inter' },\n" +
                "              padding: 20,\n" +
                "              usePointStyle: true,\n" +
                "              pointStyle: 'circle',\n" +
                "              boxWidth: 8,\n" +
                "              boxHeight: 8\n" +
                "            }\n" +
                "          },\n" +
                "          tooltip: {\n" +
                "            enabled: true,\n" +
                "            backgroundColor: 'rgba(17, 24, 39, 0.95)',\n" +
                "            titleColor: '#F9FAFB',\n" +
                "            bodyColor: '#F9FAFB',\n" +
                "            titleFont: { size: 13, weight: '500', family: 'Inter' },\n" +
                "            bodyFont: { size: 12, family: 'Inter' },\n" +
                "            padding: 12,\n" +
                "            cornerRadius: 8,\n" +
                "            displayColors: true,\n" +
                "            boxWidth: 8,\n" +
                "            boxHeight: 8,\n" +
                "            boxPadding: 4,\n" +
                "            borderColor: 'rgba(255, 255, 255, 0.1)',\n" +
                "            borderWidth: 1,\n" +
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
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        scales: {\n" +
                "          x: {\n" +
                "            grid: { \n" +
                "              display: false,\n" +
                "              drawBorder: false\n" +
                "            },\n" +
                "            ticks: {\n" +
                "              color: '#6B7280',\n" +
                "              font: { size: 11, weight: '500', family: 'Inter' },\n" +
                "              maxRotation: 0\n" +
                "            }\n" +
                "          },\n" +
                "          y: {\n" +
                "            beginAtZero: true,\n" +
                "            grid: { \n" +
                "              color: 'rgba(107, 114, 128, 0.1)',\n" +
                "              drawBorder: false\n" +
                "            },\n" +
                "            ticks: { \n" +
                "              color: '#6B7280', \n" +
                "              font: { size: 11, family: 'Inter' },\n" +
                "              callback: function(value) {\n" +
                "                return 'GHS ' + value.toLocaleString();\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        animation: {\n" +
                "          duration: 1000,\n" +
                "          easing: 'easeOutQuart'\n" +
                "        },\n" +
                "        onHover: function(event, elements) {\n" +
                "          event.native.target.style.cursor = elements.length > 0 ? 'pointer' : 'default';\n" +
                "        }\n" +
                "      }\n" +
                "    };\n" +
                "    \n" +
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

        // Update UI with smooth animations
        animateTextChange(tvTotalOrders, String.valueOf(totalOrders));
        animateTextChange(tvTodayOrders, String.valueOf(todayOrders));
        animateTextChange(tvTotalRevenue, String.format("GHS %.2f", totalRevenue));
        animateTextChange(tvWeekRevenue, String.format("GHS %.2f", weekRevenue));
        animateTextChange(tvPendingOrders, String.valueOf(pendingOrders));
        animateTextChange(tvTotalCustomers, String.valueOf(totalCustomers));
    }

    private void animateTextChange(final TextView textView, final String newText) {
        textView.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(150)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(newText);
                        textView.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(150)
                                .start();
                    }
                })
                .start();
    }

    private void loadRecentOrders() {
        recentOrdersList = databaseHelper.getAllOrders();
        if (recentOrdersList.size() > 5) recentOrdersList = recentOrdersList.subList(0, 5); // Show only 5 recent orders

        if (recentOrdersAdapter == null) {
            recentOrdersAdapter = new OrderAdapter(recentOrdersList, this, this, true);
            rvRecentOrders.setAdapter(recentOrdersAdapter);
        } else {
            recentOrdersAdapter.updateOrderList(recentOrdersList);
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        // Add ripple effect
        v.postDelayed(() -> {
            if (viewId == R.id.btn_menu) {
                openSidebar();
            } else if (viewId == R.id.card_view_orders || viewId == R.id.card_total_orders ||
                    viewId == R.id.card_pending_orders || viewId == R.id.tv_view_all_orders) {
                navigateToOrders();
            } else if (viewId == R.id.card_manage_pizzas) {
                navigateToPizzaManagement();
            } else if (viewId == R.id.card_view_customers) {
                navigateToCustomers();
            } else if (viewId == R.id.card_settings) {
                navigateToSettings();
            } else if (viewId == R.id.card_revenue || viewId == R.id.tv_view_all_reports) {
                navigateToRevenue();
            } else if (viewId == R.id.btn_logout) {
                logout();
            } else if (viewId == R.id.btn_refresh) {
                refreshData();
            }
        }, 150);
    }

    private void openSidebar() {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    private void closeSidebar() {
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Handle sidebar menu item clicks
        if (id == R.id.nav_dashboard) {
            // Already on dashboard, just close drawer
            closeSidebar();
        } else if (id == R.id.nav_orders) {
            navigateToOrders();
        } else if (id == R.id.nav_pizzas) {
            navigateToPizzaManagement();
        } else if (id == R.id.nav_customers) {
            navigateToCustomers();
        } else if (id == R.id.nav_revenue) {
            navigateToRevenue();
        } else if (id == R.id.nav_settings) {
            navigateToSettings();
        } else if (id == R.id.nav_logout) {
            logout();
        }

        return true;
    }

    private void navigateToOrders() {
        closeSidebar();
        startActivity(new Intent(this, AdminOrdersActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void navigateToPizzaManagement() {
        closeSidebar();
        startActivity(new Intent(this, AdminPizzaManagementActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void navigateToCustomers() {
        closeSidebar();
        startActivity(new Intent(this, AdminCustomersActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void navigateToSettings() {
        closeSidebar();
        startActivity(new Intent(this, AdminSettingsActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void navigateToRevenue() {
        closeSidebar();
        startActivity(new Intent(this, ActivitydminRevenueA.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void refreshData() {
        // Show refresh animation
        btnRefresh.animate()
                .rotationBy(360)
                .setDuration(500)
                .start();

        // Disable refresh button temporarily
        btnRefresh.setEnabled(false);

        // Refresh data with delay to show animation
        new Handler().postDelayed(() -> {
            loadDashboardData();
            loadRecentOrders();
            setupChart();

            // Re-enable refresh button
            btnRefresh.setEnabled(true);

            showMessage("Dashboard refreshed");
        }, 800);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeSidebar();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onOrderClick(Order order) {
        Intent intent = new Intent(this, AdminOrderDetailActivity.class);
        intent.putExtra("order_id", order.getOrderId());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
        closeSidebar();
        // Show confirmation dialog or directly logout
        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        }, 500);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to dashboard
        loadDashboardData();
        loadRecentOrders();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) databaseHelper.close();
        if (pizzaData != null) pizzaData.close();
    }
}