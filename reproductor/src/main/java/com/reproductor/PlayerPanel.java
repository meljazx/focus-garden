package com.reproductor;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * The whole player drawn from your Figma images, plus mouse handling.
 * It knows nothing about audio: it just shows the state it is given and tells
 * {@link Actions} when the user clicks something.
 */
final class PlayerPanel extends JPanel {

    interface Actions {
        void playPause();
        void previous();
        void next();
        void load();
        void useLink();
        /** @param fraction 0.0 (start) to 1.0 (end) */
        void seek(float fraction);
        void minimize();
        void close();
    }

    private record Hotspot(String name, Rectangle area, Runnable action) { }

    private static final Color TEXT_SHADOW = new Color(0xB45CF0);
    private static final Color ICON_FILL    = new Color(0xE3CFF2);
    private static final Color ICON_OUTLINE = new Color(0xBBABC2);
    private static final Color ICON_SHADOW  = new Color(0xDFB5FF);

    private final Actions actions;
    private final List<Hotspot> hotspots = new ArrayList<>();
    private final Rectangle barHitArea;
    private final Font timeFont = loadFont(16f);
    private final Font nameFont = loadFont(12f);

    // what is shown
    private String title = "no song loaded";
    private long positionMs;
    private long durationMs;
    private boolean playing;
    private boolean showTotal;      // click the time to switch elapsed <-> total

    // interaction
    private String pressed;         // hotspot currently held down
    private boolean seeking;
    private float seekFraction;
    private Point dragOffset;       // for moving the window

    PlayerPanel(Actions actions) {
        this.actions = actions;
        setOpaque(false);
        setPreferredSize(new Dimension(Layout.WIN_W, Layout.WIN_H));

        barHitArea = new Rectangle(Layout.BAR.x - 4, Layout.BAR.y - 8,
                Layout.BAR.width + 8, Layout.BAR.height + 16);

        hotspots.add(spot("close", Layout.CLOSE, "close-button.png", actions::close));
        hotspots.add(spot("minimize", Layout.MINIMIZE, "minimize-button.png", actions::minimize));
        hotspots.add(spot("back", Layout.BACK, "back-button.png", actions::previous));
        hotspots.add(spot("next", Layout.NEXT, "next-button.png", actions::next));
        hotspots.add(new Hotspot("play",
                new Rectangle(Layout.PLAY_CIRCLE.x, Layout.PLAY_CIRCLE.y,
                        Sprites.w("playelipse-2.png"), Sprites.h("playelipse-2.png")),
                actions::playPause));
        hotspots.add(new Hotspot("load",
                new Rectangle(Layout.DIVIDER_LEFT.x, Layout.DIVIDER_LEFT.y - 4,
                        Sprites.w("divider.png"), 44),
                actions::load));
        hotspots.add(new Hotspot("link",
                new Rectangle(Layout.LINK_TEXT.x, Layout.DIVIDER_RIGHT.y - 4,
                        Sprites.w("use-link-text.png"), 44),
                actions::useLink));
        hotspots.add(new Hotspot("time",
                new Rectangle(Layout.TIME_TEXT.x - 4, Layout.TIME_TEXT.y - 12, 68, 24),
                () -> { showTotal = !showTotal; repaint(); }));

        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                if (barHitArea.contains(e.getPoint())) {
                    seeking = true;
                    seekFraction = fractionAt(e.getX());
                    repaint();
                    return;
                }
                Hotspot h = hotspotAt(e.getPoint());
                if (h != null) {
                    pressed = h.name();
                    repaint();
                    return;
                }
                Window w = SwingUtilities.getWindowAncestor(PlayerPanel.this);
                if (w != null) {
                    dragOffset = new Point(e.getXOnScreen() - w.getX(), e.getYOnScreen() - w.getY());
                }
            }

            @Override public void mouseDragged(MouseEvent e) {
                if (seeking) {
                    seekFraction = fractionAt(e.getX());
                    repaint();
                } else if (dragOffset != null) {
                    Window w = SwingUtilities.getWindowAncestor(PlayerPanel.this);
                    if (w != null) {
                        w.setLocation(e.getXOnScreen() - dragOffset.x, e.getYOnScreen() - dragOffset.y);
                    }
                }
            }

