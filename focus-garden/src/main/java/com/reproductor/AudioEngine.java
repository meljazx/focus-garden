package com.reproductor;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

import javax.swing.SwingUtilities;

/**
 * Thin wrapper around VLC (through vlcj). Plays local files and online streams.
 * All {@link Listener} methods are called on the Swing thread.
 */
final class AudioEngine {

    interface Listener {
        void onTime(long ms);
        void onLength(long ms);
        void onPlaying(boolean playing);
        void onFinished();
        void onError();
    }

    private final MediaPlayerFactory factory;
    private final MediaPlayer player;

    /** @throws RuntimeException if VLC cannot be found on this computer */
    AudioEngine(Listener listener) {
        factory = new MediaPlayerFactory("--no-video");
        player = factory.mediaPlayers().newMediaPlayer();
        player.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override public void playing(MediaPlayer mp) {
                SwingUtilities.invokeLater(() -> listener.onPlaying(true));
            }
            @Override public void paused(MediaPlayer mp) {
                SwingUtilities.invokeLater(() -> listener.onPlaying(false));
            }
            @Override public void stopped(MediaPlayer mp) {
                SwingUtilities.invokeLater(() -> listener.onPlaying(false));
            }
            @Override public void finished(MediaPlayer mp) {
                // never call back into VLC from this thread: hand over to the Swing thread
                SwingUtilities.invokeLater(listener::onFinished);
            }
            @Override public void error(MediaPlayer mp) {
                SwingUtilities.invokeLater(listener::onError);
            }
            @Override public void timeChanged(MediaPlayer mp, long newTime) {
                SwingUtilities.invokeLater(() -> listener.onTime(newTime));
            }
            @Override public void lengthChanged(MediaPlayer mp, long newLength) {
                SwingUtilities.invokeLater(() -> listener.onLength(newLength));
            }
        });
    }

    /** Starts playing a file path or a URL. */
    void play(String mrl) {
        player.media().play(mrl);
    }

    /** Pause if playing, resume otherwise. */
    void togglePause() {
        if (player.status().isPlaying()) {
            player.controls().setPause(true);
        } else {
            player.controls().play();
        }
    }

    void seekToFraction(float fraction) {
        player.controls().setPosition(fraction);
    }

    void restart() {
        player.controls().setTime(0);
    }

    void stop() {
        player.controls().stop();
    }

    void release() {
        player.release();
        factory.release();
    }
}
