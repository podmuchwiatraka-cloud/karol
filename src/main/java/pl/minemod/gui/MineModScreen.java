package pl.minemod.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import pl.minemod.config.Config;
import pl.minemod.cosmetics.Cosmetic;
import pl.minemod.cosmetics.CosmeticRegistry;
import pl.minemod.cosmetics.CosmeticType;
import pl.minemod.modules.MineModule;
import pl.minemod.modules.ModuleManager;

import java.util.ArrayList;
import java.util.List;

/** Menu MineMod w stylu Lunar Client: przezroczysty panel, karty w siatce, akcent kolorystyczny. */
public class MineModScreen extends Screen {
    private static final int PANEL_W = 460;
    private static final int PANEL_H = 276;
    private static final int PER_PAGE = 6;
    private static final int CARD_W = 138;
    private static final int CARD_H = 88;

    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFFB4B4BE;
    private static final int DARK = 0xFF101014;
    private static final String[] CORNERS = {"LEWA GÓRA", "PRAWA GÓRA", "LEWY DÓŁ", "PRAWY DÓŁ"};

    private enum Tab { MODS, COSMETICS, SETTINGS }

    private record Visual(int x, int y, int w, int h, String label, boolean filled) {}
    private record Card(int x, int y, String tag, String name, String sub, boolean active) {}
    private record Row(int x, int y, int w, int h, String label, String desc, int swatch) {}

    private final List<Visual> visuals = new ArrayList<>();
    private final List<Card> cards = new ArrayList<>();
    private final List<Row> rows = new ArrayList<>();

    private Tab tab = Tab.MODS;
    private int page = 0;
    private MineModule.Category modFilter = null;
    private CosmeticType cosFilter = null;
    private String pagerText = "";
    private String footer = "";

    public MineModScreen() {
        super(Text.literal("MineMod"));
    }

    private int left() { return (width - PANEL_W) / 2; }
    private int top() { return (height - PANEL_H) / 2; }

    /** Niewidzialny przycisk (obsluguje klikniecie) + wlasny wyglad rysowany w render(). */
    private void btn(int x, int y, int w, int h, String label, boolean filled, Runnable action) {
        visuals.add(new Visual(x, y, w, h, label, filled));
        ButtonWidget b = ButtonWidget.builder(Text.literal(label), bw -> action.run())
                .dimensions(x, y, w, h).build();
        b.setAlpha(0f);
        addDrawableChild(b);
    }

    @Override
    protected void init() {
        clearChildren();
        visuals.clear();
        cards.clear();
        rows.clear();
        pagerText = "";
        footer = "";

        int x = left(), y = top();

        // pasek gorny: zakladki + zamkniecie
        String[] tabNames = {"MODY", "KOSMETYKI", "USTAWIENIA"};
        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            final Tab t = tabs[i];
            btn(x + 126 + i * 88, y + 9, 84, 16, tabNames[i], tab == t, () -> { tab = t; page = 0; init(); });
        }
        btn(x + PANEL_W - 28, y + 9, 16, 16, "X", false, this::close);

