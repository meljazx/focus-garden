package com.reproductor;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * Every position in the window, in "Figma pixels" (the images are exported at 2x,
 * so they are drawn at half their file size).
 * If something is a pixel off compared with your Figma file, change it HERE.
 *
 * Positions marked "visible" are where the top-left of the visible artwork goes
 * (transparent margins of the PNG are ignored).
 */
final class Layout {

    private Layout() { }

    /** Images were exported at 2x. */
    static final int SPRITE_SCALE = 2;

    static final int WIN_W = 614;
    static final int WIN_H = 667;
    /** horizontal centre of the pink frame, shared by the sections */
    static final int CX = 299;

    // =============================================================== window (all sections)

    static final Point MINIMIZE = new Point(471, 15);
    static final Point CLOSE    = new Point(523, 15);

    static final boolean SHOW_SECTIONS_BAR = true;
    /** the button of the section you are in is drawn 2 px lower (set false to turn it off) */
    static final boolean MARK_ACTIVE_SECTION = true;
    static final Point ARROW_LEFT  = new Point(25, 68);
    static final Point ARROW_RIGHT = new Point(540, 68);
    /** in the same order as the sections: timer, flower, books, music */
    static final Point[] SECTION_POSITIONS = {
            new Point(70, 58), new Point(187, 58), new Point(305, 58), new Point(422, 58)};

    // =============================================================== music section

    static final Point COVER     = new Point(86, 153);
    static final Point SONG_NAME = new Point(237, 165);
    static final Rectangle BAR   = new Rectangle(192, 394, 306, 12);
    /** left edge + vertical centre of the "00:00" text */
    static final Point TIME_TEXT = new Point(118, 400);

    static final Point PANEL         = new Point(84, 441);
    static final Point DIVIDER_LEFT  = new Point(106, 521);
    static final Point DIVIDER_RIGHT = new Point(391, 521);
    static final Point BACK          = new Point(124, 468);
    static final Point NEXT          = new Point(412, 468);
    static final Point LOAD_TEXT     = new Point(124, 534);
    static final Point LINK_TEXT     = new Point(382, 534);
    static final Point PLAY_CIRCLE   = new Point(238, 466);
    static final Point PLAY_ICON     = new Point(274, 496);

    // =============================================================== timer / flower / library sections
    //
    // These positions were measured on your reference image. In that image the pink frame starts higher than
    // in the real window (the section bar is not drawn there), so everything that hangs from the top of the
    // frame is moved down by a shift. Things anchored to the bottom (plants, purple bars, hearts bar) are not.

    /** how much the content that hangs from the top of the frame is moved down */
    static final int TIMER_SHIFT   = 7;
    static final int FLOWER_SHIFT  = 35;
    static final int LIBRARY_SHIFT = 35;

    // ----- timer

    /** big white 00:00: horizontal centre + vertical centre of the digits */
    static final Point TIMER_COUNTER = new Point(301, 233 + TIMER_SHIFT);
    static final int TIMER_COUNTER_SIZE = 160;
    /** START FOCUS button (visible top-left) */
    static final Point TIMER_BUTTON = new Point(157, 302 + TIMER_SHIFT);
    /** hearts + progress bar, at the bottom over the plants (visible top-left) */
    static final Point TIMER_BAR = new Point(70, 581);
    /** how full the bar is drawn (the timer is not working yet) */
    static final float TIMER_FILL = 1.0f;
    /** where the pink fill starts inside the bar image, in Figma pixels from the bar's visible top-left */
    static final double TIMER_FILL_DX = 109.5;
    static final double TIMER_FILL_DY = 2.5;

    // ----- flower

    /** pink LOCKED button (visible top-left) */
    static final Point FLOWER_PILL = new Point(386, 134 + FLOWER_SHIFT);
    /** open padlock (visible top-left) */
    static final Point FLOWER_LOCK = new Point(100, 258 + FLOWER_SHIFT);
    /** soft frame around the white card + the "time left" strip */
    static final Rectangle FLOWER_CONTAINER = new Rectangle(234, 209 + FLOWER_SHIFT, 304, 257);
    /** white card (visible top-left) */
    static final Point FLOWER_CARD = new Point(234, 211 + FLOWER_SHIFT);
    /** left edge of the item name, and the vertical centre of its two lines */
    static final int FLOWER_NAME_X = 260;
    static final int FLOWER_NAME_CY1 = 256 + FLOWER_SHIFT;
    static final int FLOWER_NAME_CY2 = 293 + FLOWER_SHIFT;
    static final int FLOWER_NAME_SIZE = 53;
    /** "TIME LEFT TO GET IT:" (visible top-left) */
    static final Point FLOWER_TIME_LABEL = new Point(249, 447 + FLOWER_SHIFT);
    /** "ITEMS" (visible top-left) and the purple bar under it (bottom anchored) */
    static final Point FLOWER_ITEMS_LABEL = new Point(92, 518);
    static final Point FLOWER_BOTTOM_FRAME = new Point(79, 549);

    // ----- library (books)

    /** pink LOAD button (visible top-left) */
    static final Point BOOKS_PILL = new Point(388, 139 + LIBRARY_SHIFT);
    /** soft frame around the list */
    static final Rectangle BOOKS_CONTAINER = new Rectangle(64, 207 + LIBRARY_SHIFT, 475, 248);
    /** "pending for reading": left edge + vertical centre */
    static final Point BOOKS_TITLE = new Point(75, 218 + LIBRARY_SHIFT);
    static final int BOOKS_TITLE_SIZE = 27;
    /** white row (visible top-left) */
    static final Point BOOKS_FRAME = new Point(66, 229 + LIBRARY_SHIFT);
    /** document and folder (visible top-left) */
    static final Point BOOKS_DOCUMENT = new Point(89, 254 + LIBRARY_SHIFT);
    static final Point BOOKS_FOLDER   = new Point(78, 363 + LIBRARY_SHIFT);
    /** vertical centre of the names written under each image */
    static final int BOOKS_DOC_NAME_CY    = 340 + LIBRARY_SHIFT;
    static final int BOOKS_FOLDER_NAME_CY = 449 + LIBRARY_SHIFT;
    /** purple bar at the bottom (bottom anchored) */
    static final Point BOOKS_BOTTOM_FRAME = new Point(79, 549);
}
