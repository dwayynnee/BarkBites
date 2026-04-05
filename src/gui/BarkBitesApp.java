package gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Bark Bites - Staff Kiosk Application (Firebase-Integrated)
 * Complete Java Swing GUI for canteen staff to manage orders and inventory
 * Integrated with Firebase Firestore for real-time data
 * Note: Firebase integration prepared via REST API when needed
 */
public class BarkBitesApp extends JFrame {
    
    private final JTabbedPane tabbedPane;
    private final OrderQueuePanel orderQueuePanel;
    private final InventoryPanel inventoryPanel;
    private final DashboardPanel dashboardPanel;

    private final CardLayout cardLayout;
    private final JPanel rootPanel;
    
    // Color scheme
    static final Color PRIMARY_COLOR = new Color(255, 107, 53);    // Orange
    static final Color SECONDARY_COLOR = new Color(247, 147, 30);  // Gold
    static final Color BG_COLOR = new Color(245, 245, 245);        // Light gray
    static final Color TEXT_COLOR = new Color(51, 51, 51);         // Dark gray
    
    public BarkBitesApp() {
        System.out.println("\n🐾 Bark Bites Staff Kiosk Starting...");
        
        // Set window properties
        setTitle("Bark Bites - Staff Kiosk");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setBackground(BG_COLOR);
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(BG_COLOR);
        
        // Create tab panels
        orderQueuePanel = new OrderQueuePanel();
        inventoryPanel = new InventoryPanel();
        dashboardPanel = new DashboardPanel();
        
        // Add tabs
        tabbedPane.addTab("Order Queue", orderQueuePanel);
        tabbedPane.addTab("Inventory", inventoryPanel);
        tabbedPane.addTab("Dashboard", dashboardPanel);

        // Root: Home screen -> Main tabs
        cardLayout = new CardLayout();
        rootPanel = new JPanel(cardLayout);
        rootPanel.setBackground(BG_COLOR);

        JPanel homePanel = new HomePanel(() -> cardLayout.show(rootPanel, "MAIN"));
        rootPanel.add(homePanel, "HOME");
        rootPanel.add(tabbedPane, "MAIN");

        setContentPane(rootPanel);
        cardLayout.show(rootPanel, "HOME");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("🚀 Bark Bites Staff Kiosk Starting...");
            new BarkBitesApp().setVisible(true);
        });
    }
}

/**
 * Simple home screen shown before the main panels.
 */
class HomePanel extends JPanel {
    public HomePanel(Runnable onGetStarted) {
        setLayout(new BorderLayout());
        setBackground(BarkBitesApp.BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel welcome = new JLabel("Welcome to Bark Bites Staff Menu", JLabel.CENTER);
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 26));
        welcome.setForeground(BarkBitesApp.TEXT_COLOR);
        add(welcome, BorderLayout.CENTER);

        JButton getStarted = new JButton("Get Started");
        getStarted.setFont(new Font("Segoe UI", Font.BOLD, 16));
        getStarted.setBackground(BarkBitesApp.PRIMARY_COLOR);
        getStarted.setForeground(Color.WHITE);
        getStarted.setFocusPainted(false);
        getStarted.addActionListener(e -> {
            if (onGetStarted != null) {
                onGetStarted.run();
            }
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottom.setBackground(BarkBitesApp.BG_COLOR);
        bottom.add(getStarted);
        add(bottom, BorderLayout.SOUTH);
    }
}

/**
 * Panel for displaying and managing order queue
 */
class OrderQueuePanel extends JPanel {
    private final JTable ordersTable;
    private final DefaultTableModel tableModel;
    private final JComboBox<String> statusCombo;
    private final JButton updateStatusBtn;
    private final JButton refreshBtn;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public OrderQueuePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(BarkBitesApp.BG_COLOR);
        
        // Title
        JLabel titleLabel = new JLabel("Live Order Queue");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(BarkBitesApp.TEXT_COLOR);
        add(titleLabel, BorderLayout.NORTH);
        
