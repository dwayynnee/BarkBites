/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.barkbites.StaffForms;

import com.mycompany.barkbites.FormNavigator;
import com.mycompany.barkbites.data.staff.StaffFirebaseBootstrap;
import com.mycompany.barkbites.data.staff.StaffStatisticsService;
import com.mycompany.barkbites.data.staff.StaffStatisticsSummary;
import java.awt.Color;
import java.awt.Font;
import java.util.concurrent.ExecutionException;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * OOP used here:
 * Encapsulation keeps summary state private to the screen.
 * Abstraction exposes only a small set of helpers for loading and formatting.
 * Inheritance comes from extending JFrame.
 * Polymorphism appears in the event listeners and SwingWorker overrides.
 *
 * @author markd
 */
public class StaffStatistics extends javax.swing.JFrame {

    private final StaffStatisticsService statisticsService = new StaffStatisticsService();
    private JLabel titleLabel;
    private JLabel statusLabel;

    /**
     * Creates new form StaffStatistics
     */
    public StaffStatistics() {
        initComponents();

        titleLabel = new JLabel();
        statusLabel = new JLabel("");
        getContentPane().add(titleLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 110, 220, 28));
        getContentPane().add(statusLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 545, 360, 22));

        getContentPane().setComponentZOrder(BG, getContentPane().getComponentCount() - 1);

        // Action: hide the navigation and refresh buttons while keeping them interactive.
        makeButtonInvisible(OrdersButton);
        makeButtonInvisible(InventoryButton);
        makeButtonInvisible(MenuButton);
        makeButtonInvisible(LogoutButton);
        makeButtonInvisible(refreshButton);
        makeButtonInvisible(HistoryButton);

        // Action: route each button to the matching staff screen.
        OrdersButton.addActionListener(evt -> openStaffOrders());
        InventoryButton.addActionListener(evt -> openStaffInventory());
        MenuButton.addActionListener(evt -> openStaffMenu());
        LogoutButton.addActionListener(evt -> openStaffLandingPage());
        HistoryButton.addActionListener(evt -> openStaffHistory());

        boolean firebaseReady = true;
        if (!java.beans.Beans.isDesignTime()) {
            firebaseReady = StaffFirebaseBootstrap.ensureInitialized(this);
        }
        configureUi();
        if (firebaseReady) {
            loadSummaryAsync();
        }

        this.setResizable(false);
    }

    private void configureUi() {
        // Action: style the screen title and status labels.
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        statusLabel.setForeground(Color.WHITE);

        // Action: refresh the summary data when the button is pressed.
        refreshButton.addActionListener(evt -> loadSummaryAsync());
    }

    private void loadSummaryAsync() {
        setBusy(true);
        javax.swing.SwingWorker<StaffStatisticsSummary, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected StaffStatisticsSummary doInBackground() {
                return statisticsService.loadSummary();
            }

            @Override
            protected void done() {
                try {
                    StaffStatisticsSummary summary = get();
                    totalOrdersValue.setText(Integer.toString(summary.totalOrders()));
                    totalSalesValue.setText(formatPesos(summary.totalSalesCents()));
                    monthOrdersValue.setText(Integer.toString(summary.monthOrders()));
                    monthSalesValue.setText(formatPesos(summary.monthSalesCents()));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Statistics load interrupted.");
                } catch (ExecutionException ee) {
                    String message = ee.getCause() != null ? ee.getCause().getMessage() : ee.getMessage();
                    statusLabel.setText("Statistics load failed.");
                    JOptionPane.showMessageDialog(StaffStatistics.this, message, "Statistics load failed", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setBusy(false);
                }
            }
        };
        worker.execute();
    }

    private void setBusy(boolean busy) {
        refreshButton.setEnabled(!busy);
    }

    private static String formatPesos(long cents) {
        return String.format(java.util.Locale.US, "₱%,.2f", cents / 100.0);
    }

    private void openStaffOrders() {
        // Action: open the Orders screen.
        FormNavigator.redirect(this, new StaffOrders());
    }

    private void openStaffInventory() {
        // Action: open the Inventory screen.
        FormNavigator.redirect(this, new StaffInventory());
    }

    private void openStaffMenu() {
        // Action: open the Menu screen.
        FormNavigator.redirect(this, new StaffMenu());
    }

    private void openStaffLandingPage() {
        // Action: return to the landing page.
        FormNavigator.redirect(this, new StaffLandingPage());
    }

    private void openStaffHistory() {
        // Action: open the History screen.
        FormNavigator.redirect(this, new StaffHistory());
    }

    private static void makeButtonInvisible(javax.swing.JButton button) {
        // Keeps the button clickable while removing the visible chrome.
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

        jButton5 = new javax.swing.JButton();
        totalOrdersValue = new javax.swing.JLabel();
        totalSalesValue = new javax.swing.JLabel();
        monthOrdersValue = new javax.swing.JLabel();
        monthSalesValue = new javax.swing.JLabel();
        refreshButton = new javax.swing.JButton();
        OrdersButton = new javax.swing.JButton();
        InventoryButton = new javax.swing.JButton();
        MenuButton = new javax.swing.JButton();
        LogoutButton = new javax.swing.JButton();
        HistoryButton = new javax.swing.JButton();
        BG = new javax.swing.JLabel();

        jButton5.setText("jButton5");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        totalOrdersValue.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        totalOrdersValue.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        totalOrdersValue.setText("0");
        getContentPane().add(totalOrdersValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 260, 260, 30));

        totalSalesValue.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        totalSalesValue.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        totalSalesValue.setText("₱0.00");
        getContentPane().add(totalSalesValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 450, 260, 30));

        monthOrdersValue.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        monthOrdersValue.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        monthOrdersValue.setText("0");
        getContentPane().add(monthOrdersValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 260, 260, 30));

        monthSalesValue.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        monthSalesValue.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        monthSalesValue.setText("₱0.00");
        getContentPane().add(monthSalesValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 450, 260, 30));

        refreshButton.setText("Refresh");
        getContentPane().add(refreshButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 120, 120, 50));

        OrdersButton.setText("jButton1");
        getContentPane().add(OrdersButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, 140, 70));

        InventoryButton.setText("jButton2");
        getContentPane().add(InventoryButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 210, 140, 70));

        MenuButton.setText("jButton3");
        getContentPane().add(MenuButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 290, 140, 70));

        LogoutButton.setText("jButton4");
        getContentPane().add(LogoutButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 540, 90, 40));

        HistoryButton.setText("jButton6");
        getContentPane().add(HistoryButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 460, 140, 70));

        BG.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/StaffDesign/StaffStatistics.png"))); // NOI18N
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
        } catch (InstantiationException ex) {
        } catch (IllegalAccessException ex) {
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new StaffStatistics().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel BG;
    private javax.swing.JButton HistoryButton;
    private javax.swing.JButton InventoryButton;
    private javax.swing.JButton LogoutButton;
    private javax.swing.JButton MenuButton;
    private javax.swing.JButton OrdersButton;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel monthOrdersValue;
    private javax.swing.JLabel monthSalesValue;
    private javax.swing.JButton refreshButton;
    private javax.swing.JLabel totalOrdersValue;
    private javax.swing.JLabel totalSalesValue;
    // End of variables declaration//GEN-END:variables
}
