package com.delaroystudios.pizza.database;

import android.provider.BaseColumns;

public interface Constants extends BaseColumns {

    // Users table
    String USERS_TABLE = "users";
    String USER_ID = "user_id";
    String USERNAME = "username";
    String EMAIL = "email";
    String PASSWORD_HASH = "password_hash";
    String FULL_NAME = "full_name";
    String PHONE = "phone";
    String ADDRESS = "address";
    String CREATED_AT = "created_at";
    String IS_ACTIVE = "is_active";

    // Admin table
    String ADMINS_TABLE = "admins";
    String ADMIN_ID = "admin_id";
    String ROLE = "role";

    // Categories table
    String CATEGORIES_TABLE = "categories";
    String CATEGORY_ID = "category_id";
    String CATEGORY_NAME = "name";
    String DESCRIPTION = "description";

    // Enhanced pizzas table
    String PIZZAS_TABLE = "pizzas";
    String PIZZA_ID = "pizza_id";
    String PIZZA_NAME = "name";
    String PIZZA_DESCRIPTION = "description";
    String BASE_PRICE = "base_price";
    String IMAGE_URL = "image_url";
    String IS_AVAILABLE = "is_available";

    // Pizza sizes table
    String SIZES_TABLE = "pizza_sizes";
    String SIZE_ID = "size_id";
    String SIZE_NAME = "size_name";
    String PRICE_MULTIPLIER = "price_multiplier";

    // Crust types table
    String CRUST_TABLE = "crust_types";
    String CRUST_ID = "crust_id";
    String CRUST_NAME = "crust_name";
    String ADDITIONAL_PRICE = "additional_price";

    // Toppings table
    String TOPPINGS_TABLE = "toppings";
    String TOPPING_ID = "topping_id";
    String TOPPING_NAME = "name";
    String TOPPING_PRICE = "price";
    String TOPPING_CATEGORY = "category";

    // Orders table
    String ORDERS_TABLE = "orders";
    String ORDER_ID = "order_id";
    String ORDER_DATE = "order_date";
    String TOTAL_AMOUNT = "total_amount";
    String STATUS = "status";
    String CUSTOMER_NAME = "customer_name";
    String CUSTOMER_PHONE = "customer_phone";
    String DELIVERY_ADDRESS = "delivery_address";
    String SPECIAL_INSTRUCTIONS = "special_instructions";
    String ESTIMATED_COMPLETION = "estimated_completion_time";

    // Order items table
    String ORDER_ITEMS_TABLE = "order_items";
    String ITEM_ID = "item_id";
    String QUANTITY = "quantity";
    String ITEM_PRICE = "item_price";

    // Order item toppings table
    String ORDER_TOPPINGS_TABLE = "order_item_toppings";
    String PLACEMENT = "placement";

    // Cart tables
    String CART_TABLE = "cart_items";
    String CART_ID = "cart_id";
    String ADDED_AT = "added_at";

    String CART_TOPPINGS_TABLE = "cart_item_toppings";

    // Legacy support (for backward compatibility)
    String TABLE_NAME = "pizza";
    String SIZE = "size";
    String CRUST = "crust";
    String TOPPINGS_WHOLE = "toppingsWhole";
    String TOPPINGS_LEFT = "toppingsLeft";
    String TOPPINGS_RIGHT = "toppingsRight";

    // Order status constants
    String STATUS_PENDING = "pending";
    String STATUS_IN_PROGRESS = "in_progress";
    String STATUS_COMPLETED = "completed";
    String STATUS_CANCELLED = "cancelled";

    // Placement constants
    String PLACEMENT_WHOLE = "whole";
    String PLACEMENT_LEFT = "left";
    String PLACEMENT_RIGHT = "right";


}
