package gui;

import data.BarkBitesSystem;
import data.InventoryManager;
import data.OrderManager;
import models.OrderStatus;
import models.PosOrder;
import models.Product;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Staff Management GUI (1600x900)
 *
 * Encapsulation + Abstraction: the staff UI NEVER touches arrays directly.
 * It calls InventoryManager / OrderManager methods.
 */
public final class StaffManagementFrame extends JFrame {
    public static final int WIDTH = 1600;
    public static final int HEIGHT = 900;

    private final InventoryManager inventory;
    private final OrderManager orders;

    private final DefaultTableModel inventoryModel;
    private final JTable inventoryTable;

    private final DefaultTableModel ordersModel;
    private final JTable ordersTable;

    public StaffManagementFrame(BarkBitesSystem system) {
        if (system == null) {
            throw new IllegalArgumentException("system is required");
        }

        this.inventory = system.getInventoryManager();
        this.orders = system.getOrderManager();

        setTitle("Bark Bites - Staff Management");
        setSize(WIDTH, HEIGHT);
        setLocationByPlatform(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLayeredPane layered = new JLayeredPane();
        layered.setLayout(null);
        setContentPane(layered);

        JLabel bg = buildBackgroundLabel("images" + File.separator + "staff-management.png", WIDTH, HEIGHT);
        bg.setBounds(0, 0, WIDTH, HEIGHT);
        layered.add(bg, Integer.valueOf(0));

        // Inventory table (left)
        inventoryModel = new DefaultTableModel(new String[] { "ID", "Name", "Type", "Price", "Stock" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        inventoryTable = new JTable(inventoryModel);
        styleTable(inventoryTable);

        JScrollPane inventoryScroll = new JScrollPane(inventoryTable);
        inventoryScroll.setBounds(30, 100, 740, 760);
        layered.add(inventoryScroll, Integer.valueOf(1));

        // Orders table (right)
        ordersModel = new DefaultTableModel(new String[] { "OrderId", "#", "Student", "Total", "Status", "Created" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ordersTable = new JTable(ordersModel);
        styleTable(ordersTable);

        JScrollPane ordersScroll = new JScrollPane(ordersTable);
        ordersScroll.setBounds(820, 100, 740, 760);
        layered.add(ordersScroll, Integer.valueOf(1));

        // Hitbox buttons (invisible) — place over Canva controls.
        JButton refresh = createHitboxButton("Refresh tables");
        refresh.setBounds(30, 30, 160, 50);
        refresh.addActionListener(e -> refreshTables());
        layered.add(refresh, Integer.valueOf(2));

        JButton restock = createHitboxButton("Set stock for selected inventory item");
        restock.setBounds(210, 30, 220, 50);
        restock.addActionListener(e -> restockSelected());
        layered.add(restock, Integer.valueOf(2));

        JButton setStatus = createHitboxButton("Set status for selected order");
        setStatus.setBounds(820, 30, 240, 50);
        setStatus.addActionListener(e -> setSelectedOrderStatus());
        layered.add(setStatus, Integer.valueOf(2));

        refreshTables();
    }

    public void refreshTables() {
        reloadInventory();
        reloadOrders();
    }

    private void reloadInventory() {
        inventoryModel.setRowCount(0);
        for (Product p : inventory.getAllProductsSortedByNameSnapshot()) {
            inventoryModel.addRow(new Object[] {
                    p.getId(),
                    p.getName(),
                    p.getTypeLabel(),
                    p.getPriceDisplay(),
                    p.getStock()
            });
        }
    }

    private void reloadOrders() {
        ordersModel.setRowCount(0);
        for (PosOrder o : orders.getAllOrdersSnapshot()) {
            ordersModel.addRow(new Object[] {
                    o.getId(),
                    o.getOrderNumber(),
                    o.getStudentId(),
                    o.getTotalDisplay(),
                    o.getStatus().name(),
                    o.getCreatedAt().toString()
            });
        }

        // Optional: hide OrderId column from view while keeping it for updates.
        ordersTable.getColumnModel().getColumn(0).setMinWidth(0);
        ordersTable.getColumnModel().getColumn(0).setMaxWidth(0);
        ordersTable.getColumnModel().getColumn(0).setWidth(0);
    }

    private void restockSelected() {
        int row = inventoryTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an inventory row first.");
            return;
        }
        String productId = String.valueOf(inventoryModel.getValueAt(row, 0));

        String input = JOptionPane.showInputDialog(this, "New stock for " + productId + ":", "0");
        if (input == null) {
            return;
        }

        try {
            int newStock = Integer.parseInt(input.trim());
            boolean ok = inventory.setStock(productId, newStock);
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Product not found: " + productId);
                return;
            }
            reloadInventory();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid integer.");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void setSelectedOrderStatus() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an order row first.");
            return;
        }

        String orderId = String.valueOf(ordersModel.getValueAt(row, 0));

        OrderStatus chosen = (OrderStatus) JOptionPane.showInputDialog(
                this,
                "Set new status:",
                "Order Status",
                JOptionPane.QUESTION_MESSAGE,
                null,
                OrderStatus.values(),
                OrderStatus.IN_PROGRESS
        );

        if (chosen == null) {
            return;
        }

        boolean ok = orders.updateStatus(orderId, chosen);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Order not found: " + orderId);
            return;
        }

        reloadOrders();
    }

    private static JButton createHitboxButton(String tooltip) {
        JButton b = new JButton();
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setToolTipText(tooltip);
        return b;
    }

    private static void styleTable(JTable table) {
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setFillsViewportHeight(true);
    }

    private static JLabel buildBackgroundLabel(String path, int w, int h) {
        File f = new File(path);
        if (f.exists()) {
            ImageIcon raw = new ImageIcon(path);
            Image scaled = raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new JLabel(new ImageIcon(scaled));
        }

        JLabel fallback = new JLabel("(Add staff-management.png)", SwingConstants.CENTER);
        fallback.setOpaque(true);
        fallback.setBackground(new Color(245, 245, 245));
        fallback.setForeground(new Color(120, 120, 120));
        return fallback;
    }
}
