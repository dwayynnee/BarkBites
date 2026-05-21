package com.mycompany.barkbites.data.auth;

/**
 * Minimal in-memory auth state for the running desktop app.
 *
 * This keeps the current Firebase Auth session (uid + idToken) available to
 * other screens/services (e.g., Firestore REST calls).
 */
public final class AuthState {

    private static volatile AuthSession current;

    private AuthState() {
    }

    public static AuthSession current() {
        return current;
    }

    public static boolean isSignedIn() {
        return current != null;
    }

    public static void set(AuthSession session) {
        current = session;
    }

    public static void clear() {
        current = null;
    }
}
