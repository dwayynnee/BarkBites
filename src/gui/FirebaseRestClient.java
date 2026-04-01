package gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

/**
 * Firebase REST API Client
 * Communicates with Node.js server which handles Firestore authentication
 */
public class FirebaseRestClient {
    private static final String PROJECT_ID = "barkbites-student"; // MUST match web firebaseConfig.projectId
    private static final String API_KEY = "AIzaSyAX_y7YlUKixzQOQD0E66pbbYOUx6s8gKE"; // MUST match web firebaseConfig.apiKey (for direct REST fallback)
    private static final String FIREBASE_CONFIG_PATH = "firebase-key.json";
    
    // Use local Node.js server as API gateway (authenticates with Firestore)
    // Default server.js runs on port 3000
    private static final String SERVER_URL = "http://localhost:3000";
    
    // Fallback to direct Firestore if server unavailable
    // Note: database path must be (default), not default
    private static final String FIREBASE_URL = 
        String.format("https://firestore.googleapis.com/v1/projects/%s/databases/(default)/documents", PROJECT_ID);
    
    private static final boolean USE_LOCAL_SERVER = true;
    
    static {
        checkFirebaseConfig();
    }
    
    /**
     * Check if firebase-key.json exists and validate it
     */
    private static void checkFirebaseConfig() {
        // Try a few common locations so it works whether you run from project root or bin/
        String[] candidatePaths = new String[] {
            FIREBASE_CONFIG_PATH,
            ".." + File.separator + FIREBASE_CONFIG_PATH,
            ".." + File.separator + ".." + File.separator + FIREBASE_CONFIG_PATH
        };

        for (String path : candidatePaths) {
            File configFile = new File(path);
            if (configFile.exists()) {
                try {
                    String content = new String(Files.readAllBytes(Paths.get(path)));
                    if (content.contains("\"project_id\"") && content.contains(PROJECT_ID)) {
                        System.out.println("✅ Firebase Service Account Found: " + configFile.getAbsolutePath());
                        System.out.println("   Using authenticated Firestore access for write operations");
                        return;
                    }
                } catch (IOException | RuntimeException e) {
                    System.err.println("⚠️ Error reading firebase-key.json at " + path + ": " + e.getMessage());
                }
            }
        }

        System.out.println("⚠️ firebase-key.json not found - Using Demo Data Mode");
        System.out.println("   To enable full Firestore sync: Add firebase-key.json to project root");
        System.out.println("   Current working directory: " + new File(".").getAbsolutePath());
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

        // Try local server first (preferred, authenticated)
        if (USE_LOCAL_SERVER) {
            try {
                String url = SERVER_URL + "/api/" + collectionName;
                URL obj = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                switch (responseCode) {
                    case 200 -> {
                        String response = readResponse(conn);
                        documents = parseServerResponse(response);
                        System.out.println("✅ Fetched " + documents.size() + " documents from server: " + collectionName);
                        return documents;
                    }
                    case 503 -> System.err.println("⚠️ Server not initialized - Firestore not connected");
                    default -> System.err.println("⚠️ Server returned HTTP " + responseCode);
                }
                conn.disconnect();
            } catch (IOException | RuntimeException e) {
                System.err.println("⚠️ Local server unavailable: " + e.getMessage());
                System.err.println("   Make sure Node.js server is running: npm start");
            }
        }

        // Fallback: Direct Firestore REST API (for offline/testing)
        // Only attempt if server was unavailable/unhealthy.
        if (documents.isEmpty()) {
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
            } catch (IOException | RuntimeException e) {
                System.err.println("⚠️ Error fetching from Firestore: " + e.getMessage());
            }
        }
        
