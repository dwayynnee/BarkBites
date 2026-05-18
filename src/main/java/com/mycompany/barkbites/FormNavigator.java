package com.mycompany.barkbites;

import java.awt.Point;
import javax.swing.JFrame;

public final class FormNavigator {

    private FormNavigator() {
    }

    /**
     * Shows {@code to} at the same screen position as {@code from}, then disposes {@code from}.
     */
    public static void redirect(JFrame from, JFrame to) {
        if (to == null) {
            if (from != null) {
                from.dispose();
            }
            return;
        }

        int fromExtendedState = JFrame.NORMAL;
        Point fromLocation = null;

        if (from != null) {
            fromExtendedState = from.getExtendedState();
            fromLocation = from.getLocation();
        }

        if (fromLocation != null) {
            to.setLocation(fromLocation);
        }

        to.setVisible(true);
        to.setExtendedState(fromExtendedState);

        if (from != null) {
            from.dispose();
        }
    }
}
