package com.mycompany.barkbites.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Loads Firebase configuration from environment variables (Option A).
 *
 * Required:
 * - FIREBASE_SERVICE_ACCOUNT_PATH (or GOOGLE_APPLICATION_CREDENTIALS)
 *
 * Optional (used later for Firebase Auth REST):
 * - FIREBASE_WEB_API_KEY
 */
public record FirebaseConfig(Path serviceAccountPath, String webApiKey) {

    public FirebaseConfig {
        Objects.requireNonNull(serviceAccountPath, "serviceAccountPath");
    }

    public static FirebaseConfig fromEnvironment() {
        String serviceAccount = firstNonBlank(
                System.getenv("FIREBASE_SERVICE_ACCOUNT_PATH"),
                System.getenv("GOOGLE_APPLICATION_CREDENTIALS")
        );

        if (serviceAccount == null) {
            throw new IllegalStateException(
                    "Missing Firebase service account path. Set env var FIREBASE_SERVICE_ACCOUNT_PATH (recommended) or GOOGLE_APPLICATION_CREDENTIALS."
            );
        }

        Path serviceAccountPath = Path.of(serviceAccount).toAbsolutePath().normalize();
        if (!Files.isRegularFile(serviceAccountPath)) {
            throw new IllegalStateException(
                    "Firebase service account JSON not found at: " + serviceAccountPath
            );
        }

        String apiKey = trimToNull(System.getenv("FIREBASE_WEB_API_KEY"));
        return new FirebaseConfig(serviceAccountPath, apiKey);
    }

    public boolean hasWebApiKey() {
        return trimToNull(webApiKey) != null;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String trimmed = trimToNull(value);
            if (trimmed != null) {
                return trimmed;
            }
        }
        return null;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
