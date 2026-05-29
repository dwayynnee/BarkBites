package com.mycompany.barkbites.CustomerForms;

/*
 * CustomerHomePagePanel — main landing UI for customers showing banners and orders.
 */

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.mycompany.barkbites.FormNavigator;
import com.mycompany.barkbites.data.auth.AuthState;
import com.mycompany.barkbites.data.auth.AuthSession;
import com.mycompany.barkbites.data.FirebaseInitializer;
import com.mycompany.barkbites.data.staff.StaffFirebaseBootstrap;
import java.awt.Cursor;
import java.util.Objects;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

public class CustomerHomePagePanel extends javax.swing.JFrame {

    public CustomerHomePagePanel() {
        if (!AuthState.isSignedIn()) {
            JOptionPane.showMessageDialog(this, "Please log in first.", "Not signed in", JOptionPane.WARNING_MESSAGE);
            FormNavigator.redirect(this, new CustomerLoginPanel());
            return;
        }
        initComponents();
        configureUI();
        StaffFirebaseBootstrap.ensureInitialized(this);
        loadOrderBannerVisibility();

        this.setResizable(false);
    }

    /**
     * Makes navigation buttons invisible but keeps them clickable.
     * Buttons are positioned over the background image for visual design.
     */
    private void configureUI() {
        getContentPane().setComponentZOrder(jLabel1, getContentPane().getComponentCount() - 1);

        makeButtonInvisible(jButton1);
        makeButtonInvisible(jButton2);
        makeButtonInvisible(jButton3);
        makeButtonInvisible(jButton4);
        makeButtonInvisible(jButton5);
        makeButtonInvisible(jButton6);
        makeButtonInvisible(jButton7);

        bringToFront(jButton5);
        bringToFront(jButton7);

        makePanelClickable(jPanel1);
        jPanel1.setVisible(false);
        jPanel1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                FormNavigator.redirect(CustomerHomePagePanel.this, new CustomerOrderDetails());
            }
        });

        jLabel2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                FormNavigator.redirect(CustomerHomePagePanel.this, new CustomerOrderDetails());
            }
        });
        jLabel3.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                FormNavigator.redirect(CustomerHomePagePanel.this, new CustomerOrderDetails());
            }
        });
        jLabel4.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                FormNavigator.redirect(CustomerHomePagePanel.this, new CustomerOrderDetails());
            }
        });

        jButton5.addActionListener(evt -> FormNavigator.redirect(this, new CustomerMenuPanel()));
        jButton7.addActionListener(evt -> FormNavigator.redirect(this, new CustomerProfilePanelVisible()));
    }

    private void loadOrderBannerVisibility() {
        AuthSession session = AuthState.current();
        if (session == null) {
            jPanel1.setVisible(false);
            return;
        }

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                Firestore firestore = FirebaseInitializer.getFirestore();
                ApiFuture<QuerySnapshot> future = firestore.collection("customers")
                    .document(Objects.requireNonNull(session.uid(), "session.uid()"))
                        .collection("orders")
                        .get();
                QuerySnapshot snapshot = future.get();
                if (snapshot == null) {
                    return false;
                }

                for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                    String status = document.getString("status");
                    if (status == null) {
                        status = document.getString("Status");
                    }
                    if (status == null || (!"completed".equalsIgnoreCase(status) && !"cancelled".equalsIgnoreCase(status))) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            protected void done() {
                try {
                    jPanel1.setVisible(Boolean.TRUE.equals(get()));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    jPanel1.setVisible(false);
                } catch (ExecutionException ex) {
                    jPanel1.setVisible(false);
                }
            }
        };

        worker.execute();
    }

    private void bringToFront(java.awt.Component component) {
        if (component != null) {
            getContentPane().setComponentZOrder(component, 0);
        }
    }

    private static void makePanelClickable(javax.swing.JPanel panel) {
        if (panel == null) {
            return;
        }
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.setOpaque(true);
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

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(39, 87, 145));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(252, 201, 41));
        jLabel2.setText("View Order Details");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 170, 30));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(248, 237, 221));
        jLabel3.setText("<-");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 10, 40, 20));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(248, 237, 221));
        jLabel4.setText("Your order is still processing…");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 40, -1, -1));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 470, 340, 90));

        jButton1.setText("jButton1");
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 330, 150, 30));

        jButton2.setText("jButton2");
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 450, 90, 100));

        jButton3.setText("jButton3");
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 450, 100, 100));

        jButton4.setText("jButton4");
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 450, 90, 100));

        jButton5.setText("jButton5");
        getContentPane().add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 570, -1, 60));

        jButton6.setText("jButton6");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 570, -1, 60));

        jButton7.setText("jButton7");
        getContentPane().add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 570, -1, 60));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/CustomerHomePagePanel.png"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        FormNavigator.redirect(this, new CustomerMenuPanel());
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        FormNavigator.redirect(this, new CustomerMenuPanel());
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        FormNavigator.redirect(this, new CustomerMenuPanel());
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        FormNavigator.redirect(this, new CustomerMenuPanel());
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        FormNavigator.redirect(this, new CustomerMenuPanel());
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        FormNavigator.redirect(this, new CustomerCartPanel());
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        FormNavigator.redirect(this, new CustomerProfilePanelVisible());
    }//GEN-LAST:event_jButton7ActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CustomerHomePagePanel().setVisible(true);
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
