package com.mycompany.barkbites.CustomerForms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.mycompany.barkbites.FormNavigator;
import com.mycompany.barkbites.data.FirebaseInitializer;
import com.mycompany.barkbites.data.FirebasePublicConfig;
import com.mycompany.barkbites.data.firestore.FirestoreRestClient;
import com.mycompany.barkbites.data.firestore.FirestoreDocuments;
import com.mycompany.barkbites.data.auth.AuthSession;
import com.mycompany.barkbites.data.auth.AuthState;
import com.mycompany.barkbites.data.staff.StaffFirebaseBootstrap;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * CustomerVoucher
 *
 * Purpose:
 * - Provide a UI screen where customers can enter a voucher code and redeem it.
 * - Validate voucher existence, usage state, expiry date, and remaining uses.
 * - Update the voucher document in Firestore (REST first; Admin SDK fallback when available).
 *
 * OOP Concepts used:
 * - Encapsulation: UI logic and Firestore access are encapsulated inside this class.
 * - Abstraction: Firestore access is abstracted via FirestoreRestClient and helper
 *   classes (FirestoreDocuments, FirebasePublicConfig).
 * - Inheritance: This class extends javax.swing.JFrame to reuse window behavior.
 * - Polymorphism: SwingWorker is subclassed to run background validation without blocking
 *   the UI thread; listeners implement callback interfaces.
 *
 * Note: This file is intentionally kept minimal in behavior changes; only comments
 *       and formatting were added in this cleanup pass.
 */

public class CustomerVoucher extends javax.swing.JFrame {

    private volatile boolean validationInProgress = false;

    /*
     * Field: validationInProgress
     * Purpose: Guard to prevent multiple concurrent validations and repeated popups.
     * OOP note: This is part of the object's internal state (encapsulation).
     */

    public CustomerVoucher() {
        initComponents();
        configureUi();
        StaffFirebaseBootstrap.ensureInitialized(this);
        this.setResizable(false);
    }

    private void configureUi() {
        // Purpose: wire up UI components, visibility and event handlers.
        // OOP note: configureUi keeps UI wiring encapsulated within the object.
        // Send background image to back so buttons are clickable
        int componentCount = getContentPane().getComponentCount();
        getContentPane().setComponentZOrder(jLabel1, componentCount - 1);
        
        // Disable mouse events on background so it doesn't intercept clicks
        jLabel1.setFocusable(false);
        
        // Bring all interactive components to the front
        bringToFront(jButton1);
        bringToFront(jButton2);
        bringToFront(jButton3);
        bringToFront(jTextField1);

        // Revalidate and repaint to apply z-order changes
        getContentPane().revalidate();
        getContentPane().repaint();

        makeButtonInvisible(jButton1);
        makeButtonInvisible(jButton2);
        makeButtonInvisible(jButton3);
        makeTextFieldInvisible(jTextField1);
        
        // Add tooltips to help locate invisible components
        jButton1.setToolTipText("Click to validate voucher");
        jTextField1.setToolTipText("Enter voucher code here");
        
        // Add keyboard support - press Enter to validate
        jTextField1.addActionListener(evt -> {
            if (!validationInProgress) {
                validateVoucher();
            }
        });
        
        // Clear default text from form builder
        jTextField1.setText("");

        jButton1.addActionListener(evt -> {
            if (!validationInProgress) {
                validateVoucher();
            }
        });
        jButton2.addActionListener(evt -> navigateToCart());
        jButton3.addActionListener(evt -> navigateToCart());
    }

    private void bringToFront(javax.swing.JComponent component) {
        // Purpose: adjust z-order so the specified component is visually on top.
        // This helps ensure invisible-but-clickable components receive input.
        if (component != null) {
            getContentPane().setComponentZOrder(component, 0);
        }
    }

    private static void makeButtonInvisible(javax.swing.JButton button) {
        if (button == null) {
            return;
        }
        button.setText("");
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setEnabled(true);
        button.setFocusable(true);
    }

    private static void makeTextFieldInvisible(javax.swing.JTextField textField) {
        if (textField == null) {
            return;
        }
        // Don't clear text - user needs to enter voucher code
        textField.setOpaque(false);
        textField.setBorder(null);
        textField.setBackground(new java.awt.Color(0, 0, 0, 0));
        textField.setEditable(true);
        textField.setFocusable(true);
    }

