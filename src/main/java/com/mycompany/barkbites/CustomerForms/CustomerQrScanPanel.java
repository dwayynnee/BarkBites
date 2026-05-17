package com.mycompany.barkbites.CustomerForms;

import com.mycompany.barkbites.FormNavigator;

public class CustomerQrScanPanel extends javax.swing.JFrame {

    public CustomerQrScanPanel() {
        initComponents();

        // Keep the background image behind the click targets.
        getContentPane().setComponentZOrder(jLabel1, getContentPane().getComponentCount() - 1);
        getContentPane().setComponentZOrder(jButton1, 0);
        getContentPane().setComponentZOrder(jButton2, 0);
        getContentPane().setComponentZOrder(jButton3, 0);
        getContentPane().setComponentZOrder(jButton4, 0);
        getContentPane().setComponentZOrder(jButton5, 0);

        // Wire button navigation.
        jButton1.addActionListener(this::jButton1ActionPerformed);
        jButton2.addActionListener(this::jButton2ActionPerformed);
        jButton3.addActionListener(this::jButton3ActionPerformed);
        jButton4.addActionListener(this::jButton4ActionPerformed);
        jButton5.addActionListener(this::jButton5ActionPerformed);

        // Make buttons invisible but still clickable.
        makeButtonInvisible(jButton1);
        makeButtonInvisible(jButton2);
        makeButtonInvisible(jButton3);
        makeButtonInvisible(jButton4);
        makeButtonInvisible(jButton5);

        this.setResizable(false);
    }

    private static void makeButtonInvisible(javax.swing.JButton button) {
        if (button == null) {
            return;
        }

        java.awt.Dimension size = button.getSize();
        if (size == null || size.width <= 0 || size.height <= 0) {
            size = button.getPreferredSize();
        }
        if (size != null && size.width > 0 && size.height > 0) {
            button.setPreferredSize(size);
            button.setMinimumSize(size);
            button.setMaximumSize(size);
            button.setSize(size);
            button.setBounds(button.getX(), button.getY(), size.width, size.height);
        }

        button.setEnabled(true);
        button.setVisible(true);
        button.setText("");
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        FormNavigator.redirect(this, new CustomerProfilePanelVisible());
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        FormNavigator.redirect(this, new CustomerHomePagePanel());
    }

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {
        FormNavigator.redirect(this, new CustomerMenuPanel());
    }

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {
        FormNavigator.redirect(this, new CustomerCartPanel());
    }

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {
        FormNavigator.redirect(this, new CustomerProfilePanelVisible());
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setText("jButton1");
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 20, -1, 50));

        jButton2.setText("jButton2");
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 570, -1, 60));

        jButton3.setText("jButton3");
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 570, -1, 60));

        jButton4.setText("jButton4");
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 570, -1, 60));

        jButton5.setText("jButton5");
        getContentPane().add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 570, -1, 60));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/CustomerQrScanPanel.png"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CustomerQrScanPanel().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
