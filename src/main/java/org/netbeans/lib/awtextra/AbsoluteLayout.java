package org.netbeans.lib.awtextra;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

/** Minimal replacement for NetBeans' AbsoluteLayout used by the GUI forms. */
public final class AbsoluteLayout implements LayoutManager2 {

    private final Map<Component, AbsoluteConstraints> constraints = Collections.synchronizedMap(new IdentityHashMap<>());

    @Override
    public void addLayoutComponent(Component comp, Object cons) {
        if (cons instanceof AbsoluteConstraints ac) {
            constraints.put(comp, ac);
        }
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0.5f;
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0.5f;
    }

    @Override
    public void invalidateLayout(Container target) {
        // no-op
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
        // deprecated method - ignore
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        constraints.remove(comp);
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        int maxX = 0;
        int maxY = 0;
        for (Component c : parent.getComponents()) {
            AbsoluteConstraints ac = constraints.get(c);
            Dimension pref = c.getPreferredSize();
            if (ac != null) {
                int w = ac.width > 0 ? ac.width : pref.width;
                int h = ac.height > 0 ? ac.height : pref.height;
                maxX = Math.max(maxX, ac.x + w);
                maxY = Math.max(maxY, ac.y + h);
            } else {
                maxX = Math.max(maxX, c.getX() + pref.width);
                maxY = Math.max(maxY, c.getY() + pref.height);
            }
        }
        return new Dimension(maxX, maxY);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return parent.getMinimumSize();
    }

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            for (Component c : parent.getComponents()) {
                AbsoluteConstraints ac = constraints.get(c);
                if (ac != null) {
                    int w = ac.width > 0 ? ac.width : c.getPreferredSize().width;
                    int h = ac.height > 0 ? ac.height : c.getPreferredSize().height;
                    c.setBounds(ac.x, ac.y, w, h);
                } else {
                    // If no constraints provided, leave the component's bounds as-is
                }
            }
        }
    }

    // Note: do not implement extra non-standard methods here.

}
