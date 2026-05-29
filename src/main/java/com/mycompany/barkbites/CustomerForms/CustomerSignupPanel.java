package com.mycompany.barkbites.CustomerForms;

/*
 * CustomerSignupPanel — registration UI allowing new customers to sign up.
 */

import com.mycompany.barkbites.FormNavigator;
import com.mycompany.barkbites.data.FirebasePublicConfig;
import com.mycompany.barkbites.data.auth.AuthSession;
import com.mycompany.barkbites.data.auth.AuthState;
import com.mycompany.barkbites.data.auth.FirebaseAuthRestService;
import com.mycompany.barkbites.data.firestore.FirestoreDocuments;
import com.mycompany.barkbites.data.firestore.FirestoreRestClient;
import java.awt.Color;
import java.util.concurrent.ExecutionException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class CustomerSignupPanel extends javax.swing.JFrame {

    private static final String PASSWORD_HIDDEN_ICON = "/com/mycompany/barkbites/CustomerDesign/eye-open.png";
    private static final String PASSWORD_VISIBLE_ICON = "/com/mycompany/barkbites/CustomerDesign/eye-close.png";

    private record RegisterResult(AuthSession session, String warning) {
    }

    private char passwordEchoChar;
    private boolean passwordVisible;
    private final javax.swing.Icon passwordHiddenIcon;
    private final javax.swing.Icon passwordVisibleIcon;

    public CustomerSignupPanel() {
        initComponents();
        passwordHiddenIcon = loadIcon(PASSWORD_HIDDEN_ICON);
        passwordVisibleIcon = loadIcon(PASSWORD_VISIBLE_ICON);

        // Keep the background image behind the click targets.
        // In Swing, index 0 is the front/top.
        // Move the background label to the back so it can't intercept clicks.
        getContentPane().setComponentZOrder(jLabel1, getContentPane().getComponentCount() - 1);
        jLabel1.setFocusable(false);

        // Wire button navigation.
        jButton1.addActionListener(e -> {
            FormNavigator.redirect(this, new CustomerLoginPanel());
        });
        jButton2.addActionListener(e -> attemptRegister());
        jButton3.addActionListener(e -> FormNavigator.redirect(this, new CustomerLoginOptions()));
        jButton4.addActionListener(e -> togglePasswordVisibility());

        // Inputs.
        jTextField1.setText(""); // Student ID
        jTextField2.setText(""); // Name
        jTextField3.setText(""); // Email
        jTextField4.setText(""); // Password

        makeTextFieldTransparent(jTextField1);
        makeTextFieldTransparent(jTextField2);
        makeTextFieldTransparent(jTextField3);
        makePasswordFieldTransparent(jTextField4);

        passwordEchoChar = jTextField4.getEchoChar();
        updatePasswordToggleButton();

        // Make buttons invisible but still clickable.
        makeButtonInvisible(jButton1);
        makeButtonInvisible(jButton2);
        makeButtonInvisible(jButton3);
        makeButtonInvisible(jButton4);

        this.setResizable(false);
    }

    private static void makeButtonInvisible(javax.swing.JButton button) {
        if (button == null) {
            return;
        }
        button.setText("");
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

    private ImageIcon loadIcon(String resourcePath) {
        java.net.URL resource = getClass().getResource(resourcePath);
        if (resource == null) {
            throw new IllegalStateException("Missing icon resource: " + resourcePath);
        }
        return new ImageIcon(resource);
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        jTextField4.setEchoChar(passwordVisible ? (char) 0 : passwordEchoChar);
        updatePasswordToggleButton();
        jTextField4.requestFocusInWindow();
    }

    private void updatePasswordToggleButton() {
        jButton4.setIcon(passwordVisible ? passwordVisibleIcon : passwordHiddenIcon);
        jButton4.setText("");
        jButton4.setToolTipText(passwordVisible ? "Hide password" : "Show password");
        jButton4.getAccessibleContext().setAccessibleDescription(passwordVisible ? "Hide password" : "Show password");
    }

    private void setBusy(boolean busy) {
        jButton1.setEnabled(!busy);
        jButton2.setEnabled(!busy);
        jButton4.setEnabled(!busy);
        jTextField1.setEnabled(!busy);
        jTextField2.setEnabled(!busy);
        jTextField3.setEnabled(!busy);
        jTextField4.setEnabled(!busy);
        setCursor(busy ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR) : java.awt.Cursor.getDefaultCursor());
    }

    private void attemptRegister() {
        String studentId = jTextField1.getText() != null ? jTextField1.getText().trim() : "";
        String name = jTextField2.getText() != null ? jTextField2.getText().trim() : "";
        String email = jTextField3.getText() != null ? jTextField3.getText().trim() : "";

        if (studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your Student ID.", "Missing Student ID", JOptionPane.WARNING_MESSAGE);
            jTextField1.requestFocusInWindow();
            return;
        }
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your name.", "Missing name", JOptionPane.WARNING_MESSAGE);
            jTextField2.requestFocusInWindow();
            return;
        }
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your email.", "Missing email", JOptionPane.WARNING_MESSAGE);
            jTextField3.requestFocusInWindow();
            return;
        }
        if (!email.contains("@")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Invalid email", JOptionPane.WARNING_MESSAGE);
            jTextField3.requestFocusInWindow();
            return;
        }
        char[] passwordChars = jTextField4.getPassword();
        String password = passwordChars != null ? new String(passwordChars) : "";
        if (passwordChars != null) {
            java.util.Arrays.fill(passwordChars, '\0');
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a password.", "Missing password", JOptionPane.WARNING_MESSAGE);
            jTextField4.requestFocusInWindow();
            return;
        }
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.", "Weak password", JOptionPane.WARNING_MESSAGE);
            jTextField4.requestFocusInWindow();
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
        FirestoreRestClient firestore = new FirestoreRestClient(config);

        setBusy(true);
        javax.swing.SwingWorker<RegisterResult, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected RegisterResult doInBackground() {
                AuthSession session = auth.signUpWithEmail(email, password);
                // Persist basic profile details (no password stored).
                // Use UID as document id to match common Firestore security rule patterns.
                String warning = null;
                try {
                    firestore.upsertDocument(
                            session.idToken(),
                            "customers",
                            session.uid(),
                            FirestoreDocuments.customerDocumentWithEmail(studentId, name, email, 0L)
                    );
                } catch (Exception ex) {
                    String msg = ex.getMessage() != null ? ex.getMessage() : "";
                    if (msg.contains("Missing or insufficient permissions")) {
                        warning = "Account created, but profile save was blocked by Firestore rules.";
                    } else {
                        warning = "Account created, but profile save failed.";
                    }
                }
                return new RegisterResult(session, warning);
            }

            @Override
            protected void done() {
                try {
                    RegisterResult result = get();
                    AuthState.set(result.session());
                    if (result.warning() != null) {
                        JOptionPane.showMessageDialog(
                                CustomerSignupPanel.this,
                                result.warning() + "\n\nIf you control the Firebase project, update Firestore rules to allow writes to customers/{uid}.",
                                "Profile not saved",
                                JOptionPane.WARNING_MESSAGE
                        );
                    }
                    FormNavigator.redirect(CustomerSignupPanel.this, new CustomerRegistrationCompletePanel());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(CustomerSignupPanel.this, "Registration interrupted.", "Registration failed", JOptionPane.ERROR_MESSAGE);
                    setBusy(false);
                } catch (ExecutionException ee) {
                    String msg = ee.getCause() != null ? ee.getCause().getMessage() : ee.getMessage();
                    JOptionPane.showMessageDialog(CustomerSignupPanel.this, friendlySignupError(msg), "Registration failed", JOptionPane.ERROR_MESSAGE);
                    jTextField4.setText("");
                    SwingUtilities.invokeLater(() -> jTextField4.requestFocusInWindow());
                    setBusy(false);
                } catch (Exception ex) {
                    String msg = ex.getMessage() != null ? ex.getMessage() : "Registration failed.";
                    JOptionPane.showMessageDialog(CustomerSignupPanel.this, msg, "Registration failed", JOptionPane.ERROR_MESSAGE);
                    setBusy(false);
                }
            }
        };

        worker.execute();
    }

    private static String friendlySignupError(String message) {
        if (message == null || message.isBlank()) {
            return "Registration failed.";
        }
        return switch (message) {
            case "EMAIL_EXISTS" -> "This email is already registered.";
            case "OPERATION_NOT_ALLOWED" -> "Email/password accounts are not enabled for this Firebase project.";
            default -> {
                if (message.startsWith("WEAK_PASSWORD")) {
                    yield "Password is too weak (minimum 6 characters).";
                }
                yield message;
            }
        };
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JPasswordField();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setText("jButton1");
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 570, 90, 25));

        jButton2.setText("jButton2");
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 490, 240, 55));

        jTextField1.setText("jTextField1");
        getContentPane().add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 220, 180, 40));

        jTextField2.setText("jTextField2");
        getContentPane().add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 290, 190, 40));

        jTextField3.setText("jTextField3");
        getContentPane().add(jTextField3, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 360, 190, 40));

        jTextField4.setText("jTextField4");
        getContentPane().add(jTextField4, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 430, 130, 40));

        jButton3.setText("jButton3");
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 60));

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/eye-open.png"))); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 430, 40, -1));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/CustomerSignupPanel.png"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // Action handled by the button's listener behavior.
    }//GEN-LAST:event_jButton4ActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CustomerSignupPanel().setVisible(true);
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
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JPasswordField jTextField4;
    // End of variables declaration//GEN-END:variables
}
