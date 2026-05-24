package com.mycompany.barkbites.data.staff;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
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
 */
public final class StaffMenuService {

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

    public List<StaffMenuItem> listMenuItems() {
        if (!FirebaseInitializer.isInitialized()) {
            return StaffDemoDataStore.listMenuItems();
        }
        Firestore firestore = FirebaseInitializer.getFirestore();
        List<StaffMenuItem> items = new ArrayList<>();
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(StaffDatabaseSchema.menuCollection()).get();
            QuerySnapshot snapshot = future.get();
            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                items.add(new StaffMenuItem(
                        document.getId(),
                        FirestoreDocuments.readString(document, "name", FirestoreDocuments.readString(document, "title", "Untitled item")),
                        FirestoreDocuments.readLong(document, "priceCents", 0L),
                        FirestoreDocuments.readInteger(document, "quantity", 0),
                        FirestoreDocuments.readString(document, "imagePath", "")
                ));
            }
            return items;
        } catch (Exception ex) {
            return StaffDemoDataStore.listMenuItems();
        }
    }

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