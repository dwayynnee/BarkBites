package com.mycompany.barkbites.data.staff;

/**
 * Aggregate sales summary for the Staff statistics page.
 */
public record StaffStatisticsSummary(int totalOrders, long totalSalesCents, int monthOrders, long monthSalesCents) {
}