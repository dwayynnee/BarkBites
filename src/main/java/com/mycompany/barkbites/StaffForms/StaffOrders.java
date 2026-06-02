/*
 * StaffOrders.java
 * UI for staff to view and manage orders. Contains card rendering
 * and status update functionality backed by StaffOrderService.
 */
package com.mycompany.barkbites.StaffForms;

import com.mycompany.barkbites.FormNavigator;
import com.mycompany.barkbites.data.staff.StaffFirebaseBootstrap;
import com.mycompany.barkbites.data.staff.StaffOrderRecord;
import com.mycompany.barkbites.data.staff.StaffOrderService;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
// Removed unused javax.swing.DefaultComboBoxModel and JComboBox imports (parameterized in place)
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

/**
 * OOP used here:
 * Encapsulation keeps order state and card rendering private to the form.
 * Abstraction hides card creation and navigation details behind helpers.
 * Inheritance comes from extending JFrame.
 * Polymorphism shows up in the event listeners and renderer callbacks.
 *
 * @author markd
 */
public class StaffOrders extends javax.swing.JFrame {

    private static final int MAX_CARDS = 8;

    private final StaffOrderService orderService = new StaffOrderService();
    private final JPanel[] orderCards = new JPanel[MAX_CARDS];
    private final JLabel[] orderNumberLabels = new JLabel[MAX_CARDS];
    private final JLabel[] nameLabels = new JLabel[MAX_CARDS];
    private final JLabel[] idLabels = new JLabel[MAX_CARDS];
    private final JLabel[] statusLabels = new JLabel[MAX_CARDS];
    private final JLabel[] paymentLabels = new JLabel[MAX_CARDS];
    private final JLabel[] orderLabels = new JLabel[MAX_CARDS];

    private List<StaffOrderRecord> displayedOrders = new ArrayList<>();
    private int selectedCardIndex = -1;

    /**
     * Creates new form StaffOrders
     */
    public StaffOrders() {
        initComponents();

        getContentPane().setComponentZOrder(BG, getContentPane().getComponentCount() - 1);

        // Action: hide the navigation and utility buttons while keeping them clickable.
        makeButtonInvisible(InventoryButton);
        makeButtonInvisible(MenuButton);
        makeButtonInvisible(StatisticsButton);
        makeButtonInvisible(LogoutButton);
        makeButtonInvisible(HistoryButton);
        makeButtonInvisible(CashIn);
        makeButtonInvisible(updateButton);
        makeButtonInvisible(refreshButton);
        makeButtonInvisible(jButton1);

        // Action: route each button to its matching form or action.
        InventoryButton.addActionListener(evt -> openStaffInventory());
        MenuButton.addActionListener(evt -> openStaffMenu());
        StatisticsButton.addActionListener(evt -> openStaffStatistics());
        LogoutButton.addActionListener(evt -> openStaffLandingPage());
        HistoryButton.addActionListener(evt -> openStaffHistory());
        CashIn.addActionListener(evt -> openStaffCashIn());
        jButton1.addActionListener(evt -> openStaffVouchers());
        refreshButton.addActionListener(evt -> loadOrdersAsync());
        updateButton.addActionListener(evt -> updateSelectedOrderStatusAsync());

        boolean firebaseReady = false;
        if (!java.beans.Beans.isDesignTime()) {
            firebaseReady = StaffFirebaseBootstrap.ensureInitialized(this);
        }
        if (!java.beans.Beans.isDesignTime()) {
            configureOrderCards();
            getContentPane().setComponentZOrder(BG, getContentPane().getComponentCount() - 1);
            if (firebaseReady) {
                loadOrdersAsync();
            } else {
                setOrderActionsEnabled(false);
            }
        }

        this.setResizable(false);
    }

