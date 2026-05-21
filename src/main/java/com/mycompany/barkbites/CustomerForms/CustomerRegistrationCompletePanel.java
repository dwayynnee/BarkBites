package com.mycompany.barkbites.CustomerForms;

import com.mycompany.barkbites.FormNavigator;

public class CustomerRegistrationCompletePanel extends javax.swing.JFrame {

    private javax.swing.Timer redirectTimer;

    public CustomerRegistrationCompletePanel() {
        initComponents();

        scheduleAutoRedirect();

        // Cleanup if the user closes the window before redirect.
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                stopRedirectTimer();
            }
        });

        this.setResizable(false);
    }

    private void scheduleAutoRedirect() {
        stopRedirectTimer();

        redirectTimer = new javax.swing.Timer(4000, e -> {
            FormNavigator.redirect(CustomerRegistrationCompletePanel.this, new CustomerHomePagePanel());
        });
        redirectTimer.setRepeats(false);
        redirectTimer.start();
    }

    private void stopRedirectTimer() {
        if (redirectTimer != null) {
            redirectTimer.stop();
            redirectTimer = null;
        }
    }


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/CustomerRegistrationCompletePanel.png"))); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 650, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CustomerRegistrationCompletePanel().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
