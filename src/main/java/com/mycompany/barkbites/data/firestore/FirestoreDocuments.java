package com.mycompany.barkbites.data.firestore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.firestore.DocumentSnapshot;

/**
 * Utility class for building and parsing Firestore documents.
 * 
 * This class provides helper methods to:
 * 1. Build Firestore REST document structures (ObjectNode format)
 * 2. Extract typed values from Firestore JSON documents with fallback defaults
 * 3. Handle type conversions (long → int, string → number, etc.)
 * 
 * Firestore documents in REST format wrap fields in a "fields" object, where
 * each field value is an object with type descriptors like:
 * - {"stringValue": "hello"}
 * - {"integerValue": "42"}
 * - {"doubleValue": "3.14"}
 * - {"booleanValue": true}
 * 
 * This class abstracts away these type descriptors for cleaner code.
 */
public final class FirestoreDocuments {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Private constructor: utility class with only static methods
    private FirestoreDocuments() {
    }

    /**
     * Builds a customer document for Firestore storage.
     * 
     * Creates a Firestore document with customer info: studentId, name, mobile, and wallet balance.
     * Used when registering new customers or updating customer records.
     * 
     * @param studentId the student identifier
     * @param name the customer's full name
     * @param mobile the customer's phone number
     * @param walletBalanceCents the wallet balance in cents (e.g., 1000 = $10.00)
     * @return an ObjectNode ready to be sent to Firestore
     */
    public static ObjectNode customerDocument(String studentId, String name, String mobile, long walletBalanceCents) {
        // Create root document structure and fields object
        ObjectNode root = MAPPER.createObjectNode();
        ObjectNode fields = root.putObject("fields");

        // Add customer fields with type descriptors
        fields.set("studentId", stringValue(studentId));
        fields.set("name", stringValue(name));
        fields.set("mobile", stringValue(mobile));
        fields.set("walletBalanceCents", integerValue(walletBalanceCents));

        return root;
    }

    /**
     * Builds a customer document with email instead of mobile.
     * 
     * Alternative to customerDocument() for customers who prefer email contact.
     * 
     * @param studentId the student identifier
     * @param name the customer's full name
     * @param email the customer's email address
     * @param walletBalanceCents the wallet balance in cents
     * @return an ObjectNode ready to be sent to Firestore
     */
    public static ObjectNode customerDocumentWithEmail(String studentId, String name, String email, long walletBalanceCents) {
        // Create root document structure and fields object
        ObjectNode root = MAPPER.createObjectNode();
        ObjectNode fields = root.putObject("fields");

        // Add customer fields with type descriptors (email instead of mobile)
        fields.set("studentId", stringValue(studentId));
        fields.set("name", stringValue(name));
        fields.set("email", stringValue(email));
        fields.set("walletBalanceCents", integerValue(walletBalanceCents));

        return root;
    }

