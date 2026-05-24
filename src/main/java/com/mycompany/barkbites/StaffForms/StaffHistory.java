/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.barkbites.StaffForms;

import com.mycompany.barkbites.FormNavigator;
import com.mycompany.barkbites.data.staff.StaffFirebaseBootstrap;
import com.mycompany.barkbites.data.staff.StaffOrderRecord;
import com.mycompany.barkbites.data.staff.StaffOrderService;
import java.awt.BorderLayout;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
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
    private final DefaultTableModel historyTableModel = new DefaultTableModel(
            new Object[] { "Order ID", "Customer", "Status", "Payment", "Total", "Order", "Created" },
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private JTable historyTable;
    private JPanel historyPanel;

    /**
     * Creates new form StaffHistory
     */
    public StaffHistory() {
        initComponents();
        StaffFirebaseBootstrap.ensureInitialized(this);
        configureHistoryPanel();
        // Action: keep the generated buttons clickable while hiding their chrome.
        makeButtonInvisible(StaffOrders);
        makeButtonInvisible(StaffInventory);
        makeButtonInvisible(StaffMenu);
        makeButtonInvisible(StaffStatistics);
        makeButtonInvisible(Refresh);
        makeButtonInvisible(Logout);

        // Action: connect each button to its matching screen.
        StaffOrders.addActionListener(evt -> openStaffOrders());
        StaffInventory.addActionListener(evt -> openStaffInventory());
        StaffMenu.addActionListener(evt -> openStaffMenu());
        StaffStatistics.addActionListener(evt -> openStaffStatistics());
        Logout.addActionListener(evt -> openStaffLandingPage());
        Refresh.addActionListener(evt -> loadHistoryAsync());

        loadHistoryAsync();
    }

    private void configureHistoryPanel() {
        historyPanel = new JPanel(new BorderLayout());
        historyPanel.setOpaque(true);
        historyPanel.setBackground(new Color(255, 255, 255, 230));
        historyPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        JLabel titleLabel = new JLabel("Order History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(25, 25, 25));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));

        historyTable = new JTable(historyTableModel);
        historyTable.setRowHeight(24);
        historyTable.setShowGrid(false);
        historyTable.setFillsViewportHeight(true);
        historyTable.setBackground(Color.WHITE);
        historyTable.setForeground(Color.BLACK);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        historyTable.getTableHeader().setReorderingAllowed(false);
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        historyTable.getTableHeader().setBackground(new Color(23, 57, 122));
        historyTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        historyPanel.add(titleLabel, BorderLayout.NORTH);
        historyPanel.add(scrollPane, BorderLayout.CENTER);

        getContentPane().add(historyPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 120, 700, 390));
        getContentPane().setComponentZOrder(historyPanel, 0);
        getContentPane().setComponentZOrder(jLabel1, getContentPane().getComponentCount() - 1);
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    private void loadHistoryAsync() {
        Refresh.setEnabled(false);
        SwingWorker<List<StaffOrderRecord>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<StaffOrderRecord> doInBackground() {
                List<StaffOrderRecord> orders = new ArrayList<>(orderService.listOrders());
                orders.sort(
                        Comparator
                                .comparingLong(StaffOrderRecord::createdAtMillis)
                                .reversed()
                                .thenComparing(StaffOrderRecord::id)
                );
                return orders;
            }

            @Override
            protected void done() {
                try {
                    List<StaffOrderRecord> orders = get();
                    historyTableModel.setRowCount(0);
                    for (StaffOrderRecord order : orders) {
                        historyTableModel.addRow(new Object[] {
                            safeText(order.id()),
                            safeText(order.customerName()),
                            safeText(order.status()),
                            safeText(order.payment()),
                            formatTotal(order.totalCents()),
                            safeText(order.order()),
                            formatCreatedAt(order.createdAtMillis())
                        });
                    }
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

    private void refreshPage() {
        // Action: reload the current history screen.
        loadHistoryAsync();
    }

    private static String formatTotal(long totalCents) {
        return "₱" + String.format("%,.2f", totalCents / 100.0d);
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

        StaffOrders = new javax.swing.JButton();
        StaffInventory = new javax.swing.JButton();
        StaffMenu = new javax.swing.JButton();
        StaffStatistics = new javax.swing.JButton();
        Refresh = new javax.swing.JButton();
        Logout = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        StaffOrders.setText("jButton1");
        getContentPane().add(StaffOrders, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, 140, 70));

        StaffInventory.setText("jButton2");
        getContentPane().add(StaffInventory, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 210, 140, 70));

        StaffMenu.setText("jButton3");
        getContentPane().add(StaffMenu, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 290, 140, 70));

        StaffStatistics.setText("jButton4");
        getContentPane().add(StaffStatistics, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 380, 140, 70));

        Refresh.setText("jButton5");
        getContentPane().add(Refresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 110, 100, 50));

        Logout.setText("jButton6");
        getContentPane().add(Logout, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 540, 100, 40));

        jLabel1.setIcon(new javax.swing.ImageIcon("C:\\Users\\markd\\Downloads\\Staff Design\\StaffHistory.png")); // NOI18N
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
    private javax.swing.JButton Logout;
    private javax.swing.JButton Refresh;
    private javax.swing.JButton StaffInventory;
    private javax.swing.JButton StaffMenu;
    private javax.swing.JButton StaffOrders;
    private javax.swing.JButton StaffStatistics;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
