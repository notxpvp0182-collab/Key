# KeystrokesHUD — Minecraft 1.21.4 Fabric Mod

A premium, fully customisable **Keystrokes + CPS + FPS HUD** for Minecraft 1.21.4.  
Built with Fabric, optimised for low-end devices, and designed to look great.

---

## ✨ Features

| Feature | Details |
|---|---|
| **Keystroke display** | W / A / S / D with real-time press animations |
| **Mouse buttons** | LMB & RMB with live CPS counter |
| **FPS counter** | Colour-coded (green→red), togglable |
| **Drag & drop** | Every element repositionable independently |
| **Rainbow mode** | Smooth HSB cycling on backgrounds and borders |
| **Per-element colours** | Text colour, border colour, pressed colour — all per element |
| **Opacity control** | Background opacity 0–100 % |
| **Borders** | Toggleable, thickness 1–4 px |
| **Rounded corners** | Configurable radius 0–8 px |
| **Lock / Grid snap** | Lock positions or snap to configurable grid |
| **Performance mode** | Disables animations & rainbow for max FPS |
| **Auto-save** | All settings saved to `config/keystrokeshud.json` |

---

## 🎮 Controls

| Action | Default |
|---|---|
| Open config menu | **K** (rebindable in Options → Controls) |
| Close menu | **Escape** |

---

## 🛠 Build Instructions

### Prerequisites

- **Java 21** (Temurin / Adoptium recommended)
- **Git**
- Internet connection (Gradle downloads dependencies automatically)

### Steps

```bash
# 1. Clone / unzip the project
cd keystrokeshud

# 2. Generate IDE run configs (optional but recommended)
./gradlew genSources        # IntelliJ: also run genIntellijRuns

# 3. Build the mod JAR
./gradlew build

# 4. Output JAR
#    build/libs/keystrokeshud-1.0.0.jar
```

On Windows, replace `./gradlew` with `gradlew.bat`.

### IntelliJ IDEA Setup

1. **File → Open** → select the `keystrokeshud` folder  
2. Wait for Gradle sync to complete  
3. Run `./gradlew genIntellijRuns` in the terminal  
4. Reload Gradle (Gradle panel → refresh icon)  
5. Select the **Minecraft Client** run config and press ▶

### VS Code Setup

1. Install the **Extension Pack for Java** (Microsoft)  
2. Open the `keystrokeshud` folder  
3. The Gradle extension will auto-detect `build.gradle`  
4. Run task: **gradle: build**

---

## 📁 Project Structure

```
keystrokeshud/
├── build.gradle                    ← Fabric Loom build config
├── gradle.properties               ← Version pins
├── settings.gradle
├── gradlew / gradlew.bat           ← Gradle wrapper scripts
├── gradle/wrapper/
│   └── gradle-wrapper.properties
└── src/main/
    ├── java/com/keystrokeshud/
    │   ├── KeystrokesHudClient.java   ← Mod entrypoint
    │   ├── config/
    │   │   └── HudConfig.java         ← All settings + JSON save/load
    │   ├── hud/
    │   │   └── HudManager.java        ← Tick/frame coordinator
    │   ├── input/
    │   │   ├── InputTracker.java      ← Key states + CPS sliding window
    │   │   └── FpsTracker.java        ← FPS value cache
    │   ├── mixin/
    │   │   ├── MouseMixin.java        ← (placeholder, polling used instead)
    │   │   └── GameRendererMixin.java ← (placeholder)
    │   ├── render/
    │   │   └── HudRenderer.java       ← All drawing logic
    │   └── screen/
    │       ├── HudConfigScreen.java   ← Main settings UI + drag system
    │       └── ColorEditorScreen.java ← RGB colour pickers
    └── resources/
        ├── fabric.mod.json
        ├── keystrokeshud.mixins.json
        └── assets/keystrokeshud/
            └── lang/en_us.json
```

---

## ⚙️ Configuration File

Located at: `.minecraft/config/keystrokeshud.json`

The file is auto-created on first run and auto-saved whenever settings change.  
You can edit it manually — changes take effect on next game launch.

Key fields:

```jsonc
{
  "showW": true,            // Toggle individual key visibility
  "showFPS": true,
  "showCPS": true,
  "xW": 10, "yW": 100,     // Position of W key (pixels from top-left)
  "colorTextW": -1,         // ARGB colour packed as signed int (0xFFFFFFFF = white)
  "colorPressed": -16777012,// Colour when key is held
  "backgroundOpacity": 0.6, // 0.0 – 1.0
  "showBorders": true,
  "borderThickness": 1,
  "cornerRadius": 4,
  "rainbowMode": false,
  "rainbowSpeed": 1.0,
  "lockPositions": false,
  "snapToGrid": false,
  "gridSize": 8,
  "performanceMode": false
}
```

---

## 🔧 Compatibility

- Minecraft **1.21.4**
- Fabric Loader **≥ 0.16.0**
- Java **21**
- Compatible with **OptiFabric**, **Sodium**, **Iris**, and most other client mods
- ModMenu compatible (menu opens via keybind or ModMenu button)

---

## 📈 Performance Notes

| Optimisation | Implementation |
|---|---|
| CPS tracking | O(1) sliding-window deque; eviction runs at 20 Hz not per frame |
| FPS string | Pre-built once per tick, never allocated per frame |
| Key state | Polled from Minecraft's own KeyBinding (no extra event hooks) |
| Rendering | Only visible elements rendered; no overdraw |
| Animations | Fixed-step lerp — no delta-time heap allocation |
| Rainbow | Hue accumulated once per frame; HSB→RGB computed once per element |
| Config save | Written to disk only on user action, not per tick |

---

## 📜 License

MIT © KeystrokesHUD Contributors
