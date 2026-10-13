<p align="center">
  <img src="docs/banner.png" alt="Gatherlist" width="100%">
</p>

<p align="center">
  <a href="https://github.com/darmixsuper-ai/pagoda-mod/releases/latest"><img alt="Download" src="https://img.shields.io/github/v/release/darmixsuper-ai/pagoda-mod?label=download&style=for-the-badge&color=5fb3a1"></a>
  <img alt="Minecraft 26.2" src="https://img.shields.io/badge/minecraft-26.2-62ba4a?style=for-the-badge">
  <img alt="Fabric" src="https://img.shields.io/badge/loader-fabric-dbd0b4?style=for-the-badge">
  <img alt="Client side" src="https://img.shields.io/badge/side-client%20only-8c93a0?style=for-the-badge">
</p>

**Gatherlist** keeps track of the materials for your next build. Make a list once, go gather, and always see what is still missing – in a small window or right on your HUD.

You found a build tutorial with a material list in the comments? Copy it, click **Paste list**, done.

---

## Features

- 📋 **Your own lists** – as many projects as you like. Pick items from every item in the game and set the amounts.
- 📥 **Paste a list** – copy a material list (for example from a YouTube comment) and paste it straight in. Lines like `1910 spruce logs`, `64x Oak Planks` or `Torch: 12` are recognised automatically.
- 🔎 **Search** – find any item in your list instantly.
- 📦 **Counts everything you own** – your inventory, the shulker boxes in it, and every chest, barrel, shulker box and ender chest you open. Chests you break are forgotten again, so nothing is counted twice.
- 🖥️ **HUD** – progress bar and the next items you still need, in the top-right corner. Turn it on or off in the window.
- ✅ **Show or hide finished items** with one click.
- 🌍 **Works on every server** – it only runs on your side. No plugin, no permissions, nothing to install on the server.
- 🇬🇧 🇩🇪 English and German.

## How to use

| | |
|---|---|
| **Open the window** | Key right of `0` (US layout: `-`). Change it under *Options → Controls → Gatherlist*. |
| **New list** | `+ New`, give it a name, click items on the left to add them. **Shift-click** adds 64. |
| **Change amounts** | Click the number to type it, or use `-` / `+` (Shift = 64). `x` removes the item. |
| **Paste a list** | Copy the list, then `Paste list` in the editor. Unknown lines are skipped and counted. |
| **Switch lists** | `<` and `>` next to the project name. |
| **Your chests** | Open each storage chest once. Gatherlist remembers what is inside, per server or world. |

An example list is included: the materials for Cortezerino's *Fire Pagoda* tutorial.

## Install

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft **26.2**.
2. Put [Fabric API](https://modrinth.com/mod/fabric-api) into your `mods` folder.
3. Download **`gatherlist-26.2.jar`** from [Releases](https://github.com/darmixsuper-ai/pagoda-mod/releases/latest) and put it into `mods` as well.

## FAQ

**Does it work on servers where I have no rights?**
Yes. Gatherlist only reads what your own game already knows. The server never notices it.

**Why is a chest still counted after someone else emptied it?**
Gatherlist only sees a chest's contents while you have it open. Open the chest again and the count is right. If you reorganised everything, click *Forget chests* and open your storage chests once.

**Where is my data?**
In `.minecraft/config/gatherlist/` – `projects.json` holds your lists, `chests.json` the remembered chests.

## Build it yourself

```bash
gradle build
```

Needs Java 25. The jar ends up in `build/libs/`. Every push to `main` is built automatically by GitHub Actions and published under *Releases*.

## License

[MIT](LICENSE) – free to use, change and share.

---

<details>
<summary><b>🇩🇪 Auf Deutsch</b></summary>

**Gatherlist** merkt sich, welche Materialien du für deinen nächsten Bau brauchst. Liste einmal anlegen, sammeln gehen und immer sehen, was noch fehlt – im kleinen Fenster oder direkt im HUD.

- **Eigene Listen:** beliebig viele Projekte, Items aus dem ganzen Spiel, Mengen frei einstellbar.
- **Liste einfügen:** Materialliste kopieren (z. B. aus einem YouTube-Kommentar) und mit *Liste einfügen* übernehmen.
- **Suche, HUD, Fertige ein-/ausblenden** direkt im Fenster.
- **Zählt alles:** Inventar, Shulker-Kisten und jede Truhe, die du öffnest. Abgebaute Truhen werden vergessen.
- **Läuft auf jedem Server**, auch ohne Rechte – alles passiert nur bei dir.

**Öffnen:** Taste rechts neben der `0`. Ändern unter *Optionen → Steuerung → Gatherlist*.

**Installieren:** Fabric Loader und Fabric API für 26.2, dann `gatherlist-26.2.jar` aus den [Releases](https://github.com/darmixsuper-ai/pagoda-mod/releases/latest) in den `mods`-Ordner.

</details>
