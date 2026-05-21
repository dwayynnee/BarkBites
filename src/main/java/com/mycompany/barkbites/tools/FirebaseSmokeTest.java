package com.mycompany.barkbites.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.barkbites.data.FirebasePublicConfig;
import com.mycompany.barkbites.data.auth.AuthSession;
import com.mycompany.barkbites.data.auth.FirebaseAuthRestService;
import com.mycompany.barkbites.data.firestore.FirestoreRestClient;
import java.time.Instant;
import java.util.Scanner;

/**
 * Manual smoke test to validate Firebase connectivity (Auth REST + Firestore REST).
 *
 * How it works:
 * 1) Loads src/main/resources/firebase.properties
 * 2) Prompts for Student ID + password
 * 3) Signs in via Firebase Auth REST
 * 4) Writes a small document to Firestore using the idToken
 */
public final class FirebaseSmokeTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) {
        FirebasePublicConfig config = FirebasePublicConfig.load();
        FirebaseAuthRestService auth = new FirebaseAuthRestService(config);
        FirestoreRestClient firestore = new FirestoreRestClient(config);

        String mode;
        String studentId;
        String password;

        System.out.println("=== BarkBites Firebase Smoke Test ===");
        System.out.println("Project: " + config.projectId());

        // Choose sign-in vs sign-up.
        if (System.console() != null) {
            mode = System.console().readLine("Mode (1=Sign in, 2=Sign up) [1]: ");
        } else {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Mode (1=Sign in, 2=Sign up) [1]: ");
            mode = scanner.nextLine();
        }
        mode = (mode == null || mode.isBlank()) ? "1" : mode.trim();

        // Prefer secure password input when available.
        if (System.console() != null) {
            studentId = System.console().readLine("Student ID: ");
            System.out.println("Password input is hidden. Type it and press Enter.");
            char[] pw = System.console().readPassword("Password: ");
            password = pw == null ? "" : new String(pw);
        } else {
            // Fallback for IDEs that don't provide a console.
            Scanner scanner = new Scanner(System.in);
            System.out.print("Student ID: ");
            studentId = scanner.nextLine();
            System.out.print("Password (will be visible): ");
            password = scanner.nextLine();
        }

        if (password == null || password.isBlank()) {
            System.err.println("FAILED: Password was empty. Enter a password and try again.");
            System.exit(1);
            return;
        }

        try {
            AuthSession session = "2".equals(mode)
                    ? auth.signUp(studentId, password)
                    : auth.signIn(studentId, password);
            System.out.println("Auth OK. UID=" + session.uid());

            // Write a simple document as proof of Firestore access.
            String collection = "smoketests";
            String docId = session.uid();

            ObjectNode doc = MAPPER.createObjectNode();
            ObjectNode fields = doc.putObject("fields");

            ObjectNode when = MAPPER.createObjectNode();
            when.put("timestampValue", Instant.now().toString());
            fields.set("lastSmokeTestAt", when);

            ObjectNode sid = MAPPER.createObjectNode();
            sid.put("stringValue", studentId);
            fields.set("studentId", sid);

            firestore.upsertDocument(session.idToken(), collection, docId, doc);
            System.out.println("Firestore OK. Wrote documents/" + collection + "/" + docId);

            System.out.println("SUCCESS: Firebase Auth + Firestore REST are working.");
        } catch (Exception ex) {
            System.err.println("FAILED: " + ex.getMessage());
            System.err.println();
            System.err.println("Common fixes:");
            System.err.println("- Ensure firebase.webApiKey is correct in src/main/resources/firebase.properties");
            System.err.println("- Ensure Firebase Auth Email/Password is enabled");
            System.err.println("- Ensure Firestore rules allow authenticated access");
            System.exit(1);
        }
    }
}
