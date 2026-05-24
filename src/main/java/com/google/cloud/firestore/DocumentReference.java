package com.google.cloud.firestore;

import com.google.api.core.ApiFuture;
import java.util.Map;

public class DocumentReference {
    public ApiFuture<DocumentSnapshot> get() { return new SimpleApiFuture<>(); }
    public ApiFuture<Void> set(Map<String,Object> data) { return new SimpleApiFuture<>(); }
}
