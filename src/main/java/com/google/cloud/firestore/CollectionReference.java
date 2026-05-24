package com.google.cloud.firestore;

import com.google.api.core.ApiFuture;

public class CollectionReference {
    public DocumentReference document(String id) { return new DocumentReference(); }
    public ApiFuture<QuerySnapshot> get() { return new SimpleApiFuture<>(); }
}
