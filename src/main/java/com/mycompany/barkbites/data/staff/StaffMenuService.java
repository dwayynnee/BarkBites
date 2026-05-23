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

    public List<StaffMenuItem> listMenuItems() {
        Firestore firestore = FirebaseInitializer.getFirestore();
        List<StaffMenuItem> items = new ArrayList<>();
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(StaffDatabaseSchema.menuCollection()).get();
            QuerySnapshot snapshot = future.get();
            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                items.add(new StaffMenuItem(
                        document.getId(),
                        FirestoreDocuments.readString(document, "title", "Untitled item"),
                        FirestoreDocuments.readString(document, "description", ""),
                        FirestoreDocuments.readLong(document, "priceCents", 0L),
                        FirestoreDocuments.readString(document, "imagePath", ""),
                        FirestoreDocuments.readBoolean(document, "active", true)
                ));
            }
            return items;
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage() != null ? ex.getMessage() : "Failed to read menu items.", ex);
        }
    }

    public void upsertMenuItem(StaffMenuItem item) {
        Firestore firestore = FirebaseInitializer.getFirestore();
        try {
            DocumentReference reference = firestore.collection(StaffDatabaseSchema.menuCollection()).document(item.id());
            reference.set(Map.of(
                    "title", item.title(),
                    "description", item.description(),
                    "priceCents", item.priceCents(),
                    "imagePath", item.imagePath(),
                    "active", item.active()
            )).get();
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage() != null ? ex.getMessage() : "Failed to save menu item.", ex);
        }
    }

    public void deleteMenuItem(String itemId) {
        Firestore firestore = FirebaseInitializer.getFirestore();
        try {
            firestore.collection(StaffDatabaseSchema.menuCollection()).document(itemId).delete().get();
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage() != null ? ex.getMessage() : "Failed to delete menu item.", ex);
        }
    }
}