    private void openStaffInventory() {
        // Action: open the Inventory screen.
        try {
            FormNavigator.redirect(this, new StaffInventory());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to open Inventory screen right now.", "Navigation error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openStaffMenu() {
        // Action: open the Menu screen.
        try {
            FormNavigator.redirect(this, new StaffMenu());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to open Menu screen right now.", "Navigation error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openStaffStatistics() {
        // Action: open the Statistics screen.
        try {
            FormNavigator.redirect(this, new StaffStatistics());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to open Statistics screen right now.", "Navigation error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openStaffLandingPage() {
        // Action: return to the landing page.
        FormNavigator.redirect(this, new StaffLandingPage());
    }

    private void openStaffHistory() {
        // Action: open the History screen.
        FormNavigator.redirect(this, new StaffHistory());
    }

    private void openStaffCashIn() {
        // Action: open the Cash In screen.
        FormNavigator.redirect(this, new StaffCashIn());
    }
    
        private void openStaffVouchers() {
        // Action: open the Cash In screen.
        FormNavigator.redirect(this, new StaffVouchers());
    }

    private static void makeButtonInvisible(javax.swing.JButton button) {
        // Keeps the button clickable while removing the visible chrome.
        button.setText("");
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
    }

    private void configureOrderCards() {
        if (java.beans.Beans.isDesignTime()) {
            return;
        }

        // Action: prepare the container for dynamic card rendering.
        cardsContainerPanel.setOpaque(false);
        cardsContainerPanel.setBorder(null);

        JPanel[] designerCards = new JPanel[] {
            jPanel1, jPanel2, jPanel3, jPanel4,
            jPanel5, jPanel6, jPanel7, jPanel8
        };

        for (int i = 0; i < MAX_CARDS; i++) {
            JPanel card = designerCards[i];
            card.removeAll();
            card.setLayout(new GridLayout(6, 1, 0, 0));
            card.setOpaque(true);
            card.setBackground(new Color(255, 255, 255, 225));
            card.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel orderNumber = createCardLabel();
            orderNumber.setFont(orderNumber.getFont().deriveFont(Font.BOLD));
            JLabel name = createCardLabel();
            JLabel id = createCardLabel();
            JLabel status = createCardLabel();
            JLabel payment = createCardLabel();
            JLabel order = createCardLabel();

            card.add(orderNumber);
            card.add(name);
            card.add(id);
            card.add(status);
            card.add(payment);
            card.add(order);

            final int cardIndex = i;
            MouseAdapter clickHandler = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectCard(cardIndex);
                }
            };
            card.addMouseListener(clickHandler);
            orderNumber.addMouseListener(clickHandler);
            name.addMouseListener(clickHandler);
            id.addMouseListener(clickHandler);
            status.addMouseListener(clickHandler);
            payment.addMouseListener(clickHandler);
            order.addMouseListener(clickHandler);

            card.setVisible(false);
            orderCards[i] = card;
            orderNumberLabels[i] = orderNumber;
            nameLabels[i] = name;
            idLabels[i] = id;
            statusLabels[i] = status;
            paymentLabels[i] = payment;
            orderLabels[i] = order;
        }

        getContentPane().setComponentZOrder(cardsContainerPanel, 0);
        getContentPane().setComponentZOrder(BG, getContentPane().getComponentCount() - 1);

        getContentPane().revalidate();
        getContentPane().repaint();
    }

    private static JLabel createCardLabel() {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        label.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        label.setForeground(new Color(25, 25, 25));
        return label;
    }

    /**
     * Loads customer orders from Firestore asynchronously (non-blocking).
     * 
     * This method:
     * 1. Fetches all orders from Firestore in a background thread
     * 2. Filters to show only "new" and "ready" orders (hides completed/cancelled)
     * 3. Sorts orders by creation time (newest first), then by ID
     * 4. Limits display to MAX_CARDS (8 cards) to fit on screen
     * 5. Updates the UI with the filtered orders
     * 
     * Uses SwingWorker to prevent blocking the GUI during network I/O.
     */
    private void loadOrdersAsync() {
        // Disable order actions during loading
        setOrderActionsEnabled(false);
        
        // Create background task to fetch and process orders
        javax.swing.SwingWorker<List<StaffOrderRecord>, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected List<StaffOrderRecord> doInBackground() {
                // Fetch all orders from Firestore
                List<StaffOrderRecord> orders = new ArrayList<>();
                for (StaffOrderRecord order : orderService.listOrders()) {
                    String status = safeText(order.status());
                    // Filter: only show active orders (not completed or cancelled)
                    if (!"completed".equalsIgnoreCase(status) && !"cancelled".equalsIgnoreCase(status)) {
                        orders.add(order);
                    }
                }
                
                // Sort by creation time (newest first), then by order ID
                orders.sort(
                        Comparator
                                .comparingLong(StaffOrderRecord::createdAtMillis)
                                .thenComparing(StaffOrderRecord::id)
                );
                
                // Cap to MAX_CARDS (8) to fit on the screen
                if (orders.size() > MAX_CARDS) {
                    return new ArrayList<>(orders.subList(0, MAX_CARDS));
                }
                return orders;
            }

            @Override
            protected void done() {
                try {
                    // Get the filtered orders and render them on the UI
                    displayedOrders = get();
                    selectedCardIndex = -1;
                    renderOrderCards();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    // Show error message if something went wrong
                    String message = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    JOptionPane.showMessageDialog(StaffOrders.this, message, "Load orders failed", JOptionPane.ERROR_MESSAGE);
                } finally {
                    // Re-enable order actions
                    setOrderActionsEnabled(true);
                }
            }
        };
        // Start the background task
        worker.execute();
    }

    /**
     * Renders order data into the card display grid.
     * 
     * Updates MAX_CARDS (8) card panels with order information:
     * - Order number, customer name, order ID, status, payment method, order items
     * - Hides empty cards if fewer than MAX_CARDS orders are available
     * - Applies visual styling (borders, background, cursor) for user interaction
     */
    private void renderOrderCards() {
        // Set layering: cards on top, background image behind
        getContentPane().setComponentZOrder(cardsContainerPanel, 0);
        getContentPane().setComponentZOrder(BG, getContentPane().getComponentCount() - 1);
        
        // Populate each card with order data or hide if no data
        for (int i = 0; i < MAX_CARDS; i++) {
            JPanel card = orderCards[i];
            if (i < displayedOrders.size()) {
                // Get order and populate card fields
                StaffOrderRecord order = displayedOrders.get(i);
                orderNumberLabels[i].setText("Order #" + (i + 1));
                orderNumberLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
                nameLabels[i].setText("Name: " + safeText(order.customerName()));
                nameLabels[i].setHorizontalAlignment(SwingConstants.LEFT);
                idLabels[i].setText("ID: " + safeText(order.id()));
                idLabels[i].setHorizontalAlignment(SwingConstants.LEFT);
                statusLabels[i].setText("Status: " + safeText(order.status()));
                statusLabels[i].setHorizontalAlignment(SwingConstants.LEFT);
                paymentLabels[i].setText("Payment: " + safeText(order.payment()));
                paymentLabels[i].setHorizontalAlignment(SwingConstants.LEFT);
                orderLabels[i].setText("Order: " + safeText(order.order()));
                orderLabels[i].setHorizontalAlignment(SwingConstants.LEFT);
                card.setVisible(true);
            } else {
                // Hide unused cards
                card.setVisible(false);
            }
        }
        // Update visual selection highlighting
        updateSelectionStyles();
    }

    private void selectCard(int cardIndex) {
        if (cardIndex < 0 || cardIndex >= displayedOrders.size()) {
            return;
        }
        selectedCardIndex = cardIndex;
        StaffOrderRecord selectedOrder = displayedOrders.get(cardIndex);
        statusComboBox.setSelectedItem(selectedOrder.status());
        updateSelectionStyles();
    }

    private void updateSelectionStyles() {
        for (int i = 0; i < MAX_CARDS; i++) {
            JPanel card = orderCards[i];
            if (!card.isVisible()) {
                continue;
            }
            boolean isSelected = i == selectedCardIndex;
            card.setBackground(isSelected ? new Color(207, 232, 255, 245) : new Color(255, 255, 255, 225));
            card.setBorder(BorderFactory.createLineBorder(isSelected ? new Color(23, 57, 122) : new Color(200, 200, 200), isSelected ? 2 : 1));
        }
        getContentPane().repaint();
    }

    private void updateSelectedOrderStatusAsync() {
        if (selectedCardIndex < 0 || selectedCardIndex >= displayedOrders.size()) {
            JOptionPane.showMessageDialog(this, "Select an order card first.", "No order selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StaffOrderRecord selectedOrder = displayedOrders.get(selectedCardIndex);
        String selectedStatus = statusComboBox.getSelectedItem() != null
                ? statusComboBox.getSelectedItem().toString().trim()
                : "";
        if (selectedStatus.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please choose a status.", "Missing status", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setOrderActionsEnabled(false);
        javax.swing.SwingWorker<Void, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                orderService.updateOrderStatus(selectedOrder.id(), selectedStatus);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    loadOrdersAsync();
                } catch (Exception ex) {
                    String message = ex.getMessage() != null ? ex.getMessage() : "Failed to update order status.";
                    JOptionPane.showMessageDialog(StaffOrders.this, message, "Status update failed", JOptionPane.ERROR_MESSAGE);
                    setOrderActionsEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void setOrderActionsEnabled(boolean enabled) {
        refreshButton.setEnabled(enabled);
        updateButton.setEnabled(enabled);
        statusComboBox.setEnabled(enabled);
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

        statusComboBox = new javax.swing.JComboBox();
        refreshButton = new javax.swing.JButton();
        updateButton = new javax.swing.JButton();
        cardsContainerPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        InventoryButton = new javax.swing.JButton();
        MenuButton = new javax.swing.JButton();
        StatisticsButton = new javax.swing.JButton();
        LogoutButton = new javax.swing.JButton();
        HistoryButton = new javax.swing.JButton();
        CashIn = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        BG = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        statusComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "new", "processing", "ready", "completed", "cancelled" }));
        getContentPane().add(statusComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 130, 160, 30));

        refreshButton.setText("Refresh");
        getContentPane().add(refreshButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 120, 110, 50));

        updateButton.setText("Update Status");
        getContentPane().add(updateButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 120, 110, 50));

        cardsContainerPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(180, 180, 180)));
        cardsContainerPanel.setOpaque(false);
        cardsContainerPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Order #1");
        jPanel1.add(jLabel1);

