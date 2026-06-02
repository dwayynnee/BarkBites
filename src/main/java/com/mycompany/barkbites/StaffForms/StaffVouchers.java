/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.barkbites.StaffForms;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.mycompany.barkbites.FormNavigator;
import com.mycompany.barkbites.data.FirebaseInitializer;
import com.mycompany.barkbites.data.staff.StaffFirebaseBootstrap;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author markd
 */
public class StaffVouchers extends javax.swing.JFrame {

    private static final String VOUCHERS_COLLECTION = "Vouchers";

    private final DefaultTableModel voucherTableModel = new DefaultTableModel(
            new Object[]{"vouchercode", "discount_percent", "expiry_date", "total_uses"},
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final javax.swing.JTable voucherTable = new javax.swing.JTable(voucherTableModel);
    private final TableRowSorter<DefaultTableModel> voucherSorter = new TableRowSorter<>(voucherTableModel);
    private final Map<String, String> voucherCodeToDocumentId = new HashMap<>();
    private String selectedVoucherDocumentId;

    /**
     * Creates new form StaffVouchers
     */
    public StaffVouchers() {
        initComponents();

        // Keep the static background image behind controls.
        getContentPane().setComponentZOrder(jLabel1, getContentPane().getComponentCount() - 1);
        bringInteractiveComponentsToFront();

        setupVoucherTable();
        wireActions();
        hideButtonsButKeepClickable();

        if (java.beans.Beans.isDesignTime()) {
            loadDesignTimePreviewRow();
            return;
        }

        boolean firebaseReady = StaffFirebaseBootstrap.ensureInitialized(this);
        if (firebaseReady) {
            refreshVoucherTable();
        }

        clearVoucherInputs();
    }

    private void bringInteractiveComponentsToFront() {
        getContentPane().setComponentZOrder(jScrollPane1, 0);
        getContentPane().setComponentZOrder(jTextField5, 0);
        getContentPane().setComponentZOrder(jTextField1, 0);
        getContentPane().setComponentZOrder(jTextField2, 0);
        getContentPane().setComponentZOrder(jTextField3, 0);
        getContentPane().setComponentZOrder(jTextField4, 0);
        getContentPane().setComponentZOrder(jButton8, 0);
        getContentPane().setComponentZOrder(jButton9, 0);
        getContentPane().setComponentZOrder(jButton10, 0);
        getContentPane().setComponentZOrder(jLabel1, getContentPane().getComponentCount() - 1);
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    private void setupVoucherTable() {
        voucherTable.setRowHeight(24);
        voucherTable.setGridColor(new Color(220, 220, 220));
        voucherTable.setShowGrid(true);
        voucherTable.setFillsViewportHeight(true);
        voucherTable.getTableHeader().setReorderingAllowed(false);
        voucherTable.setRowSorter(voucherSorter);
        jScrollPane1.setViewportView(voucherTable);

        voucherTable.getSelectionModel().addListSelectionListener(evt -> {
            if (evt.getValueIsAdjusting()) {
                return;
            }
            populateFieldsFromSelectedRow();
        });
    }

    private void wireActions() {
        jButton1.addActionListener(evt -> openStaffOrders());
        jButton2.addActionListener(evt -> openStaffInventory());
        jButton4.addActionListener(evt -> openStaffStatistics());
        jButton5.addActionListener(evt -> openStaffHistory());
        jButton6.addActionListener(evt -> openStaffCashIn());
        jButton7.addActionListener(evt -> openStaffLandingPage());
        jButton8.addActionListener(evt -> refreshAndReset());
        jButton9.addActionListener(evt -> addVoucher());
        jButton10.addActionListener(evt -> updateVoucher());

        jTextField5.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applySearchFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applySearchFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applySearchFilter();
            }
        });
    }

    private void hideButtonsButKeepClickable() {
        makeButtonInvisible(jButton1);
        makeButtonInvisible(jButton2);
        makeButtonInvisible(jButton3);
        makeButtonInvisible(jButton4);
        makeButtonInvisible(jButton5);
        makeButtonInvisible(jButton6);
        makeButtonInvisible(jButton7);
        makeButtonInvisible(jButton8);
        makeButtonInvisible(jButton9);
        makeButtonInvisible(jButton10);
    }

    private static void makeButtonInvisible(javax.swing.JButton button) {
        button.setText("");
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
    }

    private void openStaffOrders() {
        FormNavigator.redirect(this, new StaffOrders());
    }

    private void openStaffInventory() {
        FormNavigator.redirect(this, new StaffInventory());
    }

    private void openStaffMenu() {
        try {
            FormNavigator.redirect(this, new StaffMenu());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to open Menu screen right now.", "Navigation error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openStaffStatistics() {
        try {
            FormNavigator.redirect(this, new StaffStatistics());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to open Statistics screen right now.", "Navigation error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openStaffHistory() {
        FormNavigator.redirect(this, new StaffHistory());
    }

    private void openStaffCashIn() {
        FormNavigator.redirect(this, new StaffCashIn());
    }

    private void openStaffLandingPage() {
        FormNavigator.redirect(this, new StaffLandingPage());
    }

    private void refreshAndReset() {
        refreshVoucherTable();
        clearVoucherInputs();
        selectedVoucherDocumentId = null;
    }

    private void refreshVoucherTable() {
        if (!FirebaseInitializer.isInitialized()) {
            voucherTableModel.setRowCount(0);
            return;
        }

        jButton8.setEnabled(false);
        javax.swing.SwingWorker<java.util.List<VoucherRow>, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected java.util.List<VoucherRow> doInBackground() throws Exception {
                Firestore firestore = FirebaseInitializer.getFirestore();
                CollectionReference vouchersCollection = firestore.collection(VOUCHERS_COLLECTION);
                ApiFuture<QuerySnapshot> future = vouchersCollection.get();
                QuerySnapshot snapshot = future.get();

                java.util.List<VoucherRow> rows = new java.util.ArrayList<>();
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    String voucherCode = safeText(readAsString(doc, "vouchercode", doc.getId()));
                    long discountPercent = readAsLong(doc, "discount_percent", 0L);
                    String expiryDate = safeText(readAsString(doc, "expiry_date", readAsString(doc, "expiryDate", "")));
                    long totalUses = readAsLong(doc, "total_uses", 0L);
                    rows.add(new VoucherRow(doc.getId(), voucherCode, discountPercent, expiryDate, totalUses));
                }
                rows.sort(java.util.Comparator.comparing(VoucherRow::voucherCode, String.CASE_INSENSITIVE_ORDER));
                return rows;
            }

            @Override
            protected void done() {
                try {
                    java.util.List<VoucherRow> rows = get();
                    voucherTableModel.setRowCount(0);
                    voucherCodeToDocumentId.clear();
                    for (VoucherRow row : rows) {
                        voucherTableModel.addRow(new Object[]{
                            row.voucherCode(),
                            Long.valueOf(row.discountPercent()),
                            row.expiryDate(),
                            Long.valueOf(row.totalUses())
                        });
                        voucherCodeToDocumentId.put(row.voucherCode(), row.documentId());
                    }
                    applySearchFilter();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    String message = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    JOptionPane.showMessageDialog(StaffVouchers.this, message, "Load vouchers failed", JOptionPane.ERROR_MESSAGE);
                } finally {
                    jButton8.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void populateFieldsFromSelectedRow() {
        int viewRow = voucherTable.getSelectedRow();
        if (viewRow < 0) {
            return;
        }

        int modelRow = voucherTable.convertRowIndexToModel(viewRow);
        String voucherCode = String.valueOf(voucherTableModel.getValueAt(modelRow, 0));
        Object discountPercent = voucherTableModel.getValueAt(modelRow, 1);
        String expiryDate = String.valueOf(voucherTableModel.getValueAt(modelRow, 2));
        Object totalUses = voucherTableModel.getValueAt(modelRow, 3);

        jTextField1.setText(voucherCode);
        jTextField2.setText(String.valueOf(discountPercent));
        jTextField3.setText(String.valueOf(totalUses));
        jTextField4.setText(expiryDate);
        selectedVoucherDocumentId = voucherCodeToDocumentId.getOrDefault(voucherCode, voucherCode);
    }

    private void addVoucher() {
        if (!FirebaseInitializer.isInitialized()) {
            JOptionPane.showMessageDialog(this, "Firebase is not initialized.", "Voucher", JOptionPane.WARNING_MESSAGE);
            return;
        }

        VoucherInput input = readVoucherInput();
        if (input == null) {
            return;
        }

        try {
            Firestore firestore = FirebaseInitializer.getFirestore();
            DocumentReference reference = firestore.collection(VOUCHERS_COLLECTION).document(input.voucherCode());
            DocumentSnapshot existing = reference.get().get();
            if (existing.exists()) {
                JOptionPane.showMessageDialog(this, "Voucher code already exists.", "Voucher", JOptionPane.WARNING_MESSAGE);
                return;
            }
            reference.set(input.toMap(null)).get();
            refreshAndReset();
            JOptionPane.showMessageDialog(this, "Voucher added successfully.", "Voucher", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Add voucher failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateVoucher() {
        if (!FirebaseInitializer.isInitialized()) {
            JOptionPane.showMessageDialog(this, "Firebase is not initialized.", "Voucher", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedVoucherDocumentId == null || selectedVoucherDocumentId.isBlank()) {
            JOptionPane.showMessageDialog(this, "Select a voucher row first.", "Voucher", JOptionPane.WARNING_MESSAGE);
            return;
        }

        VoucherInput input = readVoucherInput();
        if (input == null) {
            return;
        }

        try {
            Firestore firestore = FirebaseInitializer.getFirestore();
            DocumentReference selectedRef = firestore.collection(VOUCHERS_COLLECTION).document(selectedVoucherDocumentId);
            DocumentSnapshot selectedDoc = selectedRef.get().get();
            if (!selectedDoc.exists()) {
                JOptionPane.showMessageDialog(this, "Selected voucher no longer exists.", "Voucher", JOptionPane.WARNING_MESSAGE);
                refreshAndReset();
                return;
            }

            if (!selectedVoucherDocumentId.equalsIgnoreCase(input.voucherCode())) {
                jTextField1.setText(selectedVoucherDocumentId);
                JOptionPane.showMessageDialog(this, "Editing keeps the voucher code the same as the selected record.", "Voucher", JOptionPane.INFORMATION_MESSAGE);
            }

            selectedRef.set(input.toMap(selectedDoc.getBoolean("used")), SetOptions.merge()).get();

            refreshAndReset();
            JOptionPane.showMessageDialog(this, "Voucher updated successfully.", "Voucher", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Update voucher failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private VoucherInput readVoucherInput() {
        String voucherCode = jTextField1.getText() == null ? "" : jTextField1.getText().trim().toUpperCase();
        String discountText = jTextField2.getText() == null ? "" : jTextField2.getText().trim();
        String totalUsesText = jTextField3.getText() == null ? "" : jTextField3.getText().trim();
        String expiryDate = jTextField4.getText() == null ? "" : jTextField4.getText().trim();

        if (voucherCode.isEmpty() || discountText.isEmpty() || totalUsesText.isEmpty() || expiryDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill voucher code, discount percent, total uses, and expiry date.", "Voucher", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        long discountPercent;
        long totalUses;
        try {
            discountPercent = Long.parseLong(discountText);
            totalUses = Long.parseLong(totalUsesText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Discount percent and total uses must be whole numbers.", "Voucher", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        if (discountPercent < 0L || totalUses < 0L) {
            JOptionPane.showMessageDialog(this, "Discount percent and total uses cannot be negative.", "Voucher", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        return new VoucherInput(voucherCode, discountPercent, expiryDate, totalUses);
    }

    private void clearVoucherInputs() {
        jTextField1.setText("");
        jTextField2.setText("");
        jTextField3.setText("");
        jTextField4.setText("");
        voucherTable.clearSelection();
    }

    private void applySearchFilter() {
        String query = jTextField5.getText() == null ? "" : jTextField5.getText().trim();
        if (query.isEmpty()) {
            voucherSorter.setRowFilter(null);
            return;
        }
        voucherSorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(query)));
    }

    private static String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private static String readAsString(DocumentSnapshot snapshot, String fieldName, String fallback) {
        if (snapshot == null || !snapshot.contains(fieldName)) {
            return fallback;
        }
        Object value = snapshot.get(fieldName);
        if (value == null) {
            return fallback;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? fallback : text;
    }

    private static long readAsLong(DocumentSnapshot snapshot, String fieldName, long fallback) {
        if (snapshot == null || !snapshot.contains(fieldName)) {
            return fallback;
        }
        Object value = snapshot.get(fieldName);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text) {
            try {
                return Long.parseLong(text.trim());
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private void loadDesignTimePreviewRow() {
        voucherTableModel.setRowCount(0);
        voucherTableModel.addRow(new Object[]{"SAMPLE10", Long.valueOf(10L), "2026-12-31", Long.valueOf(50L)});
    }

    private static final class VoucherRow {
        private final String documentId;
        private final String voucherCode;
        private final long discountPercent;
        private final String expiryDate;
        private final long totalUses;

        private VoucherRow(String documentId, String voucherCode, long discountPercent, String expiryDate, long totalUses) {
            this.documentId = documentId;
            this.voucherCode = voucherCode;
            this.discountPercent = discountPercent;
            this.expiryDate = expiryDate;
            this.totalUses = totalUses;
        }

        private String documentId() {
            return documentId;
        }

        private String voucherCode() {
            return voucherCode;
        }

        private long discountPercent() {
            return discountPercent;
        }

        private String expiryDate() {
            return expiryDate;
        }

        private long totalUses() {
            return totalUses;
        }
    }

    private static final class VoucherInput {
        private final String voucherCode;
        private final long discountPercent;
        private final String expiryDate;
        private final long totalUses;

        private VoucherInput(String voucherCode, long discountPercent, String expiryDate, long totalUses) {
            this.voucherCode = voucherCode;
            this.discountPercent = discountPercent;
            this.expiryDate = expiryDate;
            this.totalUses = totalUses;
        }

        private String voucherCode() {
            return voucherCode;
        }

        private Map<String, Object> toMap(Boolean used) {
            Map<String, Object> map = new HashMap<>();
            map.put("vouchercode", voucherCode);
            map.put("discount_percent", Long.valueOf(discountPercent));
            map.put("expiry_date", expiryDate);
            map.put("expiryDate", expiryDate);
            map.put("total_uses", Long.valueOf(totalUses));
            map.put("used", used == null ? Boolean.FALSE : used);
            return map;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setText("jButton1");
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, 140, 50));

        jButton2.setText("jButton2");
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, 140, 60));

        jButton3.setText("jButton3");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 240, 140, 60));

        jButton4.setText("jButton4");
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 300, 140, 70));

        jButton5.setText("jButton5");
        getContentPane().add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 370, 140, 60));

        jButton6.setText("jButton6");
        getContentPane().add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 430, 140, 70));

        jButton7.setText("jButton7");
        getContentPane().add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 560, 100, 30));

        jButton8.setText("jButton8");
        getContentPane().add(jButton8, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 130, 110, 50));

        jButton9.setText("jButton9");
        getContentPane().add(jButton9, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 510, -1, 40));

        jButton10.setText("jButton10");
        getContentPane().add(jButton10, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 510, -1, 40));
        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 270, 440, 300));

        jTextField1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTextField1.setText("jTextField1");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });
        getContentPane().add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 290, 140, 30));

        jTextField2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTextField2.setText("jTextField2");
        getContentPane().add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 350, 140, 30));

        jTextField3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTextField3.setText("jTextField3");
        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });
        getContentPane().add(jTextField3, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 410, 140, 30));

        jTextField4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTextField4.setText("jTextField4");
        getContentPane().add(jTextField4, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 472, 140, 30));
        getContentPane().add(jTextField5, new org.netbeans.lib.awtextra.AbsoluteConstraints(231, 210, 340, 30));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/StaffDesign/StaffVouchers.png"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        openStaffMenu();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // no-op: total uses is handled by Add/Edit buttons
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(StaffVouchers.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(StaffVouchers.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(StaffVouchers.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(StaffVouchers.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new StaffVouchers().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    // End of variables declaration//GEN-END:variables
}
