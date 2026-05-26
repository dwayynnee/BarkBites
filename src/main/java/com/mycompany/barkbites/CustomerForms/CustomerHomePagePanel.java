package com.mycompany.barkbites.CustomerForms;

import com.mycompany.barkbites.FormNavigator;
import com.mycompany.barkbites.data.auth.AuthState;
import javax.swing.JOptionPane;

public class CustomerHomePagePanel extends javax.swing.JFrame {

    public CustomerHomePagePanel() {
        if (!AuthState.isSignedIn()) {
            JOptionPane.showMessageDialog(this, "Please log in first.", "Not signed in", JOptionPane.WARNING_MESSAGE);
            FormNavigator.redirect(this, new CustomerLoginPanel());
            return;
        }
        initComponents();
        configureUI();

        this.setResizable(false);
    }

    /**
     * Makes navigation buttons invisible but keeps them clickable.
     * Buttons are positioned over the background image for visual design.
     */
    private void configureUI() {
        makeButtonInvisible(jButton1);
        makeButtonInvisible(jButton2);
        makeButtonInvisible(jButton3);
        makeButtonInvisible(jButton4);
        makeButtonInvisible(jButton5);
        makeButtonInvisible(jButton6);
        makeButtonInvisible(jButton7);
    }

    /**
     * Makes a button invisible while keeping it clickable.
     * Removes all visual elements (text, background, border, focus indicator).
     */
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
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

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
    // End of variables declaration//GEN-END:variables
}
