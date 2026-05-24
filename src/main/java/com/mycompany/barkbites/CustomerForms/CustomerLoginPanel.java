package com.mycompany.barkbites.CustomerForms;

import com.mycompany.barkbites.FormNavigator;
import com.mycompany.barkbites.data.FirebasePublicConfig;
import com.mycompany.barkbites.data.auth.AuthSession;
import com.mycompany.barkbites.data.auth.AuthState;
import com.mycompany.barkbites.data.auth.FirebaseAuthRestService;
import java.awt.Color;
import java.util.concurrent.ExecutionException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class CustomerLoginPanel extends javax.swing.JFrame {

    private static final String PASSWORD_HIDDEN_ICON = "/com/mycompany/barkbites/CustomerDesign/eye-open.png";
    private static final String PASSWORD_VISIBLE_ICON = "/com/mycompany/barkbites/CustomerDesign/eye-close.png";

    private char passwordEchoChar;
    private boolean passwordVisible;
    private final javax.swing.Icon passwordHiddenIcon;
    private final javax.swing.Icon passwordVisibleIcon;

    public CustomerLoginPanel() {
        initComponents();
        passwordHiddenIcon = loadIcon(PASSWORD_HIDDEN_ICON);
        passwordVisibleIcon = loadIcon(PASSWORD_VISIBLE_ICON);

        // Keep the background image behind the click targets.
        // In Swing, index 0 is the front/top.
        // Move the background label to the back so it can't intercept clicks.
        getContentPane().setComponentZOrder(jLabel1, getContentPane().getComponentCount() - 1);
        jLabel1.setFocusable(false);

        // Ensure interactive controls are above the background.
        bringToFront(jTextField1);
        bringToFront(jTextField2);
        bringToFront(jButton1);
        bringToFront(jButton2);
        bringToFront(jButton3);
        bringToFront(jButton4);

        // Email + Password inputs.
        jTextField1.setText("");
        jTextField2.setText("");

        makeTextFieldTransparent(jTextField1);
        makePasswordFieldTransparent(jTextField2);

        // Buttons are click targets over the background image.
        makeButtonInvisible(jButton1); // Login
        makeButtonInvisible(jButton2); // Sign up
        makeButtonInvisible(jButton3); // Back
        makeButtonInvisible(jButton4); // Toggle password visibility (eye)

        // Wire click actions (do not rely on GUI builder wiring).
        jButton1.addActionListener(e -> attemptLogin());
        jButton2.addActionListener(e -> FormNavigator.redirect(this, new CustomerSignupPanel()));
        jButton3.addActionListener(e -> FormNavigator.redirect(this, new CustomerLoginOptions()));
        jButton4.addActionListener(e -> togglePasswordVisibility());

        // Capture default echo char for show/hide.
        passwordEchoChar = jTextField2.getEchoChar();
        updatePasswordToggleButton();

        this.setResizable(false);
    }

    private void bringToFront(java.awt.Component component) {
        if (component == null) {
            return;
        }
        // index 0 is front/top
        getContentPane().setComponentZOrder(component, 0);
    }

    private static void makeButtonInvisible(javax.swing.JButton button) {
        if (button == null) {
            return;
        }
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
    }

    private static void makeTextFieldTransparent(javax.swing.JTextField field) {
        if (field == null) {
            return;
        }
        field.setOpaque(false);
        field.setBackground(new Color(0, 0, 0, 0));
        field.setBorder(null);
        field.setCaretColor(field.getForeground());
    }

    private static void makePasswordFieldTransparent(javax.swing.JPasswordField field) {
        if (field == null) {
            return;
        }
        field.setOpaque(false);
        field.setBackground(new Color(0, 0, 0, 0));
        field.setBorder(null);
        field.setCaretColor(field.getForeground());
    }

    private void setBusy(boolean busy) {
        jButton1.setEnabled(!busy);
        jButton2.setEnabled(!busy);
        jButton3.setEnabled(!busy);
        jButton4.setEnabled(!busy);
        jTextField1.setEnabled(!busy);
        jTextField2.setEnabled(!busy);
        setCursor(busy ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR) : java.awt.Cursor.getDefaultCursor());
    }

    private void attemptLogin() {
        String email = jTextField1.getText() != null ? jTextField1.getText().trim() : "";
        char[] passwordChars = jTextField2.getPassword();
        String password = passwordChars != null ? new String(passwordChars) : "";
        if (passwordChars != null) {
            java.util.Arrays.fill(passwordChars, '\0');
        }

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your email.", "Missing email", JOptionPane.WARNING_MESSAGE);
            jTextField1.requestFocusInWindow();
            return;
        }
        if (!email.contains("@")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Invalid email", JOptionPane.WARNING_MESSAGE);
            jTextField1.requestFocusInWindow();
            return;
        }
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your password.", "Missing password", JOptionPane.WARNING_MESSAGE);
            jTextField2.requestFocusInWindow();
            return;
        }

        FirebasePublicConfig config;
        try {
            config = FirebasePublicConfig.load();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Firebase config error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        FirebaseAuthRestService auth = new FirebaseAuthRestService(config);

        setBusy(true);
        javax.swing.SwingWorker<AuthSession, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected AuthSession doInBackground() {
                return auth.signInWithEmail(email, password);
            }

            @Override
            protected void done() {
                try {
                    AuthSession session = get();
                    AuthState.set(session);
                    FormNavigator.redirect(CustomerLoginPanel.this, new CustomerHomePagePanel());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(CustomerLoginPanel.this, "Login interrupted.", "Login failed", JOptionPane.ERROR_MESSAGE);
                    setBusy(false);
                } catch (ExecutionException ee) {
                    String msg = ee.getCause() != null ? ee.getCause().getMessage() : ee.getMessage();
                    JOptionPane.showMessageDialog(CustomerLoginPanel.this, friendlyAuthError(msg), "Login failed", JOptionPane.ERROR_MESSAGE);
                    jTextField2.setText("");
                    SwingUtilities.invokeLater(() -> jTextField2.requestFocusInWindow());
                    setBusy(false);
                } catch (Exception ex) {
                    String msg = ex.getMessage() != null ? ex.getMessage() : "Login failed.";
                    JOptionPane.showMessageDialog(CustomerLoginPanel.this, msg, "Login failed", JOptionPane.ERROR_MESSAGE);
                    setBusy(false);
                }
            }
        };

        worker.execute();
    }

    private static String friendlyAuthError(String message) {
        if (message == null || message.isBlank()) {
            return "Login failed.";
        }
        // Firebase common errors (keep minimal; avoid changing UX).
        return switch (message) {
            case "EMAIL_NOT_FOUND", "INVALID_PASSWORD", "INVALID_LOGIN_CREDENTIALS" -> "Invalid email or password.";
            case "USER_DISABLED" -> "This account is disabled.";
            case "TOO_MANY_ATTEMPTS_TRY_LATER" -> "Too many attempts. Try again later.";
            default -> message;
        };
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        jTextField2.setEchoChar(passwordVisible ? (char) 0 : passwordEchoChar);
        updatePasswordToggleButton();
        jTextField2.requestFocusInWindow();
    }

    private void updatePasswordToggleButton() {
        jButton4.setIcon(passwordVisible ? passwordVisibleIcon : passwordHiddenIcon);
        jButton4.setText("");
        jButton4.setToolTipText(passwordVisible ? "Hide password" : "Show password");
        jButton4.getAccessibleContext().setAccessibleDescription(passwordVisible ? "Hide password" : "Show password");
    }

    private ImageIcon loadIcon(String resourcePath) {
        java.net.URL resource = getClass().getResource(resourcePath);
        if (resource == null) {
            throw new IllegalStateException("Missing icon resource: " + resourcePath);
        }
        return new ImageIcon(resource);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JPasswordField();
        jButton4 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 410, 260, 50));
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 480, 80, 50));

        jButton3.setText("jButton3");
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 60, 50));

        jTextField1.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jTextField1.setText("jTextField1");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });
        getContentPane().add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 270, 190, 30));

        jTextField2.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jTextField2.setText("jTextField2");
        getContentPane().add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 350, 120, 30));

        jButton4.setText("Show");
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 340, 60, -1));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/CustomerLoginPanel.png"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        attemptLogin();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        FormNavigator.redirect(this, new CustomerSignupPanel());
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        FormNavigator.redirect(this, new CustomerLoginOptions());
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        togglePasswordVisibility();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CustomerLoginPanel().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JPasswordField jTextField2;
    // End of variables declaration//GEN-END:variables
}
