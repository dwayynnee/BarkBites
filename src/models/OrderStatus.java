package models;

/**
 * Order lifecycle for the local (standalone) POS prototype.
 */
public enum OrderStatus {
    PENDING,
    IN_PROGRESS,
    READY,
    COMPLETED,
    CANCELLED
}
