package data;

import models.BeverageItem;
import models.FoodItem;

/**
 * Simple composition root for the standalone (no DB/cloud) prototype.
 *
 * The GUIs receive this object and call its manager getters.
 */
public final class BarkBitesSystem {
    private final InventoryManager inventoryManager;
    private final OrderManager orderManager;

    public BarkBitesSystem(InventoryManager inventoryManager, OrderManager orderManager) {
        if (inventoryManager == null) {
            throw new IllegalArgumentException("inventoryManager is required");
        }
        if (orderManager == null) {
            throw new IllegalArgumentException("orderManager is required");
        }
        this.inventoryManager = inventoryManager;
        this.orderManager = orderManager;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public OrderManager getOrderManager() {
        return orderManager;
    }

    /**
     * Mock data populated on launch (replaces DB seed scripts).
     */
    public static BarkBitesSystem createWithMockData() {
        InventoryManager inventory = new InventoryManager(32);
        OrderManager orders = new OrderManager(200);

        // Foods
        inventory.addProduct(new FoodItem("F001", "Chicken Burger", 650, 12, false));
        inventory.addProduct(new FoodItem("F002", "Veggie Wrap", 550, 8, true));
        inventory.addProduct(new FoodItem("F003", "Nuggets (6pc)", 450, 15, false));
        inventory.addProduct(new FoodItem("F004", "Chocolate Muffin", 300, 20, true));

        // Beverages
        inventory.addProduct(new BeverageItem("B001", "Water", 150, 50, 500, true));
        inventory.addProduct(new BeverageItem("B002", "Apple Juice", 250, 18, 300, true));
        inventory.addProduct(new BeverageItem("B003", "Milk", 220, 10, 250, true));

        return new BarkBitesSystem(inventory, orders);
    }
}
