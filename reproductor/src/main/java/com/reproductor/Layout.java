package com.reproductor;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * Every position in the window, in "Figma pixels" (the images are exported at 2x,
 * so they are drawn at half their file size).
 * If something is a pixel off compared with your Figma file, change it HERE.
 */
final class Layout {

    private Layout() { }

    /** Images were exported at 2x. */
    static final int SPRITE_SCALE = 2;

    static final int WIN_W = 614;
    static final int WIN_H = 667;

    // --- window buttons (top right) ---
    static final Point MINIMIZE = new Point(471, 15);
    static final Point CLOSE    = new Point(523, 15);

    // --- sections bar (decoration only for now) ---
    static final boolean SHOW_SECTIONS_BAR = true;
    static final Point ARROW_LEFT  = new Point(25, 68);
    static final Point ARROW_RIGHT = new Point(540, 68);
    static final String[] SECTION_SPRITES = {
            "timer-button.png", "flower-button.png", "books-button.png", "music-button.png"};
    static final Point[] SECTION_POSITIONS = {
            new Point(70, 58), new Point(187, 58), new Point(305, 58), new Point(422, 58)};

    // --- cover + song name + progress ---
    static final Point COVER     = new Point(86, 153);
    static final Point SONG_NAME = new Point(237, 165);
    static final Rectangle BAR   = new Rectangle(192, 394, 306, 12);
    /** left edge + vertical centre of the "00:00" text */
    static final Point TIME_TEXT = new Point(118, 400);

    // --- control panel ---
    static final Point PANEL         = new Point(84, 441);
    static final Point DIVIDER_LEFT  = new Point(106, 521);
    static final Point DIVIDER_RIGHT = new Point(391, 521);
    static final Point BACK          = new Point(124, 468);
    static final Point NEXT          = new Point(412, 468);
    static final Point LOAD_TEXT     = new Point(124, 534);
    static final Point LINK_TEXT     = new Point(382, 534);
    static final Point PLAY_CIRCLE   = new Point(238, 466);
    static final Point PLAY_ICON     = new Point(274, 496);
}
