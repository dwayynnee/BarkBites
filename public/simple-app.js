// ===== DEMO DATA =====
const DEMO_MENU = [
    { id: 1, name: 'Pizza', price: 8.99, category: 'Main' },
    { id: 2, name: 'Spaghetti', price: 7.99, category: 'Main' },
    { id: 3, name: 'Salad', price: 5.99, category: 'Sides' },
    { id: 4, name: 'Chicken Wrap', price: 6.99, category: 'Main' },
    { id: 5, name: 'Fruit Bowl', price: 4.99, category: 'Sides' },
    { id: 6, name: 'Chocolate Cake', price: 3.99, category: 'Dessert' },
    { id: 7, name: 'Juice', price: 2.99, category: 'Drink' },
    { id: 8, name: 'Soda', price: 2.49, category: 'Drink' }
];

// ===== STATE =====
let currentStudentId = null;
let currentBalance = 50.00;
let cart = [];
let allOrders = JSON.parse(localStorage.getItem('barkbites_orders')) || [];
let currentModalItem = null;

// ===== INIT =====
window.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
});

// ===== EVENT LISTENERS =====
function setupEventListeners() {
    // Login
    document.getElementById('loginForm').addEventListener('submit', handleLogin);
    document.getElementById('logoutBtn').addEventListener('click', handleLogout);
    
    // Navigation
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const tab = e.target.getAttribute('data-tab');
            switchTab(tab);
        });
    });
    
    // Cart & Checkout
    document.getElementById('checkoutBtn').addEventListener('click', openCheckout);
    document.getElementById('addToCartBtn').addEventListener('click', addToCart);
    document.getElementById('confirmBtn').addEventListener('click', confirmOrder);
    document.getElementById('cancelBtn').addEventListener('click', closeModal);
}

// ===== LOGIN =====
function handleLogin(e) {
    e.preventDefault();
    const id = document.getElementById('studentId').value.trim();
    
    if (!id) {
        document.getElementById('loginError').textContent = 'Please enter a Student ID';
        return;
    }
    
    currentStudentId = id;
    currentBalance = 50.00;
    cart = [];
    loadStudentOrders();
    
    showScreen('appScreen');
    document.getElementById('currentUser').textContent = `Logged in as: ${id}`;
    loadMenu();
    updateWallet();
}

function handleLogout() {
    currentStudentId = null;
    cart = [];
    document.getElementById('studentId').value = '';
    document.getElementById('loginError').textContent = '';
    showScreen('loginScreen');
}

// ===== SCREEN MANAGEMENT =====
function showScreen(screenId) {
    document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
    document.getElementById(screenId).classList.add('active');
}

function switchTab(tabName) {
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    document.getElementById(tabName + 'Tab').classList.add('active');
    
    if (tabName === 'orders') loadStudentOrders();
    if (tabName === 'wallet') updateWallet();
}

// ===== MENU =====
function loadMenu() {
    const grid = document.getElementById('menuGrid');
    grid.innerHTML = '';
    
    DEMO_MENU.forEach(item => {
        const card = document.createElement('div');
        card.className = 'item-card';
        card.innerHTML = `
            <h3>${item.name}</h3>
            <p>${item.category}</p>
            <div class="item-price">₱${item.price.toFixed(2)}</div>
        `;
        card.addEventListener('click', () => openItemModal(item));
        grid.appendChild(card);
    });
}

// ===== ITEM MODAL =====
function openItemModal(item) {
    currentModalItem = item;
    document.getElementById('modalName').textContent = item.name;
    document.getElementById('modalDesc').textContent = item.category;
    document.getElementById('modalPrice').textContent = '₱' + item.price.toFixed(2);
    document.getElementById('qtyInput').value = 1;
    
    document.getElementById('itemModal').classList.add('active');
}

function closeModal() {
    document.getElementById('itemModal').classList.remove('active');
    document.getElementById('checkoutModal').classList.remove('active');
}

// ===== CART =====
function addToCart() {
    if (!currentModalItem) return;
    
    const qty = parseInt(document.getElementById('qtyInput').value) || 1;
    
    // Check if item already in cart
    const existing = cart.find(c => c.id === currentModalItem.id);
    if (existing) {
        existing.qty += qty;
    } else {
        cart.push({
            id: currentModalItem.id,
            name: currentModalItem.name,
            price: currentModalItem.price,
            qty: qty
        });
    }
    
    updateCartDisplay();
    closeModal();
    console.log('Added to cart:', currentModalItem.name, 'x', qty);
}

