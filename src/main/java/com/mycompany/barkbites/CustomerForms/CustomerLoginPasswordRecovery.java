package com.mycompany.barkbites.CustomerForms;

import com.mycompany.barkbites.FormNavigator;
import com.mycompany.barkbites.data.FirebaseInitializer;
import com.mycompany.barkbites.data.staff.StaffFirebaseBootstrap;
import java.awt.Color;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;

public class CustomerLoginPasswordRecovery extends javax.swing.JFrame {

    public CustomerLoginPasswordRecovery() {
        initComponents();
        bringToFront(jTextField1);
        bringToFront(jButton1);
        bringToFront(jButton2);
        makeTextFieldTransparent(jTextField1);
        makeButtonInvisible(jButton2);
        makeButtonInvisible(jButton1);

        jButton2.addActionListener(e -> FormNavigator.redirect(this, new CustomerLoginPanel()));
        jButton1.addActionListener(e -> attemptEmailCheck());

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

    private void setBusy(boolean busy) {
        jButton1.setEnabled(!busy);
        jButton2.setEnabled(!busy);
        jTextField1.setEnabled(!busy);
        setCursor(busy ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR) : java.awt.Cursor.getDefaultCursor());
    }

    private void attemptEmailCheck() {
        String email = jTextField1.getText() != null ? jTextField1.getText().trim() : "";

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

        if (!StaffFirebaseBootstrap.ensureInitialized(this)) {
            return;
        }

        setBusy(true);
        javax.swing.SwingWorker<Boolean, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    Firestore firestore = FirebaseInitializer.getFirestore();
                    ApiFuture<QuerySnapshot> future = firestore.collection("customers")
                            .whereEqualTo("email", email)
                            .limit(1)
                            .get();
                    QuerySnapshot snapshot = future.get();
                    for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                        if (document.exists()) {
                            return true;
                        }
                    }
                    return false;
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Lookup interrupted.", ex);
                } catch (Exception ex) {
                    throw new IllegalStateException(ex.getMessage() != null ? ex.getMessage() : "Recovery failed.", ex);
                }
            }

            @Override
            protected void done() {
                try {
                    Boolean registered = get();
                    if (Boolean.TRUE.equals(registered)) {
                        FormNavigator.redirect(CustomerLoginPasswordRecovery.this, new CustomerCheckPhone());
                        return;
                    }

                    JOptionPane.showMessageDialog(
                            CustomerLoginPasswordRecovery.this,
                            "Email not found.",
                            "Recovery failed",
                            JOptionPane.ERROR_MESSAGE
                    );
                    jTextField1.requestFocusInWindow();
                    setBusy(false);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(CustomerLoginPasswordRecovery.this, "Lookup interrupted.", "Recovery failed", JOptionPane.ERROR_MESSAGE);
                    setBusy(false);
                } catch (ExecutionException ee) {
                    String msg = ee.getCause() != null ? ee.getCause().getMessage() : ee.getMessage();
                    JOptionPane.showMessageDialog(CustomerLoginPasswordRecovery.this, msg != null ? msg : "Recovery failed.", "Recovery failed", JOptionPane.ERROR_MESSAGE);
                    setBusy(false);
                } catch (Exception ex) {
                    String msg = ex.getMessage() != null ? ex.getMessage() : "Recovery failed.";
                    JOptionPane.showMessageDialog(CustomerLoginPasswordRecovery.this, msg, "Recovery failed", JOptionPane.ERROR_MESSAGE);
                    setBusy(false);
                }
            }
        };

        worker.execute();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        getContentPane().add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 320, 180, 40));

        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 390, 240, 70));
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, -1, 70));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/CustomerLoginPasswordRecovery.png"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 360, 650));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new CustomerLoginPasswordRecovery().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
