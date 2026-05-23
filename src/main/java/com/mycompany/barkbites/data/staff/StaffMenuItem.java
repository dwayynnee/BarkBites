package com.mycompany.barkbites.data.staff;

/**
 * One Firestore menu item row used by the Staff menu editor.
 */
public record StaffMenuItem(String id, String title, String description, long priceCents, String imagePath, boolean active) {
}