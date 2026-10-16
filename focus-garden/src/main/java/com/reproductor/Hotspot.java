package com.reproductor;

import java.awt.Rectangle;

/** A clickable area: a name (to know which one is held down), where it is, and what it does. */
record Hotspot(String name, Rectangle area, Runnable action) { }
