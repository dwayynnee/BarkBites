package data;

import models.CartLine;
import models.Product;

/**
 * CartManager
 *
 * Encapsulation focus:
 * - Cart data is stored in PRIVATE arrays.
 * - GUI can only interact through methods like add/remove/setQuantity.
 */
public final class CartManager {
    private final String[] productIds;
    private final int[] quantities;
    private int size;

    public CartManager(int maxLines) {
        if (maxLines < 1) {
            throw new IllegalArgumentException("maxLines must be >= 1");
        }
        this.productIds = new String[maxLines];
        this.quantities = new int[maxLines];
        this.size = 0;
    }

    public int getLineCount() {
        return size;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            productIds[i] = null;
            quantities[i] = 0;
        }
        size = 0;
    }

    public boolean addOne(String productId) {
        return add(productId, 1);
    }

    public boolean add(String productId, int quantity) {
        String id = Product.requireNonBlank(productId, "productId");
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be >= 1");
        }

        int idx = indexOf(id);
        if (idx != -1) {
            quantities[idx] += quantity;
            return true;
        }

        if (size >= productIds.length) {
            return false;
        }

        productIds[size] = id;
        quantities[size] = quantity;
        size++;
        return true;
    }

    public boolean setQuantity(String productId, int quantity) {
        String id = Product.requireNonBlank(productId, "productId");
        int idx = indexOf(id);
        if (idx == -1) {
            return false;
        }

        if (quantity < 1) {
            remove(id);
            return true;
        }

        quantities[idx] = quantity;
        return true;
    }

    public boolean remove(String productId) {
        String id = Product.requireNonBlank(productId, "productId");
        int idx = indexOf(id);
        if (idx == -1) {
            return false;
        }

        // Shift left to keep the array compact.
        for (int i = idx; i < size - 1; i++) {
            productIds[i] = productIds[i + 1];
            quantities[i] = quantities[i + 1];
        }

        productIds[size - 1] = null;
        quantities[size - 1] = 0;
        size--;
        return true;
    }

    public CartLine[] getLinesSnapshot() {
        CartLine[] out = new CartLine[size];
        for (int i = 0; i < size; i++) {
            out[i] = new CartLine(productIds[i], quantities[i]);
        }
        return out;
    }

    /**
     * Expands the cart into a Product[] (with duplicates), which is perfect for
     * demonstrating polymorphism: the array can contain FoodItem and BeverageItem.
     */
    public Product[] toExpandedProductArraySnapshot(InventoryManager inventory) {
        if (inventory == null) {
            throw new IllegalArgumentException("inventory is required");
        }

        int totalUnits = 0;
        for (int i = 0; i < size; i++) {
            totalUnits += quantities[i];
        }

        Product[] expanded = new Product[totalUnits];
        int pos = 0;

        for (int i = 0; i < size; i++) {
            Product p = inventory.getProductByIdSnapshot(productIds[i]);
            int q = quantities[i];
            for (int k = 0; k < q; k++) {
                if (pos < expanded.length) {
                    expanded[pos] = p; // already a snapshot; safe to reuse
                    pos++;
                }
            }
        }

        return expanded;
    }

    private int indexOf(String productId) {
        for (int i = 0; i < size; i++) {
            if (productId.equals(productIds[i])) {
                return i;
            }
        }
        return -1;
    }
}
