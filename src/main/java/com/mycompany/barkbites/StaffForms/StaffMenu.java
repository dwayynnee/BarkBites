/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.barkbites.StaffForms;

import com.mycompany.barkbites.FormNavigator;
import com.mycompany.barkbites.data.staff.StaffFirebaseBootstrap;
import com.mycompany.barkbites.data.staff.StaffMenuItem;
import com.mycompany.barkbites.data.staff.StaffMenuService;
import java.awt.Color;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author markd
 */
public class StaffMenu extends javax.swing.JFrame {

    private final StaffMenuService menuService = new StaffMenuService();
    private final DefaultListModel<StaffMenuItem> menuItemsModel = new DefaultListModel<>();
    private JList<StaffMenuItem> menuItemsList;
    private JScrollPane menuItemsScroll;
    private JTextField documentIdField;
    private JTextField titleField;
    private JTextField priceField;
    private JTextField imagePathField;
    private JTextArea descriptionArea;
    private JScrollPane descriptionScroll;
    private JCheckBox activeCheckBox;
    private JLabel formTitle;
    private JLabel idLabel;
    private JLabel titleLabel;
    private JLabel priceLabel;
    private JLabel imageLabel;
    private JLabel descriptionLabel;
    private JLabel statusLabel;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton newButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton deleteButton;

    /**
     * Creates new form StaffMenu
     */
    public StaffMenu() {
        initComponents();

        getContentPane().setComponentZOrder(BG, getContentPane().getComponentCount() - 1);

        makeButtonInvisible(OrdersButton);
        makeButtonInvisible(InventoryButton);
        makeButtonInvisible(StatisticsButton);
        makeButtonInvisible(LogoutButton);

        OrdersButton.addActionListener(evt -> openStaffOrders());
        InventoryButton.addActionListener(evt -> openStaffInventory());
        StatisticsButton.addActionListener(evt -> openStaffStatistics());
        LogoutButton.addActionListener(evt -> openStaffLandingPage());

        StaffFirebaseBootstrap.ensureInitialized(this);
        configureCrudUi();
        loadMenuItemsAsync();

        this.setResizable(false);
    }

