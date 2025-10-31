package com.delaroystudios.pizza.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.delaroystudios.pizza.models.Order;
import com.delaroystudios.pizza.models.OrderItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.delaroystudios.pizza.database.Constants.*;

public class DatabaseHelper {

    private PizzaData pizzaData;

    public DatabaseHelper(Context context) {
        pizzaData = new PizzaData(context);
    }

    public Order getOrderById(int orderId) {
        SQLiteDatabase db = pizzaData.getReadableDatabase();
        String selection = ORDER_ID + "=?";
        String[] selectionArgs = {String.valueOf(orderId)};

        Cursor cursor = db.query(ORDERS_TABLE, null, selection, selectionArgs, null, null, null);

        Order order = null;
        if (cursor.moveToFirst()) {
            order = new Order();
            order.setOrderId(cursor.getInt(cursor.getColumnIndex(ORDER_ID)));
            order.setOrderDate(cursor.getString(cursor.getColumnIndex(ORDER_DATE)));
            order.setStatus(cursor.getString(cursor.getColumnIndex(STATUS)));
            order.setCustomerName(cursor.getString(cursor.getColumnIndex(CUSTOMER_NAME)));
            order.setCustomerPhone(cursor.getString(cursor.getColumnIndex(CUSTOMER_PHONE)));
            order.setDeliveryAddress(cursor.getString(cursor.getColumnIndex(DELIVERY_ADDRESS)));
            order.setSpecialInstructions(cursor.getString(cursor.getColumnIndex(SPECIAL_INSTRUCTIONS)));
            order.setTotalAmount(cursor.getDouble(cursor.getColumnIndex(TOTAL_AMOUNT)));
        }
        cursor.close();
        return order;
    }

    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> orderItems = new ArrayList<>();
        SQLiteDatabase db = pizzaData.getReadableDatabase();

