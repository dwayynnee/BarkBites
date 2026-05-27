package com.mycompany.barkbites;

import java.awt.Point;
import javax.swing.JFrame;

/**
 * Navigation utility for transitioning between screens.
 * 
 * Provides clean screen transitions by:
 * - Preserving the source window position for destination window
 * - Maintaining the window state (normal/maximized)
 * - Properly disposing of the source window after transition
 * 
 * Usage: FormNavigator.redirect(currentWindow, nextWindow);
 */
public final class FormNavigator {

    // Private constructor: utility class with only static methods
    private FormNavigator() {
    }

    /**
     * Smoothly transitions from one frame to another.
     * 
     * Shows the destination frame at the same position as the source frame,
     * preserves the window state (normal/maximized), then closes the source.
     * This creates a seamless screen transition effect.
     * 
     * @param from the current window to close (can be null)
     * @param to the next window to display (can be null to just close 'from')
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
