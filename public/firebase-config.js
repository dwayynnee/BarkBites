/**
 * Firebase Configuration for BarkBites Web Application
 * 
 * This file initializes Firebase SDK for the student-facing web app.
 * Replace the config values with your Firebase project credentials.
 * 
 * Get these values from:
 * Firebase Console > Project Settings > Your apps > Web app config
 */

// Initialize Firebase
// These are public API keys (safe to expose in frontend)
const firebaseConfig = {
  apiKey: "AIzaSyAX_y7YlUKixzQOQD0E66pbbYOUx6s8gKE",
  authDomain: "barkbites-student.firebaseapp.com",
  projectId: "barkbites-student",
  storageBucket: "barkbites-student.firebasestorage.app",
  messagingSenderId: "751754860190",
  appId: "1:751754860190:web:8eca614393be8ac09f0d8f",
  measurementId: "G-16D6BLLYKE"
};

// Initialize Firebase
try {
  firebase.initializeApp(firebaseConfig);
  console.log("Firebase initialized successfully!");
} catch (error) {
  console.error("Firebase initialization error:", error);
}

// Get Firebase services
const db = firebase.firestore();
const auth = firebase.auth();

// Simple auth so Firestore rules that require authentication can pass
auth.signInAnonymously().then(() => {
  console.log("Signed in anonymously for Firestore access");
}).catch((error) => {
  console.error("Anonymous sign-in failed:", error);
});

/**
 * Database Collections Reference
 */
const COLLECTIONS = {
  USERS: 'users',
  MENU_ITEMS: 'menu_items',
  ORDERS: 'orders',
  INVENTORY: 'inventory',
  WALLETS: 'wallets'
};

/**
 * Firestore Database Service Layer
 * Provides functions to interact with Firestore from the web app
 */
