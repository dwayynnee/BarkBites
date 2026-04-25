package data;

import models.Product;

/**
 * InventoryManager
 *
 * Encapsulation focus:
 * - The underlying Product[] array is PRIVATE.
 * - GUIs can only read/modify inventory through these public methods.
 * - All read methods return DEFENSIVE COPIES, so GUIs can't mutate internal state.
 */
public final class InventoryManager {
    private final Product[] products;
    private int size;

    public InventoryManager(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be >= 1");
        }
        this.products = new Product[capacity];
        this.size = 0;
    }

    public int getSize() {
        return size;
    }

    public int getCapacity() {
        return products.length;
    }

    public boolean addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("product is required");
        }
        if (size >= products.length) {
            return false;
        }
        if (indexOf(product.getId()) != -1) {
            return false; // enforce unique IDs
        }

        products[size] = product.copy();
        size++;
        return true;
    }

    public Product getProductByIdSnapshot(String productId) {
        int idx = indexOf(productId);
        if (idx == -1) {
            return null;
        }
        return products[idx].copy();
    }

    public Product[] getAllProductsSnapshot() {
        Product[] out = new Product[size];
        for (int i = 0; i < size; i++) {
            out[i] = products[i].copy();
        }
        return out;
    }

    /**
     * Example of "backend algorithm hidden from GUI": sorting happens here.
     */
    public Product[] getAllProductsSortedByNameSnapshot() {
        Product[] out = getAllProductsSnapshot();
        bubbleSortByName(out);
        return out;
    }

    public boolean hasStock(String productId, int requiredQuantity) {
        if (requiredQuantity < 0) {
            throw new IllegalArgumentException("requiredQuantity must be >= 0");
        }
        int idx = indexOf(productId);
        if (idx == -1) {
            return false;
        }
        return products[idx].getStock() >= requiredQuantity;
    }

    public boolean setStock(String productId, int newStock) {
        if (newStock < 0) {
            throw new IllegalArgumentException("newStock must be >= 0");
        }
        int idx = indexOf(productId);
        if (idx == -1) {
            return false;
        }
        products[idx].setStock(newStock);
        return true;
    }

    public boolean adjustStock(String productId, int delta) {
        int idx = indexOf(productId);
        if (idx == -1) {
            return false;
        }

        Product p = products[idx];
        int next = p.getStock() + delta;
        if (next < 0) {
            return false;
        }
        p.setStock(next);
        return true;
    }

    private int indexOf(String productId) {
        if (productId == null) {
            return -1;
        }
        for (int i = 0; i < size; i++) {
            if (productId.equals(products[i].getId())) {
                return i;
            }
        }
        return -1;
    }

    private static void bubbleSortByName(Product[] arr) {
        if (arr == null) {
            return;
        }
        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = 0; j < arr.length - 1 - i; j++) {
                Product a = arr[j];
                Product b = arr[j + 1];
                if (a == null || b == null) {
                    continue;
                }
                if (a.getName().compareToIgnoreCase(b.getName()) > 0) {
                    arr[j] = b;
                    arr[j + 1] = a;
                }
            }
        }
    }
}
