package com.mycompany.barkbites.data.staff;

import com.mycompany.barkbites.data.FirebaseConfig;
import com.mycompany.barkbites.data.FirebaseConfigResolver;
import com.mycompany.barkbites.data.FirebaseInitializer;
import java.awt.Component;
import javax.swing.JOptionPane;

/**
 * Shared Firebase Admin bootstrap for Staff screens.
 */
public final class StaffFirebaseBootstrap {

    private StaffFirebaseBootstrap() {
    }

    public static boolean ensureInitialized(Component parent) {
        if (FirebaseInitializer.isInitialized()) {
            return true;
        }

        FirebaseConfig config = FirebaseConfigResolver.resolveOrPrompt(parent);
        if (config == null) {
            JOptionPane.showMessageDialog(parent, "Firebase is required to load Staff data.", "Firebase setup", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Attempt initialization and show any returned error message to the user.
        FirebaseInitializer.FirebaseInitResult result = FirebaseInitializer.initialize(config);
        if (!result.success()) {
            String err = result.errorMessage() != null ? result.errorMessage() : "Firebase initialization failed.";
            JOptionPane.showMessageDialog(parent, err, "Firebase setup", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (result.warningMessage() != null) {
            JOptionPane.showMessageDialog(parent, result.warningMessage(), "Firebase setup", JOptionPane.WARNING_MESSAGE);
        }

        return FirebaseInitializer.isInitialized();
    }
}