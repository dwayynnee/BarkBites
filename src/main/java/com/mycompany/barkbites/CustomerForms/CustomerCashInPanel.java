package com.mycompany.barkbites.CustomerForms;

/*
 * CustomerCashInPanel — UI for customer to add cash to their wallet.
 */

import com.mycompany.barkbites.FormNavigator;
import com.mycompany.barkbites.data.FirebasePublicConfig;
import com.mycompany.barkbites.data.auth.AuthSession;
import com.mycompany.barkbites.data.auth.AuthState;
import com.mycompany.barkbites.data.firestore.FirestoreDocuments;
import com.mycompany.barkbites.data.firestore.FirestoreRestClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ExecutionException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class CustomerCashInPanel extends javax.swing.JFrame {

    private static final String EYE_OPEN_ICON = "/com/mycompany/barkbites/CustomerDesign/eye-open.png";
    private static final String EYE_CLOSE_ICON = "/com/mycompany/barkbites/CustomerDesign/eye-close.png";
    private static final long[] QUICK_AMOUNTS_CENTS = {2000L, 5000L, 10000L, 20000L};
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private boolean walletBalanceVisible;
    private Long walletBalanceCents;
    private final javax.swing.Icon walletHiddenIcon;
    private final javax.swing.Icon walletVisibleIcon;

    public CustomerCashInPanel() {
        initComponents();

        walletHiddenIcon = loadIcon(EYE_OPEN_ICON);
        walletVisibleIcon = loadIcon(EYE_CLOSE_ICON);

        getContentPane().setComponentZOrder(jLabel1, getContentPane().getComponentCount() - 1);
        bringToFront(jLabel2);
        bringToFront(jButton1);
        bringToFront(jButton2);
        bringToFront(jButton3);
        bringToFront(jButton4);
        bringToFront(jButton5);
        bringToFront(jButton6);
        bringToFront(jButton7);
        bringToFront(jButton8);
        bringToFront(jButton9);
        bringToFront(jButton10);
        bringToFront(jTextField1);

        jLabel1.setFocusable(false);

        jButton1.addActionListener(this::jButton1ActionPerformed);
        jButton2.addActionListener(this::jButton2ActionPerformed);
        jButton3.addActionListener(this::jButton3ActionPerformed);
        jButton4.addActionListener(this::jButton4ActionPerformed);
        jButton5.addActionListener(this::jButton5ActionPerformed);
        jButton6.addActionListener(e -> toggleWalletBalanceVisibility());
        jButton7.addActionListener(e -> cashInAmount(QUICK_AMOUNTS_CENTS[0]));
        jButton8.addActionListener(e -> cashInAmount(QUICK_AMOUNTS_CENTS[1]));
        jButton9.addActionListener(e -> cashInAmount(QUICK_AMOUNTS_CENTS[2]));
        jButton10.addActionListener(e -> cashInAmount(QUICK_AMOUNTS_CENTS[3]));
        jTextField1.addActionListener(e -> cashInTypedAmount());

        makeButtonInvisible(jButton1);
        makeButtonInvisible(jButton2);
        makeButtonInvisible(jButton3);
        makeButtonInvisible(jButton4);
        makeButtonInvisible(jButton5);
        makeIconButton(jButton6);
        makeButtonInvisible(jButton7);
        makeButtonInvisible(jButton8);
        makeButtonInvisible(jButton9);
        makeButtonInvisible(jButton10);

        makeTextFieldTransparent(jTextField1);

        walletBalanceCents = null;
        jLabel2.setText(maskWalletBalance(null));
        updateWalletToggleButton();
        loadWalletBalance();

        this.setResizable(false);
    }

    private void bringToFront(java.awt.Component component) {
        if (component == null) {
            return;
        }
        getContentPane().setComponentZOrder(component, 0);
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

    private static void makeIconButton(javax.swing.JButton button) {
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

    private ImageIcon loadIcon(String resourcePath) {
        java.net.URL resource = getClass().getResource(resourcePath);
        if (resource == null) {
            throw new IllegalStateException("Missing icon resource: " + resourcePath);
        }
        return new ImageIcon(resource);
    }

    private void setBusy(boolean busy) {
        jButton1.setEnabled(!busy);
        jButton2.setEnabled(!busy);
        jButton3.setEnabled(!busy);
        jButton4.setEnabled(!busy);
        jButton5.setEnabled(!busy);
        jButton6.setEnabled(!busy);
        jButton7.setEnabled(!busy);
        jButton8.setEnabled(!busy);
        jButton9.setEnabled(!busy);
        jButton10.setEnabled(!busy);
        jTextField1.setEnabled(!busy);
        setCursor(busy ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR) : java.awt.Cursor.getDefaultCursor());
    }

    private void toggleWalletBalanceVisibility() {
        walletBalanceVisible = !walletBalanceVisible;
        refreshWalletLabel();
        updateWalletToggleButton();
    }

    private void updateWalletToggleButton() {
        jButton6.setIcon(walletBalanceVisible ? walletVisibleIcon : walletHiddenIcon);
        jButton6.setText("");
        jButton6.setToolTipText(walletBalanceVisible ? "Hide balance" : "Show balance");
        jButton6.getAccessibleContext().setAccessibleDescription(walletBalanceVisible ? "Hide balance" : "Show balance");
    }

    private void refreshWalletLabel() {
        jLabel2.setText(walletBalanceVisible ? formatWalletBalance(walletBalanceCents) : maskWalletBalance(walletBalanceCents));
    }

    private static String formatWalletBalance(Long walletBalanceCents) {
        if (walletBalanceCents == null) {
            return "Wallet unavailable";
        }
        return String.format(java.util.Locale.US, "%,.2f", walletBalanceCents / 100.0);
    }

    private static String maskWalletBalance(Long walletBalanceCents) {
        if (walletBalanceCents == null) {
            return "Wallet unavailable";
        }
        return "******";
    }

    private static long parsePesosToCents(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Enter an amount.");
        }
        String cleaned = input.trim().replace(",", "");
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Enter an amount.");
        }
        BigDecimal pesos = new BigDecimal(cleaned);
        if (pesos.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        return pesos.multiply(BigDecimal.valueOf(100L)).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private void loadWalletBalance() {
        AuthSession session = AuthState.current();
        if (session == null) {
            walletBalanceCents = null;
            refreshWalletLabel();
            return;
        }

        FirebasePublicConfig config;
        try {
            config = FirebasePublicConfig.load();
        } catch (Exception ex) {
            walletBalanceCents = null;
            refreshWalletLabel();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Firebase config error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        FirestoreRestClient firestore = new FirestoreRestClient(config);
        SwingUtilities.invokeLater(() -> setBusy(true));
        javax.swing.SwingWorker<Long, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected Long doInBackground() {
                JsonNode document = firestore.getDocument(session.idToken(), "customers", session.uid());
                return FirestoreDocuments.readWalletBalanceCents(document);
            }

            @Override
            protected void done() {
                try {
                    walletBalanceCents = get();
                    refreshWalletLabel();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    walletBalanceCents = null;
                    refreshWalletLabel();
                } catch (ExecutionException ee) {
                    String msg = ee.getCause() != null ? ee.getCause().getMessage() : ee.getMessage();
                    walletBalanceCents = null;
                    refreshWalletLabel();
                    JOptionPane.showMessageDialog(CustomerCashInPanel.this, msg, "Balance load failed", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setBusy(false);
                }
            }
        };

        worker.execute();
    }

    private void cashInTypedAmount() {
        try {
            long amountCents = parsePesosToCents(jTextField1.getText());
            cashInAmount(amountCents);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid amount", JOptionPane.WARNING_MESSAGE);
            jTextField1.requestFocusInWindow();
        }
    }

    private void cashInAmount(long amountCents) {
        AuthSession session = AuthState.current();
        if (session == null) {
            JOptionPane.showMessageDialog(this, "Please sign in again.", "Not signed in", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FirebasePublicConfig config;
        try {
            config = FirebasePublicConfig.load();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Firebase config error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        FirestoreRestClient firestore = new FirestoreRestClient(config);
        setBusy(true);
        javax.swing.SwingWorker<Long, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected Long doInBackground() {
                JsonNode document = firestore.getDocument(session.idToken(), "customers", session.uid());
                long currentBalance = FirestoreDocuments.readWalletBalanceCents(document) != null ? FirestoreDocuments.readWalletBalanceCents(document) : 0L;
                long updatedBalance = currentBalance + amountCents;

                ObjectNode update = MAPPER.createObjectNode();
                if (FirestoreDocuments.readString(document, "email", "").isBlank()) {
                    update.set("fields", FirestoreDocuments.customerDocument(
                            FirestoreDocuments.readString(document, "studentId", ""),
                            FirestoreDocuments.readString(document, "name", ""),
                            FirestoreDocuments.readString(document, "mobile", ""),
                            updatedBalance
                    ).get("fields"));
                } else {
                    update.set("fields", FirestoreDocuments.customerDocumentWithEmail(
                            FirestoreDocuments.readString(document, "studentId", ""),
                            FirestoreDocuments.readString(document, "name", ""),
                            FirestoreDocuments.readString(document, "email", ""),
                            updatedBalance
                    ).get("fields"));
                }

                firestore.upsertDocument(session.idToken(), "customers", session.uid(), update);
                return updatedBalance;
            }

            @Override
            protected void done() {
                try {
                    walletBalanceCents = get();
                    refreshWalletLabel();
                    jTextField1.setText("");
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(CustomerCashInPanel.this, "Cash in interrupted.", "Cash in failed", JOptionPane.ERROR_MESSAGE);
                } catch (ExecutionException ee) {
                    String msg = ee.getCause() != null ? ee.getCause().getMessage() : ee.getMessage();
                    JOptionPane.showMessageDialog(CustomerCashInPanel.this, msg, "Cash in failed", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setBusy(false);
                }
            }
        };

        worker.execute();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setText("jButton1");
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(285, 20, 70, 50));

        jButton2.setText("jButton2");
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 570, -1, 60));

        jButton3.setText("jButton3");
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 570, -1, 60));

        jButton4.setText("jButton4");
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 570, -1, 60));

        jButton5.setText("jButton5");
        getContentPane().add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 570, -1, 60));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setText("jLabel2");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 150, 80, 40));

        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/eye-open.png"))); // NOI18N
        getContentPane().add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 150, -1, 40));

        jButton7.setText("jButton7");
        getContentPane().add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 330, 140, 40));

        jButton8.setText("jButton8");
        getContentPane().add(jButton8, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 320, 140, 60));

        jButton9.setText("jButton9");
        getContentPane().add(jButton9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 390, 150, 60));

        jButton10.setText("jButton10");
        getContentPane().add(jButton10, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 390, 140, 60));

        jTextField1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        getContentPane().add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 470, 130, 40));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/CustomerCashInPanel.png"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 360, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        FormNavigator.redirect(this, new CustomerProfilePanelVisible());
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        FormNavigator.redirect(this, new CustomerHomePagePanel());
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        FormNavigator.redirect(this, new CustomerMenuPanel());
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        FormNavigator.redirect(this, new CustomerCartPanel());
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        FormNavigator.redirect(this, new CustomerProfilePanelVisible());
    }//GEN-LAST:event_jButton5ActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CustomerCashInPanel().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
