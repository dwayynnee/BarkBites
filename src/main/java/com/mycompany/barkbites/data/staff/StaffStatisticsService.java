package com.mycompany.barkbites.data.staff;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Builds sales summaries from order data for the Staff statistics page.
 */
public final class StaffStatisticsService {

    private final StaffOrderService orderService;

    public StaffStatisticsService() {
        this.orderService = new StaffOrderService();
    }

    public StaffStatisticsSummary loadSummary() {
        List<StaffOrderRecord> orders = orderService.listOrders();
        long totalSalesCents = 0L;
        long monthSalesCents = 0L;
        int monthOrders = 0;
        int totalOrders = 0;

        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        for (StaffOrderRecord order : orders) {
            totalOrders++;
            totalSalesCents += Math.max(0L, order.totalCents());

            if (order.createdAtMillis() > 0L) {
                ZonedDateTime created = Instant.ofEpochMilli(order.createdAtMillis()).atZone(ZoneId.systemDefault());
                if (created.getYear() == currentYear && created.getMonthValue() == currentMonth) {
                    monthOrders++;
                    monthSalesCents += Math.max(0L, order.totalCents());
                }
            }
        }

        return new StaffStatisticsSummary(totalOrders, totalSalesCents, monthOrders, monthSalesCents);
    }
}