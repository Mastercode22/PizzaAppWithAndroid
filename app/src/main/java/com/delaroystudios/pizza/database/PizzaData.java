package com.delaroystudios.pizza.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.delaroystudios.pizza.models.Order;
import com.delaroystudios.pizza.models.OrderItem;

import java.util.ArrayList;
import java.util.List;

import static com.delaroystudios.pizza.database.Constants.*;

public class PizzaData extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "pizza_enhanced.db";
    private static final int DATABASE_VERSION = 6; // INCREMENT VERSION TO TRIGGER UPGRADE

    public PizzaData(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        db.execSQL("CREATE TABLE " + USERS_TABLE + " (" +
                USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USERNAME + " TEXT UNIQUE NOT NULL, " +
                EMAIL + " TEXT UNIQUE NOT NULL, " +
                PASSWORD_HASH + " TEXT NOT NULL, " +
                FULL_NAME + " TEXT NOT NULL, " +
                PHONE + " TEXT, " +
                ADDRESS + " TEXT, " +
                CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                IS_ACTIVE + " INTEGER DEFAULT 1);");

        // Create admins table
        db.execSQL("CREATE TABLE " + ADMINS_TABLE + " (" +
                ADMIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USERNAME + " TEXT UNIQUE NOT NULL, " +
                EMAIL + " TEXT UNIQUE NOT NULL, " +
                PASSWORD_HASH + " TEXT NOT NULL, " +
                FULL_NAME + " TEXT NOT NULL, " +
                ROLE + " TEXT DEFAULT 'admin', " +
                CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                IS_ACTIVE + " INTEGER DEFAULT 1);");

        // Create categories table
        db.execSQL("CREATE TABLE " + CATEGORIES_TABLE + " (" +
                CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CATEGORY_NAME + " TEXT NOT NULL, " +
                DESCRIPTION + " TEXT, " +
                IS_ACTIVE + " INTEGER DEFAULT 1);");

        // Create pizzas table with image_resource column
        db.execSQL("CREATE TABLE " + PIZZAS_TABLE + " (" +
                PIZZA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PIZZA_NAME + " TEXT NOT NULL, " +
                PIZZA_DESCRIPTION + " TEXT, " +
                CATEGORY_ID + " INTEGER, " +
                BASE_PRICE + " REAL NOT NULL, " +
                IMAGE_URL + " TEXT, " +
                "image_resource INTEGER, " +
                IS_AVAILABLE + " INTEGER DEFAULT 1, " +
                CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(" + CATEGORY_ID + ") REFERENCES " + CATEGORIES_TABLE + "(" + CATEGORY_ID + "));");

        // Create pizza sizes table
        db.execSQL("CREATE TABLE " + SIZES_TABLE + " (" +
                SIZE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SIZE_NAME + " TEXT NOT NULL, " +
                PRICE_MULTIPLIER + " REAL NOT NULL, " +
                IS_ACTIVE + " INTEGER DEFAULT 1);");

        // Create crust types table
        db.execSQL("CREATE TABLE " + CRUST_TABLE + " (" +
                CRUST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CRUST_NAME + " TEXT NOT NULL, " +
                ADDITIONAL_PRICE + " REAL DEFAULT 0.0, " +
                IS_ACTIVE + " INTEGER DEFAULT 1);");

        // Create toppings table
        db.execSQL("CREATE TABLE " + TOPPINGS_TABLE + " (" +
                TOPPING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TOPPING_NAME + " TEXT NOT NULL, " +
                TOPPING_PRICE + " REAL NOT NULL, " +
                IMAGE_URL + " TEXT, " +
                TOPPING_CATEGORY + " TEXT, " +
                IS_AVAILABLE + " INTEGER DEFAULT 1);");

        // Create orders table
        db.execSQL("CREATE TABLE " + ORDERS_TABLE + " (" +
                ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USER_ID + " INTEGER NOT NULL, " +
                ORDER_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                TOTAL_AMOUNT + " REAL NOT NULL, " +
                STATUS + " TEXT DEFAULT '" + STATUS_PENDING + "', " +
                CUSTOMER_NAME + " TEXT NOT NULL, " +
                CUSTOMER_PHONE + " TEXT, " +
                DELIVERY_ADDRESS + " TEXT, " +
                SPECIAL_INSTRUCTIONS + " TEXT, " +
                ESTIMATED_COMPLETION + " DATETIME, " +
                "FOREIGN KEY(" + USER_ID + ") REFERENCES " + USERS_TABLE + "(" + USER_ID + "));");

        // Create order items table
        db.execSQL("CREATE TABLE " + ORDER_ITEMS_TABLE + " (" +
                ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ORDER_ID + " INTEGER NOT NULL, " +
                PIZZA_ID + " INTEGER NOT NULL, " +
                SIZE_ID + " INTEGER NOT NULL, " +
                CRUST_ID + " INTEGER NOT NULL, " +
                QUANTITY + " INTEGER DEFAULT 1, " +
                ITEM_PRICE + " REAL NOT NULL, " +
                SPECIAL_INSTRUCTIONS + " TEXT, " +
                "FOREIGN KEY(" + ORDER_ID + ") REFERENCES " + ORDERS_TABLE + "(" + ORDER_ID + "), " +
                "FOREIGN KEY(" + PIZZA_ID + ") REFERENCES " + PIZZAS_TABLE + "(" + PIZZA_ID + "), " +
                "FOREIGN KEY(" + SIZE_ID + ") REFERENCES " + SIZES_TABLE + "(" + SIZE_ID + "), " +
                "FOREIGN KEY(" + CRUST_ID + ") REFERENCES " + CRUST_TABLE + "(" + CRUST_ID + "));");

        // Create cart table
        db.execSQL("CREATE TABLE " + CART_TABLE + " (" +
                CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USER_ID + " INTEGER NOT NULL, " +
                PIZZA_ID + " INTEGER NOT NULL, " +
                SIZE_ID + " INTEGER NOT NULL, " +
                CRUST_ID + " INTEGER NOT NULL, " +
                QUANTITY + " INTEGER DEFAULT 1, " +
                ADDED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(" + USER_ID + ") REFERENCES " + USERS_TABLE + "(" + USER_ID + "), " +
                "FOREIGN KEY(" + PIZZA_ID + ") REFERENCES " + PIZZAS_TABLE + "(" + PIZZA_ID + "), " +
                "FOREIGN KEY(" + SIZE_ID + ") REFERENCES " + SIZES_TABLE + "(" + SIZE_ID + "), " +
                "FOREIGN KEY(" + CRUST_ID + ") REFERENCES " + CRUST_TABLE + "(" + CRUST_ID + "));");

        // Legacy table for backward compatibility
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SIZE + " TEXT NOT NULL, " +
                CRUST + " TEXT NOT NULL, " +
                TOPPINGS_WHOLE + " TEXT, " +
                TOPPINGS_LEFT + " TEXT, " +
                TOPPINGS_RIGHT + " TEXT);");

        // Insert sample data
        insertSampleData(db);
    }

    private void insertSampleData(SQLiteDatabase db) {
        // Insert categories
        db.execSQL("INSERT INTO " + CATEGORIES_TABLE + " (" + CATEGORY_NAME + ", " + DESCRIPTION + ") VALUES " +
                "('Classic', 'Traditional pizza favorites'), " +
                "('Specialty', 'Gourmet specialty pizzas'), " +
                "('Vegetarian', 'Vegetable-based pizzas');");

        // Insert pizza sizes
        db.execSQL("INSERT INTO " + SIZES_TABLE + " (" + SIZE_NAME + ", " + PRICE_MULTIPLIER + ") VALUES " +
                "('Small', 0.8), " +
                "('Medium', 1.0), " +
                "('Large', 1.3), " +
                "('Party', 1.8);");

        // Insert crust types
        db.execSQL("INSERT INTO " + CRUST_TABLE + " (" + CRUST_NAME + ", " + ADDITIONAL_PRICE + ") VALUES " +
                "('Thin', 0.00), " +
                "('Thick', 1.50), " +
                "('Deep Dish', 2.50), " +
                "('Stuffed', 3.00);");

        // Insert sample pizzas
        db.execSQL("INSERT INTO " + PIZZAS_TABLE + " (" + PIZZA_NAME + ", " + PIZZA_DESCRIPTION +
                ", " + CATEGORY_ID + ", " + BASE_PRICE + ") VALUES " +
                "('Margherita', 'Fresh mozzarella, tomato sauce, basil', 1, 12.99), " +
                "('Pepperoni', 'Classic pepperoni with mozzarella', 1, 14.99), " +
                "('Supreme', 'Pepperoni, sausage, peppers, onions, mushrooms', 2, 18.99), " +
                "('Veggie Delight', 'Bell peppers, onions, mushrooms, tomatoes, olives', 3, 16.99);");

        // Insert toppings
        db.execSQL("INSERT INTO " + TOPPINGS_TABLE + " (" + TOPPING_NAME + ", " + TOPPING_PRICE +
                ", " + TOPPING_CATEGORY + ") VALUES " +
                "('Pepperoni', 2.50, 'Meat'), " +
                "('Sausage', 2.50, 'Meat'), " +
                "('Ham', 2.50, 'Meat'), " +
                "('Chicken', 3.00, 'Meat'), " +
                "('Bacon', 3.00, 'Meat'), " +
                "('Mushrooms', 1.50, 'Vegetable'), " +
                "('Green Peppers', 1.50, 'Vegetable'), " +
                "('Onions', 1.50, 'Vegetable'), " +
                "('Black Olives', 1.75, 'Vegetable'), " +
                "('Tomatoes', 1.75, 'Vegetable'), " +
                "('Extra Cheese', 2.00, 'Cheese'), " +
                "('Pineapple', 2.00, 'Fruit');");

        // Insert default admin user
        db.execSQL("INSERT INTO " + ADMINS_TABLE + " (" + USERNAME + ", " + EMAIL +
                ", " + PASSWORD_HASH + ", " + FULL_NAME + ") VALUES " +
                "('admin', 'admin@pizzaparlor.com', 'admin123', 'Pizza Admin');");

        // Insert sample users FIRST
        db.execSQL("INSERT INTO " + USERS_TABLE + " (" + USERNAME + ", " + EMAIL +
                ", " + PASSWORD_HASH + ", " + FULL_NAME + ", " + PHONE + ", " + ADDRESS + ") VALUES " +
                "('john_doe', 'john@example.com', 'password123', 'John Doe', '555-0100', '123 Main St'), " +
                "('jane_smith', 'jane@example.com', 'password123', 'Jane Smith', '555-0101', '456 Oak Ave'), " +
                "('bob_jones', 'bob@example.com', 'password123', 'Bob Jones', '555-0102', '789 Elm St');");

        // Insert sample COMPLETED orders with realistic data
        // Today's orders
        db.execSQL("INSERT INTO " + ORDERS_TABLE + " (" + USER_ID + ", " + TOTAL_AMOUNT + ", " + STATUS +
                ", " + CUSTOMER_NAME + ", " + CUSTOMER_PHONE + ", " + DELIVERY_ADDRESS +
                ", " + ORDER_DATE + ") VALUES " +
                "(1, 45.98, '" + STATUS_COMPLETED + "', 'John Doe', '555-0100', '123 Main St', datetime('now')), " +
                "(2, 32.50, '" + STATUS_COMPLETED + "', 'Jane Smith', '555-0101', '456 Oak Ave', datetime('now')), " +
                "(3, 58.75, '" + STATUS_COMPLETED + "', 'Bob Jones', '555-0102', '789 Elm St', datetime('now', '-2 hours'));");

        // This week's orders (last 7 days)
        db.execSQL("INSERT INTO " + ORDERS_TABLE + " (" + USER_ID + ", " + TOTAL_AMOUNT + ", " + STATUS +
                ", " + CUSTOMER_NAME + ", " + CUSTOMER_PHONE + ", " + DELIVERY_ADDRESS +
                ", " + ORDER_DATE + ") VALUES " +
                "(1, 67.99, '" + STATUS_COMPLETED + "', 'John Doe', '555-0100', '123 Main St', datetime('now', '-2 days')), " +
                "(2, 41.25, '" + STATUS_COMPLETED + "', 'Jane Smith', '555-0101', '456 Oak Ave', datetime('now', '-3 days')), " +
                "(3, 55.50, '" + STATUS_COMPLETED + "', 'Bob Jones', '555-0102', '789 Elm St', datetime('now', '-5 days')), " +
                "(1, 38.99, '" + STATUS_COMPLETED + "', 'John Doe', '555-0100', '123 Main St', datetime('now', '-6 days'));");

        // This month's orders (spread across different days)
        db.execSQL("INSERT INTO " + ORDERS_TABLE + " (" + USER_ID + ", " + TOTAL_AMOUNT + ", " + STATUS +
                ", " + CUSTOMER_NAME + ", " + CUSTOMER_PHONE + ", " + DELIVERY_ADDRESS +
                ", " + ORDER_DATE + ") VALUES " +
                "(2, 72.50, '" + STATUS_COMPLETED + "', 'Jane Smith', '555-0101', '456 Oak Ave', datetime('now', '-10 days')), " +
                "(3, 44.99, '" + STATUS_COMPLETED + "', 'Bob Jones', '555-0102', '789 Elm St', datetime('now', '-15 days')), " +
                "(1, 89.99, '" + STATUS_COMPLETED + "', 'John Doe', '555-0100', '123 Main St', datetime('now', '-20 days')), " +
                "(2, 51.75, '" + STATUS_COMPLETED + "', 'Jane Smith', '555-0101', '456 Oak Ave', datetime('now', '-25 days'));");

        // Pending orders (should NOT count in revenue)
        db.execSQL("INSERT INTO " + ORDERS_TABLE + " (" + USER_ID + ", " + TOTAL_AMOUNT + ", " + STATUS +
                ", " + CUSTOMER_NAME + ", " + CUSTOMER_PHONE + ", " + DELIVERY_ADDRESS + ") VALUES " +
                "(1, 25.99, '" + STATUS_PENDING + "', 'John Doe', '555-0100', '123 Main St'), " +
                "(2, 33.50, '" + STATUS_PENDING + "', 'Jane Smith', '555-0101', '456 Oak Ave');");

        // Insert order items for the first completed order
        db.execSQL("INSERT INTO " + ORDER_ITEMS_TABLE + " (" + ORDER_ID + ", " + PIZZA_ID + ", " + SIZE_ID +
                ", " + CRUST_ID + ", " + QUANTITY + ", " + ITEM_PRICE + ") VALUES " +
                "(1, 1, 2, 1, 2, 25.98), " +
                "(1, 2, 3, 2, 1, 19.99);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 6) {
            // FIX EXISTING ORDER STATUSES
            // Convert all capitalized statuses to lowercase
            db.execSQL("UPDATE " + ORDERS_TABLE + " SET " + STATUS + " = 'pending' " +
                    "WHERE LOWER(" + STATUS + ") = 'pending'");

            db.execSQL("UPDATE " + ORDERS_TABLE + " SET " + STATUS + " = 'in_progress' " +
                    "WHERE LOWER(" + STATUS + ") IN ('in progress', 'in_progress', 'preparing')");

            db.execSQL("UPDATE " + ORDERS_TABLE + " SET " + STATUS + " = 'completed' " +
                    "WHERE LOWER(" + STATUS + ") IN ('completed', 'complete', 'delivered')");

            db.execSQL("UPDATE " + ORDERS_TABLE + " SET " + STATUS + " = 'cancelled' " +
                    "WHERE LOWER(" + STATUS + ") IN ('cancelled', 'canceled')");

            android.util.Log.d("DATABASE_UPGRADE", "Fixed all order statuses to lowercase");
            return; // Don't drop tables if we're just fixing statuses
        }

        // If major upgrade needed, drop all tables and recreate
        db.execSQL("DROP TABLE IF EXISTS " + CART_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ORDER_ITEMS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ORDERS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TOPPINGS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CRUST_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + SIZES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + PIZZAS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CATEGORIES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ADMINS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // ========== STATUS FIX METHODS ==========

    /**
     * Manually fix all order statuses to ensure consistency
     * Call this method once when app starts to normalize all status values
     */
    public void fixAllOrderStatuses() {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.beginTransaction();

            // Convert all status values to lowercase
            db.execSQL("UPDATE " + ORDERS_TABLE + " SET " + STATUS + " = 'pending' " +
                    "WHERE LOWER(" + STATUS + ") = 'pending'");

            db.execSQL("UPDATE " + ORDERS_TABLE + " SET " + STATUS + " = 'in_progress' " +
                    "WHERE LOWER(" + STATUS + ") IN ('in progress', 'in_progress', 'preparing')");

            db.execSQL("UPDATE " + ORDERS_TABLE + " SET " + STATUS + " = 'completed' " +
                    "WHERE LOWER(" + STATUS + ") IN ('completed', 'complete', 'delivered')");

            db.execSQL("UPDATE " + ORDERS_TABLE + " SET " + STATUS + " = 'cancelled' " +
                    "WHERE LOWER(" + STATUS + ") IN ('cancelled', 'canceled')");

            db.setTransactionSuccessful();

            android.util.Log.d("STATUS_FIX", "Successfully fixed all order statuses");

        } catch (Exception e) {
            android.util.Log.e("STATUS_FIX", "Error fixing statuses: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Debug method to check order status distribution and revenue
     * Call this to diagnose revenue calculation issues
     */
    public void debugOrderStatuses() {
        SQLiteDatabase db = this.getReadableDatabase();

        android.util.Log.d("STATUS_DEBUG", "=== CHECKING ORDER STATUSES ===");

        // Show recent orders with their status
        Cursor cursor = db.rawQuery(
                "SELECT " + ORDER_ID + ", " + STATUS + ", " + TOTAL_AMOUNT + ", " + ORDER_DATE +
                        " FROM " + ORDERS_TABLE + " ORDER BY " + ORDER_DATE + " DESC LIMIT 20",
                null
        );

        while (cursor.moveToNext()) {
            int orderId = cursor.getInt(0);
            String status = cursor.getString(1);
            double amount = cursor.getDouble(2);
            String date = cursor.getString(3);

            android.util.Log.d("STATUS_DEBUG",
                    "Order #" + orderId + " | Status: [" + status + "] | Amount: " + amount + " | Date: " + date);
        }
        cursor.close();

        // Check status distribution
        Cursor statusCursor = db.rawQuery(
                "SELECT " + STATUS + ", COUNT(*) as count, SUM(" + TOTAL_AMOUNT + ") as revenue " +
                        "FROM " + ORDERS_TABLE + " GROUP BY " + STATUS,
                null
        );

        android.util.Log.d("STATUS_DEBUG", "=== ORDER STATUS DISTRIBUTION ===");
        while (statusCursor.moveToNext()) {
            String status = statusCursor.getString(0);
            int count = statusCursor.getInt(1);
            double revenue = statusCursor.getDouble(2);
            android.util.Log.d("STATUS_DEBUG", status + ": " + count + " orders, GHS " + revenue + " revenue");
        }
        statusCursor.close();

        // Check completed revenue specifically
        Cursor revenueCursor = db.rawQuery(
                "SELECT COUNT(*), SUM(" + TOTAL_AMOUNT + ") FROM " + ORDERS_TABLE +
                        " WHERE " + STATUS + " = '" + STATUS_COMPLETED + "'",
                null
        );

        if (revenueCursor.moveToFirst()) {
            int count = revenueCursor.getInt(0);
            double revenue = revenueCursor.getDouble(1);
            android.util.Log.d("STATUS_DEBUG",
                    "COMPLETED ORDERS: " + count + " | REVENUE: GHS " + revenue);
        }
        revenueCursor.close();

        // Check today's completed orders
        Cursor todayCursor = db.rawQuery(
                "SELECT COUNT(*), SUM(" + TOTAL_AMOUNT + ") FROM " + ORDERS_TABLE +
                        " WHERE DATE(" + ORDER_DATE + ") = DATE('now')" +
                        " AND " + STATUS + " = '" + STATUS_COMPLETED + "'",
                null
        );

        if (todayCursor.moveToFirst()) {
            int count = todayCursor.getInt(0);
            double revenue = todayCursor.getDouble(1);
            android.util.Log.d("STATUS_DEBUG",
                    "TODAY'S COMPLETED: " + count + " orders | REVENUE: GHS " + revenue);
        }
        todayCursor.close();

        android.util.Log.d("STATUS_DEBUG", "=== END STATUS CHECK ===");
    }

    // ========== ORDER CREATION METHODS ==========

    /**
     * Creates a new order in the database
     */
    public long createOrder(Order order) {
        SQLiteDatabase db = this.getWritableDatabase();

        android.content.ContentValues values = new android.content.ContentValues();
        values.put(USER_ID, order.getUserId());
        values.put(TOTAL_AMOUNT, order.getTotalAmount());
        values.put(STATUS, order.getStatus());
        values.put(CUSTOMER_NAME, order.getCustomerName());
        values.put(CUSTOMER_PHONE, order.getCustomerPhone());
        values.put(DELIVERY_ADDRESS, order.getDeliveryAddress());
        values.put(SPECIAL_INSTRUCTIONS, order.getSpecialInstructions());

        // Use current timestamp if not provided
        if (order.getOrderDate() == null) {
            values.put(ORDER_DATE, "datetime('now')");
        } else {
            values.put(ORDER_DATE, order.getOrderDate());
        }

        long orderId = db.insert(ORDERS_TABLE, null, values);

        // If order was created successfully, insert order items
        if (orderId != -1 && order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                createOrderItem((int) orderId, item);
            }
        }

        return orderId;
    }

    /**
     * Creates an order item for a specific order
     */
    public long createOrderItem(int orderId, OrderItem item) {
        SQLiteDatabase db = this.getWritableDatabase();

        android.content.ContentValues values = new android.content.ContentValues();
        values.put(ORDER_ID, orderId);
        values.put(PIZZA_ID, item.getPizzaId());
        values.put(SIZE_ID, item.getSizeId());
        values.put(CRUST_ID, item.getCrustId());
        values.put(QUANTITY, item.getQuantity());
        values.put(ITEM_PRICE, item.getItemPrice());

        if (item.getSpecialInstructions() != null) {
            values.put(SPECIAL_INSTRUCTIONS, item.getSpecialInstructions());
        }

        return db.insert(ORDER_ITEMS_TABLE, null, values);
    }

    /**
     * Creates an order from cart items for a user
     */
    public long createOrderFromCart(int userId, String customerName, String phone, String address, String instructions) {
        SQLiteDatabase db = this.getWritableDatabase();

        // First, calculate the total from cart items
        double totalAmount = calculateCartTotal(userId);

        if (totalAmount == 0) {
            return -1; // No items in cart
        }

        // Create the order
        android.content.ContentValues orderValues = new android.content.ContentValues();
        orderValues.put(USER_ID, userId);
        orderValues.put(TOTAL_AMOUNT, totalAmount);
        orderValues.put(STATUS, STATUS_PENDING);
        orderValues.put(CUSTOMER_NAME, customerName);
        orderValues.put(CUSTOMER_PHONE, phone);
        orderValues.put(DELIVERY_ADDRESS, address);
        orderValues.put(SPECIAL_INSTRUCTIONS, instructions);
        orderValues.put(ORDER_DATE, "datetime('now')");

        long orderId = db.insert(ORDERS_TABLE, null, orderValues);

        if (orderId != -1) {
            // Move cart items to order items
            String moveCartItemsQuery =
                    "INSERT INTO " + ORDER_ITEMS_TABLE + " (" + ORDER_ID + ", " + PIZZA_ID + ", " +
                            SIZE_ID + ", " + CRUST_ID + ", " + QUANTITY + ", " + ITEM_PRICE + ") " +
                            "SELECT " + orderId + ", c." + PIZZA_ID + ", c." + SIZE_ID + ", c." + CRUST_ID + ", c." +
                            QUANTITY + ", " + "(p." + BASE_PRICE + " * s." + PRICE_MULTIPLIER + " + cr." + ADDITIONAL_PRICE + ") " +
                            "FROM " + CART_TABLE + " c " +
                            "JOIN " + PIZZAS_TABLE + " p ON c." + PIZZA_ID + " = p." + PIZZA_ID + " " +
                            "JOIN " + SIZES_TABLE + " s ON c." + SIZE_ID + " = s." + SIZE_ID + " " +
                            "JOIN " + CRUST_TABLE + " cr ON c." + CRUST_ID + " = cr." + CRUST_ID + " " +
                            "WHERE c." + USER_ID + " = ?";

            db.execSQL(moveCartItemsQuery, new String[]{String.valueOf(userId)});

            // Clear the user's cart
            db.delete(CART_TABLE, USER_ID + " = ?", new String[]{String.valueOf(userId)});
        }

        return orderId;
    }

    /**
     * Calculates the total price of items in user's cart
     */
    public double calculateCartTotal(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query =
                "SELECT SUM((p." + BASE_PRICE + " * s." + PRICE_MULTIPLIER + " + cr." + ADDITIONAL_PRICE + ") * c." + QUANTITY + ") " +
                        "FROM " + CART_TABLE + " c " +
                        "JOIN " + PIZZAS_TABLE + " p ON c." + PIZZA_ID + " = p." + PIZZA_ID + " " +
                        "JOIN " + SIZES_TABLE + " s ON c." + SIZE_ID + " = s." + SIZE_ID + " " +
                        "JOIN " + CRUST_TABLE + " cr ON c." + CRUST_ID + " = cr." + CRUST_ID + " " +
                        "WHERE c." + USER_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        double total = 0.0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            total = cursor.getDouble(0);
        }
        cursor.close();

        return total;
    }

    /**
     * Adds an item to user's cart
     */
    public long addToCart(int userId, int pizzaId, int sizeId, int crustId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if item already exists in cart
        Cursor cursor = db.query(CART_TABLE, null,
                USER_ID + " = ? AND " + PIZZA_ID + " = ? AND " + SIZE_ID + " = ? AND " + CRUST_ID + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(pizzaId), String.valueOf(sizeId), String.valueOf(crustId)},
                null, null, null);

        long result;
        if (cursor.moveToFirst()) {
            // Update quantity if item exists
            int existingQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(QUANTITY));
            android.content.ContentValues values = new android.content.ContentValues();
            values.put(QUANTITY, existingQuantity + quantity);

            result = db.update(CART_TABLE, values,
                    USER_ID + " = ? AND " + PIZZA_ID + " = ? AND " + SIZE_ID + " = ? AND " + CRUST_ID + " = ?",
                    new String[]{String.valueOf(userId), String.valueOf(pizzaId), String.valueOf(sizeId), String.valueOf(crustId)});
        } else {
            // Insert new item
            android.content.ContentValues values = new android.content.ContentValues();
            values.put(USER_ID, userId);
            values.put(PIZZA_ID, pizzaId);
            values.put(SIZE_ID, sizeId);
            values.put(CRUST_ID, crustId);
            values.put(QUANTITY, quantity);

            result = db.insert(CART_TABLE, null, values);
        }
        cursor.close();

        return result;
    }

    /**
     * Gets all items in user's cart
     */
    public Cursor getCartItems(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query =
                "SELECT c.*, p." + PIZZA_NAME + ", p." + BASE_PRICE + ", s." + SIZE_NAME + ", s." + PRICE_MULTIPLIER +
                        ", cr." + CRUST_NAME + ", cr." + ADDITIONAL_PRICE +
                        ", (p." + BASE_PRICE + " * s." + PRICE_MULTIPLIER + " + cr." + ADDITIONAL_PRICE + ") * c." + QUANTITY + " as item_total " +
                        "FROM " + CART_TABLE + " c " +
                        "JOIN " + PIZZAS_TABLE + " p ON c." + PIZZA_ID + " = p." + PIZZA_ID + " " +
                        "JOIN " + SIZES_TABLE + " s ON c." + SIZE_ID + " = s." + SIZE_ID + " " +
                        "JOIN " + CRUST_TABLE + " cr ON c." + CRUST_ID + " = cr." + CRUST_ID + " " +
                        "WHERE c." + USER_ID + " = ? " +
                        "ORDER BY c." + ADDED_AT + " DESC";

        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    /**
     * Removes an item from user's cart
     */
    public boolean removeFromCart(int cartId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(CART_TABLE, CART_ID + " = ?", new String[]{String.valueOf(cartId)});
        return result > 0;
    }

    /**
     * Updates cart item quantity
     */
    public boolean updateCartQuantity(int cartId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        android.content.ContentValues values = new android.content.ContentValues();
        values.put(QUANTITY, quantity);

        int result = db.update(CART_TABLE, values, CART_ID + " = ?", new String[]{String.valueOf(cartId)});
        return result > 0;
    }

    /**
     * Clears user's cart
     */
    public boolean clearCart(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(CART_TABLE, USER_ID + " = ?", new String[]{String.valueOf(userId)});
        return result > 0;
    }

    // ========== ORDER MANAGEMENT METHODS ==========

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String orderBy = ORDER_DATE + " DESC";
        Cursor cursor = db.query(ORDERS_TABLE, null, null, null, null, null, orderBy);

        while (cursor.moveToNext()) {
            Order order = extractOrderFromCursor(cursor);
            orders.add(order);
        }
        cursor.close();
        return orders;
    }

    public Order getOrderById(int orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = ORDER_ID + "=?";
        String[] selectionArgs = {String.valueOf(orderId)};

        Cursor cursor = db.query(ORDERS_TABLE, null, selection, selectionArgs, null, null, null);

        Order order = null;
        if (cursor.moveToFirst()) {
            order = extractOrderFromCursor(cursor);
        }
        cursor.close();
        return order;
    }

    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> orderItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT oi.*, p." + PIZZA_NAME + ", s." + SIZE_NAME + ", c." + CRUST_NAME +
                " FROM " + ORDER_ITEMS_TABLE + " oi" +
                " LEFT JOIN " + PIZZAS_TABLE + " p ON oi." + PIZZA_ID + " = p." + PIZZA_ID +
                " LEFT JOIN " + SIZES_TABLE + " s ON oi." + SIZE_ID + " = s." + SIZE_ID +
                " LEFT JOIN " + CRUST_TABLE + " c ON oi." + CRUST_ID + " = c." + CRUST_ID +
                " WHERE oi." + ORDER_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(orderId)});

        while (cursor.moveToNext()) {
            OrderItem item = new OrderItem();
            item.setItemId(cursor.getInt(cursor.getColumnIndexOrThrow(ITEM_ID)));
            item.setOrderId(orderId);
            item.setPizzaId(cursor.getInt(cursor.getColumnIndexOrThrow(PIZZA_ID)));
            item.setSizeId(cursor.getInt(cursor.getColumnIndexOrThrow(SIZE_ID)));
            item.setCrustId(cursor.getInt(cursor.getColumnIndexOrThrow(CRUST_ID)));
            item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(QUANTITY)));
            item.setItemPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(ITEM_PRICE)));

            item.setPizzaName(cursor.getString(cursor.getColumnIndexOrThrow(PIZZA_NAME)));
            item.setSizeName(cursor.getString(cursor.getColumnIndexOrThrow(SIZE_NAME)));
            item.setCrustName(cursor.getString(cursor.getColumnIndexOrThrow(CRUST_NAME)));

            int specialIndex = cursor.getColumnIndex(SPECIAL_INSTRUCTIONS);
            if (specialIndex != -1) {
                item.setSpecialInstructions(cursor.getString(specialIndex));
            }

            orderItems.add(item);
        }
        cursor.close();
        return orderItems;
    }

    public List<Order> getOrdersByStatus(String status) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = STATUS + "=?";
        String[] selectionArgs = {status};
        String orderBy = ORDER_DATE + " DESC";

        Cursor cursor = db.query(ORDERS_TABLE, null, selection, selectionArgs, null, null, orderBy);

        while (cursor.moveToNext()) {
            Order order = extractOrderFromCursor(cursor);
            orders.add(order);
        }
        cursor.close();
        return orders;
    }

    public boolean updateOrderStatus(int orderId, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();

        android.content.ContentValues values = new android.content.ContentValues();
        values.put(STATUS, newStatus);

        String whereClause = ORDER_ID + "=?";
        String[] whereArgs = {String.valueOf(orderId)};

        int rowsAffected = db.update(ORDERS_TABLE, values, whereClause, whereArgs);
        return rowsAffected > 0;
    }

    // ========== STATISTICS METHODS ==========

    public int getPendingOrderCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = STATUS + "=?";
        String[] selectionArgs = {STATUS_PENDING};

        Cursor cursor = db.query(ORDERS_TABLE, null, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int getCompletedOrderCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = STATUS + "=?";
        String[] selectionArgs = {STATUS_COMPLETED};

        Cursor cursor = db.query(ORDERS_TABLE, null, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int getTotalOrderCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(ORDERS_TABLE, null, null, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    // Only count COMPLETED orders for revenue
    public double getTotalRevenue() {
        SQLiteDatabase db = this.getReadableDatabase();
        double totalRevenue = 0.0;

        String query = "SELECT SUM(" + TOTAL_AMOUNT + ") FROM " + ORDERS_TABLE +
                " WHERE " + STATUS + " = '" + STATUS_COMPLETED + "'";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            totalRevenue = cursor.getDouble(0);
        }
        cursor.close();
        return totalRevenue;
    }

    // Only count COMPLETED orders for today's revenue
    public double getTodayRevenue() {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT SUM(" + TOTAL_AMOUNT + ") FROM " + ORDERS_TABLE +
                " WHERE DATE(" + ORDER_DATE + ") = DATE('now')" +
                " AND " + STATUS + " = '" + STATUS_COMPLETED + "'";
        Cursor cursor = db.rawQuery(query, null);

        double revenue = 0.0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            revenue = cursor.getDouble(0);
        }
        cursor.close();
        return revenue;
    }

    // Only count COMPLETED orders for week revenue
    public double getWeekRevenue() {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT SUM(" + TOTAL_AMOUNT + ") FROM " + ORDERS_TABLE +
                " WHERE DATE(" + ORDER_DATE + ") >= DATE('now', '-7 days')" +
                " AND " + STATUS + " = '" + STATUS_COMPLETED + "'";
        Cursor cursor = db.rawQuery(query, null);

        double revenue = 0.0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            revenue = cursor.getDouble(0);
        }
        cursor.close();
        return revenue;
    }

    // Today's order count (ALL orders for today)
    public int getTodayOrderCount() {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM " + ORDERS_TABLE +
                " WHERE DATE(" + ORDER_DATE + ") = DATE('now')";
        Cursor cursor = db.rawQuery(query, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getUserCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(USERS_TABLE, null, null, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    // Get monthly revenue data for charts
    public int[] getMonthlyRevenueData() {
        int[] data = new int[12];
        SQLiteDatabase db = this.getReadableDatabase();

        String currentYear = "2024"; // You can make this dynamic
        String[] months = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};

        for (int i = 0; i < 12; i++) {
            String monthStart = currentYear + "-" + months[i] + "-01";
            String monthEnd = currentYear + "-" + months[i] + "-31";

            String query = "SELECT SUM(" + TOTAL_AMOUNT + ") FROM " + ORDERS_TABLE +
                    " WHERE DATE(" + ORDER_DATE + ") >= ? AND DATE(" + ORDER_DATE + ") <= ?" +
                    " AND " + STATUS + " = '" + STATUS_COMPLETED + "'";

            Cursor cursor = db.rawQuery(query, new String[]{monthStart, monthEnd});

            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                data[i] = (int) cursor.getDouble(0);
            } else {
                data[i] = 0;
            }
            cursor.close();
        }

        return data;
    }

    // Helper method to extract order from cursor
    private Order extractOrderFromCursor(Cursor cursor) {
        Order order = new Order();
        order.setOrderId(cursor.getInt(cursor.getColumnIndexOrThrow(ORDER_ID)));
        order.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(USER_ID)));
        order.setOrderDate(cursor.getString(cursor.getColumnIndexOrThrow(ORDER_DATE)));
        order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(STATUS)));
        order.setCustomerName(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_NAME)));
        order.setCustomerPhone(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_PHONE)));
        order.setDeliveryAddress(cursor.getString(cursor.getColumnIndexOrThrow(DELIVERY_ADDRESS)));
        order.setSpecialInstructions(cursor.getString(cursor.getColumnIndexOrThrow(SPECIAL_INSTRUCTIONS)));
        order.setTotalAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(TOTAL_AMOUNT)));

        int estimatedIndex = cursor.getColumnIndex(ESTIMATED_COMPLETION);
        if (estimatedIndex != -1) {
            order.setEstimatedCompletion(cursor.getString(estimatedIndex));
        }

        return order;
    }
}