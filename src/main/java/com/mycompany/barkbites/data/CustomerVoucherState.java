package com.mycompany.barkbites.data;

import java.io.IOException;
import java.util.Properties;

/**
 * Persists the customer's applied voucher code on the local machine.
 *
 * This keeps the voucher available when the user switches screens or
 * reopens the cart within the same account on the same computer.
 */
public final class CustomerVoucherState {

    private static final String KEY_PREFIX = "customer.voucher.";

    private CustomerVoucherState() {
    }

    public static String load(String uid) {
        if (uid == null || uid.isBlank()) {
            return null;
        }

        Properties props = LocalAppConfig.load();
        String value = props.getProperty(KEY_PREFIX + uid);
        return value == null || value.isBlank() ? null : value;
    }

    public static void save(String uid, String voucherCode) {
        if (uid == null || uid.isBlank()) {
            return;
        }

        Properties props = LocalAppConfig.load();
        String key = KEY_PREFIX + uid;
        if (voucherCode == null || voucherCode.isBlank()) {
            props.remove(key);
        } else {
            props.setProperty(key, voucherCode);
        }

        try {
            LocalAppConfig.save(props);
        } catch (IOException ignored) {
        }
    }
}