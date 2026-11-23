package pl.minemod.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import pl.minemod.config.Config;
import pl.minemod.gui.Fonts;
import pl.minemod.gui.Gfx;
import pl.minemod.gui.Theme;
import pl.minemod.modules.ModuleManager;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Rysuje moduly HUD (FPS, CPS, klawisze, pancerz...) oraz wlasny celownik. */
public final class HudRenderer {
    private static final Deque<Long> LEFT = new ArrayDeque<>();
    private static final Deque<Long> RIGHT = new ArrayDeque<>();
    private static boolean lastLeft, lastRight;

    private HudRenderer() {}

    public static void register() {
        HudElementRegistry.addLast(Identifier.of("minemod", "info"), (ctx, tick) -> renderInfo(ctx));

        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, original -> (ctx, tick) -> {
            if (ModuleManager.CROSSHAIR.isEnabled()) renderCrosshair(ctx);
            else original.render(ctx, tick);
        });
    }

    private static void pollClicks(MinecraftClient mc) {
        if (mc.currentScreen != null) {
            lastLeft = false;
            lastRight = false;
            return;
        }
        long handle = mc.getWindow().getHandle();
        boolean l = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        long now = System.currentTimeMillis();
        if (l && !lastLeft) LEFT.addLast(now);
        if (r && !lastRight) RIGHT.addLast(now);
        lastLeft = l;
        lastRight = r;
        while (!LEFT.isEmpty() && now - LEFT.peekFirst() > 1000) LEFT.pollFirst();
        while (!RIGHT.isEmpty() && now - RIGHT.peekFirst() > 1000) RIGHT.pollFirst();
    }

    private interface Drawer { void draw(DrawContext ctx, int x, int y); }

    private record Block(int w, int h, Drawer drawer) {}

    private static final int BG = 0x88101014;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final String[] DIRS = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};

    private static double speed = 0, lastX, lastZ;
    private static long lastNs = 0;

    /** Jedna linia tekstu w zaokraglonej plakietce. */
    private static Block textBlock(TextRenderer tr, String s, boolean bg) {
        Text t = Fonts.ui(s);
        int w = tr.getWidth(t) + 12;
        return new Block(w, 15, (ctx, x, y) -> {
            if (bg) Gfx.rrect(ctx, x, y, w, 15, BG);
            ctx.drawTextWithShadow(tr, t, x + 6, y + 4, 0xFFFFFFFF);
        });
    }

    /** Kilka linii tekstu w jednej plakietce. */
    private static Block linesBlock(TextRenderer tr, List<String> lines, boolean bg) {
        List<Text> texts = lines.stream().map(Fonts::ui).toList();
        int max = 0;
        for (Text t : texts) max = Math.max(max, tr.getWidth(t));
        int w = max + 12, h = texts.size() * 12 + 5;
        return new Block(w, h, (ctx, x, y) -> {
            if (bg) Gfx.rrect(ctx, x, y, w, h, BG);
            for (int i = 0; i < texts.size(); i++) {
                ctx.drawTextWithShadow(tr, texts.get(i), x + 6, y + 4 + i * 12, 0xFFFFFFFF);
            }
        });
    }

    private static void key(DrawContext ctx, TextRenderer tr, int x, int y, int w, int h,
                            String label, boolean pressed, boolean bg) {
        int acc = Theme.accent();
        if (pressed) Gfx.rrect(ctx, x, y, w, h, (acc & 0xFFFFFF) | 0xDD000000);
        else if (bg) Gfx.rrect(ctx, x, y, w, h, BG);
        Text t = Fonts.bold(label);
        ctx.drawText(tr, t, x + (w - tr.getWidth(t)) / 2, y + (h - 8) / 2 + 1,
                pressed ? 0xFF101014 : 0xFFFFFFFF, false);
    }

    private static Block keystrokes(MinecraftClient mc, TextRenderer tr, boolean bg) {
        return new Block(58, 56, (ctx, x, y) -> {
            key(ctx, tr, x + 20, y, 18, 18, "W", mc.options.forwardKey.isPressed(), bg);
            key(ctx, tr, x, y + 20, 18, 18, "A", mc.options.leftKey.isPressed(), bg);
            key(ctx, tr, x + 20, y + 20, 18, 18, "S", mc.options.backKey.isPressed(), bg);
            key(ctx, tr, x + 40, y + 20, 18, 18, "D", mc.options.rightKey.isPressed(), bg);
            key(ctx, tr, x, y + 40, 28, 16, "LPM", lastLeft, bg);
            key(ctx, tr, x + 30, y + 40, 28, 16, "PPM", lastRight, bg);
        });
    }

    private static Block armor(MinecraftClient mc, TextRenderer tr, boolean bg) {
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
                EquipmentSlot.FEET, EquipmentSlot.MAINHAND};
        List<ItemStack> stacks = new ArrayList<>();
        List<Text> labels = new ArrayList<>();
        int maxText = 0;
        for (EquipmentSlot slot : slots) {
            ItemStack st = mc.player.getEquippedStack(slot);
            if (st.isEmpty()) continue;
            String label = "";
            if (st.isDamageable()) label = String.valueOf(st.getMaxDamage() - st.getDamage());
            else if (st.getCount() > 1) label = "x" + st.getCount();
            Text t = Fonts.ui(label);
            stacks.add(st);
            labels.add(t);
            maxText = Math.max(maxText, tr.getWidth(t));
        }
        if (stacks.isEmpty()) return null;
        int w = 24 + maxText + 8, h = stacks.size() * 18 + 2;
        return new Block(w, h, (ctx, x, y) -> {
            if (bg) Gfx.rrect(ctx, x, y, w, h, BG);
            for (int i = 0; i < stacks.size(); i++) {
                ctx.drawItem(stacks.get(i), x + 3, y + 1 + i * 18);
                ctx.drawTextWithShadow(tr, labels.get(i), x + 24, y + 5 + i * 18, 0xFFFFFFFF);
            }
        });
    }

    private static String formatDuration(int ticks) {
        if (ticks < 0) return "∞";
        int sec = ticks / 20;
        return (sec / 60) + ":" + String.format("%02d", sec % 60);
    }

    private static void renderInfo(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        pollClicks(mc);
        TextRenderer tr = mc.textRenderer;

        List<Block> blocks = new ArrayList<>();

        if (ModuleManager.FPS.isEnabled()) {
            blocks.add(textBlock(tr, "FPS: " + mc.getCurrentFps(), ModuleManager.FPS.option() == 0));
        }
        if (ModuleManager.CPS.isEnabled()) {
            blocks.add(textBlock(tr, "CPS: " + LEFT.size() + " | " + RIGHT.size(), ModuleManager.CPS.option() == 0));
        }
        if (ModuleManager.PING.isEnabled()) {
            ClientPlayNetworkHandler nh = mc.getNetworkHandler();
            PlayerListEntry entry = nh == null ? null : nh.getPlayerListEntry(mc.player.getUuid());
            int ping = entry == null ? 0 : entry.getLatency();
            blocks.add(textBlock(tr, "PING: " + ping + " ms", ModuleManager.PING.option() == 0));
        }
        if (ModuleManager.SPRINT.isEnabled()) {
            String st = mc.player.isSprinting() ? "SPRINT" : (mc.player.isSneaking() ? "SKRADANIE" : "CHODZENIE");
            blocks.add(textBlock(tr, "[" + st + "]", ModuleManager.SPRINT.option() == 0));
        }
        if (ModuleManager.COORDS.isEnabled()) {
            blocks.add(textBlock(tr, "XYZ: " + mc.player.getBlockX() + " " + mc.player.getBlockY() + " " + mc.player.getBlockZ(),
                    ModuleManager.COORDS.option() == 0));
        }
        if (ModuleManager.SPEED.isEnabled()) {
            long now = System.nanoTime();
            double dt = (now - lastNs) / 1.0e9;
            if (lastNs == 0 || dt > 1.0) {
                lastNs = now;
                lastX = mc.player.getX();
                lastZ = mc.player.getZ();
            } else if (dt >= 0.1) {
                double dx = mc.player.getX() - lastX, dz = mc.player.getZ() - lastZ;
                speed = Math.sqrt(dx * dx + dz * dz) / dt;
                lastNs = now;
                lastX = mc.player.getX();
                lastZ = mc.player.getZ();
            }
            blocks.add(textBlock(tr, String.format("PRĘDKOŚĆ: %.1f b/s", speed), ModuleManager.SPEED.option() == 0));
        }
        if (ModuleManager.DIRECTION.isEnabled()) {
            float yaw = mc.player.getYaw();
            int idx = Math.floorMod(Math.round((yaw + 180f) / 45f), 8);
            blocks.add(textBlock(tr, "KIERUNEK: " + DIRS[idx], ModuleManager.DIRECTION.option() == 0));
        }
        if (ModuleManager.CLOCK.isEnabled()) {
            blocks.add(textBlock(tr, LocalTime.now().format(TIME), ModuleManager.CLOCK.option() == 0));
        }
        if (ModuleManager.POTIONS.isEnabled()) {
            List<String> lines = new ArrayList<>();
            for (StatusEffectInstance e : mc.player.getStatusEffects()) {
                if (lines.size() >= 6) break;
                String name = e.getEffectType().value().getName().getString();
                if (e.getAmplifier() > 0) name += " " + (e.getAmplifier() + 1);
                lines.add(name + "  " + formatDuration(e.getDuration()));
            }
            if (!lines.isEmpty()) blocks.add(linesBlock(tr, lines, ModuleManager.POTIONS.option() == 0));
        }
        if (ModuleManager.ARMOR.isEnabled()) {
            Block b = armor(mc, tr, ModuleManager.ARMOR.option() == 0);
            if (b != null) blocks.add(b);
        }
        if (ModuleManager.KEYSTROKES.isEnabled()) {
            blocks.add(keystrokes(mc, tr, ModuleManager.KEYSTROKES.option() == 0));
        }

        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
        int corner = Math.floorMod(Config.get().corner, 4);
        boolean right = corner == 1 || corner == 3;
        boolean bottom = corner >= 2;
        int margin = 6, gap = 4;
        int y = bottom ? sh - margin : margin;
        for (Block b : blocks) {
            int bx = right ? sw - margin - b.w() : margin;
            int by = bottom ? y - b.h() : y;
            b.drawer().draw(ctx, bx, by);
            y = bottom ? by - gap : by + b.h() + gap;
        }
    }

    private static void renderCrosshair(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!mc.options.getPerspective().isFirstPerson()) return;

        int cx = ctx.getScaledWindowWidth() / 2;
        int cy = ctx.getScaledWindowHeight() / 2;
        int white = 0xFFFFFFFF;
        int shade = 0xFF000000;

        switch (ModuleManager.CROSSHAIR.option()) {
            case 1 -> { // kropka
                ctx.fill(cx - 2, cy - 2, cx + 2, cy + 2, shade);
                ctx.fill(cx - 1, cy - 1, cx + 1, cy + 1, white);
            }
            case 2 -> { // gruby plus
                ctx.fill(cx - 6, cy - 2, cx + 6, cy + 2, shade);
                ctx.fill(cx - 2, cy - 6, cx + 2, cy + 6, shade);
                ctx.fill(cx - 5, cy - 1, cx + 5, cy + 1, white);
                ctx.fill(cx - 1, cy - 5, cx + 1, cy + 5, white);
            }
            case 3 -> { // okrag (kwadratowa ramka)
                ctx.fill(cx - 5, cy - 5, cx + 5, cy + 5, shade);
                ctx.fill(cx - 4, cy - 4, cx + 4, cy + 4, white);
                ctx.fill(cx - 3, cy - 3, cx + 3, cy + 3, shade);
                ctx.fill(cx - 2, cy - 2, cx + 2, cy + 2, 0x00000000);
            }
            default -> { // krzyz z przerwa
                ctx.fill(cx - 7, cy - 1, cx - 2, cy + 1, white);
                ctx.fill(cx + 2, cy - 1, cx + 7, cy + 1, white);
                ctx.fill(cx - 1, cy - 7, cx + 1, cy - 2, white);
                ctx.fill(cx - 1, cy + 2, cx + 1, cy + 7, white);
            }
        }
    }
}
