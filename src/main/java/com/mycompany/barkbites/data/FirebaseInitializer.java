package com.mycompany.barkbites.data;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Initializes Firebase Admin SDK once per JVM.
 *
 * Encapsulation note (OOP pillar): all Firebase initialization details are hidden here.
 */
public final class FirebaseInitializer {

    public record FirebaseInitResult(boolean success, String warningMessage, String errorMessage) {
        public static FirebaseInitResult success(String warningMessage) {
            return new FirebaseInitResult(true, warningMessage, null);
        }

        public static FirebaseInitResult failure(String errorMessage) {
            return new FirebaseInitResult(false, null, errorMessage);
        }
    }

    private static volatile Firestore firestore;
    private static volatile boolean attempted;

    private FirebaseInitializer() {
    }

    /**
     * Attempts to initialize Firebase from environment variables.
     *
     * @return a result containing success + optional warning, or an error.
     */
    public static synchronized FirebaseInitResult initializeFromEnvironment() {
        try {
            return initialize(FirebaseConfig.fromEnvironment());
        } catch (Exception ex) {
            firestore = null;
            return FirebaseInitResult.failure(prettyMessage(ex));
        }
    }

    /**
     * Attempts to initialize Firebase from an explicit config.
     * This is useful for desktop apps that prompt the user for a local JSON path.
     */
    public static synchronized FirebaseInitResult initialize(FirebaseConfig config) {
        if (attempted) {
            return firestore != null
                    ? FirebaseInitResult.success(null)
                    : FirebaseInitResult.failure("Firebase initialization already attempted and failed.");
        }
        attempted = true;

        try {
            // Guard against double-init if other code initializes Firebase.
            if (FirebaseApp.getApps().isEmpty()) {
                try (InputStream in = Files.newInputStream(config.serviceAccountPath())) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(in))
                            .build();
                    FirebaseApp.initializeApp(options);
                }
            }

            firestore = FirestoreClient.getFirestore();

            String warning = null;
            if (!config.hasWebApiKey()) {
                warning = "FIREBASE_WEB_API_KEY is not set. Firestore will work, but Firebase Auth (signup/login) via REST will not work until you set it.";
            }

            return FirebaseInitResult.success(warning);
        } catch (Exception ex) {
            firestore = null;
            return FirebaseInitResult.failure(prettyMessage(ex));
        }
    }

    public static boolean isInitialized() {
        return firestore != null;
    }

    public static Firestore getFirestore() {
        Firestore db = firestore;
        if (db == null) {
            throw new IllegalStateException("Firebase is not initialized. Call FirebaseInitializer.initializeFromEnvironment() first.");
        }
        return db;
    }

    private static String prettyMessage(Exception ex) {
        String message = ex.getMessage();
        if (message != null && !message.isBlank()) {
            return message;
        }
        if (ex instanceof IOException) {
            return "I/O error while reading Firebase credentials.";
        }
        return ex.getClass().getSimpleName();
    }
}
