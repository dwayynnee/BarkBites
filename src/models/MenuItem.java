package models;

import java.time.Instant;

/**
 * Represents a menu item in the BarkBites system
 * Maps to the 'menu_items' Firestore collection
 */
public class MenuItem {
    private String id;              // Document ID in Firestore
    private String name;            // Dish name (e.g., "Chicken Biryani")
    private String description;     // Meal description
    private double price;           // Price in currency units
    private String category;        // Category (e.g., "Main Course", "Dessert")
    private boolean available;      // Whether it's available for ordering
    private String inventory_id;    // Reference to inventory document
    private String image_url;       // URL to dish image
    private Instant created_at;
    private Instant updated_at;

    // Constructor
    public MenuItem() {
    }

    public MenuItem(String name, String description, double price, String category, 
                    String inventory_id) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.inventory_id = inventory_id;
        this.available = true;
        this.created_at = Instant.now();
        this.updated_at = Instant.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getInventory_id() {
        return inventory_id;
    }

    public void setInventory_id(String inventory_id) {
        this.inventory_id = inventory_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public Instant getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Instant created_at) {
        this.created_at = created_at;
    }

    public Instant getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Instant updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", available=" + available +
                '}';
    }
}