        String query = "SELECT oi.*, p." + PIZZA_NAME + ", s." + SIZE_NAME + ", c." + CRUST_NAME +
                " FROM " + ORDER_ITEMS_TABLE + " oi" +
                " LEFT JOIN " + PIZZAS_TABLE + " p ON oi." + PIZZA_ID + " = p." + PIZZA_ID +
                " LEFT JOIN " + SIZES_TABLE + " s ON oi." + SIZE_ID + " = s." + SIZE_ID +
                " LEFT JOIN " + CRUST_TABLE + " c ON oi." + CRUST_ID + " = c." + CRUST_ID +
                " WHERE oi." + ORDER_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(orderId)});

        while (cursor.moveToNext()) {
            OrderItem item = new OrderItem();
            item.setItemId(cursor.getInt(cursor.getColumnIndex(ITEM_ID)));
            item.setPizzaId(cursor.getInt(cursor.getColumnIndex(PIZZA_ID)));
            item.setQuantity(cursor.getInt(cursor.getColumnIndex(QUANTITY)));
            item.setItemPrice(cursor.getDouble(cursor.getColumnIndex(ITEM_PRICE)));
            item.setPizzaName(cursor.getString(cursor.getColumnIndex(PIZZA_NAME)));
            item.setSizeName(cursor.getString(cursor.getColumnIndex(SIZE_NAME)));
            item.setCrustName(cursor.getString(cursor.getColumnIndex(CRUST_NAME)));

            orderItems.add(item);
        }
        cursor.close();
        return orderItems;
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = pizzaData.getReadableDatabase();

        String orderBy = ORDER_DATE + " DESC";
        Cursor cursor = db.query(ORDERS_TABLE, null, null, null, null, null, orderBy);

        while (cursor.moveToNext()) {
            Order order = new Order();
            order.setOrderId(cursor.getInt(cursor.getColumnIndex(ORDER_ID)));
            order.setOrderDate(cursor.getString(cursor.getColumnIndex(ORDER_DATE)));
            order.setStatus(cursor.getString(cursor.getColumnIndex(STATUS)));
            order.setCustomerName(cursor.getString(cursor.getColumnIndex(CUSTOMER_NAME)));
            order.setCustomerPhone(cursor.getString(cursor.getColumnIndex(CUSTOMER_PHONE)));
            order.setDeliveryAddress(cursor.getString(cursor.getColumnIndex(DELIVERY_ADDRESS)));
            order.setTotalAmount(cursor.getDouble(cursor.getColumnIndex(TOTAL_AMOUNT)));
            orders.add(order);
        }
        cursor.close();
        return orders;
    }

    public boolean updateOrderStatus(int orderId, String newStatus) {
        SQLiteDatabase db = pizzaData.getWritableDatabase();

        android.content.ContentValues values = new android.content.ContentValues();
        values.put(STATUS, newStatus);

        String whereClause = ORDER_ID + "=?";
        String[] whereArgs = {String.valueOf(orderId)};

        int rowsAffected = db.update(ORDERS_TABLE, values, whereClause, whereArgs);
        return rowsAffected > 0;
    }

    // FIXED: Proper implementation for today's order count (ALL orders for today)
    public int getTodayOrderCount() {
        SQLiteDatabase db = pizzaData.getReadableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + ORDERS_TABLE +
                        " WHERE DATE(" + ORDER_DATE + ") = ?",
                new String[]{today}
        );

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    // FIXED: Proper implementation for today's revenue (ONLY COMPLETED orders)
    public double getTodayRevenue() {
        SQLiteDatabase db = pizzaData.getReadableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + TOTAL_AMOUNT + ") FROM " + ORDERS_TABLE +
                        " WHERE DATE(" + ORDER_DATE + ") = ?" +
                        " AND " + STATUS + " = '" + STATUS_COMPLETED + "'",
                new String[]{today}
        );

        double revenue = 0.0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            revenue = cursor.getDouble(0);
        }
        cursor.close();
        return revenue;
    }

    // FIXED: Get total revenue (ONLY COMPLETED orders)
    public double getTotalRevenue() {
        SQLiteDatabase db = pizzaData.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + TOTAL_AMOUNT + ") FROM " + ORDERS_TABLE +
                        " WHERE " + STATUS + " = '" + STATUS_COMPLETED + "'",
                null
        );

        double revenue = 0.0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            revenue = cursor.getDouble(0);
        }
        cursor.close();
        return revenue;
    }

    // FIXED: Get week revenue (ONLY COMPLETED orders)
    public double getWeekRevenue() {
        SQLiteDatabase db = pizzaData.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + TOTAL_AMOUNT + ") FROM " + ORDERS_TABLE +
                        " WHERE DATE(" + ORDER_DATE + ") >= DATE('now', '-7 days')" +
                        " AND " + STATUS + " = '" + STATUS_COMPLETED + "'",
                null
        );

        double revenue = 0.0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            revenue = cursor.getDouble(0);
        }
        cursor.close();
        return revenue;
    }

    // FIXED: Get total orders count (ALL orders)
    public int getTotalOrderCount() {
        SQLiteDatabase db = pizzaData.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + ORDERS_TABLE,
                null
        );

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    // FIXED: Get completed orders count
    public int getCompletedOrderCount() {
        SQLiteDatabase db = pizzaData.getReadableDatabase();
        String selection = STATUS + "=?";
        String[] selectionArgs = {STATUS_COMPLETED};

        Cursor cursor = db.query(ORDERS_TABLE, null, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int getPendingOrderCount() {
        SQLiteDatabase db = pizzaData.getReadableDatabase();
        String selection = STATUS + "=?";
        String[] selectionArgs = {STATUS_PENDING};

        Cursor cursor = db.query(ORDERS_TABLE, null, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    // NEW: Debug method to check order statuses
    public void debugOrderStatuses() {
        SQLiteDatabase db = pizzaData.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT " + STATUS + ", COUNT(*) as count FROM " + ORDERS_TABLE +
                        " GROUP BY " + STATUS,
                null
        );

        System.out.println("=== ORDER STATUS DISTRIBUTION ===");
        while (cursor.moveToNext()) {
            String status = cursor.getString(0);
            int count = cursor.getInt(1);
            System.out.println(status + ": " + count + " orders");
        }
        cursor.close();

        // Check if we have any completed orders with revenue
        Cursor completedCursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + ORDERS_TABLE +
                        " WHERE " + STATUS + " = '" + STATUS_COMPLETED + "'" +
                        " AND " + TOTAL_AMOUNT + " > 0",
                null
        );

        if (completedCursor.moveToFirst()) {
            int completedWithRevenue = completedCursor.getInt(0);
            System.out.println("Completed orders with revenue > 0: " + completedWithRevenue);
        }
        completedCursor.close();
    }

    public void close() {
        if (pizzaData != null) {
            pizzaData.close();
        }
    }
}