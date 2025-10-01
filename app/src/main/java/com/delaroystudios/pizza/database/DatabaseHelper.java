package com.delaroystudios.pizza.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.delaroystudios.pizza.models.Order;
import com.delaroystudios.pizza.models.OrderItem;

import java.util.ArrayList;
import java.util.List;

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

    public int getTodayOrderCount() {
        // Implementation for getting today's order count
        // You'll need to implement this based on your date format
        return 0; // Placeholder
    }

    public double getTodayRevenue() {
        // Implementation for getting today's revenue
        // You'll need to implement this based on your date format
        return 0.0; // Placeholder
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

    public void close() {
        if (pizzaData != null) {
            pizzaData.close();
        }
    }
}