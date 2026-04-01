package data;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import gui.FirebaseRestClient;
import models.Inventory;
import models.MenuItem;
import models.Order;
import models.User;
import models.Wallet;

/**
 * FirebaseManager - Singleton class for managing Firestore operations.
 *
 * This repository uses a local Node.js server (see server.js) as an authenticated
 * API gateway to Firestore via firebase-admin.
 *
 * The original Firebase Admin Java SDK implementation required a large set of
 * external JAR dependencies (firebase-admin + many transitive deps). Those
 * dependencies are not currently present in this repo, so compiling the Java app
 * would fail.
 *
 * To keep the Java Swing app compiling and usable out of the box, this
 * FirebaseManager provides a "REST mode" implementation that delegates supported
 * operations to {@link gui.FirebaseRestClient}. Unsupported operations throw an
 * exception with guidance.
 */
public class FirebaseManager {
    private static FirebaseManager instance;
    private boolean initialized;

    private FirebaseManager() {
    }

    /**
     * Get singleton instance.
     */
    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    /**
     * Initialize Firebase.
     *
     * In REST mode this validates the service account path (if provided) and
     * marks the manager as initialized. Firestore access is performed by the
     * Node.js server.
     */
    public void initializeFirebase(String serviceAccountKeyPath) throws IOException {
        if (serviceAccountKeyPath != null && !serviceAccountKeyPath.trim().isEmpty()) {
            File keyFile = new File(serviceAccountKeyPath);
            if (!keyFile.exists()) {
                throw new IOException("firebase-key.json not found: " + keyFile.getAbsolutePath());
            }
        }

        initialized = true;
        System.out.println("✅ FirebaseManager initialized (REST mode). Start the Node server with: npm start");
    }

    /**
     * Check if Firebase is initialized.
     */
    public boolean isInitialized() {
        return initialized;
    }

    // ==================== USER OPERATIONS ====================

    public User getUserById(String student_id) throws ExecutionException, InterruptedException {
        throw unsupported("getUserById");
    }

    public void saveUser(User user) throws ExecutionException, InterruptedException {
        throw unsupported("saveUser");
    }

    public List<User> getAllUsers() throws ExecutionException, InterruptedException {
        throw unsupported("getAllUsers");
    }

    public void updateLastLogin(String student_id) throws ExecutionException, InterruptedException {
        throw unsupported("updateLastLogin");
    }

    // ==================== MENU ITEM OPERATIONS ====================

    public List<MenuItem> getAllMenuItems() throws ExecutionException, InterruptedException {
        List<Map<String, Object>> raw = FirebaseRestClient.getMenuItems();
        List<MenuItem> items = new ArrayList<>();
        if (raw == null) {
            return items;
        }

        for (Map<String, Object> doc : raw) {
            MenuItem item = new MenuItem();
            item.setId(asString(doc.get("id")));
            item.setName(asString(doc.get("name")));
            item.setDescription(asString(doc.get("description")));
            item.setCategory(asString(doc.get("category")));
            item.setAvailable(asBoolean(doc.get("available"), true));
            item.setPrice(asDouble(doc.get("price"), 0.0));
            items.add(item);
        }

        return items;
    }

    public List<MenuItem> getMenuItemsByCategory(String category) throws ExecutionException, InterruptedException {
        List<MenuItem> all = getAllMenuItems();
        if (category == null || category.trim().isEmpty()) {
            return all;
        }

        List<MenuItem> filtered = new ArrayList<>();
        for (MenuItem item : all) {
            if (category.equalsIgnoreCase(item.getCategory())) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    public MenuItem getMenuItemById(String itemId) throws ExecutionException, InterruptedException {
        if (itemId == null || itemId.trim().isEmpty()) {
            return null;
        }
        for (MenuItem item : getAllMenuItems()) {
            if (itemId.equals(item.getId())) {
                return item;
            }
        }
        return null;
    }

    public String createMenuItem(MenuItem item) throws ExecutionException, InterruptedException {
        if (item == null) {
            return null;
        }

        String id = item.getId();
        if (id == null || id.trim().isEmpty()) {
            id = UUID.randomUUID().toString();
        }

        boolean success = FirebaseRestClient.addMenuItem(
            id,
            nullToEmpty(item.getName()),
            item.getPrice(),
            nullToEmpty(item.getCategory()),
            nullToEmpty(item.getDescription())
        );

        return success ? id : null;
    }

    public void updateMenuItemAvailability(String itemId, boolean available) throws ExecutionException, InterruptedException {
        throw unsupported("updateMenuItemAvailability");
    }

    // ==================== ORDER OPERATIONS ====================

    public List<Order> getAllOrders() throws ExecutionException, InterruptedException {
        List<Map<String, Object>> raw = FirebaseRestClient.getOrders();
        List<Order> orders = new ArrayList<>();
        if (raw == null) {
            return orders;
        }

        for (Map<String, Object> doc : raw) {
            Order order = new Order();
            order.setId(asString(doc.get("id")));
            order.setStudent_id(asString(doc.get("student_id")));
            order.setStatus(asString(doc.get("status")));
            order.setTotal_price(asDouble(doc.get("total_price"), 0.0));
            order.setCreated_at(Instant.now());
            orders.add(order);
        }

        return orders;
    }

    public List<Order> getOrdersByStatus(String status) throws ExecutionException, InterruptedException {
        List<Order> all = getAllOrders();
        if (status == null || status.trim().isEmpty()) {
            return all;
        }

        List<Order> filtered = new ArrayList<>();
        for (Order order : all) {
            if (status.equalsIgnoreCase(order.getStatus())) {
                filtered.add(order);
            }
        }
        return filtered;
    }

    public List<Order> getOrdersByStudent(String student_id) throws ExecutionException, InterruptedException {
        List<Order> all = getAllOrders();
        if (student_id == null || student_id.trim().isEmpty()) {
            return all;
        }

        List<Order> filtered = new ArrayList<>();
        for (Order order : all) {
            if (student_id.equalsIgnoreCase(order.getStudent_id())) {
                filtered.add(order);
            }
        }
        return filtered;
    }

    public Order getOrderById(String orderId) throws ExecutionException, InterruptedException {
        if (orderId == null || orderId.trim().isEmpty()) {
            return null;
        }
        for (Order order : getAllOrders()) {
            if (orderId.equals(order.getId())) {
                return order;
            }
        }
        return null;
    }

    public String createOrder(Order order) throws ExecutionException, InterruptedException {
        throw unsupported("createOrder");
    }

    public void updateOrderStatus(String orderId, String newStatus) throws ExecutionException, InterruptedException {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("orderId is required");
        }
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("newStatus is required");
        }

        boolean success = FirebaseRestClient.updateOrderStatus(orderId, newStatus);
        if (!success) {
            throw new IllegalStateException("Failed to update order status (check server.js is running and Firestore is initialized)");
        }
    }