        cardsContainerPanel.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 160, 170));

        jLabel2.setText("Order #2");
        jPanel2.add(jLabel2);

        cardsContainerPanel.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 0, 160, 170));

        jLabel3.setText("Order #3");
        jPanel3.add(jLabel3);

        cardsContainerPanel.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 0, 170, 170));

        jLabel4.setText("Order #4");
        jPanel4.add(jLabel4);

        cardsContainerPanel.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 0, 160, 170));

        jLabel5.setText("Order #5");
        jPanel5.add(jLabel5);

        cardsContainerPanel.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 200, 160, 180));

        jLabel6.setText("Order #6");
        jPanel6.add(jLabel6);

        cardsContainerPanel.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 200, 160, 180));

        jLabel7.setText("Order #7");
        jPanel7.add(jLabel7);

        cardsContainerPanel.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 200, 170, 180));

        jLabel8.setText("Order #8");
        jPanel8.add(jLabel8);

        cardsContainerPanel.add(jPanel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 200, 160, 180));

        getContentPane().add(cardsContainerPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 190, 710, 380));

        InventoryButton.setText("jButton1");
        InventoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InventoryButtonActionPerformed(evt);
            }
        });
        getContentPane().add(InventoryButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, 140, 50));

        MenuButton.setText("jButton2");
        getContentPane().add(MenuButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 240, 140, 50));

        StatisticsButton.setText("jButton3");
        getContentPane().add(StatisticsButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 300, 140, 60));

        LogoutButton.setText("jButton4");
        getContentPane().add(LogoutButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 560, 90, 30));

        HistoryButton.setText("jButton1");
        getContentPane().add(HistoryButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 370, 140, 50));

        CashIn.setText("jButton1");
        getContentPane().add(CashIn, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 430, 140, 50));

        jButton1.setText("jButton1");
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 490, 140, 60));

        BG.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/StaffDesign/StaffOrders.png"))); // NOI18N
        getContentPane().add(BG, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void InventoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_InventoryButtonActionPerformed
        // Action handled through the button listener.
    }//GEN-LAST:event_InventoryButtonActionPerformed

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
                new StaffOrders().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel BG;
    private javax.swing.JButton CashIn;
    private javax.swing.JButton HistoryButton;
    private javax.swing.JButton InventoryButton;
    private javax.swing.JButton LogoutButton;
    private javax.swing.JButton MenuButton;
    private javax.swing.JButton StatisticsButton;
    private javax.swing.JPanel cardsContainerPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JButton refreshButton;
    private javax.swing.JComboBox statusComboBox;
    private javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables
}
