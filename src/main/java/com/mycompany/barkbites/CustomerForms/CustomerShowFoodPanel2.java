package com.mycompany.barkbites.CustomerForms;

import com.mycompany.barkbites.FormNavigator;
import com.mycompany.barkbites.data.FirebasePublicConfig;
import com.mycompany.barkbites.data.auth.AuthSession;
import com.mycompany.barkbites.data.auth.AuthState;
import com.mycompany.barkbites.data.firestore.FirestoreDocuments;
import com.mycompany.barkbites.data.firestore.FirestoreRestClient;
import com.mycompany.barkbites.data.staff.StaffFirebaseBootstrap;
import com.mycompany.barkbites.data.staff.StaffMenuItem;
import com.mycompany.barkbites.data.staff.StaffMenuService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.Image;
import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class CustomerShowFoodPanel2 extends javax.swing.JFrame {

    private static final String CART_COLLECTION = "cart";
    private static final String MENU_DOCUMENT_ID = "menu-002";

    private final StaffMenuService menuService = new StaffMenuService();
    private final DecimalFormat priceFormat = new DecimalFormat("#,##0.00");

    private String currentMenuId;
    private String currentMenuName = "Loading...";
    private long currentMenuPriceCents;
    private String currentMenuImagePath = "";
    private int quantity = 1;

    public CustomerShowFoodPanel2() {
        initComponents();
        configureUi();
        StaffFirebaseBootstrap.ensureInitialized(this);
        loadMenuItem();
        this.setResizable(false);
    }

    private void configureUi() {
        getContentPane().setComponentZOrder(jLabel1, getContentPane().getComponentCount() - 1);
        bringToFront(jLabel2);
        bringToFront(jLabel3);
        bringToFront(jLabel4);
        bringToFront(jLabel5);
        bringToFront(jButton1);
        bringToFront(jButton2);
        bringToFront(jButton3);
        bringToFront(jButton4);
        bringToFront(jCheckBox1);
        bringToFront(jCheckBox2);

        makeButtonInvisible(jButton1);
        makeButtonInvisible(jButton2);
        makeButtonInvisible(jButton3);
        makeButtonInvisible(jButton4);

        jLabel2.setText(Integer.toString(quantity));
        refreshMenuLabels();

        jButton1.addActionListener(this::jButton1ActionPerformed);
        jButton2.addActionListener(this::jButton2ActionPerformed);
        jButton3.addActionListener(this::jButton3ActionPerformed);
        jButton4.addActionListener(this::jButton4ActionPerformed);
    }

    private void bringToFront(java.awt.Component component) {
        if (component == null) {
            return;
        }
        getContentPane().setComponentZOrder(component, 0);
    }

    private static void makeButtonInvisible(javax.swing.JButton button) {
        if (button == null) {
            return;
        }
        button.setText("");
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
    }

    private void loadMenuItem() {
        javax.swing.SwingWorker<MenuCardData, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected MenuCardData doInBackground() {
                List<StaffMenuItem> items = menuService.listMenuItems();
                for (StaffMenuItem item : items) {
                    if (MENU_DOCUMENT_ID.equals(item.id())) {
                        return new MenuCardData(item.id(), item.name(), item.priceCents(), item.imagePath());
                    }
                }
                if (items.isEmpty()) {
                    return null;
                }
                StaffMenuItem item = items.get(0);
                return new MenuCardData(item.id(), item.name(), item.priceCents(), item.imagePath());
            }

            @Override
            protected void done() {
                try {
                    MenuCardData data = get();
                    if (data == null) {
                        showUnavailableMenu("No menu item found.");
                        return;
                    }
                    currentMenuId = data.menuId();
                    currentMenuName = data.name();
                    currentMenuPriceCents = data.priceCents();
                    currentMenuImagePath = data.imagePath();
                    refreshMenuLabels();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    showUnavailableMenu("Menu load interrupted.");
                } catch (ExecutionException ee) {
                    String msg = ee.getCause() != null ? ee.getCause().getMessage() : ee.getMessage();
                    showUnavailableMenu(msg);
                    JOptionPane.showMessageDialog(CustomerShowFoodPanel2.this, msg, "Menu load failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void refreshMenuLabels() {
        jLabel2.setText(Integer.toString(quantity));
        jLabel3.setText(formatPrice(currentMenuPriceCents));
        jLabel4.setText(currentMenuName != null ? currentMenuName : "Unavailable");

        ImageIcon icon = loadMenuImage(currentMenuImagePath, jLabel5.getWidth(), jLabel5.getHeight());
        if (icon != null) {
            jLabel5.setText("");
            jLabel5.setIcon(icon);
        } else {
            jLabel5.setIcon(null);
            jLabel5.setText(currentMenuImagePath == null || currentMenuImagePath.isBlank() ? "Image" : currentMenuImagePath);
        }
    }

    private void showUnavailableMenu(String message) {
        currentMenuId = null;
        currentMenuName = "Menu unavailable";
        currentMenuPriceCents = 0L;
        currentMenuImagePath = "";
        quantity = 1;
        refreshMenuLabels();
        if (message != null && !message.isBlank()) {
            jLabel4.setText(message);
        }
    }

    private void increaseQuantity() {
        quantity++;
        jLabel2.setText(Integer.toString(quantity));
    }

    private void decreaseQuantity() {
        if (quantity > 1) {
            quantity--;
            jLabel2.setText(Integer.toString(quantity));
        }
    }

    private void addToCart() {
        if (currentMenuName == null || currentMenuName.isBlank() || "Menu unavailable".equals(currentMenuName)) {
            JOptionPane.showMessageDialog(this, "Menu item is not available yet.", "Add to cart failed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AuthSession session = AuthState.current();
        if (session == null) {
            JOptionPane.showMessageDialog(this, "Please sign in again.", "Add to cart failed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FirebasePublicConfig config;
        try {
            config = FirebasePublicConfig.load();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Firebase config error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String safeMenuId = currentMenuId != null && !currentMenuId.isBlank() ? currentMenuId : MENU_DOCUMENT_ID;
        FirestoreRestClient firestore = new FirestoreRestClient(config);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode cartDocument = mapper.createObjectNode();
        ObjectNode fields = cartDocument.putObject("fields");
        fields.set("customerId", FirestoreDocuments.stringValue(session.uid()));
        fields.set("menuItemId", FirestoreDocuments.stringValue(safeMenuId));
        fields.set("name", FirestoreDocuments.stringValue(currentMenuName));
        fields.set("priceCents", FirestoreDocuments.integerValue(currentMenuPriceCents));
        fields.set("quantity", FirestoreDocuments.integerValue(quantity));
        fields.set("totalCents", FirestoreDocuments.integerValue(currentMenuPriceCents * quantity));
        fields.set("imagePath", FirestoreDocuments.stringValue(currentMenuImagePath));
        fields.set("updatedAtMillis", FirestoreDocuments.integerValue(System.currentTimeMillis()));

        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
        javax.swing.SwingWorker<Void, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                String documentPath = String.format("customers/%s/cart/%s", session.uid(), safeMenuId);
                firestore.upsertDocumentAtPath(session.idToken(), documentPath, cartDocument);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(CustomerShowFoodPanel2.this, "Added to cart.", "Cart updated", JOptionPane.INFORMATION_MESSAGE);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(CustomerShowFoodPanel2.this, "Cart save interrupted.", "Add to cart failed", JOptionPane.ERROR_MESSAGE);
                } catch (ExecutionException ee) {
                    String msg = ee.getCause() != null ? ee.getCause().getMessage() : ee.getMessage();
                    JOptionPane.showMessageDialog(CustomerShowFoodPanel2.this, msg, "Add to cart failed", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(java.awt.Cursor.getDefaultCursor());
                }
            }
        };

        worker.execute();
    }

    private String formatPrice(long priceCents) {
        return "₱" + priceFormat.format(priceCents / 100.0d);
    }

    private ImageIcon loadMenuImage(String imagePath, int width, int height) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }

        URL resource = resolveImageResource(imagePath);
        Image sourceImage = null;
        if (resource != null) {
            sourceImage = new ImageIcon(resource).getImage();
        } else {
            File file = new File(imagePath);
            if (file.exists()) {
                sourceImage = new ImageIcon(file.getAbsolutePath()).getImage();
            }
        }

        if (sourceImage == null) {
            return null;
        }

        int targetWidth = width > 0 ? width : 320;
        int targetHeight = height > 0 ? height : 220;
        Image scaled = sourceImage.getScaledInstance(targetWidth, targetHeight, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private URL resolveImageResource(String imagePath) {
        String normalizedPath = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;

        URL resource = getClass().getResource("/com/mycompany/barkbites/Uploads/" + normalizedPath);
        if (resource != null) {
            return resource;
        }

        resource = getClass().getResource(imagePath.startsWith("/") ? imagePath : "/" + imagePath);
        if (resource != null) {
            return resource;
        }

        resource = getClass().getResource("/com/mycompany/barkbites/CustomerDesign/" + normalizedPath);
        if (resource != null) {
            return resource;
        }

        resource = getClass().getResource("/com/mycompany/barkbites/StaffDesign/" + normalizedPath);
        if (resource != null) {
            return resource;
        }

        File projectRoot = new File(System.getProperty("user.dir", "."));
        File[] candidates = new File[] {
            new File(projectRoot, "src/main/java/com/mycompany/barkbites/CustomerDesign/" + normalizedPath),
            new File(projectRoot, "src/main/java/com/mycompany/barkbites/StaffDesign/" + normalizedPath),
            new File(projectRoot, "src/main/resources/com/mycompany/barkbites/CustomerDesign/" + normalizedPath),
            new File(projectRoot, "src/main/resources/com/mycompany/barkbites/StaffDesign/" + normalizedPath),
            new File(projectRoot, "target/classes/com/mycompany/barkbites/CustomerDesign/" + normalizedPath),
            new File(projectRoot, "target/classes/com/mycompany/barkbites/StaffDesign/" + normalizedPath)
        };
        for (File candidate : candidates) {
            if (candidate.exists()) {
                try {
                    return candidate.toURI().toURL();
                } catch (Exception ignored) {
                }
            }
        }

        return null;
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setText("jButton1");
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 360, 90));

        jButton2.setText("jButton2");
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 370, 40, 40));

        jButton3.setText("jButton3");
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 370, 40, 40));

        jLabel2.setText("jLabel2");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 380, -1, -1));
        getContentPane().add(jCheckBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 480, 70, -1));
        getContentPane().add(jCheckBox2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 520, -1, -1));

        jLabel3.setText("jLabel3");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 570, 70, 50));

        jLabel4.setText("jLabel4");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 340, 110, 30));

        jLabel5.setText("jLabel5");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 320, 220));

        jLabel6.setText("jLabel3");
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 370, 70, 40));

        jLabel7.setText("jLabel3");
        getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 370, 70, 40));

        jButton4.setText("jButton4");
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 560, 310, 70));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/CustomerShowFoodPanel2.png"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 360, 650));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        FormNavigator.redirect(this, new CustomerMenuPanel());
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        decreaseQuantity();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        increaseQuantity();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        addToCart();
    }//GEN-LAST:event_jButton4ActionPerformed

    private record MenuCardData(String menuId, String name, long priceCents, String imagePath) {
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new CustomerShowFoodPanel2().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    // End of variables declaration//GEN-END:variables
}
