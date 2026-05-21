package com.mycompany.barkbites.data;

import java.awt.Component;
import java.nio.file.Path;
import java.util.Properties;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * Resolves Firebase config with minimal setup for desktop usage.
 *
 * Priority:
 * 1) Environment variables (Option A)
 * 2) Local user-home config file (~/.barkbites/app.properties)
 * 3) Prompt user to pick the service account JSON (and optionally API key)
 */
public final class FirebaseConfigResolver {

    private static final String KEY_SERVICE_ACCOUNT_PATH = "firebase.serviceAccountPath";
    private static final String KEY_WEB_API_KEY = "firebase.webApiKey";

    private FirebaseConfigResolver() {
    }

    public static FirebaseConfig resolveOrPrompt(Component parent) {
        // 1) Environment
        try {
            return FirebaseConfig.fromEnvironment();
        } catch (Exception ignored) {
        }

        // 2) Local config file
        Properties props = LocalAppConfig.load();
        String serviceAccountPath = props.getProperty(KEY_SERVICE_ACCOUNT_PATH);
        String webApiKey = props.getProperty(KEY_WEB_API_KEY);
        if (serviceAccountPath != null && !serviceAccountPath.isBlank()) {
            try {
                return new FirebaseConfig(Path.of(serviceAccountPath), webApiKey);
            } catch (Exception ignored) {
            }
        }

        // 3) Prompt
        int choose = JOptionPane.showConfirmDialog(
                parent,
                "Firebase is not configured on this device.\n\n" +
                        "To connect to Firestore, select your Firebase service account JSON file.\n" +
                        "(You only need to do this once per device.)\n\n" +
                        "Select the JSON file now?",
                "Firebase Setup",
                JOptionPane.YES_NO_OPTION
        );

        if (choose != JOptionPane.YES_OPTION) {
            return null;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Firebase service account JSON");
        int result = chooser.showOpenDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION || chooser.getSelectedFile() == null) {
            return null;
        }

        Path selected = chooser.getSelectedFile().toPath().toAbsolutePath().normalize();

        String apiKeyInput = JOptionPane.showInputDialog(
                parent,
                "Optional: paste your Firebase Web API Key (for Auth REST).\n" +
                        "You can skip this for now; Firestore will still work.\n\n" +
                        "Web API Key:",
                "Firebase Setup",
                JOptionPane.QUESTION_MESSAGE
        );

        String apiKey = apiKeyInput == null ? null : apiKeyInput.trim();
        if (apiKey != null && apiKey.isBlank()) {
            apiKey = null;
        }

        FirebaseConfig config = new FirebaseConfig(selected, apiKey);

        props.setProperty(KEY_SERVICE_ACCOUNT_PATH, selected.toString());
        if (apiKey != null) {
            props.setProperty(KEY_WEB_API_KEY, apiKey);
        }

        try {
            LocalAppConfig.save(props);
        } catch (Exception ignored) {
        }

        return config;
    }
}
