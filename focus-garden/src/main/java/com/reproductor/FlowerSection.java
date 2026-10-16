package com.reproductor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Flower (items) screen: LOCKED button, padlock, the item card with its name and the "time left" strip.
 * Layout only for now; use setItemName when items exist.
 */
final class FlowerSection extends Section {

    private static final Gfx.TextStyle PILL_TEXT =
            Gfx.TextStyle.outlined(Gfx.font(32), new Color(240, 220, 255), new Color(181, 140, 217));
    private static final Gfx.TextStyle NAME_STYLE =
            Gfx.TextStyle.plain(Gfx.font(Layout.FLOWER_NAME_SIZE), new Color(192, 121, 255));

    private String line1 = "PURPLE";
    private String line2 = "FLOWER";

    FlowerSection() {
        hotspots.add(new Hotspot("locked", new Rectangle(Layout.FLOWER_PILL.x, Layout.FLOWER_PILL.y,
                (int) Gfx.w("global-pink-button.png"), (int) Gfx.h("global-pink-button.png")),
                () -> { }));      // nothing happens yet
    }

    @Override String barSprite() {
        return "flower-button.png";
    }

    /** Name of the item, shown on up to two lines (it is split at the first space). */
    void setItemName(String name) {
        int space = name.indexOf(' ');
        line1 = space < 0 ? name : name.substring(0, space);
        line2 = space < 0 ? "" : name.substring(space + 1);
        repaint();
    }

    @Override void paint(Graphics2D g) {
        // pink LOCKED button
        int sink = isPressed("locked") ? 2 : 0;
        double px = Layout.FLOWER_PILL.x;
        double py = Layout.FLOWER_PILL.y + sink;
        Gfx.drawAt(g, "global-pink-button.png", px, py);
        Gfx.text(g, PILL_TEXT, "LOCKED", px + Gfx.w("global-pink-button.png") / 2,
                py + Gfx.h("global-pink-button.png") / 2, Gfx.Align.CENTER);

        // padlock
        Gfx.drawAt(g, "flower-lock-open.png", Layout.FLOWER_LOCK.x, Layout.FLOWER_LOCK.y);

        // item card
        Gfx.softFrame(g, Layout.FLOWER_CONTAINER);
        Gfx.drawAt(g, "flower-frame.png", Layout.FLOWER_CARD.x, Layout.FLOWER_CARD.y);
        Gfx.text(g, NAME_STYLE, line1, Layout.FLOWER_NAME_X, Layout.FLOWER_NAME_CY1, Gfx.Align.LEFT);
        Gfx.text(g, NAME_STYLE, line2, Layout.FLOWER_NAME_X, Layout.FLOWER_NAME_CY2, Gfx.Align.LEFT);
        Gfx.drawAt(g, "flower-time-left-label.png", Layout.FLOWER_TIME_LABEL.x, Layout.FLOWER_TIME_LABEL.y);

        // ITEMS + bottom purple bar (over the plants)
        Gfx.drawAt(g, "flower-items-label.png", Layout.FLOWER_ITEMS_LABEL.x, Layout.FLOWER_ITEMS_LABEL.y);
        Gfx.drawAt(g, "global-purple-frame.png", Layout.FLOWER_BOTTOM_FRAME.x, Layout.FLOWER_BOTTOM_FRAME.y);
    }
}
