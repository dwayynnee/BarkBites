package com.mycompany.barkbites.data.auth;

/**
 * Result of Firebase Auth REST sign-in/up.
 */
public record AuthSession(String uid, String idToken) {
}
