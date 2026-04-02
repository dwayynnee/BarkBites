require('dotenv').config();
const express = require('express');
const path = require('path');
const admin = require('firebase-admin');

const app = express();

// Initialize Firebase Admin SDK
let db = null;

try {
  // Check if firebase-key.json exists
  const fs = require('fs');
  if (fs.existsSync('./firebase-key.json')) {
    const serviceAccount = require('./firebase-key.json');
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount)
    });
    db = admin.firestore();
    console.log('✅ Firebase Firestore initialized successfully');
  } else {
    console.log('⚠️ firebase-key.json not found - Running in demo mode');
  }
} catch (error) {
  console.log('⚠️ Firebase initialization warning:', error.message);
}

// Middleware
app.use(express.static(path.join(__dirname, 'public')));
app.use(express.json());

// Convert Firestore SDK types (Timestamp, etc.) into JSON-friendly values
function serializeFirestoreValue(value) {
  if (value == null) return value;
  if (value instanceof admin.firestore.Timestamp) {
    return value.toDate().toISOString();
  }
  if (Array.isArray(value)) {
    return value.map(serializeFirestoreValue);
  }
  if (typeof value === 'object') {
    const out = {};
    for (const [k, v] of Object.entries(value)) {
      out[k] = serializeFirestoreValue(v);
    }
    return out;
  }
  return value;
}

// Route for home page
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', message: 'Bark Bites is running!' });
});

// API endpoint to verify Firebase config is loaded
app.get('/api/config', (req, res) => {
  const hasFirebaseConfig = !!process.env.REACT_APP_FIREBASE_PROJECT_ID || db;
  res.json({ 
    status: 'ok', 
    firebase_configured: hasFirebaseConfig,
    firebase_initialized: !!db,
    environment: process.env.NODE_ENV || 'development'
  });
});

// ==================== FIRESTORE API ENDPOINTS ====================

// TEST DATA ENDPOINT - Add sample menu items
app.post('/api/setup', async (req, res) => {
  try {
    if (!db) {
      return res.status(503).json({ 
        error: 'Firebase not initialized',
        message: 'Firestore database not connected. Check firebase-key.json exists and is valid.'
      });
    }

    const menuItems = [
      { id: "1", name: "Pizza Margherita", description: "Classic pizza", price: 8.99, category: "Main Course", available: true },
      { id: "2", name: "Pepperoni Pizza", description: "Pizza with pepperoni", price: 9.99, category: "Main Course", available: true },
      { id: "3", name: "Caesar Salad", description: "Fresh greens with dressing", price: 6.99, category: "Sides", available: true },
      { id: "4", name: "Spaghetti Carbonara", description: "Creamy pasta with bacon", price: 10.99, category: "Main Course", available: true },
      { id: "5", name: "Chocolate Cake", description: "Rich chocolate cake", price: 4.99, category: "Dessert", available: true },
      { id: "6", name: "Iced Tea", description: "Refreshing iced tea", price: 2.49, category: "Drink", available: true },
      { id: "7", name: "Soda", description: "Cold soda drink", price: 1.99, category: "Drink", available: true },
      { id: "8", name: "Chicken Sandwich", description: "Grilled chicken with lettuce", price: 7.99, category: "Main Course", available: true }
    ];

    let added = 0;
    const errors = [];
    
    for (const item of menuItems) {
      try {
        await db.collection('menu_items').doc(item.id).set(item);
        added++;
      } catch (itemError) {
        errors.push({ id: item.id, error: itemError.message });
        console.error(`Failed to add item ${item.id}:`, itemError.message);
      }
    }

    if (added === 0) {
      console.error('Setup failed - No items added. Firestore database may not exist.');
      console.error('Instructions: Create a Firestore database at https://console.firebase.google.com/');
      return res.status(500).json({ 
        error: 'Failed to add menu items',
        message: 'Firestore database may not exist. Create it in Firebase Console: https://console.firebase.google.com/project/barkbites-22cdf/firestore',
        details: errors
      });
    }

    console.log(`✅ Setup complete: Added ${added}/${menuItems.length} menu items`);
    res.json({ 
      success: true, 
      message: `Added ${added} menu items to Firestore`,
      items_added: added,
      failed_items: errors.length > 0 ? errors : undefined
    });

  } catch (error) {
    console.error('Error in setup:', error.message);
    if (error.code === 5 || error.message.includes('NOT_FOUND')) {
      return res.status(500).json({ 
        error: 'Firestore database not found',
        message: 'Please create a Firestore database in Firebase Console: https://console.firebase.google.com/project/barkbites-22cdf/firestore/data',
        code: error.code
      });
    }
    res.status(500).json({ error: error.message });
  }
});

