package com.reproductor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Library screen: LOAD button, "pending for reading" list with a document and a folder, each with its name
 * under it. Layout only for now; use setDocumentName / setFolderName when uploading works.
 */
final class BooksSection extends Section {

    private static final Color LILAC = new Color(192, 121, 255);
    private static final Gfx.TextStyle TITLE_STYLE =
            Gfx.TextStyle.plain(Gfx.font(Layout.BOOKS_TITLE_SIZE), LILAC);
    private static final Gfx.TextStyle NAME_STYLE =
            Gfx.TextStyle.plain(Gfx.font(16), LILAC);

    private String documentName = "notes.pdf";
    private String folderName = "study folder";

    BooksSection() {
        hotspots.add(new Hotspot("load", new Rectangle(Layout.BOOKS_PILL.x, Layout.BOOKS_PILL.y,
                (int) Gfx.w("global-pink-button.png"), (int) Gfx.h("global-pink-button.png")),
                () -> { }));      // nothing happens yet
    }

    @Override String barSprite() {
        return "books-button.png";
    }

    void setDocumentName(String name) {
        documentName = name;
        repaint();
    }

    void setFolderName(String name) {
        folderName = name;
        repaint();
    }

    @Override void paint(Graphics2D g) {
        // pink LOAD button
        int sink = isPressed("load") ? 2 : 0;
        double px = Layout.BOOKS_PILL.x;
        double py = Layout.BOOKS_PILL.y + sink;
        Gfx.drawAt(g, "global-pink-button.png", px, py);
        Gfx.drawAt(g, "load-text.png",
                px + (Gfx.w("global-pink-button.png") - Gfx.w("load-text.png")) / 2,
                py + (Gfx.h("global-pink-button.png") - Gfx.h("load-text.png")) / 2);

        // list
        Gfx.softFrame(g, Layout.BOOKS_CONTAINER);
        Gfx.text(g, TITLE_STYLE, "pending for reading", Layout.BOOKS_TITLE.x, Layout.BOOKS_TITLE.y, Gfx.Align.LEFT);
        Gfx.drawAt(g, "books-frame.png", Layout.BOOKS_FRAME.x, Layout.BOOKS_FRAME.y);
        drawItem(g, "books-document.png", Layout.BOOKS_DOCUMENT, documentName, Layout.BOOKS_DOC_NAME_CY);
        drawItem(g, "books-folder.png", Layout.BOOKS_FOLDER, folderName, Layout.BOOKS_FOLDER_NAME_CY);

        // bottom purple bar (over the plants)
        Gfx.drawAt(g, "global-purple-frame.png", Layout.BOOKS_BOTTOM_FRAME.x, Layout.BOOKS_BOTTOM_FRAME.y);
    }

    /** image with its name centred under it */
    private void drawItem(Graphics2D g, String sprite, java.awt.Point topLeft, String name, int nameCy) {
        Gfx.drawAt(g, sprite, topLeft.x, topLeft.y);
        String shown = Gfx.fit(g, NAME_STYLE.font(), name, 190);
        Gfx.text(g, NAME_STYLE, shown, topLeft.x + Gfx.w(sprite) / 2, nameCy, Gfx.Align.CENTER);
    }
}
