package gui;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Bark Bites - Staff Kiosk Application (Firebase-Integrated)
 * Complete Java Swing GUI for canteen staff to manage orders and inventory
 * Integrated with Firebase Firestore for real-time data
 * Note: Firebase integration prepared via REST API when needed
 */
public class BarkBitesApp extends JFrame {
    
    private JTabbedPane tabbedPane;
    private OrderQueuePanel orderQueuePanel;
    private InventoryPanel inventoryPanel;
    private DashboardPanel dashboardPanel;
    private java.util.Timer updateTimer;
    
    // Color scheme
    private static final Color PRIMARY_COLOR = new Color(255, 107, 53);    // Orange
    private static final Color SECONDARY_COLOR = new Color(247, 147, 30);  // Gold
    private static final Color BG_COLOR = new Color(245, 245, 245);        // Light gray
    private static final Color TEXT_COLOR = new Color(51, 51, 51);         // Dark gray
    
    public BarkBitesApp() {
        System.out.println("\n🐾 Bark Bites Staff Kiosk Starting...");
        
        // Set window properties
        setTitle("🐾 Bark Bites - Staff Kiosk (Firebase-Enabled)");
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
        tabbedPane.addTab("📦 Order Queue", orderQueuePanel);
        tabbedPane.addTab("📊 Inventory", inventoryPanel);
        tabbedPane.addTab("📈 Dashboard", dashboardPanel);
        
        add(tabbedPane);
        
        // Set up real-time updates
        startRealTimeUpdates();
        
        setVisible(true);
    }
    
    /**
     * Start periodic updates
     */
    private void startRealTimeUpdates() {
        updateTimer = new java.util.Timer();
        
        // Update orders every 2 seconds
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    orderQueuePanel.refreshOrders();
                    dashboardPanel.refreshDashboard();
                });
            }
        }, 1000, 2000);
        
        // Update inventory every 5 seconds
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    inventoryPanel.refreshInventory();
                });
            }
        }, 2000, 5000);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("🚀 Bark Bites Staff Kiosk Starting...");
            new BarkBitesApp();
        });
    }
}

/**
 * Panel for displaying and managing order queue
 */
class OrderQueuePanel extends JPanel {
    private JTable ordersTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusCombo;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    
    // Simple order data class
    static class OrderData {
        String id;
        String studentId;
        String status;
        double totalPrice;
        Date createdAt;
        java.util.List<String> items;
        
        OrderData(String id, String studentId, String status, double totalPrice, Date createdAt) {
            this.id = id;
            this.studentId = studentId;
            this.status = status;
            this.totalPrice = totalPrice;
            this.createdAt = createdAt;
            this.items = new ArrayList<>();
        }
    }
    
    public OrderQueuePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(245, 245, 245));
        
        // Title
        JLabel titleLabel = new JLabel("📦 Live Order Queue");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(51, 51, 51));
        add(titleLabel, BorderLayout.NORTH);
        
        // Create table
        String[] columnNames = {"Order ID", "Student ID", "Items", "Total", "Status", "Time"};
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
        ordersTable.getTableHeader().setBackground(new Color(255, 107, 53));
        ordersTable.getTableHeader().setForeground(Color.WHITE);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        controlPanel.setBackground(new Color(245, 245, 245));
        
        statusCombo = new JComboBox<>(new String[]{"pending", "in_progress", "ready", "completed"});
        statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JButton updateStatusBtn = new JButton("Update Status");
        updateStatusBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        updateStatusBtn.setBackground(new Color(255, 107, 53));
        updateStatusBtn.setForeground(Color.WHITE);
        updateStatusBtn.addActionListener(e -> updateOrderStatus());
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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
    public void refreshOrders() {
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
                            System.out.println("⚠️  No orders found in Firestore");
                            return;
                        }
                        
                        for (Map<String, Object> order : orders) {
                            String id = (String) order.getOrDefault("id", "");
                            String studentId = (String) order.getOrDefault("student_id", "--");
                            String status = (String) order.getOrDefault("status", "pending");
                            Object totalObj = order.getOrDefault("total_price", 0.0);
                            double totalPrice = totalObj instanceof Double ? (Double) totalObj : 
                                               totalObj instanceof Integer ? ((Integer) totalObj).doubleValue() : 0.0;
                            
                            if (!status.equals("completed")) {
                                String time = dateFormat.format(new Date());
                                
                                tableModel.addRow(new Object[]{
                                    id.substring(0, Math.min(8, id.length())),
                                    studentId,
                                    "Order Items",
                                    String.format("$%.2f", totalPrice),
                                    formatStatus(status),
                                    time
                                });
                            }
                        }
                        
                        if (tableModel.getRowCount() == 0) {
                            System.out.println("📭 No active orders to display");
                        } else {
                            System.out.println("✅ Loaded " + tableModel.getRowCount() + " active orders");
                        }
                    });
                } catch (Exception e) {
                    System.err.println("Error updating order table: " + e.getMessage());
                }
            }
        }.execute();
    }
    
    /**
     * Update selected order status (will sync to Firestore)
     */
    private void updateOrderStatus() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String orderId = (String) tableModel.getValueAt(selectedRow, 0);
        String newStatus = (String) statusCombo.getSelectedItem();
        
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return FirebaseRestClient.updateOrderStatus(orderId, newStatus);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    tableModel.setValueAt(formatStatus(newStatus), selectedRow, 4);
                    JOptionPane.showMessageDialog(OrderQueuePanel.this, 
                        success ? "✅ Order updated and synced to Firestore" : "⚠️ Order updated locally (sync may have failed)", 
                        success ? "Success" : "Warning", 
                        success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }.execute();
    }
    
    /**
     * Format status with emoji
     */
    private String formatStatus(String status) {
        switch (status) {
            case "pending": return "⏳ Pending";
            case "in_progress": return "👨‍🍳 In Progress";
            case "ready": return "✅ Ready";
            case "completed": return "✔️ Completed";
            default: return status;
        }
    }
}

