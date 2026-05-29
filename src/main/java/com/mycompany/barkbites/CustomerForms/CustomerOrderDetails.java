package com.mycompany.barkbites.CustomerForms;

/*
 * CustomerOrderDetails — shows order progress steps for customers.
 */

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.mycompany.barkbites.FormNavigator;
import com.mycompany.barkbites.data.FirebaseInitializer;
import com.mycompany.barkbites.data.auth.AuthSession;
import com.mycompany.barkbites.data.auth.AuthState;
import com.mycompany.barkbites.data.staff.StaffFirebaseBootstrap;
import java.util.Objects;
import javax.swing.SwingWorker;

public class CustomerOrderDetails extends javax.swing.JFrame {

    public CustomerOrderDetails() {
        initComponents();
        configureUi();
        loadOrderStatusAsync();
        this.setResizable(false);
    }

    private void configureUi() {
        makeButtonInvisible(jButton1);
        makeButtonInvisible(jButton2);

        jLabel2.setVisible(false);
        jLabel3.setVisible(false);
        jLabel4.setVisible(false);
        jLabel5.setVisible(false);
        jLabel6.setVisible(false);

        jButton1.addActionListener(evt -> FormNavigator.redirect(this, new CustomerHomePagePanel()));
        jButton2.addActionListener(evt -> FormNavigator.redirect(this, new CustomerHomePagePanel()));
    }

    private void loadOrderStatusAsync() {
        AuthSession session = AuthState.current();
        if (session == null) {
            return;
        }

        if (!StaffFirebaseBootstrap.ensureInitialized(this)) {
            return;
        }

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                Firestore firestore = FirebaseInitializer.getFirestore();
                ApiFuture<QuerySnapshot> future = firestore.collection("customers")
                    .document(Objects.requireNonNull(session.uid(), "session.uid()"))
                        .collection("orders")
                        .get();

                QuerySnapshot snapshot = future.get();
                if (snapshot == null || snapshot.isEmpty()) {
                    return null;
                }

                QueryDocumentSnapshot latestActiveOrder = null;
                long latestCreatedAt = Long.MIN_VALUE;
                for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                    String status = readStatus(document);
                    if (status == null || isFinishedStatus(status)) {
                        continue;
                    }

                    Long createdAt = document.getLong("createdAtMillis");
                    long createdAtValue = createdAt != null ? createdAt.longValue() : 0L;
                    if (latestActiveOrder == null || createdAtValue > latestCreatedAt) {
                        latestActiveOrder = document;
                        latestCreatedAt = createdAtValue;
                    }
                }

                return latestActiveOrder != null ? readStatus(latestActiveOrder) : null;
            }

            @Override
            protected void done() {
                try {
                    updateProgressByStatus(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    hideProgressLabels();
                } catch (Exception ex) {
                    hideProgressLabels();
                }
            }
        };

        worker.execute();
    }

    private void updateProgressByStatus(String status) {
        hideProgressLabels();

        if (status == null || status.isBlank()) {
            return;
        }

        int visibleCount = switch (status.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "pending" -> 2;
            case "confirmed" -> 3;
            case "processing" -> 4;
            case "ready" -> 5;
            default -> 0;
        };

        javax.swing.JLabel[] labels = new javax.swing.JLabel[] { jLabel2, jLabel3, jLabel4, jLabel5, jLabel6 };
        for (int i = 0; i < visibleCount && i < labels.length; i++) {
            labels[i].setVisible(true);
        }
    }

    private void hideProgressLabels() {
        jLabel2.setVisible(false);
        jLabel3.setVisible(false);
        jLabel4.setVisible(false);
        jLabel5.setVisible(false);
        jLabel6.setVisible(false);
    }

    private static String readStatus(QueryDocumentSnapshot document) {
        String status = document.getString("status");
        if (status == null || status.isBlank()) {
            status = document.getString("Status");
        }
        return status;
    }

    private static boolean isFinishedStatus(String status) {
        return "completed".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status);
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
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setText("jButton1");
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 10, -1, 60));

        jButton2.setText("jButton2");
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 520, 220, 70));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/check.png"))); // NOI18N
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 110, 40, 50));

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/check.png"))); // NOI18N
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 200, 50, 40));

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/check.png"))); // NOI18N
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(92, 280, 40, 40));

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/check.png"))); // NOI18N
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 360, -1, 40));

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/check.png"))); // NOI18N
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 440, -1, 40));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/CustomerOrderDetails.png"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 640));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new CustomerOrderDetails().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    // End of variables declaration//GEN-END:variables

    // loadIcon removed: not used in this class
}
