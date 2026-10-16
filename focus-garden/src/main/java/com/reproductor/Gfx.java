package com.reproductor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;

/** Small drawing helpers shared by the window and all the sections. */
final class Gfx {

    private static Font baseFont;

    private Gfx() { }

    // ------------------------------------------------------------------ font

    /** Pixeboy (src/main/resources/fonts/Pixeboy.ttf). Falls back to a monospace font if it is missing. */
    static Font font(float size) {
        if (baseFont == null) {
            try (InputStream in = Gfx.class.getResourceAsStream("/fonts/Pixeboy.ttf")) {
                baseFont = in != null ? Font.createFont(Font.TRUETYPE_FONT, in)
                        : new Font(Font.MONOSPACED, Font.BOLD, 12);
            } catch (Exception e) {
                baseFont = new Font(Font.MONOSPACED, Font.BOLD, 12);
            }
        }
        return baseFont.deriveFont(size);
    }

    // ------------------------------------------------------------------ images

    static void quality(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    /** Draws the image with its file corner at (x, y). */
    static void draw(Graphics2D g, String sprite, int x, int y) {
        BufferedImage img = Sprites.get(sprite);
        g.drawImage(img, x, y, img.getWidth() / Layout.SPRITE_SCALE, img.getHeight() / Layout.SPRITE_SCALE, null);
    }

    /** Draws the image so that the top-left of its VISIBLE artwork lands at (x, y). */
    static void drawAt(Graphics2D g, String sprite, double x, double y) {
        Rectangle2D.Double v = Sprites.visible(sprite);
        AffineTransform at = new AffineTransform();
        at.translate(x - v.x, y - v.y);
        at.scale(1.0 / Layout.SPRITE_SCALE, 1.0 / Layout.SPRITE_SCALE);
        g.drawImage(Sprites.get(sprite), at, null);
    }

    static double w(String sprite) {
        return Sprites.visible(sprite).width;
    }

    static double h(String sprite) {
        return Sprites.visible(sprite).height;
    }

    /** x that centres a sprite's visible artwork on cx. */
    static double centerX(String sprite, double cx) {
        return cx - w(sprite) / 2;
    }

    /** The faint rounded outline that holds a list (the "pending for reading" and flower cards). */
    static void softFrame(Graphics2D g, Rectangle r) {
        g.setColor(new Color(233, 205, 255));
        g.setStroke(new BasicStroke(2f));
        g.draw(new RoundRectangle2D.Double(r.x + 1, r.y + 1, r.width - 2, r.height - 2, 36, 36));
        g.setStroke(new BasicStroke(1f));
    }

    // ------------------------------------------------------------------ text

    enum Align { LEFT, CENTER }

    record TextStyle(Font font, Color fill, Color shadow, int shadowDx, int shadowDy, Color outline) {
        static TextStyle plain(Font f, Color fill) {
            return new TextStyle(f, fill, null, 0, 0, null);
        }

        static TextStyle shadowed(Font f, Color fill, Color shadow, int offset) {
            return new TextStyle(f, fill, shadow, offset, offset, null);
        }

        static TextStyle outlined(Font f, Color fill, Color outline) {
            return new TextStyle(f, fill, null, 0, 0, outline);
        }
    }

    static Rectangle2D textBounds(Graphics2D g, Font f, String text) {
        return f.createGlyphVector(g.getFontRenderContext(), text).getVisualBounds();
    }

    /**
     * Draws text so that its visible left edge (or centre) is at x and its visible vertical
     * centre is at cy, which makes it easy to line up with the Figma design.
     */
    static void text(Graphics2D g, TextStyle st, String text, double x, double cy, Align align) {
        if (text == null || text.isEmpty()) {
            return;
        }
        Rectangle2D b = textBounds(g, st.font(), text);
        double left = align == Align.CENTER ? x - b.getWidth() / 2 : x;
        float drawX = (float) (left - b.getX());
        float baseline = (float) (cy - (b.getY() + b.getHeight() / 2));
        g.setFont(st.font());
        if (st.outline() != null) {
            g.setColor(st.outline());
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx != 0 || dy != 0) {
                        g.drawString(text, drawX + dx, baseline + dy);
                    }
                }
            }
        }
        if (st.shadow() != null) {
            g.setColor(st.shadow());
            g.drawString(text, drawX + st.shadowDx(), baseline + st.shadowDy());
        }
        g.setColor(st.fill());
        g.drawString(text, drawX, baseline);
    }

    /** Shortens text with "..." until it fits maxWidth. */
    static String fit(Graphics2D g, Font f, String text, double maxWidth) {
        if (textBounds(g, f, text).getWidth() <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int end = text.length();
        while (end > 1 && textBounds(g, f, text.substring(0, end).trim() + ellipsis).getWidth() > maxWidth) {
            end--;
        }
        return text.substring(0, end).trim() + ellipsis;
    }

    static String formatTime(long ms) {
        long total = Math.max(0, ms) / 1000;
        return String.format("%02d:%02d", total / 60, total % 60);
    }
}
