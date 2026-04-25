package models;

/**
 * Base class for anything that can be sold in Bark Bites.
 *
 * Key OOP goal: shared state/behavior lives here (id, name, price, stock),
 * while specific product types extend it.
 */
public abstract class Product {
    private final String id;
    private String name;
    private int priceCents;
    private int stock;

    protected Product(String id, String name, int priceCents, int stock) {
        this.id = requireNonBlank(id, "id");
        setName(name);
        setPriceCents(priceCents);
        setStock(stock);
    }

    public final String getId() {
        return id;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = requireNonBlank(name, "name");
    }

    public final int getPriceCents() {
        return priceCents;
    }

    public final void setPriceCents(int priceCents) {
        if (priceCents < 0) {
            throw new IllegalArgumentException("priceCents must be >= 0");
        }
        this.priceCents = priceCents;
    }

    public final int getStock() {
        return stock;
    }

    public final void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("stock must be >= 0");
        }
        this.stock = stock;
    }

    /**
     * Returns a short label the UI can show (polymorphic across subclasses).
     */
    public abstract String getTypeLabel();

    /**
     * Defensive copy used by Manager classes when exposing data to the GUI.
     */
    public abstract Product copy();

    public final String getPriceDisplay() {
        return formatMoney(priceCents);
    }

    public static String formatMoney(int cents) {
        return String.format("$%.2f", cents / 100.0);
    }

    public static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    @Override
    public String toString() {
        return getTypeLabel() + "{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price=" + getPriceDisplay() +
                ", stock=" + stock +
                '}';
    }
}
