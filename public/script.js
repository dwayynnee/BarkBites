// ===== DOM ELEMENTS =====
const loginScreen = document.getElementById('loginScreen');
const appScreen = document.getElementById('appScreen');
const loginForm = document.getElementById('loginForm');
const studentIdInput = document.getElementById('studentId');
const loginError = document.getElementById('loginError');
const logoutBtn = document.getElementById('logoutBtn');

// Navigation
const navBtns = document.querySelectorAll('.nav-btn:not(.logout)');
const tabContents = document.querySelectorAll('.tab-content');

// Menu Tab
const menuGrid = document.getElementById('menuGrid');
const filterBtns = document.querySelectorAll('.filter-btn');

// Cart Tab
const cartItems = document.getElementById('cartItems');
const cartCount = document.getElementById('cartCount');
const subtotalEl = document.getElementById('subtotal');
const taxEl = document.getElementById('tax');
const totalEl = document.getElementById('total');
const checkoutBtn = document.getElementById('checkoutBtn');
const continueShopping = document.getElementById('continueShopping');
const cartWalletBalance = document.getElementById('walletBalance');

// Orders Tab
const ordersList = document.getElementById('ordersList');

// Wallet Tab
const walletBalanceLarge = document.getElementById('walletBalanceLarge');
const totalSpent = document.getElementById('totalSpent');
const orderCount = document.getElementById('orderCount');
const transactionHistory = document.getElementById('transactionHistory');

// Modals
const itemModal = document.getElementById('itemModal');
const checkoutModal = document.getElementById('checkoutModal');
const modalCloses = document.querySelectorAll('.close');
const itemModalName = document.getElementById('modalItemName');
const itemModalDescription = document.getElementById('modalItemDescription');
const itemModalPrice = document.getElementById('modalItemPrice');
const modalSubtotal = document.getElementById('modalSubtotal');
const qtyInput = document.getElementById('qtyInput');
const increaseQty = document.getElementById('increaseQty');
const decreaseQty = document.getElementById('decreaseQty');
const addToCartBtn = document.getElementById('addToCartBtn');
const confirmOrderBtn = document.getElementById('confirmOrderBtn');
const cancelOrderBtn = document.getElementById('cancelOrderBtn');
const checkoutSummary = document.getElementById('checkoutSummary');

// Toast
const toast = document.getElementById('toast');

// ===== STATE MANAGEMENT =====
let currentStudent = null;
let currentWallet = null;
let cart = [];
let allMenuItems = [];
let currentFilter = 'all';
let currentModalItem = null;

// ===== INITIALIZATION =====
async function init() {
    setupEventListeners();
    console.log('App initialized');
}

// ===== EVENT LISTENERS =====
function setupEventListeners() {
    // Login
    loginForm.addEventListener('submit', handleLogin);
    logoutBtn.addEventListener('click', handleLogout);

    // Navigation
    navBtns.forEach(btn => {
        btn.addEventListener('click', (e) => {
            const tabName = e.target.closest('.nav-btn').getAttribute('data-tab');
            switchTab(tabName);
        });
    });

    // Menu Filters
    filterBtns.forEach(btn => {
        btn.addEventListener('click', (e) => {
            filterBtns.forEach(b => b.classList.remove('active'));
            e.target.classList.add('active');
            currentFilter = e.target.getAttribute('data-category');
            displayMenuItems();
        });
    });

    // Cart
    checkoutBtn.addEventListener('click', openCheckoutModal);
    continueShopping.addEventListener('click', () => switchTab('menu'));

    // Modal
    modalCloses.forEach(close => {
        close.addEventListener('click', (e) => {
            e.target.closest('.modal').classList.remove('active');
        });
    });

    // Modal quantity controls
    increaseQty.addEventListener('click', () => {
        qtyInput.value = parseInt(qtyInput.value) + 1;
        updateModalSubtotal();
    });

    decreaseQty.addEventListener('click', () => {
        const current = parseInt(qtyInput.value);
        if (current > 1) {
            qtyInput.value = current - 1;
            updateModalSubtotal();
        }
    });

    qtyInput.addEventListener('change', updateModalSubtotal);
    addToCartBtn.addEventListener('click', addToCart);

    // Checkout
    confirmOrderBtn.addEventListener('click', confirmOrder);
    cancelOrderBtn.addEventListener('click', () => {
        checkoutModal.classList.remove('active');
    });

    // Toast
    document.addEventListener('click', (e) => {
        if (e.target.classList.contains('modal')) {
            e.target.classList.remove('active');
        }
    });
}

