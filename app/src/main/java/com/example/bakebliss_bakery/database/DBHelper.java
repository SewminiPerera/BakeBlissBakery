package com.example.bakebliss_bakery.database;

import android.content.Context;
import android.util.Log;

import com.example.bakebliss_bakery.models.CartModel;
import com.example.bakebliss_bakery.models.FoodModel;
import com.example.bakebliss_bakery.models.OrderModel;
import com.example.bakebliss_bakery.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Firebase-backed DBHelper for BakeBliss Bakery.
 * Replaces SQLite with Cloud Firestore & Firebase Authentication.
 */
public class DBHelper {

    private static final String TAG = "DBHelper";

    // Firestore Collections
    public static final String COLLECTION_FOOD = "food_items";
    public static final String COLLECTION_USERS = "users";
    public static final String SUB_COLLECTION_CART = "cart";
    public static final String SUB_COLLECTION_ORDERS = "orders";

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    // --- Callback Interfaces ---
    public interface ActionCallback {
        void onComplete(boolean success);
    }

    public interface FoodListCallback {
        void onCallback(List<FoodModel> list);
    }

    public interface CartListCallback {
        void onCallback(List<CartModel> list);
    }

    public interface OrderListCallback {
        void onCallback(List<OrderModel> list);
    }

    public interface UserProfileCallback {
        void onCallback(User user);
    }

