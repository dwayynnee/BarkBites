package models;

import java.time.Instant;

/**
 * A local in-memory order for the standalone POS prototype.
 *
 * Uses arrays (not Lists/Maps) so it can replace database tables for the assignment.
 */
public final class PosOrder {
    private final String id;
    private final String orderNumber;
    private final String studentId;
    private final OrderLine[] lines;
    private final int totalCents;
    private final Instant createdAt;
    private OrderStatus status;

    public PosOrder(String id, String orderNumber, String studentId, OrderLine[] lines, int totalCents) {
        this(id, orderNumber, studentId, lines, totalCents, Instant.now(), OrderStatus.PENDING);
    }

    private PosOrder(String id, String orderNumber, String studentId, OrderLine[] lines, int totalCents, Instant createdAt, OrderStatus status) {
        this.id = Product.requireNonBlank(id, "id");
        this.orderNumber = Product.requireNonBlank(orderNumber, "orderNumber");
        this.studentId = Product.requireNonBlank(studentId, "studentId");
        if (lines == null) {
            throw new IllegalArgumentException("lines is required");
        }
        this.lines = copyLines(lines);
        if (totalCents < 0) {
            throw new IllegalArgumentException("totalCents must be >= 0");
        }
        this.totalCents = totalCents;
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt is required");
        }
        this.createdAt = createdAt;
        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getStudentId() {
        return studentId;
    }

    public OrderLine[] getLinesSnapshot() {
        return copyLines(lines);
    }

    public int getTotalCents() {
        return totalCents;
    }

    public String getTotalDisplay() {
        return Product.formatMoney(totalCents);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }
        this.status = status;
    }

    public PosOrder copy() {
        return new PosOrder(id, orderNumber, studentId, lines, totalCents, createdAt, status);
    }

    private static OrderLine[] copyLines(OrderLine[] src) {
        OrderLine[] out = new OrderLine[src.length];
        for (int i = 0; i < src.length; i++) {
            out[i] = (src[i] == null) ? null : src[i].copy();
        }
        return out;
    }
}
