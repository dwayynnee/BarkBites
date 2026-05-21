package com.mycompany.barkbites.data;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Shared HTTP client for Firebase REST calls.
 */
public final class FirebaseHttp {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private FirebaseHttp() {
    }

    public static HttpClient client() {
        return CLIENT;
    }
}
