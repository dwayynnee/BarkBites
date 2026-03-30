package models;

import java.time.Instant;

/**
 * Represents inventory for a menu item in the BarkBites system
 * Maps to the 'inventory' Firestore collection
 */
public class Inventory {
    private String id;                      // Document ID in Firestore
    private String menu_item_id;            // Reference to menu_items document
    private int quantity_available;         // Current stock count
    private int quantity_sold_today;        // How many sold today
    private int low_stock_threshold;        // Alert when below this count
    private boolean is_out_of_stock;        // Quick flag for UI
    private Instant last_updated;           // When stock was last updated

    // Constructor
    public Inventory() {
    }

    public Inventory(String menu_item_id, int quantity_available, int low_stock_threshold) {
        this.menu_item_id = menu_item_id;
        this.quantity_available = quantity_available;
        this.low_stock_threshold = low_stock_threshold;
        this.quantity_sold_today = 0;
        this.is_out_of_stock = false;
        this.last_updated = Instant.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMenu_item_id() {
        return menu_item_id;
    }

    public void setMenu_item_id(String menu_item_id) {
        this.menu_item_id = menu_item_id;
    }

    public int getQuantity_available() {
        return quantity_available;
    }

    public void setQuantity_available(int quantity_available) {
        this.quantity_available = quantity_available;
    }

    public int getQuantity_sold_today() {
        return quantity_sold_today;
    }

    public void setQuantity_sold_today(int quantity_sold_today) {
        this.quantity_sold_today = quantity_sold_today;
    }

    public int getLow_stock_threshold() {
        return low_stock_threshold;
    }

    public void setLow_stock_threshold(int low_stock_threshold) {
        this.low_stock_threshold = low_stock_threshold;
    }

    public boolean isOut_of_stock() {
        return is_out_of_stock;
    }

    public void setOut_of_stock(boolean out_of_stock) {
        is_out_of_stock = out_of_stock;
    }

    public Instant getLast_updated() {
        return last_updated;
    }

    public void setLast_updated(Instant last_updated) {
        this.last_updated = last_updated;
    }

    /**
     * Check if inventory is low
     */
    public boolean isLowStock() {
        return quantity_available <= low_stock_threshold && !is_out_of_stock;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "id='" + id + '\'' +
                ", menu_item_id='" + menu_item_id + '\'' +
                ", quantity_available=" + quantity_available +
                ", is_out_of_stock=" + is_out_of_stock +
                '}';
    }
}
