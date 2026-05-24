package com.mycompany.barkbites.StaffForms;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.mycompany.barkbites.FormNavigator;
import com.mycompany.barkbites.data.FirebaseInitializer;
import com.mycompany.barkbites.data.staff.StaffDatabaseSchema;
import com.mycompany.barkbites.data.staff.StaffFirebaseBootstrap;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

public class StaffPassword extends javax.swing.JFrame {

    private static final String STAFF_PIN_FIELD = "pin";
    private static final String DEFAULT_STAFF_PIN = "1234";

    private volatile String configuredStaffPin;
    private volatile boolean pinLoaded;

    public StaffPassword() {
        initComponents();

        setupPinFields();
        loadStaffPinAsync();

        this.setResizable(false);
    }

    private void setupPinFields() {
        setPinInputsEnabled(false);
        jTextField1.setText("");
        jTextField2.setText("");
        jTextField3.setText("");
        jTextField4.setText("");

        makePinFieldTransparent(jTextField1);
        makePinFieldTransparent(jTextField2);
        makePinFieldTransparent(jTextField3);
        makePinFieldTransparent(jTextField4);

        jTextField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField3.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField4.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        configurePinField(jTextField2, jTextField3, null);
        configurePinField(jTextField3, jTextField4, jTextField2);
        configurePinField(jTextField4, jTextField1, jTextField3);
        configurePinField(jTextField1, null, jTextField4);

        SwingUtilities.invokeLater(() -> jTextField2.requestFocusInWindow());
    }

    private void setPinInputsEnabled(boolean enabled) {
        jTextField1.setEnabled(enabled);
        jTextField2.setEnabled(enabled);
        jTextField3.setEnabled(enabled);
        jTextField4.setEnabled(enabled);
    }

    @SuppressWarnings("null")
    private void loadStaffPinAsync() {
        javax.swing.SwingWorker<String, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                if (!StaffFirebaseBootstrap.ensureInitialized(StaffPassword.this)) {
                    return DEFAULT_STAFF_PIN;
                }

                if (!FirebaseInitializer.isInitialized()) {
                    return DEFAULT_STAFF_PIN;
                }

                Firestore firestore = FirebaseInitializer.getFirestore();
                DocumentReference docRef = firestore.collection(StaffDatabaseSchema.settingsCollection())
                        .document(StaffDatabaseSchema.staffPasswordDocument());
                DocumentSnapshot snapshot = docRef.get().get();

                if (!snapshot.exists()) {
                    docRef.set(Map.of(STAFF_PIN_FIELD, DEFAULT_STAFF_PIN)).get();
                    return DEFAULT_STAFF_PIN;
                }

                String storedPin = snapshot.getString(STAFF_PIN_FIELD);
                if (storedPin == null || storedPin.isBlank()) {
                    docRef.set(Map.of(STAFF_PIN_FIELD, DEFAULT_STAFF_PIN)).get();
                    return DEFAULT_STAFF_PIN;
                }

                return storedPin.trim();
            }

            @Override
            protected void done() {
                try {
                    configuredStaffPin = get();
                } catch (Exception ex) {
                    configuredStaffPin = DEFAULT_STAFF_PIN;
                    String message = ex.getMessage() != null ? ex.getMessage() : "Unable to load staff PIN from Firestore.";
                    JOptionPane.showMessageDialog(StaffPassword.this, message + "\nUsing the default PIN for now.", "Staff PIN", JOptionPane.WARNING_MESSAGE);
                } finally {
                    pinLoaded = true;
                    setPinInputsEnabled(true);
                    SwingUtilities.invokeLater(() -> jTextField2.requestFocusInWindow());
                }
            }
        };

        worker.execute();
    }

    private void makePinFieldTransparent(javax.swing.JTextField field) {
        field.setOpaque(false);
        field.setBackground(new Color(0, 0, 0, 0));
        field.setBorder(null);
        field.setCaretColor(field.getForeground());
    }

    private void configurePinField(javax.swing.JTextField field, javax.swing.JTextField next, javax.swing.JTextField prev) {
        if (field.getDocument() instanceof PlainDocument plainDocument) {
            plainDocument.setDocumentFilter(new DigitLimitFilter(1));
        }

        field.addActionListener(e -> checkPinAndMaybeLogin());

        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onChanged();
            }

            private void onChanged() {
                SwingUtilities.invokeLater(() -> {
                    if (field.getText().length() == 1 && next != null) {
                        next.requestFocusInWindow();
                        next.selectAll();
                    }
                    checkPinAndMaybeLogin();
                });
            }
        });

        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && field.getText().isEmpty() && prev != null) {
                    prev.requestFocusInWindow();
                    prev.selectAll();
                }
            }
        });
    }

    private void checkPinAndMaybeLogin() {
        if (!pinLoaded || configuredStaffPin == null) {
            return;
        }

        String pin = jTextField2.getText() + jTextField3.getText() + jTextField4.getText() + jTextField1.getText();
        if (pin.length() < 4) {
            return;
        }

        if (configuredStaffPin.equals(pin)) {
            JOptionPane.showMessageDialog(this, "PIN correct. Logging in...", "Success", JOptionPane.INFORMATION_MESSAGE);
            openStaffOrders();
            return;
        }

        JOptionPane.showMessageDialog(this, "Incorrect PIN. Please try again.", "Invalid PIN", JOptionPane.ERROR_MESSAGE);
        clearPin();
    }

    private void clearPin() {
        jTextField1.setText("");
        jTextField2.setText("");
        jTextField3.setText("");
        jTextField4.setText("");
        jTextField2.requestFocusInWindow();
    }

    private void openStaffOrders() {
        FormNavigator.redirect(this, new StaffOrders());
    }

    private static final class DigitLimitFilter extends DocumentFilter {

        private final int maxChars;

        private DigitLimitFilter(int maxChars) {
            this.maxChars = maxChars;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
            replace(fb, offset, 0, text, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text == null || text.isEmpty()) {
                fb.replace(offset, length, text, attrs);
                return;
            }

            String digitsOnly = text.replaceAll("\\D", "");
            if (digitsOnly.isEmpty()) {
                return;
            }
            if (digitsOnly.length() > 1) {
                digitsOnly = digitsOnly.substring(0, 1);
            }

            int currentLength = fb.getDocument().getLength();
            int newLength = currentLength - length + digitsOnly.length();
            if (newLength > maxChars) {
                return;
            }

            fb.replace(offset, length, digitsOnly, attrs);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTextField1.setFont(new java.awt.Font("Berlin Sans FB Demi", 0, 24)); // NOI18N
        jTextField1.setText("jTextField1");
        getContentPane().add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 360, 80, 70));

        jTextField2.setFont(new java.awt.Font("Berlin Sans FB Demi", 0, 24)); // NOI18N
        jTextField2.setText("jTextField1");
        getContentPane().add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(201, 360, 80, 70));

        jTextField3.setFont(new java.awt.Font("Berlin Sans FB Demi", 0, 24)); // NOI18N
        jTextField3.setText("jTextField1");
        getContentPane().add(jTextField3, new org.netbeans.lib.awtextra.AbsoluteConstraints(341, 360, 80, 70));

        jTextField4.setFont(new java.awt.Font("Berlin Sans FB Demi", 0, 24)); // NOI18N
        jTextField4.setText("jTextField1");
        getContentPane().add(jTextField4, new org.netbeans.lib.awtextra.AbsoluteConstraints(481, 360, 80, 70));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/StaffDesign/StaffPassword.png"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new StaffPassword().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    // End of variables declaration//GEN-END:variables
}