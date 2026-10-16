package com.reproductor;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * The window: pink frame, section bar, minimize/close buttons, window dragging,
 * and whichever {@link Section} is selected.
 */
final class PlayerPanel extends JPanel {

    interface WindowActions {
        void minimize();
        void close();
    }

    private final Section[] sections;
    private final List<Hotspot> windowSpots = new ArrayList<>();
    private int active;

    private String pressedWindowSpot;   // window-level button held down
    private Section captured;           // section that took the mouse press
    private Point dragOffset;           // for moving the window

    /**
     * @param sections in the same order as the buttons of the section bar
     * @param start    index of the section shown first
     */
    PlayerPanel(WindowActions actions, int start, Section... sections) {
        this.sections = sections;
        this.active = start;
        setOpaque(false);
        setPreferredSize(new Dimension(Layout.WIN_W, Layout.WIN_H));

        for (Section s : sections) {
            s.attach(this::repaint);
        }

        windowSpots.add(spot("close", Layout.CLOSE, "close-button.png", actions::close));
        windowSpots.add(spot("minimize", Layout.MINIMIZE, "minimize-button.png", actions::minimize));
        for (int i = 0; i < sections.length && i < Layout.SECTION_POSITIONS.length; i++) {
            int index = i;
            windowSpots.add(spot("section" + i, Layout.SECTION_POSITIONS[i], sections[i].barSprite(),
                    () -> select(index)));
        }

        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                Hotspot h = windowSpotAt(e.getPoint());
                if (h != null) {
                    pressedWindowSpot = h.name();
                    repaint();
                    return;
                }
                if (sections[active].mousePressed(e.getPoint())) {
                    captured = sections[active];
                    return;
                }
                Window w = SwingUtilities.getWindowAncestor(PlayerPanel.this);
                if (w != null) {
                    dragOffset = new Point(e.getXOnScreen() - w.getX(), e.getYOnScreen() - w.getY());
                }
            }

            @Override public void mouseDragged(MouseEvent e) {
                if (captured != null) {
                    captured.mouseDragged(e.getPoint());
                } else if (dragOffset != null) {
                    Window w = SwingUtilities.getWindowAncestor(PlayerPanel.this);
                    if (w != null) {
                        w.setLocation(e.getXOnScreen() - dragOffset.x, e.getYOnScreen() - dragOffset.y);
                    }
                }
            }

            @Override public void mouseReleased(MouseEvent e) {
                dragOffset = null;
                if (captured != null) {
                    Section s = captured;
                    captured = null;
                    s.mouseReleased(e.getPoint());
                    return;
                }
                String was = pressedWindowSpot;
                pressedWindowSpot = null;
                if (was != null) {
                    Hotspot h = windowSpotAt(e.getPoint());
                    repaint();
                    if (h != null && h.name().equals(was)) {
                        h.action().run();
                    }
                }
            }

            @Override public void mouseMoved(MouseEvent e) {
                boolean hand = windowSpotAt(e.getPoint()) != null || sections[active].wantsHand(e.getPoint());
                setCursor(Cursor.getPredefinedCursor(hand ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    /** Shows another section. */
    void select(int index) {
        if (index < 0 || index >= sections.length || index == active) {
            return;
        }
        sections[active].reset();
        active = index;
        repaint();
    }

    private static Hotspot spot(String name, Point p, String sprite, Runnable action) {
        return new Hotspot(name, new Rectangle(p.x, p.y, Sprites.w(sprite), Sprites.h(sprite)), action);
    }

    private Hotspot windowSpotAt(Point p) {
        for (Hotspot h : windowSpots) {
            if (h.area().contains(p)) {
                return h;
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Gfx.quality(g);

        // window (frame, shadow and pixel plants are all in this one image)
        Gfx.draw(g, "window-base.png", 0, 0);

        // section bar
        if (Layout.SHOW_SECTIONS_BAR) {
            Gfx.draw(g, "rightnext-button.png", Layout.ARROW_LEFT.x, Layout.ARROW_LEFT.y);
            Gfx.draw(g, "leftnext-button.png", Layout.ARROW_RIGHT.x, Layout.ARROW_RIGHT.y);
            for (int i = 0; i < sections.length && i < Layout.SECTION_POSITIONS.length; i++) {
                boolean sunk = (Layout.MARK_ACTIVE_SECTION && i == active) || ("section" + i).equals(pressedWindowSpot);
                Gfx.draw(g, sections[i].barSprite(), Layout.SECTION_POSITIONS[i].x,
                        Layout.SECTION_POSITIONS[i].y + (sunk ? 2 : 0));
            }
        }

        sections[active].paint(g);

        // window buttons (top right)
        Gfx.draw(g, "minimize-button.png", Layout.MINIMIZE.x,
                Layout.MINIMIZE.y + ("minimize".equals(pressedWindowSpot) ? 2 : 0));
        Gfx.draw(g, "close-button.png", Layout.CLOSE.x,
                Layout.CLOSE.y + ("close".equals(pressedWindowSpot) ? 2 : 0));

        g.dispose();
    }
}
