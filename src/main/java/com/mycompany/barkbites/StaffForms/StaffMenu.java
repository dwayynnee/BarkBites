/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.barkbites.StaffForms;

import com.mycompany.barkbites.FormNavigator;
import com.mycompany.barkbites.data.staff.StaffMenuItem;
import com.mycompany.barkbites.data.staff.StaffMenuService;
import com.mycompany.barkbites.data.staff.StaffFirebaseBootstrap;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.SwingUtilities;

/**
 *
 * @author markd
 */
public class StaffMenu extends javax.swing.JFrame {

    private final StaffMenuService menuService = new StaffMenuService();
    private final DefaultListModel<StaffMenuItem> menuItemsModel = new DefaultListModel<>();
    private final JList<StaffMenuItem> menuItemsList = new JList<>(menuItemsModel);
    private final JTextField documentIdField = new JTextField();
    private final JTextField titleField = new JTextField();
    private final JTextField priceField = new JTextField();
    private final JTextField imagePathField = new JTextField();
    private final JTextArea descriptionArea = new JTextArea();
    private final JCheckBox activeCheckBox = new JCheckBox("Active", true);
    private final JLabel statusLabel = new JLabel("Ready");
    private final javax.swing.JButton refreshButton = new javax.swing.JButton("Refresh");
    private final javax.swing.JButton newButton = new javax.swing.JButton("New");
    private final javax.swing.JButton saveButton = new javax.swing.JButton("Save");
    private final javax.swing.JButton deleteButton = new javax.swing.JButton("Delete");

    /**
     * Creates new form StaffMenu
     */
    public StaffMenu() {
        initComponents();

        getContentPane().setComponentZOrder(jLabel1, getContentPane().getComponentCount() - 1);

        makeButtonInvisible(jButton1);
        makeButtonInvisible(jButton2);
        makeButtonInvisible(jButton3);
        makeButtonInvisible(jButton4);

        jButton1.addActionListener(evt -> openStaffOrders());
        jButton2.addActionListener(evt -> openStaffInventory());
        jButton3.addActionListener(evt -> openStaffStatistics());
        jButton4.addActionListener(evt -> openStaffLandingPage());

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

        JScrollPane listScroll = new JScrollPane(menuItemsList);
        listScroll.setOpaque(true);
        listScroll.getViewport().setBackground(Color.WHITE);
        listScroll.setBounds(180, 120, 250, 420);

        JLabel formTitle = new JLabel("Menu Item Editor");
        formTitle.setFont(new Font("Arial", Font.BOLD, 18));
        formTitle.setForeground(Color.WHITE);
        formTitle.setBounds(455, 120, 220, 24);

        JLabel idLabel = new JLabel("Document ID");
        JLabel titleLabel = new JLabel("Title");
        JLabel priceLabel = new JLabel("Price (pesos)");
        JLabel imageLabel = new JLabel("Image Path");
        JLabel descriptionLabel = new JLabel("Description");

        Color labelColor = Color.WHITE;
        idLabel.setForeground(labelColor);
        titleLabel.setForeground(labelColor);
        priceLabel.setForeground(labelColor);
        imageLabel.setForeground(labelColor);
        descriptionLabel.setForeground(labelColor);

        idLabel.setBounds(455, 155, 120, 20);
        documentIdField.setBounds(455, 176, 220, 28);
        titleLabel.setBounds(455, 215, 80, 20);
        titleField.setBounds(455, 236, 220, 28);
        priceLabel.setBounds(455, 275, 120, 20);
        priceField.setBounds(455, 296, 220, 28);
        imageLabel.setBounds(455, 335, 100, 20);
        imagePathField.setBounds(455, 356, 220, 28);
        descriptionLabel.setBounds(455, 395, 100, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setBounds(455, 416, 220, 92);
        activeCheckBox.setBounds(455, 515, 90, 24);

        refreshButton.setBounds(695, 176, 110, 32);
        newButton.setBounds(695, 220, 110, 32);
        saveButton.setBounds(695, 264, 110, 32);
        deleteButton.setBounds(695, 308, 110, 32);
        statusLabel.setBounds(455, 545, 360, 22);
        statusLabel.setForeground(Color.WHITE);

        refreshButton.addActionListener(evt -> loadMenuItemsAsync());
        newButton.addActionListener(evt -> clearMenuForm());
        saveButton.addActionListener(evt -> saveMenuItem());
        deleteButton.addActionListener(evt -> deleteMenuItem());

        addOverlay(listScroll, formTitle, idLabel, documentIdField, titleLabel, titleField, priceLabel, priceField, imageLabel, imagePathField, descriptionLabel, descriptionScroll, activeCheckBox, refreshButton, newButton, saveButton, deleteButton, statusLabel);
    }

    private void addOverlay(Component... components) {
        for (Component component : components) {
            java.awt.Rectangle bounds = component.getBounds();
            getContentPane().add(component, new org.netbeans.lib.awtextra.AbsoluteConstraints(bounds.x, bounds.y, bounds.width, bounds.height));
            getContentPane().setComponentZOrder(component, 0);
        }
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

        setBusy(true);
        javax.swing.SwingWorker<Void, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                menuService.upsertMenuItem(new StaffMenuItem(id, title, description, priceCents, imagePath, activeCheckBox.isSelected()));
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("Saved menu item '" + title + "'.");
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

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setText("jButton1");
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, 140, 60));

        jButton2.setText("jButton2");
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 230, 140, 70));

        jButton3.setText("jButton3");
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 440, 140, 80));

        jButton4.setText("jButton4");
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 550, 100, 30));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/StaffDesign/StaffMenu.png"))); // NOI18N
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
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