        int total = 0;
        if (tab == Tab.MODS) {
            footer = "Prawy Shift – otwórz menu";
            btn(x + 14, y + 42, 62, 16, "WSZYSTKIE", modFilter == null, () -> { modFilter = null; page = 0; init(); });
            int i = 1;
            for (MineModule.Category cat : MineModule.Category.values()) {
                btn(x + 14 + i * 66, y + 42, 62, 16, cat.label, modFilter == cat, () -> { modFilter = cat; page = 0; init(); });
                i++;
            }
            List<MineModule> mods = ModuleManager.ALL.stream()
                    .filter(m -> modFilter == null || m.category == modFilter).toList();
            total = mods.size();
            clampPage(total);
            int start = page * PER_PAGE, end = Math.min(total, start + PER_PAGE);
            for (int k = start; k < end; k++) {
                MineModule m = mods.get(k);
                int cx = x + 14 + ((k - start) % 3) * (CARD_W + 9);
                int cy = y + 64 + ((k - start) / 3) * (CARD_H + 6);
                cards.add(new Card(cx, cy, m.tag, m.name, "", m.isEnabled()));
                btn(cx + 7, cy + 54, CARD_W - 14, 15, m.optionLabel(), false, () -> { m.cycleOption(); init(); });
                btn(cx + 7, cy + 71, CARD_W - 14, 15, m.isEnabled() ? "WŁĄCZONY" : "WYŁĄCZONY", m.isEnabled(), () -> { m.toggle(); init(); });
            }
        } else if (tab == Tab.COSMETICS) {
            footer = "Podgląd – w grze wkrótce";
            btn(x + 14, y + 42, 62, 16, "WSZYSTKIE", cosFilter == null, () -> { cosFilter = null; page = 0; init(); });
            int i = 1;
            for (CosmeticType t : CosmeticType.values()) {
                btn(x + 14 + i * 68, y + 42, 64, 16, t.label.toUpperCase(), cosFilter == t, () -> { cosFilter = t; page = 0; init(); });
                i++;
            }
            List<Cosmetic> list = CosmeticRegistry.all().stream()
                    .filter(c -> cosFilter == null || c.type() == cosFilter).toList();
            total = list.size();
            clampPage(total);
            int start = page * PER_PAGE, end = Math.min(total, start + PER_PAGE);
            for (int k = start; k < end; k++) {
                Cosmetic c = list.get(k);
                int cx = x + 14 + ((k - start) % 3) * (CARD_W + 9);
                int cy = y + 64 + ((k - start) / 3) * (CARD_H + 6);
                String tag = c.type().name().substring(0, Math.min(4, c.type().name().length()));
                boolean on = CosmeticRegistry.isEquipped(c);
                cards.add(new Card(cx, cy, tag, c.name().toUpperCase(), c.type().label.toUpperCase(), on));
                btn(cx + 7, cy + 71, CARD_W - 14, 15, on ? "ZAŁOŻONE" : "ZAŁÓŻ", on, () -> { CosmeticRegistry.toggle(c); init(); });
            }
        } else {
            footer = "Zmiany zapisują się automatycznie";
            Config cfg = Config.get();
            int rx = x + 14, rw = PANEL_W - 28, rh = 34;
            int ry = y + 46;
            int bx = rx + rw - 152;

            rows.add(new Row(rx, ry, rw, rh, "KOLOR AKCENTU", "Kolor aktywnych przycisków i HUD", Theme.accent()));
            btn(bx, ry + 9, 140, 16, Theme.ACCENT_NAMES[Theme.accentIndex()], false,
                    () -> { cfg.accent = (Theme.accentIndex() + 1) % Theme.ACCENT_NAMES.length; Config.save(); init(); });
            ry += rh + 6;

            rows.add(new Row(rx, ry, rw, rh, "PRZEZROCZYSTOŚĆ", "Im wyższy poziom, tym bardziej widać grę", 0));
            btn(bx, ry + 9, 140, 16, "POZIOM " + (Theme.transparencyLevel() + 1) + " / 4", false,
                    () -> { cfg.transparency = (Theme.transparencyLevel() + 1) % 4; Config.save(); init(); });
            ry += rh + 6;

            rows.add(new Row(rx, ry, rw, rh, "CZCIONKA", "Inter (z moda) lub czcionka Minecrafta", 0));
            btn(bx, ry + 9, 140, 16, cfg.vanillaFont ? "MINECRAFT" : "INTER", false,
                    () -> { cfg.vanillaFont = !cfg.vanillaFont; Config.save(); init(); });
            ry += rh + 6;

            int corner = Math.floorMod(cfg.corner, 4);
            rows.add(new Row(rx, ry, rw, rh, "POZYCJA HUD", "Róg ekranu dla FPS, CPS, klawiszy itd.", 0));
            btn(bx, ry + 9, 140, 16, CORNERS[corner], false,
                    () -> { cfg.corner = (Math.floorMod(cfg.corner, 4) + 1) % 4; Config.save(); init(); });
        }