        return documents;
    }
    
    /**
     * Parse server JSON response into documents
     */
    private static List<Map<String, Object>> parseServerResponse(String jsonResponse) {
        try {
            Object parsed = Json.parse(Objects.requireNonNullElse(jsonResponse, ""));
            if (!(parsed instanceof List<?> list)) {
                return new ArrayList<>();
            }

            List<Map<String, Object>> out = new ArrayList<>();
            for (Object el : list) {
                if (el instanceof Map<?, ?> map) {
                    out.add(toStringObjectMap(map));
                }
            }
            return out;
        } catch (RuntimeException e) {
            System.err.println("⚠️ Error parsing server response JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Update an order status in Firestore
     */
    public static boolean updateOrderStatus(String orderId, String newStatus) {
        // Try local server first
        if (USE_LOCAL_SERVER) {
            try {
                // Use POST instead of PATCH because some Java runtimes reject PATCH with HttpURLConnection
                String url = SERVER_URL + "/api/orders/" + orderId;
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("POST");
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
                }
            } catch (IOException | RuntimeException e) {
                System.err.println("⚠️ Server unavailable: " + e.getMessage());
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
        if (USE_LOCAL_SERVER) {
            try {
                String url = SERVER_URL + "/api/menu_items/add";
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                
                String addJson = String.format(
                    "{\"id\":\"%s\",\"name\":\"%s\",\"price\":%s,\"category\":\"%s\",\"description\":\"%s\",\"available\":true}",
                    escapeJson(id),
                    escapeJson(name),
                    Double.toString(price),
                    escapeJson(category),
                    escapeJson(description)
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
                }
            } catch (IOException | RuntimeException e) {
                System.err.println("❌ Error adding menu item: " + e.getMessage());
            }
        }

        // Fallback: Direct Firestore REST (may be blocked by Firestore rules)
        return addMenuItemDirect(id, name, price, category, description);
    }

    /**
     * Delete a menu item from Firestore
     */
    public static boolean deleteMenuItem(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }

        String trimmedId = id.trim();

        // Try local server first
        if (USE_LOCAL_SERVER) {
            try {
                String encodedId = URLEncoder.encode(trimmedId, "UTF-8");
                String url = SERVER_URL + "/api/menu_items/" + encodedId;
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("DELETE");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                conn.disconnect();

                if (responseCode == 200 || responseCode == 204) {
                    System.out.println("✅ Deleted menu item: " + trimmedId);
                    return true;
                }
                System.err.println("⚠️ Failed to delete menu item: HTTP " + responseCode);
            } catch (IOException | RuntimeException e) {
                System.err.println("❌ Error deleting menu item: " + e.getMessage());
            }
        }

        // Fallback: Direct Firestore REST (may be blocked by rules)
        return deleteMenuItemDirect(trimmedId);
    }
    
    private static boolean updateOrderStatusDirect(String orderId, String newStatus) {
        // Use Firestore Commit API (POST) to avoid PATCH, which some Java runtimes reject.
        try {
            String url = String.format(
                "https://firestore.googleapis.com/v1/projects/%s/databases/(default)/documents:commit?key=%s",
                PROJECT_ID,
                API_KEY
            );
            HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .format(new java.util.Date());

            String docName = String.format(
                "projects/%s/databases/(default)/documents/orders/%s",
                PROJECT_ID,
                escapeJson(orderId)
            );

            String updateJson = String.format(
                "{\"writes\":[{\"update\":{\"name\":\"%s\",\"fields\":{\"status\":{\"stringValue\":\"%s\"},\"updated_at\":{\"timestampValue\":\"%s\"}}},\"updateMask\":{\"fieldPaths\":[\"status\",\"updated_at\"]}}]}",
                docName,
                escapeJson(newStatus),
                timestamp
            );

            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(updateJson.getBytes());
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            conn.disconnect();

            if (responseCode == 200) {
                System.out.println("✅ Updated orders/" + orderId + " (commit)");
                return true;
            }

            System.err.println("❌ Commit update failed: HTTP " + responseCode);
            return false;
        } catch (IOException | RuntimeException e) {
            System.err.println("⚠️ Error updating document (commit): " + e.getMessage());
            return false;
        }
    }

    private static boolean addMenuItemDirect(String id, String name, double price, String category, String description) {
        // Use Firestore Commit API (POST) to avoid PATCH, which some Java runtimes reject.
        try {
            String url = String.format(
                "https://firestore.googleapis.com/v1/projects/%s/databases/(default)/documents:commit?key=%s",
                PROJECT_ID,
                API_KEY
            );
            HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .format(new java.util.Date());

            String docName = String.format(
                "projects/%s/databases/(default)/documents/menu_items/%s",
                PROJECT_ID,
                escapeJson(id)
            );

            String addJson = String.format(
                "{\"writes\":[{\"update\":{\"name\":\"%s\",\"fields\":{" +
                    "\"id\":{\"stringValue\":\"%s\"}," +
                    "\"name\":{\"stringValue\":\"%s\"}," +
                    "\"description\":{\"stringValue\":\"%s\"}," +
                    "\"price\":{\"doubleValue\":%s}," +
                    "\"category\":{\"stringValue\":\"%s\"}," +
                    "\"available\":{\"booleanValue\":true}," +
                    "\"updated_at\":{\"timestampValue\":\"%s\"}" +
                "}},\"updateMask\":{\"fieldPaths\":[\"id\",\"name\",\"description\",\"price\",\"category\",\"available\",\"updated_at\"]}}]}",
                docName,
                escapeJson(id),
                escapeJson(name),
                escapeJson(description),
                Double.toString(price),
                escapeJson(category),
                timestamp
            );

            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(addJson.getBytes());
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            conn.disconnect();

            if (responseCode == 200) {
                System.out.println("✅ Added menu_items/" + id + " (commit)");
                return true;
            }
            System.err.println("❌ Commit add failed: HTTP " + responseCode);
            return false;
        } catch (IOException | RuntimeException e) {
            System.err.println("⚠️ Error adding menu item (commit): " + e.getMessage());
            return false;
        }
    }

    private static boolean deleteMenuItemDirect(String id) {
        try {
            String url = FIREBASE_URL + "/menu_items/" + URLEncoder.encode(id, "UTF-8") + "?key=" + API_KEY;
            HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("DELETE");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            conn.disconnect();

            if (responseCode == 200) {
                System.out.println("✅ Deleted menu_items/" + id + " (direct REST)");
                return true;
            }
            System.err.println("❌ Direct delete failed: HTTP " + responseCode);
            return false;
        } catch (IOException | RuntimeException e) {
            System.err.println("⚠️ Error deleting menu item (direct REST): " + e.getMessage());
            return false;
        }
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
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
        try {
            Object parsed = Json.parse(Objects.requireNonNullElse(jsonResponse, ""));
            if (!(parsed instanceof Map<?, ?> root)) {
                return new ArrayList<>();
            }

            Object docsObj = root.get("documents");
            if (!(docsObj instanceof List<?> docs)) {
                return new ArrayList<>();
            }

            List<Map<String, Object>> out = new ArrayList<>();
            for (Object docEl : docs) {
                if (!(docEl instanceof Map<?, ?> docMap)) continue;
                Map<String, Object> doc = parseFirestoreDocument(docMap);
                if (!doc.isEmpty()) out.add(doc);
            }

            return out;
        } catch (RuntimeException e) {
            System.err.println("⚠️ Error parsing Firestore REST documents JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    private static Map<String, Object> parseFirestoreDocument(Map<?, ?> docObj) {
        Map<String, Object> doc = new HashMap<>();

        try {
            Object nameObj = docObj.get("name");
            if (nameObj != null) {
                String name = String.valueOf(nameObj);
                int lastSlash = name.lastIndexOf('/');
                if (lastSlash >= 0 && lastSlash + 1 < name.length()) {
                    doc.put("id", name.substring(lastSlash + 1));
                }
            }

            Object fieldsObj = docObj.get("fields");
            if (fieldsObj instanceof Map<?, ?> fields) {
                for (Map.Entry<?, ?> entry : fields.entrySet()) {
                    String key = String.valueOf(entry.getKey());
                    Object value = decodeFirestoreValue(entry.getValue());
                    doc.put(key, value);
                }
            }
        } catch (RuntimeException e) {
            System.err.println("⚠️ Error parsing Firestore document: " + e.getMessage());
        }

        return doc;
    }

    private static Object decodeFirestoreValue(Object wrapper) {
        if (!(wrapper instanceof Map<?, ?> valueObj)) {
            return wrapper;
        }

        if (valueObj.containsKey("nullValue")) return null;
        if (valueObj.containsKey("stringValue")) return String.valueOf(valueObj.get("stringValue"));
        if (valueObj.containsKey("booleanValue")) return Boolean.valueOf(String.valueOf(valueObj.get("booleanValue")));

        if (valueObj.containsKey("integerValue")) {
            String s = String.valueOf(valueObj.get("integerValue"));
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                return s;
            }
        }

        if (valueObj.containsKey("doubleValue")) {
            Object v = valueObj.get("doubleValue");
            if (v instanceof Number n) return n.doubleValue();
            try {
                return Double.parseDouble(String.valueOf(v));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }

        if (valueObj.containsKey("timestampValue")) return String.valueOf(valueObj.get("timestampValue"));

        if (valueObj.containsKey("mapValue")) {
            Object mv = valueObj.get("mapValue");
            if (mv instanceof Map<?, ?> mapValue) {
                Object fieldsObj = mapValue.get("fields");
                if (fieldsObj instanceof Map<?, ?> fields) {
                    Map<String, Object> out = new HashMap<>();
                    for (Map.Entry<?, ?> entry : fields.entrySet()) {
                        out.put(String.valueOf(entry.getKey()), decodeFirestoreValue(entry.getValue()));
                    }
                    return out;
                }
            }
            return new HashMap<String, Object>();
        }

        if (valueObj.containsKey("arrayValue")) {
            Object av = valueObj.get("arrayValue");
            if (av instanceof Map<?, ?> arrayValue) {
                Object valuesObj = arrayValue.get("values");
                if (valuesObj instanceof List<?> values) {
                    List<Object> out = new ArrayList<>();
                    for (Object v : values) {
                        out.add(decodeFirestoreValue(v));
                    }
                    return out;
                }
            }
            return new ArrayList<Object>();
        }

        // Unknown wrapper type; return as generic map
        return toStringObjectMap(valueObj);
    }

    private static Map<String, Object> toStringObjectMap(Map<?, ?> map) {
        Map<String, Object> out = new HashMap<>();
        for (Map.Entry<?, ?> e : map.entrySet()) {
            out.put(String.valueOf(e.getKey()), e.getValue());
        }
        return out;
    }

    /**
     * Minimal JSON parser (objects, arrays, strings, numbers, booleans, null).
     * Returns nested Maps/Lists using Java types.
     */
    private static final class Json {
        static Object parse(String input) {
            Parser p = new Parser(input == null ? "" : input);
            Object v = p.parseValue();
            p.skipWhitespace();
            if (!p.isEof()) {
                throw new IllegalArgumentException("Trailing data at position " + p.pos);
            }
            return v;
        }

        private static final class Parser {
            private final String s;
            private int pos;

            Parser(String s) {
                this.s = s;
            }

            boolean isEof() {
                return pos >= s.length();
            }

            void skipWhitespace() {
                while (!isEof()) {
                    char c = s.charAt(pos);
                    if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                        pos++;
                    } else {
                        break;
                    }
                }
            }

            Object parseValue() {
                skipWhitespace();
                if (isEof()) throw new IllegalArgumentException("Empty JSON");
                char c = s.charAt(pos);
                return switch (c) {
                    case '{' -> parseObject();
                    case '[' -> parseArray();
                    case '"' -> parseString();
                    case 't' -> parseLiteral("true", Boolean.TRUE);
                    case 'f' -> parseLiteral("false", Boolean.FALSE);
                    case 'n' -> parseLiteral("null", null);
                    default -> {
                        if (c == '-' || (c >= '0' && c <= '9')) {
                            yield parseNumber();
                        }
                        throw new IllegalArgumentException("Unexpected character '" + c + "' at position " + pos);
                    }
                };
            }

            Object parseLiteral(String literal, Object value) {
                if (s.regionMatches(pos, literal, 0, literal.length())) {
                    pos += literal.length();
                    return value;
                }
                throw new IllegalArgumentException("Invalid literal at position " + pos);
            }

            Map<String, Object> parseObject() {
                expect('{');
                skipWhitespace();
                Map<String, Object> obj = new HashMap<>();
                if (peek('}')) {
                    pos++;
                    return obj;
                }
                while (true) {
                    skipWhitespace();
                    String key = parseString();
                    skipWhitespace();
                    expect(':');
                    Object value = parseValue();
                    obj.put(key, value);
                    skipWhitespace();
                    if (peek(',')) {
                        pos++;
                        continue;
                    }
                    expect('}');
                    break;
                }
                return obj;
            }

            List<Object> parseArray() {
                expect('[');
                skipWhitespace();
                List<Object> arr = new ArrayList<>();
                if (peek(']')) {
                    pos++;
                    return arr;
                }
                while (true) {
                    Object value = parseValue();
                    arr.add(value);
                    skipWhitespace();
                    if (peek(',')) {
                        pos++;
                        continue;
                    }
                    expect(']');
                    break;
                }
                return arr;
            }

            String parseString() {
                expect('"');
                StringBuilder sb = new StringBuilder();
                while (!isEof()) {
                    char c = s.charAt(pos++);
                    if (c == '"') {
                        return sb.toString();
                    }
                    if (c == '\\') {
                        if (isEof()) throw new IllegalArgumentException("Unterminated escape");
                        char e = s.charAt(pos++);
                        switch (e) {
                            case '"' -> sb.append('"');
                            case '\\' -> sb.append('\\');
                            case '/' -> sb.append('/');
                            case 'b' -> sb.append('\b');
                            case 'f' -> sb.append('\f');
                            case 'n' -> sb.append('\n');
                            case 'r' -> sb.append('\r');
                            case 't' -> sb.append('\t');
                            case 'u' -> {
                                if (pos + 4 > s.length()) throw new IllegalArgumentException("Invalid unicode escape");
                                String hex = s.substring(pos, pos + 4);
                                pos += 4;
                                sb.append((char) Integer.parseInt(hex, 16));
                            }
                            default -> throw new IllegalArgumentException("Invalid escape: \\" + e);
                        }
                    } else {
                        sb.append(c);
                    }
                }
                throw new IllegalArgumentException("Unterminated string");
            }

            Object parseNumber() {
                int start = pos;
                if (peek('-')) pos++;
                while (!isEof() && Character.isDigit(s.charAt(pos))) pos++;
                boolean isFloat = false;
                if (!isEof() && s.charAt(pos) == '.') {
                    isFloat = true;
                    pos++;
                    while (!isEof() && Character.isDigit(s.charAt(pos))) pos++;
                }
                if (!isEof()) {
                    char c = s.charAt(pos);
                    if (c == 'e' || c == 'E') {
                        isFloat = true;
                        pos++;
                        if (!isEof() && (s.charAt(pos) == '+' || s.charAt(pos) == '-')) pos++;
                        while (!isEof() && Character.isDigit(s.charAt(pos))) pos++;
                    }
                }
                String num = s.substring(start, pos);
                try {
                    if (isFloat) return Double.parseDouble(num);
                    return Long.parseLong(num);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }

            void expect(char expected) {
                if (isEof() || s.charAt(pos) != expected) {
                    throw new IllegalArgumentException("Expected '" + expected + "' at position " + pos);
                }
                pos++;
            }

            boolean peek(char c) {
                return !isEof() && s.charAt(pos) == c;
            }
        }
    }
}
