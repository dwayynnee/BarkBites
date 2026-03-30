package data;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import models.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * FirebaseManager - Singleton class for managing Firestore operations
 * Handles all database interactions for BarkBites
 * 
 * Usage:
 *   FirebaseManager manager = FirebaseManager.getInstance();
 *   manager.initializeFirebase("path/to/firebase-key.json");
 *   List<Order> orders = manager.getAllOrders();
 */
public class FirebaseManager {
    private static FirebaseManager instance = null;
    private Firestore db = null;
    private static final String USERS_COLLECTION = "users";
    private static final String MENU_ITEMS_COLLECTION = "menu_items";
    private static final String ORDERS_COLLECTION = "orders";
    private static final String INVENTORY_COLLECTION = "inventory";
    private static final String WALLETS_COLLECTION = "wallets";

    // Private constructor for Singleton
    private FirebaseManager() {
    }

    /**
     * Get singleton instance
     */
    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    /**
     * Initialize Firebase with service account key
     * Must be called once at application startup
     * 
     * @param serviceAccountKeyPath Path to firebase-key.json file
     * @throws IOException If key file not found
     */
    public void initializeFirebase(String serviceAccountKeyPath) throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            FileInputStream serviceAccount = new FileInputStream(serviceAccountKeyPath);
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
            this.db = FirestoreClient.getFirestore();
            System.out.println("Firebase initialized successfully!");
        }
    }

    /**
     * Check if Firebase is initialized
     */
    public boolean isInitialized() {
        return db != null;
    }

    // ==================== USER OPERATIONS ====================

    /**
     * Get user by student ID
     */
    public User getUserById(String student_id) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db.collection(USERS_COLLECTION).document(student_id).get().get();
        return doc.exists() ? doc.toObject(User.class) : null;
    }

    /**
     * Create or update user
     */
    public void saveUser(User user) throws ExecutionException, InterruptedException {
        db.collection(USERS_COLLECTION).document(user.getStudent_id()).set(user).get();
        System.out.println("User saved: " + user.getStudent_id());
    }

    /**
     * Get all users (staff only operation)
     */
    public List<User> getAllUsers() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documents = db.collection(USERS_COLLECTION).get().get().getDocuments();
        List<User> users = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            users.add(doc.toObject(User.class));
        }
        return users;
    }

    /**
     * Update last login time
     */
    public void updateLastLogin(String student_id) throws ExecutionException, InterruptedException {
        db.collection(USERS_COLLECTION).document(student_id)
                .update("last_login", Instant.now()).get();
    }

    // ==================== MENU ITEM OPERATIONS ====================

    /**
     * Get all menu items
     */
    public List<MenuItem> getAllMenuItems() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documents = db.collection(MENU_ITEMS_COLLECTION)
                .whereEqualTo("available", true).get().get().getDocuments();
        List<MenuItem> items = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            MenuItem item = doc.toObject(MenuItem.class);
            item.setId(doc.getId());
            items.add(item);
        }
        return items;
    }

    /**
     * Get menu items by category
     */
    public List<MenuItem> getMenuItemsByCategory(String category) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documents = db.collection(MENU_ITEMS_COLLECTION)
                .whereEqualTo("category", category)
                .whereEqualTo("available", true).get().get().getDocuments();
        List<MenuItem> items = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            MenuItem item = doc.toObject(MenuItem.class);
            item.setId(doc.getId());
            items.add(item);
        }
        return items;
    }

    /**
     * Get menu item by ID
     */
    public MenuItem getMenuItemById(String itemId) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db.collection(MENU_ITEMS_COLLECTION).document(itemId).get().get();
        if (doc.exists()) {
            MenuItem item = doc.toObject(MenuItem.class);
            item.setId(doc.getId());
            return item;
        }
        return null;
    }

    /**
     * Create new menu item
     */
    public String createMenuItem(MenuItem item) throws ExecutionException, InterruptedException {
        DocumentReference ref = db.collection(MENU_ITEMS_COLLECTION).add(item).get();
        System.out.println("Menu item created: " + ref.getId());
        return ref.getId();
    }

    /**
     * Update menu item availability
     */
    public void updateMenuItemAvailability(String itemId, boolean available) throws ExecutionException, InterruptedException {
        db.collection(MENU_ITEMS_COLLECTION).document(itemId)
                .update("available", available, "updated_at", Instant.now()).get();
    }

    // ==================== ORDER OPERATIONS ====================

    /**
     * Get all orders (staff dashboard)
     */
    public List<Order> getAllOrders() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documents = db.collection(ORDERS_COLLECTION)
                .orderBy("created_at", Query.Direction.DESCENDING).get().get().getDocuments();
        List<Order> orders = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Order order = doc.toObject(Order.class);
            order.setId(doc.getId());
            orders.add(order);
        }
        return orders;
    }

    /**
     * Get orders by status (for staff - filtering pending/in_progress/ready)
     */
    public List<Order> getOrdersByStatus(String status) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documents = db.collection(ORDERS_COLLECTION)
                .whereEqualTo("status", status)
                .orderBy("created_at", Query.Direction.DESCENDING).get().get().getDocuments();
        List<Order> orders = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Order order = doc.toObject(Order.class);
            order.setId(doc.getId());
            orders.add(order);
        }
        return orders;
    }

    /**
     * Get orders for a specific student
     */
    public List<Order> getOrdersByStudent(String student_id) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documents = db.collection(ORDERS_COLLECTION)
                .whereEqualTo("student_id", student_id)
                .orderBy("created_at", Query.Direction.DESCENDING).get().get().getDocuments();
        List<Order> orders = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Order order = doc.toObject(Order.class);
            order.setId(doc.getId());
            orders.add(order);
        }
        return orders;
    }

    /**
     * Get specific order by ID
     */
    public Order getOrderById(String orderId) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db.collection(ORDERS_COLLECTION).document(orderId).get().get();
        if (doc.exists()) {
            Order order = doc.toObject(Order.class);
            order.setId(doc.getId());
            return order;
        }
        return null;
    }

    /**
     * Create new order
     */
    public String createOrder(Order order) throws ExecutionException, InterruptedException {
        DocumentReference ref = db.collection(ORDERS_COLLECTION).add(order).get();
        String orderId = ref.getId();
        
        // Generate order number (can be customized)
        String orderNumber = "#" + String.format("%03d", System.currentTimeMillis() % 1000);
        db.collection(ORDERS_COLLECTION).document(orderId)
                .update("order_number", orderNumber).get();
        
        System.out.println("Order created: " + orderId + " (" + orderNumber + ")");
        return orderId;
    }

    /**
     * Update order status (staff operation)
     */
    public void updateOrderStatus(String orderId, String newStatus) throws ExecutionException, InterruptedException {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        updates.put("updated_at", Instant.now());
        
        if ("ready".equalsIgnoreCase(newStatus)) {
            updates.put("ready_at", Instant.now());
        }
        
        db.collection(ORDERS_COLLECTION).document(orderId).update(updates).get();
        System.out.println("Order " + orderId + " status updated to: " + newStatus);
    }

    /**
     * Mark order as picked up
     */
    public void markOrderPickedUp(String orderId) throws ExecutionException, InterruptedException {
        db.collection(ORDERS_COLLECTION).document(orderId)
                .update("picked_up_at", Instant.now(), "status", "completed").get();
    }

    /**
     * Get pending orders count (for staff dashboard)
     */
    public long getPendingOrderCount() throws ExecutionException, InterruptedException {
        return db.collection(ORDERS_COLLECTION)
                .whereEqualTo("status", "pending").get().get().size();
    }

    // ==================== INVENTORY OPERATIONS ====================

    /**
     * Get inventory item
     */
    public Inventory getInventoryByItemId(String menu_item_id) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documents = db.collection(INVENTORY_COLLECTION)
                .whereEqualTo("menu_item_id", menu_item_id).get().get().getDocuments();
        if (!documents.isEmpty()) {
            Inventory inv = documents.get(0).toObject(Inventory.class);
            inv.setId(documents.get(0).getId());
            return inv;
        }
        return null;
    }

    /**
     * Get all inventory
     */
    public List<Inventory> getAllInventory() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documents = db.collection(INVENTORY_COLLECTION).get().get().getDocuments();
        List<Inventory> inventory = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Inventory inv = doc.toObject(Inventory.class);
            inv.setId(doc.getId());
            inventory.add(inv);
        }
        return inventory;
    }

    /**
     * Create inventory item
     */
    public String createInventory(Inventory inventory) throws ExecutionException, InterruptedException {
        DocumentReference ref = db.collection(INVENTORY_COLLECTION).add(inventory).get();
        System.out.println("Inventory item created: " + ref.getId());
        return ref.getId();
    }

    /**
     * Update inventory quantity
     */
    public void updateInventoryQuantity(String inventoryId, int newQuantity) throws ExecutionException, InterruptedException {
        Map<String, Object> updates = new HashMap<>();
        updates.put("quantity_available", newQuantity);
        updates.put("is_out_of_stock", newQuantity <= 0);
        updates.put("last_updated", Instant.now());
        
        db.collection(INVENTORY_COLLECTION).document(inventoryId).update(updates).get();
    }

    /**
     * Deduct from inventory when order is placed
     */
    public void deductInventory(String menu_item_id, int quantity) throws ExecutionException, InterruptedException {
        Inventory inv = getInventoryByItemId(menu_item_id);
        if (inv != null) {
            int newQuantity = inv.getQuantity_available() - quantity;
            int newSold = inv.getQuantity_sold_today() + quantity;
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("quantity_available", Math.max(0, newQuantity));
            updates.put("quantity_sold_today", newSold);
            updates.put("is_out_of_stock", newQuantity <= 0);
            updates.put("last_updated", Instant.now());
            
            db.collection(INVENTORY_COLLECTION).document(inv.getId()).update(updates).get();
        }
    }

    /**
     * Get low stock items (for staff alerts)
     */
    public List<Inventory> getLowStockItems() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documents = db.collection(INVENTORY_COLLECTION).get().get().getDocuments();
        List<Inventory> lowStockItems = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Inventory inv = doc.toObject(Inventory.class);
            inv.setId(doc.getId());
            if (inv.isLowStock()) {
                lowStockItems.add(inv);
            }
        }
        return lowStockItems;
    }

    // ==================== WALLET OPERATIONS ====================

    /**
     * Get wallet for student
     */
    public Wallet getWalletByStudentId(String student_id) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db.collection(WALLETS_COLLECTION).document(student_id).get().get();
        return doc.exists() ? doc.toObject(Wallet.class) : null;
    }

    /**
     * Create new wallet
     */
    public void createWallet(Wallet wallet) throws ExecutionException, InterruptedException {
        db.collection(WALLETS_COLLECTION).document(wallet.getStudent_id()).set(wallet).get();
        System.out.println("Wallet created for: " + wallet.getStudent_id());
    }

    /**
     * Deduct amount from wallet when order is placed
     */
    public void deductFromWallet(String student_id, double amount, String order_id) throws ExecutionException, InterruptedException {
        Wallet wallet = getWalletByStudentId(student_id);
        if (wallet != null && wallet.hasSufficientBalance(amount)) {
            double newBalance = wallet.getBalance() - amount;
            double newTotalSpent = wallet.getTotal_spent() + amount;
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("balance", newBalance);
            updates.put("total_spent", newTotalSpent);
            updates.put("last_transaction", Instant.now());
            
            db.collection(WALLETS_COLLECTION).document(student_id).update(updates).get();
            System.out.println("Deducted $" + amount + " from wallet: " + student_id);
        }
    }

    /**
     * Recharge wallet (admin operation)
     */
    public void rechargeWallet(String student_id, double amount) throws ExecutionException, InterruptedException {
        Wallet wallet = getWalletByStudentId(student_id);
        if (wallet != null) {
            double newBalance = wallet.getBalance() + amount;
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("balance", newBalance);
            updates.put("last_transaction", Instant.now());
            
            db.collection(WALLETS_COLLECTION).document(student_id).update(updates).get();
            System.out.println("Recharged $" + amount + " to wallet: " + student_id);
        }
    }

    /**
     * Get student spending for today
     */
    public double getDailySpendingByStudent(String student_id) throws ExecutionException, InterruptedException {
        List<Order> orders = getOrdersByStudent(student_id);
        Instant today = Instant.now().minusSeconds(24 * 60 * 60); // Last 24 hours
        
        double totalSpent = 0;
        for (Order order : orders) {
            if (order.getCreated_at().isAfter(today) && "completed".equalsIgnoreCase(order.getStatus())) {
                totalSpent += order.getTotal_price();
            }
        }
        return totalSpent;
    }

    // ==================== LISTENER OPERATIONS ====================

    /**
     * Set up real-time listener for orders (staff dashboard)
     * Calls callback whenever orders change
     */
    public ListenerRegistration listenToOrders(
            com.google.cloud.firestore.EventListener<QuerySnapshot> listener) {
        return db.collection(ORDERS_COLLECTION)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }

    /**
     * Set up real-time listener for specific order (student tracking)
     */
    public ListenerRegistration listenToOrder(String orderId,
            com.google.cloud.firestore.EventListener<DocumentSnapshot> listener) {
        return db.collection(ORDERS_COLLECTION).document(orderId)
                .addSnapshotListener(listener);
    }

    /**
     * Set up real-time listener for inventory
     */
    public ListenerRegistration listenToInventory(
            com.google.cloud.firestore.EventListener<QuerySnapshot> listener) {
        return db.collection(INVENTORY_COLLECTION).addSnapshotListener(listener);
    }

    /**
     * Close Firestore connection
     */
    public void close() throws Exception {
        if (db != null) {
            db.close();
            System.out.println("Firestore connection closed");
        }
    }
}
