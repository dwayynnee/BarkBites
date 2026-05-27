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
 * 
 * This service provides read and status update capabilities for customer orders.
 * It reads from the Firestore 'orders' collection and allows staff to update order status.
 */
@SuppressWarnings("null")
public final class StaffOrderService {

    /**
     * Retrieves all customer orders from Firestore.
     * 
     * @return List of orders with customer details and status information
     * @throws IllegalStateException if Firebase is not initialized or if Firestore read fails
     */
    public List<StaffOrderRecord> listOrders() {
        if (!FirebaseInitializer.isInitialized()) {
            throw new IllegalStateException("Firebase is not initialized.");
        }
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
                        FirestoreDocuments.readString(document, "payment", "cash"),
                        FirestoreDocuments.readString(
                                document,
                                "order",
                                FirestoreDocuments.readString(document, "orderSummary", "No items")
                        ),
                        FirestoreDocuments.readLong(document, "totalCents", 0L),
                        FirestoreDocuments.readLong(document, "createdAtMillis", 0L)
                ));
            }
            return orders;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load orders from Firestore.", ex);
        }
    }

    /**
     * Updates the status of a customer order in Firestore.
     * Common status values: "new", "ready", "completed", "cancelled"
     * 
     * @param orderId the ID of the order to update
     * @param status the new status value
     * @throws IllegalStateException if Firebase is not initialized or if Firestore update fails
     */
    public void updateOrderStatus(String orderId, String status) {
        if (!FirebaseInitializer.isInitialized()) {
            throw new IllegalStateException("Firebase is not initialized.");
        }
        Firestore firestore = FirebaseInitializer.getFirestore();
        try {
            DocumentReference reference = firestore.collection(StaffDatabaseSchema.ordersCollection()).document(orderId);
            reference.set(Map.of("status", status), com.google.cloud.firestore.SetOptions.merge()).get();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to update order status in Firestore.", ex);
        }
    }
}