    public void markOrderPickedUp(String orderId) throws ExecutionException, InterruptedException {
        throw unsupported("markOrderPickedUp");
    }

    public long getPendingOrderCount() throws ExecutionException, InterruptedException {
        long pending = 0;
        for (Order order : getAllOrders()) {
            if ("pending".equalsIgnoreCase(order.getStatus())) {
                pending++;
            }
        }
        return pending;
    }

    // ==================== INVENTORY OPERATIONS ====================

    public Inventory getInventoryByItemId(String menu_item_id) throws ExecutionException, InterruptedException {
        throw unsupported("getInventoryByItemId");
    }

    public List<Inventory> getAllInventory() throws ExecutionException, InterruptedException {
        List<Map<String, Object>> raw = FirebaseRestClient.getInventory();
        List<Inventory> inventory = new ArrayList<>();
        if (raw == null) {
            return inventory;
        }

        for (Map<String, Object> doc : raw) {
            Inventory inv = new Inventory();
            inv.setId(asString(doc.get("id")));
            inv.setMenu_item_id(asString(doc.get("menu_item_id")));
            inv.setQuantity_available((int) asDouble(doc.get("quantity_available"), 0));
            inv.setQuantity_sold_today((int) asDouble(doc.get("quantity_sold_today"), 0));
            inv.setLow_stock_threshold((int) asDouble(doc.get("low_stock_threshold"), 0));
            inv.setOut_of_stock(asBoolean(doc.get("is_out_of_stock"), false));
            inv.setLast_updated(Instant.now());
            inventory.add(inv);
        }

        return inventory;
    }

    public String createInventory(Inventory inventory) throws ExecutionException, InterruptedException {
        throw unsupported("createInventory");
    }

    public void updateInventoryQuantity(String inventoryId, int newQuantity) throws ExecutionException, InterruptedException {
        throw unsupported("updateInventoryQuantity");
    }

    public void deductInventory(String menu_item_id, int quantity) throws ExecutionException, InterruptedException {
        throw unsupported("deductInventory");
    }

    public List<Inventory> getLowStockItems() throws ExecutionException, InterruptedException {
        List<Inventory> lowStock = new ArrayList<>();
        for (Inventory inv : getAllInventory()) {
            if (inv.isLowStock()) {
                lowStock.add(inv);
            }
        }
        return lowStock;
    }

    // ==================== WALLET OPERATIONS ====================

    public Wallet getWalletByStudentId(String student_id) throws ExecutionException, InterruptedException {
        throw unsupported("getWalletByStudentId");
    }

    public void createWallet(Wallet wallet) throws ExecutionException, InterruptedException {
        throw unsupported("createWallet");
    }

    public void deductFromWallet(String student_id, double amount, String order_id) throws ExecutionException, InterruptedException {
        throw unsupported("deductFromWallet");
    }

    public void rechargeWallet(String student_id, double amount) throws ExecutionException, InterruptedException {
        throw unsupported("rechargeWallet");
    }

    public double getDailySpendingByStudent(String student_id) throws ExecutionException, InterruptedException {
        throw unsupported("getDailySpendingByStudent");
    }

    // ==================== LISTENER OPERATIONS ====================

    public void listenToOrders(Runnable listener) {
        throw unsupported("listenToOrders");
    }

    public void listenToOrder(String orderId, Runnable listener) {
        throw unsupported("listenToOrder");
    }

    public void listenToInventory(Runnable listener) {
        throw unsupported("listenToInventory");
    }

    /**
     * Close connection.
     */
    public void close() {
        // No-op in REST mode
    }

    private static UnsupportedOperationException unsupported(String operation) {
        return new UnsupportedOperationException(
            "FirebaseManager." + operation + " is not available in REST mode. " +
            "Use the Node server API (server.js) / FirebaseRestClient instead."
        );
    }

    private static String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static double asDouble(Object value, double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static boolean asBoolean(Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        String s = String.valueOf(value).trim().toLowerCase();
        if ("true".equals(s)) return true;
        if ("false".equals(s)) return false;
        return defaultValue;
    }
}
