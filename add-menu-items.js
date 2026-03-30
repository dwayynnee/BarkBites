/**
 * Add test data to Firestore via REST API
 * This script uses the local Node.js server which already authenticates with Firestore
 */

const http = require('http');

// Test menu items
const menuItems = [
  {
    id: "1",
    name: "Pizza Margherita",
    description: "Classic pizza with tomato, mozzarella, and basil",
    price: 8.99,
    category: "Main Course",
    available: true
  },
  {
    id: "2",
    name: "Pepperoni Pizza",
    description: "Pizza with pepperoni slices and cheese",
    price: 9.99,
    category: "Main Course",
    available: true
  },
  {
    id: "3",
    name: "Caesar Salad",
    description: "Fresh greens with Caesar dressing and croutons",
    price: 6.99,
    category: "Sides",
    available: true
  },
  {
    id: "4",
    name: "Spaghetti Carbonara",
    description: "Creamy pasta with bacon and parmesan",
    price: 10.99,
    category: "Main Course",
    available: true
  },
  {
    id: "5",
    name: "Chocolate Cake",
    description: "Rich chocolate cake with frosting",
    price: 4.99,
    category: "Dessert",
    available: true
  },
  {
    id: "6",
    name: "Iced Tea",
    description: "Refreshing iced tea",
    price: 2.49,
    category: "Drink",
    available: true
  },
  {
    id: "7",
    name: "Soda",
    description: "Cold soda drink",
    price: 1.99,
    category: "Drink",
    available: true
  },
  {
    id: "8",
    name: "Chicken Sandwich",
    description: "Grilled chicken breast with lettuce and tomato",
    price: 7.99,
    category: "Main Course",
    available: true
  }
];

const admin = require('firebase-admin');
const fs = require('fs');

try {
  const serviceAccount = require('./firebase-key.json');
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
} catch (error) {
  console.error('❌ firebase-key.json not found');
  console.error('   Please ensure firebase-key.json is in the project root');
  process.exit(1);
}

const db = admin.firestore();

async function addMenuItems() {
  try {
    console.log('📝 Adding menu items to Firestore...\n');

    for (const item of menuItems) {
      try {
        await db.collection('menu_items').doc(item.id).set(item);
        console.log(`✅ Added: ${item.name} ($${item.price})`);
      } catch (err) {
        console.error(`❌ Failed to add ${item.name}:`, err.message);
      }
    }

    console.log('\n🎉 All menu items added successfully!\n');
    console.log('📱 Now you can:');
    console.log('   1. Refresh browser: http://localhost:3002');
    console.log('   2. Login with Student ID (e.g., "student123")');
    console.log('   3. See 8 menu items in your app\n');

    // Keep the process alive for a moment, then exit
    setTimeout(() => {
      process.exit(0);
    }, 1000);

  } catch (error) {
    console.error('❌ Error:', error.message);
    process.exit(1);
  }
}

addMenuItems();
