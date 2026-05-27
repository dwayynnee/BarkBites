package com.mycompany.barkbites.data.staff;

/**
 * Editable Firestore collection and document names used by the Staff side.
 *
 * This centralized configuration allows collection and document names to be
 * renamed later without modifying every screen or service class.
 * 
 * Default structure:
 * - menu: Contains staff menu items (dishes, prices, quantities)
 * - inventory: Contains inventory tracking data
 * - orders: Contains customer orders placed via the app
 * - statistics: Contains sales summary and reporting data
 * - settings: Contains configuration documents (like staffPassword)
 */
public final class StaffDatabaseSchema {

    // Firestore collection names (editable via setters)
    private static volatile String menuCollection = "menu";
    private static volatile String inventoryCollection = "inventory";
    private static volatile String ordersCollection = "orders";
    private static volatile String statisticsCollection = "statistics";
    private static volatile String settingsCollection = "settings";
    private static volatile String staffPasswordDocument = "staffPassword";

    // Private constructor: all access is through static methods
    private StaffDatabaseSchema() {
    }

    // ==================== Menu Collection ====================
    /** @return the current menu collection name */
    public static String menuCollection() {
        return menuCollection;
    }

    /** Set the menu collection name to a custom value */
    public static void setMenuCollection(String collection) {
        menuCollection = normalize(collection, "menu");
    }

    // ==================== Inventory Collection ====================
    /** @return the current inventory collection name */
    public static String inventoryCollection() {
        return inventoryCollection;
    }

    /** Set the inventory collection name to a custom value */
    public static void setInventoryCollection(String collection) {
        inventoryCollection = normalize(collection, "inventory");
    }

    // ==================== Orders Collection ====================
    /** @return the current orders collection name */
    public static String ordersCollection() {
        return ordersCollection;
    }

    /** Set the orders collection name to a custom value */
    public static void setOrdersCollection(String collection) {
        ordersCollection = normalize(collection, "orders");
    }

    // ==================== Statistics Collection ====================
    /** @return the current statistics collection name */
    public static String statisticsCollection() {
        return statisticsCollection;
    }

    /** Set the statistics collection name to a custom value */
    public static void setStatisticsCollection(String collection) {
        statisticsCollection = normalize(collection, "statistics");
    }

    // ==================== Settings Collection ====================
    /** @return the current settings collection name */
    public static String settingsCollection() {
        return settingsCollection;
    }

    /** Set the settings collection name to a custom value */
    public static void setSettingsCollection(String collection) {
        settingsCollection = normalize(collection, "settings");
    }

    // ==================== Staff Password Document ====================
    /** @return the current staff password document ID (within settings collection) */
    public static String staffPasswordDocument() {
        return staffPasswordDocument;
    }

    /** Set the staff password document ID to a custom value */
    public static void setStaffPasswordDocument(String documentId) {
        staffPasswordDocument = normalize(documentId, "staffPassword");
    }

    /**
     * Normalizes collection/document names by trimming whitespace.
     * Returns fallback value if input is null or empty.
     * 
     * @param value the input name to normalize
     * @param fallback the default name if value is invalid
     * @return the normalized name
     */
    private static String normalize(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }
}