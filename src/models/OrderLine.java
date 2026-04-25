package models;

/**
 * A snapshot of a purchased item at checkout time.
 * Stores name + unit price so the order remains accurate even if inventory changes later.
 */
public final class OrderLine {
    private final String productId;
    private final String productName;
    private final int unitPriceCents;
    private final int quantity;

    public OrderLine(String productId, String productName, int unitPriceCents, int quantity) {
        this.productId = Product.requireNonBlank(productId, "productId");
        this.productName = Product.requireNonBlank(productName, "productName");
        if (unitPriceCents < 0) {
            throw new IllegalArgumentException("unitPriceCents must be >= 0");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be >= 1");
        }
        this.unitPriceCents = unitPriceCents;
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getUnitPriceCents() {
        return unitPriceCents;
    }

    public String getUnitPriceDisplay() {
        return Product.formatMoney(unitPriceCents);
    }

    public int getQuantity() {
        return quantity;
    }

    public int getLineTotalCents() {
        return unitPriceCents * quantity;
    }

    public String getLineTotalDisplay() {
        return Product.formatMoney(getLineTotalCents());
    }

    public OrderLine copy() {
        return new OrderLine(productId, productName, unitPriceCents, quantity);
    }
}
