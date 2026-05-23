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
 */
public final class StaffInventoryService {

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

    public void upsertInventoryItem(StaffInventoryItem item) {
        if (!FirebaseInitializer.isInitialized()) {
            StaffDemoDataStore.upsertInventoryItem(item);
            return;
        }
        Firestore firestore = FirebaseInitializer.getFirestore();
        try {
            DocumentReference reference = firestore.collection(StaffDatabaseSchema.inventoryCollection()).document(item.id());
            reference.set(Map.of(
                    "name", item.name(),
                    "quantity", item.quantity(),
                    "unit", item.unit(),
                    "imagePath", item.imagePath()
            )).get();
        } catch (Exception ex) {
            StaffDemoDataStore.upsertInventoryItem(item);
        }
    }

    public void deleteInventoryItem(String itemId) {
        if (!FirebaseInitializer.isInitialized()) {
            StaffDemoDataStore.deleteInventoryItem(itemId);
            return;
        }
        Firestore firestore = FirebaseInitializer.getFirestore();
        try {
            firestore.collection(StaffDatabaseSchema.inventoryCollection()).document(itemId).delete().get();
        } catch (Exception ex) {
            StaffDemoDataStore.deleteInventoryItem(itemId);
        }
    }
}