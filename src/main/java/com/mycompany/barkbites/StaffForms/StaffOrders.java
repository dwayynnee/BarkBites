/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.barkbites.StaffForms;

import com.mycompany.barkbites.FormNavigator;
import com.mycompany.barkbites.data.staff.StaffOrderRecord;
import com.mycompany.barkbites.data.staff.StaffOrderService;
import com.mycompany.barkbites.data.staff.StaffFirebaseBootstrap;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.concurrent.ExecutionException;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

/**
 *
 * @author markd
 */
public class StaffOrders extends javax.swing.JFrame {

    private final StaffOrderService orderService = new StaffOrderService();
    private final DefaultListModel<StaffOrderRecord> orderModel = new DefaultListModel<>();
    private final JList<StaffOrderRecord> orderList = new JList<>(orderModel);
    private final JLabel titleLabel = new JLabel("Orders");
    private final JLabel selectedOrderLabel = new JLabel("Select an order to edit its status.");
    private final JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"new", "processing", "ready", "completed", "cancelled"});
    private final javax.swing.JButton refreshButton = new javax.swing.JButton("Refresh");
    private final javax.swing.JButton updateButton = new javax.swing.JButton("Update Status");

    /**
     * Creates new form StaffOrders
     */
    public StaffOrders() {
        initComponents();

        getContentPane().setComponentZOrder(jLabel1, getContentPane().getComponentCount() - 1);

        makeButtonInvisible(jButton1);
        makeButtonInvisible(jButton2);
        makeButtonInvisible(jButton3);
        makeButtonInvisible(jButton4);

        jButton1.addActionListener(evt -> openStaffInventory());
        jButton2.addActionListener(evt -> openStaffMenu());
        jButton3.addActionListener(evt -> openStaffStatistics());
        jButton4.addActionListener(evt -> openStaffLandingPage());

        StaffFirebaseBootstrap.ensureInitialized(this);
        configureUi();
        loadOrdersAsync();

        this.setResizable(false);
    }

    private void configureUi() {
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(185, 120, 200, 28);

        orderList.setCellRenderer((JList<? extends StaffOrderRecord> list, StaffOrderRecord value, int index, boolean isSelected, boolean cellHasFocus) -> {
            JLabel label = new JLabel(formatOrder(value));
            label.setOpaque(true);
            label.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10));
            label.setBackground(isSelected ? new Color(23, 57, 122) : Color.WHITE);
            label.setForeground(isSelected ? Color.WHITE : Color.BLACK);
            return label;
        });
        orderList.addListSelectionListener(evt -> {
            if (!evt.getValueIsAdjusting()) {
                StaffOrderRecord selected = orderList.getSelectedValue();
                if (selected != null) {
                    selectedOrderLabel.setText(selected.customerName() + " | ₱" + String.format(java.util.Locale.US, "%,.2f", selected.totalCents() / 100.0));
                    statusComboBox.setSelectedItem(selected.status());
                }
            }
        });

        JScrollPane listScroll = new JScrollPane(orderList);
        listScroll.setBounds(185, 160, 340, 360);

        JLabel statusLabel = new JLabel("Status");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBounds(560, 200, 80, 20);
        statusComboBox.setBounds(560, 222, 160, 30);

        selectedOrderLabel.setForeground(Color.WHITE);
        selectedOrderLabel.setBounds(560, 160, 250, 20);
        refreshButton.setBounds(560, 270, 120, 32);
        updateButton.setBounds(560, 315, 120, 32);

        refreshButton.addActionListener(evt -> loadOrdersAsync());
        updateButton.addActionListener(evt -> updateStatusForSelection());

        addOverlay(titleLabel, listScroll, statusLabel, statusComboBox, selectedOrderLabel, refreshButton, updateButton);
    }

    private void addOverlay(Component... components) {
        for (Component component : components) {
            java.awt.Rectangle bounds = component.getBounds();
            getContentPane().add(component, new org.netbeans.lib.awtextra.AbsoluteConstraints(bounds.x, bounds.y, bounds.width, bounds.height));
            getContentPane().setComponentZOrder(component, 0);
        }
    }

    private void loadOrdersAsync() {
        setBusy(true);
        javax.swing.SwingWorker<java.util.List<StaffOrderRecord>, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected java.util.List<StaffOrderRecord> doInBackground() {
                return orderService.listOrders();
            }

            @Override
            protected void done() {
                try {
                    orderModel.clear();
                    for (StaffOrderRecord order : get()) {
                        orderModel.addElement(order);
                    }
                    if (!orderModel.isEmpty()) {
                        orderList.setSelectedIndex(0);
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ee) {
                    String message = ee.getCause() != null ? ee.getCause().getMessage() : ee.getMessage();
                    JOptionPane.showMessageDialog(StaffOrders.this, message, "Orders load failed", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setBusy(false);
                }
            }
        };
        worker.execute();
    }

    private void updateStatusForSelection() {
        StaffOrderRecord selected = orderList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select an order first.", "No order selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String status = (String) statusComboBox.getSelectedItem();
        if (status == null || status.isBlank()) {
            JOptionPane.showMessageDialog(this, "Choose a status.", "Missing status", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setBusy(true);
        javax.swing.SwingWorker<Void, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                orderService.updateOrderStatus(selected.id(), status);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    loadOrdersAsync();
                } catch (Exception ex) {
                    String message = ex.getMessage() != null ? ex.getMessage() : "Failed to update the order status.";
                    JOptionPane.showMessageDialog(StaffOrders.this, message, "Update failed", JOptionPane.ERROR_MESSAGE);
                    setBusy(false);
                }
            }
        };
        worker.execute();
    }

    private void setBusy(boolean busy) {
        refreshButton.setEnabled(!busy);
        updateButton.setEnabled(!busy);
    }

    private static String formatOrder(StaffOrderRecord order) {
        if (order == null) {
            return "";
        }
        return order.customerName() + "  •  " + order.status() + "  •  ₱" + String.format(java.util.Locale.US, "%,.2f", order.totalCents() / 100.0);
    }

    private void openStaffInventory() {
        FormNavigator.redirect(this, new StaffInventory());
    }

    private void openStaffMenu() {
        FormNavigator.redirect(this, new StaffMenu());
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
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 230, 140, 70));

        jButton2.setText("jButton2");
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 340, 140, 70));

        jButton3.setText("jButton3");
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 450, 140, 60));

        jButton4.setText("jButton4");
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 550, 90, 30));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/StaffDesign/StaffOrders.png"))); // NOI18N
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
            java.util.logging.Logger.getLogger(StaffOrders.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(StaffOrders.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(StaffOrders.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(StaffOrders.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new StaffOrders().setVisible(true);
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
