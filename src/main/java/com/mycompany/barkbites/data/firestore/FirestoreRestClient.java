package com.mycompany.barkbites.data.firestore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.barkbites.data.FirebaseHttp;
import com.mycompany.barkbites.data.FirebasePublicConfig;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Minimal Firestore REST client using Firebase Auth idToken for authorization.
 *
 * This avoids service-account JSON so the app can run on other devices
 * with no per-device setup.
 */
public final class FirestoreRestClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final FirebasePublicConfig config;

    public FirestoreRestClient(FirebasePublicConfig config) {
        this.config = config;
    }

    public JsonNode getDocument(String idToken, String collection, String documentId) {
        String url = documentUrl(collection, documentId);
        try {
            return send(idToken, HttpRequest.newBuilder().uri(URI.create(url)).GET().build());
        } catch (IllegalStateException ex) {
            String msg = ex.getMessage();
            if (msg != null) {
                String lower = msg.toLowerCase();
                if (lower.contains("not found") || lower.contains("document") && lower.contains("not found")) {
                    return null;
                }
            }
            throw ex;
        }
    }

    public JsonNode getDocumentAtPath(String idToken, String documentPath) {
        String url = documentUrlFromPath(documentPath);
        try {
            return send(idToken, HttpRequest.newBuilder().uri(URI.create(url)).GET().build());
        } catch (IllegalStateException ex) {
            String msg = ex.getMessage();
            if (msg != null) {
                String lower = msg.toLowerCase();
                if (lower.contains("not found") || lower.contains("document") && lower.contains("not found")) {
                    return null;
                }
            }
            throw ex;
        }
    }

    public JsonNode upsertDocument(String idToken, String collection, String documentId, JsonNode documentBody) {
        String url = documentUrl(collection, documentId);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(documentBody.toString(), StandardCharsets.UTF_8))
                .build();
        return send(idToken, req);
    }

    public JsonNode upsertDocumentAtPath(String idToken, String documentPath, JsonNode documentBody) {
        String url = documentUrlFromPath(documentPath);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(documentBody.toString(), StandardCharsets.UTF_8))
                .build();
        return send(idToken, req);
    }

    public JsonNode listDocuments(String idToken, String collection) {
        String url = collectionUrl(collection);
        return send(idToken, HttpRequest.newBuilder().uri(URI.create(url)).GET().build());
    }

    public JsonNode listDocumentsAtPath(String idToken, String collectionPath) {
        String url = collectionUrlFromPath(collectionPath);
        return send(idToken, HttpRequest.newBuilder().uri(URI.create(url)).GET().build());
    }

    public JsonNode deleteDocument(String idToken, String collection, String documentId) {
        String url = documentUrl(collection, documentId);
        return send(idToken, HttpRequest.newBuilder().uri(URI.create(url)).DELETE().build());
    }

    private JsonNode send(String idToken, HttpRequest request) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(request.uri())
                    .method(request.method(), request.bodyPublisher().orElse(HttpRequest.BodyPublishers.noBody()));

            request.headers().map().forEach((name, values) -> {
                for (String value : values) {
                    builder.header(name, value);
                }
            });

            // Ensure Authorization is present and not duplicated.
            builder.setHeader("Authorization", "Bearer " + idToken);

            HttpRequest authed = builder.build();
            HttpResponse<String> resp = FirebaseHttp.client().send(authed, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                return MAPPER.readTree(resp.body());
            }
            String message = extractError(resp.body());
            throw new IllegalStateException(message != null ? message : ("Firestore request failed (HTTP " + resp.statusCode() + ")"));
        } catch (Exception ex) {
            String msg = ex.getMessage();
            throw new IllegalStateException(msg != null ? msg : "Firestore request failed.");
        }
    }

    private String documentUrl(String collection, String documentId) {
        String c = encodePath(collection);
        String d = encodePath(documentId);
        return "https://firestore.googleapis.com/v1/projects/" + config.projectId() + "/databases/(default)/documents/" + c + "/" + d;
    }

    private String documentUrlFromPath(String documentPath) {
        String[] parts = documentPath.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append("/");
            sb.append(encodeSegment(parts[i]));
        }
        return "https://firestore.googleapis.com/v1/projects/" + config.projectId() + "/databases/(default)/documents/" + sb.toString();
    }

    private String collectionUrl(String collection) {
        String c = encodePath(collection);
        return "https://firestore.googleapis.com/v1/projects/" + config.projectId() + "/databases/(default)/documents/" + c;
    }

    private String collectionUrlFromPath(String collectionPath) {
        String[] parts = collectionPath.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append("/");
            sb.append(encodeSegment(parts[i]));
        }
        return "https://firestore.googleapis.com/v1/projects/" + config.projectId() + "/databases/(default)/documents/" + sb.toString();
    }

    private static String encodePath(String segment) {
        return URLEncoder.encode(segment, StandardCharsets.UTF_8);
    }

    private static String encodeSegment(String segment) {
        // Encode a single path segment so that slashes remain separators
        return URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("%2F", "/");
    }

    private static String extractError(String body) {
        try {
            JsonNode json = MAPPER.readTree(body);
            JsonNode error = json.get("error");
            if (error != null) {
                JsonNode message = error.get("message");
                if (message != null && message.isTextual()) {
                    return message.asText();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
