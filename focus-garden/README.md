# ------------

please use this space to note what are the updates on the system and specific stuff in the Java code

## Before running (for the player/reproductor funtions)

1. **JDK 17 or newer** and **NetBeans** (any recent version).
2. **VLC 3.x, 64-bit**, installed normally (videolan.org). VLC 4 does not work with this library yet.
3. *(Only for YouTube / Spotify links)* download **yt-dlp.exe** from
   https://github.com/yt-dlp/yt-dlp/releases and put it in the `tools` folder of this project
   (or anywhere on your PATH). Update it from time to time (`yt-dlp -U`): YouTube changes often.

## Open in NetBeans PREFERBABLY (way more easy to code)

File > Open Project > choose the `reproductor` folder (NetBeans shows it with the Maven icon).
Wait for it to download the dependencies (vlcj), then press **Run (F6)**.

## What the buttons do

- In the player (reproductor) basically everything works, the sections are already connected
- There's small animations for each buttom when you interact with them, nothing fancy

## Sections

The section bar switches screens:
-> **clock** = timer
-> **flower** = items and flowers for unlocking
-> **book** = library (pending for reading, upload images, pdfs, documents)
-> **note** = music player (the only one that works).

## IMPORTANT TO CLARIFY

The button of the current section is drawn 2 px lower (`MARK_ACTIVE_SECTION` in `Layout.java`).
The arrows at the sides don't do anything yet (in teh section bar)
The program starts with the player (reproductor) screen, FIX THIS !

Timer, flower and library are layout only: the counter shows 00:00, the hearts bar is full, and the names
(`FlowerSection.setItemName`, `BooksSection.setDocumentName / setFolderName`) are placeholders.

## Tweaking the design (extra notes)

- Every position is in `Layout.java`, grouped by section. In your reference image the pink frame starts higher
  than in the real window (the bar is not drawn there), so `TIMER_SHIFT`, `FLOWER_SHIFT` and `LIBRARY_SHIFT`
  move the content that hangs from the top of the frame down; the plants, purple bars and hearts bar stay at the bottom. Numbers are in PNG pixels.
  Set `SHOW_SECTIONS_BAR = false` to hide the section bar.
- The font is `src/main/resources/fonts/Pixeboy.ttf` (used for every text drawn by the code).
- Images live in `src/main/resources/img`. Replace a PNG with another one of the same size and it updates.
