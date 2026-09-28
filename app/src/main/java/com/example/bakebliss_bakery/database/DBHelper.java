package com.example.bakebliss_bakery.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DBNAME = "FoodApp.db";
    public static final int DBVERSION = 2;

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_FOOD = "food_items";
    public static final String TABLE_ORDERS = "orders";
    public static final String TABLE_CART = "cart";

    // User Table Columns
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";

    public DBHelper(Context context) {
        super(context, DBNAME, null, DBVERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase MyDB) {
        // 1. Create Users Table
        MyDB.execSQL("create Table " + TABLE_USERS + "(" +
                "username TEXT primary key, " +
                "password TEXT, " +
                "email TEXT, " +
                "phone TEXT, " +
                "address TEXT, " +
                "profile_image TEXT)");

        // Create Food Items Table
        MyDB.execSQL("create Table " + TABLE_FOOD + "(" +
                "id INTEGER primary key autoincrement, " +
                "name TEXT, " +
                "description TEXT, " +
                "price DOUBLE, " +
                "image_resource INTEGER)"); // We can store drawable ID for simplicity

        // Create Orders Table WITH FOREIGN KEY
        MyDB.execSQL("create Table " + TABLE_ORDERS + "(" +
                "order_id INTEGER primary key autoincrement, " +
                "username TEXT, " +
                "food_id INTEGER, " +
                "quantity INTEGER, " +
                "total_price DOUBLE, " +
                "status TEXT, " +
                "order_date DATETIME DEFAULT (datetime('now','localtime')), " +
                "FOREIGN KEY(username) REFERENCES " + TABLE_USERS + "(username), " +
                "FOREIGN KEY(food_id) REFERENCES " + TABLE_FOOD + "(id))");

        // Create Cart Table
        MyDB.execSQL("create Table " + TABLE_CART + "(" +
                "cart_id INTEGER primary key autoincrement, " +
                "username TEXT, " +
                "food_name TEXT, " +
                "price DOUBLE, " +
                "quantity INTEGER)");

        // Pre-insert 20 Sri Lankan & Popular food items (Seeding)
        seedFoodItems(MyDB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase MyDB, int oldVersion, int newVersion) {
        // --- If database is upgraded, add new columns to existing Users table instead of dropping it ---
        if (oldVersion < 2) {
            MyDB.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN address TEXT DEFAULT 'Not Set'");
            MyDB.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN profile_image TEXT DEFAULT ''");
        }
    }

    // Method to insert initial 20 food data items
    private void seedFoodItems(SQLiteDatabase db) {
        // Sri Lankan and Popular Items with prices in Rs.
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price) VALUES ('Chicken Kottu', 'Spicy Sri Lankan street food with chicken', 850.00)");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price) VALUES ('Cheese Kottu', 'Creamy cheese kottu with roasted chicken', 1200.00)");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price) VALUES ('Chicken Fried Rice', 'Basmati rice wok-tossed with chicken and veggies', 900.00)");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price) VALUES ('Nasi Goreng', 'Indonesian style spicy rice topped with a fried egg', 1100.00)");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price) VALUES ('Chicken Biryani', 'Aromatic spiced rice served with chicken and egg', 1350.00)");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price) VALUES ('String Hoppers (10 pcs)', 'Served with dhal, coconut sambol and chicken curry', 450.00)");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price) VALUES ('Plain Hoppers (5 pcs)', 'Crispy plain hoppers served with lunu miris', 250.00)");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price) VALUES ('Egg Hopper', 'Crispy hopper with a soft baked egg inside', 100.00)");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price) VALUES ('Lamprais', 'Traditional Dutch burgher meal wrapped in banana leaf', 1500.00)");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price) VALUES ('Roast Paan & Curry', 'Wood-fired roast bread with spicy pork or chicken curry', 650.00)");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price) VALUES ('Chicken Burger', 'Crispy chicken patty with fresh lettuce and mayo', 650.00)");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price) VALUES ('Double Cheese Burger', 'Double beef patty with melted cheese and pickles', 1250.00)");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price) VALUES ('Spicy Chicken Sub', 'Footlong submarine sandwich with spicy chicken fillings', 950.00)");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price) VALUES ('BBQ Chicken Pizza', 'Large pizza topped with BBQ chicken, onions and cheese', 2200.00)");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price) VALUES ('Margherita Pizza', 'Classic medium pizza with fresh tomatoes and mozzarella', 1500.00)");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price) VALUES ('Fish Roll', 'Crispy crumbed roll stuffed with spicy fish and potato', 120.00)");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price) VALUES ('Vegetable Roti', 'Spicy vegetable mix folded into a triangular flatbread', 80.00)");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price) VALUES ('Chicken Samosa', 'Crispy pastry filled with minced chicken and peas', 100.00)");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price) VALUES ('Isso Vade', 'Crispy lentil patty topped with spicy fried prawns', 150.00)");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price) VALUES ('Chocolate Biscuit Pudding', 'Classic Sri Lankan dessert layered with chocolate', 400.00)");
    }

    // Insert User Logic (Updated with Default empty fields for new columns)
    public boolean registerUser(String username, String email, String password, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);
        values.put("email", email);
        values.put("phone", phone);
        values.put("address", "Not Set");
        values.put("profile_image", "");
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    // Check if user already exists
    public boolean checkUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        // --- Use COLLATE NOCASE to make username case-insensitive ---
        Cursor cursor = db.rawQuery("Select * from " + TABLE_USERS + " where username COLLATE NOCASE = ?", new String[]{username});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Check Username and Password (Login Logic)
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        // --- Use COLLATE NOCASE to make username case-insensitive for login ---
        Cursor cursor = db.rawQuery("Select * from " + TABLE_USERS + " where username COLLATE NOCASE = ? and password = ?", new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Get User Details by Username
    public Cursor getUserDetails(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        // --- Use COLLATE NOCASE to fetch user details correctly regardless of case ---
        Cursor cursor = db.rawQuery("Select * from " + TABLE_USERS + " where username COLLATE NOCASE = ?", new String[]{username});
        return cursor;
    }

    // --- Update User Profile Details ---
    public boolean updateUserProfile(String username, String email, String phone, String address, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("phone", phone);
        values.put("address", address);
        values.put("profile_image", imagePath);

        // --- Use COLLATE NOCASE for updating profile ---
        long result = db.update(TABLE_USERS, values, "username COLLATE NOCASE = ?", new String[]{username});
        return result != -1;
    }

    // Method to retrieve all food items from the 'food_items' table
    public Cursor getAllFoodItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_FOOD, null);
    }

    // Helper method to find food_id by food_name
    public int getFoodIdByName(String foodName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String cleanName = foodName.replace(" (50% OFF)", "");
        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_FOOD + " WHERE name = ?", new String[]{cleanName});
        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }
        cursor.close();
        return id;
    }

    // Insert Order
    public boolean insertOrder(String username, int foodId, int quantity, double totalPrice, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("food_id", foodId);
        values.put("quantity", quantity);
        values.put("total_price", totalPrice);
        values.put("status", status);
        long result = db.insert(TABLE_ORDERS, null, values);
        return result != -1;
    }

    // Fetch User Orders with Food Name
    public Cursor getUserOrders(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        // --- Use COLLATE NOCASE for orders ---
        String query = "SELECT f.name, o.status, o.quantity, o.total_price, o.order_date FROM " + TABLE_ORDERS + " o " +
                "INNER JOIN " + TABLE_FOOD + " f ON o.food_id = f.id " +
                "WHERE o.username COLLATE NOCASE = ? ORDER BY o.order_id DESC";
        return db.rawQuery(query, new String[]{username});
    }

    // Add Item to Cart
    public boolean addToCart(String username, String foodName, double price, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        // --- Use COLLATE NOCASE for cart check ---
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CART + " WHERE username COLLATE NOCASE = ? AND food_name=?", new String[]{username, foodName});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int existingQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
            int cartId = cursor.getInt(cursor.getColumnIndexOrThrow("cart_id"));
            cursor.close();
            ContentValues values = new ContentValues();
            values.put("quantity", existingQuantity + quantity);
            long result = db.update(TABLE_CART, values, "cart_id=?", new String[]{String.valueOf(cartId)});
            return result != -1;
        } else {
            cursor.close();
            ContentValues values = new ContentValues();
            values.put("username", username);
            values.put("food_name", foodName);
            values.put("price", price);
            values.put("quantity", quantity);
            long result = db.insert(TABLE_CART, null, values);
            return result != -1;
        }
    }

    // Get all cart items for a user
    public Cursor getCartItems(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        // --- Use COLLATE NOCASE for cart items ---
        return db.rawQuery("SELECT * FROM " + TABLE_CART + " WHERE username COLLATE NOCASE = ?", new String[]{username});
    }

    // Update Cart Item Quantity
    public void updateCartQuantity(int cartId, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("quantity", newQuantity);
        db.update(TABLE_CART, values, "cart_id=?", new String[]{String.valueOf(cartId)});
    }

    // Remove Item from Cart
    public void removeFromCart(int cartId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART, "cart_id=?", new String[]{String.valueOf(cartId)});
    }

    // Clear Cart after placing order
    public void clearCart(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        // --- Use COLLATE NOCASE for clearing cart ---
        db.delete(TABLE_CART, "username COLLATE NOCASE = ?", new String[]{username});
    }
}