        // Create table
        String[] columnNames = {"Doc ID", "Order #", "Student ID", "Items", "Total", "Status", "Created"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        ordersTable = new JTable(tableModel);
        ordersTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ordersTable.setRowHeight(25);
        ordersTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        ordersTable.getTableHeader().setBackground(BarkBitesApp.PRIMARY_COLOR);
        ordersTable.getTableHeader().setForeground(Color.WHITE);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ordersTable.setShowGrid(true);
        ordersTable.setGridColor(new Color(225, 225, 225));
        ordersTable.getTableHeader().setReorderingAllowed(false);

        // Hide Doc ID column (still used for updates)
        ordersTable.getColumnModel().getColumn(0).setMinWidth(0);
        ordersTable.getColumnModel().getColumn(0).setMaxWidth(0);
        ordersTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Status coloring
        ordersTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                                    boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String text = value != null ? value.toString() : "";
                    if (text.contains("Pending")) {
                        c.setBackground(new Color(255, 244, 229));
                    } else if (text.contains("In Progress")) {
                        c.setBackground(new Color(232, 244, 253));
                    } else if (text.contains("Ready")) {
                        c.setBackground(new Color(232, 245, 233));
                    } else if (text.contains("Completed")) {
                        c.setBackground(new Color(240, 240, 240));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        controlPanel.setBackground(BarkBitesApp.BG_COLOR);
        
        statusCombo = new JComboBox<>(new String[]{"pending", "in_progress", "ready", "completed"});
        statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        updateStatusBtn = new JButton("Update Status");
        updateStatusBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        updateStatusBtn.setBackground(BarkBitesApp.PRIMARY_COLOR);
        updateStatusBtn.setForeground(Color.WHITE);
        updateStatusBtn.setFocusPainted(false);
        updateStatusBtn.addActionListener(e -> updateOrderStatus());
        
        refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> refreshOrders());
        
        controlPanel.add(new JLabel("Update to:"));
        controlPanel.add(statusCombo);
        controlPanel.add(updateStatusBtn);
        controlPanel.add(refreshBtn);
        
        add(controlPanel, BorderLayout.SOUTH);
        
        // Load initial data
        refreshOrders();
    }
    
