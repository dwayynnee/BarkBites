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
        return send(idToken, HttpRequest.newBuilder().uri(URI.create(url)).GET().build());
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

    private JsonNode send(String idToken, HttpRequest request) {
        try {
            HttpRequest authed = HttpRequest.newBuilder(request)
                    .header("Authorization", "Bearer " + idToken)
                    .build();
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

    private static String encodePath(String segment) {
        return URLEncoder.encode(segment, StandardCharsets.UTF_8);
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