const firestoreService = {
  
  // ==================== USER OPERATIONS ====================
  
  /**
   * Get user by student ID
   */
  async getUserByStudentId(studentId) {
    try {
      const doc = await db.collection(COLLECTIONS.USERS).doc(studentId).get();
      return doc.exists ? doc.data() : null;
    } catch (error) {
      console.error("Error getting user:", error);
      return null;
    }
  },

  /**
   * Get user by ID (alias)
   */
  async getUserById(studentId) {
    return this.getUserByStudentId(studentId);
  },

  /**
   * Create new user
   */
  async createUser(user) {
    try {
      await db.collection(COLLECTIONS.USERS).doc(user.student_id).set(user);
      console.log("User created:", user.student_id);
      return true;
    } catch (error) {
      console.error("Error creating user:", error);
      throw error;
    }
  },

  /**
   * Update user
   */
  async updateUser(studentId, updates) {
    try {
      await db.collection(COLLECTIONS.USERS).doc(studentId).update(updates);
      console.log("User updated:", studentId);
      return true;
    } catch (error) {
      console.error("Error updating user:", error);
      throw error;
    }
  },

  /**
   * Save or update user (alias)
   */
  async saveUser(user) {
    try {
      await db.collection(COLLECTIONS.USERS).doc(user.student_id).set(user, { merge: true });
      console.log("User saved:", user.student_id);
      return true;
    } catch (error) {
      console.error("Error saving user:", error);
      return false;
    }
  },

  /**
   * Update last login time
   */
  async updateLastLogin(studentId) {
    try {
      await db.collection(COLLECTIONS.USERS).doc(studentId).update({
        last_login: new Date()
      });
    } catch (error) {
      console.error("Error updating last login:", error);
    }
  },

  // ==================== MENU OPERATIONS ====================

  /**
   * Get all available menu items
   */
  async getAllMenuItems() {
    try {
      const snapshot = await db.collection(COLLECTIONS.MENU_ITEMS)
        .where('available', '==', true)
        .orderBy('category')
        .get();
      return snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      }));
    } catch (error) {
      console.error("Error getting menu items:", error);
      return [];
    }
  },

  /**
   * Get menu items by category
   */
  async getMenuItemsByCategory(category) {
    try {
      const snapshot = await db.collection(COLLECTIONS.MENU_ITEMS)
        .where('category', '==', category)
        .where('available', '==', true)
        .get();
      return snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      }));
    } catch (error) {
      console.error("Error getting menu items by category:", error);
      return [];
    }
  },

  /**
   * Get single menu item
   */
  async getMenuItemById(itemId) {
    try {
      const doc = await db.collection(COLLECTIONS.MENU_ITEMS).doc(itemId).get();
      return doc.exists ? {
        id: doc.id,
        ...doc.data()
      } : null;
    } catch (error) {
      console.error("Error getting menu item:", error);
      return null;
    }
  },

  // ==================== ORDER OPERATIONS ====================

  /**
   * Create new order
   */
  async createOrder(order) {
    try {
      const orderRef = await db.collection(COLLECTIONS.ORDERS).add({
        ...order,
        created_at: new Date(),
        status: 'pending'
      });
      
      // Generate order number
      const orderNumber = "#" + String(Date.now()).slice(-3).padStart(3, '0');
      await orderRef.update({ order_number: orderNumber });
      
      console.log("Order created:", orderRef.id);
      return orderRef.id;
    } catch (error) {
      console.error("Error creating order:", error);
      throw error;
    }
  },

  /**
   * Get orders for student
   */
  async getOrdersByStudent(studentId) {
    try {
      const snapshot = await db.collection(COLLECTIONS.ORDERS)
        .where('student_id', '==', studentId)
        .orderBy('created_at', 'desc')
        .get();
      return snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      }));
    } catch (error) {
      console.error("Error getting orders:", error);
      return [];
    }
  },

  /**
   * Get single order by ID
   */
  async getOrderById(orderId) {
    try {
      const doc = await db.collection(COLLECTIONS.ORDERS).doc(orderId).get();
      return doc.exists ? {
        id: doc.id,
        ...doc.data()
      } : null;
    } catch (error) {
      console.error("Error getting order:", error);
      return null;
    }
  },

  /**
   * Set up real-time listener for student orders
   */
  onStudentOrdersChange(studentId, callback) {
    try {
      return db.collection(COLLECTIONS.ORDERS)
        .where('student_id', '==', studentId)
        .orderBy('created_at', 'desc')
        .onSnapshot(snapshot => {
          const orders = snapshot.docs.map(doc => ({
            id: doc.id,
            ...doc.data()
          }));
          callback(orders);
        });
    } catch (error) {
      console.error("Error setting up orders listener:", error);
    }
  },

  /**
   * Set up real-time listener for single order
   */
  onOrderChange(orderId, callback) {
    try {
      return db.collection(COLLECTIONS.ORDERS)
        .doc(orderId)
        .onSnapshot(doc => {
          if (doc.exists) {
            callback({
              id: doc.id,
              ...doc.data()
            });
          }
        });
    } catch (error) {
      console.error("Error setting up order listener:", error);
    }
  },

  // ==================== INVENTORY OPERATIONS ====================

  /**
   * Get all inventory items
   */
  async getAllInventory() {
    try {
      const snapshot = await db.collection(COLLECTIONS.INVENTORY).get();
      return snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      }));
    } catch (error) {
      console.error("Error getting inventory:", error);
      return [];
    }
  },

  /**
   * Get inventory for specific menu item
   */
  async getInventoryByItemId(menuItemId) {
    try {
      const snapshot = await db.collection(COLLECTIONS.INVENTORY)
        .where('menu_item_id', '==', menuItemId)
        .get();
      return snapshot.docs.length > 0 ? {
        id: snapshot.docs[0].id,
        ...snapshot.docs[0].data()
      } : null;
    } catch (error) {
      console.error("Error getting inventory:", error);
      return null;
    }
  },

  /**
   * Deduct from inventory (when order is placed)
   */
  async deductInventory(menuItemId, quantity) {
    try {
      const inventory = await this.getInventoryByItemId(menuItemId);
      if (!inventory) {
        throw new Error("Inventory not found for item");
      }

      const newQuantity = inventory.quantity_available - quantity;
      if (newQuantity < 0) {
        throw new Error("Insufficient inventory");
      }

      await db.collection(COLLECTIONS.INVENTORY).doc(inventory.id).update({
        quantity_available: newQuantity,
        quantity_sold_today: (inventory.quantity_sold_today || 0) + quantity,
        out_of_stock: newQuantity === 0
      });

      console.log("Inventory deducted:", menuItemId, quantity);
      return true;
    } catch (error) {
      console.error("Error deducting inventory:", error);
      throw error;
    }
  },

  /**
   * Set up real-time listener for inventory
   */
  onInventoryChange(callback) {
    try {
      return db.collection(COLLECTIONS.INVENTORY).onSnapshot(snapshot => {
        const inventory = snapshot.docs.map(doc => ({
          id: doc.id,
          ...doc.data()
        }));
        callback(inventory);
      });
    } catch (error) {
      console.error("Error setting up inventory listener:", error);
    }
  },

  // ==================== WALLET OPERATIONS ====================

  /**
   * Get wallet for student
   */
  async getWalletByStudentId(studentId) {
    try {
      const doc = await db.collection(COLLECTIONS.WALLETS).doc(studentId).get();
      return doc.exists ? doc.data() : null;
    } catch (error) {
      console.error("Error getting wallet:", error);
      return null;
    }
  },

  /**
   * Create new wallet
   */
  async createWallet(wallet) {
    try {
      await db.collection(COLLECTIONS.WALLETS).doc(wallet.student_id).set(wallet);
      console.log("Wallet created:", wallet.student_id);
      return true;
    } catch (error) {
      console.error("Error creating wallet:", error);
      throw error;
    }
  },

  /**
   * Deduct from wallet for purchase
   */
  async deductFromWallet(studentId, amount, orderId) {
    try {
      const wallet = await this.getWalletByStudentId(studentId);
      if (!wallet) {
        throw new Error("Wallet not found");
      }

      if (wallet.balance < amount) {
        throw new Error("Insufficient balance");
      }

      const newBalance = wallet.balance - amount;
      const transaction = {
        type: 'order',
        amount: amount,
        date: new Date(),
        order_id: orderId
      };

      const transactions = wallet.transactions || [];
      transactions.push(transaction);

      await db.collection(COLLECTIONS.WALLETS).doc(studentId).update({
        balance: newBalance,
        transactions: transactions
      });

      console.log("Wallet deducted:", studentId, amount);
      return true;
    } catch (error) {
      console.error("Error deducting from wallet:", error);
      throw error;
    }
  },

  /**
   * Add to wallet (recharge)
   */
  async addToWallet(studentId, amount) {
    try {
      const wallet = await this.getWalletByStudentId(studentId);
      if (!wallet) {
        throw new Error("Wallet not found");
      }

      const newBalance = wallet.balance + amount;
      const transaction = {
        type: 'recharge',
        amount: amount,
        date: new Date()
      };

      const transactions = wallet.transactions || [];
      transactions.push(transaction);

      await db.collection(COLLECTIONS.WALLETS).doc(studentId).update({
        balance: newBalance,
        transactions: transactions
      });

      return true;
    } catch (error) {
      console.error("Error adding to wallet:", error);
      throw error;
    }
  },

  /**
   * Set up real-time listener for wallet
   */
  onWalletChange(studentId, callback) {
    try {
      return db.collection(COLLECTIONS.WALLETS)
        .doc(studentId)
        .onSnapshot(doc => {
          if (doc.exists) {
            callback(doc.data());
          }
        });
    } catch (error) {
      console.error("Error setting up wallet listener:", error);
    }
  }
};

/**
 * Export for use in scripts
 * Usage in HTML: 
 *   <script src="firebase-config.js"></script>
 *   Then access via: firestoreService.getAllMenuItems()
 */