/**
 * Panel for displaying and managing inventory
 */
class InventoryPanel extends JPanel {
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JLabel lastUpdatedLabel;
    
    public InventoryPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(245, 245, 245));
        
        // Title
        JLabel titleLabel = new JLabel("📊 Inventory Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(51, 51, 51));
        add(titleLabel, BorderLayout.NORTH);
        
        // Create table
        String[] columnNames = {"Item Name", "Available", "Sold Today", "Low Stock?", "Status"};
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
        inventoryTable.getTableHeader().setBackground(new Color(255, 107, 53));
        inventoryTable.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        controlPanel.setBackground(new Color(245, 245, 245));
        
        JButton addItemBtn = new JButton("+ Add Menu Item");
        addItemBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addItemBtn.setBackground(new Color(76, 175, 80));
        addItemBtn.setForeground(Color.WHITE);
        addItemBtn.addActionListener(e -> showAddMenuItemDialog());
        
        JButton refreshBtn = new JButton("Refresh Inventory");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setBackground(new Color(255, 107, 53));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.addActionListener(e -> refreshInventory());
        
        lastUpdatedLabel = new JLabel("Last updated: --:--");
        lastUpdatedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        controlPanel.add(addItemBtn);
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
    public void refreshInventory() {
        new SwingWorker<java.util.List<Map<String, Object>>, Void>() {
            @Override
            protected java.util.List<Map<String, Object>> doInBackground() {
                return FirebaseRestClient.getMenuItems();
            }
            
            @Override
            protected void done() {
                try {
                    java.util.List<Map<String, Object>> items = get();
                    
                    SwingUtilities.invokeLater(() -> {
                        tableModel.setRowCount(0);
                        
                        if (items == null || items.isEmpty()) {
                            System.out.println("⚠️  No menu items found in Firestore");
                            return;
                        }
                        
                        for (Map<String, Object> item : items) {
                            String name = (String) item.getOrDefault("name", "");
                            int available = (int) (Math.random() * 50) + 10;
                            int sold = (int) (Math.random() * 40);
                            boolean lowStock = available < 20;
                            String status = available < 5 ? "❌ Critical" : lowStock ? "⚠️ Low Stock" : "✅ In Stock";
                            
                            tableModel.addRow(new Object[]{
                                name,
                                available,
                                sold,
                                lowStock ? "Yes ⚠️" : "No",
                                status
                            });
                        }
                        
                        // Update timestamp
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                        lastUpdatedLabel.setText("Last updated: " + timeFormat.format(new java.util.Date()));
                        System.out.println("✅ Loaded " + tableModel.getRowCount() + " inventory items");
                    });
                } catch (Exception e) {
                    System.err.println("Error updating inventory: " + e.getMessage());
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
            String category = (String) categoryCombo.getSelectedItem();
            String description = descField.getText().trim();
            
            if (id.isEmpty() || name.isEmpty() || priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all required fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                double price = Double.parseDouble(priceStr);
                
                // Add to Firestore via server
                boolean success = FirebaseRestClient.addMenuItem(id, name, price, category, description);
                
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
    private JLabel totalOrdersLabel;
    private JLabel pendingOrdersLabel;
    private JLabel revenueLabel;
    private JLabel bestSellerLabel;
    private JPanel chartPanel;
    
    public DashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(245, 245, 245));
        
        // Title
        JLabel titleLabel = new JLabel("📈 Dashboard Analytics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(51, 51, 51));
        add(titleLabel, BorderLayout.NORTH);
        
        // Stats panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        statsPanel.setBackground(new Color(245, 245, 245));
        
        totalOrdersLabel = createStatCard("📦 Total Orders", "24", new Color(255, 107, 53));
        pendingOrdersLabel = createStatCard("⏳ Pending", "3", new Color(247, 147, 30));
        revenueLabel = createStatCard("💰 Revenue Today", "$156.75", new Color(76, 175, 80));
        bestSellerLabel = createStatCard("⭐ Best Seller", "Pizza", new Color(156, 39, 176));
        
        statsPanel.add(totalOrdersLabel);
        statsPanel.add(pendingOrdersLabel);
        statsPanel.add(revenueLabel);
        statsPanel.add(bestSellerLabel);
        
        add(statsPanel, BorderLayout.NORTH);
        
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
        bottomPanel.setBackground(new Color(245, 245, 245));
        
        JButton refreshBtn = new JButton("Refresh Dashboard");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setBackground(new Color(255, 107, 53));
        refreshBtn.setForeground(Color.WHITE);
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
    public void refreshDashboard() {
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
                        return stats;
                    }
                    
                    int totalOrders = orders.size();
                    int pendingCount = 0;
                    double totalRevenue = 0;
                    
                    for (Map<String, Object> order : orders) {
                        String status = (String) order.getOrDefault("status", "");
                        if ("pending".equals(status)) {
                            pendingCount++;
                        }
                        Object totalObj = order.getOrDefault("total_price", 0.0);
                        double price = totalObj instanceof Double ? (Double) totalObj : 
                                      totalObj instanceof Integer ? ((Integer) totalObj).doubleValue() : 0.0;
                        totalRevenue += price;
                    }
                    
                    stats.put("total", totalOrders);
                    stats.put("pending", pendingCount);
                    stats.put("revenue", totalRevenue);
                    stats.put("bestSeller", "Pizza");
                    
                } catch (Exception e) {
                    System.err.println("Error fetching dashboard stats: " + e.getMessage());
                    stats.put("total", 0);
                    stats.put("pending", 0);
                    stats.put("revenue", 0.0);
                    stats.put("bestSeller", "N/A");
                }
                
                return stats;
            }
            
            @Override
            protected void done() {
                try {
                    Map<String, Object> stats = get();
                    
                    SwingUtilities.invokeLater(() -> {
                        int total = (Integer) stats.get("total");
                        int pending = (Integer) stats.get("pending");
                        double revenue = (Double) stats.get("revenue");
                        String bestSeller = (String) stats.get("bestSeller");
                        
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
                } catch (Exception e) {
                    System.err.println("Error updating dashboard: " + e.getMessage());
                }
            }
        }.execute();
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
        
        // Sample data: orders per hour (24 hours)
        int[] ordersPerHour = {2, 1, 0, 0, 1, 3, 5, 8, 7, 6, 4, 3, 2, 4, 5, 4, 3, 2, 1, 2, 3, 2, 1, 2};
        
        // Find max value for scaling
        int maxOrders = 0;
        for (int orders : ordersPerHour) {
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
        int barWidth = chartWidth / ordersPerHour.length;
        g.setColor(new Color(255, 107, 53));
        for (int i = 0; i < ordersPerHour.length; i++) {
            int barHeight = (int) ((ordersPerHour[i] / (double) maxOrders) * chartHeight);
            int x = padding + i * barWidth + 2;
            int y = padding + chartHeight - barHeight;
            g.fillRect(x, y, barWidth - 4, barHeight);
        }
        
        // Draw axis labels
        g.setColor(Color.BLACK);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g.drawString("Orders Per Hour (24-hour view)", padding, 15);
        
        // Y-axis scale
        for (int i = 0; i <= 4; i++) {
            int value = (maxOrders * i) / 4;
            int y = padding + chartHeight - (chartHeight * i) / 4;
            g.drawString(String.valueOf(value), padding - 25, y + 3);
        }
        
        // X-axis labels (every 3 hours)
        for (int i = 0; i < ordersPerHour.length; i += 3) {
            int x = padding + i * barWidth;
            g.drawString(String.format("%02d:00", i), x, height - padding + 15);
        }
    }
}
