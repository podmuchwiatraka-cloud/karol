package pl.minemod.gui;

import pl.minemod.config.Config;

/** Kolory i przezroczystosc interfejsu. */
public final class Theme {
    public static final String[] ACCENT_NAMES = {
            "BIAŁY", "NIEBIESKI", "ZIELONY", "CZERWONY", "FIOLETOWY", "POMARAŃCZOWY", "TURKUSOWY", "RÓŻOWY"
    };
    private static final int[] ACCENTS = {
            0xFFFFFF, 0x4D9BFF, 0x4ADE80, 0xF87171, 0xA78BFA, 0xFB923C, 0x22D3EE, 0xF472B6
    };
    private static final int[] PANEL_ALPHA = {0xE6, 0xB8, 0x8C, 0x5E};

    private Theme() {}

    public static int accentIndex() {
        int i = Config.get().accent;
        return (i < 0 || i >= ACCENTS.length) ? 0 : i;
    }

    public static int accent() { return 0xFF000000 | ACCENTS[accentIndex()]; }

    public static int transparencyLevel() {
        int t = Config.get().transparency;
        return (t < 0 || t >= PANEL_ALPHA.length) ? 0 : t;
    }

    public static int panelAlpha() { return PANEL_ALPHA[transparencyLevel()]; }

    public static int argb(int alpha, int rgb) { return ((alpha & 0xFF) << 24) | (rgb & 0xFFFFFF); }

    public static int lighten(int argb) {
        int a = argb >>> 24, r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
        r += (255 - r) / 4; g += (255 - g) / 4; b += (255 - b) / 4;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
