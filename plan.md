## Plan: Implement UI panels, sync menu, Firebase profile, and cart

TL;DR - Implement dynamic menu sync (Firestore), show/hide password toggle, load customer profile from Firestore, build a dynamic cart UI using item panels, and create Java panels for missing CustomerDesign PNGs. Use existing patterns in CustomerSignupPanel/CustomerLoginPanel/CustomerCartPanel and FirestoreRestClient/Auth services.


**Progress (May 23, 2026)**
- Scaffolded panels added: CustomerCheckPhone, CustomerLoginPasswordRecovery, CustomerOrderConfirmed, CustomerOrderProcessing, CustomerReadyPickup, CustomerResetPass, CustomerVoucher, CustomerShowFoodPanel1, CustomerShowFoodPanel2, CustomerShowFoodPanel3, CustomerShowFoodPanel4.
- Files created under `src/main/java/com/mycompany/barkbites/CustomerForms/`.


**Steps**
1. Profile: Add Firestore read in `CustomerProfilePanelVisible` to load `customers/{uid}` using `AuthState.current().idToken()` and `FirestoreRestClient.getDocument(...)`. Populate visible fields (name, studentId). *depends on auth state being set at login/signup*
2. Password toggle: Reuse `CustomerLoginPanel.togglePasswordVisibility()` pattern for other screens (Staff login/password). Capture `passwordEchoChar` at init and toggle `JPasswordField.setEchoChar((char)0)` when visible.
3. Cart panel: Replace static background-only UI with a scrollable list:
   - Create `CartItem` model (id, productId, name, priceCents, qty, imagePath).
   - Add a `JScrollPane` with a vertical `JPanel` (BoxLayout.Y_AXIS) as the cart container.
   - Create a reusable `CartItemPanel` (image label, name, qty controls, price, remove button) and add one per `CartItem`.
   - Add cart state store (in-memory singleton or simple class) to manage items and provide add/remove/update methods. Persist only in-memory for now.
4. Menu sync (Staff <-> Customer): Introduce `MenuItem` model and a `MenuService` using `FirestoreRestClient` with collection `menu`:
   - Staff UI (StaffInventory or StaffMenu editor) should call `MenuService.upsertMenuItem(...)` to create/update items (image path, title, price, description).
   - Customer UI (CustomerMenuPanel/CustomerShowFoodPanel) should fetch menu items at init and dynamically render item tiles (similar to CartItemPanel UI pattern).
   - Use item document id as productId. Store images as packaged resources (for prototype) or as URL fields.
5. Create Java panels for missing CustomerDesign PNGs (screens to scaffold): CustomerCheckPhone, CustomerLoginPasswordRecovery, CustomerOrderConfirmed, CustomerOrderProcessing, CustomerReadyPickup, CustomerResetPass, CustomerVoucher, and optional variants of CustomerShowFoodPanel (1..4). For each: create a JFrame class that follows existing pattern (background label, invisible clickable buttons, bringToFront for input controls). *parallelizable*
6. Assets & images: Add new product thumbnail images into `src/main/resources/com/mycompany/barkbites/CustomerDesign/` or reuse existing PNGs. Update resource paths in new panels.
7. Tests & verification: Manual UI smoke tests (launch screens). Add small unit tests for Firestore JSON parsing helpers (FirestoreDocuments.readWalletBalanceCents) if test harness exists.

**Relevant files**
- `src/main/java/com/mycompany/barkbites/CustomerForms/CustomerProfilePanelVisible.java` — modify to fetch and display profile
- `src/main/java/com/mycompany/barkbites/CustomerForms/CustomerLoginPanel.java` — example toggle implementation
- `src/main/java/com/mycompany/barkbites/CustomerForms/CustomerSignupPanel.java` — shows Firestore upsert pattern
- `src/main/java/com/mycompany/barkbites/data/firestore/FirestoreRestClient.java` — REST client to reuse
- `src/main/java/com/mycompany/barkbites/data/firestore/FirestoreDocuments.java` — helpers for reading/writing
- `src/main/java/com/mycompany/barkbites/data/auth/AuthState.java` — current session provider
- `src/main/java/com/mycompany/barkbites/CustomerForms/CustomerCartPanel.java` — current scaffold to extend
- `src/main/java/com/mycompany/barkbites/CustomerDesign` — PNG assets directory (designs)

**Missing design screens (PNG-only) to scaffold**
- CustomerCheckPhone.png
- CustomerLoginPasswordRecovery.png
- CustomerOrderConfirmed.png
- CustomerOrderProcessing.png
- CustomerReadyPickup.png
- CustomerResetPass.png
- CustomerVoucher.png
- CustomerShowFoodPanel1.png
- CustomerShowFoodPanel2.png
- CustomerShowFoodPanel3.png
- CustomerShowFoodPanel4.png

**Verification**
1. Manual: Launch `CustomerLoginPanel` → sign in → open `CustomerProfilePanelVisible` and confirm name+studentId populate.
2. Manual: Toggle password visibility on login and staff password screens.
3. Manual: Add sample items to cart state and open `CustomerCartPanel` to verify `CartItemPanel` rendering, quantity changes, and removal.
4. Manual: Edit/create menu item in Staff editor and confirm CustomerMenu updates after refresh.

**Decisions & Assumptions**
- Use Firestore REST client + idToken (existing pattern) rather than Admin SDK for cross-device simplicity.
- Store images as packaged resources or remote URLs; production should use Cloud Storage but out of scope now.
- Cart will be in-memory for now; if persistence needed later, persist under a `carts/{uid}` collection.

**Next actions (I can implement or hand off)**
1. Scaffold Java classes for the listed missing designs. (completed — 11 panels created)
2. Implement profile fetch in `CustomerProfilePanelVisible`.
3. Implement `CartItemPanel` + cart state and wire into `CustomerCartPanel`.
4. Add `MenuService` + simple Staff editor UI to update Firestore.

**Code Quality, Readability & OOP**
- **Simplify code:** prefer small focused methods, remove duplicated logic (use helpers), and extract UI wiring into clear `initUI()`/`wireListeners()` helper methods.
- **Readability:** add short Javadoc on public classes and methods, and inline // comments for non-obvious logic. Use consistent naming (verbs for methods, nouns for models).
- **Commenting:** add `// TODO` and explanatory comments where complex flows exist (Firebase calls, threading, error handling) and add Javadocs for APIs used across screens.
- **Highlight OOP usage:** mark existing files to review and annotate which OOP core principle they follow:
  - `FirebaseInitializer.java` — Encapsulation (initialization hidden), Factory-like init pattern.
  - `FirestoreRestClient.java` & `FirestoreDocuments.java` — Abstraction of REST + Data mapping.
  - `AuthState.java` & `AuthSession.java` — Encapsulation / Singleton-like state holder.
  - UI frames (e.g., `CustomerSignupPanel.java`, `CustomerLoginPanel.java`, `CustomerProfilePanelVisible.java`) — Separation of concerns: UI vs services; can be refactored to use small controller/helper classes.
  - `FormNavigator.java` — Single Responsibility for navigation between screens.
- **Refactor notes:** create small service classes (`CartService`, `MenuService`) to encapsulate data operations, and small UI component classes (`CartItemPanel`) to reuse rendering logic (promotes composition over inheritance).
- **Verification for readability:** include a brief code-review checklist in the repo (naming, method length, comments, clear error messages) and run a pass to add Javadoc to public classes.

These additions are saved in the plan. Let me know which implementation task to begin: scaffold panels, implement profile fetch, or build the cart UI.