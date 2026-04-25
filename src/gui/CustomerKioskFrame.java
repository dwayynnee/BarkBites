package gui;

import data.BarkBitesSystem;
import data.CartManager;
import data.InventoryManager;
import data.OrderManager;
import models.CartLine;
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
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Customer Ordering GUI (360x650)
 *
 * UI notes for your Canva workflow:
 * - Uses null layout to place components at exact pixel coordinates.
 * - The background image is optional; if it doesn't exist yet, we show a plain fallback.
 * - Buttons are created as "hitboxes" (invisible) so they can sit over the Canva design.
 */
public final class CustomerKioskFrame extends JFrame {
    public static final int WIDTH = 360;
    public static final int HEIGHT = 650;

    private final InventoryManager inventory;
    private final OrderManager orders;
    private final CartManager cart;
    private final Runnable onOrderPlaced;

    private final DefaultTableModel menuModel;
    private final JTable menuTable;

    private final DefaultTableModel cartModel;
    private final JTable cartTable;

    private final JLabel totalLabel;
    private final JTextField studentIdField;

    public CustomerKioskFrame(BarkBitesSystem system, Runnable onOrderPlaced) {
        if (system == null) {
            throw new IllegalArgumentException("system is required");
        }

        this.inventory = system.getInventoryManager();
        this.orders = system.getOrderManager();
        this.cart = new CartManager(20);
        this.onOrderPlaced = onOrderPlaced;

        setTitle("Bark Bites - Customer Kiosk");
        setSize(WIDTH, HEIGHT);
        setResizable(false);
        setLocationByPlatform(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLayeredPane layered = new JLayeredPane();
        layered.setLayout(null);
        setContentPane(layered);

        JLabel bg = buildBackgroundLabel("images" + File.separator + "customer-kiosk.png", WIDTH, HEIGHT);
        bg.setBounds(0, 0, WIDTH, HEIGHT);
        layered.add(bg, Integer.valueOf(0));

        // Student ID (simple mock field for prototype)
        studentIdField = new JTextField("S0001");
        studentIdField.setBounds(10, 10, 120, 28);
        layered.add(studentIdField, Integer.valueOf(1));

        // Menu table
        menuModel = new DefaultTableModel(new String[] { "ID", "Name", "Type", "Price", "Stock" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        menuTable = new JTable(menuModel);
        styleTable(menuTable);

        JScrollPane menuScroll = new JScrollPane(menuTable);
        menuScroll.setBounds(10, 50, 340, 240);
        layered.add(menuScroll, Integer.valueOf(1));

        // Cart table
        cartModel = new DefaultTableModel(new String[] { "ID", "Name", "Qty", "Subtotal" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cartTable = new JTable(cartModel);
        styleTable(cartTable);

        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setBounds(10, 330, 340, 180);
        layered.add(cartScroll, Integer.valueOf(1));

        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalLabel.setForeground(new Color(30, 30, 30));
        totalLabel.setBounds(10, 520, 200, 28);
        layered.add(totalLabel, Integer.valueOf(1));

        // Hitbox buttons (invisible by default) — place over Canva buttons.
        JButton addSelected = createHitboxButton("Add selected menu item");
        addSelected.setBounds(220, 10, 130, 28);
        addSelected.addActionListener(e -> addSelectedMenuItemToCart());
        layered.add(addSelected, Integer.valueOf(2));

        JButton removeSelected = createHitboxButton("Remove selected cart line");
        removeSelected.setBounds(220, 520, 130, 28);
        removeSelected.addActionListener(e -> removeSelectedCartLine());
        layered.add(removeSelected, Integer.valueOf(2));

        JButton checkout = createHitboxButton("Checkout");
        checkout.setBounds(10, 560, 340, 50);
        checkout.addActionListener(e -> checkout());
        layered.add(checkout, Integer.valueOf(2));

        // Initial paint
        reloadMenu();
        reloadCart();
    }

    private void reloadMenu() {
        menuModel.setRowCount(0);
        for (Product p : inventory.getAllProductsSortedByNameSnapshot()) {
            menuModel.addRow(new Object[] {
                    p.getId(),
                    p.getName(),
                    p.getTypeLabel(),
                    p.getPriceDisplay(),
                    p.getStock()
            });
        }
    }

    private void reloadCart() {
        cartModel.setRowCount(0);
        CartLine[] lines = cart.getLinesSnapshot();
        for (CartLine line : lines) {
            Product p = inventory.getProductByIdSnapshot(line.getProductId());
            String name = (p == null) ? "(Unknown)" : p.getName();
            int unit = (p == null) ? 0 : p.getPriceCents();
            int subtotal = unit * line.getQuantity();
            cartModel.addRow(new Object[] {
                    line.getProductId(),
                    name,
                    line.getQuantity(),
                    Product.formatMoney(subtotal)
            });
        }

        int totalCents = OrderManager.calculateCartTotalCents(cart.toExpandedProductArraySnapshot(inventory));
        totalLabel.setText("Total: " + Product.formatMoney(totalCents));
    }

    private void addSelectedMenuItemToCart() {
        int row = menuTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a menu item first.");
            return;
        }

        String productId = String.valueOf(menuModel.getValueAt(row, 0));
        boolean ok = cart.addOne(productId);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Cart is full (too many unique items).");
            return;
        }

        reloadCart();
    }

    private void removeSelectedCartLine() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a cart row first.");
            return;
        }
        String productId = String.valueOf(cartModel.getValueAt(row, 0));
        cart.remove(productId);
        reloadCart();
    }

    private void checkout() {
        try {
            PosOrder placed = orders.placeOrder(studentIdField.getText(), cart, inventory);
            JOptionPane.showMessageDialog(
                    this,
                    "Order placed: " + placed.getOrderNumber() + "\nTotal: " + placed.getTotalDisplay(),
                    "Checkout Success",
                    JOptionPane.INFORMATION_MESSAGE
            );
            reloadMenu();
            reloadCart();

            if (onOrderPlaced != null) {
                onOrderPlaced.run();
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Checkout Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
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
        table.setRowHeight(22);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setFillsViewportHeight(true);
    }

    private static JLabel buildBackgroundLabel(String path, int w, int h) {
        File f = new File(path);
        if (f.exists()) {
            ImageIcon raw = new ImageIcon(path);
            Image scaled = raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new JLabel(new ImageIcon(scaled));
        }

        JLabel fallback = new JLabel("(Add customer-kiosk.png)", SwingConstants.CENTER);
        fallback.setOpaque(true);
        fallback.setBackground(new Color(245, 245, 245));
        fallback.setForeground(new Color(120, 120, 120));
        return fallback;
    }
}
