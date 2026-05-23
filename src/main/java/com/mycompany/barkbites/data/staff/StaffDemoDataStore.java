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
        MENU.put("menu-001", new StaffMenuItem("menu-001", "Chicken Burger", "Grilled chicken burger", 12900L, "", true));
        MENU.put("menu-002", new StaffMenuItem("menu-002", "Fries", "Crispy potato fries", 5900L, "", true));
        INVENTORY.put("inv-001", new StaffInventoryItem("inv-001", "Chicken Patty", 24, "pcs", ""));
        INVENTORY.put("inv-002", new StaffInventoryItem("inv-002", "Potatoes", 48, "pcs", ""));
        ORDERS.put("ord-001", new StaffOrderRecord("ord-001", "Demo Customer", "processing", 18800L, System.currentTimeMillis()));
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
            ORDERS.put(orderId, new StaffOrderRecord(existing.id(), existing.customerName(), status, existing.totalCents(), existing.createdAtMillis()));
        }
    }
}