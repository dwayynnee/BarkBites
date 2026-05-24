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
import java.awt.Component;
import java.awt.Image;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 *
 * @author markd
 */
public class StaffMenu extends javax.swing.JFrame {

    private final StaffMenuService menuService = new StaffMenuService();
    private final List<StaffMenuItem> menuItems = new ArrayList<>();
    private String selectedMenuItemId;
    private final DecimalFormat priceFormat = new DecimalFormat("#,##0.00");
    private final JLabel statusLabel = new JLabel();

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
        menuService.seedDefaultMenuItemsIfMissing();
        configureCrudUi();
        makeButtonInvisible(refreshButton);
        makeButtonInvisible(saveButton);
        makeTextFieldInvisible(titleField);
        makeTextFieldInvisible(priceField);
        makeTextFieldInvisible(imagePathField);
        makeTextFieldInvisible(QuantityField);
        MenuCards.setOpaque(false);
        MenuCards.setVisible(true);
        getContentPane().setComponentZOrder(MenuCards, 0);
        loadMenuItemsAsync();

        this.setResizable(false);
    }

    private void configureCrudUi() {
        statusLabel.setForeground(Color.WHITE);
        hideCardImage(jLabel4);
        hideCardImage(jLabel8);
        hideCardImage(jLabel10);
        hideCardImage(jLabel13);

        refreshButton.addActionListener(evt -> loadMenuItemsAsync());
        saveButton.addActionListener(evt -> saveMenuItem());
        HistoryButton.setText("Delete");
        HistoryButton.addActionListener(evt -> deleteSelectedMenuItem());

        installCardClickTarget(jPanel1, 0);
        installCardClickTarget(jPanel2, 1);
        installCardClickTarget(jPanel3, 2);
        installCardClickTarget(jPanel4, 3);
        installCardClickTarget(jLabel1, 0);
        installCardClickTarget(jLabel2, 0);
        installCardClickTarget(jLabel3, 0);
        installCardClickTarget(jLabel4, 0);
        installCardClickTarget(jLabel5, 1);
        installCardClickTarget(jLabel6, 1);
        installCardClickTarget(jLabel7, 1);
        installCardClickTarget(jLabel8, 1);
        installCardClickTarget(jLabel9, 2);
        installCardClickTarget(jLabel10, 2);
        installCardClickTarget(jLabel11, 2);
        installCardClickTarget(jLabel12, 2);
        installCardClickTarget(jLabel13, 3);
        installCardClickTarget(jLabel14, 3);
        installCardClickTarget(jLabel15, 3);
        installCardClickTarget(jLabel16, 3);
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
                    menuItems.clear();
                    menuItems.addAll(get());
                    statusLabel.setText("Loaded " + menuItems.size() + " menu item(s).");
                    renderMenuCards();
                    if (selectedMenuItemId == null && !menuItems.isEmpty()) {
                        selectMenuItem(menuItems.get(0));
                    } else {
                        renderMenuCards();
                    }
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
        StaffMenuItem item = findSelectedMenuItem();
        if (item == null) {
            return;
        }
        titleField.setText(item.name());
        priceField.setText(Long.toString(item.priceCents()));
        QuantityField.setText(Integer.toString(item.quantity()));
        imagePathField.setText(item.imagePath());
    }

    private void clearMenuForm() {
        titleField.setText("");
        priceField.setText("");
        QuantityField.setText("");
        imagePathField.setText("");
        selectedMenuItemId = null;
        renderMenuCards();
        statusLabel.setText("Ready to add a new menu item.");
    }

    private void saveMenuItem() {
        String name = titleField.getText() != null ? titleField.getText().trim() : "";
        String priceText = priceField.getText() != null ? priceField.getText().trim() : "";
        String quantityText = QuantityField.getText() != null ? QuantityField.getText().trim() : "";
        String imagePath = imagePathField.getText() != null ? imagePathField.getText().trim() : "";

        if (name.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please enter a name.", "Missing name", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long priceCents;
        try {
            priceCents = Long.parseLong(priceText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid price in cents.", "Invalid price", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid quantity.", "Invalid quantity", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final String finalId = selectedMenuItemId != null && !selectedMenuItemId.isBlank()
                ? selectedMenuItemId
                : UUID.randomUUID().toString().substring(0, 8);
        final String finalName = name;
        final long finalPriceCents = priceCents;
        final int finalQuantity = quantity;
        final String finalImagePath = imagePath;

        setBusy(true);
        javax.swing.SwingWorker<Void, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                menuService.upsertMenuItem(new StaffMenuItem(finalId, finalName, finalPriceCents, finalQuantity, finalImagePath));
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    selectedMenuItemId = finalId;
                    statusLabel.setText("Saved menu item '" + finalName + "'.");
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

    private void deleteSelectedMenuItem() {
        StaffMenuItem selectedItem = findSelectedMenuItem();
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Select a menu card first.", "Nothing selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete '" + selectedItem.name() + "'?", "Delete menu item", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        setBusy(true);
        javax.swing.SwingWorker<Void, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                menuService.deleteMenuItem(selectedItem.id());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    clearMenuForm();
                    loadMenuItemsAsync();
                    statusLabel.setText("Deleted menu item '" + selectedItem.name() + "'.");
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
        saveButton.setEnabled(!busy);
        HistoryButton.setEnabled(!busy);
    }

    private void renderMenuCards() {
        StaffMenuItem[] items = new StaffMenuItem[] {
            menuItems.size() > 0 ? menuItems.get(0) : null,
            menuItems.size() > 1 ? menuItems.get(1) : null,
            menuItems.size() > 2 ? menuItems.get(2) : null,
            menuItems.size() > 3 ? menuItems.get(3) : null
        };

        applyCard(items[0], jPanel1, jLabel1, jLabel2, jLabel3, jLabel4, 0);
        applyCard(items[1], jPanel2, jLabel6, jLabel5, jLabel7, jLabel8, 1);
        applyCard(items[2], jPanel3, jLabel11, jLabel12, jLabel9, jLabel10, 2);
        applyCard(items[3], jPanel4, jLabel14, jLabel15, jLabel16, jLabel13, 3);
    }

    private void applyCard(StaffMenuItem item, javax.swing.JPanel panel, JLabel nameLabel, JLabel priceLabel, JLabel quantityLabel, JLabel imageLabel, int index) {
        panel.setOpaque(false);
        panel.setBackground(new Color(0, 0, 0, 0));

        if (item == null) {
            nameLabel.setText(index == 0 ? "Name" : index == 1 ? "Name" : index == 2 ? "Name" : "Name");
            priceLabel.setText("Price");
            quantityLabel.setText("Quantity");
            imageLabel.setIcon(null);
            imageLabel.setText("");
            panel.setBorder(null);
            return;
        }

        nameLabel.setText(item.name());
        priceLabel.setText(formatPrice(item.priceCents()));
        quantityLabel.setText(Integer.toString(item.quantity()));

        ImageIcon icon = loadMenuImage(item.imagePath(), imageLabel.getWidth(), imageLabel.getHeight());
        if (icon != null) {
            imageLabel.setIcon(icon);
        } else {
            imageLabel.setIcon(null);
            imageLabel.setText("");
        }

        if (item.id().equals(selectedMenuItemId)) {
            panel.setBorder(null);
        } else {
            panel.setBorder(null);
        }
    }

    private static void hideCardImage(JLabel label) {
        label.setVisible(false);
        label.setText("");
        label.setIcon(null);
    }

    private void installCardClickTarget(Component component, int itemIndex) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (itemIndex < menuItems.size()) {
                    selectMenuItem(menuItems.get(itemIndex));
                }
            }
        });
    }

    private void selectMenuItem(StaffMenuItem item) {
        if (item == null) {
            return;
        }
        selectedMenuItemId = item.id();
        titleField.setText(item.name());
        priceField.setText(Long.toString(item.priceCents()));
        QuantityField.setText(Integer.toString(item.quantity()));
        imagePathField.setText(item.imagePath());
        statusLabel.setText("Selected '" + item.name() + "'.");
        renderMenuCards();
    }

    private StaffMenuItem findSelectedMenuItem() {
        if (selectedMenuItemId == null || selectedMenuItemId.isBlank()) {
            return null;
        }
        for (StaffMenuItem item : menuItems) {
            if (selectedMenuItemId.equals(item.id())) {
                return item;
            }
        }
        return null;
    }

    private String formatPrice(long priceCents) {
        return "₱" + priceFormat.format(priceCents / 100.0d);
    }

    private ImageIcon loadMenuImage(String imagePath, int width, int height) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }

        URL resource = getClass().getResource(imagePath.startsWith("/") ? imagePath : "/" + imagePath);
        Image sourceImage = null;
        if (resource != null) {
            sourceImage = new ImageIcon(resource).getImage();
        } else {
            File file = new File(imagePath);
            if (file.exists()) {
                sourceImage = new ImageIcon(file.getAbsolutePath()).getImage();
            }
        }

        if (sourceImage == null) {
            return null;
        }

        int targetWidth = width > 0 ? width : 120;
        int targetHeight = height > 0 ? height : 80;
        Image scaled = sourceImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
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

    private static void makeTextFieldInvisible(JTextField textField) {
        textField.setOpaque(false);
        textField.setBorder(BorderFactory.createEmptyBorder());
        textField.setBackground(new Color(0, 0, 0, 0));
        textField.setColumns(textField.getColumns());
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
        imagePathField = new javax.swing.JTextField();
        refreshButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        OrdersButton = new javax.swing.JButton();
        InventoryButton = new javax.swing.JButton();
        StatisticsButton = new javax.swing.JButton();
        LogoutButton = new javax.swing.JButton();
        HistoryButton = new javax.swing.JButton();
        QuantityField = new javax.swing.JTextField();
        MenuCards = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        BG = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        getContentPane().add(titleField, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 230, 190, 28));
        getContentPane().add(priceField, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 290, 190, 40));
        getContentPane().add(imagePathField, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 420, 190, 28));

        refreshButton.setText("Refresh");
        getContentPane().add(refreshButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 512, 110, 50));

        saveButton.setText("Update");
        getContentPane().add(saveButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 510, 100, 50));

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

        QuantityField.setText("jTextField1");
        getContentPane().add(QuantityField, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 360, 190, 30));

        MenuCards.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setText("jLabel4");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, 130, 60));

        jLabel2.setText("jLabel2");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 156, 110, 20));

        jLabel3.setText("jLabel3");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 176, 100, -1));

        jLabel1.setText("jLabel1");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 136, 110, 20));

        MenuCards.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 190, 200));

        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel8.setText("jLabel8");
        jPanel2.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(37, 56, 120, 70));

        jLabel7.setText("jLabel7");
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(97, 176, 50, 20));

        jLabel6.setText("jLabel6");
        jPanel2.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(77, 136, 90, 20));

        jLabel5.setText("jLabel5");
        jPanel2.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(67, 156, 60, -1));

        MenuCards.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 0, 190, 200));

        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel9.setText("jLabel9");
        jPanel3.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 180, 80, 20));

        jLabel10.setText("jLabel10");
        jPanel3.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(33, 56, 120, 70));

        jLabel11.setText("jLabel11");
        jPanel3.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 140, 100, 20));

        jLabel12.setText("jLabel12");
        jPanel3.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 160, 100, -1));

        MenuCards.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 230, 190, 210));

        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel13.setText("jLabel13");
        jPanel4.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 60, 120, 60));

        jLabel14.setText("jLabel14");
        jPanel4.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(73, 140, 100, 20));

        jLabel15.setText("jLabel15");
        jPanel4.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 160, 100, 20));

        jLabel16.setText("jLabel16");
        jPanel4.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 180, 80, -1));

        MenuCards.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 230, 190, 210));

        getContentPane().add(MenuCards, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 130, 400, 440));

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
    private javax.swing.JPanel MenuCards;
    private javax.swing.JButton OrdersButton;
    private javax.swing.JTextField QuantityField;
    private javax.swing.JButton StatisticsButton;
    private javax.swing.JTextField imagePathField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTextField priceField;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JTextField titleField;
    // End of variables declaration//GEN-END:variables
}
