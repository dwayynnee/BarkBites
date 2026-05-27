package com.mycompany.barkbites;

import com.mycompany.barkbites.CustomerForms.CustomerLandingPage;
import com.mycompany.barkbites.StaffForms.StaffLandingPage;
import com.mycompany.barkbites.data.FirebaseInitializer;
import com.mycompany.barkbites.data.FirebasePublicConfig;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * BarkBites Main Application Entry Point
 * 
 * Launches the school canteen POS and management system with:
 * - Dual-mode interface: Customer ordering and Staff management
 * - Firebase/Firestore backend (with local demo data fallback)
 * - Responsive window positioning across multi-monitor setups
 * - Nimbus look-and-feel for modern UI appearance
 */
public class BarkBites {

    public static void main(String[] args) {
        // Apply Nimbus theme for modern UI appearance\n        setNimbusLookAndFeelIfAvailable();

        // Start on the Event Dispatch Thread for thread-safe GUI operations
        java.awt.EventQueue.invokeLater(() -> {
            // Attempt to load Firebase configuration from properties file
            String firebaseWarning = null;
            try {
                FirebasePublicConfig.load();
            } catch (Exception ex) {
                firebaseWarning = ex.getMessage();
            }

            // Initialize Firebase Admin SDK from environment if not already configured
            if (!FirebaseInitializer.isInitialized()) {
                FirebaseInitializer.initializeFromEnvironment();
            }

            // Show warning if Firebase configuration is missing
            // (App can still run with demo data)
            if (firebaseWarning != null && !firebaseWarning.isBlank()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Firebase is not configured.\n\n" +
                                "Update src/main/resources/firebase.properties with:\n" +
                                "- firebase.projectId\n" +
                                "- firebase.webApiKey\n\n" +
                                "Error: " + firebaseWarning,
                        "Firebase Setup",
                        JOptionPane.WARNING_MESSAGE
                );
            }

            // Launch Staff management portal
            StaffLandingPage staffLandingPage = new StaffLandingPage();
            staffLandingPage.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

            // Launch Customer ordering portal
            CustomerLandingPage customerLandingPage = new CustomerLandingPage();
            customerLandingPage.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

            positionSideBySide(customerLandingPage, staffLandingPage);

            staffLandingPage.setVisible(true);
            customerLandingPage.setVisible(true);
        });
    }

    private static void positionSideBySide(JFrame leftFrame, JFrame rightFrame) {
        if (leftFrame == null || rightFrame == null) {
            return;
        }

        GraphicsDevice screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        GraphicsConfiguration config = screen.getDefaultConfiguration();
        Rectangle bounds = config.getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);

        Rectangle usable = new Rectangle(
                bounds.x + insets.left,
                bounds.y + insets.top,
                bounds.width - insets.left - insets.right,
                bounds.height - insets.top - insets.bottom
        );

        int leftWidth = Math.max(1, leftFrame.getWidth());
        int leftHeight = Math.max(1, leftFrame.getHeight());
        int rightWidth = Math.max(1, rightFrame.getWidth());
        int rightHeight = Math.max(1, rightFrame.getHeight());

        int horizontalGap = 24;
        int totalWidth = leftWidth + horizontalGap + rightWidth;
        if (totalWidth > usable.width) {
            horizontalGap = 12;
            totalWidth = leftWidth + horizontalGap + rightWidth;
        }

        // Preferred: centered pair, side-by-side.
        if (totalWidth <= usable.width) {
            int startX = usable.x + Math.max(0, (usable.width - totalWidth) / 2);
            int leftX = startX;
            int rightX = startX + leftWidth + horizontalGap;

            int leftY = usable.y + Math.max(0, (usable.height - leftHeight) / 2);
            int rightY = usable.y + Math.max(0, (usable.height - rightHeight) / 2);

            leftFrame.setLocation(leftX, leftY);
            rightFrame.setLocation(rightX, rightY);
            return;
        }

        // Fallback: center them vertically stacked (still centered, never overlapping).
        int verticalGap = 24;
        int totalHeight = leftHeight + verticalGap + rightHeight;
        if (totalHeight > usable.height) {
            verticalGap = 12;
            totalHeight = leftHeight + verticalGap + rightHeight;
        }

        int centerXLeft = usable.x + Math.max(0, (usable.width - leftWidth) / 2);
        int centerXRight = usable.x + Math.max(0, (usable.width - rightWidth) / 2);
        int startY = usable.y + Math.max(0, (usable.height - totalHeight) / 2);

        leftFrame.setLocation(centerXLeft, startY);
        rightFrame.setLocation(centerXRight, startY + leftHeight + verticalGap);
    }

    private static void setNimbusLookAndFeelIfAvailable() {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }
    }
}
