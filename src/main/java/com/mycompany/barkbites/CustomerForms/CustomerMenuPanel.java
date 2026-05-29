package com.mycompany.barkbites.CustomerForms;

/*
 * CustomerMenuPanel — displays menu items and navigates to food details.
 */

import com.mycompany.barkbites.FormNavigator;
import com.mycompany.barkbites.data.staff.StaffFirebaseBootstrap;
import com.mycompany.barkbites.data.staff.StaffMenuItem;
import com.mycompany.barkbites.data.staff.StaffMenuService;
import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CustomerMenuPanel extends javax.swing.JFrame {

    private final StaffMenuService menuService = new StaffMenuService();
    private final List<StaffMenuItem> menuItems = new ArrayList<>();
    private final DecimalFormat priceFormat = new DecimalFormat("#,##0.00");

    public CustomerMenuPanel() {
        initComponents();

        configureMenuUi();
        StaffFirebaseBootstrap.ensureInitialized(this);
        loadMenuItemsAsync();

        this.setResizable(false);
    }

    private void configureMenuUi() {
        getContentPane().setComponentZOrder(jLabel1, getContentPane().getComponentCount() - 1);

        makeButtonInvisible(jButton1);
        makeButtonInvisible(jButton2);
        makeButtonInvisible(jButton3);
        makeButtonInvisible(jButton4);
        makeButtonInvisible(jButton5);
        makeButtonInvisible(jButton6);
        makeButtonInvisible(jButton7);

        makePanelInvisible(jPanel1);
        makePanelInvisible(jPanel2);
        makePanelInvisible(jPanel3);
        makePanelInvisible(jPanel4);

        jButton1.addActionListener(this::jButton1ActionPerformed);
        jButton2.addActionListener(this::jButton2ActionPerformed);
        jButton3.addActionListener(this::jButton3ActionPerformed);
        jButton4.addActionListener(this::jButton4ActionPerformed);
        jButton5.addActionListener(this::jButton5ActionPerformed);
        jButton6.addActionListener(this::jButton6ActionPerformed);
        jButton7.addActionListener(this::jButton7ActionPerformed);
    }

    private void loadMenuItemsAsync() {
        javax.swing.SwingWorker<List<StaffMenuItem>, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected List<StaffMenuItem> doInBackground() {
                menuService.seedDefaultMenuItemsIfMissing();
                return menuService.listMenuItems();
            }

            @Override
            protected void done() {
                try {
                    menuItems.clear();
                    menuItems.addAll(get());
                    renderMenuCards();
                } catch (Exception ex) {
                    menuItems.clear();
                    renderMenuCards();
                }
            }
        };
        worker.execute();
    }

    private void renderMenuCards() {
        StaffMenuItem[] items = new StaffMenuItem[] {
            menuItems.size() > 0 ? menuItems.get(0) : null,
            menuItems.size() > 1 ? menuItems.get(1) : null,
            menuItems.size() > 2 ? menuItems.get(2) : null,
            menuItems.size() > 3 ? menuItems.get(3) : null
        };

        applyCard(items[0], jPanel1, Image, Name, Price);
        applyCard(items[1], jPanel2, jLabel5, jLabel6, jLabel7);
        applyCard(items[2], jPanel3, jLabel8, jLabel9, jLabel10);
        applyCard(items[3], jPanel4, jLabel11, jLabel12, jLabel13);
    }

    private void applyCard(StaffMenuItem item, JPanel panel, JLabel imageLabel, JLabel nameLabel, JLabel priceLabel) {
        panel.setOpaque(false);
        panel.setBackground(new Color(0, 0, 0, 0));

        if (item == null) {
            imageLabel.setIcon(null);
            imageLabel.setVisible(true);
            imageLabel.setText("Image");
            nameLabel.setText("Name");
            priceLabel.setText("Price");
            return;
        }

        nameLabel.setText(item.name());
        priceLabel.setText(formatPrice(item.priceCents()));

        ImageIcon icon = loadMenuImage(item.imagePath(), imageLabel.getWidth(), imageLabel.getHeight());
        if (icon != null) {
            imageLabel.setVisible(true);
            imageLabel.setText("");
            imageLabel.setIcon(icon);
        } else {
            imageLabel.setIcon(null);
            imageLabel.setVisible(true);
            imageLabel.setText(item.imagePath() == null || item.imagePath().isBlank() ? "Image" : item.imagePath());
        }
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

        int targetWidth = width > 0 ? width : 140;
        int targetHeight = height > 0 ? height : 140;
        Image scaled = sourceImage.getScaledInstance(targetWidth, targetHeight, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private URL resolveImageResource(String imagePath) {
        String normalizedPath = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
        String uploadsPath = "/com/mycompany/barkbites/Uploads/" + normalizedPath;
        URL resource = getClass().getResource(uploadsPath);
        if (resource != null) {
            return resource;
        }

        String rawClasspathPath = imagePath.startsWith("/") ? imagePath : "/" + imagePath;
        resource = getClass().getResource(rawClasspathPath);
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

    private static void makeButtonInvisible(javax.swing.JButton button) {
        button.setText("");
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
    }

    private static void makePanelInvisible(JPanel panel) {
        panel.setOpaque(false);
        panel.setBackground(new Color(0, 0, 0, 0));
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        Image = new javax.swing.JLabel();
        Name = new javax.swing.JLabel();
        Price = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setText("jButton1");
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 570, -1, 60));

        jButton2.setText("jButton2");
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 570, -1, 60));

        jButton3.setText("jButton3");
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 570, -1, 60));

        jButton4.setText("jButton4");
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 180, 150, 180));

        jButton5.setText("jButton5");
        getContentPane().add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 180, 150, 180));

        jButton6.setText("jButton6");
        getContentPane().add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 370, 150, 190));

        jButton7.setText("jButton7");
        getContentPane().add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 370, 150, 190));

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Image.setText("jLabel2");
        Image.setMaximumSize(new java.awt.Dimension(33, 16));
        Image.setMinimumSize(new java.awt.Dimension(33, 16));
        Image.setPreferredSize(new java.awt.Dimension(33, 16));
        jPanel1.add(Image, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, 140, 130));

        Name.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        Name.setForeground(new java.awt.Color(255, 255, 255));
        Name.setText("jLabel3");
        jPanel1.add(Name, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 140, 140, 20));

        Price.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        Price.setForeground(new java.awt.Color(255, 255, 255));
        Price.setText("jLabel4");
        jPanel1.add(Price, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 160, 130, -1));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 180, 150, 180));

        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel5.setText("Image");
        jPanel2.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 140, 130));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("jLabel4");
        jPanel2.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 140, 140, 20));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("jLabel4");
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 160, 140, -1));

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 180, 150, 180));

        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel8.setText("jLabel4");
        jPanel3.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 6, 140, 140));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("jLabel4");
        jPanel3.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 150, 150, -1));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("jLabel4");
        jPanel3.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 170, 150, 20));

        getContentPane().add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 370, 150, 190));

        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel11.setText("jLabel4");
        jPanel4.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 6, 140, 140));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setText("jLabel4");
        jPanel4.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 150, 140, 20));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setText("jLabel4");
        jPanel4.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 170, 140, 20));

        getContentPane().add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 370, 150, 190));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/CustomerMenuPanel.png"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        FormNavigator.redirect(this, new CustomerHomePagePanel());
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        FormNavigator.redirect(this, new CustomerCartPanel());
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        FormNavigator.redirect(this, new CustomerProfilePanelVisible());
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        FormNavigator.redirect(this, new CustomerShowFoodPanel1());
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        FormNavigator.redirect(this, new CustomerShowFoodPanel2());
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        FormNavigator.redirect(this, new CustomerShowFoodPanel3());
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        FormNavigator.redirect(this, new CustomerShowFoodPanel4());
    }//GEN-LAST:event_jButton7ActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CustomerMenuPanel().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Image;
    private javax.swing.JLabel Name;
    private javax.swing.JLabel Price;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    // End of variables declaration//GEN-END:variables
}
