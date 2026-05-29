package org.netbeans.lib.awtextra;

import java.awt.Dimension;

/** Minimal replacement for NetBeans' AbsoluteConstraints used by the GUI forms. */
public final class AbsoluteConstraints {

    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public AbsoluteConstraints(int x, int y) {
        this(x, y, -1, -1);
    }

    public AbsoluteConstraints(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Dimension getSizeHint() {
        return (width > 0 && height > 0) ? new Dimension(width, height) : null;
    }
}