    public static Long readWalletBalanceCents(JsonNode firestoreDoc) {
        if (firestoreDoc == null) {
            return null;
        }
        JsonNode fields = firestoreDoc.get("fields");
        if (fields == null) {
            return null;
        }
        JsonNode wallet = fields.get("walletBalanceCents");
        if (wallet == null) {
            return null;
        }
        JsonNode integerValue = wallet.get("integerValue");
        if (integerValue != null && integerValue.isTextual()) {
            try {
                return Long.parseLong(integerValue.asText());
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    public static Long readWalletBalanceCents(DocumentSnapshot firestoreDoc) {
        if (firestoreDoc == null || !firestoreDoc.exists()) {
            return null;
        }
        Long value = readLongField(firestoreDoc, "walletBalanceCents", null);
        return value;
    }

    public static String readString(JsonNode firestoreDoc, String fieldName, String fallback) {
        JsonNode value = readField(firestoreDoc, fieldName);
        if (value == null) {
            return fallback;
        }
        JsonNode stringValue = value.get("stringValue");
        if (stringValue != null && stringValue.isTextual()) {
            String text = stringValue.asText().trim();
            return text.isEmpty() ? fallback : text;
        }
        return fallback;
    }

    public static String readString(DocumentSnapshot firestoreDoc, String fieldName, String fallback) {
        return readStringField(firestoreDoc, fieldName, fallback);
    }

    /**
     * Extracts a long integer value from a Firestore JSON document.
     * 
     * Handles multiple numeric formats that Firestore may return:
     * - integerValue: stored as text (e.g., "42")
     * - doubleValue: stored as text (e.g., "3.14") which gets rounded
     * 
     * If the field is missing or cannot be parsed, returns the fallback value.
     * 
     * @param firestoreDoc the Firestore JSON document (REST format)
     * @param fieldName the name of the field to read
     * @param fallback the default value if field is missing or invalid
     * @return the parsed long value, or fallback if not found/invalid
     */
    public static Long readLong(JsonNode firestoreDoc, String fieldName, Long fallback) {
        // Retrieve field value from document
        JsonNode value = readField(firestoreDoc, fieldName);
        if (value == null) {
            return fallback;
        }
        
        // Try parsing as integer
        JsonNode longValue = value.get("integerValue");
        if (longValue != null && longValue.isTextual()) {
            try {
                return Long.parseLong(longValue.asText());
            } catch (NumberFormatException ignored) {
                // Fall through to try doubleValue
            }
        }
        
        // Try parsing as double (round to nearest long)
        JsonNode doubleValue = value.get("doubleValue");
        if (doubleValue != null && doubleValue.isTextual()) {
            try {
                return Math.round(Double.parseDouble(doubleValue.asText()));
            } catch (NumberFormatException ignored) {
                // Fall through to return fallback
            }
        }
        
        // Could not parse any numeric format
        return fallback;
    }

    public static Long readLong(DocumentSnapshot firestoreDoc, String fieldName, Long fallback) {
        return readLongField(firestoreDoc, fieldName, fallback);
    }

    @SuppressWarnings("null")
    public static Integer readInteger(JsonNode firestoreDoc, String fieldName, Integer fallback) {
        Long value = readLong(firestoreDoc, fieldName, fallback == null ? null : fallback.longValue());
        return value != null ? (int)value.longValue() : fallback;
    }

    @SuppressWarnings("null")
    public static Integer readInteger(DocumentSnapshot firestoreDoc, String fieldName, Integer fallback) {
        Long value = readLongField(firestoreDoc, fieldName, fallback == null ? null : fallback.longValue());
        return value != null ? (int)value.longValue() : fallback;
    }

    public static Boolean readBoolean(JsonNode firestoreDoc, String fieldName, Boolean fallback) {
        JsonNode value = readField(firestoreDoc, fieldName);
        if (value == null) {
            return fallback;
        }
        JsonNode booleanValue = value.get("booleanValue");
        if (booleanValue != null && booleanValue.isBoolean()) {
            return booleanValue.asBoolean();
        }
        if (booleanValue != null && booleanValue.isTextual()) {
            return Boolean.parseBoolean(booleanValue.asText());
        }
        return fallback;
    }

    public static Boolean readBoolean(DocumentSnapshot firestoreDoc, String fieldName, Boolean fallback) {
        return readBooleanField(firestoreDoc, fieldName, fallback);
    }

    /**
     * Wraps a string value in Firestore format.
     * 
     * Used when building documents to send to Firestore.
     * Empty strings are converted to "" and null values are converted to empty string.
     * 
     * Firestore format: {"stringValue": "hello"}
     * 
     * @param value the string to wrap (null → "")
     * @return an ObjectNode with the wrapped value
     */
    public static ObjectNode stringValue(String value) {
        // Wrap in Firestore's REST format for strings
        ObjectNode n = MAPPER.createObjectNode();
        n.put("stringValue", value == null ? "" : value);
        return n;
    }

    /**
     * Wraps a long integer in Firestore format.
     * 
     * Used when building documents to send to Firestore.
     * Firestore stores integers as text strings.
     * 
     * Firestore format: {"integerValue": "42"}
     * 
     * @param value the long value to wrap
     * @return an ObjectNode with the wrapped value
     */
    public static ObjectNode integerValue(long value) {
        // Convert to string for Firestore (integers stored as text)
        ObjectNode n = MAPPER.createObjectNode();
        n.put("integerValue", Long.toString(value));
        return n;
    }

    /**
     * Wraps a boolean in Firestore format.
     * 
     * Used when building documents to send to Firestore.
     * 
     * Firestore format: {"booleanValue": true}
     * 
     * @param value the boolean to wrap
     * @return an ObjectNode with the wrapped value
     */
    public static ObjectNode booleanValue(boolean value) {
        // Wrap in Firestore's REST format for booleans
        ObjectNode n = MAPPER.createObjectNode();
        n.put("booleanValue", value);
        return n;
    }

    private static JsonNode readField(JsonNode firestoreDoc, String fieldName) {
        if (firestoreDoc == null || fieldName == null || fieldName.isBlank()) {
            return null;
        }
        JsonNode fields = firestoreDoc.get("fields");
        if (fields == null) {
            return null;
        }
        return fields.get(fieldName);
    }

    private static String readStringField(DocumentSnapshot firestoreDoc, String fieldName, String fallback) {
        if (firestoreDoc == null || !firestoreDoc.exists() || fieldName == null || fieldName.isBlank()) {
            return fallback;
        }
        Object value = firestoreDoc.get(fieldName);
        if (value instanceof String text) {
            String trimmed = text.trim();
            return trimmed.isEmpty() ? fallback : trimmed;
        }
        return fallback;
    }

    private static Long readLongField(DocumentSnapshot firestoreDoc, String fieldName, Long fallback) {
        if (firestoreDoc == null || !firestoreDoc.exists() || fieldName == null || fieldName.isBlank()) {
            return fallback;
        }
        Object value = firestoreDoc.get(fieldName);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text) {
            try {
                return Long.parseLong(text.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return fallback;
    }

    private static Boolean readBooleanField(DocumentSnapshot firestoreDoc, String fieldName, Boolean fallback) {
        if (firestoreDoc == null || !firestoreDoc.exists() || fieldName == null || fieldName.isBlank()) {
            return fallback;
        }
        Object value = firestoreDoc.get(fieldName);
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String text) {
            return Boolean.parseBoolean(text.trim());
        }
        return fallback;
    }

}
