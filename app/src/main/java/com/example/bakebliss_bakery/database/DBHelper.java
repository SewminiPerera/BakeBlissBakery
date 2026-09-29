package com.example.bakebliss_bakery.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DBNAME = "FoodApp.db";
    public static final int DBVERSION = 3;

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
                "category TEXT, " +
                "image_resource INTEGER)");

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
        if (oldVersion < 3) {
            // Drop old food table and re-seed with new categories
            MyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_FOOD);
            MyDB.execSQL("create Table " + TABLE_FOOD + "(" +
                    "id INTEGER primary key autoincrement, " +
                    "name TEXT, " +
                    "description TEXT, " +
                    "price DOUBLE, " +
                    "category TEXT, " +
                    "image_resource INTEGER)");
            seedFoodItems(MyDB);
        }
    }

    // Method to insert all food items by category
    private void seedFoodItems(SQLiteDatabase db) {
        // ===== BURGER =====
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Chicken Burger', 'A juicy, crispy-fried chicken patty layered with fresh lettuce, ripe tomatoes, creamy mayo, and pickles in a toasted sesame bun. A timeless classic that satisfies every craving.', 650.00, 'Burger')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Double Cheese Burger', 'Two flame-grilled beef patties stacked high with double layers of melted cheddar cheese, caramelized onions, mustard, and ketchup in a brioche bun. Bold, hearty, and irresistible.', 1250.00, 'Burger')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Cheese Burger', 'A perfectly seasoned beef patty crowned with a thick slice of melted American cheese, crisp lettuce, fresh tomato, and tangy special sauce in a soft toasted bun.', 850.00, 'Burger')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Crispy Chicken Burger', 'Golden, crunchy double-breaded chicken fillet tossed in a buttery honey glaze, topped with coleslaw and sriracha aioli in a soft brioche bun. Crunchy on the outside, tender inside.', 1050.00, 'Burger')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Classic Beef Burger', 'A hand-formed prime beef patty grilled to perfection with classic toppings — fresh lettuce, ripe tomatoes, dill pickles, and our signature house sauce, all in a lightly toasted bun.', 950.00, 'Burger')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Spicy Chicken Burger', 'Fiery marinated chicken fillet coated in a bold chilli spice blend, topped with jalapeños, pepper jack cheese, and cooling ranch drizzle. For those who love the heat!', 850.00, 'Burger')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('BBQ Beef Burger', 'Smoky BBQ-glazed beef patty loaded with crispy onion rings, bacon strips, melted gouda, and tangy BBQ sauce. A backyard BBQ experience in every bite.', 1150.00, 'Burger')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Grilled Chicken Burger', 'Tender char-grilled chicken breast marinated in herbs and lemon, served with avocado slices, mixed greens, and a light yogurt dressing. Healthy never tasted this good.', 1100.00, 'Burger')");

        // ===== PASTRY =====
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Chicken Pastry', 'Flaky golden puff pastry filled with a savory blend of spiced minced chicken, herbs, and sautéed onions. Freshly baked and served warm — a perfect snack anytime.', 200.00, 'Pastry')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Vegetable Pastry', 'A light, crispy pastry shell packed with a delicious mix of seasoned garden vegetables including carrots, peas, potatoes, and bell peppers. A wholesome and satisfying treat.', 150.00, 'Pastry')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Fish Pastry', 'Buttery puff pastry filled with a flavorful blend of flaked fish, spiced potatoes, and fresh herbs. Golden-baked to perfection with a satisfying crunch in every bite.', 170.00, 'Pastry')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Beef Pastry', 'Rich and hearty puff pastry filled with slow-cooked spiced minced beef, caramelized onions, and herbs. A bolder, meat-lovers pastry baked to a beautiful golden brown.', 250.00, 'Pastry')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Egg Pastry', 'A delicate pastry shell cradling a perfectly set spiced egg filling with a hint of pepper and fresh herbs. Simple, satisfying, and great for breakfast or as a snack.', 150.00, 'Pastry')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Sausage Pastry', 'Crispy golden pastry wrapped snugly around a juicy, seasoned pork sausage. Classic comfort food baked fresh every day — great on its own or with a dipping sauce.', 150.00, 'Pastry')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Chicken & Cheese Pastry', 'Premium puff pastry generously filled with tender seasoned chicken and melted mozzarella cheese, creating the perfect gooey, savory combination in every flaky bite.', 350.00, 'Pastry')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Curry Pastry', 'A warm, aromatic pastry filled with a rich and fragrant curry blend of potatoes, spices, and tender pieces of meat. An exciting spiced twist on the traditional pastry.', 100.00, 'Pastry')");

        // ===== CAKE =====
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Chocolate Fudge Cake', 'An indulgent, ultra-moist layered chocolate cake generously frosted with rich chocolate fudge ganache. Decorated with chocolate shavings — a chocoholic dream come true.', 2200.00, 'Cake')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Strawberry Cream Cake', 'Light and airy vanilla sponge layers filled and frosted with fresh whipped cream and ripe strawberries. Elegant, refreshing, and perfect for any celebration.', 2100.00, 'Cake')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Vanilla Dream Cake', 'A soft, fluffy vanilla chiffon cake with silky smooth vanilla buttercream frosting. Delicate pearl-white decorations make this a dreamy, timeless centerpiece.', 1500.00, 'Cake')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Red Velvet Cake', 'The iconic deep red velvet cake with a velvety, tender crumb, layered with luscious cream cheese frosting. A showstopping cake that looks and tastes absolutely stunning.', 3000.00, 'Cake')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Lemon Delight Cake', 'A zesty, sunshine-bright lemon sponge cake filled with tangy lemon curd and frosted with light lemon buttercream. Refreshing, vibrant, and perfectly balanced in sweetness.', 2000.00, 'Cake')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Caramel Crunch Cake', 'Decadent caramel sponge layers filled with salted caramel cream and topped with crunchy caramelized toffee shards. A sophisticated treat with the perfect contrast of textures.', 2500.00, 'Cake')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Mango Bliss Cake', 'Tropical mango mousse layered between moist vanilla sponge, topped with fresh mango coulis and coconut cream. A taste of paradise in every slice.', 1700.00, 'Cake')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Oreo Cream Cake', 'Luscious cookies and cream cake featuring Oreo-studded sponge layers, whipped Oreo cream filling, and a dark chocolate ganache drizzle with whole Oreo cookie decorations.', 2000.00, 'Cake')");

        // ===== BUN =====
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Vegetable Bun', 'Soft, pillowy steamed bun filled with a deliciously seasoned mix of fresh vegetables including carrots, cabbage, and green onions. A light and wholesome everyday snack.', 100.00, 'Bun')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Fish Bun', 'A freshly baked soft bun generously filled with spicy flaked fish and potato filling, perfectly seasoned with herbs and spices. A flavorful Sri Lankan-style bakery favorite.', 120.00, 'Bun')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Sausage Bun', 'A soft, golden-baked bun wrapped around a juicy whole pork sausage with a streak of mustard and ketchup. Classic, quick, and always delicious.', 150.00, 'Bun')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Egg Bun', 'A fluffy baked bun filled with a perfectly seasoned spiced egg mixture. Simple, hearty, and full of flavor — a popular breakfast and snack-time staple.', 120.00, 'Bun')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Chocolate Bun', 'A soft, enriched sweet bun filled or swirled with rich chocolate filling. Slightly sweet with a tender crumb — an irresistible treat for all chocolate lovers.', 150.00, 'Bun')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Tea Bun', 'A classic simple soft bun, lightly sweetened with a golden crust — the perfect companion for your morning or evening cup of tea. Soft, plain, and comforting.', 50.00, 'Bun')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Cream Bun', 'A beautifully soft bun split open and filled with a generous dollop of lightly sweetened whipped cream. A bakery classic that never goes out of style.', 80.00, 'Bun')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Hotdog Bun', 'A premium soft hotdog roll filled with a juicy grilled sausage, topped with caramelized onions, mustard, ketchup, and relish. Street food satisfaction at its finest.', 250.00, 'Bun')");

        // ===== BEVERAGE =====
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Iced Coffee', 'A bold, refreshing cold brew coffee poured over ice with a splash of milk and your choice of sweetness. Smooth, rich, and energizing — your perfect daily pick-me-up.', 700.00, 'Beverage')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Vanilla Milkshake', 'A thick and creamy classic vanilla milkshake blended with premium ice cream and topped with fresh whipped cream. Nostalgic, sweet, and endlessly satisfying.', 650.00, 'Beverage')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Chocolate Milkshake', 'Rich, velvety chocolate milkshake made with dark chocolate ice cream, blended to perfection and finished with whipped cream and chocolate syrup drizzle.', 800.00, 'Beverage')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Strawberry Milkshake', 'Fresh strawberries blended with creamy strawberry ice cream into a gorgeous pink shake, topped with whipped cream and a fresh strawberry garnish. Sweet, fruity, and fun.', 800.00, 'Beverage')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Bubble Tea', 'Creamy, chilled milk tea loaded with perfectly chewy tapioca pearls. Choose from a variety of flavors — taro, matcha, or classic brown sugar. A boba experience like no other.', 1000.00, 'Beverage')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Coca Cola', 'Ice-cold refreshing Coca-Cola served over crushed ice. The world\'s most iconic beverage — perfectly fizzy, sweet, and refreshing. Goes great with any meal.', 400.00, 'Beverage')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Sprite', 'Crisp, clean lemon-lime Sprite served ice cold. Refreshingly light and bubbly with a perfect citrus zing — the ideal thirst-quencher on a warm day.', 350.00, 'Beverage')");
        db.execSQL("INSERT INTO " + TABLE_FOOD + " (name, description, price, category) VALUES ('Cream Soda', 'A delightfully sweet and creamy soda with a smooth vanilla-cream flavor. Light pink, effervescent, and nostalgic — a uniquely satisfying soft drink experience.', 350.00, 'Beverage')");
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