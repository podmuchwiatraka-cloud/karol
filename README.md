# MineMod — Fabric 1.21.11

Projekt przygotowany do automatycznego budowania przez GitHub Actions.

## Budowanie na GitHubie

1. Utwórz nowe repozytorium na GitHubie (np. `MineMod`).
2. Wybierz **Add file → Upload files** i przeciągnij zawartość tego folderu do repozytorium (nie sam folder ZIP).
3. Zatwierdź zmiany przyciskiem **Commit changes**.
4. Otwórz kartę **Actions**. Workflow **Build MineMod JAR** powinien uruchomić się automatycznie.
5. Po zakończeniu wejdź w wykonany workflow i na dole w sekcji **Artifacts** pobierz `MineMod-1.21.11-Fabric`.
6. Rozpakuj pobrane archiwum i skopiuj `minemod-1.0.0.jar` do folderu `.minecraft/mods`.

Wymagania w grze: Minecraft Java 1.21.11, Fabric Loader oraz Fabric API dla 1.21.11. To jest mod kliencki.

Menu moda otwiera się klawiszem **Right Shift**.

## Co jest w menu

- **MODY** – FPS, CPS, ping, status biegu, koordynaty, prędkość, kierunek, zegar, klawisze (WASD + myszka), stan pancerza, efekty mikstur, celownik.
- **KOSMETYKI** – wybór jest zapisywany w configu; renderowanie na postaci w grze jest w przygotowaniu.
- **USTAWIENIA** – kolor akcentu, przezroczystość (4 poziomy), czcionka (Inter / Minecraft), róg ekranu dla HUD.

Config: `.minecraft/config/minemod.json`.

## Czcionka

Mod zawiera czcionkę **Inter** (licencja SIL OFL 1.1, plik `Inter-LICENSE.txt` w `assets/minemod/font`). Nie jest potrzebny żaden texturepack.
