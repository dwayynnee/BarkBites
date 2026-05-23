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
import java.awt.Component;
import java.awt.Font;
import java.util.concurrent.ExecutionException;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author markd
 */
public class StaffStatistics extends javax.swing.JFrame {

    private final StaffStatisticsService statisticsService = new StaffStatisticsService();
    private final JLabel titleLabel = new JLabel("Sales Statistics");
    private final JLabel totalOrdersValue = new JLabel("0");
    private final JLabel totalSalesValue = new JLabel("₱0.00");
    private final JLabel monthOrdersValue = new JLabel("0");
    private final JLabel monthSalesValue = new JLabel("₱0.00");
    private final JLabel statusLabel = new JLabel("Ready");
    private final javax.swing.JButton refreshButton = new javax.swing.JButton("Refresh");

    /**
     * Creates new form StaffStatistics
     */
    public StaffStatistics() {
        initComponents();

        getContentPane().setComponentZOrder(jLabel1, getContentPane().getComponentCount() - 1);

        makeButtonInvisible(jButton1);
        makeButtonInvisible(jButton2);
        makeButtonInvisible(jButton3);
        makeButtonInvisible(jButton4);

        jButton1.addActionListener(evt -> openStaffOrders());
        jButton2.addActionListener(evt -> openStaffInventory());
        jButton3.addActionListener(evt -> openStaffMenu());
        jButton4.addActionListener(evt -> openStaffLandingPage());

        if (!StaffFirebaseBootstrap.ensureInitialized(this)) {
            return;
        }
        configureUi();
        loadSummaryAsync();

        this.setResizable(false);
    }

    private void configureUi() {
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(185, 120, 250, 28);

        addMetricLabel("Total Orders", totalOrdersValue, 185, 180);
        addMetricLabel("Total Sales", totalSalesValue, 185, 280);
        addMetricLabel("This Month Orders", monthOrdersValue, 430, 180);
        addMetricLabel("This Month Sales", monthSalesValue, 430, 280);

        refreshButton.setBounds(185, 400, 120, 34);
        statusLabel.setBounds(185, 445, 400, 22);
        statusLabel.setForeground(Color.WHITE);

        refreshButton.addActionListener(evt -> loadSummaryAsync());

        addOverlay(titleLabel, totalOrdersValue, totalSalesValue, monthOrdersValue, monthSalesValue, refreshButton, statusLabel);
    }

    private void addMetricLabel(String caption, JLabel valueLabel, int x, int y) {
        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        captionLabel.setForeground(Color.WHITE);
        captionLabel.setBounds(x, y, 200, 20);

        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setBounds(x, y + 26, 220, 30);

        addOverlay(captionLabel, valueLabel);
    }

    private void addOverlay(Component... components) {
        for (Component component : components) {
            java.awt.Rectangle bounds = component.getBounds();
            getContentPane().add(component, new org.netbeans.lib.awtextra.AbsoluteConstraints(bounds.x, bounds.y, bounds.width, bounds.height));
            getContentPane().setComponentZOrder(component, 0);
        }
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
                    statusLabel.setText("Loaded current sales summary.");
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
        FormNavigator.redirect(this, new StaffOrders());
    }

    private void openStaffInventory() {
        FormNavigator.redirect(this, new StaffInventory());
    }

    private void openStaffMenu() {
        FormNavigator.redirect(this, new StaffMenu());
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
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, 140, 70));

        jButton2.setText("jButton2");
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 230, 140, 70));

        jButton3.setText("jButton3");
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 340, 140, 70));

        jButton4.setText("jButton4");
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 540, 90, 40));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/StaffDesign/StaffStatistics.png"))); // NOI18N
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
            java.util.logging.Logger.getLogger(StaffStatistics.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(StaffStatistics.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(StaffStatistics.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(StaffStatistics.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