// ===== AUTHENTICATION =====
async function handleLogin(e) {
    e.preventDefault();
    const studentId = studentIdInput.value.trim();

    if (!studentId) {
        loginError.textContent = 'Please enter a Student ID';
        return;
    }

    try {
        // Try normal Firestore-backed login first
        const userDoc = await firestoreService.getUserByStudentId(studentId);
        
        if (!userDoc) {
            const newUser = {
                student_id: studentId,
                name: `Student ${studentId}`,
                email: `${studentId}@school.edu`,
                role: 'student',
                created_at: new Date(),
                last_login: new Date()
            };
            await firestoreService.createUser(newUser);
            currentStudent = { id: studentId, ...newUser };
        } else {
            currentStudent = { id: studentId, ...userDoc };
            await firestoreService.updateUser(studentId, { last_login: new Date() });
        }

        currentWallet = await firestoreService.getWalletByStudentId(studentId);
        if (!currentWallet) {
            await firestoreService.createWallet({
                student_id: studentId,
                balance: 50.00,
                created_at: new Date(),
                transactions: []
            });
            currentWallet = {
                student_id: studentId,
                balance: 50.00,
                transactions: []
            };
        }

        allMenuItems = await firestoreService.getAllMenuItems();

        loginError.textContent = '';
        showAppScreen();
        showToast(`Welcome, ${currentStudent.name || studentId}!`, 'success');
    } catch (error) {
        // Fallback: allow login even if Firestore is unavailable
        console.error('Login error (falling back to demo mode):', error);

        currentStudent = {
            id: studentId,
            student_id: studentId,
            name: `Student ${studentId}`,
            email: `${studentId}@demo.local`,
            role: 'student',
            created_at: new Date(),
            last_login: new Date()
        };

        currentWallet = {
            student_id: studentId,
            balance: 50.00,
            transactions: []
        };

        loginError.textContent = '';
        showAppScreen();
        showToast(`Demo login for ${studentId} (Firestore offline)`, 'info');
    }
}

function handleLogout() {
    currentStudent = null;
    currentWallet = null;
    cart = [];
    loginError.textContent = '';
    studentIdInput.value = '';
    qtyInput.value = '1';
    showLoginScreen();
    showToast('Logged out successfully', 'info');
}

// ===== SCREEN MANAGEMENT =====
function showLoginScreen() {
    loginScreen.classList.add('active');
    appScreen.classList.remove('active');
}

function showAppScreen() {
    loginScreen.classList.remove('active');
    appScreen.classList.add('active');
    switchTab('menu');
    loadMenuItems();
    loadOrders();
    loadWallet();
}

// ===== TAB SWITCHING =====
function switchTab(tabName) {
    // Update nav buttons
    navBtns.forEach(btn => btn.classList.remove('active'));
    document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');

    // Update tab content
    tabContents.forEach(tab => tab.classList.remove('active'));
    document.getElementById(`${tabName}Tab`).classList.add('active');
}

// ===== MENU MANAGEMENT =====
async function loadMenuItems() {
    try {
        allMenuItems = await firestoreService.getAllMenuItems();
        displayMenuItems();
    } catch (error) {
        console.error('Error loading menu items:', error);
        showToast('Failed to load menu items', 'error');
    }
}

function displayMenuItems() {
    let itemsToDisplay = allMenuItems;

    // Filter items
    if (currentFilter !== 'all') {
        itemsToDisplay = allMenuItems.filter(item => item.category === currentFilter);
    }

    // Render menu grid
    menuGrid.innerHTML = itemsToDisplay.map(item => `
        <div class="menu-card${item.available ? '' : ' unavailable'}">
            <div class="menu-card-image">${item.emoji || '🍱'}</div>
            <div class="menu-card-body">
                <div class="menu-card-name">${item.name}</div>
                <div class="menu-card-category">${item.category}</div>
                <div class="menu-card-price">$${item.price.toFixed(2)}</div>
                <button class="menu-card-btn" onclick="openItemModal('${item.id}', '${item.name}', '${item.description}', ${item.price}, ${item.available})">
                    ${item.available ? 'View & Order' : 'Out of Stock'}
                </button>
            </div>
        </div>
    `).join('');
}

// ===== MODAL MANAGEMENT =====
function openItemModal(itemId, itemName, itemDescription, itemPrice, available) {
    if (!available) {
        showToast('This item is currently out of stock', 'info');
        return;
    }

    currentModalItem = { id: itemId, name: itemName, price: itemPrice };
    itemModalName.textContent = itemName;
    itemModalDescription.textContent = itemDescription;
    itemModalPrice.textContent = `$${itemPrice.toFixed(2)}`;
    qtyInput.value = '1';
    updateModalSubtotal();
    itemModal.classList.add('active');
}

function updateModalSubtotal() {
    const qty = parseInt(qtyInput.value) || 1;
    const subtotal = currentModalItem.price * qty;
    modalSubtotal.textContent = `$${subtotal.toFixed(2)}`;
}

