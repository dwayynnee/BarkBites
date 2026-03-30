require('dotenv').config();
const admin = require('firebase-admin');
const fs = require('fs');

// Initialize Firebase Admin SDK
try {
  const serviceAccount = require('./firebase-key.json');
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
} catch (error) {
  console.error('❌ Error: firebase-key.json not found');
  process.exit(1);
}

const db = admin.firestore();

// Test menu items
const menuItems = [
  {
    id: "1",
    name: "Pizza Margherita",
    description: "Classic pizza with tomato, mozzarella, and basil",
    price: 8.99,
    category: "Main Course",
    available: true,
    image_url: "https://via.placeholder.com/200?text=Pizza",
    created_at: new Date(),
    updated_at: new Date()
  },
  {
    id: "2",
    name: "Pepperoni Pizza",
    description: "Pizza with pepperoni slices and cheese",
    price: 9.99,
    category: "Main Course",
    available: true,
    image_url: "https://via.placeholder.com/200?text=Pepperoni",
    created_at: new Date(),
    updated_at: new Date()
  },
  {
    id: "3",
    name: "Caesar Salad",
    description: "Fresh greens with Caesar dressing and croutons",
    price: 6.99,
    category: "Sides",
    available: true,
    image_url: "https://via.placeholder.com/200?text=Salad",
    created_at: new Date(),
    updated_at: new Date()
  },
  {
    id: "4",
    name: "Spaghetti Carbonara",
    description: "Creamy pasta with bacon and parmesan",
    price: 10.99,
    category: "Main Course",
    available: true,
    image_url: "https://via.placeholder.com/200?text=Spaghetti",
    created_at: new Date(),
    updated_at: new Date()
  },
  {
    id: "5",
    name: "Chocolate Cake",
    description: "Rich chocolate cake with frosting",
    price: 4.99,
    category: "Dessert",
    available: true,
    image_url: "https://via.placeholder.com/200?text=Cake",
    created_at: new Date(),
    updated_at: new Date()
  },
  {
    id: "6",
    name: "Iced Tea",
    description: "Refreshing iced tea",
    price: 2.49,
    category: "Drink",
    available: true,
    image_url: "https://via.placeholder.com/200?text=Tea",
    created_at: new Date(),
    updated_at: new Date()
  },
  {
    id: "7",
    name: "Soda",
    description: "Cold soda drink",
    price: 1.99,
    category: "Drink",
    available: true,
    image_url: "https://via.placeholder.com/200?text=Soda",
    created_at: new Date(),
    updated_at: new Date()
  },
  {
    id: "8",
    name: "Chicken Sandwich",
    description: "Grilled chicken breast with lettuce and tomato",
    price: 7.99,
    category: "Main Course",
    available: true,
    image_url: "https://via.placeholder.com/200?text=Sandwich",
    created_at: new Date(),
    updated_at: new Date()
  }
];

// Test inventory
const inventory = [
  { menu_item_id: "1", quantity_available: 50, quantity_sold_today: 5, low_stock_threshold: 10, is_out_of_stock: false, last_updated: new Date() },
  { menu_item_id: "2", quantity_available: 45, quantity_sold_today: 8, low_stock_threshold: 10, is_out_of_stock: false, last_updated: new Date() },
  { menu_item_id: "3", quantity_available: 30, quantity_sold_today: 3, low_stock_threshold: 5, is_out_of_stock: false, last_updated: new Date() },
  { menu_item_id: "4", quantity_available: 25, quantity_sold_today: 2, low_stock_threshold: 10, is_out_of_stock: false, last_updated: new Date() },
  { menu_item_id: "5", quantity_available: 8, quantity_sold_today: 12, low_stock_threshold: 10, is_out_of_stock: false, last_updated: new Date() },
  { menu_item_id: "6", quantity_available: 100, quantity_sold_today: 5, low_stock_threshold: 20, is_out_of_stock: false, last_updated: new Date() },
  { menu_item_id: "7", quantity_available: 80, quantity_sold_today: 10, low_stock_threshold: 20, is_out_of_stock: false, last_updated: new Date() },
  { menu_item_id: "8", quantity_available: 35, quantity_sold_today: 4, low_stock_threshold: 10, is_out_of_stock: false, last_updated: new Date() }
];

async function setupTestData() {
  try {
    console.log('🚀 Adding test menu items to Firestore...\n');
    
    // Add menu items
    for (const item of menuItems) {
      await db.collection('menu_items').doc(item.id).set(item);
      console.log(`✅ Added: ${item.name}`);
    }
    
    console.log('\n🚀 Adding test inventory data to Firestore...\n');
    
    // Add inventory
    for (const inv of inventory) {
      await db.collection('inventory').doc(inv.menu_item_id).set(inv);
      console.log(`✅ Set inventory for item ${inv.menu_item_id}`);
    }
    
    console.log('\n✅ Test data setup complete!');
    console.log('\n📝 You can now:');
    console.log('   1. Refresh your browser at http://localhost:3002');
    console.log('   2. Login with any Student ID (e.g., "student123")');
    console.log('   3. See the menu items and navigate the app');
    console.log('   4. Check the Java app - it should show inventory data\n');
    
    process.exit(0);
  } catch (error) {
    console.error('❌ Error adding test data:', error);
    process.exit(1);
  }
}

setupTestData();
