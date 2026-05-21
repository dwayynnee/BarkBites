package com.mycompany.barkbites.data;

import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Public Firebase configuration suitable for client-side use.
 *
 * This supports a "no per-device setup" workflow: the repo contains firebase.properties
 * with the Web API key and project id.
 */
public record FirebasePublicConfig(String projectId, String webApiKey, String studentIdEmailDomain) {

    public FirebasePublicConfig {
        projectId = requireNonBlank(projectId, "projectId");
        webApiKey = requireNonBlank(webApiKey, "webApiKey");
        studentIdEmailDomain = requireNonBlank(studentIdEmailDomain, "studentIdEmailDomain");
    }

    public static FirebasePublicConfig load() {
        // Allow env vars to override for dev, but require no env vars for normal use.
        String projectId = trimToNull(System.getenv("FIREBASE_PROJECT_ID"));
        String apiKey = trimToNull(System.getenv("FIREBASE_WEB_API_KEY"));
        String domain = trimToNull(System.getenv("FIREBASE_STUDENTID_EMAIL_DOMAIN"));

        Properties props = new Properties();
        try (InputStream in = FirebasePublicConfig.class.getClassLoader().getResourceAsStream("firebase.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception ignored) {
        }

        if (projectId == null) {
            projectId = trimToNull(props.getProperty("firebase.projectId"));
        }
        if (apiKey == null) {
            apiKey = trimToNull(props.getProperty("firebase.webApiKey"));
        }
        if (domain == null) {
            domain = trimToNull(props.getProperty("firebase.studentIdEmailDomain"));
        }
        if (domain == null) {
            domain = "barkbites.local";
        }

        return new FirebasePublicConfig(projectId, apiKey, domain);
    }

    public String emailFromStudentId(String studentId) {
        String sid = requireNonBlank(studentId, "studentId");
        // minimal mapping (not an actual mailbox; just to satisfy Firebase email format)
        return sid + "@" + studentIdEmailDomain;
    }

    private static String requireNonBlank(String value, String name) {
        Objects.requireNonNull(value, name);
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalStateException("Missing Firebase config: " + name);
        }
        if ("REPLACE_WITH_YOUR_WEB_API_KEY".equals(trimmed)) {
            throw new IllegalStateException("firebase.webApiKey is still set to placeholder. Update src/main/resources/firebase.properties");
        }
        return trimmed;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