        int maxPage = Math.max(0, (total - 1) / PER_PAGE);
        if (maxPage > 0) {
            int cxm = x + PANEL_W / 2;
            btn(cxm - 52, y + 254, 22, 15, "<", false, () -> { if (page > 0) page--; init(); });
            btn(cxm + 30, y + 254, 22, 15, ">", false, () -> { if (page < maxPage) page++; init(); });
            pagerText = (page + 1) + " / " + (maxPage + 1);
        }
    }

    private void clampPage(int total) {
        int maxPage = Math.max(0, (total - 1) / PER_PAGE);
        if (page > maxPage) page = maxPage;
        if (page < 0) page = 0;
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0x50000000);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);

        int x = left(), y = top();
        int pa = Theme.panelAlpha();
        int acc = Theme.accent();
        int cardAlpha = Math.max(0x66, pa - 0x20);

        // panel + pasek gorny
        Gfx.rbox(ctx, x, y, PANEL_W, PANEL_H, Theme.argb(pa, 0x12121A), Theme.argb(0xC0, 0x44445A));
        ctx.fill(x + 1, y + 1, x + PANEL_W - 1, y + 34, Theme.argb(Math.min(0xFF, pa + 0x10), 0x0A0A10));
        ctx.fill(x + 1, y + 34, x + PANEL_W - 1, y + 35, Theme.argb(0xC0, 0x44445A));

        ctx.drawTextWithShadow(textRenderer, Fonts.bold("MINEMOD"), x + 14, y + 13, acc);

        // karty
        for (Card c : cards) {
            Gfx.rbox(ctx, c.x(), c.y(), CARD_W, CARD_H, Theme.argb(cardAlpha, 0x1E1E28),
                    c.active() ? Theme.argb(0xE0, acc & 0xFFFFFF) : Theme.argb(0xA0, 0x4A4A5C));
            Gfx.rbox(ctx, c.x() + 49, c.y() + 8, 40, 26, Theme.argb(0x90, 0x000000), Theme.argb(0x60, 0x6A6A80));
            Gfx.centered(ctx, textRenderer, Fonts.bold(c.tag()), c.x() + 69, c.y() + 17, c.active() ? acc : GRAY);
            Gfx.centered(ctx, textRenderer, Fonts.bold(c.name()), c.x() + CARD_W / 2, c.y() + 39, WHITE);
            if (!c.sub().isEmpty()) {
                Gfx.centered(ctx, textRenderer, Fonts.ui(c.sub()), c.x() + CARD_W / 2, c.y() + 52, GRAY);
            }
        }

        // wiersze ustawien
        for (Row r : rows) {
            Gfx.rbox(ctx, r.x(), r.y(), r.w(), r.h(), Theme.argb(cardAlpha, 0x1E1E28), Theme.argb(0xA0, 0x4A4A5C));
            ctx.drawTextWithShadow(textRenderer, Fonts.bold(r.label()), r.x() + 12, r.y() + 7, WHITE);
            ctx.drawTextWithShadow(textRenderer, Fonts.ui(r.desc()), r.x() + 12, r.y() + 19, GRAY);
        }

        // przyciski
        for (Visual v : visuals) {
            boolean hover = mouseX >= v.x() && mouseX < v.x() + v.w() && mouseY >= v.y() && mouseY < v.y() + v.h();
            int bg, fg, border;
            if (v.filled()) {
                bg = hover ? Theme.lighten(acc) : acc;
                fg = DARK;
                border = bg;
            } else {
                bg = hover ? 0xCC40405A : 0xAA24242E;
                fg = 0xFFEDEDF2;
                border = 0xA0555568;
            }
            Gfx.rbox(ctx, v.x(), v.y(), v.w(), v.h(), bg, border);
            Text label = v.filled() ? Fonts.bold(v.label()) : Fonts.ui(v.label());
            ctx.drawText(textRenderer, label, v.x() + (v.w() - textRenderer.getWidth(label)) / 2,
                    v.y() + (v.h() - 8) / 2 + 1, fg, false);
        }

        // probka koloru akcentu w ustawieniach
        for (Row r : rows) {
            if (r.swatch() != 0) {
                ctx.fill(r.x() + r.w() - 146, r.y() + 12, r.x() + r.w() - 136, r.y() + 22, r.swatch());
            }
        }

        if (!footer.isEmpty()) {
            ctx.drawTextWithShadow(textRenderer, Fonts.ui(footer), x + 14, y + PANEL_H - 19, GRAY);
        }
        if (!pagerText.isEmpty()) {
            Gfx.centered(ctx, textRenderer, Fonts.ui(pagerText), x + PANEL_W / 2, y + 258, GRAY);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() { return false; }
}
