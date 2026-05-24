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
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 *
 * @author markd
 */
public class StaffMenu extends javax.swing.JFrame {

    private final StaffMenuService menuService = new StaffMenuService();
    private final DefaultListModel<StaffMenuItem> menuItemsModel = new DefaultListModel<>();
    private JList<StaffMenuItem> menuItemsList;

    /**
     * Creates new form StaffMenu
     */
    public StaffMenu() {
        initComponents();

        menuItemsList = new JList<>();

        menuItemsList.setModel(menuItemsModel);

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

        statusLabel.setForeground(Color.WHITE);

        refreshButton.addActionListener(evt -> loadMenuItemsAsync());
        saveButton.addActionListener(evt -> saveMenuItem());
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
        titleField.setText(item.title());
        activeCheckBox.setSelected(item.active());
    }

    private void clearMenuForm() {
        titleField.setText("");
        activeCheckBox.setSelected(true);
        menuItemsList.clearSelection();
        statusLabel.setText("Ready to add a new menu item.");
    }

    private void saveMenuItem() {
        String title = titleField.getText() != null ? titleField.getText().trim() : "";
        if (title.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please enter a title.", "Missing title", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final String finalTitle = title;
        final boolean finalActive = activeCheckBox.isSelected();

        setBusy(true);
        javax.swing.SwingWorker<Void, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                menuService.upsertMenuItem(new StaffMenuItem(UUID.randomUUID().toString().substring(0, 8), finalTitle, "", 0L, "", finalActive));
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

    private void setBusy(boolean busy) {
        refreshButton.setEnabled(!busy);
        saveButton.setEnabled(!busy);
    }

    private static String formatMenuItem(StaffMenuItem item) {
        if (item == null) {
            return "";
        }
        return item.title() + (item.active() ? "" : " (inactive)");
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        getContentPane().add(titleField, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 230, 200, 28));

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
    private javax.swing.JButton StatisticsButton;
    private javax.swing.JCheckBox activeCheckBox;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JTextField titleField;
    // End of variables declaration//GEN-END:variables
}
