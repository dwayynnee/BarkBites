package com.mycompany.barkbites.data.staff;

import com.google.api.core.ApiFuture;
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
            // 1) Read any root-level orders collection.
            addOrdersFromSnapshot(orders, firestore.collection(StaffDatabaseSchema.ordersCollection()).get());

            // 2) Read all nested orders subcollections via collection group so orders
            //    created under customers/{uid}/orders/{orderId} are visible to staff.
            addOrdersFromSnapshot(orders, firestore.collectionGroup(StaffDatabaseSchema.ordersCollection()).get());

            // 3) Sort newest-first so recent orders appear at the top.
            orders.sort(
                    java.util.Comparator
                            .comparingLong(StaffOrderRecord::createdAtMillis)
                            .reversed()
                            .thenComparing(StaffOrderRecord::id)
            );

            return orders;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load orders from Firestore.", ex);
        }
    }

    private void addOrdersFromSnapshot(List<StaffOrderRecord> target, ApiFuture<QuerySnapshot> future) throws Exception {
        QuerySnapshot snapshot = future.get();
        for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
            target.add(new StaffOrderRecord(
                    document.getId(),
                    firstNonBlank(
                            FirestoreDocuments.readString(document, "customerName", null),
                            FirestoreDocuments.readString(document, "Customer Name", null),
                            FirestoreDocuments.readString(document, "name", null),
                            "Guest"
                    ),
                    firstNonBlank(
                            FirestoreDocuments.readString(document, "status", null),
                            FirestoreDocuments.readString(document, "Status", null),
                            "new"
                    ),
                    firstNonBlank(
                            FirestoreDocuments.readString(document, "payment", null),
                            FirestoreDocuments.readString(document, "Payment", null),
                            "cash"
                    ),
                    firstNonBlank(
                            FirestoreDocuments.readString(document, "Order", null),
                            FirestoreDocuments.readString(document, "order", null),
                            FirestoreDocuments.readString(document, "orderSummary", null),
                            "No items"
                    ),
                    FirestoreDocuments.readLong(document, "totalCents", 0L),
                    firstNonZero(
                            FirestoreDocuments.readLong(document, "createdAtMillis", null),
                            FirestoreDocuments.readLong(document, "createdAt", null),
                            FirestoreDocuments.readLong(document, "timestamp", null),
                            0L
                    )
            ));
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private static long firstNonZero(Long... values) {
        for (Long value : values) {
            if (value != null && value.longValue() != 0L) {
                return value.longValue();
            }
        }
        return 0L;
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
            ApiFuture<QuerySnapshot> future = firestore.collectionGroup(StaffDatabaseSchema.ordersCollection()).get();
            QuerySnapshot snapshot = future.get();
            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                if (orderId.equals(document.getId())) {
                    document.getReference().set(Map.of("status", status), com.google.cloud.firestore.SetOptions.merge()).get();
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to update order status in Firestore.", ex);
        }
    }
}