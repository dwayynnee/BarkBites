package com.google.api.core;

public interface ApiFuture<V> {
    V get() throws Exception;
}
