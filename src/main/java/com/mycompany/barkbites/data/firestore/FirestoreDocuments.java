package com.mycompany.barkbites.data.firestore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.firestore.DocumentSnapshot;

/**
 * Helpers to build Firestore REST document bodies.
 */
public final class FirestoreDocuments {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private FirestoreDocuments() {
    }

    public static ObjectNode customerDocument(String studentId, String name, String mobile, long walletBalanceCents) {
        ObjectNode root = MAPPER.createObjectNode();
        ObjectNode fields = root.putObject("fields");

        fields.set("studentId", stringValue(studentId));
        fields.set("name", stringValue(name));
        fields.set("mobile", stringValue(mobile));
        fields.set("walletBalanceCents", integerValue(walletBalanceCents));

        return root;
    }

    public static ObjectNode customerDocumentWithEmail(String studentId, String name, String email, long walletBalanceCents) {
        ObjectNode root = MAPPER.createObjectNode();
        ObjectNode fields = root.putObject("fields");

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

    public static Long readLong(JsonNode firestoreDoc, String fieldName, Long fallback) {
        JsonNode value = readField(firestoreDoc, fieldName);
        if (value == null) {
            return fallback;
        }
        JsonNode longValue = value.get("integerValue");
        if (longValue != null && longValue.isTextual()) {
            try {
                return Long.parseLong(longValue.asText());
            } catch (NumberFormatException ignored) {
            }
        }
        JsonNode doubleValue = value.get("doubleValue");
        if (doubleValue != null && doubleValue.isTextual()) {
            try {
                return Math.round(Double.parseDouble(doubleValue.asText()));
            } catch (NumberFormatException ignored) {
            }
        }
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

    public static ObjectNode stringValue(String value) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("stringValue", value == null ? "" : value);
        return n;
    }

    public static ObjectNode integerValue(long value) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("integerValue", Long.toString(value));
        return n;
    }

    public static ObjectNode booleanValue(boolean value) {
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