// ===== CART MANAGEMENT =====
function addToCart() {
    const qty = parseInt(qtyInput.value) || 1;

    const cartItem = {
        itemId: currentModalItem.id,
        name: currentModalItem.name,
        price: currentModalItem.price,
        quantity: qty
    };

    // Check if item already in cart
    const existingItem = cart.find(item => item.itemId === cartItem.itemId);
    if (existingItem) {
        existingItem.quantity += qty;
    } else {
        cart.push(cartItem);
    }

    updateCartDisplay();
    itemModal.classList.remove('active');
    showToast(`Added ${cartItem.name} to cart`, 'success');
}

function removeFromCart(itemId) {
    cart = cart.filter(item => item.itemId !== itemId);
    updateCartDisplay();
    showToast('Removed from cart', 'info');
}

function updateCartQuantity(itemId, newQty) {
    const cartItem = cart.find(item => item.itemId === itemId);
    if (cartItem) {
        cartItem.quantity = parseInt(newQty) || 1;
        if (cartItem.quantity <= 0) {
            removeFromCart(itemId);
        } else {
            updateCartDisplay();
        }
    }
}

function updateCartDisplay() {
    // Update cart count badge
    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
    cartCount.textContent = totalItems;

    // Calculate totals
    const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    const tax = subtotal * 0.05;
    const total = subtotal + tax;

    // Display cart items
    if (cart.length === 0) {
        cartItems.innerHTML = '<p style="text-align: center; color: #999; padding: 40px;">Your cart is empty</p>';
    } else {
        cartItems.innerHTML = cart.map(item => `
            <div class="cart-item">
                <div class="cart-item-info">
                    <div class="cart-item-name">${item.name}</div>
                    <div class="cart-item-price">$${item.price.toFixed(2)} each</div>
                </div>
                <div class="cart-item-controls">
                    <button class="qty-btn" onclick="updateCartQuantity('${item.itemId}', ${item.quantity - 1})">−</button>
                    <input type="number" class="cart-item-qty" value="${item.quantity}" onchange="updateCartQuantity('${item.itemId}', this.value)">
                    <button class="qty-btn" onclick="updateCartQuantity('${item.itemId}', ${item.quantity + 1})">+</button>
                </div>
                <div class="cart-item-subtotal">$${(item.price * item.quantity).toFixed(2)}</div>
                <button class="cart-item-remove" onclick="removeFromCart('${item.itemId}')">Remove</button>
            </div>
        `).join('');
    }

    // Update summary
    subtotalEl.textContent = `$${subtotal.toFixed(2)}`;
    taxEl.textContent = `$${tax.toFixed(2)}`;
    totalEl.textContent = `$${total.toFixed(2)}`;
    if (currentWallet) {
        cartWalletBalance.textContent = `$${currentWallet.balance.toFixed(2)}`;
    }
}

// ===== CHECKOUT =====
function openCheckoutModal() {
    if (cart.length === 0) {
        showToast('Your cart is empty', 'info');
        return;
    }

    const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    const tax = subtotal * 0.05;
    const total = subtotal + tax;

    // Check balance
    if (currentWallet.balance < total) {
        showToast('Insufficient balance in wallet', 'error');
        return;
    }

    // Display checkout summary
    checkoutSummary.innerHTML = `
        <div class="checkout-item">
            <span>Subtotal</span>
            <span>$${subtotal.toFixed(2)}</span>
        </div>
        <div class="checkout-item">
            <span>Tax (5%)</span>
            <span>$${tax.toFixed(2)}</span>
        </div>
        <div class="checkout-item" style="border-top: 1px solid var(--border-color); padding-top: 10px; font-weight: bold; font-size: 18px;">
            <span>Total</span>
            <span>$${total.toFixed(2)}</span>
        </div>
        <div style="margin-top: 15px; padding: 10px; background: var(--light-bg); border-radius: 6px;">
            <p>Current Balance: <strong>$${currentWallet.balance.toFixed(2)}</strong></p>
            <p>After Payment: <strong>$${(currentWallet.balance - total).toFixed(2)}</strong></p>
        </div>
    `;

    checkoutModal.classList.add('active');
}

