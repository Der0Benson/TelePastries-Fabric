# Portierungsnotizen zu TelePastries Fabric 1.21.1

## Ausgangsbasis

Die Portierung basiert auf dem Branch `1.21` des Originalprojekts [Mrbysco/TelePastries](https://github.com/Mrbysco/TelePastries), Version `0.11.2` für NeoForge und Minecraft 1.21.1. Die Git-Historie, MIT-Lizenz und ursprünglichen Assets bleiben erhalten. Die Mod-ID `telepastries` wurde beibehalten, damit Welten, Gegenstands-IDs, Rezepte, Befehle und Ressourcenpakete denselben Namensraum weiterverwenden.

## Ersetzte Schnittstellen

| NeoForge-System des Originals | Fabric- beziehungsweise Vanilla-Ersatz |
| --- | --- |
| `@Mod` und NeoForge-Mod-Bus | Fabric-Einstiegspunkt über `ModInitializer` |
| `DeferredRegister`, `DeferredBlock`, `DeferredItem` | Vanilla-Registrierung über `Registry.register` |
| NeoForge-Kreativtab | `FabricItemGroup` und Vanilla-Registry |
| `ModConfigSpec` mit TOML | Leichtgewichtige JSON-Konfiguration unter `config/telepastries.json` |
| `ModList.isLoaded` | `FabricLoader.isModLoaded` |
| `CommonHooks.onTravelToDimension` | Serverseitiges `ServerPlayerEntity.teleportTo(TeleportTarget)` |
| `DimensionTransition` | Yarn-API `TeleportTarget` |
| Persistente Forge-Spielerdaten | Server-Spieler-Mixin mit NBT-Lesen und -Schreiben sowie `ServerPlayerEvents.COPY_FROM` |
| `ExplosionEvent.Detonate` | Beim Start konfigurierte Explosionsresistenz der Kuchenblöcke |
| NeoForge-Rezeptbedingungen | Fabric-Ressourcenbedingungen |
| NeoForge-Datengeneratoren | Mitgelieferte und geprüfte JSON-Ressourcen |

## Verhalten der Portierung

- Alle acht Block- und Gegenstands-IDs des Originals sind registriert.
- Teleportation wird ausschließlich für echte `ServerPlayerEntity`-Instanzen auf dem logischen Server ausgeführt.
- Gespeicherte Zielpositionen werden zuerst geprüft. Beim ersten Wechsel ins Ende wird die Endplattform erzeugt. Danach sucht die Mod von der Oberfläche abwärts nach einem sicheren Standort und baut nur im Notfall einen geschützten Ersatzbereich.
- Beim Dimensionswechsel wird die Koordinatenskalierung von Minecraft verwendet.
- Die zuletzt verlassene Position wird je Dimension im Spieler-NBT gespeichert und beim Spielerwechsel nach Tod beziehungsweise Respawn übernommen.
- Widerstandseffekt, Zurücksetzen der Fallhöhe und das Aktualisieren geeigneter Wiedereinstiegspunkte bleiben erhalten.
- Kuchenbisse, Hungerregeln, Kreativmodus, Nachfüllgegenstände, Zurücksetzen, Komparatorausgabe, Platzierungsregeln und Fehlermeldungen werden serverseitig verarbeitet.
- Ein fehlerhafter clientseitiger Server-Spieler-Cast des Originals wurde durch eine ausschließlich serverseitige Änderung ersetzt.
- Der ältere Konfigurationswert `minecraft:bed` wird als beliebiges Vanilla-Bett interpretiert.
- Explosionsschutz wird ohne versionsabhängiges Explosions-Mixin über die Blockeigenschaften umgesetzt.

## Ressourcen und Übersetzungen

Texturen, Blockzustände, Block- und Gegenstandsmodelle, Rezepte, Fortschritte, Beutetabellen sowie die englische, französische und chinesische Übersetzung des Originals werden weiterverwendet. Diese Portierung ergänzt eine vollständige deutsche Übersetzung. Das Twilight-Forest-Rezept verwendet eine Fabric-Ressourcenbedingung und wird nur bei installierter Ziel-Mod geladen.

## Noch offene optionale Integrationen

- Jade-Unterstützung für eine ausdrückliche Anzeige der gegessenen Kuchenstücke
- The-One-Probe-Unterstützung, sofern eine passende Fabric-API für Minecraft 1.21.1 verfügbar ist

Keine Kernfunktion wurde absichtlich ausgelassen.

## Empfohlene Spieltests

- Hin- und Rückreise für alle Vanilla-Dimensionen auf einem dedizierten Server
- Sichere Zielsuche an Weltgrenzen, in Flüssigkeiten und bei blockierten gespeicherten Positionen
- Twilight Forest und The Lost Cities mit den jeweils passenden Fabric-Versionen
- Eigene Kuchen mit Dimensionen anderer Mods und eingeschränkten Y-Grenzen
- Positionsspeicherung nach Abmeldung, Tod, Ende-Abspann und Serverneustart
- Explosionsschutz gegen TNT, Creeper, Betten, Respawnanker und Explosionen anderer Mods
- Nachfüllen und Zurücksetzen im Überlebens- und Kreativmodus

Fabric besitzt keinen allgemeinen Fake-Player-Typ wie NeoForge. Automatisierungsmods, die vollständige Server-Spieler simulieren, sollten deshalb gesondert geprüft werden.
