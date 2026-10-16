package com.reproductor;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * One screen of the player (timer, flower, books, music). The window frame, the section bar and
 * the minimize/close buttons belong to {@link PlayerPanel}; a section only draws what goes
 * inside the pink frame and reacts to clicks there.
 */
abstract class Section {

    protected final List<Hotspot> hotspots = new ArrayList<>();
    protected String pressedName;
    private Runnable repainter = () -> { };

    /** Image of this section's button in the section bar. */
    abstract String barSprite();

    abstract void paint(Graphics2D g);

    final void attach(Runnable repainter) {
        this.repainter = repainter;
    }

    protected final void repaint() {
        repainter.run();
    }

    /** Called when the user leaves this section. */
    void reset() {
        pressedName = null;
    }

    /** @return true if the press was used by this section (so the window must not start dragging) */
    boolean mousePressed(Point p) {
        Hotspot h = hotspotAt(p);
        if (h != null) {
            pressedName = h.name();
            repaint();
            return true;
        }
        return false;
    }

    void mouseDragged(Point p) { }

    void mouseReleased(Point p) {
        String was = pressedName;
        pressedName = null;
        if (was != null) {
            Hotspot h = hotspotAt(p);
            repaint();
            if (h != null && h.name().equals(was)) {
                h.action().run();
            }
        }
    }

    boolean wantsHand(Point p) {
        return hotspotAt(p) != null;
    }

    protected final Hotspot hotspotAt(Point p) {
        for (Hotspot h : hotspots) {
            if (h.area().contains(p)) {
                return h;
            }
        }
        return null;
    }

    protected final boolean isPressed(String name) {
        return name.equals(pressedName);
    }
}