    // Constructors
    public DBHelper() {
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public DBHelper(Context context) {
        this();
    }

    /**
     * Resolves the current user's document ID.
     * Uses Firebase Auth UID if logged in, otherwise falls back to username or guest.
     */
    public String resolveUserDocId(String username) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && currentUser.getUid() != null && !currentUser.getUid().isEmpty()) {
            return currentUser.getUid();
        }
        if (username != null && !username.trim().isEmpty()) {
            return username.trim().toLowerCase(Locale.ROOT);
        }
        return "guest_user";
    }

    // =========================================================================
    //  FOOD ITEMS & SEEDING
    // =========================================================================

    /**
     * Seeds initial bakery items to Firestore if the collection is empty.
     */
    public void seedFoodItemsIfNeeded() {
        firestore.collection(COLLECTION_FOOD)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        Log.d(TAG, "food_items collection is empty. Seeding data...");
                        seedFoodItems();
                    } else {
                        Log.d(TAG, "food_items already seeded.");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error checking food items seed: " + e.getMessage()));
    }

    public void seedFoodItems() {
        List<Map<String, Object>> items = new ArrayList<>();

        // ===== BURGER =====
        items.add(createFoodMap(1, "Chicken Burger", "A juicy, crispy-fried chicken patty layered with fresh lettuce, ripe tomatoes, creamy mayo, and pickles in a toasted sesame bun. A timeless classic that satisfies every craving.", 650.00, "Burger"));
        items.add(createFoodMap(2, "Double Cheese Burger", "Two flame-grilled beef patties stacked high with double layers of melted cheddar cheese, caramelized onions, mustard, and ketchup in a brioche bun. Bold, hearty, and irresistible.", 1250.00, "Burger"));
        items.add(createFoodMap(3, "Cheese Burger", "A perfectly seasoned beef patty crowned with a thick slice of melted American cheese, crisp lettuce, fresh tomato, and tangy special sauce in a soft toasted bun.", 850.00, "Burger"));
        items.add(createFoodMap(4, "Crispy Chicken Burger", "Golden, crunchy double-breaded chicken fillet tossed in a buttery honey glaze, topped with coleslaw and sriracha aioli in a soft brioche bun. Crunchy on the outside, tender inside.", 1050.00, "Burger"));
        items.add(createFoodMap(5, "Classic Beef Burger", "A hand-formed prime beef patty grilled to perfection with classic toppings — fresh lettuce, ripe tomatoes, dill pickles, and our signature house sauce, all in a lightly toasted bun.", 950.00, "Burger"));
        items.add(createFoodMap(6, "Spicy Chicken Burger", "Fiery marinated chicken fillet coated in a bold chilli spice blend, topped with jalapeños, pepper jack cheese, and cooling ranch drizzle. For those who love the heat!", 850.00, "Burger"));
        items.add(createFoodMap(7, "BBQ Beef Burger", "Smoky BBQ-glazed beef patty loaded with crispy onion rings, bacon strips, melted gouda, and tangy BBQ sauce. A backyard BBQ experience in every bite.", 1150.00, "Burger"));
        items.add(createFoodMap(8, "Grilled Chicken Burger", "Tender char-grilled chicken breast marinated in herbs and lemon, served with avocado slices, mixed greens, and a light yogurt dressing. Healthy never tasted this good.", 1100.00, "Burger"));

        // ===== PASTRY =====
        items.add(createFoodMap(9, "Chicken Pastry", "Flaky golden puff pastry filled with a savory blend of spiced minced chicken, herbs, and sautéed onions. Freshly baked and served warm — a perfect snack anytime.", 200.00, "Pastry"));
        items.add(createFoodMap(10, "Vegetable Pastry", "A light, crispy pastry shell packed with a delicious mix of seasoned garden vegetables including carrots, peas, potatoes, and bell peppers. A wholesome and satisfying treat.", 150.00, "Pastry"));
        items.add(createFoodMap(11, "Fish Pastry", "Buttery puff pastry filled with a flavorful blend of flaked fish, spiced potatoes, and fresh herbs. Golden-baked to perfection with a satisfying crunch in every bite.", 170.00, "Pastry"));
        items.add(createFoodMap(12, "Beef Pastry", "Rich and hearty puff pastry filled with slow-cooked spiced minced beef, caramelized onions, and herbs. A bolder, meat-lovers pastry baked to a beautiful golden brown.", 250.00, "Pastry"));
        items.add(createFoodMap(13, "Egg Pastry", "A delicate pastry shell cradling a perfectly set spiced egg filling with a hint of pepper and fresh herbs. Simple, satisfying, and great for breakfast or as a snack.", 150.00, "Pastry"));
        items.add(createFoodMap(14, "Sausage Pastry", "Crispy golden pastry wrapped snugly around a juicy, seasoned pork sausage. Classic comfort food baked fresh every day — great on its own or with a dipping sauce.", 150.00, "Pastry"));
        items.add(createFoodMap(15, "Chicken & Cheese Pastry", "Premium puff pastry generously filled with tender seasoned chicken and melted mozzarella cheese, creating the perfect gooey, savory combination in every flaky bite.", 350.00, "Pastry"));
        items.add(createFoodMap(16, "Curry Pastry", "A warm, aromatic pastry filled with a rich and fragrant curry blend of potatoes, spices, and tender pieces of meat. An exciting spiced twist on the traditional pastry.", 100.00, "Pastry"));

        // ===== CAKE =====
        items.add(createFoodMap(17, "Chocolate Fudge Cake", "An indulgent, ultra-moist layered chocolate cake generously frosted with rich chocolate fudge ganache. Decorated with chocolate shavings — a chocoholic dream come true.", 2200.00, "Cake"));
        items.add(createFoodMap(18, "Strawberry Cream Cake", "Light and airy vanilla sponge layers filled and frosted with fresh whipped cream and ripe strawberries. Elegant, refreshing, and perfect for any celebration.", 2100.00, "Cake"));
        items.add(createFoodMap(19, "Vanilla Dream Cake", "A soft, fluffy vanilla chiffon cake with silky smooth vanilla buttercream frosting. Delicate pearl-white decorations make this a dreamy, timeless centerpiece.", 1500.00, "Cake"));
        items.add(createFoodMap(20, "Red Velvet Cake", "The iconic deep red velvet cake with a velvety, tender crumb, layered with luscious cream cheese frosting. A showstopping cake that looks and tastes absolutely stunning.", 3000.00, "Cake"));
        items.add(createFoodMap(21, "Lemon Delight Cake", "A zesty, sunshine-bright lemon sponge cake filled with tangy lemon curd and frosted with light lemon buttercream. Refreshing, vibrant, and perfectly balanced in sweetness.", 2000.00, "Cake"));
        items.add(createFoodMap(22, "Caramel Crunch Cake", "Decadent caramel sponge layers filled with salted caramel cream and topped with crunchy caramelized toffee shards. A sophisticated treat with the perfect contrast of textures.", 2500.00, "Cake"));
        items.add(createFoodMap(23, "Mango Bliss Cake", "Tropical mango mousse layered between moist vanilla sponge, topped with fresh mango coulis and coconut cream. A taste of paradise in every slice.", 1700.00, "Cake"));
        items.add(createFoodMap(24, "Oreo Cream Cake", "Luscious cookies and cream cake featuring Oreo-studded sponge layers, whipped Oreo cream filling, and a dark chocolate ganache drizzle with whole Oreo cookie decorations.", 2000.00, "Cake"));

        // ===== BUN =====
        items.add(createFoodMap(25, "Vegetable Bun", "Soft, pillowy steamed bun filled with a deliciously seasoned mix of fresh vegetables including carrots, cabbage, and green onions. A light and wholesome everyday snack.", 100.00, "Bun"));
        items.add(createFoodMap(26, "Fish Bun", "A freshly baked soft bun generously filled with spicy flaked fish and potato filling, perfectly seasoned with herbs and spices. A flavorful Sri Lankan-style bakery favorite.", 120.00, "Bun"));
        items.add(createFoodMap(27, "Sausage Bun", "A soft, golden-baked bun wrapped around a juicy whole pork sausage with a streak of mustard and ketchup. Classic, quick, and always delicious.", 150.00, "Bun"));
        items.add(createFoodMap(28, "Egg Bun", "A fluffy baked bun filled with a perfectly seasoned spiced egg mixture. Simple, hearty, and full of flavor — a popular breakfast and snack-time staple.", 120.00, "Bun"));
        items.add(createFoodMap(29, "Chocolate Bun", "A soft, enriched sweet bun filled or swirled with rich chocolate filling. Slightly sweet with a tender crumb — an irresistible treat for all chocolate lovers.", 150.00, "Bun"));
        items.add(createFoodMap(30, "Tea Bun", "A classic simple soft bun, lightly sweetened with a golden crust — the perfect companion for your morning or evening cup of tea. Soft, plain, and comforting.", 50.00, "Bun"));
        items.add(createFoodMap(31, "Cream Bun", "A beautifully soft bun split open and filled with a generous dollop of lightly sweetened whipped cream. A bakery classic that never goes out of style.", 80.00, "Bun"));
        items.add(createFoodMap(32, "Hotdog Bun", "A premium soft hotdog roll filled with a juicy grilled sausage, topped with caramelized onions, mustard, ketchup, and relish. Street food satisfaction at its finest.", 250.00, "Bun"));

        // ===== BEVERAGE =====
        items.add(createFoodMap(33, "Iced Coffee", "A bold, refreshing cold brew coffee poured over ice with a splash of milk and your choice of sweetness. Smooth, rich, and energizing — your perfect daily pick-me-up.", 700.00, "Beverage"));
        items.add(createFoodMap(34, "Vanilla Milkshake", "A thick and creamy classic vanilla milkshake blended with premium ice cream and topped with fresh whipped cream. Nostalgic, sweet, and endlessly satisfying.", 650.00, "Beverage"));
        items.add(createFoodMap(35, "Chocolate Milkshake", "Rich, velvety chocolate milkshake made with dark chocolate ice cream, blended to perfection and finished with whipped cream and chocolate syrup drizzle.", 800.00, "Beverage"));
        items.add(createFoodMap(36, "Strawberry Milkshake", "Fresh strawberries blended with creamy strawberry ice cream into a gorgeous pink shake, topped with whipped cream and a fresh strawberry garnish. Sweet, fruity, and fun.", 800.00, "Beverage"));
        items.add(createFoodMap(37, "Bubble Tea", "Creamy, chilled milk tea loaded with perfectly chewy tapioca pearls. Choose from a variety of flavors — taro, matcha, or classic brown sugar. A boba experience like no other.", 1000.00, "Beverage"));
        items.add(createFoodMap(38, "Coca Cola", "Ice-cold refreshing Coca-Cola served over crushed ice. The world's most iconic beverage — perfectly fizzy, sweet, and refreshing. Goes great with any meal.", 400.00, "Beverage"));
        items.add(createFoodMap(39, "Sprite", "Crisp, clean lemon-lime Sprite served ice cold. Refreshingly light and bubbly with a perfect citrus zing — the ideal thirst-quencher on a warm day.", 350.00, "Beverage"));
        items.add(createFoodMap(40, "Cream Soda", "A delightfully sweet and creamy soda with a smooth vanilla-cream flavor. Light pink, effervescent, and nostalgic — a uniquely satisfying soft drink experience.", 350.00, "Beverage"));

        WriteBatch batch = firestore.batch();
        for (Map<String, Object> item : items) {
            DocumentReference docRef = firestore.collection(COLLECTION_FOOD).document();
            batch.set(docRef, item);
        }
        batch.commit()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Successfully seeded all 40 food items to Firestore."))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to seed food items: " + e.getMessage()));
    }

    private Map<String, Object> createFoodMap(int id, String name, String description, double price, String category) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("description", description);
        map.put("price", price);
        map.put("category", category);
        return map;
    }

    /**
     * Retrieves all food items from Firestore.
     */
    public void getAllFoodItems(FoodListCallback callback) {
        firestore.collection(COLLECTION_FOOD)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<FoodModel> list = new ArrayList<>();
                    if (queryDocumentSnapshots.isEmpty()) {
                        seedFoodItems();
                    }
                    int defaultId = 1;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Long idLong = doc.getLong("id");
                        int id = (idLong != null) ? idLong.intValue() : defaultId++;
                        String name = doc.getString("name");
                        String desc = doc.getString("description");
                        Double price = doc.getDouble("price");
                        String cat = doc.getString("category");

                        list.add(new FoodModel(
                                id,
                                name != null ? name : "",
                                desc != null ? desc : "",
                                price != null ? price : 0.0,
                                cat != null ? cat : "",
                                doc.getId()
                        ));
                    }
                    if (callback != null) {
                        callback.onCallback(list);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching food items: " + e.getMessage());
                    if (callback != null) {
                        callback.onCallback(new ArrayList<>());
                    }
                });
    }

    // =========================================================================
    //  USER PROFILE
    // =========================================================================

    /**
     * Save user profile details in Firestore.
     */
    public void saveUserProfile(String username, String email, String phone, String address, String profileImage, ActionCallback callback) {
        String docId = resolveUserDocId(username);
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("email", email);
        userMap.put("phone", phone);
        userMap.put("address", address != null && !address.isEmpty() ? address : "Not Set");
        userMap.put("profile_image", profileImage != null ? profileImage : "");

        firestore.collection(COLLECTION_USERS)
                .document(docId)
                .set(userMap, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user profile: " + e.getMessage());
                    if (callback != null) callback.onComplete(false);
                });
    }

    /**
     * Update user profile details in Firestore.
     */
    public void updateUserProfile(String username, String email, String phone, String address, String profileImage, ActionCallback callback) {
        saveUserProfile(username, email, phone, address, profileImage, callback);
    }

    /**
     * Get user profile details from Firestore.
     */
    public void getUserProfile(String username, UserProfileCallback callback) {
        String docId = resolveUserDocId(username);
        firestore.collection(COLLECTION_USERS)
                .document(docId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String uName = doc.getString("username");
                        String email = doc.getString("email");
                        String phone = doc.getString("phone");
                        String address = doc.getString("address");
                        String profileImage = doc.getString("profile_image");

                        User user = new User(
                                uName != null ? uName : username,
                                email != null ? email : "",
                                phone != null ? phone : "",
                                address != null ? address : "Not Set",
                                profileImage != null ? profileImage : ""
                        );
                        if (callback != null) callback.onCallback(user);
                    } else {
                        // Fall back to FirebaseAuth values if available
                        FirebaseUser fbUser = auth.getCurrentUser();
                        String fallbackEmail = (fbUser != null && fbUser.getEmail() != null) ? fbUser.getEmail() : "";
                        User user = new User(username, fallbackEmail, "", "Not Set", "");
                        if (callback != null) callback.onCallback(user);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user profile: " + e.getMessage());
                    if (callback != null) callback.onCallback(null);
                });
    }

    // =========================================================================
    //  CART OPERATIONS
    // =========================================================================

    /**
     * Add food item to user's cart in Firestore.
     */
    public void addToCart(String username, String foodName, double price, int quantity, ActionCallback callback) {
        String docId = resolveUserDocId(username);
        CollectionReference cartRef = firestore.collection(COLLECTION_USERS)
                .document(docId)
                .collection(SUB_COLLECTION_CART);

        cartRef.whereEqualTo("food_name", foodName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Already in cart: update quantity
                        QueryDocumentSnapshot existingDoc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        Long currentQty = existingDoc.getLong("quantity");
                        long newQty = (currentQty != null ? currentQty : 0) + quantity;

                        existingDoc.getReference().update("quantity", newQty)
                                .addOnSuccessListener(aVoid -> {
                                    if (callback != null) callback.onComplete(true);
                                })
                                .addOnFailureListener(e -> {
                                    if (callback != null) callback.onComplete(false);
                                });
                    } else {
                        // Not in cart: add new document
                        Map<String, Object> cartItem = new HashMap<>();
                        cartItem.put("food_name", foodName);
                        cartItem.put("price", price);
                        cartItem.put("quantity", quantity);

                        cartRef.add(cartItem)
                                .addOnSuccessListener(docRef -> {
                                    if (callback != null) callback.onComplete(true);
                                })
                                .addOnFailureListener(e -> {
                                    if (callback != null) callback.onComplete(false);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking cart item: " + e.getMessage());
                    if (callback != null) callback.onComplete(false);
                });
    }

    /**
     * Fetch all cart items for a user from Firestore.
     */
    public void getCartItems(String username, CartListCallback callback) {
        String docId = resolveUserDocId(username);
        firestore.collection(COLLECTION_USERS)
                .document(docId)
                .collection(SUB_COLLECTION_CART)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CartModel> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String cartDocId = doc.getId();
                        String foodName = doc.getString("food_name");
                        Double price = doc.getDouble("price");
                        Long qtyLong = doc.getLong("quantity");
                        int qty = (qtyLong != null) ? qtyLong.intValue() : 1;

                        list.add(new CartModel(
                                cartDocId,
                                foodName != null ? foodName : "",
                                price != null ? price : 0.0,
                                qty
                        ));
                    }
                    if (callback != null) callback.onCallback(list);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching cart items: " + e.getMessage());
                    if (callback != null) callback.onCallback(new ArrayList<>());
                });
    }

    /**
     * Update quantity of a cart item.
     */
    public void updateCartQuantity(String username, String cartDocId, int newQuantity, ActionCallback callback) {
        String docId = resolveUserDocId(username);
        firestore.collection(COLLECTION_USERS)
                .document(docId)
                .collection(SUB_COLLECTION_CART)
                .document(cartDocId)
                .update("quantity", newQuantity)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating cart quantity: " + e.getMessage());
                    if (callback != null) callback.onComplete(false);
                });
    }

    public void updateCartQuantity(String cartDocId, int newQuantity) {
        updateCartQuantity(null, cartDocId, newQuantity, null);
    }

    /**
     * Remove an item from the cart.
     */
    public void removeFromCart(String username, String cartDocId, ActionCallback callback) {
        String docId = resolveUserDocId(username);
        firestore.collection(COLLECTION_USERS)
                .document(docId)
                .collection(SUB_COLLECTION_CART)
                .document(cartDocId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error removing item from cart: " + e.getMessage());
                    if (callback != null) callback.onComplete(false);
                });
    }

    public void removeFromCart(String cartDocId) {
        removeFromCart(null, cartDocId, null);
    }

    /**
     * Clear all items from user's cart in Firestore.
     */
    public void clearCart(String username, ActionCallback callback) {
        String docId = resolveUserDocId(username);
        CollectionReference cartRef = firestore.collection(COLLECTION_USERS)
                .document(docId)
                .collection(SUB_COLLECTION_CART);

        cartRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                if (callback != null) callback.onComplete(true);
                return;
            }
            WriteBatch batch = firestore.batch();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                batch.delete(doc.getReference());
            }
            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        if (callback != null) callback.onComplete(true);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error clearing cart: " + e.getMessage());
                        if (callback != null) callback.onComplete(false);
                    });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching cart to clear: " + e.getMessage());
            if (callback != null) callback.onComplete(false);
        });
    }

    public void clearCart(String username) {
        clearCart(username, null);
    }

    // =========================================================================
    //  ORDER OPERATIONS
    // =========================================================================

    /**
     * Insert a single order into Firestore.
     */
    public void insertOrder(String username, String foodName, int quantity, double totalPrice, String status, ActionCallback callback) {
        String docId = resolveUserDocId(username);
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> order = new HashMap<>();
        order.put("food_name", foodName);
        order.put("quantity", quantity);
        order.put("total_price", totalPrice);
        order.put("status", status != null ? status : "Completed");
        order.put("order_date", timestamp);

        firestore.collection(COLLECTION_USERS)
                .document(docId)
                .collection(SUB_COLLECTION_ORDERS)
                .add(order)
                .addOnSuccessListener(docRef -> {
                    if (callback != null) callback.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error inserting order: " + e.getMessage());
                    if (callback != null) callback.onComplete(false);
                });
    }

    /**
     * Batch place all orders from cart items, and clear the cart upon success.
     */
    public void placeOrders(String username, List<CartModel> cartItems, double deliveryFee, ActionCallback callback) {
        if (cartItems == null || cartItems.isEmpty()) {
            if (callback != null) callback.onComplete(false);
            return;
        }

        String docId = resolveUserDocId(username);
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        WriteBatch batch = firestore.batch();

        CollectionReference ordersRef = firestore.collection(COLLECTION_USERS)
                .document(docId)
                .collection(SUB_COLLECTION_ORDERS);

        for (CartModel item : cartItems) {
            double itemTotalPrice = (item.getPrice() * item.getQuantity()) + deliveryFee;
            DocumentReference newOrderDoc = ordersRef.document();

            Map<String, Object> order = new HashMap<>();
            order.put("food_name", item.getFoodName());
            order.put("quantity", item.getQuantity());
            order.put("total_price", itemTotalPrice);
            order.put("status", "Completed");
            order.put("order_date", timestamp);

            batch.set(newOrderDoc, order);
        }

        // Commit orders
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    // Clear the cart
                    clearCart(username, clearSuccess -> {
                        if (callback != null) callback.onComplete(true);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to place orders: " + e.getMessage());
                    if (callback != null) callback.onComplete(false);
                });
    }

    /**
     * Fetch user's orders from Firestore ordered by date descending.
     */
    public void getUserOrders(String username, OrderListCallback callback) {
        String docId = resolveUserDocId(username);
        firestore.collection(COLLECTION_USERS)
                .document(docId)
                .collection(SUB_COLLECTION_ORDERS)
                .orderBy("order_date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<OrderModel> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("food_name");
                        String status = doc.getString("status");
                        Double price = doc.getDouble("total_price");
                        Long qtyLong = doc.getLong("quantity");
                        int qty = (qtyLong != null) ? qtyLong.intValue() : 1;
                        String date = doc.getString("order_date");

                        list.add(new OrderModel(
                                name != null ? name : "",
                                status != null ? status : "Completed",
                                price != null ? price : 0.0,
                                qty,
                                date != null ? date : ""
                        ));
                    }
                    if (callback != null) callback.onCallback(list);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user orders: " + e.getMessage());
                    if (callback != null) callback.onCallback(new ArrayList<>());
                });
    }
}