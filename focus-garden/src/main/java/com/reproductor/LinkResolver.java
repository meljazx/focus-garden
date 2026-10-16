package com.reproductor;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns a pasted link into something VLC can play.
 *
 *  - YouTube: asks yt-dlp for the direct audio address of the video.
 *  - Spotify: Spotify audio is protected and cannot be played by other programs, so the
 *    song name + artist are read from the Spotify page and the same song is searched on
 *    YouTube instead (through yt-dlp).
 *  - Direct links to audio files (.mp3, .ogg ...) are played as they are.
 *
 * Blocking: call it from a background thread.
 */
final class LinkResolver {

    private static final Pattern PAGE_TITLE = Pattern.compile("<title[^>]*>(.*?)</title>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern DIRECT_AUDIO = Pattern.compile(
            "(?i)^https?://[^?#]+\\.(mp3|ogg|oga|opus|wav|flac|m4a|aac)([?#].*)?$");

    private LinkResolver() { }

    static Track resolve(String input) throws Exception {
        String url = input.trim();
        if (!url.matches("(?i)^https?://.*")) {
            url = "https://" + url;
        }
        String host;
        try {
            host = URI.create(url).getHost();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("That doesn't look like a valid link.");
        }
        host = host == null ? "" : host.toLowerCase();

        if (host.endsWith("youtube.com") || host.equals("youtu.be")) {
            return fromYtDlp(url, null);
        }
        if (host.endsWith("spotify.com")) {
            return fromSpotify(url);
        }
        if (DIRECT_AUDIO.matcher(url).matches()) {
            String name = url.substring(url.lastIndexOf('/') + 1).replaceAll("[?#].*$", "");
            return new Track(URLDecoder.decode(stripExtension(name), StandardCharsets.UTF_8), url);
        }
        throw new IllegalArgumentException("Use a YouTube link, a Spotify track link or a direct link to an audio file.");
    }

    // ------------------------------------------------------------------ Spotify

    private static Track fromSpotify(String url) throws Exception {
        if (!url.contains("/track/")) {
            throw new IllegalArgumentException("Only single Spotify songs work (links that contain /track/).");
        }
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120 Safari/537.36")
                .header("Accept-Language", "en")
                .build();
        String html = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

        Matcher m = PAGE_TITLE.matcher(html);
        if (!m.find()) {
            throw new IOException("Could not read the song name from the Spotify page.");
        }
        // usually: "Song - song and lyrics by Artist | Spotify"
        String raw = unescape(m.group(1)).replaceAll("\\s*\\|\\s*Spotify\\s*$", "").trim();
        String song = raw;
        String artist = "";
        int cut = raw.toLowerCase().indexOf(" - song and lyrics by ");
        if (cut > 0) {
            song = raw.substring(0, cut).trim();
            artist = raw.substring(cut + " - song and lyrics by ".length()).trim();
        } else if (raw.toLowerCase().indexOf(" - song by ") > 0) {
            cut = raw.toLowerCase().indexOf(" - song by ");
            song = raw.substring(0, cut).trim();
            artist = raw.substring(cut + " - song by ".length()).trim();
        }
        String shownTitle = artist.isEmpty() ? song : song + " - " + artist;
        return fromYtDlp("ytsearch1:" + (artist + " " + song).trim(), shownTitle);
    }

    private static String unescape(String s) {
        return s.replace("&amp;", "&").replace("&quot;", "\"").replace("&#x27;", "'")
                .replace("&#39;", "'").replace("&lt;", "<").replace("&gt;", ">");
    }

    // ------------------------------------------------------------------ yt-dlp

    private static Track fromYtDlp(String target, String forcedTitle) throws Exception {
        List<String> cmd = List.of(ytDlpCommand(), "--no-playlist", "--no-warnings",
                "-f", "bestaudio/best",
                "--print", "%(title)s", "--print", "%(url)s",
                target);
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.environment().put("PYTHONIOENCODING", "utf-8");
        Process process;
        try {
            process = pb.start();
        } catch (IOException e) {
            throw new IllegalStateException("yt-dlp was not found.\n"
                    + "Download yt-dlp.exe from github.com/yt-dlp/yt-dlp/releases and put it in the\n"
                    + "\"tools\" folder next to the project (or add it to your PATH).");
        }
        CompletableFuture<String> err = CompletableFuture.supplyAsync(() -> readAll(process));
        String out = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).strip();
        if (!process.waitFor(60, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IOException("yt-dlp took too long.");
        }
        if (process.exitValue() != 0 || out.isEmpty()) {
            String msg = err.getNow("").strip();
            String last = msg.isEmpty() ? "unknown error" : msg.substring(msg.lastIndexOf('\n') + 1);
            throw new IOException("yt-dlp could not get that song: " + last);
        }
        String[] lines = out.split("\\R");
        String title = forcedTitle != null ? forcedTitle : lines[0].trim();
        String streamUrl = lines[lines.length - 1].trim();
        return new Track(title, streamUrl);
    }

    private static String readAll(Process p) {
        try {
            return new String(p.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    /** Looks for tools/yt-dlp(.exe) next to the project first, then for "yt-dlp" on the PATH. */
    private static String ytDlpCommand() {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        Path local = Path.of("tools", windows ? "yt-dlp.exe" : "yt-dlp");
        return Files.isRegularFile(local) ? local.toAbsolutePath().toString() : "yt-dlp";
    }

    static String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }
}
