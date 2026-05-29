/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.barkbites.StaffForms;

import com.mycompany.barkbites.FormNavigator;
import com.mycompany.barkbites.data.staff.StaffCashInRecord;
import com.mycompany.barkbites.data.staff.StaffCashInService;
import com.mycompany.barkbites.data.staff.StaffFirebaseBootstrap;
import com.mycompany.barkbites.data.staff.StaffOrderRecord;
import com.mycompany.barkbites.data.staff.StaffOrderService;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * OOP used here:
 * Encapsulation keeps the navigation helpers private to this form.
 * Abstraction hides the screen-switching details behind button clicks.
 * Inheritance comes from extending JFrame.
 * Polymorphism shows up in the event listeners reacting to runtime clicks.
 *
 * @author markd
 */
public class StaffHistory extends javax.swing.JFrame {

    private static final DateTimeFormatter HISTORY_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a").withZone(ZoneId.systemDefault());

    private final StaffOrderService orderService = new StaffOrderService();
    private final StaffCashInService cashInService = new StaffCashInService();
    private final DefaultTableModel historyTableModel = new DefaultTableModel(
            new Object[] { "Type", "Customer", "Amount", "Reference", "Details", "Created" },
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private javax.swing.JLabel historyTitleLabel;
    private javax.swing.JScrollPane historyScrollPane;
    private javax.swing.JTable historyTable;
    /**
     * Creates new form StaffHistory
     */
    public StaffHistory() {
        initComponents();
        boolean firebaseReady = true;
        if (!java.beans.Beans.isDesignTime()) {
            firebaseReady = StaffFirebaseBootstrap.ensureInitialized(this);
        }
        configureHistoryPanel();
        // Action: keep the generated buttons clickable while hiding their chrome.
        makeButtonInvisible(StaffOrders);
        makeButtonInvisible(StaffInventory);
        makeButtonInvisible(StaffMenu);
        makeButtonInvisible(StaffStatistics);
        makeButtonInvisible(Refresh);
        makeButtonInvisible(Logout);
        makeButtonInvisible(Cashin);

        // Action: connect each button to its matching screen.
        StaffOrders.addActionListener(evt -> openStaffOrders());
        StaffInventory.addActionListener(evt -> openStaffInventory());
        StaffMenu.addActionListener(evt -> openStaffMenu());
        StaffStatistics.addActionListener(evt -> openStaffStatistics());
        Logout.addActionListener(evt -> openStaffLandingPage());
        Cashin.addActionListener(evt -> openStaffCashIn());
        Refresh.addActionListener(evt -> loadHistoryAsync());

        if (firebaseReady) {
            loadHistoryAsync();
        }
    }

    private void configureHistoryPanel() {
        historyPanel.setOpaque(true);
        historyPanel.setBackground(new Color(255, 255, 255, 230));
        historyPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        historyTable.setRowHeight(24);
        historyTable.setShowGrid(false);
        historyTable.setFillsViewportHeight(true);
        historyTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        historyTable.setPreferredScrollableViewportSize(new Dimension(660, 330));
        historyTable.setBackground(Color.WHITE);
        historyTable.setForeground(Color.BLACK);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        historyTable.getTableHeader().setReorderingAllowed(false);
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        historyTable.getTableHeader().setBackground(new Color(23, 57, 122));
        historyTable.getTableHeader().setForeground(Color.WHITE);

        historyScrollPane.setBorder(BorderFactory.createEmptyBorder());
        historyScrollPane.getViewport().setBackground(Color.WHITE);
        historyScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        historyScrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        historyScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        historyScrollPane.setViewportView(historyTable);

        historyTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        historyTitleLabel.setForeground(new Color(25, 25, 25));
        historyTitleLabel.setText("History");
    }

    private void loadHistoryAsync() {
        Refresh.setEnabled(false);
        SwingWorker<List<HistoryEntry>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<HistoryEntry> doInBackground() {
                List<HistoryEntry> historyEntries = new ArrayList<>();

                for (StaffOrderRecord order : orderService.listOrders()) {
                    historyEntries.add(new HistoryEntry(
                            "Order",
                            safeText(order.customerName()),
                            formatTotal(order.totalCents()),
                            safeText(order.id()),
                            safeText(order.status()),
                            order.createdAtMillis()
                    ));
                }

                for (StaffCashInRecord cashIn : cashInService.listCashInRecords()) {
                    historyEntries.add(new HistoryEntry(
                            "Cash In",
                            safeText(cashIn.customerName()),
                            formatTotal(cashIn.amountCents()),
                            safeText(cashIn.id()),
                            formatTotal(cashIn.balanceBeforeCents()),
                            cashIn.createdAtMillis()
                    ));
                }

                historyEntries.sort(
                        Comparator
                                .comparingLong(HistoryEntry::createdAtMillis)
                                .reversed()
                                .thenComparing(HistoryEntry::reference)
                );

                return historyEntries;
            }

            @Override
            protected void done() {
                try {
                    List<HistoryEntry> historyEntries = get();
                    historyTableModel.setRowCount(0);
                    for (HistoryEntry entry : historyEntries) {
                        historyTableModel.addRow(new Object[] {
                            entry.type(),
                            entry.customer(),
                            entry.amount(),
                            entry.reference(),
                            entry.details(),
                            formatCreatedAt(entry.createdAtMillis())
                        });
                    }
                    historyTableModel.fireTableDataChanged();
                    historyTable.revalidate();
                    historyTable.repaint();
                    historyScrollPane.revalidate();
                    historyScrollPane.repaint();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    String message = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    JOptionPane.showMessageDialog(StaffHistory.this, message, "Load history failed", JOptionPane.ERROR_MESSAGE);
                } finally {
                    Refresh.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private static void makeButtonInvisible(javax.swing.JButton button) {
        // Keeps the hit area active while removing the visible button styling.
        button.setText("");
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
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

    private void openStaffStatistics() {
        // Action: open the Statistics screen.
        FormNavigator.redirect(this, new StaffStatistics());
    }

    private void openStaffLandingPage() {
        // Action: return to the landing page.
        FormNavigator.redirect(this, new StaffLandingPage());
    }

    private void openStaffCashIn() {
        // Action: open the Cash In screen.
        FormNavigator.redirect(this, new StaffCashIn());
    }

    private static String formatTotal(long totalCents) {
        return "₱" + String.format("%,.2f", totalCents / 100.0d);
    }

    private record HistoryEntry(String type, String customer, String amount, String reference, String details, long createdAtMillis) {
    }

    private static String formatCreatedAt(long createdAtMillis) {
        if (createdAtMillis <= 0L) {
            return "-";
        }
        return HISTORY_TIME_FORMATTER.format(Instant.ofEpochMilli(createdAtMillis));
    }

    private static String safeText(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        historyPanel = new javax.swing.JPanel();
        historyTitleLabel = new javax.swing.JLabel();
        historyScrollPane = new javax.swing.JScrollPane();
        historyTable = new javax.swing.JTable(historyTableModel);
        StaffOrders = new javax.swing.JButton();
        StaffInventory = new javax.swing.JButton();
        StaffMenu = new javax.swing.JButton();
        StaffStatistics = new javax.swing.JButton();
        Refresh = new javax.swing.JButton();
        Logout = new javax.swing.JButton();
        Cashin = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        getContentPane().add(historyPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 200, 660, 360));

        historyScrollPane.setViewportView(historyTable);
        historyPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        historyPanel.add(historyTitleLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
        historyPanel.add(historyScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 30, 660, 330));

        StaffOrders.setText("jButton1");
        getContentPane().add(StaffOrders, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, 140, 60));

        StaffInventory.setText("jButton2");
        getContentPane().add(StaffInventory, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 200, 140, 60));

        StaffMenu.setText("jButton3");
        getContentPane().add(StaffMenu, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 270, 140, 50));

        StaffStatistics.setText("jButton4");
        getContentPane().add(StaffStatistics, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 330, 140, 70));

        Refresh.setText("jButton5");
        getContentPane().add(Refresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 110, 100, 50));

        Logout.setText("jButton6");
        getContentPane().add(Logout, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 540, 100, 40));

        Cashin.setText("jButton1");
        getContentPane().add(Cashin, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 400, 140, 60));

        jLabel1.setIcon(new javax.swing.ImageIcon("C:\\BarkBites\\src\\main\\java\\com\\mycompany\\barkbites\\StaffDesign\\StaffHistory.png")); // NOI18N
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
        } catch (InstantiationException ex) {
        } catch (IllegalAccessException ex) {
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new StaffHistory().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Cashin;
    private javax.swing.JButton Logout;
    private javax.swing.JButton Refresh;
    private javax.swing.JButton StaffInventory;
    private javax.swing.JButton StaffMenu;
    private javax.swing.JButton StaffOrders;
    private javax.swing.JButton StaffStatistics;
    private javax.swing.JPanel historyPanel;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