async function confirmOrder() {
    if (cart.length === 0) {
        showToast('Your cart is empty', 'error');
        return;
    }

    const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    const tax = subtotal * 0.05;
    const total = subtotal + tax;

    try {
        // Deduct from wallet
        await firestoreService.deductFromWallet(currentStudent.id, total, null);

        // Create order
        const order = {
            student_id: currentStudent.id,
            items: cart.map(item => ({
                menu_item_id: item.itemId,
                name: item.name,
                quantity: item.quantity,
                unit_price: item.price
            })),
            total_price: total,
            status: 'pending',
            created_at: new Date(),
            updated_at: new Date(),
            notes: ''
        };

        await firestoreService.createOrder(order);

        // Update inventory
        for (const cartItem of cart) {
            await firestoreService.deductInventory(cartItem.itemId, cartItem.quantity);
        }

        // Update wallet display
        currentWallet.balance -= total;
        currentWallet.transactions.push({
            type: 'order',
            amount: total,
            date: new Date(),
            order_id: null
        });

        // Clear cart
        cart = [];
        updateCartDisplay();
        checkoutModal.classList.remove('active');

        showToast('Order placed successfully! 🎉', 'success');
        loadOrders();
        loadWallet();
    } catch (error) {
        console.error('Order creation error:', error);
        showToast('Failed to place order. Please try again.', 'error');
    }
}

// ===== ORDERS =====
async function loadOrders() {
    try {
        if (!currentStudent) return;

        const orders = await firestoreService.getOrdersByStudent(currentStudent.id);
        displayOrders(orders);

        // Set up real-time listener
        firestoreService.onStudentOrdersChange(currentStudent.id, (updatedOrders) => {
            displayOrders(updatedOrders);
        });
    } catch (error) {
        console.error('Error loading orders:', error);
    }
}

function displayOrders(orders) {
    if (!orders || orders.length === 0) {
        ordersList.innerHTML = '<p style="text-align: center; color: #999; padding: 40px;">No orders yet</p>';
        return;
    }

    ordersList.innerHTML = orders.map(order => `
        <div class="order-card">
            <div class="order-header">
                <div class="order-id">Order #${order.id?.slice(0, 8) || 'N/A'}</div>
                <span class="order-status ${order.status}">${order.status.replace('_', ' ').toUpperCase()}</span>
            </div>
            <div class="order-items">
                ${order.items.map(item => `
                    <div class="order-item">
                        <span>${item.name} × ${item.quantity}</span>
                        <span>$${(item.unit_price * item.quantity).toFixed(2)}</span>
                    </div>
                `).join('')}
            </div>
            <div class="order-summary">
                <span>Total: <strong>$${order.total_price.toFixed(2)}</strong></span>
                <span>${new Date(order.created_at?.toDate?.() || order.created_at).toLocaleDateString()}</span>
            </div>
        </div>
    `).join('');
}

// ===== WALLET =====
async function loadWallet() {
    try {
        if (!currentStudent) return;

        currentWallet = await firestoreService.getWalletByStudentId(currentStudent.id);
        displayWallet();

        // Set up real-time listener
        firestoreService.onWalletChange(currentStudent.id, (updatedWallet) => {
            currentWallet = updatedWallet;
            displayWallet();
        });
    } catch (error) {
        console.error('Error loading wallet:', error);
    }
}

function displayWallet() {
    if (!currentWallet) return;

    walletBalanceLarge.textContent = `$${currentWallet.balance.toFixed(2)}`;

    // Calculate total spent
    const transactions = currentWallet.transactions || [];
    const totalSpentAmount = transactions
        .filter(t => t.type === 'order')
        .reduce((sum, t) => sum + t.amount, 0);

    // Count orders
    const orders = transactions.filter(t => t.type === 'order').length;

    totalSpent.textContent = `$${totalSpentAmount.toFixed(2)}`;
    orderCount.textContent = orders;

    // Display transactions
    if (transactions.length === 0) {
        transactionHistory.innerHTML = '<p style="text-align: center; color: #999; padding: 20px;">No transactions yet</p>';
    } else {
        transactionHistory.innerHTML = transactions
            .sort((a, b) => (b.date?.toDate?.() || b.date) - (a.date?.toDate?.() || a.date))
            .map(transaction => `
                <div class="transaction-item">
                    <div class="transaction-info">
                        <div class="transaction-type">${transaction.type === 'order' ? '🍱 Order' : '💰 Recharge'}</div>
                        <div class="transaction-date">${new Date(transaction.date?.toDate?.() || transaction.date).toLocaleDateString()}</div>
                    </div>
                    <div class="transaction-amount ${transaction.type === 'order' ? 'debit' : 'credit'}">
                        ${transaction.type === 'order' ? '-' : '+'}$${transaction.amount.toFixed(2)}
                    </div>
                </div>
            `).join('');
    }

    // Update cart wallet display
    cartWalletBalance.textContent = `$${currentWallet.balance.toFixed(2)}`;
}

// ===== NOTIFICATIONS =====
function showToast(message, type = 'info') {
    toast.textContent = message;
    toast.className = `toast ${type} show`;

    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

// ===== INITIALIZATION =====
init();
