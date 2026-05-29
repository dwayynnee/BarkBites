package com.mycompany.barkbites.CustomerForms;

public class CustomerOrderDetails extends javax.swing.JFrame {

    public CustomerOrderDetails() {
        initComponents();
        this.setResizable(false);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setText("jButton1");
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 10, -1, 60));

        jButton2.setText("jButton2");
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 520, 220, 70));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/CustomerOrderProcessing.png"))); // NOI18N
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
    // End of variables declaration//GEN-END:variables
}
