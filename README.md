# Apple HomeView for Amazon Fire 7 (HomeView OS)

Native Android implementation of Apple's smart-home display (Apple HomeView) tailored for the Amazon Fire 7 tablet (1024x600 landscape display). Transformed from `breadboard-launcher` into a 100% pure Apple HomeView native experience.

---

## 1. Overview & Zweck
HomeView verwandelt das Amazon Fire 7 Tablet in ein natives Apple HomeView Smart Display:
- **Faces Gallery:** 3 Reihen personalisierter Watch Faces im kontinuierlichen, endlosen Marquee-Drift (Reihe 1 links, Reihe 2 rechts, Reihe 3 links) mit 1.0 -> 1.08 Skalierungseffekt.
- **Full Screen Watch Face:** Edge-to-Edge Anzeige (Standard: Dad & Son Foto-Face mit gigantischer transluzenter 10:09 Ziffernglasur). Antippen startet die Gesichtserkennung; dezenter "⊞ Galerie"-Pill führt zurück.
- **Recognizing Flow:** Stufenweises Einblenden von 6 Memoji-Profilen (Oma, Amira, Leo, Mei, Dev, Marius), rotierender Scan-Arc-Ring mit Farbverlauf, Zoom auf Marius und nahtloser Übergang zu Marius Home.
- **Marius Home:** Persönlicher Homescreen mit "GUTEN MORGEN, MARIUS", Dattenfeld/Windeck Wetter-Widget (14°C, Teils sonnig), App-Squircles (Musik, Podcasts, Home, Cookbook/Rezept, Siri) und Kalender-Widget.
- **Siri AI Assistant:** Drop-in Siri Orb mit Sprung-Animation, Wort-für-Wort Transkription ("Siri, wann muss ich zum Sport losfahren?"), Answer Card mit Vektorkarte des Siegtals / Sportpark Windeck über L333 (4,2 km, 7 Min.).
- **Amber Home Dashboard:** Authentisches 3x2 Amber/Orange Thermostat-Dashboard (`21°C` mit interaktiven Chevrons und Wisch-Geste, Analoguhr 10:09, Intercom, Guten Morgen, Türschloss, Licht) sowie Unterseiten für Klimaanlage (Kühlen auf 20°) und Lampen-Dimmer (35% -> 46%).
- **Now Playing (Apple Music):** Album-Artwork von M83 ("Midnight City"), AirPlay Pill ("Marius Zimmer HomePod"), Scrubber `2:03` / `-0:47`, Lautstärkeregler und zeit-synchronisierter Lyrics-Modus mit automatischem Zeilenlauf alle 2,5 Sekunden.
- **Cookbook (Rezept):** Kürbis-Ingwer-Suppe mit Vektorgrafik, Zutatenliste und Schritt-für-Schritt-Zubereitung.

---

## 2. Execution Log & Changelog
- **v3.1.0-homeOS:**
  - **Reines Fullscreen:** Vollständiges Entfernen von `SystemStatusBarView` und `CupBoardDrawer`. Content füllt die vollen 1024x600 Pixel ohne Letterboxing oder Top-Margins.
  - **Immersive Sticky Guard:** `View.OnSystemUiVisibilityChangeListener` erzwingt permanentes Ausblenden der Navigations- und Statusleiste auch bei Benutzerinteraktionen.
  - **Flüssige Apple-Animationen:** Echte Spring- und Decelerate-Übergänge (`SLIDE_UP`, `ZOOM_IN`, `ZOOM_OUT`, `FADE`) für alle App-Öffnungen und -Rücksprünge.
  - **Endloser Marquee Faces Gallery:** `FacesGalleryMarqueeView` mit 3 horizontalen Zeilen im kontinuierlichen Marquee-Drift (~25dp/s) und 1.0 -> 1.08 Grid-Scale.
  - **Amber Home Dashboard 1:1:** Vollständiger Neubau von `HomeDashboardView.java` passend zur Swift-Referenz (Amber Glossy Gradient, `21°C` Thermostat-Card, 10:09 Analoguhr, Intercom/Szenen/Schloss/Licht-Kacheln, AC- und Dimmer-Tabs).
  - **Now Playing & Lyrics:** `AppleMusicView.java` mit "✕"-Close Button, AirPlay Pill und synchronisiertem Lyrics-Viewer.

---

## 3. Installation & Dependencies
- **Zielgerät:** Amazon Fire 7 (7. Generation / Ford / KFAUWI), Android 5.1 (API 22), 1024x600 px.
- **Java/Kotlin Runtime:** Java 17, Gradle 8.4 (Android Gradle Plugin 8.3.0).
- **Core Dependencies:**
  - `org.java-websocket:Java-WebSocket:1.5.6` (Optionale Home Assistant WebSocket API Anbindung).
  - Keine externen Heavyweight UI-Frameworks; 100% native Android Views mit Canvas/Hardware Acceleration.

---

## 4. Parameter & Entities
| Parameter / Konfiguration | Wert | Beschreibung |
| :--- | :--- | :--- |
| **Primary User** | Marius | Personalisierter Display-Nutzer |
| **Location** | Dattenfeld, Windeck | Wetter- und Navigationskontext |
| **Wetter-Kondition** | 14°C, Teils sonnig • H:16° T:8° | Lokales Fixture-Wetter |
| **Siri Query** | "Siri, wann muss ich zum Sport losfahren?" | Ziel: Sportpark Windeck (L333, 4,2 km) |
| **Music Track** | M83 – "Midnight City" | Album: Hurry Up, We're Dreaming |
| **Default Clock Time** | 10:09 (Mittwoch, 1. April) | Apple Marketing Standard-Uhrzeit |
| **Thermostat Temp** | 21°C | Wohnzimmer Sollwert (16°C - 30°C) |

---

## 5. Wartung & Tests
- **Automatisierter CI-Build:** GitHub Actions Workflow `.github/workflows/build-breadboard.yml` baut automatisch den Release-APK bei jedem Push auf `main`.
- **ADB Deployment auf Fire 7 (über 2015 Mac Bridge):**
  ```bash
  ssh mariusschaffrath@192.168.178.42 "adb -s G000KL0265240H5P install -r /tmp/app-release.apk"
  ssh mariusschaffrath@192.168.178.42 "adb -s G000KL0265240H5P shell am start -n me.jxl.breadboard/.MainActivity"
  ```
- **Live Screenshot Inspektion:**
  ```bash
  ssh mariusschaffrath@192.168.178.42 "adb -s G000KL0265240H5P shell screencap -p /sdcard/screen.png && adb -s G000KL0265240H5P pull /sdcard/screen.png /tmp/screen.png"
  ```
