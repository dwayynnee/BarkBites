package models;

/**
 * A single line in the customer's cart.
 *
 * Stored as simple primitives so the cart can remain an in-memory array structure.
 */
public final class CartLine {
    private final String productId;
    private int quantity;

    public CartLine(String productId, int quantity) {
        this.productId = Product.requireNonBlank(productId, "productId");
        setQuantity(quantity);
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be >= 1");
        }
        this.quantity = quantity;
    }

    public CartLine copy() {
        return new CartLine(productId, quantity);
    }
}
