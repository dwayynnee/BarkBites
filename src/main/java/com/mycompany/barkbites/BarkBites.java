package com.mycompany.barkbites;

import com.mycompany.barkbites.CustomerForms.CustomerLandingPage;
import com.mycompany.barkbites.StaffForms.StaffLandingPage;

public class BarkBites {

    public static void main(String[] args) {
        setNimbusLookAndFeelIfAvailable();

        java.awt.EventQueue.invokeLater(() -> {
            StaffLandingPage staffLandingPage = new StaffLandingPage();
            staffLandingPage.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            staffLandingPage.setVisible(true);

            CustomerLandingPage customerLandingPage = new CustomerLandingPage();
            customerLandingPage.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            customerLandingPage.setVisible(true);
        });
    }

    private static void setNimbusLookAndFeelIfAvailable() {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }
    }
}
