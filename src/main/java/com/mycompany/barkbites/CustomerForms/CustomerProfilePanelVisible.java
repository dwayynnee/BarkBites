package com.mycompany.barkbites.CustomerForms;

import com.mycompany.barkbites.FormNavigator;
import com.mycompany.barkbites.data.FirebasePublicConfig;
import com.mycompany.barkbites.data.auth.AuthSession;
import com.mycompany.barkbites.data.auth.AuthState;
import com.mycompany.barkbites.data.firestore.FirestoreDocuments;
import com.mycompany.barkbites.data.firestore.FirestoreRestClient;
import java.util.concurrent.ExecutionException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class CustomerProfilePanelVisible extends javax.swing.JFrame {

    private static final String EYE_OPEN_ICON = "/com/mycompany/barkbites/CustomerDesign/eye-open.png";
    private static final String EYE_CLOSE_ICON = "/com/mycompany/barkbites/CustomerDesign/eye-close.png";

    private boolean walletBalanceVisible;
    private Long walletBalanceCents;
    private final javax.swing.Icon walletHiddenIcon;
    private final javax.swing.Icon walletVisibleIcon;

    public CustomerProfilePanelVisible() {
        initComponents();
        walletHiddenIcon = loadIcon(EYE_OPEN_ICON);
        walletVisibleIcon = loadIcon(EYE_CLOSE_ICON);

        // Ensure the background image doesn't sit on top of the click targets.
        getContentPane().setComponentZOrder(jLabel1, getContentPane().getComponentCount() - 1);
        getContentPane().setComponentZOrder(jButton1, 0);
        getContentPane().setComponentZOrder(jButton2, 0);
        getContentPane().setComponentZOrder(jButton3, 0);
        getContentPane().setComponentZOrder(jButton4, 0);
        getContentPane().setComponentZOrder(jButton5, 0);
        getContentPane().setComponentZOrder(jButton6, 0);

        // Wire button actions (the methods exist, but no listeners were attached).
        jButton1.addActionListener(this::jButton1ActionPerformed);
        jButton2.addActionListener(this::jButton2ActionPerformed);
        jButton3.addActionListener(this::jButton3ActionPerformed);
        jButton4.addActionListener(this::jButton4ActionPerformed);
        jButton5.addActionListener(this::jButton5ActionPerformed);
        jButton6.addActionListener(this::jButton6ActionPerformed);
        jButton7.addActionListener(e -> toggleWalletBalanceVisibility());

        makeButtonInvisible(jButton1);
        makeButtonInvisible(jButton2);
        makeButtonInvisible(jButton3);
        makeButtonInvisible(jButton4);
        makeButtonInvisible(jButton5);
        makeButtonInvisible(jButton6);
        makeButtonInvisible(jButton7);

        jLabel4.setVisible(true);
        jLabel4.setText(maskWalletBalance(null));
        updateWalletToggleButton();
        loadProfileDetails();

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

    private ImageIcon loadIcon(String resourcePath) {
        java.net.URL resource = getClass().getResource(resourcePath);
        if (resource == null) {
            throw new IllegalStateException("Missing icon resource: " + resourcePath);
        }
        return new ImageIcon(resource);
    }

    private void toggleWalletBalanceVisibility() {
        walletBalanceVisible = !walletBalanceVisible;
        refreshWalletLabel();
        updateWalletToggleButton();
    }

    private void updateWalletToggleButton() {
        jButton7.setIcon(walletBalanceVisible ? walletVisibleIcon : walletHiddenIcon);
        jButton7.setText("");
        jButton7.setToolTipText(walletBalanceVisible ? "Hide wallet balance" : "Show wallet balance");
        jButton7.getAccessibleContext().setAccessibleDescription(walletBalanceVisible ? "Hide wallet balance" : "Show wallet balance");
    }

    private void loadProfileDetails() {
        AuthSession session = AuthState.current();
        if (session == null) {
            jLabel2.setText("Name unavailable");
            jLabel3.setText("Student ID unavailable");
            walletBalanceCents = null;
            refreshWalletLabel();
            return;
        }

        FirebasePublicConfig config;
        try {
            config = FirebasePublicConfig.load();
        } catch (Exception ex) {
            jLabel2.setText("Name unavailable");
            jLabel3.setText("Student ID unavailable");
            walletBalanceCents = null;
            refreshWalletLabel();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Firebase config error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        FirestoreRestClient firestore = new FirestoreRestClient(config);
        javax.swing.SwingWorker<ProfileData, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected ProfileData doInBackground() {
                com.fasterxml.jackson.databind.JsonNode document = firestore.getDocument(session.idToken(), "customers", session.uid());
                String name = FirestoreDocuments.readString(document, "name", "Name unavailable");
                String studentId = FirestoreDocuments.readString(document, "studentId", "Student ID unavailable");
                Long walletBalanceCents = FirestoreDocuments.readWalletBalanceCents(document);
                return new ProfileData(name, studentId, walletBalanceCents);
            }

            @Override
            protected void done() {
                try {
                    ProfileData profile = get();
                    jLabel2.setText(profile.name());
                    jLabel3.setText(profile.studentId());
                    walletBalanceCents = profile.walletBalanceCents();
                    refreshWalletLabel();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    jLabel2.setText("Name unavailable");
                    jLabel3.setText("Student ID unavailable");
                    walletBalanceCents = null;
                    refreshWalletLabel();
                } catch (ExecutionException ee) {
                    String msg = ee.getCause() != null ? ee.getCause().getMessage() : ee.getMessage();
                    jLabel2.setText("Name unavailable");
                    jLabel3.setText("Student ID unavailable");
                    walletBalanceCents = null;
                    refreshWalletLabel();
                    JOptionPane.showMessageDialog(CustomerProfilePanelVisible.this, msg, "Profile load failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
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

    private void refreshWalletLabel() {
        jLabel4.setVisible(true);
        jLabel4.setText(walletBalanceVisible ? formatWalletBalance(walletBalanceCents) : maskWalletBalance(walletBalanceCents));
    }

    private record ProfileData(String name, String studentId, Long walletBalanceCents) {
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setText("jButton1");
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 570, -1, 60));

        jButton2.setText("jButton2");
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 570, -1, 60));

        jButton3.setText("jButton3");
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 570, -1, 60));

        jButton4.setText("jButton4");
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 305, 150, 70));

        jButton5.setText("jButton5");
        getContentPane().add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 400, 120, 130));

        jButton6.setText("jButton6");
        getContentPane().add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 400, 130, 130));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("jLabel2");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 90, 170, 30));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("jLabel3");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 120, 220, 20));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel4.setText("jLabel4");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 250, 70, 30));

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/eye-open.png"))); // NOI18N
        getContentPane().add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 250, -1, 30));

        jButton8.setText("jButton8");
        getContentPane().add(jButton8, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 180, 130, 50));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/CustomerProfilePanelVisible.png"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        FormNavigator.redirect(this, new CustomerHomePagePanel());
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        FormNavigator.redirect(this, new CustomerMenuPanel());
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        FormNavigator.redirect(this, new CustomerCartPanel());
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        FormNavigator.redirect(this, new CustomerCashInPanel());
    }//GEN-LAST:event_jButton4ActionPerformed
    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        FormNavigator.redirect(this, new CustomerQrScannerPanel());
    } 
    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        FormNavigator.redirect(this, new CustomerQrScanPanel());
    }  
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CustomerProfilePanelVisible().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    // End of variables declaration//GEN-END:variables
}
