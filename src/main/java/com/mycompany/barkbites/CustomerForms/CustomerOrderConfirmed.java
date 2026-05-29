package com.mycompany.barkbites.CustomerForms;

import com.mycompany.barkbites.FormNavigator;

public class CustomerOrderConfirmed extends javax.swing.JFrame {

    public CustomerOrderConfirmed() {
        initComponents();
        configureUi();
        this.setResizable(false);
    }

    private void configureUi() {
        getContentPane().setComponentZOrder(jLabel1, getContentPane().getComponentCount() - 1);

        bringToFront(jButton1);
        bringToFront(jButton2);

        makeButtonInvisible(jButton1);
        makeButtonInvisible(jButton2);

        jButton1.addActionListener(evt -> FormNavigator.redirect(this, new CustomerMenuPanel()));
        jButton2.addActionListener(evt -> FormNavigator.redirect(this, new CustomerOrderDetails()));
    }

    private void bringToFront(javax.swing.JComponent component) {
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

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setText("jButton1");
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 360, 200, 50));

        jButton2.setText("jButton2");
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 330, 100, 20));

        jLabel2.setText("jLabel2");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 420, 60, 30));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/CustomerOrderConfirmed .png"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 360, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new CustomerOrderConfirmed().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables
}