    private void configureCrudUi() {
        menuItemsList.setFont(new Font("Arial", Font.PLAIN, 14));
        menuItemsList.setCellRenderer((JList<? extends StaffMenuItem> list, StaffMenuItem value, int index, boolean isSelected, boolean cellHasFocus) -> {
            JLabel label = new JLabel(formatMenuItem(value));
            label.setOpaque(true);
            label.setFont(new Font("Arial", Font.PLAIN, 13));
            label.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10));
            label.setBackground(isSelected ? new Color(23, 57, 122) : Color.WHITE);
            label.setForeground(isSelected ? Color.WHITE : new Color(25, 25, 25));
            return label;
        });
        menuItemsList.addListSelectionListener(evt -> {
            if (!evt.getValueIsAdjusting()) {
                populateFormFromSelection();
            }
        });

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        statusLabel.setForeground(Color.WHITE);

        refreshButton.addActionListener(evt -> loadMenuItemsAsync());
        newButton.addActionListener(evt -> clearMenuForm());
        saveButton.addActionListener(evt -> saveMenuItem());
        deleteButton.addActionListener(evt -> deleteMenuItem());
    }

    private void loadMenuItemsAsync() {
        setBusy(true);
        javax.swing.SwingWorker<java.util.List<StaffMenuItem>, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected java.util.List<StaffMenuItem> doInBackground() {
                return menuService.listMenuItems();
            }

            @Override
            protected void done() {
                try {
                    menuItemsModel.clear();
                    for (StaffMenuItem item : get()) {
                        menuItemsModel.addElement(item);
                    }
                    statusLabel.setText("Loaded " + menuItemsModel.size() + " menu item(s).");
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Menu load interrupted.");
                } catch (ExecutionException ee) {
                    String message = ee.getCause() != null ? ee.getCause().getMessage() : ee.getMessage();
                    statusLabel.setText("Menu load failed.");
                    JOptionPane.showMessageDialog(StaffMenu.this, message, "Menu load failed", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setBusy(false);
                }
            }
        };
        worker.execute();
    }

    private void populateFormFromSelection() {
        StaffMenuItem item = menuItemsList.getSelectedValue();
        if (item == null) {
            return;
        }
        documentIdField.setText(item.id());
        titleField.setText(item.title());
        priceField.setText(formatPesos(item.priceCents()));
        imagePathField.setText(item.imagePath());
        descriptionArea.setText(item.description());
        activeCheckBox.setSelected(item.active());
    }

    private void clearMenuForm() {
        documentIdField.setText(generateDocumentId());
        titleField.setText("");
        priceField.setText("0.00");
        imagePathField.setText("");
        descriptionArea.setText("");
        activeCheckBox.setSelected(true);
        menuItemsList.clearSelection();
        statusLabel.setText("Ready to add a new menu item.");
    }

    private void saveMenuItem() {
        String id = documentIdField.getText() != null ? documentIdField.getText().trim() : "";
        String title = titleField.getText() != null ? titleField.getText().trim() : "";
        String priceText = priceField.getText() != null ? priceField.getText().trim() : "";
        String imagePath = imagePathField.getText() != null ? imagePathField.getText().trim() : "";
        String description = descriptionArea.getText() != null ? descriptionArea.getText().trim() : "";

        if (id.isBlank()) {
            id = generateDocumentId();
            documentIdField.setText(id);
        }
        if (title.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please enter a title.", "Missing title", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long priceCents;
        try {
            priceCents = parsePesos(priceText);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid price.", "Invalid price", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final String finalId = id;
        final String finalTitle = title;
        final String finalDescription = description;
        final long finalPriceCents = priceCents;
        final String finalImagePath = imagePath;
        final boolean finalActive = activeCheckBox.isSelected();

        setBusy(true);
        javax.swing.SwingWorker<Void, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                menuService.upsertMenuItem(new StaffMenuItem(finalId, finalTitle, finalDescription, finalPriceCents, finalImagePath, finalActive));
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("Saved menu item '" + finalTitle + "'.");
                    loadMenuItemsAsync();
                } catch (Exception ex) {
                    String message = ex.getMessage() != null ? ex.getMessage() : "Failed to save menu item.";
                    JOptionPane.showMessageDialog(StaffMenu.this, message, "Save failed", JOptionPane.ERROR_MESSAGE);
                    setBusy(false);
                }
            }
        };
        worker.execute();
    }

    private void deleteMenuItem() {
        String id = documentIdField.getText() != null ? documentIdField.getText().trim() : "";
        if (id.isBlank()) {
            JOptionPane.showMessageDialog(this, "Select or enter a document ID first.", "Missing document ID", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete menu item '" + id + "'?", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        setBusy(true);
        javax.swing.SwingWorker<Void, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                menuService.deleteMenuItem(id);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    clearMenuForm();
                    statusLabel.setText("Deleted menu item '" + id + "'.");
                    loadMenuItemsAsync();
                } catch (Exception ex) {
                    String message = ex.getMessage() != null ? ex.getMessage() : "Failed to delete menu item.";
                    JOptionPane.showMessageDialog(StaffMenu.this, message, "Delete failed", JOptionPane.ERROR_MESSAGE);
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

    private static String formatMenuItem(StaffMenuItem item) {
        if (item == null) {
            return "";
        }
        return item.title() + "  •  ₱" + String.format(java.util.Locale.US, "%,.2f", item.priceCents() / 100.0) + (item.active() ? "" : " (inactive)");
    }

    private static String generateDocumentId() {
        return "menu-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private static long parsePesos(String text) {
        if (text == null || text.isBlank()) {
            return 0L;
        }
        BigDecimal value = new BigDecimal(text.trim());
        return value.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private static String formatPesos(long cents) {
        return String.format(java.util.Locale.US, "%.2f", cents / 100.0);
    }

    private void openStaffOrders() {
        FormNavigator.redirect(this, new StaffOrders());
    }

    private void openStaffInventory() {
        FormNavigator.redirect(this, new StaffInventory());
    }

    private void openStaffStatistics() {
        FormNavigator.redirect(this, new StaffStatistics());
    }

    private void openStaffLandingPage() {
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

        titleField = new javax.swing.JTextField();
        priceField = new javax.swing.JTextField();
        imageLabel = new javax.swing.JLabel();
        imagePathField = new javax.swing.JTextField();
        activeCheckBox = new javax.swing.JCheckBox();
        refreshButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        statusLabel = new javax.swing.JLabel();
        OrdersButton = new javax.swing.JButton();
        InventoryButton = new javax.swing.JButton();
        StatisticsButton = new javax.swing.JButton();
        LogoutButton = new javax.swing.JButton();
        HistoryButton = new javax.swing.JButton();
        BG = new javax.swing.JLabel();
        QuantityField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        getContentPane().add(titleField, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 230, 200, 28));
        getContentPane().add(priceField, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 300, 200, 30));

        imageLabel.setText("Image Path");
        getContentPane().add(imageLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(455, 335, 100, 20));
        getContentPane().add(imagePathField, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 420, 210, 28));

        activeCheckBox.setSelected(true);
        activeCheckBox.setText("Active");
        getContentPane().add(activeCheckBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(455, 515, 90, 24));

        refreshButton.setText("Refresh");
        getContentPane().add(refreshButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 512, 110, 50));

        saveButton.setText("Update");
        getContentPane().add(saveButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 510, 90, 50));

        statusLabel.setText("Ready");
        getContentPane().add(statusLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(455, 545, 360, 22));

        OrdersButton.setText("jButton1");
        getContentPane().add(OrdersButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, 140, 60));

        InventoryButton.setText("jButton2");
        getContentPane().add(InventoryButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 210, 140, 70));

        StatisticsButton.setText("jButton3");
        getContentPane().add(StatisticsButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 370, 140, 80));

        LogoutButton.setText("jButton4");
        getContentPane().add(LogoutButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 550, 100, 30));

        HistoryButton.setText("jButton1");
        getContentPane().add(HistoryButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 460, 140, 70));

        BG.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/StaffDesign/StaffMenu.png"))); // NOI18N
        getContentPane().add(BG, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        QuantityField.setText("jTextField1");
        getContentPane().add(QuantityField, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 360, 190, 30));

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
            java.util.logging.Logger.getLogger(StaffMenu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(StaffMenu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(StaffMenu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(StaffMenu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new StaffMenu().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel BG;
    private javax.swing.JButton HistoryButton;
    private javax.swing.JButton InventoryButton;
    private javax.swing.JButton LogoutButton;
    private javax.swing.JButton OrdersButton;
    private javax.swing.JTextField QuantityField;
    private javax.swing.JButton StatisticsButton;
    private javax.swing.JCheckBox activeCheckBox;
    private javax.swing.JLabel imageLabel;
    private javax.swing.JTextField imagePathField;
    private javax.swing.JTextField priceField;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JTextField titleField;
    // End of variables declaration//GEN-END:variables
}
