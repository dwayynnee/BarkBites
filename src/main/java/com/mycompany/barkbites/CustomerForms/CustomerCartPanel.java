package com.mycompany.barkbites.CustomerForms;

/*
 * CustomerCartPanel — UI for viewing and editing the customer's cart.
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.mycompany.barkbites.data.FirebasePublicConfig;
import com.mycompany.barkbites.data.CustomerVoucherState;
import com.mycompany.barkbites.data.firestore.FirestoreRestClient;
import com.mycompany.barkbites.data.firestore.FirestoreDocuments;
import com.mycompany.barkbites.FormNavigator;
import com.mycompany.barkbites.data.auth.AuthSession;
import com.mycompany.barkbites.data.auth.AuthState;
import java.awt.Image;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class CustomerCartPanel extends javax.swing.JFrame {

    private final List<CartItemData> cartItems = new ArrayList<>();
    private long subtotalCents;
    private Long discountCents;
    private String appliedVoucherCode;

    public CustomerCartPanel() {
        initComponents();
        configureUi();
        AuthSession session = AuthState.current();
        if (session != null) {
            String savedVoucher = CustomerVoucherState.load(session.uid());
            if (savedVoucher != null) {
                appliedVoucherCode = savedVoucher;
                jLabel22.setText(savedVoucher);
                jButton4.setEnabled(false);
            }
        }
        loadCartItems();

        this.setResizable(false);
    }

    /**
     * Create the cart panel and display an applied voucher code.
     * This constructor forwards to the default constructor and then sets
     * the voucher label so the UI reflects any voucher the customer applied.
     */
    public CustomerCartPanel(String appliedVoucher) {
        this();
        setAppliedVoucher(appliedVoucher);
    }

    private void configureUi() {
        // Send background image to back so buttons remain clickable
        getContentPane().setComponentZOrder(jLabel1, getContentPane().getComponentCount() - 1);
        jLabel1.setFocusable(false);

        bringToFront(jLabel2);
        bringToFront(jLabel3);
        bringToFront(jLabel4);
        bringToFront(jLabel5);
        // Ensure jButton4 (voucher) is on top so clicks register
        bringToFront(jButton4);
        bringToFront(jPanel1);
        bringToFront(jPanel2);
        bringToFront(jPanel3);
        bringToFront(jPanel4);

        makeButtonInvisible(jButton1);
        makeButtonInvisible(jButton2);
        makeButtonInvisible(jButton3);
        makeButtonInvisible(jButton4);
        makeButtonInvisible(jButton5);
        makeButtonInvisible(jButton6);
        makeButtonInvisible(jButton7);
        makeButtonInvisible(jButton8);
        makeButtonInvisible(jButton9);

        // Wire action listeners for the clickable (but visually invisible) buttons
        jButton4.addActionListener(this::jButton4ActionPerformed);

        setEmptyState();
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

    private void setEmptyState() {
        jLabel5.setText("0");
        jLabel2.setText(formatPrice(0L));
        jLabel3.setText("");
        jLabel3.setVisible(false);
        jLabel4.setText(formatPrice(0L));

        clearPanel(jPanel1, jLabel6, jLabel14, jLabel10, jLabel18);
        clearPanel(jPanel2, jLabel7, jLabel15, jLabel11, jLabel19);
        clearPanel(jPanel3, jLabel8, jLabel16, jLabel12, jLabel20);
        clearPanel(jPanel4, jLabel9, jLabel17, jLabel13, jLabel21);
    }

    private void clearPanel(javax.swing.JPanel panel, javax.swing.JLabel imageLabel, javax.swing.JLabel nameLabel, javax.swing.JLabel quantityLabel, javax.swing.JLabel priceLabel) {
        panel.setVisible(false);
        imageLabel.setIcon(null);
        imageLabel.setText("");
        nameLabel.setText("");
        quantityLabel.setText("");
        priceLabel.setText("");
    }

    /**
     * Loads cart items from Firestore for the current customer asynchronously.
     * 
     * Process:
     * 1. Get the authenticated customer session
     * 2. Fetch the customer's cart documents from Firestore using REST API
     * 3. Parse each cart item (name, quantity, price, discount)
     * 4. Sort alphabetically by item name
     * 5. Update UI to display cart contents
     */
    private void loadCartItems() {
        // Get the authenticated user session
        AuthSession session = AuthState.current();
        if (session == null) {
            setEmptyState();
            return;
        }

        // Load Firebase public config (API key, project, etc.)
        FirebasePublicConfig config;
        try {
            config = FirebasePublicConfig.load();
        } catch (Exception ex) {
            setEmptyState();
            return;
        }

        // Create REST client for Firestore operations
        FirestoreRestClient rest = new FirestoreRestClient(config);
        
        // Use SwingWorker to load items in background (non-blocking)
        javax.swing.SwingWorker<List<CartItemData>, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected List<CartItemData> doInBackground() throws Exception {
                // Build path to customer's cart subcollection
                String collectionPath = String.format("customers/%s/cart", session.uid());
                
                // Query all documents in the cart
                JsonNode response = rest.listDocumentsAtPath(session.idToken(), collectionPath);
                List<CartItemData> items = new ArrayList<>();
                Long firstDiscount = null;

                // Parse each cart item document
                if (response != null && response.has("documents")) {
                    for (JsonNode doc : response.get("documents")) {
                        // Extract item details from Firestore document
                        String name = FirestoreDocuments.readString(doc, "name", "Unnamed item");
                        long quantity = FirestoreDocuments.readLong(doc, "quantity", 1L);
                        long priceCents = FirestoreDocuments.readLong(doc, "priceCents", 0L);
                        long totalCents = FirestoreDocuments.readLong(doc, "totalCents", priceCents * quantity);
                        String imagePath = FirestoreDocuments.readString(doc, "imagePath", "");
                        String menuItemId = FirestoreDocuments.readString(doc, "menuItemId", null);
                        
                        // Extract menu item ID from document path if not stored explicitly
                        if (menuItemId == null) {
                            String namePath = doc.path("name").asText(null);
                            if (namePath != null && namePath.lastIndexOf('/') >= 0) {
                                menuItemId = namePath.substring(namePath.lastIndexOf('/') + 1);
                            } else {
                                menuItemId = "";
                            }
                        }
                        
                        // Capture the first discount found (applied to whole cart)
                        Long itemDiscount = readOptionalDiscountCents(doc);
                        if (firstDiscount == null && itemDiscount != null && itemDiscount > 0L) {
                            firstDiscount = itemDiscount;
                        }
                        
                        // Add to cart
                        items.add(new CartItemData(name, quantity, totalCents, imagePath));
                    }
                }

                // Sort items alphabetically for consistent display
                items.sort(Comparator.comparing(CartItemData::name, String.CASE_INSENSITIVE_ORDER));
                discountCents = firstDiscount;
                return items;
            }

            @Override
            protected void done() {
                try {
                    // Update in-memory cart with fetched items
                    cartItems.clear();
                    cartItems.addAll(get());
                    renderCart();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    cartItems.clear();
                    setEmptyState();
                } catch (ExecutionException ee) {
                    String msg = ee.getCause() != null ? ee.getCause().getMessage() : ee.getMessage();
                    cartItems.clear();
                    setEmptyState();
                    JOptionPane.showMessageDialog(CustomerCartPanel.this, msg, "Cart load failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        // Start the background task
        worker.execute();
    }

    private void renderCart() {
        if (cartItems.isEmpty()) {
            clearAppliedVoucherState();
            setEmptyState();
            return;
        }

        subtotalCents = 0L;
        for (CartItemData item : cartItems) {
            subtotalCents += item.totalCents();
        }

        long appliedDiscountCents = discountCents != null ? Math.max(0L, discountCents) : 0L;
        long finalTotalCents = Math.max(0L, subtotalCents - appliedDiscountCents);

        jLabel5.setText(Integer.toString(cartItems.size()));
        jLabel2.setText(formatPrice(subtotalCents));
        if (appliedDiscountCents > 0L) {
            jLabel3.setVisible(true);
            jLabel3.setText(formatPrice(appliedDiscountCents));
        } else {
            jLabel3.setVisible(false);
            jLabel3.setText("");
        }
        jLabel4.setText(formatPrice(finalTotalCents));

        showPanel(jPanel1, jLabel6, jLabel14, jLabel10, jLabel18, cartItems.size() > 0 ? cartItems.get(0) : null);
        showPanel(jPanel2, jLabel7, jLabel15, jLabel11, jLabel19, cartItems.size() > 1 ? cartItems.get(1) : null);
        showPanel(jPanel3, jLabel8, jLabel16, jLabel12, jLabel20, cartItems.size() > 2 ? cartItems.get(2) : null);
        showPanel(jPanel4, jLabel9, jLabel17, jLabel13, jLabel21, cartItems.size() > 3 ? cartItems.get(3) : null);

        if (appliedVoucherCode != null && !appliedVoucherCode.isBlank()) {
            applyVoucherToCartAsync(appliedVoucherCode);
        }
    }

    private void showPanel(javax.swing.JPanel panel, javax.swing.JLabel imageLabel, javax.swing.JLabel nameLabel, javax.swing.JLabel quantityLabel, javax.swing.JLabel priceLabel, CartItemData item) {
        if (item == null) {
            clearPanel(panel, imageLabel, nameLabel, quantityLabel, priceLabel);
            return;
        }

        panel.setVisible(true);
        nameLabel.setText(item.name());
        quantityLabel.setText(Long.toString(item.quantity()));
        priceLabel.setText(formatPrice(item.totalCents()));

        ImageIcon icon = loadMenuImage(item.imagePath(), imageLabel.getWidth(), imageLabel.getHeight());
        if (icon != null) {
            imageLabel.setIcon(icon);
            imageLabel.setText("");
        } else {
            imageLabel.setIcon(null);
            imageLabel.setText(item.imagePath() == null || item.imagePath().isBlank() ? "" : item.imagePath());
        }
    }

    private Long readOptionalDiscountCents(com.fasterxml.jackson.databind.JsonNode document) {
        Long discount = FirestoreDocuments.readLong(document, "discountCents", null);
        if (discount != null) {
            return discount;
        }
        discount = FirestoreDocuments.readLong(document, "discountAmountCents", null);
        if (discount != null) {
            return discount;
        }
        return FirestoreDocuments.readLong(document, "discount", null);
    }

    private String formatPrice(long priceCents) {
        return "₱" + String.format(java.util.Locale.US, "%,.2f", priceCents / 100.0d);
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

        int targetWidth = width > 0 ? width : 60;
        int targetHeight = height > 0 ? height : 50;
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

    private void submitCart() {
        if (cartItems.isEmpty()) {
            clearAppliedVoucherState();
            JOptionPane.showMessageDialog(this, "Your cart is empty.", "Submit cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        clearAppliedVoucherState();
        JOptionPane.showMessageDialog(this, "Cart submitted.", "Submit cart", JOptionPane.INFORMATION_MESSAGE);
        FormNavigator.redirect(this, new CustomerPayment());
    }

    /**
     * Clears the applied voucher from the current UI and from persisted local storage.
     * This keeps checkout and empty-cart behavior consistent.
     */
    private void clearAppliedVoucherState() {
        appliedVoucherCode = null;
        discountCents = null;
        jLabel22.setText("");
        jLabel3.setVisible(false);
        jLabel3.setText("");
        jLabel4.setText(formatPrice(subtotalCents));
        jButton4.setEnabled(true);

        AuthSession session = AuthState.current();
        if (session != null) {
            CustomerVoucherState.save(session.uid(), null);
        }
    }

    /**
     * Set the applied voucher code to display in the cart UI.
     * Encapsulates the UI detail for where the voucher is shown (jLabel22).
     */
    public void setAppliedVoucher(String voucherCode) {
        if (voucherCode == null || voucherCode.isBlank()) {
            appliedVoucherCode = null;
            jLabel22.setText("");
            // clear any previously-applied discount
            discountCents = null;
            jLabel3.setVisible(false);
            jLabel3.setText("");
            jLabel4.setText(formatPrice(subtotalCents));
            jButton4.setEnabled(true);
            AuthSession session = AuthState.current();
            if (session != null) {
                CustomerVoucherState.save(session.uid(), null);
            }
            return;
        }

        // display the voucher code and apply its discount to the current cart totals
        appliedVoucherCode = voucherCode;
        jLabel22.setText(voucherCode);
        AuthSession session = AuthState.current();
        if (session != null) {
            CustomerVoucherState.save(session.uid(), voucherCode);
        }
        applyVoucherToCartAsync(voucherCode);
        // once applied, prevent re-applying from the cart UI
        jButton4.setEnabled(false);
    }

    /**
     * Fetches voucher metadata (discount_percent) from Firestore and applies
     * it to the current cart totals. The Firestore field `discount_percent` is
     * expected to be stored as an integer (e.g. 10 for 10%). We convert it to
     * a decimal percentage and compute the monetary discount against
     * `subtotalCents` then update `discountCents` and the visible totals.
     */
    private void applyVoucherToCartAsync(String voucherCode) {
        AuthSession session = AuthState.current();
        if (session == null) {
            JOptionPane.showMessageDialog(this, "Please sign in to apply vouchers.", "Voucher", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FirebasePublicConfig config;
        try {
            config = FirebasePublicConfig.load();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load Firebase configuration.", "Voucher", JOptionPane.ERROR_MESSAGE);
            return;
        }

        FirestoreRestClient rest = new FirestoreRestClient(config);
        javax.swing.SwingWorker<Long, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected Long doInBackground() throws Exception {
                com.fasterxml.jackson.databind.JsonNode voucherDoc = rest.getDocument(session.idToken(), "Vouchers", voucherCode);
                if (voucherDoc == null) {
                    return null;
                }

                Long discountPercentInt = FirestoreDocuments.readLong(voucherDoc, "discount_percent", null);
                if (discountPercentInt == null) {
                    return null;
                }

                double percent = discountPercentInt.doubleValue() / 100.0d;
                return Math.max(0L, Math.round(subtotalCents * percent));
            }

            @Override
            protected void done() {
                try {
                    Long computedDiscount = get();
                    if (computedDiscount == null) {
                        JOptionPane.showMessageDialog(CustomerCartPanel.this, "Voucher not found or invalid.", "Voucher", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    discountCents = computedDiscount;
                    jLabel3.setVisible(true);
                    jLabel3.setText(formatPrice(discountCents));
                    long finalTotal = Math.max(0L, subtotalCents - discountCents);
                    jLabel4.setText(formatPrice(finalTotal));
                    jLabel2.setText(formatPrice(subtotalCents));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    JOptionPane.showMessageDialog(CustomerCartPanel.this, "Unable to retrieve voucher details.", "Voucher", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButton8 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jButton5 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jButton7 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jButton9 = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setBorderPainted(false);
        jButton1.setContentAreaFilled(false);
        jButton1.setFocusPainted(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 570, 80, 60));

        jButton2.setBorderPainted(false);
        jButton2.setContentAreaFilled(false);
        jButton2.setFocusPainted(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 570, 80, 60));

        jButton3.setBorderPainted(false);
        jButton3.setContentAreaFilled(false);
        jButton3.setFocusPainted(false);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 570, 80, 60));

        jButton4.setText("jButton4");
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 390, 90, 50));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("jLabel2");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 440, 80, -1));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 0, 0));
        jLabel3.setText("jLabel3");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 460, 80, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(0, 255, 0));
        jLabel4.setText("jLabel4");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 470, 90, 40));

        jButton8.setText("jButton8");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton8, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 510, 190, 50));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("1");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, -10, 110, 90));

        jPanel1.setBackground(new java.awt.Color(248, 237, 221));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton5.setText("jButton5");
        jPanel1.add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 0, 40, 30));

        jLabel6.setText("jLabel6");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 60, 50));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setText("jLabel10");
        jPanel1.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 50, 90, -1));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setText("jLabel14");
        jPanel1.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 0, 90, 20));

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel18.setText("jLabel18");
        jPanel1.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 50, 90, -1));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 320, 70));

        jPanel2.setBackground(new java.awt.Color(248, 237, 221));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton6.setText("jButton6");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 0, 40, 30));

        jLabel7.setText("jLabel7");
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 60, 50));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel11.setText("jLabel11");
        jPanel2.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 50, 100, -1));

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel15.setText("jLabel15");
        jPanel2.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 0, 90, -1));

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel19.setText("jLabel19");
        jPanel2.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 50, 80, -1));

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 320, 70));

        jPanel3.setBackground(new java.awt.Color(248, 237, 221));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton7.setText("jButton7");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 0, 40, 30));

        jLabel8.setText("jLabel8");
        jPanel3.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 60, 50));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel12.setText("jLabel12");
        jPanel3.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 50, 100, -1));

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel16.setText("jLabel16");
        jPanel3.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 0, 100, -1));

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel20.setText("jLabel20");
        jPanel3.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 50, 90, -1));

        getContentPane().add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 240, 320, 70));

        jPanel4.setBackground(new java.awt.Color(248, 237, 221));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton9.setText("jButton9");
        jPanel4.add(jButton9, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 0, 40, 30));

        jLabel9.setText("jLabel9");
        jPanel4.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 60, 40));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel13.setText("jLabel13");
        jPanel4.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 40, 90, -1));

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel17.setText("jLabel17");
        jPanel4.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 0, -1, -1));

        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel21.setText("jLabel21");
        jPanel4.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 40, 80, -1));

        getContentPane().add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 320, 320, 60));
        getContentPane().add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 390, 60, 40));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/CustomerCartPanel.png"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        FormNavigator.redirect(this, new CustomerHomePagePanel());
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        FormNavigator.redirect(this, new CustomerMenuPanel());
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        FormNavigator.redirect(this, new CustomerProfilePanelVisible());
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        submitCart();
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // If a voucher is already displayed, prevent re-opening the voucher screen
        // and inform the user that the voucher has already been claimed.
        String displayed = jLabel22.getText();
        if (displayed != null && !displayed.isBlank()) {
            JOptionPane.showMessageDialog(this, "Voucher already claimed", "Voucher", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        FormNavigator.redirect(this, new CustomerVoucher());
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // Reserved for per-item actions.
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // Reserved for per-item actions (button 7). Kept as a stub so NetBeans-generated
        // action listener wiring compiles cleanly.
    }//GEN-LAST:event_jButton7ActionPerformed

    private static final class CartItemData {

        private final String name;
        private final long quantity;
        private final long totalCents;
        private final String imagePath;

        private CartItemData(String name, long quantity, long totalCents, String imagePath) {
            this.name = name;
            this.quantity = quantity;
            this.totalCents = totalCents;
            this.imagePath = imagePath;
        }

        private String name() {
            return name;
        }

        private long quantity() {
            return quantity;
        }

        private long totalCents() {
            return totalCents;
        }

        private String imagePath() {
            return imagePath;
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CustomerCartPanel().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
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
