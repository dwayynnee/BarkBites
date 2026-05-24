package com.google.cloud.firestore;

import com.google.api.core.ApiFuture;

public class SimpleApiFuture<V> implements ApiFuture<V> {
    @Override
    public V get() throws Exception { return null; }
}
