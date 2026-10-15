# Reproductor

Pastel pixel music player (Java Swing + VLC). Built from the Figma export.

## Before running

1. **JDK 17 or newer** and **NetBeans** (any recent version).
2. **VLC 3.x, 64-bit**, installed normally (videolan.org). VLC 4 does not work with this library yet.
3. *(Only for YouTube / Spotify links)* download **yt-dlp.exe** from
   https://github.com/yt-dlp/yt-dlp/releases and put it in the `tools` folder of this project
   (or anywhere on your PATH). Update it from time to time (`yt-dlp -U`): YouTube changes often.

## Open in NetBeans

File > Open Project > choose the `reproductor` folder (NetBeans shows it with the Maven icon).
Wait for it to download the dependencies (vlcj), then press **Run (F6)**.

## What the buttons do

- **Play** (big circle): play / pause.
- **Rewind**: restarts the song if it has been playing more than 3 s, otherwise goes to the previous song.
- **Forward**: next song.
- **LOAD**: pick one or more audio files from your computer (added to the playlist, the first new one plays).
- **USE LINK**: paste a YouTube link, a Spotify *track* link, or a direct link to an audio file.
  Spotify audio can't be played outside Spotify, so the song is looked up on YouTube by name + artist.
- **Progress bar**: click or drag to jump to a point in the song.
- **Time** (bottom-left of the cover): elapsed time. Click it to show the total duration instead.
- Drag the window by clicking any empty part of it. Minimize and close (top right) work.
  The section bar (clock, flower, book, note) is only drawn for now.

## Tweaking the design

- Every position is in `Layout.java`. Numbers are in Figma pixels (the PNGs are 2x).
  Set `SHOW_SECTIONS_BAR = false` to hide the section bar.
- Your pixel font: copy the `.ttf` to `src/main/resources/fonts/pixel.ttf` and the song name
  and the time will use it automatically (otherwise a normal monospace font is used).
- Images live in `src/main/resources/img`. Replace a PNG with another one of the same size and it updates.
