package com.mycompany.barkbites.data.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.barkbites.data.FirebaseHttp;
import com.mycompany.barkbites.data.FirebasePublicConfig;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Firebase Authentication via REST API (Email/Password).
 *
 * Abstraction note (OOP pillar): UI calls this service; UI does not do HTTP.
 */
public final class FirebaseAuthRestService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final FirebasePublicConfig config;

    public FirebaseAuthRestService(FirebasePublicConfig config) {
        this.config = config;
    }

    public AuthSession signUp(String studentId, String password) {
        String email = config.emailFromStudentId(studentId);
        return callAuthEndpoint("signUp", email, password);
    }

    public AuthSession signIn(String studentId, String password) {
        String email = config.emailFromStudentId(studentId);
        return callAuthEndpoint("signInWithPassword", email, password);
    }

    private AuthSession callAuthEndpoint(String method, String email, String password) {
        try {
            String url = "https://identitytoolkit.googleapis.com/v1/accounts:" + method + "?key=" + config.webApiKey();
            String body = "{\"email\":" + jsonString(email) + ",\"password\":" + jsonString(password) + ",\"returnSecureToken\":true}";

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = FirebaseHttp.client().send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                JsonNode json = MAPPER.readTree(resp.body());
                String uid = text(json, "localId");
                String idToken = text(json, "idToken");
                if (uid == null || idToken == null) {
                    throw new IllegalStateException("Unexpected Auth response.");
                }
                return new AuthSession(uid, idToken);
            }

            // Try to extract Firebase error message
            String message = extractFirebaseError(resp.body());
            throw new IllegalStateException(message != null ? message : ("Auth failed (HTTP " + resp.statusCode() + ")"));
        } catch (Exception ex) {
            String msg = ex.getMessage();
            throw new IllegalStateException(msg != null ? msg : "Auth failed.");
        }
    }

    private static String extractFirebaseError(String body) {
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

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && value.isTextual() ? value.asText() : null;
    }

    private static String jsonString(String value) {
        if (value == null) {
            return "null";
        }
        // Minimal JSON string escape
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
