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
 * Firestore CRUD for staff-managed inventory rows.
 * 
 * This service manages inventory items including stock levels and related details.
 * Automatically falls back to demo data if Firestore is not configured.
 */
public final class StaffInventoryService {

    /**
     * Retrieves all inventory items from Firestore.
     * 
     * @return List of inventory items, or demo data if Firestore unavailable
     */
    @SuppressWarnings("null")
    public List<StaffInventoryItem> listInventoryItems() {
        if (!FirebaseInitializer.isInitialized()) {
            return StaffDemoDataStore.listInventoryItems();
        }
        Firestore firestore = FirebaseInitializer.getFirestore();
        List<StaffInventoryItem> items = new ArrayList<>();
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(StaffDatabaseSchema.inventoryCollection()).get();
            QuerySnapshot snapshot = future.get();
            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                items.add(new StaffInventoryItem(
                        document.getId(),
                        FirestoreDocuments.readString(document, "name", "Unnamed item"),
                        FirestoreDocuments.readInteger(document, "quantity", 0),
                        FirestoreDocuments.readString(document, "unit", "pcs"),
                        FirestoreDocuments.readString(document, "imagePath", "")
                ));
            }
            return items;
        } catch (Exception ex) {
            return StaffDemoDataStore.listInventoryItems();
        }
    }

    /**
     * Creates or updates an inventory item in Firestore.
     * Falls back to in-memory storage if Firestore is unavailable.
     * 
     * Pattern: Try Firestore → catch exception → fallback to demo data
     * This ensures the app works even when Firebase is down.
     * 
     * @param item the inventory item to save
     */
    @SuppressWarnings("null")
    public void upsertInventoryItem(StaffInventoryItem item) {
        // Check if Firebase is initialized
        if (!FirebaseInitializer.isInitialized()) {
            StaffDemoDataStore.upsertInventoryItem(item);
            return;
        }
        
        // Try to save to Firestore
        Firestore firestore = FirebaseInitializer.getFirestore();
        try {
            // Get reference to the inventory collection and this item's document
            DocumentReference reference = firestore.collection(StaffDatabaseSchema.inventoryCollection()).document(item.id());
            
            // Save the inventory item fields (blocking with .get())
            reference.set(Map.of(
                    "name", item.name(),
                    "quantity", item.quantity(),
                    "unit", item.unit(),
                    "imagePath", item.imagePath()
            )).get();
        } catch (Exception ex) {
            // Firestore failed - fall back to in-memory demo data
            StaffDemoDataStore.upsertInventoryItem(item);
        }
    }

    /**
     * Deletes an inventory item from Firestore by ID.
     * Falls back to in-memory storage if Firestore is unavailable.
     * 
     * @param itemId the ID of the inventory item to delete
     */
    @SuppressWarnings("null")
    public void deleteInventoryItem(String itemId) {
        // Check if Firebase is initialized
        if (!FirebaseInitializer.isInitialized()) {
            StaffDemoDataStore.deleteInventoryItem(itemId);
            return;
        }
        
        // Try to delete from Firestore
        Firestore firestore = FirebaseInitializer.getFirestore();
        try {
            // Delete the document (blocking with .get())
            firestore.collection(StaffDatabaseSchema.inventoryCollection()).document(itemId).delete().get();
        } catch (Exception ex) {
            // Firestore failed - fall back to in-memory demo data
            StaffDemoDataStore.deleteInventoryItem(itemId);
        }
    }
}