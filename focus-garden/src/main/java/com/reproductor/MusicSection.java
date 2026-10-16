package com.reproductor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

/** The music player screen (cover, progress bar and play controls). */
final class MusicSection extends Section {

    interface Actions {
        void playPause();
        void previous();
        void next();
        void load();
        void useLink();
        /** @param fraction 0.0 (start) to 1.0 (end) */
        void seek(float fraction);
    }

    private static final Color PURPLE_SHADOW = new Color(0xB45CF0);
    private static final Color ICON_FILL    = new Color(0xE3CFF2);
    private static final Color ICON_OUTLINE = new Color(0xBBABC2);
    private static final Color ICON_SHADOW  = new Color(0xDFB5FF);

    private static final Gfx.TextStyle NAME_STYLE =
            Gfx.TextStyle.shadowed(Gfx.font(16), Color.WHITE, PURPLE_SHADOW, 1);
    private static final Gfx.TextStyle TIME_STYLE =
            Gfx.TextStyle.shadowed(Gfx.font(26), Color.WHITE, PURPLE_SHADOW, 1);

    private final Actions actions;
    private final Rectangle barHitArea;

    private String title = "no song loaded";
    private long positionMs;
    private long durationMs;
    private boolean playing;
    private boolean showTotal;      // click the time to switch elapsed <-> total
    private boolean seeking;
    private float seekFraction;

    MusicSection(Actions actions) {
        this.actions = actions;
        barHitArea = new Rectangle(Layout.BAR.x - 4, Layout.BAR.y - 8,
                Layout.BAR.width + 8, Layout.BAR.height + 16);

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
    }

    @Override String barSprite() {
        return "music-button.png";
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

    // ------------------------------------------------------------------ mouse

    @Override boolean mousePressed(Point p) {
        if (barHitArea.contains(p)) {
            seeking = true;
            seekFraction = fractionAt(p.x);
            repaint();
            return true;
        }
        return super.mousePressed(p);
    }

    @Override void mouseDragged(Point p) {
        if (seeking) {
            seekFraction = fractionAt(p.x);
            repaint();
        }
    }

    @Override void mouseReleased(Point p) {
        if (seeking) {
            seeking = false;
            actions.seek(fractionAt(p.x));
            repaint();
            return;
        }
        super.mouseReleased(p);
    }

    @Override boolean wantsHand(Point p) {
        return barHitArea.contains(p) || super.wantsHand(p);
    }

    @Override void reset() {
        super.reset();
        seeking = false;
    }

    private float fractionAt(int x) {
        float f = (x - Layout.BAR.x) / (float) Layout.BAR.width;
        return Math.max(0f, Math.min(1f, f));
    }

    private static Hotspot spot(String name, Point p, String sprite, Runnable action) {
        return new Hotspot(name, new Rectangle(p.x, p.y, Sprites.w(sprite), Sprites.h(sprite)), action);
    }

    // ------------------------------------------------------------------ painting

    @Override void paint(Graphics2D g) {
        // cover + song name
        Gfx.draw(g, "song-image.png", Layout.COVER.x, Layout.COVER.y);
        Gfx.draw(g, "song-name-base.png", Layout.SONG_NAME.x, Layout.SONG_NAME.y);
        int tagW = Sprites.w("song-name-base.png");
        int tagH = Sprites.h("song-name-base.png");
        String shown = Gfx.fit(g, NAME_STYLE.font(), title, tagW - 22);
        Gfx.text(g, NAME_STYLE, shown, Layout.SONG_NAME.x + tagW / 2.0, Layout.SONG_NAME.y + tagH / 2.0,
                Gfx.Align.CENTER);

        // progress bar
        Gfx.draw(g, "progressionbar-back.png", Layout.BAR.x, Layout.BAR.y);
        float fraction = seeking ? seekFraction
                : (durationMs > 0 ? Math.min(1f, positionMs / (float) durationMs) : 0f);
        Shape oldClip = g.getClip();
        g.clipRect(Layout.BAR.x, Layout.BAR.y, Math.round(Layout.BAR.width * fraction), Layout.BAR.height);
        Gfx.draw(g, "progressionbar-filling.png", Layout.BAR.x, Layout.BAR.y);
        g.setClip(oldClip);

        // time
        long shownMs = seeking ? Math.round(seekFraction * durationMs)
                : (showTotal ? durationMs : positionMs);
        Gfx.text(g, TIME_STYLE, Gfx.formatTime(shownMs), Layout.TIME_TEXT.x, Layout.TIME_TEXT.y, Gfx.Align.LEFT);

        // control panel
        Gfx.draw(g, "outer-frame.png", Layout.PANEL.x, Layout.PANEL.y);
        Gfx.draw(g, "divider.png", Layout.DIVIDER_LEFT.x, Layout.DIVIDER_LEFT.y);
        Gfx.draw(g, "divider.png", Layout.DIVIDER_RIGHT.x, Layout.DIVIDER_RIGHT.y);
        Gfx.draw(g, "back-button.png", Layout.BACK.x, Layout.BACK.y + (isPressed("back") ? 2 : 0));
        Gfx.draw(g, "next-button.png", Layout.NEXT.x, Layout.NEXT.y + (isPressed("next") ? 2 : 0));
        Gfx.draw(g, "load-text.png", Layout.LOAD_TEXT.x, Layout.LOAD_TEXT.y + (isPressed("load") ? 2 : 0));
        Gfx.draw(g, "use-link-text.png", Layout.LINK_TEXT.x, Layout.LINK_TEXT.y + (isPressed("link") ? 2 : 0));

        // big play button: purple underside + white top that sinks when pressed
        int sink = isPressed("play") ? 3 : 0;
        Gfx.draw(g, "playelipse-1.png", Layout.PLAY_CIRCLE.x, Layout.PLAY_CIRCLE.y);
        Gfx.draw(g, "playelipse-2.png", Layout.PLAY_CIRCLE.x, Layout.PLAY_CIRCLE.y + sink);
        if (playing) {
            drawPauseIcon(g, sink);
        } else {
            Gfx.draw(g, "play-icon.png", Layout.PLAY_ICON.x, Layout.PLAY_ICON.y + sink);
        }
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
