package com.mycompany.barkbites.CustomerForms;

import com.mycompany.barkbites.FormNavigator;

public class CustomerSignupPanel extends javax.swing.JFrame {

    public CustomerSignupPanel() {
        initComponents();

        // Keep the background image behind the click targets.
        getContentPane().setComponentZOrder(jLabel1, getContentPane().getComponentCount() - 1);
        getContentPane().setComponentZOrder(jButton1, 0);
        getContentPane().setComponentZOrder(jButton2, 0);

        // Wire button navigation.
        jButton1.addActionListener(e -> {
            FormNavigator.redirect(this, new CustomerLoginPanel());
        });
        jButton2.addActionListener(e -> {
            FormNavigator.redirect(this, new CustomerRegistrationCompletePanel());
        });

        // Make buttons invisible but still clickable.
        makeButtonInvisible(jButton1);
        makeButtonInvisible(jButton2);

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

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setText("jButton1");
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 570, 90, 25));

        jButton2.setText("jButton2");
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 490, 240, 55));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/CustomerSignupPanel.png"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CustomerSignupPanel().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
