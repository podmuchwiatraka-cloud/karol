package pl.minemod.modules;

import java.util.List;

public final class ModuleManager {
    private static final String[] BG = {"TŁO: TAK", "TŁO: NIE"};

    public static final MineModule FPS = new MineModule("fps", "FPS", "FPS", MineModule.Category.HUD, true, BG);
    public static final MineModule CPS = new MineModule("cps", "CPS", "CPS", MineModule.Category.HUD, true, BG);
    public static final MineModule PING = new MineModule("ping", "PING", "MS", MineModule.Category.HUD, true, BG);
    public static final MineModule SPRINT = new MineModule("sprint", "STATUS BIEGU", "RUN", MineModule.Category.HUD, true, BG);
    public static final MineModule COORDS = new MineModule("coords", "KOORDYNATY", "XYZ", MineModule.Category.HUD, false, BG);
    public static final MineModule SPEED = new MineModule("speed", "PRĘDKOŚĆ", "SPD", MineModule.Category.HUD, false, BG);
    public static final MineModule DIRECTION = new MineModule("direction", "KIERUNEK", "DIR", MineModule.Category.HUD, false, BG);
    public static final MineModule CLOCK = new MineModule("clock", "ZEGAR", "CLK", MineModule.Category.HUD, false, BG);
    public static final MineModule KEYSTROKES = new MineModule("keystrokes", "KLAWISZE", "KEY", MineModule.Category.HUD, false, BG);
    public static final MineModule ARMOR = new MineModule("armor", "STAN PANCERZA", "ARM", MineModule.Category.HUD, false, BG);
    public static final MineModule POTIONS = new MineModule("potions", "EFEKTY", "POT", MineModule.Category.HUD, false, BG);
    public static final MineModule CROSSHAIR = new MineModule("crosshair", "CELOWNIK", "+", MineModule.Category.VISUAL, false,
            "KRZYŻ", "KROPKA", "PLUS", "OKRĄG");

    public static final List<MineModule> ALL = List.of(
            FPS, CPS, PING, SPRINT, COORDS, SPEED, DIRECTION, CLOCK, KEYSTROKES, ARMOR, POTIONS, CROSSHAIR);

    private ModuleManager() {}
}
