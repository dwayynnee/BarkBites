package com.mycompany.barkbites.data.staff;

/**
 * One Firestore order record used by the Staff orders page.
 */
public record StaffOrderRecord(String id, String customerName, String status, long totalCents, long createdAtMillis) {
}