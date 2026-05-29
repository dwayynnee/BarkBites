/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.barkbites.CustomerForms;

/*
 * CustomerPayment — payment options UI for customers (wallet, cashless placeholders).
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.barkbites.FormNavigator;
import com.mycompany.barkbites.data.CustomerVoucherState;
import com.mycompany.barkbites.data.FirebasePublicConfig;
import com.mycompany.barkbites.data.auth.AuthSession;
import com.mycompany.barkbites.data.auth.AuthState;
import com.mycompany.barkbites.data.firestore.FirestoreDocuments;
import com.mycompany.barkbites.data.firestore.FirestoreRestClient;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;

/**
 *
 * @author markd
 */
public class CustomerPayment extends javax.swing.JFrame {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Creates new form CustomerPayment
     */
    public CustomerPayment() {
        initComponents();
        configureUi();
        this.setResizable(false);
    }

    private void configureUi() {
        getContentPane().setComponentZOrder(BG, getContentPane().getComponentCount() - 1);
        BG.setFocusable(false);

        bringToFront(jButton1);
        bringToFront(jButton2);
        bringToFront(jButton3);
        bringToFront(jButton4);
        bringToFront(jButton5);
        bringToFront(jButton6);

        makeButtonInvisible(jButton1);
        makeButtonInvisible(jButton2);
        makeButtonInvisible(jButton3);
        makeButtonInvisible(jButton4);
        makeButtonInvisible(jButton5);
        makeButtonInvisible(jButton6);

        jButton1.addActionListener(evt -> JOptionPane.showMessageDialog(
                this,
                "We Are Currently Upgrading our System Please try Bark Bites Wallet.",
                "Payment",
                JOptionPane.INFORMATION_MESSAGE
        ));

        jButton2.addActionListener(evt -> processWalletPayment());

        jButton3.addActionListener(evt -> JOptionPane.showMessageDialog(
                this,
                "We Are Currently Upgrading our System Please try Bark Bites Wallet.",
                "Payment",
                JOptionPane.INFORMATION_MESSAGE
        ));

        jButton4.addActionListener(evt -> JOptionPane.showMessageDialog(
                this,
                "We Are Currently transitioning to Cashless Payments. Try to CashIn in your Barkbites Wallet",
                "Payment",
                JOptionPane.INFORMATION_MESSAGE
        ));

        jButton5.addActionListener(evt -> FormNavigator.redirect(this, new CustomerCartPanel()));

        this.setResizable(false);
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
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setEnabled(true);
    }

