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
 * Firestore reader for customer orders visible to staff.
 */
public final class StaffOrderService {

    public List<StaffOrderRecord> listOrders() {
        Firestore firestore = FirebaseInitializer.getFirestore();
        List<StaffOrderRecord> orders = new ArrayList<>();
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(StaffDatabaseSchema.ordersCollection()).get();
            QuerySnapshot snapshot = future.get();
            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                orders.add(new StaffOrderRecord(
                        document.getId(),
                        FirestoreDocuments.readString(document, "customerName", "Guest"),
                        FirestoreDocuments.readString(document, "status", "new"),
                        FirestoreDocuments.readLong(document, "totalCents", 0L),
                        FirestoreDocuments.readLong(document, "createdAtMillis", 0L)
                ));
            }
            return orders;
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage() != null ? ex.getMessage() : "Failed to read orders.", ex);
        }
    }

    public void updateOrderStatus(String orderId, String status) {
        Firestore firestore = FirebaseInitializer.getFirestore();
        try {
            DocumentReference reference = firestore.collection(StaffDatabaseSchema.ordersCollection()).document(orderId);
            reference.set(Map.of("status", status), com.google.cloud.firestore.SetOptions.merge()).get();
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage() != null ? ex.getMessage() : "Failed to update order status.", ex);
        }
    }
}