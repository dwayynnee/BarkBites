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
import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 *
 * @author markd
 */
public class StaffInventory extends javax.swing.JFrame {

    private final StaffInventoryService inventoryService = new StaffInventoryService();
    private final DefaultListModel<StaffInventoryItem> inventoryModel = new DefaultListModel<>();
    private JList<StaffInventoryItem> inventoryList;
    private JScrollPane listScroll;
    private JTextField documentIdField;
    private JTextField nameField;
    private JTextField quantityField;
    private JTextField unitField;
    private JTextField imagePathField;
    private JCheckBox lowStockCheckBox;
    private JLabel titleLabel;
    private JLabel idLabel;
    private JLabel nameLabel;
    private JLabel quantityLabel;
    private JLabel unitLabel;
    private JLabel imageLabel;
    private JLabel statusLabel;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton newButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton deleteButton;

    /**
     * Creates new form StaffInventory
     */
    public StaffInventory() {
        initComponents();

        getContentPane().setComponentZOrder(jLabel1, getContentPane().getComponentCount() - 1);

        makeButtonInvisible(jButton1);
        makeButtonInvisible(jButton2);
        makeButtonInvisible(jButton3);
        makeButtonInvisible(jButton4);

        jButton1.addActionListener(evt -> openStaffOrders());
        jButton2.addActionListener(evt -> openStaffMenu());
        jButton3.addActionListener(evt -> openStaffStatistics());
        jButton4.addActionListener(evt -> logout());

        inventoryList.setModel(inventoryModel);
        listScroll.setViewportView(inventoryList);

        StaffFirebaseBootstrap.ensureInitialized(this);
        configureUi();
        loadInventoryAsync();

        this.setResizable(false);
    }

    private void configureUi() {
        inventoryList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(formatInventoryItem(value));
            label.setOpaque(true);
            label.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10));
            label.setBackground(isSelected ? new Color(23, 57, 122) : Color.WHITE);
            label.setForeground(isSelected ? Color.WHITE : Color.BLACK);
            return label;
        });
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
        FormNavigator.redirect(this, new StaffOrders());
    }
    private void openStaffMenu() {
        FormNavigator.redirect(this, new StaffMenu());
    }
    private void openStaffStatistics() {
        FormNavigator.redirect(this, new StaffStatistics());
    }
    private void logout() {
        FormNavigator.redirect(this, new StaffLandingPage());
    }

    private static void makeButtonInvisible(javax.swing.JButton button) {
        button.setText("");
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        inventoryList = new javax.swing.JList<>();
        titleLabel = new javax.swing.JLabel();
        listScroll = new javax.swing.JScrollPane();
        idLabel = new javax.swing.JLabel();
        documentIdField = new javax.swing.JTextField();
        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        quantityLabel = new javax.swing.JLabel();
        quantityField = new javax.swing.JTextField();
        unitLabel = new javax.swing.JLabel();
        unitField = new javax.swing.JTextField();
        imageLabel = new javax.swing.JLabel();
        imagePathField = new javax.swing.JTextField();
        lowStockCheckBox = new javax.swing.JCheckBox();
        refreshButton = new javax.swing.JButton();
        newButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        statusLabel = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        titleLabel.setFont(new java.awt.Font("Arial", 1, 22)); // NOI18N
        titleLabel.setText("Inventory Editor");
        getContentPane().add(titleLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(185, 105, 220, 28));
        getContentPane().add(listScroll, new org.netbeans.lib.awtextra.AbsoluteConstraints(185, 140, 320, 380));

        idLabel.setText("Document ID");
        getContentPane().add(idLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(535, 140, 120, 20));
        getContentPane().add(documentIdField, new org.netbeans.lib.awtextra.AbsoluteConstraints(535, 160, 220, 28));

        nameLabel.setText("Item Name");
        getContentPane().add(nameLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(535, 202, 100, 20));
        getContentPane().add(nameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(535, 222, 220, 28));

        quantityLabel.setText("Quantity");
        getContentPane().add(quantityLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(535, 264, 100, 20));
        getContentPane().add(quantityField, new org.netbeans.lib.awtextra.AbsoluteConstraints(535, 284, 220, 28));

        unitLabel.setText("Unit");
        getContentPane().add(unitLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(535, 326, 100, 20));
        getContentPane().add(unitField, new org.netbeans.lib.awtextra.AbsoluteConstraints(535, 346, 220, 28));

        imageLabel.setText("Image Path");
        getContentPane().add(imageLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(535, 388, 100, 20));
        getContentPane().add(imagePathField, new org.netbeans.lib.awtextra.AbsoluteConstraints(535, 408, 220, 28));

        lowStockCheckBox.setText("Low stock");
        getContentPane().add(lowStockCheckBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(535, 450, 120, 24));

        refreshButton.setText("Refresh");
        getContentPane().add(refreshButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(785, 160, 100, 32));

        newButton.setText("New");
        getContentPane().add(newButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(785, 205, 100, 32));

        saveButton.setText("Save");
        getContentPane().add(saveButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(785, 250, 100, 32));

        deleteButton.setText("Delete");
        getContentPane().add(deleteButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(785, 295, 100, 32));

        statusLabel.setText("Ready");
        getContentPane().add(statusLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(535, 490, 320, 22));

        jButton1.setText("jButton1");
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, 140, 70));

        jButton2.setText("jButton2");
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 340, 140, 70));

        jButton3.setText("jButton3");
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 440, 140, 80));

        jButton4.setText("jButton4");
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 550, 100, 30));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/StaffDesign/StaffInventory.png"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
            java.util.logging.Logger.getLogger(StaffInventory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(StaffInventory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(StaffInventory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(StaffInventory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
