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

    class CustomerCashInPanel {
      +cashInAmount(amountCents): void
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
    CustomerPayment --> CustomerCashInPanel : may call when insufficient funds
    StaffOrderService --> StaffInventoryService : validate/reduceStock

    note for CustomerPayment "processWalletPayment() reads totals, verifies wallet, writes wallet update and order doc"
```

OOP Principles (mapped to diagram)

- Encapsulation: `CustomerPayment` encapsulates payment logic (reading totals, balance checks, writes). UI panels encapsulate presentation and user actions (`CustomerCartPanel`, `CustomerMenuPanel`).

- Abstraction: services (e.g., `StaffOrderService`) hide Firestore details from UI code.

- Inheritance: UI classes extend Swing components (JFrame/JPanel) to reuse lifecycle/behavior.

- Polymorphism/Callbacks: event listeners (actionPerformed, mouseClicked) let different components react to the same events.