// Get all menu items
app.get('/api/menu_items', async (req, res) => {
  try {
    if (!db) {
      return res.status(503).json({ error: 'Firebase not initialized', items: [] });
    }
    const snapshot = await db.collection('menu_items').get();
    const items = [];
    snapshot.forEach(doc => {
      items.push(serializeFirestoreValue({ id: doc.id, ...doc.data() }));
    });
    console.log(`✅ Fetched ${items.length} menu items from Firestore`);
    res.json(items);
  } catch (error) {
    console.error('Error fetching menu:', error.message);
    // Return empty array instead of error - collections might not exist yet
    res.json([]);
  }
});

// Add menu item (used by Swing staff kiosk)
app.post('/api/menu_items/add', async (req, res) => {
  try {
    if (!db) {
      return res.status(503).json({ error: 'Firebase not initialized' });
    }
    const { id, name, description, price, category, available, quantity_available } = req.body || {};
    if (!id || !name) {
      return res.status(400).json({ error: 'Missing required fields: id, name' });
    }

    let quantityAvailable = null;
    if (quantity_available !== undefined && quantity_available !== null && String(quantity_available).trim() !== '') {
      const parsed = Number.parseInt(quantity_available, 10);
      if (!Number.isFinite(parsed) || parsed < 0) {
        return res.status(400).json({ error: 'Invalid quantity_available (must be a non-negative integer)' });
      }
      quantityAvailable = parsed;
    }

    const payload = {
      id: String(id),
      name: String(name),
      description: description ? String(description) : '',
      price: typeof price === 'number' ? price : Number(price),
      category: category ? String(category) : 'Uncategorized',
      available: typeof available === 'boolean' ? available : true,
      updated_at: new Date()
    };
    if (!Number.isFinite(payload.price)) {
      return res.status(400).json({ error: 'Invalid price' });
    }

    await db.collection('menu_items').doc(payload.id).set(payload, { merge: true });

    // If quantity is provided, also create/update inventory for the item.
    if (quantityAvailable !== null) {
      const invRef = db.collection('inventory').doc(payload.id);
      const invSnap = await invRef.get();

      const invBase = {
        menu_item_id: payload.id,
        quantity_available: quantityAvailable,
        is_out_of_stock: quantityAvailable <= 0,
        last_updated: new Date()
      };

      if (invSnap.exists) {
        await invRef.set(invBase, { merge: true });
      } else {
        await invRef.set({
          ...invBase,
          quantity_sold_today: 0,
          low_stock_threshold: 10
        }, { merge: true });
      }
    }
    console.log(`✅ Added/updated menu item ${payload.id}: ${payload.name}`);
    res.status(201).json({ success: true });
  } catch (error) {
    console.error('Error adding menu item:', error.message);
    res.status(500).json({ error: error.message });
  }
});

// Delete menu item (used by Swing staff kiosk)
app.delete('/api/menu_items/:itemId', async (req, res) => {
  try {
    if (!db) {
      return res.status(503).json({ error: 'Firebase not initialized' });
    }
    const { itemId } = req.params;
    if (!itemId) {
      return res.status(400).json({ error: 'Missing itemId' });
    }

    await db.collection('menu_items').doc(itemId).delete();
    // Best-effort: also remove inventory record for the item if present
    try {
      await db.collection('inventory').doc(itemId).delete();
    } catch {}

    console.log(`✅ Deleted menu item ${itemId}`);
    res.status(204).send();
  } catch (error) {
    console.error('Error deleting menu item:', error.message);
    res.status(500).json({ error: error.message });
  }
});

// Alias for backward compatibility
app.get('/api/menu', async (req, res) => {
  res.redirect(301, '/api/menu_items');
});

// Get all inventory
app.get('/api/inventory', async (req, res) => {
  try {
    if (!db) {
      return res.status(503).json({ error: 'Firebase not initialized', items: [] });
    }
    const snapshot = await db.collection('inventory').get();
    const inventory = [];
    snapshot.forEach(doc => {
      inventory.push(serializeFirestoreValue({ id: doc.id, ...doc.data() }));
    });
    console.log(`✅ Fetched ${inventory.length} inventory items from Firestore`);
    res.json(inventory);
  } catch (error) {
    console.error('Error fetching inventory:', error.message);
    res.json([]);
  }
});

// Get all orders
app.get('/api/orders', async (req, res) => {
  try {
    if (!db) {
      return res.status(503).json({ error: 'Firebase not initialized', orders: [] });
    }
    const snapshot = await db.collection('orders').get();
    const orders = [];
    snapshot.forEach(doc => {
      orders.push(serializeFirestoreValue({ id: doc.id, ...doc.data() }));
    });
    console.log(`✅ Fetched ${orders.length} orders from Firestore`);
    res.json(orders);
  } catch (error) {
    console.error('Error fetching orders:', error.message);
    res.json([]);
  }
});

