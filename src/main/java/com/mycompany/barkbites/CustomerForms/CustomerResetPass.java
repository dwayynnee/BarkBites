package com.mycompany.barkbites.CustomerForms;

import com.mycompany.barkbites.FormNavigator;
import java.awt.Color;
import javax.swing.JOptionPane;

public class CustomerResetPass extends javax.swing.JFrame {

    public CustomerResetPass() {
        initComponents();
        configureForm();
        this.setResizable(false);
    }

    private void configureForm() {
        sendToBack(jLabel1);

        bringToFront(jTextField1);
        bringToFront(jTextField2);
        bringToFront(jButton1);
        bringToFront(jButton2);

        makeTextFieldTransparent(jTextField1);
        makeTextFieldTransparent(jTextField2);
        jTextField1.setText("");
        jTextField2.setText("");
        jTextField1.setEditable(true);
        jTextField2.setEditable(true);
        jTextField1.setEnabled(true);
        jTextField2.setEnabled(true);
        makeButtonInvisible(jButton1);
        makeButtonInvisible(jButton2);

        jButton1.addActionListener(e -> FormNavigator.redirect(this, new CustomerCheckPhone()));
        jButton2.addActionListener(e -> submitNewPassword());
    }

    private void bringToFront(java.awt.Component component) {
        if (component == null) {
            return;
        }
        getContentPane().setComponentZOrder(component, 0);
    }

    private void sendToBack(java.awt.Component component) {
        if (component == null) {
            return;
        }
        getContentPane().setComponentZOrder(component, getContentPane().getComponentCount() - 1);
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

    private static void makeTextFieldTransparent(javax.swing.JTextField field) {
        if (field == null) {
            return;
        }
        field.setOpaque(false);
        field.setBackground(new Color(0, 0, 0, 0));
        field.setBorder(null);
        field.setCaretColor(field.getForeground());
    }

    private void submitNewPassword() {
        String password = jTextField1.getText() != null ? jTextField1.getText().trim() : "";
        String confirmPassword = jTextField2.getText() != null ? jTextField2.getText().trim() : "";

        if (password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in both password fields.", "Missing password", JOptionPane.WARNING_MESSAGE);
            jTextField1.requestFocusInWindow();
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.", "Weak password", JOptionPane.WARNING_MESSAGE);
            jTextField1.requestFocusInWindow();
            return;
        }

        if (!password.matches(".*\\d.*")) {
            JOptionPane.showMessageDialog(this, "Password must contain at least one number.", "Weak password", JOptionPane.WARNING_MESSAGE);
            jTextField1.requestFocusInWindow();
            return;
        }

        if (!password.matches(".*[^A-Za-z0-9].*")) {
            JOptionPane.showMessageDialog(this, "Password must contain at least one special character.", "Weak password", JOptionPane.WARNING_MESSAGE);
            jTextField1.requestFocusInWindow();
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Mismatch", JOptionPane.WARNING_MESSAGE);
            jTextField2.requestFocusInWindow();
            return;
        }

        JOptionPane.showMessageDialog(this, "Password reset successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        FormNavigator.redirect(this, new CustomerLoginPanel());
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setText("jButton1");
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, -1, 60));

        jButton2.setText("jButton2");
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 490, 250, 60));

        jTextField1.setText("jTextField1");
        getContentPane().add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 270, 220, 30));

        jTextField2.setText("jTextField2");
        getContentPane().add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 340, 220, 40));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/CustomerResetPass.png"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 360, 650));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new CustomerResetPass().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
}
