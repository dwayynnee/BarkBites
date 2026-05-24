package com.mycompany.barkbites.data.staff;

/**
 * One Firestore menu item row used by the Staff menu editor.
 */
public record StaffMenuItem(String id, String name, long priceCents, int quantity, String imagePath) {
}