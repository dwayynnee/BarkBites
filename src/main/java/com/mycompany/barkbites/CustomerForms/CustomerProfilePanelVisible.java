package com.mycompany.barkbites.CustomerForms;

import com.mycompany.barkbites.FormNavigator;

public class CustomerProfilePanelVisible extends javax.swing.JFrame {

    public CustomerProfilePanelVisible() {
        initComponents();

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

        makeButtonInvisible(jButton1);
        makeButtonInvisible(jButton2);
        makeButtonInvisible(jButton3);
        makeButtonInvisible(jButton4);
        makeButtonInvisible(jButton5);
        makeButtonInvisible(jButton6);

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

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
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
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 290, 150, 55));

        jButton5.setText("jButton5");
        getContentPane().add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 400, 120, 130));

        jButton6.setText("jButton6");
        getContentPane().add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 400, 130, 130));

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
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
