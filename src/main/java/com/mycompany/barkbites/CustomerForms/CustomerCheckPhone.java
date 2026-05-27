package com.mycompany.barkbites.CustomerForms;

import com.mycompany.barkbites.FormNavigator;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

public class CustomerCheckPhone extends javax.swing.JFrame {

    private static final int RESEND_SECONDS = 30;

    private javax.swing.Timer resendTimer;
    private int remainingSeconds = RESEND_SECONDS;

    public CustomerCheckPhone() {
        initComponents();
        configureForm();
        this.setResizable(false);
    }

    private void configureForm() {
        bringToFront(jTextField1);
        bringToFront(jTextField2);
        bringToFront(jTextField3);
        bringToFront(jTextField4);
        bringToFront(jButton1);
        bringToFront(jButton3);
        bringToFront(jButton4);

        makeTextFieldTransparent(jTextField1);
        makeTextFieldTransparent(jTextField2);
        makeTextFieldTransparent(jTextField3);
        makeTextFieldTransparent(jTextField4);

        makeButtonInvisible(jButton1);
        makeButtonInvisible(jButton3);
        makeButtonInvisible(jButton4);

        jButton1.addActionListener(e -> FormNavigator.redirect(this, new CustomerLoginPasswordRecovery()));
        jButton3.addActionListener(e -> attemptResetCode());
        jButton4.addActionListener(e -> resendCode());

        configureDigitField(jTextField1, jTextField2, null);
        configureDigitField(jTextField2, jTextField3, jTextField1);
        configureDigitField(jTextField3, jTextField4, jTextField2);
        configureDigitField(jTextField4, null, jTextField3);

        updateTimerLabel();
        updateResendButtonState();
        startResendTimer();
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
        button.setText("");
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

    private void configureDigitField(javax.swing.JTextField field, javax.swing.JTextField next, javax.swing.JTextField previous) {
        if (field.getDocument() instanceof PlainDocument plainDocument) {
            plainDocument.setDocumentFilter(new SingleDigitFilter());
        }

        field.addActionListener(e -> attemptResetCode());

        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                onChanged();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                onChanged();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                onChanged();
            }

            private void onChanged() {
                SwingUtilities.invokeLater(() -> {
                    if (field.getText().length() == 1 && next != null) {
                        next.requestFocusInWindow();
                        next.selectAll();
                    }
                });
            }
        });

        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && field.getText().isEmpty() && previous != null) {
                    previous.requestFocusInWindow();
                    previous.selectAll();
                }
            }
        });
    }

    private String enteredCode() {
        return jTextField1.getText().trim()
                + jTextField2.getText().trim()
                + jTextField3.getText().trim()
                + jTextField4.getText().trim();
    }

    private void attemptResetCode() {
        String code = enteredCode();
        if (code.length() < 4) {
            return;
        }

        if ("1234".equals(code)) {
            FormNavigator.redirect(this, new CustomerResetPass());
            return;
        }

        JOptionPane.showMessageDialog(this, "Incorrect code.", "Invalid code", JOptionPane.ERROR_MESSAGE);
        clearCodeFields();
    }

    private void clearCodeFields() {
        jTextField1.setText("");
        jTextField2.setText("");
        jTextField3.setText("");
        jTextField4.setText("");
        jTextField1.requestFocusInWindow();
    }

    private void startResendTimer() {
        stopResendTimer();
        remainingSeconds = RESEND_SECONDS;
        updateTimerLabel();
        updateResendButtonState();

        resendTimer = new javax.swing.Timer(1000, e -> {
            if (remainingSeconds > 0) {
                remainingSeconds--;
            }
            updateTimerLabel();
            updateResendButtonState();
            if (remainingSeconds <= 0) {
                stopResendTimer();
            }
        });
        resendTimer.start();
    }

    private void stopResendTimer() {
        if (resendTimer != null) {
            resendTimer.stop();
            resendTimer = null;
        }
    }

    private void updateTimerLabel() {
        jLabel2.setText(Integer.toString(Math.max(0, remainingSeconds)));
    }

    private void updateResendButtonState() {
        jButton4.setEnabled(remainingSeconds <= 0);
    }

    private void resendCode() {
        if (remainingSeconds > 0) {
            return;
        }

        JOptionPane.showMessageDialog(this, "New Code has been sent to your email.", "Code sent", JOptionPane.INFORMATION_MESSAGE);
        clearCodeFields();
        startResendTimer();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        jButton2.setText("jButton2");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setText("jButton1");
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, -1, 60));

        jButton3.setText("jButton3");
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 380, 240, 60));

        jButton4.setText("jButton4");
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 450, 240, 60));

        jTextField1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jTextField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        getContentPane().add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 270, 50, 50));

        jTextField2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jTextField2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        getContentPane().add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(121, 270, 50, 50));

        jTextField3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jTextField3.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        getContentPane().add(jTextField3, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 270, 40, 50));

        jTextField4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jTextField4.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        getContentPane().add(jTextField4, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 270, 50, 50));

        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("jLabel2");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 350, 50, 20));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/CustomerCheckPhone.png"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, -50, 360, 750));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CustomerCheckPhone().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    // End of variables declaration//GEN-END:variables

    private static final class SingleDigitFilter extends DocumentFilter {

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            replace(fb, offset, 0, string, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text == null || text.isEmpty()) {
                return;
            }

            String digits = text.replaceAll("\\D", "");
            if (digits.isEmpty()) {
                return;
            }

            fb.replace(0, fb.getDocument().getLength(), digits.substring(0, 1), attrs);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            fb.remove(offset, length);
        }
    }
}
