## Plan: Split Customer and Staff Roadmaps

TL;DR - Keep the app connected through shared auth, Firestore, and navigation, but organize the work into two separate implementation tracks: one for Customer flows and one for Staff flows. Customer work centers on profile, menu, cart, and order-status screens; Staff work is now limited to design revisions and visual polish on the existing screens.

**Current progress (May 24, 2026)**
- Completed customer auth polish: `CustomerLoginPanel` and `CustomerSignupPanel` now use eye-icon password toggles that show/hide asterisks and stay editable in NetBeans GUI Builder.
- Completed customer profile and cash-in flow: `CustomerProfilePanelVisible` now loads name, student ID, and wallet balance from Firestore, and `CustomerCashInPanel` now loads the balance, masks/unmasks it, and supports preset/custom top-up actions.
- Still in progress: customer menu rendering, cart state/actions, and the remaining order-status journey screens.
- Staff-side implementation remains complete and unchanged from the May 23 update.

**Implemented on Staff side (May 23, 2026)**
- Added an editable Firestore schema layer in `StaffDatabaseSchema` so the collection/document names can be renamed later without touching every screen.
- Added Firestore-backed staff services for menu CRUD, inventory CRUD, order listing/status updates, and sales summaries.
- Added a shared `StaffFirebaseBootstrap` so staff screens initialize Firebase Admin before loading Firestore data.
- Added a local Staff demo-data fallback so the staff screens still open and load even when Firebase Admin is not configured.
- Wired `StaffPassword` to load the staff PIN from Firestore settings instead of using only a hardcoded PIN.
- Wired `StaffMenu`, `StaffOrders`, `StaffInventory`, and `StaffStatistics` to live Firestore data with refreshable CRUD/summary controls.
- Updated this plan so the completed Staff work is already recorded and does not need to be re-iterated.

**Shared foundation**
- Reuse `FormNavigator` for screen transitions across both tracks.
- Reuse `AuthState`, `FirestoreRestClient`, and `FirestoreDocuments` for all server-backed reads and writes.
- Keep menu data in Firestore so Staff edits are immediately reflected in Customer menu screens.
- Keep customer session data and staff session data logically separate, even though they share the same Firebase project.

**Customer plan**
1. Customer entry and profile flow: done for the login/signup/password-visibility portion, and profile data now loads from Firestore in `CustomerProfilePanelVisible` using the signed-in user id token and customer document lookup.
2. Customer menu flow: make `CustomerMenuPanel` and `CustomerShowFoodPanel` render menu items dynamically from Firestore instead of relying on static UI content.
3. Customer cart flow: turn `CustomerCartPanel` into a real item list with add, update quantity, and remove actions using a small cart state/service layer.
4. Customer order-status flow: keep the existing order confirmation, processing, ready-pickup, voucher, reset-password, and recovery screens as the customer-facing post-order journey.
5. Customer design scaffolding: preserve the generated PNG-backed panels already created and use them as the visual shell for the customer journey screens.

**Staff plan**
1. Staff landing page: keep `StaffLandingPage` as the entry shell only, with design-only refinements and visual polish. No heavy functional revision is expected.
2. Staff PIN screen: done. `StaffPassword` now reads the 4-digit PIN from the Firestore settings doc and falls back to the default only when Firebase Admin is unavailable.
3. Staff menu management: done. `StaffMenu` now supports Firestore CRUD for menu items through an editable document id, refresh, save, and delete flow.
4. Staff orders page: done. `StaffOrders` now lists customer orders from Firestore and updates order status.
5. Staff inventory page: done. `StaffInventory` now lists inventory items from Firestore and supports save/delete editing.
6. Staff statistics page: done. `StaffStatistics` now summarizes sales totals and monthly totals from Firestore orders.

**Relevant files**
- `src/main/java/com/mycompany/barkbites/FormNavigator.java` — shared screen navigation.
- `src/main/java/com/mycompany/barkbites/data/auth/AuthState.java` — shared signed-in session state.
- `src/main/java/com/mycompany/barkbites/data/firestore/FirestoreRestClient.java` — shared Firestore REST access.
- `src/main/java/com/mycompany/barkbites/data/firestore/FirestoreDocuments.java` — shared document mapping helpers.
- `src/main/java/com/mycompany/barkbites/CustomerForms/CustomerLoginPanel.java` — customer auth and session pattern.
- `src/main/java/com/mycompany/barkbites/CustomerForms/CustomerSignupPanel.java` — customer signup password visibility flow.
- `src/main/java/com/mycompany/barkbites/CustomerForms/CustomerProfilePanelVisible.java` — customer profile fetch target.
- `src/main/java/com/mycompany/barkbites/CustomerForms/CustomerCashInPanel.java` — customer wallet balance and cash-in flow.
- `src/main/java/com/mycompany/barkbites/CustomerForms/CustomerMenuPanel.java` — customer menu rendering target.
- `src/main/java/com/mycompany/barkbites/CustomerForms/CustomerCartPanel.java` — customer cart rendering target.
- `src/main/java/com/mycompany/barkbites/StaffForms/StaffLandingPage.java` — staff entry screen.
- `src/main/java/com/mycompany/barkbites/StaffForms/StaffPassword.java` — staff PIN gate.
- `src/main/java/com/mycompany/barkbites/StaffForms/StaffMenu.java` — staff menu CRUD hub.
- `src/main/java/com/mycompany/barkbites/StaffForms/StaffOrders.java` — staff order queue.
- `src/main/java/com/mycompany/barkbites/StaffForms/StaffInventory.java` — staff inventory management.
- `src/main/java/com/mycompany/barkbites/StaffForms/StaffStatistics.java` — staff sales reporting.

**Verification**
1. Customer: sign in, open profile, and confirm Firestore-backed fields load correctly.
2. Customer: refresh the menu screen and verify staff menu changes appear on the customer side.
3. Customer: add and remove items from the cart and confirm the UI updates correctly.
4. Staff: enter the shared PIN and verify access to the staff area is blocked when the PIN is wrong.
5. Staff: create, edit, and delete a menu item and confirm the customer menu reflects the change after refresh.
6. Staff: validate that orders, inventory, and statistics screens can read from Firestore once their collections are populated.
7. Staff: if Firebase Admin is not configured on a machine, confirm the screens still open with demo data instead of failing the load.

**Decisions**
- The staff PIN will be a shared 4-digit PIN for now.
- The staff PIN will be stored in Firestore as plain text for the prototype.
- Customer and staff flows stay separate in the UI, but they remain connected through shared Firestore data and navigation helpers.
- The previously scaffolded customer design panels remain in scope and belong to the customer track.