    private void setBusy(boolean busy) {
        jButton1.setEnabled(!busy);
        jButton2.setEnabled(!busy);
        jButton3.setEnabled(!busy);
        jButton4.setEnabled(!busy);
        jButton5.setEnabled(!busy);
        jButton6.setEnabled(!busy);
        setCursor(busy
                ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR)
                : java.awt.Cursor.getDefaultCursor());
    }

    private void processWalletPayment() {
        AuthSession session = AuthState.current();
        if (session == null) {
            JOptionPane.showMessageDialog(this, "Please sign in again.", "Payment", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FirebasePublicConfig config;
        try {
            config = FirebasePublicConfig.load();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Payment", JOptionPane.ERROR_MESSAGE);
            return;
        }

        FirestoreRestClient firestore = new FirestoreRestClient(config);
        setBusy(true);

        javax.swing.SwingWorker<PaymentResult, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected PaymentResult doInBackground() {
                JsonNode customerDoc = firestore.getDocument(session.idToken(), "customers", session.uid());
                if (customerDoc == null) {
                    throw new IllegalStateException("Customer account not found.");
                }

                PaymentTotals totals = readCartTotals(firestore, session);
                if (totals.finalTotalCents() <= 0L) {
                    throw new IllegalStateException("Your cart is empty.");
                }

                Long walletBalanceCents = FirestoreDocuments.readWalletBalanceCents(customerDoc);
                long currentWallet = walletBalanceCents != null ? walletBalanceCents : 0L;
                if (currentWallet < totals.finalTotalCents()) {
                    throw new IllegalStateException("Insufficient wallet balance.");
                }

                long updatedWallet = currentWallet - totals.finalTotalCents();
                ObjectNode updateDoc = buildCustomerWalletUpdate(customerDoc, updatedWallet);
                firestore.upsertDocument(session.idToken(), "customers", session.uid(), updateDoc);

                // Build order document once, then write it to both the customer order
                // subcollection and the top-level staff-visible orders collection.
                String orderId = "order-" + System.currentTimeMillis();
                String customerName = FirestoreDocuments.readString(customerDoc, "name", "");
                ObjectNode order = buildOrderDocument(orderId, session.uid(), customerName, totals.finalTotalCents(), totals.orderSummary());

                // Persist order under customers/{uid}/orders/{orderId}
                String ordersCollectionPath = String.format("customers/%s/orders", session.uid());
                JsonNode orderResult;
                try {
                    orderResult = firestore.createDocumentWithId(session.idToken(), ordersCollectionPath, orderId, order);
                    System.out.println("Order create result: " + (orderResult == null ? "null" : orderResult.toString()));
                } catch (Exception ex) {
                    System.err.println("Order creation failed: " + ex.getMessage());
                    ex.printStackTrace(System.err);
                    throw ex;
                }

                // Delete all cart items under customers/{uid}/cart
                JsonNode cartList = firestore.listDocumentsAtPath(session.idToken(), String.format("customers/%s/cart", session.uid()));
                if (cartList != null && cartList.has("documents")) {
                    for (JsonNode doc : cartList.get("documents")) {
                        JsonNode nameNode = doc.get("name");
                        if (nameNode != null && nameNode.isTextual()) {
                            String fullName = nameNode.asText();
                            int idx = fullName.indexOf("/documents/");
                            String relPath = idx >= 0 ? fullName.substring(idx + "/documents/".length()) : null;
                            if (relPath != null && !relPath.isBlank()) {
                                JsonNode delRes = firestore.deleteDocumentAtPath(session.idToken(), relPath);
                                System.out.println("Deleted cart doc " + relPath + " -> " + (delRes == null ? "null" : delRes.toString()));
                            }
                        }
                    }
                }

                // Clear persisted voucher state
                CustomerVoucherState.save(session.uid(), null);

                return new PaymentResult(updatedWallet, totals.finalTotalCents());
            }

            @Override
            protected void done() {
                try {
                    get();
                    FormNavigator.redirect(CustomerPayment.this, new CustomerOrderConfirmed());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(CustomerPayment.this, "Payment interrupted.", "Payment", JOptionPane.ERROR_MESSAGE);
                } catch (ExecutionException ee) {
                    String msg = ee.getCause() != null ? ee.getCause().getMessage() : ee.getMessage();
                    JOptionPane.showMessageDialog(CustomerPayment.this, msg, "Payment", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setBusy(false);
                }
            }
        };

        worker.execute();
    }

    private PaymentTotals readCartTotals(FirestoreRestClient firestore, AuthSession session) {
        JsonNode list = firestore.listDocumentsAtPath(session.idToken(), String.format("customers/%s/cart", session.uid()));
        long subtotal = 0L;
        StringBuilder orderSummary = new StringBuilder();

        if (list != null && list.has("documents")) {
            for (JsonNode doc : list.get("documents")) {
                String name = FirestoreDocuments.readString(doc, "name", "");
                long quantity = FirestoreDocuments.readLong(doc, "quantity", 1L);
                long priceCents = FirestoreDocuments.readLong(doc, "priceCents", 0L);
                long lineTotal = FirestoreDocuments.readLong(doc, "totalCents", priceCents * quantity);
                subtotal += Math.max(0L, lineTotal);

                if (!name.isBlank()) {
                    if (orderSummary.length() > 0) {
                        orderSummary.append(' ');
                    }
                    orderSummary.append(name).append(' ').append(quantity).append('x');
                }
            }
        }

        long discount = 0L;
        String voucherCode = CustomerVoucherState.load(session.uid());
        if (voucherCode != null && !voucherCode.isBlank() && subtotal > 0L) {
            JsonNode voucherDoc = firestore.getDocument(session.idToken(), "Vouchers", voucherCode);
            if (voucherDoc != null) {
                Long discountPercent = FirestoreDocuments.readLong(voucherDoc, "discount_percent", null);
                if (discountPercent != null && discountPercent > 0L) {
                    double percent = discountPercent.doubleValue() / 100.0d;
                    discount = Math.max(0L, Math.round(subtotal * percent));
                }
            }
        }

        long finalTotal = Math.max(0L, subtotal - discount);
        return new PaymentTotals(subtotal, discount, finalTotal, orderSummary.toString().trim());
    }

    private ObjectNode buildCustomerWalletUpdate(JsonNode customerDoc, long updatedWalletBalanceCents) {
        ObjectNode update = MAPPER.createObjectNode();
        String studentId = FirestoreDocuments.readString(customerDoc, "studentId", "");
        String name = FirestoreDocuments.readString(customerDoc, "name", "");
        String email = FirestoreDocuments.readString(customerDoc, "email", "");
        String mobile = FirestoreDocuments.readString(customerDoc, "mobile", "");

        if (email.isBlank()) {
            update.set("fields", FirestoreDocuments.customerDocument(studentId, name, mobile, updatedWalletBalanceCents).get("fields"));
        } else {
            update.set("fields", FirestoreDocuments.customerDocumentWithEmail(studentId, name, email, updatedWalletBalanceCents).get("fields"));
        }

        return update;
    }

    private ObjectNode buildOrderDocument(String orderId, String customerId, String customerName, long totalCents, String orderSummary) {
        ObjectNode order = MAPPER.createObjectNode();
        ObjectNode fields = order.putObject("fields");
        fields.set("id", FirestoreDocuments.stringValue(orderId));
        fields.set("customerName", FirestoreDocuments.stringValue(customerName));
        fields.set("Customer Name", FirestoreDocuments.stringValue(customerName));
        fields.set("customerId", FirestoreDocuments.stringValue(customerId));
        fields.set("CustomerID", FirestoreDocuments.stringValue(customerId));
        fields.set("payment", FirestoreDocuments.stringValue("wallet"));
        fields.set("Payment", FirestoreDocuments.stringValue("wallet"));
        fields.set("status", FirestoreDocuments.stringValue("processing"));
        fields.set("totalCents", FirestoreDocuments.integerValue(totalCents));
        fields.set("createdAtMillis", FirestoreDocuments.integerValue(System.currentTimeMillis()));
        fields.set("Order", FirestoreDocuments.stringValue(orderSummary));
        return order;
    }

    private record PaymentTotals(long subtotalCents, long discountCents, long finalTotalCents, String orderSummary) {
    }

    private record PaymentResult(long updatedWalletCents, long paidAmountCents) {
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        BG = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setText("jButton1");
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 160, 300, 80));

        jButton2.setText("jButton2");
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 250, 300, 70));

        jButton3.setText("jButton3");
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 330, 300, 70));

        jButton4.setText("jButton4");
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 410, 300, 80));

        jButton5.setText("jButton5");
        getContentPane().add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(285, 10, 70, 70));

        jButton6.setText("jButton6");
        getContentPane().add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 500, 200, 80));

        BG.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/CustomerDesign/CustomerPayment.png"))); // NOI18N
        getContentPane().add(BG, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

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
            java.util.logging.Logger.getLogger(CustomerPayment.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CustomerPayment.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CustomerPayment.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CustomerPayment.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CustomerPayment().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel BG;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    // End of variables declaration//GEN-END:variables
}