    private void validateVoucher() {
        // Purpose: Validate the voucher entered by the user.
        // Steps:
        // 1. Guard reentry with validationInProgress.
        // 2. Ensure voucher code is present.
        // 3. Ensure user is authenticated (idToken present).
        // 4. Fetch voucher document via Firestore REST client.
        // 5. Check used flag and remaining uses.
        // 6. Verify expiry date.
        // 7. Decrement uses and persist change (REST or Admin fallback).
        // OOP note: heavy operations run inside a SwingWorker to avoid blocking UI thread.
        // Prevent multiple concurrent validations
        if (validationInProgress) {
            return;
        }
        
        String voucherCode = jTextField1.getText().trim().toUpperCase();

        if (voucherCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a voucher code.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        validationInProgress = true;
        
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    AuthSession session = AuthState.current();
                    if (session == null) {
                        showMessageOnEDT("Please log in to redeem a voucher.", "Validation", JOptionPane.WARNING_MESSAGE);
                        return null;
                    }
                    String idToken;
                    try {
                        idToken = session.idToken();
                    } catch (Exception e) {
                        idToken = null;
                    }
                    if (idToken == null || idToken.isBlank()) {
                        showMessageOnEDT("Please log in to redeem a voucher.", "Validation", JOptionPane.WARNING_MESSAGE);
                        return null;
                    }

                    FirebasePublicConfig config = FirebasePublicConfig.load();
                    FirestoreRestClient client = new FirestoreRestClient(config);

                    JsonNode voucherDoc = client.getDocument(idToken, "Vouchers", voucherCode);

                    if (voucherDoc == null || voucherDoc.isNull() || !voucherDoc.has("fields")) {
                        showMessageOnEDT("Voucher not found.", "Validation", JOptionPane.ERROR_MESSAGE);
                        return null;
                    }

                    Boolean isUsed = FirestoreDocuments.readBoolean(voucherDoc, "used", false);
                    String expiryDateStr = FirestoreDocuments.readString(voucherDoc, "expiryDate", "");
                    Long totalUses = FirestoreDocuments.readLong(voucherDoc, "total_uses", 0L);

                    // Treat "used" or exhausted uses as the same outcome.
                    if (Boolean.TRUE.equals(isUsed) || totalUses <= 0) {
                        showMessageOnEDT("Voucher already used.", "Validation", JOptionPane.ERROR_MESSAGE);
                        return null;
                    }

                    // Expiry check: expiryDate must exist, be valid, and not be expired.
                    if (expiryDateStr == null || expiryDateStr.isBlank()) {
                        showMessageOnEDT("Voucher is already expired.", "Validation", JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        sdf.setLenient(false);
                        Date expiryDate = sdf.parse(expiryDateStr.trim());
                        Date today = new Date();

                        if (today.after(expiryDate)) {
                            showMessageOnEDT("Voucher is already expired.", "Validation", JOptionPane.ERROR_MESSAGE);
                            return null;
                        }
                    } catch (Exception e) {
                        showMessageOnEDT("Voucher is already expired.", "Validation", JOptionPane.ERROR_MESSAGE);
                        return null;
                    }

                    // Decrement total_uses and set used=true if it reaches 0
                    long newTotalUses = totalUses - 1L;
                    if (newTotalUses < 0) {
                        newTotalUses = 0;
                    }
                    boolean shouldMarkAsUsed = (newTotalUses == 0);

                    // Build update document using FirestoreDocuments helpers
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode updateDoc = mapper.createObjectNode();
                    ObjectNode fields = mapper.createObjectNode();

                    fields.set("total_uses", FirestoreDocuments.integerValue(newTotalUses));
                    fields.set("used", FirestoreDocuments.booleanValue(shouldMarkAsUsed));

                    updateDoc.set("fields", fields);

                    // Update the voucher document
                    try {
                        client.upsertDocument(idToken, "Vouchers", voucherCode, updateDoc);
                    } catch (IllegalStateException ex) {
                        String msg = ex.getMessage();
                        boolean permissionDenied = msg != null && msg.toLowerCase().contains("missing or insufficient permissions");
                        if (!permissionDenied) {
                            throw ex;
                        }

                        // Fallback: if Firebase Admin SDK is initialized, perform the update with admin privileges.
                        if (!FirebaseInitializer.isInitialized()) {
                            throw ex;
                        }
                        Firestore firestore = FirebaseInitializer.getFirestore();
                        DocumentReference ref = firestore.collection("Vouchers").document(voucherCode);
                        java.util.Map<String, Object> updateMap = new java.util.HashMap<>();
                        updateMap.put("total_uses", Long.valueOf(newTotalUses));
                        updateMap.put("used", Boolean.valueOf(shouldMarkAsUsed));
                        ref.set(updateMap, SetOptions.merge()).get();
                    }

                    showMessageOnEDT("Voucher Activated", "Validation", JOptionPane.INFORMATION_MESSAGE);
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        jTextField1.setText("");
                        // After activation, navigate back to cart and show the applied voucher
                        FormNavigator.redirect(CustomerVoucher.this, new CustomerCartPanel(voucherCode));
                    });
                } catch (IllegalStateException ex) {
                    String msg = ex.getMessage();
                    if (msg != null && msg.toLowerCase().contains("missing or insufficient permissions")) {
                        showMessageOnEDT("Unable to redeem the voucher at this time.", "Validation", JOptionPane.ERROR_MESSAGE);
                    } else {
                        showMessageOnEDT("Unable to redeem the voucher at this time.", "Validation", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    showMessageOnEDT("Unable to redeem the voucher at this time.", "Validation", JOptionPane.ERROR_MESSAGE);
                }

                return null;
            }

            @Override
            protected void done() {
                // Validation complete - reset flag to allow next validation
                validationInProgress = false;
            }
        }.execute();
    }

    private void showMessageOnEDT(String message, String title, int messageType) {
        // Purpose: Ensure dialogs are presented on the Event Dispatch Thread (EDT)
        // so they integrate cleanly with Swing's threading model.
        javax.swing.SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(CustomerVoucher.this, message, title, messageType)
        );
    }

    private void navigateToCart() {
        // Purpose: Navigate to the shopping cart screen using the project's
        // central `FormNavigator` helper (abstraction for screen transitions).
        FormNavigator.redirect(this, new CustomerCartPanel());
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setText("jButton1");
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 100, 90, 40));

        jButton2.setText("jButton2");
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 360, 80));

        jTextField1.setText("jTextField1");
        getContentPane().add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 100, 120, 40));

        jButton3.setText("jButton3");
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 560, 310, 70));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/CustomerVoucher.png"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, -50, 360, 750));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new CustomerVoucher().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
