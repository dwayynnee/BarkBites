/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.barkbites.StaffForms;

import com.mycompany.barkbites.FormNavigator;
import com.mycompany.barkbites.data.staff.StaffInventoryItem;
import com.mycompany.barkbites.data.staff.StaffInventoryService;
import com.mycompany.barkbites.data.staff.StaffFirebaseBootstrap;
import java.awt.Color;
import java.awt.Dimension;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;

/**
 * OOP used here:
 * Encapsulation keeps inventory state and helpers inside the form.
 * Abstraction wraps CRUD behavior behind simple action methods.
 * Inheritance comes from extending JFrame.
 * Polymorphism appears in the event listeners and list renderer callbacks.
 *
 * @author markd
 */
public class StaffInventory extends javax.swing.JFrame {

    private final StaffInventoryService inventoryService = new StaffInventoryService();
    private final DefaultListModel<StaffInventoryItem> inventoryModel = new DefaultListModel<>();
    private JList<StaffInventoryItem> inventoryList = new JList<>();
    // additional fields expected by the logic but not present in the GUI builder block
    private javax.swing.JTextField documentIdField;
    private javax.swing.JTextField unitField;
    private javax.swing.JCheckBox lowStockCheckBox;
    private javax.swing.JLabel statusLabel;

    /**
     * Creates new form StaffInventory
     */
    public StaffInventory() {
        initComponents();

        // Action: create hidden helper fields used by the inventory logic.
        if (documentIdField == null) {
            documentIdField = new javax.swing.JTextField();
            documentIdField.setVisible(false);
            getContentPane().add(documentIdField, new org.netbeans.lib.awtextra.AbsoluteConstraints(0,0,0,0));
        }
        if (unitField == null) {
            unitField = new javax.swing.JTextField();
            unitField.setVisible(false);
            getContentPane().add(unitField, new org.netbeans.lib.awtextra.AbsoluteConstraints(0,0,0,0));
        }
        if (lowStockCheckBox == null) {
            lowStockCheckBox = new javax.swing.JCheckBox();
            lowStockCheckBox.setVisible(false);
            getContentPane().add(lowStockCheckBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(0,0,0,0));
        }
        if (statusLabel == null) {
            statusLabel = new javax.swing.JLabel();
            statusLabel.setVisible(false);
            getContentPane().add(statusLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0,0,0,0));
        }

        getContentPane().setComponentZOrder(BG, getContentPane().getComponentCount() - 1);

        makeButtonInvisible(OrdersButton);
        makeButtonInvisible(MenuButton);
        makeButtonInvisible(InventoryButton);
        makeButtonInvisible(LogoutButton);
        makeButtonInvisible(HistoryButton);
        makeButtonInvisible(CashIn);
        makeButtonInvisible(newButton);
        makeButtonInvisible(saveButton);
        makeButtonInvisible(deleteButton);
        makeButtonInvisible(refreshButton);
        makeButtonInvisible(jButton2);

        makeFieldBackgroundInvisible(nameField);
        makeFieldBackgroundInvisible(quantityField);
        makeFieldBackgroundInvisible(imagePathField);

        // Action: connect the navigation buttons to their target screens.
        OrdersButton.addActionListener(evt -> openStaffOrders());
        MenuButton.addActionListener(evt -> openStaffMenu());
        InventoryButton.addActionListener(evt -> openStaffStatistics());
        LogoutButton.addActionListener(evt -> logout());
        jButton2.addActionListener(evt -> openStaffVouchers());
        HistoryButton.addActionListener(evt -> openStaffHistory());
        CashIn.addActionListener(evt -> openStaffCashIn());

        inventoryList.setModel(inventoryModel);
        listScroll.setViewportView(inventoryList);

        boolean firebaseReady = true;
        if (!java.beans.Beans.isDesignTime()) {
            firebaseReady = StaffFirebaseBootstrap.ensureInitialized(this);
        }
        configureUi();
        if (firebaseReady) {
            loadInventoryAsync();
        }

