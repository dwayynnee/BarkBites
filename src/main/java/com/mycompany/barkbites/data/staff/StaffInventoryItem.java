package com.mycompany.barkbites.data.staff;

/**
 * One Firestore inventory row used by the Staff inventory page.
 */
public record StaffInventoryItem(String id, String name, int quantity, String unit, String imagePath) {
}