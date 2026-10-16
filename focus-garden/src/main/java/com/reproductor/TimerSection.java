package com.reproductor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/** Timer screen: big counter, START FOCUS button, and hearts + progress bar at the bottom. Layout only for now. */
final class TimerSection extends Section {

    private static final double LIP = 6;   // darker edge under the purple button

    private static final Gfx.TextStyle COUNTER_STYLE = Gfx.TextStyle.outlined(
            Gfx.font(Layout.TIMER_COUNTER_SIZE), Color.WHITE, new Color(214, 172, 250));

    TimerSection() {
        hotspots.add(new Hotspot("focus", new Rectangle(Layout.TIMER_BUTTON.x, Layout.TIMER_BUTTON.y,
                (int) Gfx.w("timer-main-button.png"), (int) Gfx.h("timer-main-button.png")),
                () -> { }));      // nothing happens yet
    }

    @Override String barSprite() {
        return "timer-button.png";
    }

    @Override void paint(Graphics2D g) {
        Gfx.text(g, COUNTER_STYLE, "00:00", Layout.TIMER_COUNTER.x, Layout.TIMER_COUNTER.y, Gfx.Align.CENTER);

        // START FOCUS button
        int sink = isPressed("focus") ? 2 : 0;
        double bx = Layout.TIMER_BUTTON.x;
        double by = Layout.TIMER_BUTTON.y + sink;
        Gfx.drawAt(g, "timer-main-button.png", bx, by);
        double faceCy = by + (Gfx.h("timer-main-button.png") - LIP) / 2;
        Gfx.drawAt(g, "timer-start-focus.png",
                bx + (Gfx.w("timer-main-button.png") - Gfx.w("timer-start-focus.png")) / 2,
                faceCy - Gfx.h("timer-start-focus.png") / 2);

        // hearts + progress bar
        Gfx.drawAt(g, "timer-bar.png", Layout.TIMER_BAR.x, Layout.TIMER_BAR.y);
        double fillX = Layout.TIMER_BAR.x + Layout.TIMER_FILL_DX;
        double fillY = Layout.TIMER_BAR.y + Layout.TIMER_FILL_DY;
        Shape oldClip = g.getClip();
        g.clip(new Rectangle2D.Double(fillX, fillY,
                Gfx.w("timer-bar-fill.png") * Layout.TIMER_FILL, Gfx.h("timer-bar-fill.png")));
        Gfx.drawAt(g, "timer-bar-fill.png", fillX, fillY);
        g.setClip(oldClip);
    }
}