            @Override public void mouseReleased(MouseEvent e) {
                dragOffset = null;
                if (seeking) {
                    seeking = false;
                    PlayerPanel.this.actions.seek(fractionAt(e.getX()));
                    repaint();
                    return;
                }
                String was = pressed;
                pressed = null;
                if (was != null) {
                    Hotspot h = hotspotAt(e.getPoint());
                    repaint();
                    if (h != null && h.name().equals(was)) {
                        h.action().run();
                    }
                }
            }

            @Override public void mouseMoved(MouseEvent e) {
                boolean hand = barHitArea.contains(e.getPoint()) || hotspotAt(e.getPoint()) != null;
                setCursor(Cursor.getPredefinedCursor(hand ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    // ------------------------------------------------------------------ state from outside

    void setTitle(String title) {
        this.title = title;
        repaint();
    }

    void setTime(long positionMs, long durationMs) {
        this.positionMs = positionMs;
        this.durationMs = durationMs;
        repaint();
    }

    void setPlaying(boolean playing) {
        this.playing = playing;
        repaint();
    }

    // ------------------------------------------------------------------ helpers

    private static Hotspot spot(String name, Point p, String sprite, Runnable action) {
        return new Hotspot(name, new Rectangle(p.x, p.y, Sprites.w(sprite), Sprites.h(sprite)), action);
    }

    private Hotspot hotspotAt(Point p) {
        for (Hotspot h : hotspots) {
            if (h.area().contains(p)) {
                return h;
            }
        }
        return null;
    }

    private float fractionAt(int x) {
        float f = (x - Layout.BAR.x) / (float) Layout.BAR.width;
        return Math.max(0f, Math.min(1f, f));
    }

    private boolean isPressed(String name) {
        return name.equals(pressed);
    }

    private static String formatTime(long ms) {
        long total = Math.max(0, ms) / 1000;
        return String.format("%02d:%02d", total / 60, total % 60);
    }

    /** Uses src/main/resources/fonts/pixel.ttf if you put your pixel font there. */
    private static Font loadFont(float size) {
        try (InputStream in = PlayerPanel.class.getResourceAsStream("/fonts/pixel.ttf")) {
            if (in != null) {
                return Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(size);
            }
        } catch (Exception ignored) {
            // fall through to the default font
        }
        return new Font(Font.MONOSPACED, Font.BOLD, Math.round(size));
    }

    private static void draw(Graphics2D g, String sprite, int x, int y) {
        BufferedImage img = Sprites.get(sprite);
        g.drawImage(img, x, y, img.getWidth() / Layout.SPRITE_SCALE, img.getHeight() / Layout.SPRITE_SCALE, null);
    }

    private static void shadowText(Graphics2D g, String text, int x, int y) {
        g.setColor(TEXT_SHADOW);
        g.drawString(text, x + 1, y + 1);
        g.setColor(Color.WHITE);
        g.drawString(text, x, y);
    }

    private static String fit(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "\u2026";
        int end = text.length();
        while (end > 1 && fm.stringWidth(text.substring(0, end) + ellipsis) > maxWidth) {
            end--;
        }
        return text.substring(0, end).trim() + ellipsis;
    }

    // ------------------------------------------------------------------ painting

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // window (frame, shadow and pixel plants are all in this one image)
        draw(g, "window-base.png", 0, 0);

        if (Layout.SHOW_SECTIONS_BAR) {
            draw(g, "rightnext-button.png", Layout.ARROW_LEFT.x, Layout.ARROW_LEFT.y);
            draw(g, "leftnext-button.png", Layout.ARROW_RIGHT.x, Layout.ARROW_RIGHT.y);
            for (int i = 0; i < Layout.SECTION_SPRITES.length; i++) {
                draw(g, Layout.SECTION_SPRITES[i], Layout.SECTION_POSITIONS[i].x, Layout.SECTION_POSITIONS[i].y);
            }
        }

        // cover + song name
        draw(g, "song-image.png", Layout.COVER.x, Layout.COVER.y);
        draw(g, "song-name-base.png", Layout.SONG_NAME.x, Layout.SONG_NAME.y);
        g.setFont(nameFont);
        FontMetrics nfm = g.getFontMetrics();
        int tagW = Sprites.w("song-name-base.png");
        int tagH = Sprites.h("song-name-base.png");
        String shown = fit(title, nfm, tagW - 22);
        int nx = Layout.SONG_NAME.x + (tagW - nfm.stringWidth(shown)) / 2;
        int ny = Layout.SONG_NAME.y + (tagH - nfm.getHeight()) / 2 + nfm.getAscent() - 1;
        shadowText(g, shown, nx, ny);

        // progress bar
        draw(g, "progressionbar-back.png", Layout.BAR.x, Layout.BAR.y);
        float fraction = seeking ? seekFraction
                : (durationMs > 0 ? Math.min(1f, positionMs / (float) durationMs) : 0f);
        Shape oldClip = g.getClip();
        g.clipRect(Layout.BAR.x, Layout.BAR.y, Math.round(Layout.BAR.width * fraction), Layout.BAR.height);
        draw(g, "progressionbar-filling.png", Layout.BAR.x, Layout.BAR.y);
        g.setClip(oldClip);

        // time
        g.setFont(timeFont);
        FontMetrics tfm = g.getFontMetrics();
        long shownMs = seeking ? Math.round(seekFraction * durationMs)
                : (showTotal ? durationMs : positionMs);
        int ty = Layout.TIME_TEXT.y - tfm.getHeight() / 2 + tfm.getAscent();
        shadowText(g, formatTime(shownMs), Layout.TIME_TEXT.x, ty);

        // control panel
        draw(g, "outer-frame.png", Layout.PANEL.x, Layout.PANEL.y);
        draw(g, "divider.png", Layout.DIVIDER_LEFT.x, Layout.DIVIDER_LEFT.y);
        draw(g, "divider.png", Layout.DIVIDER_RIGHT.x, Layout.DIVIDER_RIGHT.y);
        draw(g, "back-button.png", Layout.BACK.x, Layout.BACK.y + (isPressed("back") ? 2 : 0));
        draw(g, "next-button.png", Layout.NEXT.x, Layout.NEXT.y + (isPressed("next") ? 2 : 0));
        draw(g, "load-text.png", Layout.LOAD_TEXT.x, Layout.LOAD_TEXT.y + (isPressed("load") ? 2 : 0));
        draw(g, "use-link-text.png", Layout.LINK_TEXT.x, Layout.LINK_TEXT.y + (isPressed("link") ? 2 : 0));

        // big play button: purple underside + white top that sinks when pressed
        int sink = isPressed("play") ? 3 : 0;
        draw(g, "playelipse-1.png", Layout.PLAY_CIRCLE.x, Layout.PLAY_CIRCLE.y);
        draw(g, "playelipse-2.png", Layout.PLAY_CIRCLE.x, Layout.PLAY_CIRCLE.y + sink);
        if (playing) {
            drawPauseIcon(g, sink);
        } else {
            draw(g, "play-icon.png", Layout.PLAY_ICON.x, Layout.PLAY_ICON.y + sink);
        }

        // window buttons (top right)
        draw(g, "minimize-button.png", Layout.MINIMIZE.x, Layout.MINIMIZE.y + (isPressed("minimize") ? 2 : 0));
        draw(g, "close-button.png", Layout.CLOSE.x, Layout.CLOSE.y + (isPressed("close") ? 2 : 0));

        g.dispose();
    }

    /** There is no pause image in the Figma export, so it is drawn in the same colours as the play icon. */
    private void drawPauseIcon(Graphics2D g, int sink) {
        int cx = Layout.PLAY_CIRCLE.x + Sprites.w("playelipse-2.png") / 2;
        int cy = Layout.PLAY_CIRCLE.y + Sprites.h("playelipse-2.png") / 2 + sink - 2;
        int barW = 16;
        int barH = 50;
        int gap = 14;
        int left = cx - (barW * 2 + gap) / 2;
        int top = cy - barH / 2;
        for (int i = 0; i < 2; i++) {
            int x = left + i * (barW + gap);
            g.setColor(ICON_SHADOW);
            g.fill(new RoundRectangle2D.Float(x, top + 5, barW, barH, 8, 8));
            g.setColor(ICON_OUTLINE);
            g.fill(new RoundRectangle2D.Float(x, top, barW, barH, 8, 8));
            g.setColor(ICON_FILL);
            g.fill(new RoundRectangle2D.Float(x + 1, top + 1, barW - 2, barH - 2, 7, 7));
        }
    }
}
