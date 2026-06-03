# BarkBites Essential Code Guide

This file points to the core code paths for the customer buying flow and shows the key methods you should read first. Each code excerpt is followed by a concise note explaining what it does, preconditions, and any gotchas.

## Where to look

- Payment: [CustomerPayment.java](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerPayment.java)
- Cart: [CustomerCartPanel.java](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerCartPanel.java)
- Wallet: [CustomerCashInPanel.java](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerCashInPanel.java)
- Menu UI: [CustomerMenuPanel.java](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerMenuPanel.java)
- Menu backend: [StaffMenuService.java](src/main/java/com/mycompany/barkbites/data/staff/StaffMenuService.java)
- Firestore REST client: [FirestoreRestClient.java](src/main/java/com/mycompany/barkbites/data/firestore/FirestoreRestClient.java)

## Payment flow

High-level: `CustomerPayment.processWalletPayment()` performs a client-driven wallet checkout. Preconditions: user must be signed in and `FirebasePublicConfig` must load successfully. Important: multiple independent writes are performed (wallet update, order create, cart deletes), so the sequence is NOT atomic — a failure mid-way can leave partial state.

Purpose: the code below shows the essential orchestration steps (validate session, compute totals, verify balance, persist wallet change, create order, clear cart, clear voucher).

```java
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

            String orderId = "order-" + System.currentTimeMillis();
            String customerName = FirestoreDocuments.readString(customerDoc, "name", "");
            ObjectNode order = buildOrderDocument(orderId, session.uid(), customerName, totals.finalTotalCents(), totals.orderSummary());

            String ordersCollectionPath = String.format("customers/%s/orders", session.uid());
            firestore.createDocumentWithId(session.idToken(), ordersCollectionPath, orderId, order);

            JsonNode cartList = firestore.listDocumentsAtPath(session.idToken(), String.format("customers/%s/cart", session.uid()));
            if (cartList != null && cartList.has("documents")) {
                for (JsonNode doc : cartList.get("documents")) {
                    JsonNode nameNode = doc.get("name");
                    if (nameNode != null && nameNode.isTextual()) {
                        String fullName = nameNode.asText();
                        int idx = fullName.indexOf("/documents/");
                        String relPath = idx >= 0 ? fullName.substring(idx + "/documents/".length()) : null;
                        if (relPath != null && !relPath.isBlank()) {
                            firestore.deleteDocumentAtPath(session.idToken(), relPath);
                        }
                    }
                }
            }

            CustomerVoucherState.save(session.uid(), null);
            return new PaymentResult(updatedWallet, totals.finalTotalCents());
        }
    };

    worker.execute();
}
```

```

The cart total calculation is here (currency stored in cents to avoid floating errors):

```java

```java
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
```

## Cart flow

Summary: `CustomerCartPanel` is responsible for reading the customer's cart documents, displaying items, calculating `subtotalCents`, optionally applying a voucher, and navigating to the payment screen on submit. UI responsiveness is preserved via `SwingWorker` background tasks.

Key behavior: `submitCart()` only navigates to payment — it does not persist an "in-progress" order or reserve inventory.

```java
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
```

Voucher application is handled asynchronously so the UI stays responsive. Expected voucher document fields: `discount_percent` (integer percent). If voucher lookup fails, user sees an info dialog and no discount is applied.

```java
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
            JsonNode voucherDoc = rest.getDocument(session.idToken(), "Vouchers", voucherCode);
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
    };

    worker.execute();
}
```

## Wallet flow

Purpose: `CustomerCashInPanel` reads the customer's wallet balance and allows adding funds. Balances are stored in cents. `cashInAmount()` reads the current document, computes the new balance, then `upsertDocument()` replaces the Firestore fields fragment.

Note: This client-side approach writes the full `fields` object for the customer; take care when other concurrent writers may update customer fields (merge vs overwrite semantics).

```java
private void loadWalletBalance() {
    AuthSession session = AuthState.current();
    if (session == null) {
        walletBalanceCents = null;
        refreshWalletLabel();
        return;
    }

    FirebasePublicConfig config;
    try {
        config = FirebasePublicConfig.load();
    } catch (Exception ex) {
        walletBalanceCents = null;
        refreshWalletLabel();
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Firebase config error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    FirestoreRestClient firestore = new FirestoreRestClient(config);
    javax.swing.SwingWorker<Long, Void> worker = new javax.swing.SwingWorker<>() {
        @Override
        protected Long doInBackground() {
            JsonNode document = firestore.getDocument(session.idToken(), "customers", session.uid());
            return FirestoreDocuments.readWalletBalanceCents(document);
        }
    };

    worker.execute();
}
```

