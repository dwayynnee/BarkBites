package com.mycompany.barkbites.CustomerForms;

import com.mycompany.barkbites.FormNavigator;

public class CustomerShowFoodPanel2 extends javax.swing.JFrame {

    public CustomerShowFoodPanel2() {
        initComponents();
        this.setResizable(false);
    }

    private void initComponents() {
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setBorderPainted(false);
        jButton1.setContentAreaFilled(false);
        jButton1.setFocusPainted(false);
        jButton1.addActionListener(evt -> FormNavigator.redirect(this, new CustomerHomePagePanel()));
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 570, 80, 60));

        jButton2.setBorderPainted(false);
        jButton2.setContentAreaFilled(false);
        jButton2.setFocusPainted(false);
        jButton2.addActionListener(evt -> FormNavigator.redirect(this, new CustomerMenuPanel()));
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 570, 80, 60));

        jButton3.setBorderPainted(false);
        jButton3.setContentAreaFilled(false);
        jButton3.setFocusPainted(false);
        jButton3.addActionListener(evt -> FormNavigator.redirect(this, new CustomerProfilePanelVisible()));
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 570, 80, 60));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/CustomerShowFoodPanel2.png")));
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        pack();
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new CustomerShowFoodPanel2().setVisible(true));
    }

    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
}
