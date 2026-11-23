package pl.minemod.gui;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import pl.minemod.config.Config;

/**
 * Czcionka Inter dolaczona do moda (assets/minemod/font).
 * Jesli gracz wylaczy ja w ustawieniach, uzywana jest czcionka Minecrafta.
 */
public final class Fonts {
    private static final Style UI = Style.EMPTY.withFont(
            new StyleSpriteSource.Font(Identifier.of("minemod", "ui")));
    private static final Style BOLD = Style.EMPTY.withFont(
            new StyleSpriteSource.Font(Identifier.of("minemod", "ui_bold")));

    private Fonts() {}

    public static Text ui(String s) {
        MutableText t = Text.literal(s);
        return Config.get().vanillaFont ? t : t.setStyle(UI);
    }

    public static Text bold(String s) {
        MutableText t = Text.literal(s);
        return Config.get().vanillaFont ? t.formatted(Formatting.BOLD) : t.setStyle(BOLD);
    }
}
