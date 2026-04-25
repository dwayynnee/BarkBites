package models;

/**
 * A food product (e.g., sandwich, noodles, pastry).
 */
public final class FoodItem extends Product {
    private boolean vegetarian;

    public FoodItem(String id, String name, int priceCents, int stock, boolean vegetarian) {
        super(id, name, priceCents, stock);
        this.vegetarian = vegetarian;
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public void setVegetarian(boolean vegetarian) {
        this.vegetarian = vegetarian;
    }

    @Override
    public String getTypeLabel() {
        return "Food";
    }

    @Override
    public Product copy() {
        return new FoodItem(getId(), getName(), getPriceCents(), getStock(), vegetarian);
    }
}
