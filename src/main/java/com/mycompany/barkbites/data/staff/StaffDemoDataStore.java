package com.mycompany.barkbites.data.staff;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Local in-memory fallback data for Staff screens when Firestore is not configured.
 */
public final class StaffDemoDataStore {

    private static final Map<String, StaffMenuItem> MENU = new LinkedHashMap<>();
    private static final Map<String, StaffInventoryItem> INVENTORY = new LinkedHashMap<>();
    private static final Map<String, StaffOrderRecord> ORDERS = new LinkedHashMap<>();

    static {
        MENU.put("menu-001", new StaffMenuItem("menu-001", "Pancake", 4999L, 20, "pancake.png"));
        MENU.put("menu-002", new StaffMenuItem("menu-002", "Shanghai", 6999L, 10, "shanghai.png"));
        MENU.put("menu-003", new StaffMenuItem("menu-003", "Nilaga", 10900L, 15, "nilaga.png"));
        MENU.put("menu-004", new StaffMenuItem("menu-004", "Bicol Express", 8900L, 10, "bicolex.png"));
        INVENTORY.put("inv-001", new StaffInventoryItem("inv-001", "Chicken Patty", 24, "pcs", ""));
        INVENTORY.put("inv-002", new StaffInventoryItem("inv-002", "Potatoes", 48, "pcs", ""));
        long base = System.currentTimeMillis() - 300_000L;
        ORDERS.put("ord-001", new StaffOrderRecord("ord-001", "Alex", "new", "cash", "2x Chicken Burger", 25800L, base + 10_000L));
        ORDERS.put("ord-002", new StaffOrderRecord("ord-002", "Bianca", "processing", "gcash", "1x Fries, 1x Coke", 10900L, base + 20_000L));
        ORDERS.put("ord-003", new StaffOrderRecord("ord-003", "Chris", "ready", "cash", "1x Pasta", 14900L, base + 30_000L));
        ORDERS.put("ord-004", new StaffOrderRecord("ord-004", "Diane", "completed", "card", "2x Burger Set", 39800L, base + 40_000L));
        ORDERS.put("ord-005", new StaffOrderRecord("ord-005", "Eli", "new", "gcash", "1x Nuggets", 9900L, base + 50_000L));
    }

    private StaffDemoDataStore() {
    }

    public static synchronized List<StaffMenuItem> listMenuItems() {
        return new ArrayList<>(MENU.values());
    }

    public static synchronized void upsertMenuItem(StaffMenuItem item) {
        MENU.put(item.id(), item);
    }

    public static synchronized void deleteMenuItem(String itemId) {
        MENU.remove(itemId);
    }

    public static synchronized List<StaffInventoryItem> listInventoryItems() {
        return new ArrayList<>(INVENTORY.values());
    }

    public static synchronized void upsertInventoryItem(StaffInventoryItem item) {
        INVENTORY.put(item.id(), item);
    }

    public static synchronized void deleteInventoryItem(String itemId) {
        INVENTORY.remove(itemId);
    }

    public static synchronized List<StaffOrderRecord> listOrders() {
        return new ArrayList<>(ORDERS.values());
    }

    public static synchronized void updateOrderStatus(String orderId, String status) {
        StaffOrderRecord existing = ORDERS.get(orderId);
        if (existing != null) {
            ORDERS.put(orderId, new StaffOrderRecord(
                    existing.id(),
                    existing.customerName(),
                    status,
                    existing.payment(),
                    existing.order(),
                    existing.totalCents(),
                    existing.createdAtMillis()
            ));
        }
    }
}