package com.reproductor;

import javax.imageio.ImageIO;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

/** Loads (and caches) the PNGs from src/main/resources/img. */
final class Sprites {

    private static final Map<String, BufferedImage> CACHE = new HashMap<>();

    private Sprites() { }

    static BufferedImage get(String name) {
        return CACHE.computeIfAbsent(name, n -> {
            try (InputStream in = Sprites.class.getResourceAsStream("/img/" + n)) {
                if (in == null) {
                    throw new IllegalStateException("Missing image: /img/" + n);
                }
                return ImageIO.read(in);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private static final Map<String, Rectangle2D.Double> VISIBLE = new HashMap<>();

    /**
     * The part of the image that is not transparent, in Figma pixels, relative to the image corner.
     * Some exports keep transparent margins around the artwork; this lets us place the artwork itself.
     */
    static Rectangle2D.Double visible(String name) {
        return VISIBLE.computeIfAbsent(name, n -> {
            BufferedImage img = get(n);
            int minX = img.getWidth();
            int minY = img.getHeight();
            int maxX = -1;
            int maxY = -1;
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    if ((img.getRGB(x, y) >>> 24) > 16) {
                        minX = Math.min(minX, x);
                        maxX = Math.max(maxX, x);
                        minY = Math.min(minY, y);
                        maxY = Math.max(maxY, y);
                    }
                }
            }
            double s = Layout.SPRITE_SCALE;
            if (maxX < 0) {
                return new Rectangle2D.Double(0, 0, img.getWidth() / s, img.getHeight() / s);
            }
            return new Rectangle2D.Double(minX / s, minY / s, (maxX - minX + 1) / s, (maxY - minY + 1) / s);
        });
    }

    /** Width in Figma pixels (file width / 2). */
    static int w(String name) {
        return get(name).getWidth() / Layout.SPRITE_SCALE;
    }

    /** Height in Figma pixels (file height / 2). */
    static int h(String name) {
        return get(name).getHeight() / Layout.SPRITE_SCALE;
    }
}
