package data;

import models.CartLine;
import models.OrderLine;
import models.OrderStatus;
import models.PosOrder;
import models.Product;

/**
 * OrderManager
 *
 * Abstraction focus:
 * - Checkout logic (array traversal, stock validation/deduction, totals) lives here.
 * - GUI only calls placeOrder(...) and refreshes its tables/labels.
 */
public final class OrderManager {
    private final PosOrder[] orders;
    private int size;
    private int nextOrderNumber;

    public OrderManager(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be >= 1");
        }
        this.orders = new PosOrder[capacity];
        this.size = 0;
        this.nextOrderNumber = 1;
    }

    public int getSize() {
        return size;
    }

    public PosOrder[] getAllOrdersSnapshot() {
        PosOrder[] out = new PosOrder[size];
        for (int i = 0; i < size; i++) {
            out[i] = orders[i].copy();
        }
        return out;
    }

    public boolean updateStatus(String orderId, OrderStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("newStatus is required");
        }
        int idx = indexOf(orderId);
        if (idx == -1) {
            return false;
        }
        orders[idx].setStatus(newStatus);
        return true;
    }

    /**
     * Places an order using in-memory arrays only.
     */
    public PosOrder placeOrder(String studentId, CartManager cart, InventoryManager inventory) {
        String sid = Product.requireNonBlank(studentId, "studentId");
        if (cart == null) {
            throw new IllegalArgumentException("cart is required");
        }
        if (inventory == null) {
            throw new IllegalArgumentException("inventory is required");
        }
        if (size >= orders.length) {
            throw new IllegalStateException("Order capacity reached");
        }

        CartLine[] cartLines = cart.getLinesSnapshot();
        if (cartLines.length == 0) {
            throw new IllegalStateException("Cart is empty");
        }

        // 1) Validate stock BEFORE changing anything.
        for (CartLine line : cartLines) {
            if (line == null) continue;
            if (!inventory.hasStock(line.getProductId(), line.getQuantity())) {
                Product p = inventory.getProductByIdSnapshot(line.getProductId());
                String name = (p == null) ? line.getProductId() : p.getName();
                throw new IllegalStateException("Insufficient stock for: " + name);
            }
        }

        // 2) Polymorphism demo: total is calculated from a Product[] containing mixed subclasses.
        Product[] expanded = cart.toExpandedProductArraySnapshot(inventory);
        int totalCents = calculateCartTotalCents(expanded);

        // 3) Deduct stock (business rule hidden from GUI).
        for (CartLine line : cartLines) {
            if (line == null) continue;
            boolean ok = inventory.adjustStock(line.getProductId(), -line.getQuantity());
            if (!ok) {
                // Should never happen due to earlier validation, but keep it safe.
                throw new IllegalStateException("Stock deduction failed for productId=" + line.getProductId());
            }
        }

        // 4) Convert cart lines -> order lines snapshot.
        OrderLine[] orderLines = new OrderLine[cartLines.length];
        for (int i = 0; i < cartLines.length; i++) {
            CartLine line = cartLines[i];
            if (line == null) continue;

            Product p = inventory.getProductByIdSnapshot(line.getProductId());
            if (p == null) {
                orderLines[i] = new OrderLine(line.getProductId(), "(Unknown)", 0, line.getQuantity());
            } else {
                orderLines[i] = new OrderLine(p.getId(), p.getName(), p.getPriceCents(), line.getQuantity());
            }
        }

        String id = "ORD-" + System.currentTimeMillis() + "-" + size;
        String orderNumber = String.format("#%03d", nextOrderNumber++);
        PosOrder order = new PosOrder(id, orderNumber, sid, orderLines, totalCents);

        orders[size] = order;
        size++;

        cart.clear();
        return order.copy();
    }

    /**
     * Polymorphism: accepts base type Product, works for FoodItem and BeverageItem.
     */
    public static int calculateCartTotalCents(Product[] cartItems) {
        int total = 0;
        if (cartItems == null) {
            return total;
        }
        for (Product p : cartItems) {
            if (p == null) continue;
            total += p.getPriceCents();
        }
        return total;
    }

    private int indexOf(String orderId) {
        if (orderId == null) {
            return -1;
        }
        for (int i = 0; i < size; i++) {
            if (orderId.equals(orders[i].getId())) {
                return i;
            }
        }
        return -1;
    }
}