    /**
     * Refresh orders from Firestore via REST API
     */
    public final void refreshOrders() {
        new SwingWorker<java.util.List<Map<String, Object>>, Void>() {
            @Override
            protected java.util.List<Map<String, Object>> doInBackground() {
                return FirebaseRestClient.getOrders();
            }
            
            @Override
            protected void done() {
                try {
                    java.util.List<Map<String, Object>> orders = get();
                    
                    SwingUtilities.invokeLater(() -> {
                        tableModel.setRowCount(0);
                        
                        if (orders == null || orders.isEmpty()) {
                            System.out.println("No orders found in Firestore");
                            return;
                        }
                        
                        for (Map<String, Object> order : orders) {
                            String docId = asString(order.get("id"), "");
                            String orderNumber = asString(order.get("order_number"), "--");
                            String studentId = asString(order.get("student_id"), "--");
                            String status = asString(order.get("status"), "pending");
                            double totalPrice = asDouble(order.get("total_price"), 0.0);
                            String itemsSummary = formatOrderItems(order.get("items"));
                            String createdTime = formatOrderTime(order.get("created_at"));

                            if (!"completed".equalsIgnoreCase(status)) {
                                tableModel.addRow(new Object[]{
                                    docId,
                                    orderNumber,
                                    studentId,
                                    itemsSummary,
                                    String.format("$%.2f", totalPrice),
                                    formatStatus(status),
                                    createdTime
                                });
                            }
                        }
                        
                        if (tableModel.getRowCount() == 0) {
                            System.out.println("📭 No active orders to display");
                        } else {
                            System.out.println("✅ Loaded " + tableModel.getRowCount() + " active orders");
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Error updating order table: " + e.getMessage());
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    System.err.println("Error updating order table: " + (cause != null ? cause.getMessage() : e.getMessage()));
                }
            }
        }.execute();
    }
    
    /**
     * Update selected order status (will sync to Firestore)
     */
    private void updateOrderStatus() {
        int viewRow = ordersTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = viewRow;
        if (ordersTable.getRowSorter() != null) {
            selectedRow = ordersTable.convertRowIndexToModel(viewRow);
        }

        String orderId = asString(tableModel.getValueAt(selectedRow, 0), "");
        String newStatus = (String) statusCombo.getSelectedItem();

        if (orderId.isBlank()) {
            JOptionPane.showMessageDialog(this, "Selected order has no Doc ID", "Update Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        updateStatusBtn.setEnabled(false);
        refreshBtn.setEnabled(false);
        
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return FirebaseRestClient.updateOrderStatus(orderId, newStatus);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(OrderQueuePanel.this,
                            "✅ Order updated and synced to Firestore",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(OrderQueuePanel.this,
                            "❌ Failed to update order status.\nMake sure the Node server is running and Firestore is initialized.",
                            "Update Failed",
                            JOptionPane.ERROR_MESSAGE);
                    }

                    // Refresh from source of truth (also avoids row-index issues if completed rows disappear)
                    refreshOrders();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Error: " + e.getMessage());
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    System.err.println("Error: " + (cause != null ? cause.getMessage() : e.getMessage()));
                    JOptionPane.showMessageDialog(OrderQueuePanel.this,
                        "❌ Error updating order: " + (cause != null ? cause.getMessage() : e.getMessage()),
                        "Update Failed",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    updateStatusBtn.setEnabled(true);
                    refreshBtn.setEnabled(true);
                }
            }
        }.execute();
    }

    private static String asString(Object value, String defaultValue) {
        if (value == null) return defaultValue;
        String s = String.valueOf(value);
        return s == null || s.isBlank() ? defaultValue : s;
    }

    private static double asDouble(Object value, double defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String formatOrderItems(Object itemsObj) {
        if (itemsObj == null) return "--";

        if (itemsObj instanceof java.util.List<?> list) {
            java.util.List<String> parts = new java.util.ArrayList<>();
            for (Object entry : list) {
                if (entry instanceof Map<?, ?> m) {
                    String name = asString(m.get("name"), "Item");
                    int qty = (int) asDouble(m.get("quantity"), 1);
                    parts.add(name + " x" + qty);
                } else {
                    parts.add(String.valueOf(entry));
                }
            }
            return parts.isEmpty() ? "--" : String.join(", ", parts);
        }

        return String.valueOf(itemsObj);
    }

    private String formatOrderTime(Object createdAtObj) {
        if (createdAtObj == null) return "--";

        if (createdAtObj instanceof String s) {
            try {
                Instant inst = Instant.parse(s);
                return timeFormatter.withZone(ZoneId.systemDefault()).format(inst);
            } catch (DateTimeParseException e) {
                return s;
            }
        }

        if (createdAtObj instanceof Map<?, ?> m) {
            Object seconds = m.containsKey("seconds") ? m.get("seconds") : m.get("_seconds");
            if (seconds instanceof Number n) {
                Instant inst = Instant.ofEpochSecond(n.longValue());
                return timeFormatter.withZone(ZoneId.systemDefault()).format(inst);
            }
        }

        return String.valueOf(createdAtObj);
    }
    
    /**
     * Format status with emoji
     */
    private String formatStatus(String status) {
        return switch (status) {
            case "pending" -> "⏳ Pending";
            case "in_progress" -> "👨‍🍳 In Progress";
            case "ready" -> "✅ Ready";
            case "completed" -> "✔️ Completed";
            default -> status;
        };
    }
}

/**
 * Panel for displaying and managing inventory
 */
class InventoryPanel extends JPanel {
    private final JTable inventoryTable;
    private final DefaultTableModel tableModel;
    private final JLabel lastUpdatedLabel;
    
    public InventoryPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(BarkBitesApp.BG_COLOR);
        
        // Title
        JLabel titleLabel = new JLabel("Inventory Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(BarkBitesApp.TEXT_COLOR);
        add(titleLabel, BorderLayout.NORTH);
        
        // Create table
        String[] columnNames = {"Item ID", "Item Name", "Available", "Sold Today", "Low Stock?", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        inventoryTable = new JTable(tableModel);
        inventoryTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        inventoryTable.setRowHeight(25);
        inventoryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        inventoryTable.getTableHeader().setBackground(BarkBitesApp.PRIMARY_COLOR);
        inventoryTable.getTableHeader().setForeground(Color.WHITE);
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        controlPanel.setBackground(BarkBitesApp.BG_COLOR);
        
        JButton addItemBtn = new JButton("+ Add Menu Item");
        addItemBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addItemBtn.setBackground(new Color(76, 175, 80));
        addItemBtn.setForeground(Color.WHITE);
        addItemBtn.setFocusPainted(false);
        addItemBtn.addActionListener(e -> showAddMenuItemDialog());

        JButton deleteItemBtn = new JButton("Delete Menu Item");
        deleteItemBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deleteItemBtn.setBackground(new Color(244, 67, 54));
        deleteItemBtn.setForeground(Color.WHITE);
        deleteItemBtn.setFocusPainted(false);
        deleteItemBtn.addActionListener(e -> deleteSelectedMenuItem());
        
        JButton refreshBtn = new JButton("Refresh Inventory");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setBackground(BarkBitesApp.PRIMARY_COLOR);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> refreshInventory());
        
        lastUpdatedLabel = new JLabel("Last updated: --:--");
        lastUpdatedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        controlPanel.add(addItemBtn);
        controlPanel.add(deleteItemBtn);
        controlPanel.add(refreshBtn);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(lastUpdatedLabel);
        
        add(controlPanel, BorderLayout.SOUTH);
        
        // Load initial data
        refreshInventory();
    }
    
    /**
     * Refresh inventory from Firestore via REST API
     */
    public final void refreshInventory() {
        new SwingWorker<java.util.Map<String, java.util.List<Map<String, Object>>>, Void>() {
            @Override
            protected java.util.Map<String, java.util.List<Map<String, Object>>> doInBackground() {
                java.util.Map<String, java.util.List<Map<String, Object>>> result = new java.util.HashMap<>();
                result.put("menu_items", FirebaseRestClient.getMenuItems());
                result.put("inventory", FirebaseRestClient.getInventory());
                return result;
            }
            
            @Override
            protected void done() {
                try {
                    java.util.Map<String, java.util.List<Map<String, Object>>> result = get();
                    java.util.List<Map<String, Object>> items = result != null ? result.get("menu_items") : null;
                    java.util.List<Map<String, Object>> inventory = result != null ? result.get("inventory") : null;
                    
                    SwingUtilities.invokeLater(() -> {
                        tableModel.setRowCount(0);
                        
                        if (items == null || items.isEmpty()) {
                            System.out.println("⚠️  No menu items found in Firestore");
                            return;
                        }

                        java.util.Map<String, Map<String, Object>> invByMenuItemId = new java.util.HashMap<>();
                        if (inventory != null) {
                            for (Map<String, Object> inv : inventory) {
                                String menuItemId = String.valueOf(inv.getOrDefault("menu_item_id", inv.getOrDefault("id", "")));
                                if (menuItemId != null && !menuItemId.isBlank()) {
                                    invByMenuItemId.put(menuItemId, inv);
                                }
                            }
                        }
                        
                        for (Map<String, Object> item : items) {
                            String id = String.valueOf(item.getOrDefault("id", ""));
                            String name = String.valueOf(item.getOrDefault("name", ""));

                            boolean availableFlag = Boolean.TRUE.equals(item.get("available"))
                                || "true".equalsIgnoreCase(String.valueOf(item.get("available")));

                            Map<String, Object> inv = invByMenuItemId.get(id);
                            Integer qtyAvailable = inv != null ? toInt(inv.get("quantity_available")) : null;
                            Integer qtySold = inv != null ? toInt(inv.get("quantity_sold_today")) : null;
                            Integer threshold = inv != null ? toInt(inv.get("low_stock_threshold")) : null;
                            Boolean outOfStock = inv != null ? toBool(inv.getOrDefault("is_out_of_stock", inv.get("out_of_stock"))) : null;

                            int availableQtySafe = qtyAvailable != null ? qtyAvailable : 0;
                            int thresholdSafe = threshold != null ? threshold : 10;
                            boolean lowStock = qtyAvailable != null && availableQtySafe <= thresholdSafe;
                            boolean isOut = (outOfStock != null && outOfStock) || (qtyAvailable != null && availableQtySafe <= 0);

                            String status;
                            if (inv == null) {
                                status = "⚠️ No Inventory Data";
                            } else if (!availableFlag) {
                                status = "⛔ Unavailable";
                            } else if (isOut) {
                                status = "❌ Out of Stock";
                            } else if (lowStock) {
                                status = "⚠️ Low Stock";
                            } else {
                                status = "✅ In Stock";
                            }

                            String lowStockText = qtyAvailable == null ? "—" : (lowStock ? "Yes ⚠️" : "No");
                            
                            tableModel.addRow(new Object[]{
                                id,
                                name,
                                qtyAvailable != null ? qtyAvailable : "—",
                                qtySold != null ? qtySold : "—",
                                lowStockText,
                                status
                            });
                        }
                        
                        // Update timestamp
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                        lastUpdatedLabel.setText("Last updated: " + timeFormat.format(new java.util.Date()));
                        System.out.println("✅ Loaded " + tableModel.getRowCount() + " inventory items");
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Error updating inventory: " + e.getMessage());
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    System.err.println("Error updating inventory: " + (cause != null ? cause.getMessage() : e.getMessage()));
                }
            }
        }.execute();
    }

    private static Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Boolean toBool(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean b) return b;
        String s = String.valueOf(value).trim();
        if (s.isEmpty()) return null;
        return "true".equalsIgnoreCase(s) || "1".equals(s) || "yes".equalsIgnoreCase(s);
    }

    private void deleteSelectedMenuItem() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a menu item to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String itemId = String.valueOf(tableModel.getValueAt(selectedRow, 0));
        String itemName = String.valueOf(tableModel.getValueAt(selectedRow, 1));
        if (itemId == null || itemId.trim().isEmpty() || "null".equalsIgnoreCase(itemId.trim())) {
            JOptionPane.showMessageDialog(this, "Selected row has no Item ID", "Delete Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
            this,
            String.format("Delete menu item '%s' (ID: %s)?\nThis will remove it from Firestore.", itemName, itemId),
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return FirebaseRestClient.deleteMenuItem(itemId);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(InventoryPanel.this, "✅ Menu item deleted", "Success", JOptionPane.INFORMATION_MESSAGE);
                        refreshInventory();
                    } else {
                        JOptionPane.showMessageDialog(InventoryPanel.this,
                            "❌ Failed to delete menu item.\nMake sure the Node server is running and Firestore is initialized.",
                            "Delete Failed",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(InventoryPanel.this,
                        "❌ Error deleting menu item: " + e.getMessage(),
                        "Delete Failed",
                        JOptionPane.ERROR_MESSAGE);
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    JOptionPane.showMessageDialog(InventoryPanel.this,
                        "❌ Error deleting menu item: " + (cause != null ? cause.getMessage() : e.getMessage()),
                        "Delete Failed",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
    
    /**
     * Show dialog to add a new menu item
     */
    private void showAddMenuItemDialog() {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Add Menu Item", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create form fields
        JLabel idLabel = new JLabel("Item ID:");
        JTextField idField = new JTextField();
        
        JLabel nameLabel = new JLabel("Item Name:");
        JTextField nameField = new JTextField();
        
        JLabel priceLabel = new JLabel("Price:");
        JTextField priceField = new JTextField();

        JLabel qtyLabel = new JLabel("Quantity Available:");
        JTextField qtyField = new JTextField("0");
        
        JLabel categoryLabel = new JLabel("Category:");
        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{"Main Course", "Sides", "Dessert", "Drink"});
        
        JLabel descLabel = new JLabel("Description:");
        JTextField descField = new JTextField();
        
        // Add fields to panel
        panel.add(idLabel);
        panel.add(idField);
        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(priceLabel);
        panel.add(priceField);
        panel.add(qtyLabel);
        panel.add(qtyField);
        panel.add(categoryLabel);
        panel.add(categoryCombo);
        panel.add(descLabel);
        panel.add(descField);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        JButton addBtn = new JButton("✓ Add Item");
        addBtn.setBackground(new Color(76, 175, 80));
        addBtn.setForeground(Color.WHITE);
        addBtn.addActionListener(e -> {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String priceStr = priceField.getText().trim();
            String qtyStr = qtyField.getText().trim();
            String category = (String) categoryCombo.getSelectedItem();
            String description = descField.getText().trim();
            
            if (id.isEmpty() || name.isEmpty() || priceStr.isEmpty() || qtyStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all required fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                double price = Double.parseDouble(priceStr);

                int quantityAvailable;
                try {
                    quantityAvailable = Integer.parseInt(qtyStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Quantity must be a whole number", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (quantityAvailable < 0) {
                    JOptionPane.showMessageDialog(dialog, "Quantity cannot be negative", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Add to Firestore via server
                boolean success = FirebaseRestClient.addMenuItem(id, name, price, category, description, quantityAvailable);
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Menu item added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    refreshInventory();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add menu item", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Price must be a valid number", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton cancelBtn = new JButton("✗ Cancel");
        cancelBtn.setBackground(new Color(244, 67, 54));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(addBtn);
        buttonPanel.add(cancelBtn);
        
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }
}

/**
 * Panel for displaying dashboard analytics
 */
class DashboardPanel extends JPanel {
    private final JLabel totalOrdersLabel;
    private final JLabel pendingOrdersLabel;
    private final JLabel revenueLabel;
    private final JLabel bestSellerLabel;
    private final JPanel chartPanel;
    private int[] ordersPerHour = new int[24];
    
    public DashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(BarkBitesApp.BG_COLOR);

        // Title + stats (single top panel so NORTH isn't overwritten)
        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.setBackground(BarkBitesApp.BG_COLOR);

        JLabel titleLabel = new JLabel("Dashboard Analytics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(BarkBitesApp.TEXT_COLOR);
        topPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        statsPanel.setBackground(BarkBitesApp.BG_COLOR);
        
        totalOrdersLabel = createStatCard("📦 Total Orders", "--", BarkBitesApp.PRIMARY_COLOR);
        pendingOrdersLabel = createStatCard("⏳ Pending", "--", BarkBitesApp.SECONDARY_COLOR);
        revenueLabel = createStatCard("💰 Revenue Today", "--", new Color(76, 175, 80));
        bestSellerLabel = createStatCard("⭐ Best Seller", "--", new Color(156, 39, 176));
        
        statsPanel.add(totalOrdersLabel);
        statsPanel.add(pendingOrdersLabel);
        statsPanel.add(revenueLabel);
        statsPanel.add(bestSellerLabel);

        topPanel.add(statsPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);
        
        // Chart panel
        chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawChart((Graphics2D) g);
            }
        };
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        
        add(chartPanel, BorderLayout.CENTER);
        
        // Refresh button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        bottomPanel.setBackground(BarkBitesApp.BG_COLOR);
        
        JButton refreshBtn = new JButton("Refresh Dashboard");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setBackground(BarkBitesApp.PRIMARY_COLOR);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> refreshDashboard());
        
        bottomPanel.add(refreshBtn);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Initialize data
        refreshDashboard();
    }
    
    /**
     * Create a stat card
     */
    private JLabel createStatCard(String label, String value, Color bgColor) {
        JLabel card = new JLabel("<html><center>" + label + "<br><font size=5><b>" + value + "</b></font></center></html>");
        card.setOpaque(true);
        card.setBackground(bgColor);
        card.setForeground(Color.WHITE);
        card.setFont(new Font("Segoe UI", Font.BOLD, 14));
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.setHorizontalAlignment(JLabel.CENTER);
        card.setVerticalAlignment(JLabel.CENTER);
        return card;
    }
    
    /**
     * Refresh dashboard from Firestore via REST API
     */
    public final void refreshDashboard() {
        new SwingWorker<Map<String, Object>, Void>() {
            @Override
            protected Map<String, Object> doInBackground() {
                Map<String, Object> stats = new HashMap<>();
                
                try {
                    java.util.List<Map<String, Object>> orders = FirebaseRestClient.getOrders();
                    
                    if (orders == null || orders.isEmpty()) {
                        stats.put("total", 0);
                        stats.put("pending", 0);
                        stats.put("revenue", 0.0);
                        stats.put("bestSeller", "N/A");
                        stats.put("ordersPerHour", new int[24]);
                        return stats;
                    }

                    LocalDate today = LocalDate.now();
                    int totalOrders = orders.size();
                    int pendingCount = 0;
                    double revenueToday = 0.0;
                    java.util.Map<String, Integer> itemCounts = new java.util.HashMap<>();

                    int[] perHour = new int[24];
                    Instant now = Instant.now();

                    for (Map<String, Object> order : orders) {
                        String status = String.valueOf(order.getOrDefault("status", ""));
                        if ("pending".equalsIgnoreCase(status)) {
                            pendingCount++;
                        }

                        Instant createdAt = parseToInstant(order.get("created_at"));
                        if (createdAt != null) {
                            // Revenue + best seller for today's orders
                            LocalDate createdDate = createdAt.atZone(ZoneId.systemDefault()).toLocalDate();
                            if (today.equals(createdDate)) {
                                revenueToday += asDouble(order.get("total_price"), 0.0);
                                Object itemsObj = order.get("items");
                                if (itemsObj instanceof java.util.List<?> list) {
                                    for (Object entry : list) {
                                        if (entry instanceof Map<?, ?> m) {
                                            Object nameObj = m.containsKey("name") ? m.get("name") : "Item";
                                            String name = String.valueOf(nameObj);
                                            int qty = (int) asDouble(m.get("quantity"), 1);
                                            itemCounts.put(name, itemCounts.getOrDefault(name, 0) + Math.max(1, qty));
                                        }
                                    }
                                }
                            }

                            // Orders per hour (last 24h)
                            long hoursAgo = java.time.Duration.between(createdAt, now).toHours();
                            if (hoursAgo >= 0 && hoursAgo < 24) {
                                int bucket = (int) (23 - hoursAgo); // oldest -> newest
                                perHour[bucket]++;
                            }
                        }
                    }

                    String bestSeller = "N/A";
                    int bestCount = 0;
                    for (Map.Entry<String, Integer> e : itemCounts.entrySet()) {
                        if (e.getValue() > bestCount) {
                            bestCount = e.getValue();
                            bestSeller = e.getKey();
                        }
                    }

                    stats.put("total", totalOrders);
                    stats.put("pending", pendingCount);
                    stats.put("revenue", revenueToday);
                    stats.put("bestSeller", bestSeller);
                    stats.put("ordersPerHour", perHour);
                    
                } catch (Exception e) {
                    System.err.println("Error fetching dashboard stats: " + e.getMessage());
                    stats.put("total", 0);
                    stats.put("pending", 0);
                    stats.put("revenue", 0.0);
                    stats.put("bestSeller", "N/A");
                    stats.put("ordersPerHour", new int[24]);
                }
                
                return stats;
            }
            
            @Override
            protected void done() {
                try {
                    Map<String, Object> stats = get();
                    
                    SwingUtilities.invokeLater(() -> {
                        int total = (int) asDouble(stats.get("total"), 0);
                        int pending = (int) asDouble(stats.get("pending"), 0);
                        double revenue = asDouble(stats.get("revenue"), 0.0);
                        String bestSeller = String.valueOf(stats.getOrDefault("bestSeller", "N/A"));

                        Object oph = stats.get("ordersPerHour");
                        if (oph instanceof int[] arr && arr.length == 24) {
                            ordersPerHour = arr;
                        } else {
                            ordersPerHour = new int[24];
                        }
                        
                        totalOrdersLabel.setText(String.format(
                            "<html><center>📦 Total Orders<br><font size=5><b>%d</b></font></center></html>", total));
                        pendingOrdersLabel.setText(String.format(
                            "<html><center>⏳ Pending<br><font size=5><b>%d</b></font></center></html>", pending));
                        revenueLabel.setText(String.format(
                            "<html><center>💰 Revenue Today<br><font size=5><b>$%.2f</b></font></center></html>", revenue));
                        bestSellerLabel.setText(String.format(
                            "<html><center>⭐ Best Seller<br><font size=5><b>%s</b></font></center></html>", bestSeller));
                        
                        chartPanel.repaint();
                        System.out.println("✅ Dashboard updated: " + total + " orders, $" + String.format("%.2f", revenue));
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Error updating dashboard: " + e.getMessage());
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    System.err.println("Error updating dashboard: " + (cause != null ? cause.getMessage() : e.getMessage()));
                }
            }
        }.execute();
    }

    private static double asDouble(Object value, double defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static Instant parseToInstant(Object value) {
        if (value == null) return null;
        if (value instanceof String s) {
            try {
                return Instant.parse(s);
            } catch (DateTimeParseException e) {
                return null;
            }
        }
        if (value instanceof Map<?, ?> m) {
            Object seconds = m.containsKey("seconds") ? m.get("seconds") : m.get("_seconds");
            if (seconds instanceof Number n) {
                return Instant.ofEpochSecond(n.longValue());
            }
        }
        return null;
    }
    
    /**
     * Draw bar chart for orders per hour
     */
    private void drawChart(Graphics2D g) {
        int width = chartPanel.getWidth();
        int height = chartPanel.getHeight();
        int padding = 30;
        int chartWidth = width - 2 * padding;
        int chartHeight = height - 2 * padding;
        
        int[] data = (ordersPerHour != null && ordersPerHour.length == 24) ? ordersPerHour : new int[24];
        
        // Find max value for scaling
        int maxOrders = 0;
        for (int orders : data) {
            maxOrders = Math.max(maxOrders, orders);
        }
        if (maxOrders == 0) maxOrders = 1;
        
        // Draw background grid
        g.setColor(new Color(220, 220, 220));
        g.setStroke(new BasicStroke(0.5f));
        for (int i = 0; i <= 4; i++) {
            int y = padding + (chartHeight * i) / 4;
            g.drawLine(padding, y, width - padding, y);
        }
        
        // Draw bars
        int barWidth = chartWidth / data.length;
        g.setColor(BarkBitesApp.PRIMARY_COLOR);
        for (int i = 0; i < data.length; i++) {
            int barHeight = (int) ((data[i] / (double) maxOrders) * chartHeight);
            int x = padding + i * barWidth + 2;
            int y = padding + chartHeight - barHeight;
            g.fillRect(x, y, barWidth - 4, barHeight);
        }
        
        // Draw axis labels
        g.setColor(Color.BLACK);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g.drawString("Orders Per Hour (last 24h)", padding, 15);
        
        // Y-axis scale
        for (int i = 0; i <= 4; i++) {
            int value = (maxOrders * i) / 4;
            int y = padding + chartHeight - (chartHeight * i) / 4;
            g.drawString(String.valueOf(value), padding - 25, y + 3);
        }
        
        // X-axis labels (every 3 hours)
        int nowHour = java.time.LocalTime.now().getHour();
        for (int i = 0; i < data.length; i += 3) {
            int hoursAgo = 23 - i;
            int hour = nowHour - hoursAgo;
            hour %= 24;
            if (hour < 0) hour += 24;

            int x = padding + i * barWidth;
            g.drawString(String.format("%02d:00", hour), x, height - padding + 15);
        }
    }
}
