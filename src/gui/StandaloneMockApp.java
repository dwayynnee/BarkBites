package gui;

import data.BarkBitesSystem;

import javax.swing.SwingUtilities;

/**
 * Entry point for the localized, standalone (no DB/cloud) prototype.
 */
public final class StandaloneMockApp {
    private StandaloneMockApp() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BarkBitesSystem system = BarkBitesSystem.createWithMockData();

            StaffManagementFrame staff = new StaffManagementFrame(system);
            staff.setVisible(true);

            CustomerKioskFrame customer = new CustomerKioskFrame(system, staff::refreshTables);
            customer.setVisible(true);
        });
    }
}
