package models;

/**
 * A beverage product (e.g., water, juice, milk).
 */
public final class BeverageItem extends Product {
    private int volumeMl;
    private boolean servedCold;

    public BeverageItem(String id, String name, int priceCents, int stock, int volumeMl, boolean servedCold) {
        super(id, name, priceCents, stock);
        setVolumeMl(volumeMl);
        this.servedCold = servedCold;
    }

    public int getVolumeMl() {
        return volumeMl;
    }

    public void setVolumeMl(int volumeMl) {
        if (volumeMl <= 0) {
            throw new IllegalArgumentException("volumeMl must be > 0");
        }
        this.volumeMl = volumeMl;
    }

    public boolean isServedCold() {
        return servedCold;
    }

    public void setServedCold(boolean servedCold) {
        this.servedCold = servedCold;
    }

    @Override
    public String getTypeLabel() {
        return "Beverage";
    }

    @Override
    public Product copy() {
        return new BeverageItem(getId(), getName(), getPriceCents(), getStock(), volumeMl, servedCold);
    }
}
