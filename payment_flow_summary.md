# Payment & Buying Flow — UML-style (methods that affect payment)

classDiagram
    class StaffMenuItem {
      -String id
      -String name
      -long priceCents
      -int stockQuantity
      +getPrice(): long
      +reduceStock(int qty): void
    }

    class StaffInventoryService {
      +getAllInventoryItems(): List
      +saveInventoryItem(item): void
      +reduceStock(itemId, qty): boolean
    }

    class StaffOrderService {
      +createOrder(orderDoc): String
      +updateOrderStatus(orderId, status): void
      +getPendingOrders(): List
    }

    class CustomerMenuPanel {
      +renderMenuCards(): void
      +addToCart(productId, qty): void
    }

    class CustomerCartPanel {
      +loadCartItems(): void
      +applyVoucherToCartAsync(voucherCode): void
      +submitCart(): void
    }

    class CustomerPayment {
      +readCartTotals(firestore, session): PaymentTotals
      +processWalletPayment(): PaymentResult
      +buildCustomerWalletUpdate(customerDoc, updatedWalletCents): ObjectNode
      +buildOrderDocument(orderId, customerId, customerName, totalCents, orderSummary): ObjectNode
    }

    class StaffCashIn {
      +loadUsersAsync(): void
      +confirmCashIn(): void
      +performCashIn(userId, amountCents): void
      +parseMoneyToCents(amountText): long
      +formatWallet(walletBalanceCents): String
    }

    %% helper records
    class PaymentTotals {
      +long subtotalCents
      +long discountCents
      +long finalTotalCents
      +String orderSummary
    }

    class PaymentResult {
      +long updatedWalletCents
      +long paidAmountCents
    }

    %% relationships (flow)
    CustomerMenuPanel --> StaffMenuItem : displays/
    CustomerMenuPanel --> CustomerCartPanel : adds-to
    CustomerCartPanel --> CustomerPayment : checkout -> submitCart()
    CustomerPayment --> StaffOrderService : createOrder()
    CustomerPayment --> StaffInventoryService : reduceStock()
    CustomerPayment --> StaffCashIn : may call when insufficient funds
    StaffOrderService --> StaffInventoryService : validate/reduceStock

    note for CustomerPayment "processWalletPayment() reads totals, verifies wallet, writes wallet update and order doc"
```


OOP Principles (mapped to codebase findings)

- **Encapsulation:** `CustomerPayment` centralizes payment orchestration (readCartTotals, balance checks, wallet update, order creation). See [CustomerPayment](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerPayment.java#L35-L35).

- **Abstraction / Service Boundary:** Data access and Firestore shape handling are abstracted into service classes and helpers: `StaffMenuService`, `StaffInventoryService`, `StaffOrderService`, and `FirestoreRestClient`. These services hide REST details from UI code—see [FirestoreRestClient](src/main/java/com/mycompany/barkbites/data/firestore/FirestoreRestClient.java#L1-L1) and [StaffMenuService](src/main/java/com/mycompany/barkbites/data/staff/StaffMenuService.java#L32-L32).

- **Composition over Monoliths:** There is no single `InventoryManager` or `OrderManager` class. Responsibility is split across service classes and UI orchestration (e.g., menu/inventory CRUD in StaffForms + Staff*Service classes). Model this in UML as services + UI collaborators rather than manager singletons.

- **Asynchronous / Concurrency:** UI → backend interactions run in background threads via `javax.swing.SwingWorker` (examples: `CustomerPayment.processWalletPayment()` and `CustomerCartPanel.loadCartItems()`). This is a prominent architectural pattern for keeping the Swing UI responsive; anchor: [CustomerCartPanel](src/main/java/com/mycompany/barkbites/CustomerForms/CustomerCartPanel.java#L141-L141).

- **Single Responsibility (practical):** UI classes handle presentation and some orchestration (navigation, wiring actions). Pure data operations are implemented in services; when updating UML, show UI → service calls rather than embedding DB logic in UI boxes.

- **Transactions & Atomicity (design risk):** Current payment implementation uses multiple REST calls (wallet patch, create order, delete cart items) without Firestore transactions. This implies potential race conditions—model this as a non-atomic multi-step interaction in UML and annotate as a recommended improvement (use Firestore transactions or a server-side function).

- **Observer / Callbacks:** Event listeners, card-click handlers, and model-driven table listeners implement UI reactions and user-driven flows (typical Swing event model). Represent these as callback edges in sequence diagrams where helpful.

- **Missing Domain Class:** There is no dedicated `Payment` domain class; payment behavior is implemented inside `CustomerPayment`. Also, inventory decrement is not present in checkout paths (stock changes done via Staff UI). Reflect this in UML: mark inventory decrement as "manual / staff-side" unless you want it implemented in checkout.




