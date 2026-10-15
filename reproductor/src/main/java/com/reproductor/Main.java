package com.reproductor;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // default look and feel is fine
        }
        SwingUtilities.invokeLater(() -> new PlayerFrame().setVisible(true));
    }
}
