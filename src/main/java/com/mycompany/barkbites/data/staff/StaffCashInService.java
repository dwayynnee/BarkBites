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
 * Firestore helper for staff cash-in transactions.
 */
@SuppressWarnings("null")
public final class StaffCashInService {

    public List<StaffCashInRecord> listCashInRecords() {
        if (!FirebaseInitializer.isInitialized()) {
            return List.of();
        }

        Firestore firestore = FirebaseInitializer.getFirestore();
        List<StaffCashInRecord> records = new ArrayList<>();
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(StaffDatabaseSchema.cashInHistoryCollection()).get();
            QuerySnapshot snapshot = future.get();
            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                records.add(new StaffCashInRecord(
                        document.getId(),
                        FirestoreDocuments.readString(document, "customerName", FirestoreDocuments.readString(document, "name", "Guest")),
                        FirestoreDocuments.readString(document, "studentId", FirestoreDocuments.readString(document, "customerId", "-")),
                        FirestoreDocuments.readLong(document, "amountCents", 0L),
                        FirestoreDocuments.readLong(document, "balanceBeforeCents", 0L),
                        FirestoreDocuments.readLong(document, "balanceAfterCents", 0L),
                        firstNonZero(
                                FirestoreDocuments.readLong(document, "createdAtMillis", null),
                                FirestoreDocuments.readLong(document, "createdAt", null),
                                0L
                        )
                ));
            }
            records.sort(java.util.Comparator
                    .comparingLong(StaffCashInRecord::createdAtMillis)
                    .reversed()
                    .thenComparing(StaffCashInRecord::id));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load cash-in history from Firestore.", ex);
        }
        return records;
    }

    public void recordCashIn(String customerId, String customerName, String studentId, long amountCents, long balanceBeforeCents, long balanceAfterCents) {
        if (!FirebaseInitializer.isInitialized()) {
            return;
        }

        Firestore firestore = FirebaseInitializer.getFirestore();
        try {
            String historyId = "cashin-" + System.currentTimeMillis() + "-" + customerId;
            DocumentReference reference = firestore.collection(StaffDatabaseSchema.cashInHistoryCollection()).document(historyId);
            reference.set(Map.of(
                    "customerId", customerId,
                    "customerName", customerName,
                    "studentId", studentId,
                    "amountCents", amountCents,
                    "balanceBeforeCents", balanceBeforeCents,
                    "balanceAfterCents", balanceAfterCents,
                    "createdAtMillis", System.currentTimeMillis(),
                    "type", "cashIn"
            )).get();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to save cash-in history to Firestore.", ex);
        }
    }

    private static long firstNonZero(Long... values) {
        for (Long value : values) {
            if (value != null && value.longValue() != 0L) {
                return value.longValue();
            }
        }
        return 0L;
    }
}