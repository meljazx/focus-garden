package com.reproductor;

/** One song in the playlist: a display name and where VLC can play it from (file path or URL). */
record Track(String title, String mrl) { }
