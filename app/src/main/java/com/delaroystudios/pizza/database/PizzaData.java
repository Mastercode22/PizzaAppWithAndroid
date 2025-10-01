package com.delaroystudios.pizza.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import static com.delaroystudios.pizza.database.Constants.*;

public class PizzaData extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "pizza_enhanced.db";
    private static final int DATABASE_VERSION = 1;

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

        // Create pizzas table
        db.execSQL("CREATE TABLE " + PIZZAS_TABLE + " (" +
                PIZZA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PIZZA_NAME + " TEXT NOT NULL, " +
                PIZZA_DESCRIPTION + " TEXT, " +
                CATEGORY_ID + " INTEGER, " +
                BASE_PRICE + " REAL NOT NULL, " +
                IMAGE_URL + " TEXT, " +
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop all tables and recreate
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
}
