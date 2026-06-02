 # Updated Methods Summary (generated)

 Packages discovered:
 - com.mycompany.barkbites
 - com.mycompany.barkbites.CustomerForms
 - com.mycompany.barkbites.StaffForms
 - com.mycompany.barkbites.data
 - com.mycompany.barkbites.data.auth
 - com.mycompany.barkbites.data.firestore
 - com.mycompany.barkbites.data.staff
 - org.netbeans.lib.awtextra

 ---

 ## Key UI classes (Customer)

 - CustomerPayment — [src/main/java/com/mycompany/barkbites/CustomerForms/CustomerPayment.java](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerPayment.java#L35-L35)
   - constructor: `public CustomerPayment()` — [file](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerPayment.java#L35-L35)
   - `private void configureUi()` — [file](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerPayment.java#L41-L41)
   - `private void processWalletPayment()` — [file](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerPayment.java#L118-L118)
   - `private PaymentTotals readCartTotals(FirestoreRestClient firestore, AuthSession session)` — [file](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerPayment.java#L220-L220)
   - `private ObjectNode buildCustomerWalletUpdate(JsonNode customerDoc, long updatedWalletBalanceCents)` — [file](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerPayment.java#L259-L259)
   - `private ObjectNode buildOrderDocument(String orderId, String customerId, String customerName, long totalCents, String orderSummary)` — [file](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerPayment.java#L275-L275)

 - CustomerCartPanel — [src/main/java/com/mycompany/barkbites/CustomerForms/CustomerCartPanel.java](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerCartPanel.java#L32-L32)
   - constructors: `public CustomerCartPanel()` [file](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerCartPanel.java#L32-L32), `public CustomerCartPanel(String appliedVoucher)` [file](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerCartPanel.java#L54-L54)
   - `private void configureUi()` — [file](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerCartPanel.java#L59-L59)
   - `private void loadCartItems()` — [file](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerCartPanel.java#L141-L141)
   - `private void applyVoucherToCartAsync(String voucherCode)` — [file](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerCartPanel.java#L449-L449)
   - `private void submitCart()` — [file](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerCartPanel.java#L378-L378)

 - CustomerMenuPanel — [src/main/java/com/mycompany/barkbites/CustomerForms/CustomerMenuPanel.java](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerMenuPanel.java#L28-L28)
   - `public CustomerMenuPanel()` — [file](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerMenuPanel.java#L28-L28)
   - `private void configureMenuUi()` — [file](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerMenuPanel.java#L33-L33)
   - `private void loadMenuItemsAsync()` — [file](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerMenuPanel.java#L??-L??) (see file for exact placement)

 - CustomerCashInPanel — [src/main/java/com/mycompany/barkbites/CustomerForms/CustomerCashInPanel.java](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerCashInPanel.java#L36-L36)
   - `public CustomerCashInPanel()` — [file](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerCashInPanel.java#L36-L36)
   - wallet helpers: `loadWalletBalance()`, `cashInAmount(long)` — [file](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerCashInPanel.java#L199-L199)

 - CustomerOrderDetails, CustomerOrderConfirmed, CustomerLoginPanel, CustomerHomePagePanel — constructors and `configureUi()` present (see files under CustomerForms/ for anchors).

 ## Key UI classes (Staff)

 - StaffMenu — [src/main/java/com/mycompany/barkbites/StaffForms/StaffMenu.java](src/main/java/com/mycompany/barkbites/StaffForms/StaffMenu.java#L49-L49)
   - `public StaffMenu()` — [file](src/main/java/com/mycompany/barkbites/StaffForms/StaffMenu.java#L49-L49)
   - CRUD: `private void loadMenuItemsAsync()` [file](src/main/java/com/mycompany/barkbites/StaffForms/StaffMenu.java#L124-L124), `private void saveMenuItem()` [file](src/main/java/com/mycompany/barkbites/StaffForms/StaffMenu.java#L181-L181), `private void deleteSelectedMenuItem()` [file](src/main/java/com/mycompany/barkbites/StaffForms/StaffMenu.java#L242-L242)
   - card helpers: `private void renderMenuCards()`, `private void applyCard(...)` — [file anchors available]

 - StaffInventory — [src/main/java/com/mycompany/barkbites/StaffForms/StaffInventory.java](src/main/java/com/mycompany/barkbites/StaffForms/StaffInventory.java#L43-L43)
   - `public StaffInventory()` — [file](src/main/java/com/mycompany/barkbites/StaffForms/StaffInventory.java#L43-L43)
   - `private void loadInventoryAsync()` — [file](src/main/java/com/mycompany/barkbites/StaffForms/StaffInventory.java#L172-L172)
   - `private void saveInventoryItem()` — [file](src/main/java/com/mycompany/barkbites/StaffForms/StaffInventory.java#L226-L226)
   - `private void deleteInventoryItem()` — [file](src/main/java/com/mycompany/barkbites/StaffForms/StaffInventory.java#L280-L280)

 - StaffOrders — [src/main/java/com/mycompany/barkbites/StaffForms/StaffOrders.java](src/main/java/com/mycompany/barkbites/StaffForms/StaffOrders.java#L57-L57)
   - `public StaffOrders()` — [file](src/main/java/com/mycompany/barkbites/StaffForms/StaffOrders.java#L57-L57)
   - `private void loadOrdersAsync()` — [file](src/main/java/com/mycompany/barkbites/StaffForms/StaffOrders.java#L241-L241)
   - `private void updateSelectedOrderStatusAsync()` — [file](src/main/java/com/mycompany/barkbites/StaffForms/StaffOrders.java#L360-L360)

 - StaffCashIn — [src/main/java/com/mycompany/barkbites/StaffForms/StaffCashIn.java](src/main/java/com/mycompany/barkbites/StaffForms/StaffCashIn.java#L61-L61)
   - `public StaffCashIn()` — [file](src/main/java/com/mycompany/barkbites/StaffForms/StaffCashIn.java#L61-L61)
   - `private void loadUsersAsync()` — [file](src/main/java/com/mycompany/barkbites/StaffForms/StaffCashIn.java#L164-L164)
   - `private void performCashIn(CashInUser user, long cashInAmountCents)` — [file](src/main/java/com/mycompany/barkbites/StaffForms/StaffCashIn.java#L317-L317)

 - StaffHistory, StaffStatistics — UI constructors and `loadXAsync()` present (see files under StaffForms/ for anchors).

 ## Services and data layer

 - FirestoreRestClient — [src/main/java/com/mycompany/barkbites/data/firestore/FirestoreRestClient.java](src/main/java/com/mycompany/barkbites/data/firestore/FirestoreRestClient.java#L1-L1)
   - `public FirestoreRestClient(FirebasePublicConfig config)`
   - `public JsonNode getDocument(String idToken, String collection, String documentId)`
   - `public JsonNode getDocumentAtPath(String idToken, String documentPath)`
   - `public JsonNode createDocumentWithId(String idToken, String collectionPath, String documentId, JsonNode documentBody)`
   - `public JsonNode listDocumentsAtPath(String idToken, String collectionPath)`
   - `public JsonNode deleteDocumentAtPath(String idToken, String documentPath)`

 - FirestoreDocuments — [src/main/java/com/mycompany/barkbites/data/firestore/FirestoreDocuments.java](src/main/java/com/mycompany/barkbites/data/firestore/FirestoreDocuments.java#L1-L1)
   - Utilities: `customerDocument`, `customerDocumentWithEmail`, `readLong`, `readString`, `integerValue`, `stringValue`, `integerValue`

 - FirebaseAuthRestService — [src/main/java/com/mycompany/barkbites/data/auth/FirebaseAuthRestService.java](src/main/java/com/mycompany/barkbites/data/auth/FirebaseAuthRestService.java#L1-L1)
   - `public AuthSession signUp(String studentId, String password)`
   - `public AuthSession signIn(String studentId, String password)`
   - `public boolean isEmailRegistered(String email)`

 - StaffMenuService — [src/main/java/com/mycompany/barkbites/data/staff/StaffMenuService.java](src/main/java/com/mycompany/barkbites/data/staff/StaffMenuService.java#L32-L32)
   - `public void seedDefaultMenuItemsIfMissing()` — [file](src/main/java/com/mycompany/barkbites/data/staff/StaffMenuService.java#L32-L32)
   - `public List<StaffMenuItem> listMenuItems()` — [file](src/main/java/com/mycompany/barkbites/data/staff/StaffMenuService.java#L81-L81)
   - `public void upsertMenuItem(StaffMenuItem item)` — [file](src/main/java/com/mycompany/barkbites/data/staff/StaffMenuService.java#L119-L119)
   - `public void deleteMenuItem(String itemId)` — [file](src/main/java/com/mycompany/barkbites/data/staff/StaffMenuService.java#L144-L144)

 - StaffInventoryService — [src/main/java/com/mycompany/barkbites/data/staff/StaffInventoryService.java](src/main/java/com/mycompany/barkbites/data/staff/StaffInventoryService.java#L28-L28)
   - `public List<StaffInventoryItem> listInventoryItems()` — [file](src/main/java/com/mycompany/barkbites/data/staff/StaffInventoryService.java#L28-L28)
   - `public void upsertInventoryItem(StaffInventoryItem item)` — [file](src/main/java/com/mycompany/barkbites/data/staff/StaffInventoryService.java#L62-L62)
   - `public void deleteInventoryItem(String itemId)` — [file](src/main/java/com/mycompany/barkbites/data/staff/StaffInventoryService.java#L95-L95)

 - StaffOrderService — [src/main/java/com/mycompany/barkbites/data/staff/StaffOrderService.java](src/main/java/com/mycompany/barkbites/data/staff/StaffOrderService.java#L28-L28)
   - `public List<StaffOrderRecord> listOrders()` — [file](src/main/java/com/mycompany/barkbites/data/staff/StaffOrderService.java#L28-L28)
   - `public void updateOrderStatus(String orderId, String status)` — [file](src/main/java/com/mycompany/barkbites/data/staff/StaffOrderService.java#L120-L120)

 - StaffCashInService — [src/main/java/com/mycompany/barkbites/data/staff/StaffCashInService.java](src/main/java/com/mycompany/barkbites/data/staff/StaffCashInService.java#L20-L20)
   - `public List<StaffCashInRecord> listCashInRecords()` — [file](src/main/java/com/mycompany/barkbites/data/staff/StaffCashInService.java#L20-L20)
   - `public void recordCashIn(String customerId, String customerName, String studentId, long amountCents, long balanceBeforeCents, long balanceAfterCents)` — [file](src/main/java/com/mycompany/barkbites/data/staff/StaffCashInService.java#L55-L55)

 ## Models / Records

 - `StaffMenuItem` — [src/main/java/com/mycompany/barkbites/data/staff/StaffMenuItem.java](src/main/java/com/mycompany/barkbites/data/staff/StaffMenuItem.java#L1-L1) — `record StaffMenuItem(String id, String name, long priceCents, int quantity, String imagePath)`
 - `StaffInventoryItem` — [src/main/java/com/mycompany/barkbites/data/staff/StaffInventoryItem.java](src/main/java/com/mycompany/barkbites/data/staff/StaffInventoryItem.java#L1-L1) — `record StaffInventoryItem(String id, String name, int quantity, String unit, String imagePath)`
 - `StaffOrderRecord` — [src/main/java/com/mycompany/barkbites/data/staff/StaffOrderRecord.java](src/main/java/com/mycompany/barkbites/data/staff/StaffOrderRecord.java#L1-L1) — `record StaffOrderRecord(String id, String customerName, String status, String payment, String order, long totalCents, long createdAtMillis)`
 - `AuthSession` — [src/main/java/com/mycompany/barkbites/data/auth/AuthSession.java](src/main/java/com/mycompany/barkbites/data/auth/AuthSession.java#L1-L1)

 ## Async patterns

 - UI screens use `javax.swing.SwingWorker<>` heavily for background Firestore operations (examples: `CustomerPayment.processWalletPayment()` — background worker, `CustomerCartPanel.loadCartItems()` — SwingWorker, `StaffForms` loadXAsync methods use SwingWorker).

 ## Noted mismatches / ambiguous areas

 - Some UI helper methods appear duplicated across screens (image loaders, formatPrice), consider centralizing.
 - A few method anchors for `CustomerMenuPanel.loadMenuItemsAsync()` and other smaller helpers were omitted above (I can add full-file anchors on request).

 ---

 If you want, I can now produce an exact patch for `methods_summary.md` using this content (overwrite). Confirm and I'll prepare the patch file for you to apply.