// Update order status
app.patch('/api/orders/:orderId', async (req, res) => {
  try {
    if (!db) {
      return res.status(503).json({ error: 'Firebase not initialized' });
    }
    const { orderId } = req.params;
    const { status } = req.body;
    
    await db.collection('orders').doc(orderId).update({
      status: status,
      updated_at: new Date()
    });
    
    console.log(`✅ Updated order ${orderId} to ${status}`);
    res.json({ success: true, message: `Order ${orderId} updated to ${status}` });
  } catch (error) {
    console.error('Error updating order:', error.message);
    res.status(500).json({ error: error.message });
  }
});

// Update order status (POST alternative for Java HttpURLConnection compatibility)
app.post('/api/orders/:orderId', async (req, res) => {
  try {
    if (!db) {
      return res.status(503).json({ error: 'Firebase not initialized' });
    }
    const { orderId } = req.params;
    const { status } = req.body;

    await db.collection('orders').doc(orderId).update({
      status: status,
      updated_at: new Date()
    });

    console.log(`✅ Updated order ${orderId} to ${status} (POST)`);
    res.json({ success: true, message: `Order ${orderId} updated to ${status}` });
  } catch (error) {
    console.error('Error updating order (POST):', error.message);
    res.status(500).json({ error: error.message });
  }
});

// Alternative seed endpoint - uses REST API if Admin SDK fails
app.post('/api/seed', async (req, res) => {
  try {
    const menuItems = [
      { id: "1", name: "Pizza Margherita", description: "Classic pizza", price: 8.99, category: "Main Course", available: true },
      { id: "2", name: "Pepperoni Pizza", description: "Pizza with pepperoni", price: 9.99, category: "Main Course", available: true },
      { id: "3", name: "Caesar Salad", description: "Fresh greens with dressing", price: 6.99, category: "Sides", available: true },
      { id: "4", name: "Spaghetti Carbonara", description: "Creamy pasta with bacon", price: 10.99, category: "Main Course", available: true },
      { id: "5", name: "Chocolate Cake", description: "Rich chocolate cake", price: 4.99, category: "Dessert", available: true },
      { id: "6", name: "Iced Tea", description: "Refreshing iced tea", price: 2.49, category: "Drink", available: true },
      { id: "7", name: "Soda", description: "Cold soda drink", price: 1.99, category: "Drink", available: true },
      { id: "8", name: "Chicken Sandwich", description: "Grilled chicken with lettuce", price: 7.99, category: "Main Course", available: true }
    ];

    let added = 0;
    let failed = [];

    // Try Admin SDK first
    if (db) {
      for (const item of menuItems) {
        try {
          await db.collection('menu_items').doc(item.id).set(item);
          added++;
        } catch (err) {
          console.error(`Failed with Admin SDK for ${item.id}:`, err.message);
          failed.push(item);
        }
      }
    }

    // If Admin SDK failed partially or completely, use REST API
    if (failed.length > 0 || added === 0) {
      const apiKey = "AIzaSyBhEIJfhAyWqXim6zP-22I3Y0gLlc91LV4";
      const projectId = "barkbites-22cdf";
      
      for (const item of (failed.length > 0 ? failed : menuItems)) {
        try {
          const docRef = `projects/${projectId}/databases/default/documents/menu_items/${item.id}`;
          const response = await fetch(
            `https://firestore.googleapis.com/v1/${docRef}?key=${apiKey}`,
            {
              method: 'PATCH',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({
                fields: {
                  id: { stringValue: item.id },
                  name: { stringValue: item.name },
                  description: { stringValue: item.description },
                  price: { doubleValue: item.price },
                  category: { stringValue: item.category },
                  available: { booleanValue: item.available }
                }
              })
            }
          );
          
          if (response.ok) {
            added++;
            console.log(`✅ REST API: Added ${item.name}`);
          } else {
            const errData = await response.json();
            console.error(`REST API failed for ${item.id}:`, errData.error);
          }
        } catch (err) {
          console.error(`REST API error for ${item.id}:`, err.message);
        }
      }
    }

    console.log(`✅ Seeding complete: ${added} items added`);
    res.json({ 
      success: added > 0, 
      message: `Added ${added} menu items to Firestore`,
      items_added: added 
    });

  } catch (error) {
    console.error('Error in seed:', error.message);
    res.status(500).json({ error: error.message });
  }
});

// Add new menu item
// NOTE: menu item add/delete routes are defined earlier for the Swing staff kiosk.

// Start server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`\n🐾 Bark Bites web app running on http://localhost:${PORT}`);
  console.log(`Environment: ${process.env.NODE_ENV || 'development'}`);
  if (db) {
    console.log('✅ Firestore connected - Full sync enabled');
  } else {
    console.log('⚠️ Running without Firestore - Add firebase-key.json for sync');
  }
  console.log('');
});

