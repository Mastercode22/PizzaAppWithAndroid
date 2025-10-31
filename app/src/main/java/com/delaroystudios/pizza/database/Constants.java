package com.delaroystudios.pizza.database;

public class Constants {

    // Database info
    public static final String DATABASE_NAME = "pizza_enhanced.db";
    public static final int DATABASE_VERSION = 6; // INCREMENT THIS to trigger database upgrade

    // Table names
    public static final String USERS_TABLE = "users";
    public static final String ADMINS_TABLE = "admins";
    public static final String CATEGORIES_TABLE = "categories";
    public static final String PIZZAS_TABLE = "pizzas";
    public static final String SIZES_TABLE = "sizes";
    public static final String CRUST_TABLE = "crust_types";
    public static final String TOPPINGS_TABLE = "toppings";
    public static final String ORDERS_TABLE = "orders";
    public static final String ORDER_ITEMS_TABLE = "order_items";
    public static final String CART_TABLE = "cart";
    public static final String TABLE_NAME = "pizza_orders"; // Legacy table

    // User columns
    public static final String USER_ID = "user_id";
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";
    public static final String PASSWORD_HASH = "password_hash";
    public static final String FULL_NAME = "full_name";
    public static final String PHONE = "phone";
    public static final String ADDRESS = "address";
    public static final String CREATED_AT = "created_at";
    public static final String IS_ACTIVE = "is_active";

    // Admin columns
    public static final String ADMIN_ID = "admin_id";
    public static final String ROLE = "role";

    // Category columns
    public static final String CATEGORY_ID = "category_id";
    public static final String CATEGORY_NAME = "category_name";
    public static final String DESCRIPTION = "description";

    // Pizza columns
    public static final String PIZZA_ID = "pizza_id";
    public static final String PIZZA_NAME = "pizza_name";
    public static final String PIZZA_DESCRIPTION = "pizza_description";
    public static final String BASE_PRICE = "base_price";
    public static final String IMAGE_URL = "image_url";
    public static final String IMAGE_RESOURCE = "image_resource";
    public static final String IS_AVAILABLE = "is_available";

    // Size columns
    public static final String SIZE_ID = "size_id";
    public static final String SIZE_NAME = "size_name";
    public static final String PRICE_MULTIPLIER = "price_multiplier";

    // Crust columns
    public static final String CRUST_ID = "crust_id";
    public static final String CRUST_NAME = "crust_name";
    public static final String ADDITIONAL_PRICE = "additional_price";

    // Topping columns
    public static final String TOPPING_ID = "topping_id";
    public static final String TOPPING_NAME = "topping_name";
    public static final String TOPPING_PRICE = "topping_price";
    public static final String TOPPING_CATEGORY = "topping_category";

    // Order columns
    public static final String ORDER_ID = "order_id";
    public static final String ORDER_DATE = "order_date";
    public static final String TOTAL_AMOUNT = "total_amount";
    public static final String STATUS = "status";
    public static final String CUSTOMER_NAME = "customer_name";
    public static final String CUSTOMER_PHONE = "customer_phone";
    public static final String DELIVERY_ADDRESS = "delivery_address";
    public static final String SPECIAL_INSTRUCTIONS = "special_instructions";
    public static final String ESTIMATED_COMPLETION = "estimated_completion";

    // Order Item columns
    public static final String ITEM_ID = "item_id";
    public static final String ITEM_PRICE = "item_price";
    public static final String QUANTITY = "quantity";

    // Cart columns
    public static final String CART_ID = "cart_id";
    public static final String ADDED_AT = "added_at";

    // Legacy columns
    public static final String _ID = "_id";
    public static final String SIZE = "size";
    public static final String CRUST = "crust";
    public static final String TOPPINGS_WHOLE = "toppings_whole";
    public static final String TOPPINGS_LEFT = "toppings_left";
    public static final String TOPPINGS_RIGHT = "toppings_right";

    // ============================================
    // CRITICAL FIX: Order Status Constants
    // MUST be lowercase to match Order.java and database
    // ============================================
    public static final String STATUS_PENDING = "pending";          // Changed from "Pending"
    public static final String STATUS_IN_PROGRESS = "in_progress";  // NEW - was missing
    public static final String STATUS_COMPLETED = "completed";      // Changed from "Completed"
    public static final String STATUS_CANCELLED = "cancelled";      // Changed from "Cancelled"

    // Legacy status (if needed for backward compatibility)
    @Deprecated
    public static final String STATUS_DELIVERED = "delivered";
    @Deprecated
    public static final String STATUS_PREPARING = "preparing";

    // Helper method to validate status
    public static boolean isValidStatus(String status) {
        return STATUS_PENDING.equals(status) ||
                STATUS_IN_PROGRESS.equals(status) ||
                STATUS_COMPLETED.equals(status) ||
                STATUS_CANCELLED.equals(status);
    }

    // Helper method to get display name for status (with proper capitalization)
    public static String getStatusDisplayName(String status) {
        if (status == null) return "Unknown";

        switch (status.toLowerCase()) {
            case STATUS_PENDING:
                return "Pending";
            case STATUS_IN_PROGRESS:
                return "In Progress";
            case STATUS_COMPLETED:
                return "Completed";
            case STATUS_CANCELLED:
                return "Cancelled";
            case STATUS_DELIVERED:
                return "Delivered";
            case STATUS_PREPARING:
                return "Preparing";
            default:
                return "Unknown";
        }
    }

    // Helper method to normalize status from any case to lowercase
    public static String normalizeStatus(String status) {
        if (status == null) return STATUS_PENDING;

        String lower = status.toLowerCase().trim();
        switch (lower) {
            case "pending":
                return STATUS_PENDING;
            case "in progress":
            case "in_progress":
            case "inprogress":
                return STATUS_IN_PROGRESS;
            case "completed":
            case "complete":
                return STATUS_COMPLETED;
            case "cancelled":
            case "canceled":
                return STATUS_CANCELLED;
            default:
                return status.toLowerCase();
        }
    }

    // Private constructor to prevent instantiation
    private Constants() {
    }
}