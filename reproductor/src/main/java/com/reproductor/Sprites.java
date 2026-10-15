package com.reproductor;

import javax.imageio.ImageIO;
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

    /** Width in Figma pixels (file width / 2). */
    static int w(String name) {
        return get(name).getWidth() / Layout.SPRITE_SCALE;
    }

    /** Height in Figma pixels (file height / 2). */
    static int h(String name) {
        return get(name).getHeight() / Layout.SPRITE_SCALE;
    }
}