        this.setResizable(false);
    }

    private void configureUi() {
        // Action: give the inventory list a more polished container treatment.
        listScroll.setOpaque(false);
        listScroll.getViewport().setOpaque(false);
        listScroll.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(23, 57, 122), 2),
                javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));
        listScroll.getVerticalScrollBar().setUnitIncrement(16);
        listScroll.getVerticalScrollBar().setPreferredSize(new Dimension(14, 0));
        listScroll.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(23, 57, 122);
                this.trackColor = new Color(236, 241, 248);
            }

            @Override
            protected javax.swing.JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected javax.swing.JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private javax.swing.JButton createZeroButton() {
                javax.swing.JButton button = new javax.swing.JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });

        // Action: render inventory rows with a clean, readable layout.
        inventoryList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(formatInventoryItem(value));
            label.setOpaque(true);
            label.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(223, 230, 240)),
                    javax.swing.BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));
            label.setBackground(isSelected ? new Color(23, 57, 122) : Color.WHITE);
            label.setForeground(isSelected ? Color.WHITE : Color.BLACK);
            return label;
        });
        inventoryList.setBackground(Color.WHITE);
        inventoryList.setSelectionBackground(new Color(23, 57, 122));
        inventoryList.setSelectionForeground(Color.WHITE);
        inventoryList.setFixedCellHeight(42);
        inventoryList.addListSelectionListener(evt -> {
            if (!evt.getValueIsAdjusting()) {
                populateFormFromSelection();
            }
        });

        refreshButton.addActionListener(evt -> loadInventoryAsync());
        newButton.addActionListener(evt -> clearForm());
        saveButton.addActionListener(evt -> saveInventoryItem());
        deleteButton.addActionListener(evt -> deleteInventoryItem());
    }

    private void loadInventoryAsync() {
        setBusy(true);
        javax.swing.SwingWorker<java.util.List<StaffInventoryItem>, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected java.util.List<StaffInventoryItem> doInBackground() {
                return inventoryService.listInventoryItems();
            }

            @Override
            protected void done() {
                try {
                    inventoryModel.clear();
                    for (StaffInventoryItem item : get()) {
                        inventoryModel.addElement(item);
                    }
                    statusLabel.setText("Loaded " + inventoryModel.size() + " inventory item(s).");
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Inventory load interrupted.");
                } catch (ExecutionException ee) {
                    String message = ee.getCause() != null ? ee.getCause().getMessage() : ee.getMessage();
                    JOptionPane.showMessageDialog(StaffInventory.this, message, "Inventory load failed", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setBusy(false);
                }
            }
        };
        worker.execute();
    }

    private void populateFormFromSelection() {
        StaffInventoryItem item = inventoryList.getSelectedValue();
        if (item == null) {
            return;
        }
        documentIdField.setText(item.id());
        nameField.setText(item.name());
        quantityField.setText(Integer.toString(item.quantity()));
        unitField.setText(item.unit());
        imagePathField.setText(item.imagePath());
        lowStockCheckBox.setSelected(item.quantity() <= 5);
    }

    private void clearForm() {
        documentIdField.setText(generateDocumentId());
        nameField.setText("");
        quantityField.setText("0");
        unitField.setText("pcs");
        imagePathField.setText("");
        lowStockCheckBox.setSelected(false);
        inventoryList.clearSelection();
        statusLabel.setText("Ready to add a new inventory item.");
    }

    private void saveInventoryItem() {
        String id = documentIdField.getText() != null ? documentIdField.getText().trim() : "";
        String name = nameField.getText() != null ? nameField.getText().trim() : "";
        String quantityText = quantityField.getText() != null ? quantityField.getText().trim() : "";
        String unit = unitField.getText() != null ? unitField.getText().trim() : "";
        String imagePath = imagePathField.getText() != null ? imagePathField.getText().trim() : "";

        if (id.isBlank()) {
            id = generateDocumentId();
            documentIdField.setText(id);
        }
        if (name.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please enter an item name.", "Missing item name", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid quantity.", "Invalid quantity", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (unit.isBlank()) {
            unit = "pcs";
            unitField.setText(unit);
        }

        StaffInventoryItem item = new StaffInventoryItem(id, name, quantity, unit, imagePath);
        setBusy(true);
        javax.swing.SwingWorker<Void, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                inventoryService.upsertInventoryItem(item);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("Saved inventory item '" + name + "'.");
                    loadInventoryAsync();
                } catch (Exception ex) {
                    String message = ex.getMessage() != null ? ex.getMessage() : "Failed to save inventory item.";
                    JOptionPane.showMessageDialog(StaffInventory.this, message, "Save failed", JOptionPane.ERROR_MESSAGE);
                    setBusy(false);
                }
            }
        };
        worker.execute();
    }

    private void deleteInventoryItem() {
        String id = documentIdField.getText() != null ? documentIdField.getText().trim() : "";
        if (id.isBlank()) {
            JOptionPane.showMessageDialog(this, "Select or enter a document ID first.", "Missing document ID", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete inventory item '" + id + "'?", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        setBusy(true);
        javax.swing.SwingWorker<Void, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                inventoryService.deleteInventoryItem(id);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    clearForm();
                    statusLabel.setText("Deleted inventory item '" + id + "'.");
                    loadInventoryAsync();
                } catch (Exception ex) {
                    String message = ex.getMessage() != null ? ex.getMessage() : "Failed to delete inventory item.";
                    JOptionPane.showMessageDialog(StaffInventory.this, message, "Delete failed", JOptionPane.ERROR_MESSAGE);
                    setBusy(false);
                }
            }
        };
        worker.execute();
    }

    private void setBusy(boolean busy) {
        refreshButton.setEnabled(!busy);
        newButton.setEnabled(!busy);
        saveButton.setEnabled(!busy);
        deleteButton.setEnabled(!busy);
    }

    private static String generateDocumentId() {
        return "inv-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private static String formatInventoryItem(StaffInventoryItem item) {
        if (item == null) {
            return "";
        }
        return item.name() + "  •  " + item.quantity() + " " + item.unit();
    }

    private void openStaffOrders() {
        // Action: open the Orders screen.
        FormNavigator.redirect(this, new StaffOrders());
    }
    private void openStaffMenu() {
        // Action: open the Menu screen.
        FormNavigator.redirect(this, new StaffMenu());
    }
    private void openStaffStatistics() {
        // Action: open the Statistics screen.
        FormNavigator.redirect(this, new StaffStatistics());
    }

    private void openStaffHistory() {
        // Action: open the History screen.
        FormNavigator.redirect(this, new StaffHistory());
    }
    
        private void openStaffVouchers() {
        // Action: open the History screen.
        FormNavigator.redirect(this, new StaffVouchers());
    }

    private void openStaffCashIn() {
        // Action: open the Cash In screen.
        FormNavigator.redirect(this, new StaffCashIn());
    }

    private void logout() {
        // Action: return to the landing page.
        FormNavigator.redirect(this, new StaffLandingPage());
    }

    private static void makeButtonInvisible(javax.swing.JButton button) {
        // Keeps the button clickable while removing the visible chrome.
        button.setText("");
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
    }

    private static void makeFieldBackgroundInvisible(javax.swing.JTextField field) {
        // Keeps the field editable while blending it into the background.
        field.setOpaque(false);
        field.setBackground(new Color(0, 0, 0, 0));
        field.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        field.setEditable(true);
        field.setFocusable(true);
        field.setCaretColor(field.getForeground());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        listScroll = new javax.swing.JScrollPane();
        nameField = new javax.swing.JTextField();
        quantityField = new javax.swing.JTextField();
        imagePathField = new javax.swing.JTextField();
        refreshButton = new javax.swing.JButton();
        newButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        OrdersButton = new javax.swing.JButton();
        MenuButton = new javax.swing.JButton();
        InventoryButton = new javax.swing.JButton();
        LogoutButton = new javax.swing.JButton();
        HistoryButton = new javax.swing.JButton();
        CashIn = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        BG = new javax.swing.JLabel();

        jButton1.setText("jButton1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        getContentPane().add(listScroll, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 190, 380, 370));

        nameField.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        nameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameFieldActionPerformed(evt);
            }
        });
        getContentPane().add(nameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 288, 190, 30));

        quantityField.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        getContentPane().add(quantityField, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 340, 190, 30));

        imagePathField.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        getContentPane().add(imagePathField, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 400, 190, 30));

        refreshButton.setText("Refresh");
        getContentPane().add(refreshButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 480, 100, 50));

        newButton.setText("New");
        getContentPane().add(newButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 482, 100, 50));

        saveButton.setText("Save");
        getContentPane().add(saveButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 532, 100, 40));

        deleteButton.setText("Delete");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });
        getContentPane().add(deleteButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 532, 100, 40));

        OrdersButton.setText("jButton1");
        getContentPane().add(OrdersButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 100, 140, 70));

        MenuButton.setText("jButton2");
        getContentPane().add(MenuButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 230, 140, 70));

        InventoryButton.setText("jButton3");
        getContentPane().add(InventoryButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 300, 140, 60));

        LogoutButton.setText("jButton4");
        getContentPane().add(LogoutButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 560, 100, 30));

        HistoryButton.setText("jButton1");
        HistoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HistoryButtonActionPerformed(evt);
            }
        });
        getContentPane().add(HistoryButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 360, 140, 60));

        CashIn.setText("jButton1");
        getContentPane().add(CashIn, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 430, 140, 60));

        jButton2.setText("jButton2");
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 490, 140, 60));

        BG.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/StaffDesign/StaffInventory.png"))); // NOI18N
        getContentPane().add(BG, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void nameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        // Action handled through the form controls.
    }//GEN-LAST:event_nameFieldActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        // Action handled through the form controls.
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void HistoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HistoryButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_HistoryButtonActionPerformed

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
        } catch (InstantiationException ex) {
        } catch (IllegalAccessException ex) {
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new StaffInventory().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel BG;
    private javax.swing.JButton CashIn;
    private javax.swing.JButton HistoryButton;
    private javax.swing.JButton InventoryButton;
    private javax.swing.JButton LogoutButton;
    private javax.swing.JButton MenuButton;
    private javax.swing.JButton OrdersButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JTextField imagePathField;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JScrollPane listScroll;
    private javax.swing.JTextField nameField;
    private javax.swing.JButton newButton;
    private javax.swing.JTextField quantityField;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables
}
