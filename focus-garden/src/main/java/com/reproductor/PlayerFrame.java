package com.reproductor;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/** The window + the "brain": playlist, buttons, and the link to the audio engine. */
final class PlayerFrame extends JFrame implements MusicSection.Actions, PlayerPanel.WindowActions, AudioEngine.Listener {

    private final MusicSection music = new MusicSection(this);
    /** sections in the same order as the section bar: timer, flower, books, music */
    private final PlayerPanel panel =
            new PlayerPanel(this, 3, new TimerSection(), new FlowerSection(), new BooksSection(), music);
    private final List<Track> playlist = new ArrayList<>();
    private AudioEngine engine;          // null if VLC is not installed

    private int index = -1;              // current song in the playlist
    private long positionMs;
    private long durationMs;
    private boolean resolvingLink;

    PlayerFrame() {
        super("Reproductor");
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));      // transparent corners
        setContentPane(panel);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                close();
            }
        });

        try {
            engine = new AudioEngine(this);
        } catch (Throwable t) {
            music.setTitle("VLC not found");
            showVlcMissing();
        }
    }

    private boolean ready() {
        if (engine == null) {
            showVlcMissing();
            return false;
        }
        return true;
    }

    private void showVlcMissing() {
        JOptionPane.showMessageDialog(this,
                "VLC could not be found.\n\n"
                        + "Install VLC 3.x (64-bit) from videolan.org and restart the player.\n"
                        + "It must be the same type as your Java (64-bit Java needs 64-bit VLC).",
                "VLC needed", JOptionPane.WARNING_MESSAGE);
    }

    // ------------------------------------------------------------------ buttons

    @Override public void playPause() {
        if (!ready()) {
            return;
        }
        if (playlist.isEmpty()) {
            return;                                   // nothing loaded yet
        }
        if (index < 0) {
            startTrack(0);
        } else {
            engine.togglePause();
        }
    }

    @Override public void previous() {
        if (!ready() || playlist.isEmpty()) {
            return;
        }
        if (index < 0 || positionMs > 3000) {
            if (index < 0) {
                startTrack(0);
            } else {
                engine.restart();                     // more than 3 s in: go back to the start of this song
            }
        } else {
            startTrack((index - 1 + playlist.size()) % playlist.size());
        }
    }

    @Override public void next() {
        if (!ready() || playlist.isEmpty()) {
            return;
        }
        startTrack((index + 1) % playlist.size());
    }

    @Override public void load() {
        if (!ready()) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Load music");
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new FileNameExtensionFilter("Audio files",
                "mp3", "wav", "flac", "ogg", "oga", "opus", "m4a", "aac", "wma"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        int firstNew = playlist.size();
        for (File f : chooser.getSelectedFiles()) {
            playlist.add(new Track(LinkResolver.stripExtension(f.getName()), f.getAbsolutePath()));
        }
        if (playlist.size() > firstNew) {
            startTrack(firstNew);
        }
    }

    @Override public void useLink() {
        if (!ready() || resolvingLink) {
            return;
        }
        String link = JOptionPane.showInputDialog(this,
                "Paste a YouTube or Spotify link:", "Use link", JOptionPane.PLAIN_MESSAGE);
        if (link == null || link.isBlank()) {
            return;
        }
        resolvingLink = true;
        music.setTitle("loading...");
        new SwingWorker<Track, Void>() {
            @Override protected Track doInBackground() throws Exception {
                return LinkResolver.resolve(link);
            }

            @Override protected void done() {
                resolvingLink = false;
                try {
                    playlist.add(get());
                    startTrack(playlist.size() - 1);
                } catch (ExecutionException e) {
                    refreshTitle();
                    Throwable cause = e.getCause() == null ? e : e.getCause();
                    JOptionPane.showMessageDialog(PlayerFrame.this, cause.getMessage(),
                            "Could not load the link", JOptionPane.ERROR_MESSAGE);
                } catch (InterruptedException e) {
                    refreshTitle();
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    @Override public void seek(float fraction) {
        if (!ready() || index < 0) {
            return;
        }
        engine.seekToFraction(fraction);
        positionMs = Math.round(fraction * durationMs);
        music.setTime(positionMs, durationMs);
    }

    @Override public void minimize() {
        setState(ICONIFIED);
    }

    @Override public void close() {
        if (engine != null) {
            engine.release();
        }
        dispose();
        System.exit(0);
    }

    // ------------------------------------------------------------------ playlist

    private void startTrack(int i) {
        index = i;
        Track t = playlist.get(i);
        positionMs = 0;
        durationMs = 0;
        music.setTitle(t.title());
        music.setTime(0, 0);
        engine.play(t.mrl());
    }

    private void refreshTitle() {
        music.setTitle(index >= 0 ? playlist.get(index).title() : "no song loaded");
    }

    // ------------------------------------------------------------------ events from the audio engine

    @Override public void onTime(long ms) {
        positionMs = ms;
        music.setTime(positionMs, durationMs);
    }

    @Override public void onLength(long ms) {
        durationMs = ms;
        music.setTime(positionMs, durationMs);
    }

    @Override public void onPlaying(boolean playing) {
        music.setPlaying(playing);
    }

    @Override public void onFinished() {
        if (index >= 0 && index < playlist.size() - 1) {
            startTrack(index + 1);                    // automatically continue with the next song
        } else {
            positionMs = 0;
            music.setTime(0, durationMs);
            music.setPlaying(false);
        }
    }

    @Override public void onError() {
        music.setPlaying(false);
        refreshTitle();
        JOptionPane.showMessageDialog(this, "VLC could not play that song.",
                "Playback error", JOptionPane.ERROR_MESSAGE);
    }
}
