package com.mycompany.barkbites.data.staff;

/**
 * Editable Firestore collection and document names used by the Staff side.
 *
 * Keep the names centralized here so they can be renamed later without
 * touching every screen or service.
 */
public final class StaffDatabaseSchema {

    private static volatile String menuCollection = "menu";
    private static volatile String inventoryCollection = "inventory";
    private static volatile String ordersCollection = "orders";
    private static volatile String statisticsCollection = "statistics";
    private static volatile String settingsCollection = "settings";
    private static volatile String staffPasswordDocument = "staffPassword";

    private StaffDatabaseSchema() {
    }

    public static String menuCollection() {
        return menuCollection;
    }

    public static void setMenuCollection(String collection) {
        menuCollection = normalize(collection, "menu");
    }

    public static String inventoryCollection() {
        return inventoryCollection;
    }

    public static void setInventoryCollection(String collection) {
        inventoryCollection = normalize(collection, "inventory");
    }

    public static String ordersCollection() {
        return ordersCollection;
    }

    public static void setOrdersCollection(String collection) {
        ordersCollection = normalize(collection, "orders");
    }

    public static String statisticsCollection() {
        return statisticsCollection;
    }

    public static void setStatisticsCollection(String collection) {
        statisticsCollection = normalize(collection, "statistics");
    }

    public static String settingsCollection() {
        return settingsCollection;
    }

    public static void setSettingsCollection(String collection) {
        settingsCollection = normalize(collection, "settings");
    }

    public static String staffPasswordDocument() {
        return staffPasswordDocument;
    }

    public static void setStaffPasswordDocument(String documentId) {
        staffPasswordDocument = normalize(documentId, "staffPassword");
    }

    private static String normalize(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }
}