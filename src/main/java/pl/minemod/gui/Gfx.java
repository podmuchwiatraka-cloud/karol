package pl.minemod.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/** Male helpery do rysowania zaokraglonych prostokatow i tekstu. */
public final class Gfx {
    private Gfx() {}

    /** Wypelniony prostokat z lekko scietymi rogami. */
    public static void rrect(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x + 1, y, x + w - 1, y + h, color);
        ctx.fill(x, y + 1, x + 1, y + h - 1, color);
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    /** Prostokat z ramka (ramka i wypelnienie sie nie nakladaja, wiec przezroczystosc nie ciemnieje). */
    public static void rbox(DrawContext ctx, int x, int y, int w, int h, int fill, int border) {
        ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);
        ctx.fill(x + 1, y, x + w - 1, y + 1, border);
        ctx.fill(x + 1, y + h - 1, x + w - 1, y + h, border);
        ctx.fill(x, y + 1, x + 1, y + h - 1, border);
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, border);
    }

    public static void centered(DrawContext ctx, TextRenderer tr, Text t, int cx, int y, int color) {
        ctx.drawTextWithShadow(tr, t, cx - tr.getWidth(t) / 2, y, color);
    }
}