function updateCartDisplay() {
    const container = document.getElementById('cartItemsList');
    container.innerHTML = '';
    
    if (cart.length === 0) {
        container.innerHTML = '<p>Cart is empty</p>';
        document.getElementById('cartCount').textContent = '0';
        updateCartSummary();
        return;
    }
    
    let total = 0;
    cart.forEach(item => {
        const subtotal = item.price * item.qty;
        total += subtotal;
        
        const cartItem = document.createElement('div');
        cartItem.className = 'cart-item';
        cartItem.innerHTML = `
            <div>
                <strong>${item.name}</strong><br>
                ₱${item.price.toFixed(2)} x ${item.qty} = ₱${subtotal.toFixed(2)}
            </div>
            <button onclick="removeFromCart(${item.id})" style="width: auto; padding: 5px 10px;">Remove</button>
        `;
        container.appendChild(cartItem);
    });
    
    document.getElementById('cartCount').textContent = cart.length;
    updateCartSummary();
}

function removeFromCart(itemId) {
    cart = cart.filter(c => c.id !== itemId);
    updateCartDisplay();
}

function updateCartSummary() {
    const subtotal = cart.reduce((sum, item) => sum + (item.price * item.qty), 0);
    const tax = subtotal * 0.05;
    const total = subtotal + tax;
    
    document.getElementById('subtotal').textContent = '₱' + subtotal.toFixed(2);
    document.getElementById('tax').textContent = '₱' + tax.toFixed(2);
    document.getElementById('total').textContent = '₱' + total.toFixed(2);
    document.getElementById('balance').textContent = '₱' + currentBalance.toFixed(2);
}

// ===== CHECKOUT =====
function openCheckout() {
    if (cart.length === 0) {
        alert('Cart is empty!');
        return;
    }
    
    const subtotal = cart.reduce((sum, item) => sum + (item.price * item.qty), 0);
    const tax = subtotal * 0.05;
    const total = subtotal + tax;
    
    if (total > currentBalance) {
        alert('Insufficient balance! Need ₱' + total.toFixed(2) + ', have ₱' + currentBalance.toFixed(2));
        return;
    }
    
    let summary = '<strong>Order Summary:</strong><br><br>';
    cart.forEach(item => {
        summary += `${item.name} x${item.qty}: ₱${(item.price * item.qty).toFixed(2)}<br>`;
    });
    summary += `<br>Subtotal: ₱${subtotal.toFixed(2)}<br>Tax: ₱${tax.toFixed(2)}<br><strong>Total: ₱${total.toFixed(2)}</strong>`;
    
    document.getElementById('checkoutSummary').innerHTML = summary;
    document.getElementById('checkoutModal').classList.add('active');
}

function confirmOrder() {
    const subtotal = cart.reduce((sum, item) => sum + (item.price * item.qty), 0);
    const tax = subtotal * 0.05;
    const total = subtotal + tax;
    
    // Create order
    const order = {
        id: 'ORD-' + Date.now(),
        student_id: currentStudentId,
        items: [...cart],
        subtotal: subtotal,
        tax: tax,
        total: total,
        status: 'pending',
        created_at: new Date().toLocaleString(),
        updated_at: new Date().toLocaleString()
    };
    
    // Save order
    allOrders.push(order);
    localStorage.setItem('barkbites_orders', JSON.stringify(allOrders));
    
    // Deduct from balance
    currentBalance -= total;
    localStorage.setItem('balance_' + currentStudentId, currentBalance);
    
    // Clear cart
    cart = [];
    updateCartDisplay();
    
    closeModal();
    alert('Order placed successfully! Order ID: ' + order.id);
    switchTab('orders');
}

// ===== ORDERS =====
function loadStudentOrders() {
    const container = document.getElementById('ordersList');
    const studentOrders = allOrders.filter(o => o.student_id === currentStudentId);
    
    if (studentOrders.length === 0) {
        container.innerHTML = '<p>No orders yet</p>';
        return;
    }
    
    container.innerHTML = '';
    studentOrders.forEach(order => {
        const orderDiv = document.createElement('div');
        orderDiv.className = 'order-item';
        
        let itemsList = '';
        order.items.forEach(item => {
            itemsList += `${item.name} x${item.qty}, `;
        });
        itemsList = itemsList.slice(0, -2);
        
        orderDiv.innerHTML = `
            <p><strong>Order ID:</strong> ${order.id}</p>
            <p><strong>Items:</strong> ${itemsList}</p>
            <p><strong>Total:</strong> ₱${order.total.toFixed(2)}</p>
            <p><strong>Status:</strong> <span class="status ${order.status}">${order.status.toUpperCase()}</span></p>
            <p style="color: #999; font-size: 12px;">${order.created_at}</p>
        `;
        container.appendChild(orderDiv);
    });
}

// ===== WALLET =====
function updateWallet() {
    const saved = localStorage.getItem('balance_' + currentStudentId);
    if (saved) {
        currentBalance = parseFloat(saved);
    }
    
    document.getElementById('walletBalance').textContent = '₱' + currentBalance.toFixed(2);
    document.getElementById('balance').textContent = '₱' + currentBalance.toFixed(2);
    updateCartSummary();
}

console.log('Bark Bites Web App Ready - Demo Mode');
