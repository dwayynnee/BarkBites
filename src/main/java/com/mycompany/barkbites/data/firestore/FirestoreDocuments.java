package com.mycompany.barkbites.data.firestore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

    private static ObjectNode stringValue(String value) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("stringValue", value == null ? "" : value);
        return n;
    }

    private static ObjectNode integerValue(long value) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("integerValue", Long.toString(value));
        return n;
    }
}
