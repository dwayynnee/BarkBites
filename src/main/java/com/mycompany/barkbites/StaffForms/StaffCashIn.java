/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.barkbites.StaffForms;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;
import com.mycompany.barkbites.FormNavigator;
import com.mycompany.barkbites.data.FirebaseInitializer;
import com.mycompany.barkbites.data.firestore.FirestoreDocuments;
import com.mycompany.barkbites.data.staff.StaffCashInService;
import com.mycompany.barkbites.data.staff.StaffFirebaseBootstrap;
import java.awt.Color;
import java.awt.Font;
import java.util.Objects;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author markd
 */
public class StaffCashIn extends javax.swing.JFrame {

    private final StaffCashInService cashInService = new StaffCashInService();

    private final DefaultTableModel usersTableModel = new DefaultTableModel(
            new Object[] { "Name", "Student ID", "Wallet" },
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final TableRowSorter<DefaultTableModel> usersSorter = new TableRowSorter<>(usersTableModel);
    private final List<CashInUser> loadedUsers = new ArrayList<>();
    private javax.swing.JTable usersTable;

    /**
     * Creates new form StaffCashIn
     */
    public StaffCashIn() {
        initComponents();
        configureUi();

        boolean firebaseReady = true;
        if (!java.beans.Beans.isDesignTime()) {
            firebaseReady = StaffFirebaseBootstrap.ensureInitialized(this);
        }

        if (firebaseReady) {
            loadUsersAsync();
        }
    }

    private void configureUi() {
        boolean designTime = java.beans.Beans.isDesignTime();
        getContentPane().setComponentZOrder(jLabel1, getContentPane().getComponentCount() - 1);

        usersTable = new javax.swing.JTable(usersTableModel);
        usersTable.setRowHeight(24);
        usersTable.setShowGrid(false);
        usersTable.setFillsViewportHeight(true);
        usersTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        usersTable.setBackground(Color.WHITE);
        usersTable.setForeground(Color.BLACK);
        usersTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        usersTable.getTableHeader().setReorderingAllowed(false);
        usersTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        usersTable.getTableHeader().setBackground(new Color(23, 57, 122));
        usersTable.getTableHeader().setForeground(Color.WHITE);
        usersTable.setRowSorter(usersSorter);
        usersSorter.setSortsOnUpdates(false);
        usersSorter.setSortable(0, false);
        usersSorter.setSortable(1, false);
        usersSorter.setSortable(2, false);
        Users.setBorder(BorderFactory.createEmptyBorder());
        Users.getViewport().setBackground(Color.WHITE);
        Users.setViewportView(usersTable);

        if (!designTime) {
            makeButtonInvisible(Order);
            makeButtonInvisible(Inventory);
            makeButtonInvisible(Menu);
            makeButtonInvisible(Statistics);
            makeButtonInvisible(History);
            makeButtonInvisible(Logout);
            makeButtonInvisible(refresh);
            makeButtonInvisible(send);
            makeButtonInvisible(cancel);
        }

        Order.addActionListener(evt -> FormNavigator.redirect(this, new StaffOrders()));
        Inventory.addActionListener(evt -> FormNavigator.redirect(this, new StaffInventory()));
        Menu.addActionListener(evt -> FormNavigator.redirect(this, new StaffMenu()));
        Statistics.addActionListener(evt -> FormNavigator.redirect(this, new StaffStatistics()));
        History.addActionListener(evt -> FormNavigator.redirect(this, new StaffHistory()));
        Logout.addActionListener(evt -> FormNavigator.redirect(this, new StaffLandingPage()));
        refresh.addActionListener(evt -> loadUsersAsync());
        send.addActionListener(evt -> confirmCashIn());

        usersTable.getSelectionModel().addListSelectionListener(evt -> {
            if (!evt.getValueIsAdjusting()) {
                populateEditorFromSelection();
            }
        });

        Search.setText("");
        name.setText("");
        amount.setText("");
        name.setEditable(true);
        amount.setEditable(true);

        Search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applySearchFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applySearchFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applySearchFilter();
            }
        });

        this.setResizable(false);
    }

    private void makeButtonInvisible(javax.swing.JButton button) {
        if (button == null) {
            return;
        }
        button.setText("");
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
    }

    private void loadUsersAsync() {
        refresh.setEnabled(false);
        clearEditorFields();
        SwingWorker<List<CashInUser>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<CashInUser> doInBackground() {
                if (!FirebaseInitializer.isInitialized()) {
                    return List.of();
                }

                Firestore firestore = FirebaseInitializer.getFirestore();
                List<CashInUser> users = new ArrayList<>();
                try {
                    ApiFuture<QuerySnapshot> future = firestore.collection("customers").get();
                    QuerySnapshot snapshot = future.get();
                    for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                        users.add(new CashInUser(
                                document.getId(),
                                FirestoreDocuments.readString(document, "name", "Unnamed user"),
                                FirestoreDocuments.readString(document, "studentId", document.getId()),
                                FirestoreDocuments.readWalletBalanceCents(document)
                        ));
                    }
                    users.sort(Comparator
                            .comparing(CashInUser::name, String.CASE_INSENSITIVE_ORDER)
                            .thenComparing(CashInUser::studentId, String.CASE_INSENSITIVE_ORDER));
                } catch (Exception ex) {
                    throw new IllegalStateException(ex.getMessage() != null ? ex.getMessage() : "Failed to load users.", ex);
                }
                return users;
            }

            @Override
            protected void done() {
                try {
                    List<CashInUser> users = get();
                    loadedUsers.clear();
                    loadedUsers.addAll(users);
                    usersTableModel.setRowCount(0);
                    for (CashInUser user : users) {
                        usersTableModel.addRow(new Object[] {
                            user.name(),
                            user.studentId(),
                            formatWallet(user.walletBalanceCents())
                        });
                    }
                    usersTableModel.fireTableDataChanged();
                    applySearchFilter();
                    usersTable.clearSelection();
                    usersTable.revalidate();
                    usersTable.repaint();
                    Users.revalidate();
                    Users.repaint();
                    if (!users.isEmpty()) {
                        usersTable.setRowSelectionInterval(0, 0);
                    } else {
                        clearEditorFields();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    clearTableAndEditor();
                } catch (ExecutionException ex) {
                    clearTableAndEditor();
                    String message = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    JOptionPane.showMessageDialog(StaffCashIn.this, message, "Load users failed", JOptionPane.ERROR_MESSAGE);
                } finally {
                    refresh.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void applySearchFilter() {
        String query = Search.getText() != null ? Search.getText().trim().toLowerCase(java.util.Locale.ROOT) : "";
        if (query.isEmpty()) {
            usersSorter.setRowFilter(null);
            return;
        }

        usersSorter.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                for (int i = 0; i < entry.getValueCount(); i++) {
                    Object value = entry.getValue(i);
                    if (value != null && value.toString().toLowerCase(java.util.Locale.ROOT).contains(query)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void populateEditorFromSelection() {
        int viewRow = usersTable.getSelectedRow();
        if (viewRow < 0) {
            clearEditorFields();
            return;
        }

        int modelRow = usersTable.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= loadedUsers.size()) {
            clearEditorFields();
            return;
        }

        CashInUser user = loadedUsers.get(modelRow);
        name.setText(user.name());
        amount.setText("");
        amount.requestFocusInWindow();
    }

    private void confirmCashIn() {
        CashInUser selectedUser = getSelectedUser();
        String userName = selectedUser != null ? selectedUser.name() : name.getText() != null ? name.getText().trim() : "";
        String amountText = amount.getText() != null ? amount.getText().trim() : "";

        if (selectedUser == null) {
            JOptionPane.showMessageDialog(this, "Please select a user first.", "Missing user", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (amountText.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please enter or select an amount.", "Missing amount", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long cashInAmountCents;
        try {
            cashInAmountCents = parseMoneyToCents(amountText);
        } catch (NumberFormatException | ArithmeticException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount.", "Invalid amount", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (cashInAmountCents <= 0L) {
            JOptionPane.showMessageDialog(this, "Please enter an amount greater than zero.", "Invalid amount", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Confirm cash in details?\n\nName: " + userName + "\nAmount: " + String.format(java.util.Locale.US, "₱%,.2f", cashInAmountCents / 100.0),
                "Confirm Cash In",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            performCashIn(selectedUser, cashInAmountCents);
        }
    }

    private void performCashIn(CashInUser user, long cashInAmountCents) {
        refresh.setEnabled(false);
        send.setEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (!FirebaseInitializer.isInitialized()) {
                    throw new IllegalStateException("Firestore is not ready yet.");
                }

                Firestore firestore = FirebaseInitializer.getFirestore();
                DocumentSnapshot customerDoc = firestore.collection("customers").document(Objects.requireNonNull(user.uid(), "user.uid()")).get().get();
                if (customerDoc == null || !customerDoc.exists()) {
                    throw new IllegalStateException("Selected customer no longer exists.");
                }

                Long currentWalletBalanceCents = FirestoreDocuments.readWalletBalanceCents(customerDoc);
                long currentWalletCents = currentWalletBalanceCents != null ? currentWalletBalanceCents : 0L;
                long updatedWalletCents = Math.addExact(currentWalletCents, cashInAmountCents);

                WriteBatch batch = firestore.batch();
                DocumentReference customerRef = firestore.collection("customers").document(Objects.requireNonNull(user.uid(), "user.uid()"));
                DocumentReference historyRef = firestore.collection(Objects.requireNonNull(com.mycompany.barkbites.data.staff.StaffDatabaseSchema.cashInHistoryCollection(), "StaffDatabaseSchema.cashInHistoryCollection()"))
                    .document("cashin-" + System.currentTimeMillis() + "-" + Objects.requireNonNull(user.uid(), "user.uid()"));

                batch.update(customerRef, "walletBalanceCents", updatedWalletCents);
                batch.set(historyRef, java.util.Map.of(
                    "customerId", Objects.requireNonNull(user.uid(), "user.uid()"),
                    "customerName", Objects.requireNonNull(user.name(), "user.name()"),
                    "studentId", Objects.requireNonNull(user.studentId(), "user.studentId()"),
                    "amountCents", cashInAmountCents,
                    "balanceBeforeCents", currentWalletCents,
                    "balanceAfterCents", updatedWalletCents,
                    "createdAtMillis", System.currentTimeMillis(),
                    "type", "cashIn"
                ));
                batch.commit().get();

                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(
                            StaffCashIn.this,
                            "Cash in completed for " + user.name() + " (" + String.format(java.util.Locale.US, "₱%,.2f", cashInAmountCents / 100.0) + ").",
                            "Cash In Complete",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    loadUsersAsync();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(StaffCashIn.this, "Cash in interrupted.", "Cash In Failed", JOptionPane.ERROR_MESSAGE);
                } catch (ExecutionException ex) {
                    String message = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    JOptionPane.showMessageDialog(StaffCashIn.this, message, "Cash In Failed", JOptionPane.ERROR_MESSAGE);
                } finally {
                    refresh.setEnabled(true);
                    send.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private CashInUser getSelectedUser() {
        int viewRow = usersTable.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }

        int modelRow = usersTable.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= loadedUsers.size()) {
            return null;
        }

        return loadedUsers.get(modelRow);
    }

    private static long parseMoneyToCents(String amountText) {
        BigDecimal amount = new BigDecimal(amountText).setScale(2, RoundingMode.HALF_UP);
        return amount.movePointRight(2).longValueExact();
    }

    private void clearEditorFields() {
        name.setText("");
        amount.setText("");
    }

    private void clearTableAndEditor() {
        loadedUsers.clear();
        usersTableModel.setRowCount(0);
        usersTableModel.fireTableDataChanged();
        clearEditorFields();
        applySearchFilter();
        usersTable.clearSelection();
        usersTable.revalidate();
        usersTable.repaint();
        Users.revalidate();
        Users.repaint();
    }

    private static String formatWallet(Long walletBalanceCents) {
        if (walletBalanceCents == null) {
            return "Wallet unavailable";
        }
        return String.format(java.util.Locale.US, "₱%,.2f", walletBalanceCents / 100.0);
    }

    private record CashInUser(String uid, String name, String studentId, Long walletBalanceCents) {
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Order = new javax.swing.JButton();
        Inventory = new javax.swing.JButton();
        Menu = new javax.swing.JButton();
        Statistics = new javax.swing.JButton();
        History = new javax.swing.JButton();
        Logout = new javax.swing.JButton();
        refresh = new javax.swing.JButton();
        Search = new javax.swing.JTextField();
        Users = new javax.swing.JScrollPane();
        name = new javax.swing.JTextField();
        amount = new javax.swing.JTextField();
        send = new javax.swing.JButton();
        cancel = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Order.setText("jButton1");
        getContentPane().add(Order, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, 140, 60));

        Inventory.setText("jButton2");
        getContentPane().add(Inventory, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, 140, 50));

        Menu.setText("jButton3");
        getContentPane().add(Menu, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 240, 140, 50));

        Statistics.setText("jButton4");
        getContentPane().add(Statistics, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 300, 140, 60));

        History.setText("jButton5");
        getContentPane().add(History, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 360, 140, 60));

        Logout.setText("jButton6");
        getContentPane().add(Logout, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 560, 90, 30));

        refresh.setText("jButton1");
        getContentPane().add(refresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 130, 110, 60));

        Search.setText("jTextField1");
        getContentPane().add(Search, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 210, 390, 30));
        getContentPane().add(Users, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 280, 420, 280));

        name.setText("jTextField1");
        getContentPane().add(name, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 320, 140, 30));

        amount.setText("a");
        getContentPane().add(amount, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 380, 140, 30));

        send.setText("jButton1");
        getContentPane().add(send, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 430, 80, 40));

        cancel.setText("jButton2");
        getContentPane().add(cancel, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 430, -1, 40));

        jButton1.setText("jButton1");
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 490, 140, 60));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/barkbites/StaffDesign/StaffCashIn.png"))); // NOI18N
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
            java.util.logging.Logger.getLogger(StaffCashIn.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(StaffCashIn.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(StaffCashIn.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(StaffCashIn.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new StaffCashIn().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton History;
    private javax.swing.JButton Inventory;
    private javax.swing.JButton Logout;
    private javax.swing.JButton Menu;
    private javax.swing.JButton Order;
    private javax.swing.JTextField Search;
    private javax.swing.JButton Statistics;
    private javax.swing.JScrollPane Users;
    private javax.swing.JTextField amount;
    private javax.swing.JButton cancel;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField name;
    private javax.swing.JButton refresh;
    private javax.swing.JButton send;
    // End of variables declaration//GEN-END:variables
}