```java
private void cashInAmount(long amountCents) {
    AuthSession session = AuthState.current();
    if (session == null) {
        JOptionPane.showMessageDialog(this, "Please sign in again.", "Not signed in", JOptionPane.WARNING_MESSAGE);
        return;
    }

    FirebasePublicConfig config;
    try {
        config = FirebasePublicConfig.load();
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Firebase config error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    FirestoreRestClient firestore = new FirestoreRestClient(config);
    javax.swing.SwingWorker<Long, Void> worker = new javax.swing.SwingWorker<>() {
        @Override
        protected Long doInBackground() {
            JsonNode document = firestore.getDocument(session.idToken(), "customers", session.uid());
            long currentBalance = FirestoreDocuments.readWalletBalanceCents(document) != null ? FirestoreDocuments.readWalletBalanceCents(document) : 0L;
            long updatedBalance = currentBalance + amountCents;

            ObjectNode update = MAPPER.createObjectNode();
            if (FirestoreDocuments.readString(document, "email", "").isBlank()) {
                update.set("fields", FirestoreDocuments.customerDocument(
                        FirestoreDocuments.readString(document, "studentId", ""),
                        FirestoreDocuments.readString(document, "name", ""),
                        FirestoreDocuments.readString(document, "mobile", ""),
                        updatedBalance
                ).get("fields"));
            } else {
                update.set("fields", FirestoreDocuments.customerDocumentWithEmail(
                        FirestoreDocuments.readString(document, "studentId", ""),
                        FirestoreDocuments.readString(document, "name", ""),
                        FirestoreDocuments.readString(document, "email", ""),
                        updatedBalance
                ).get("fields"));
            }

            firestore.upsertDocument(session.idToken(), "customers", session.uid(), update);
            return updatedBalance;
        }
    };

    worker.execute();
}
```

## Menu flow

Summary: `CustomerMenuPanel` uses `StaffMenuService` to seed demo data if required and to load live menu items. The UI maps `StaffMenuItem` fields into card views. The backend service falls back to `StaffDemoDataStore` when Firestore is unavailable.

UI note: `applyCard(...)` is a presentational helper — it does not mutate backend state.

```java
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
```

```java
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
}
```

The backend menu service is simple CRUD with Firestore fallback to demo data. It prefers Firestore when `FirebaseInitializer.isInitialized()` is true and otherwise returns in-memory demo items.

```java
public List<StaffMenuItem> listMenuItems() {
    if (!FirebaseInitializer.isInitialized()) {
        return StaffDemoDataStore.listMenuItems();
    }

    Firestore firestore = FirebaseInitializer.getFirestore();
    List<StaffMenuItem> items = new ArrayList<>();
    try {
        ApiFuture<QuerySnapshot> future = firestore.collection(StaffDatabaseSchema.menuCollection()).get();
        QuerySnapshot snapshot = future.get();
        for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
            items.add(new StaffMenuItem(
                    document.getId(),
                    FirestoreDocuments.readString(document, "name", FirestoreDocuments.readString(document, "title", "Untitled item")),
                    FirestoreDocuments.readLong(document, "priceCents", 0L),
                    FirestoreDocuments.readInteger(document, "quantity", 0),
                    FirestoreDocuments.readString(document, "imagePath", "")
            ));
        }
        return items;
    } catch (Exception ex) {
        // On any Firestore error, return demo items so the UI remains usable offline.
        return StaffDemoDataStore.listMenuItems();
    }
}
```

## Firestore client

All of the screens above depend on the same `FirestoreRestClient` REST wrapper. It uses `idToken` for user-scoped authorization and performs GET/POST/PATCH/DELETE requests against the Firestore REST API.

Important: The REST client builds full JSON fragments and typically `PATCH`es with a `fields` object. Error extraction attempts to return `null` for 404-like "not found" responses so callers can treat missing docs as absent instead of failing.

```java
public JsonNode getDocument(String idToken, String collection, String documentId) {
    String url = documentUrl(collection, documentId);
    try {
        return send(idToken, HttpRequest.newBuilder().uri(URI.create(url)).GET().build());
    } catch (IllegalStateException ex) {
        String msg = ex.getMessage();
        if (msg != null) {
            String lower = msg.toLowerCase();
            if (lower.contains("not found") || lower.contains("document") && lower.contains("not found")) {
                return null;
            }
        }
        throw ex;
    }
}
```

```java
public JsonNode createDocumentWithId(String idToken, String collectionPath, String documentId, JsonNode documentBody) {
    String url = collectionUrlFromPath(collectionPath) + "?documentId=" + encodeSegment(documentId);
    HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(documentBody.toString(), StandardCharsets.UTF_8))
            .build();
    return send(idToken, req);
}
```

## Quick read order

1. [CustomerCartPanel.java](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerCartPanel.java) for cart and voucher logic.
2. [CustomerPayment.java](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerPayment.java) for checkout and order creation.
3. [CustomerCashInPanel.java](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerCashInPanel.java) for wallet top-up.
4. [CustomerMenuPanel.java](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerMenuPanel.java) and [StaffMenuService.java](src/main/java/com/mycompany/barkbites/data/staff/StaffMenuService.java) for menu data flow.
