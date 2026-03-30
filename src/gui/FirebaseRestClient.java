package gui;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import javax.net.ssl.HttpsURLConnection;

/**
 * Firebase REST API Client
 * Communicates with Node.js server which handles Firestore authentication
 */
public class FirebaseRestClient {
    private static final String PROJECT_ID = "barkbites-22cdf";
    private static final String API_KEY = "AIzaSyBhEIJfhAyWqXim6zP-22I3Y0gLlc91LV4";
    private static final String FIREBASE_CONFIG_PATH = "firebase-key.json";
    
    // Use local Node.js server as API gateway (authenticates with Firestore)
    // Try port 3002 first, then 3001, then 3000
    private static String SERVER_URL = "http://localhost:3002";
    
    // Fallback to direct Firestore if server unavailable
    private static final String FIREBASE_URL = 
        String.format("https://firestore.googleapis.com/v1/projects/%s/databases/default/documents", PROJECT_ID);
    
    private static boolean useLocalServer = true;
    
    static {
        checkFirebaseConfig();
    }
    
    /**
     * Check if firebase-key.json exists and validate it
     */
    private static void checkFirebaseConfig() {
        File configFile = new File(FIREBASE_CONFIG_PATH);
        if (configFile.exists()) {
            try {
                String content = new String(Files.readAllBytes(Paths.get(FIREBASE_CONFIG_PATH)));
                if (content.contains("\"project_id\"") && content.contains("barkbites-22cdf")) {
                    System.out.println("✅ Firebase Service Account Found: firebase-key.json");
                    System.out.println("   Using authenticated Firestore access for write operations");
                    return;
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error reading firebase-key.json: " + e.getMessage());
            }
        }
        System.out.println("⚠️ firebase-key.json not found - Using Demo Data Mode");
        System.out.println("   To enable full Firestore sync: Add firebase-key.json to project root");
    }
    
    /**
     * Fetch all orders from Firestore
     */
    public static List<Map<String, Object>> getOrders() {
        List<Map<String, Object>> orders = getCollection("orders");
        return orders;  // Return actual Firestore data only - no demo fallback
    }
    
    /**
     * Fetch all menu items from Firestore
     */
    public static List<Map<String, Object>> getMenuItems() {
        List<Map<String, Object>> items = getCollection("menu_items");
        return items;  // Return actual Firestore data only - no demo fallback
    }
    
    /**
     * Fetch inventory from Firestore
     */
    public static List<Map<String, Object>> getInventory() {
        return getCollection("inventory");
    }
    
    /**
     * Get a specific collection from Firestore (via local server)
     */
    private static List<Map<String, Object>> getCollection(String collectionName) {
        List<Map<String, Object>> documents = new ArrayList<>();
        
        // Try local server first
        if (useLocalServer) {
            try {
                String url = SERVER_URL + "/api/" + collectionName;
                URL obj = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    String response = readResponse(conn);
                    documents = parseServerResponse(response);
                    System.out.println("✅ Fetched " + documents.size() + " documents from server: " + collectionName);
                    return documents;
                } else if (responseCode == 503) {
                    System.err.println("⚠️ Server not initialized - Firestore not connected");
                    useLocalServer = false;  // Fall back to direct API
                } else {
                    System.err.println("⚠️ Server returned HTTP " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                System.err.println("⚠️ Local server unavailable: " + e.getMessage());
                System.err.println("   Make sure Node.js server is running: npm start");
                useLocalServer = false;  // Fall back to direct API
            }
        }
        
        // Fallback: Direct Firestore REST API (for offline/testing)
        if (!useLocalServer && documents.isEmpty()) {
            try {
                String url = FIREBASE_URL + "/" + collectionName + "?key=" + API_KEY;
                HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    String response = readResponse(conn);
                    documents = parseDocuments(response);
                    System.out.println("✅ Fetched " + documents.size() + " documents from Firestore: " + collectionName);
                } else {
                    System.err.println("❌ Firebase API Error: HTTP " + responseCode);
                    if (responseCode == 403) {
                        System.err.println("   → Firestore rules might be blocking REST API");
                        System.err.println("   → Make sure Node.js server is running for authenticated access");
                    }
                }
                conn.disconnect();
            } catch (Exception e) {
                System.err.println("⚠️ Error fetching from Firestore: " + e.getMessage());
            }
        }
        
        return documents;
    }
    
    /**
     * Parse server JSON response into documents
     */
    private static List<Map<String, Object>> parseServerResponse(String jsonResponse) {
        List<Map<String, Object>> documents = new ArrayList<>();
        
        try {
            // Server returns simple JSON array: [{"id":"123", "name":"Item", ...}, ...]
            if (!jsonResponse.startsWith("[")) {
                return documents;
            }
            
            // Remove outer brackets
            String arrayBody = jsonResponse.substring(1, jsonResponse.length() - 1);
            if (arrayBody.isEmpty()) return documents;
            
            // Split by document objects
            int braceCount = 0;
            StringBuilder currentDoc = new StringBuilder();
            
            for (char c : arrayBody.toCharArray()) {
                if (c == '{') braceCount++;
                if (c == '}') braceCount--;
                
                currentDoc.append(c);
                
                if (braceCount == 0 && currentDoc.length() > 1 && c == '}') {
                    String doc = currentDoc.toString();
                    if (doc.contains("\"id\"")) {
                        Map<String, Object> parsed = parseJsonObject(doc);
                        if (!parsed.isEmpty()) {
                            documents.add(parsed);
                        }
                    }
                    currentDoc = new StringBuilder();
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error parsing server response: " + e.getMessage());
        }
        
        return documents;
    }
    
    /**
     * Parse JSON object string into map
     */
    private static Map<String, Object> parseJsonObject(String jsonStr) {
        Map<String, Object> map = new HashMap<>();
        
        try {
            // Simple JSON parsing - handles basic types
            String[] pairs = jsonStr.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            
            for (String pair : pairs) {
                pair = pair.trim().replaceAll("[{}\\[\\]]", "");
                if (!pair.contains(":")) continue;
                
                int colonIdx = pair.indexOf(":");
                String key = pair.substring(0, colonIdx).trim().replaceAll("\"", "");
                String value = pair.substring(colonIdx + 1).trim().replaceAll("\"", "");
                
                if (!key.isEmpty() && !value.isEmpty()) {
                    // Try to parse as number
                    try {
                        if (value.contains(".")) {
                            map.put(key, Double.parseDouble(value));
                        } else {
                            map.put(key, Integer.parseInt(value));
                        }
                    } catch (NumberFormatException e) {
                        // Keep as string
                        map.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error parsing JSON: " + e.getMessage());
        }
        
        return map;
    }
    
    /**
     * Update an order status in Firestore
     */
    public static boolean updateOrderStatus(String orderId, String newStatus) {
        // Try local server first
        if (useLocalServer) {
            try {
                String url = SERVER_URL + "/api/orders/" + orderId;
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                
                String updateJson = String.format("{\"status\":\"%s\"}", newStatus);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(updateJson.getBytes());
                    os.flush();
                }
                
                int responseCode = conn.getResponseCode();
                conn.disconnect();
                
                if (responseCode == 200 || responseCode == 201) {
                    System.out.println("✅ Updated " + orderId + " to " + newStatus);
                    return true;
                } else {
                    System.err.println("⚠️ Server update failed: HTTP " + responseCode);
                    useLocalServer = false;
                }
            } catch (Exception e) {
                System.err.println("⚠️ Server unavailable: " + e.getMessage());
                useLocalServer = false;
            }
        }
        
        // Fallback: Direct Firestore REST
        return updateOrderStatusDirect(orderId, newStatus);
    }
    
    /**
     * Add a new menu item to Firestore
     */
    public static boolean addMenuItem(String id, String name, double price, String category, String description) {
        // Try local server first
        if (useLocalServer) {
            try {
                String url = SERVER_URL + "/api/menu_items/add";
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                
                String addJson = String.format(
                    "{\"id\":\"%s\",\"name\":\"%s\",\"price\":%f,\"category\":\"%s\",\"description\":\"%s\",\"available\":true}",
                    id, name, price, category, description
                );
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(addJson.getBytes());
                    os.flush();
                }
                
                int responseCode = conn.getResponseCode();
                conn.disconnect();
                
                if (responseCode == 200 || responseCode == 201) {
                    System.out.println("✅ Added menu item: " + name);
                    return true;
                } else {
                    System.err.println("⚠️ Failed to add menu item: HTTP " + responseCode);
                    return false;
                }
            } catch (Exception e) {
                System.err.println("❌ Error adding menu item: " + e.getMessage());
                return false;
            }
        }
        return false;
    }
    
    private static boolean updateOrderStatusDirect(String orderId, String newStatus) {
        try {
            String url = FIREBASE_URL + "/orders/" + orderId + "?key=" + API_KEY;
            HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("PATCH");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            // Build update JSON
            String updateJson = String.format(
                "{\"fields\":{\"status\":{\"stringValue\":\"%s\"},\"updated_at\":{\"timestampValue\":\"%s\"}}}",
                newStatus, new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                    .format(new java.util.Date())
            );
            
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(updateJson.getBytes());
                os.flush();
            }
            
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            
            if (responseCode == 200) {
                System.out.println("✅ Updated orders/" + orderId);
                return true;
            } else {
                System.err.println("❌ Update failed: HTTP " + responseCode);
                return false;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error updating document: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Read response from connection
     */
    private static String readResponse(HttpURLConnection conn) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }
    
    /**
     * Read response from HTTPS connection
     */
    private static String readResponse(HttpsURLConnection conn) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }
    
    /**
     * Parse Firestore JSON response into documents
     */
    private static List<Map<String, Object>> parseDocuments(String jsonResponse) {
        List<Map<String, Object>> documents = new ArrayList<>();
        
        try {
            // Simple JSON parsing - in production use JSON library
            if (!jsonResponse.contains("\"documents\"")) {
                return documents;
            }
            
            int startIdx = jsonResponse.indexOf("[");
            int endIdx = jsonResponse.lastIndexOf("]");
            if (startIdx == -1 || endIdx == -1) {
                return documents;
            }
            
            String docsArray = jsonResponse.substring(startIdx, endIdx + 1);
            
            // Split by document objects
            int braceCount = 0;
            StringBuilder currentDoc = new StringBuilder();
            
            for (char c : docsArray.toCharArray()) {
                if (c == '{') braceCount++;
                if (c == '}') braceCount--;
                
                currentDoc.append(c);
                
                if (braceCount == 0 && currentDoc.length() > 1 && c == '}') {
                    String doc = currentDoc.toString();
                    if (doc.contains("\"fields\"")) {
                        Map<String, Object> parsed = parseDocument(doc);
                        if (!parsed.isEmpty()) {
                            documents.add(parsed);
                        }
                    }
                    currentDoc = new StringBuilder();
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error parsing documents: " + e.getMessage());
        }
        
        return documents;
    }
    
    /**
     * Parse a single Firestore document
     */
    private static Map<String, Object> parseDocument(String docJson) {
        Map<String, Object> doc = new HashMap<>();
        
        try {
            // Extract document ID
            int nameIdx = docJson.indexOf("\"name\"");
            if (nameIdx != -1) {
                int idStart = docJson.indexOf("/", nameIdx) + 1;
                int idEnd = docJson.indexOf("\"", idStart);
                if (idStart > 0 && idEnd > idStart) {
                    String docId = docJson.substring(idStart, idEnd);
                    doc.put("id", docId);
                }
            }
            
            // Extract fields
            int fieldsIdx = docJson.indexOf("\"fields\"");
            if (fieldsIdx != -1) {
                int start = docJson.indexOf("{", fieldsIdx);
                int end = docJson.lastIndexOf("}");
                String fieldsJson = docJson.substring(start, end + 1);
                
                // Parse key-value pairs
                String[] pairs = fieldsJson.split("\"");
                for (int i = 0; i < pairs.length - 2; i += 2) {
                    String key = pairs[i];
                    if (!key.isEmpty() && !key.equals("{") && !key.contains(":")) {
                        // Try to extract value
                        String rawValue = extractFieldValue(fieldsJson, key);
                        doc.put(key.trim(), rawValue != null ? rawValue : "");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error parsing single document: " + e.getMessage());
        }
        
        return doc;
    }
    
    /**
     * Extract field value from Firestore JSON
     */
    private static String extractFieldValue(String json, String fieldName) {
        try {
            String pattern = "\"" + fieldName + "\":{";
            int idx = json.indexOf(pattern);
            if (idx == -1) return null;
            
            int start = json.indexOf(":", idx) + 1;
            int depth = 0;
            int end = start;
            
            for (int i = start; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c == '{' || c == '[') depth++;
                if (c == '}' || c == ']') depth--;
                
                if (depth == 0) {
                    end = i;
                    break;
                }
            }
            
            String value = json.substring(start, end).trim();
            
            // Extract actual value from Firestore type wrapper
            if (value.contains("stringValue")) {
                int strStart = value.indexOf("\"", value.indexOf("stringValue")) + 1;
                int strEnd = value.indexOf("\"", strStart);
                return value.substring(strStart, strEnd);
            } else if (value.contains("integerValue")) {
                int intStart = value.indexOf(":") + 1;
                int intEnd = value.indexOf("}", intStart);
                return value.substring(intStart, intEnd).trim();
            } else if (value.contains("doubleValue")) {
                int doubleStart = value.indexOf(":") + 1;
                int doubleEnd = value.indexOf("}", doubleStart);
                return value.substring(doubleStart, doubleEnd).trim();
            }
            
            return value;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Demo data removed - showing only real Firestore data
     */
    private static List<Map<String, Object>> getDemoOrders() {
        System.out.println("❌ No orders in Firestore - Sync not working");
        return new ArrayList<>();
    }
    
    /**
     * Demo data removed - showing only real Firestore data
     */
    private static List<Map<String, Object>> getDemoMenuItems() {
        System.out.println("❌ No menu items in Firestore - Sync not working");
        return new ArrayList<>();
    }
}
