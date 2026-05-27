package com.mycompany.barkbites.data.staff;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.mycompany.barkbites.data.FirebaseInitializer;
import com.mycompany.barkbites.data.firestore.FirestoreDocuments;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Firestore CRUD for staff-managed menu items.
 * 
 * This service provides the interface between the staff menu management UI and Firestore.
 * When Firebase is not initialized, it falls back to local demo data for offline testing.
 * 
 * Key features:
 * - Create/Read/Update/Delete menu items in Firestore
 * - Automatic fallback to demo data if Firestore unavailable
 * - Seed default menu items on first use
 */
@SuppressWarnings("null")
public final class StaffMenuService {

    /**
     * Seeds default menu items into Firestore if the collection is empty.
     * Useful for initial setup. Silently fails if Firebase is not available.
     */
    public void seedDefaultMenuItemsIfMissing() {
        if (!FirebaseInitializer.isInitialized()) {
            return;
        }

        Firestore firestore = FirebaseInitializer.getFirestore();
        try {
            QuerySnapshot snapshot = firestore.collection(StaffDatabaseSchema.menuCollection()).get().get();
            if (!snapshot.isEmpty()) {
                return;
            }

            firestore.collection(StaffDatabaseSchema.menuCollection()).document("menu-001").set(Map.of(
                    "name", "Pancake",
                    "priceCents", 4999L,
                    "quantity", 20,
                    "imagePath", "pancake.png"
            )).get();
            firestore.collection(StaffDatabaseSchema.menuCollection()).document("menu-002").set(Map.of(
                    "name", "Shanghai",
                    "priceCents", 6999L,
                    "quantity", 10,
                    "imagePath", "shanghai.png"
            )).get();
            firestore.collection(StaffDatabaseSchema.menuCollection()).document("menu-003").set(Map.of(
                    "name", "Nilaga",
                    "priceCents", 10900L,
                    "quantity", 15,
                    "imagePath", "nilaga.png"
            )).get();
            firestore.collection(StaffDatabaseSchema.menuCollection()).document("menu-004").set(Map.of(
                    "name", "Bicol Express",
                    "priceCents", 8900L,
                    "quantity", 10,
                    "imagePath", "bicolex.png"
            )).get();
        } catch (Exception ex) {
            // Fall back to local demo data if Firestore cannot be seeded.
        }
    }

    /**
     * Retrieves all menu items from Firestore.
     * 
     * Pattern: Try Firestore → parse documents → catch exception → fallback to demo data
     * Reads name (or legacy "title" field), priceCents, quantity, and imagePath.
     * 
     * @return List of menu items, or demo data if Firestore is unavailable
     */
    public List<StaffMenuItem> listMenuItems() {
        // If Firebase not initialized, use demo data immediately
        if (!FirebaseInitializer.isInitialized()) {
            return StaffDemoDataStore.listMenuItems();
        }
        
        // Fetch from Firestore
        Firestore firestore = FirebaseInitializer.getFirestore();
        List<StaffMenuItem> items = new ArrayList<>();
        try {
            // Query all documents in the menu collection
            ApiFuture<QuerySnapshot> future = firestore.collection(StaffDatabaseSchema.menuCollection()).get();
            QuerySnapshot snapshot = future.get();
            
            // Convert each document to a StaffMenuItem object
            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                items.add(new StaffMenuItem(
                        document.getId(),
                        // Support both "name" and legacy "title" field names
                        FirestoreDocuments.readString(document, "name", FirestoreDocuments.readString(document, "title", "Untitled item")),
                        FirestoreDocuments.readLong(document, "priceCents", 0L),
                        FirestoreDocuments.readInteger(document, "quantity", 0),
                        FirestoreDocuments.readString(document, "imagePath", "")
                ));
            }
            return items;
        } catch (Exception ex) {
            // Firestore failed - fall back to demo data
            return StaffDemoDataStore.listMenuItems();
        }
    }

    /**
     * Creates or updates a menu item in Firestore.
     * Falls back to in-memory storage if Firestore is unavailable.
     * 
     * @param item the menu item to save
     */
    public void upsertMenuItem(StaffMenuItem item) {
        if (!FirebaseInitializer.isInitialized()) {
            StaffDemoDataStore.upsertMenuItem(item);
            return;
        }
        Firestore firestore = FirebaseInitializer.getFirestore();
        try {
            DocumentReference reference = firestore.collection(StaffDatabaseSchema.menuCollection()).document(item.id());
            reference.set(Map.of(
                    "name", item.name(),
                    "priceCents", item.priceCents(),
                    "quantity", item.quantity(),
                    "imagePath", item.imagePath()
            )).get();
        } catch (Exception ex) {
            StaffDemoDataStore.upsertMenuItem(item);
        }
    }

    /**
     * Deletes a menu item from Firestore by ID.
     * Falls back to in-memory storage if Firestore is unavailable.
     * 
     * @param itemId the ID of the menu item to delete
     */
    public void deleteMenuItem(String itemId) {
        if (!FirebaseInitializer.isInitialized()) {
            StaffDemoDataStore.deleteMenuItem(itemId);
            return;
        }
        Firestore firestore = FirebaseInitializer.getFirestore();
        try {
            firestore.collection(StaffDatabaseSchema.menuCollection()).document(itemId).delete().get();
        } catch (Exception ex) {
            StaffDemoDataStore.deleteMenuItem(itemId);
        }
    }
}