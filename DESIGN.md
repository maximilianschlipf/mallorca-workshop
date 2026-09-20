---
name: SimplyTest Academy
description: Das disziplinierte Planungsbrett — ein ruhiges Arbeitsgerät für Schulungskatalog, Termine und Trainerqualifikationen.
colors:
  accent: "#006fae"
  accent-hover: "#005d92"
  accent-contrast: "#ffffff"
  header-text: "#ffffff"
  brand-highlight: "#ff6633"
  seam-orange: "#ea6839"
  seam-blue: "#1c85c3"
  text: "#171942"
  muted: "#565d70"
  surface: "#e7ecf0"
  surface-raised: "#ffffff"
  line: "rgba(23, 25, 66, 0.16)"
  success-text: "#175c3a"
  success-surface: "#eaf8f0"
  danger-text: "#8e1f1b"
  danger-surface: "#fff0ef"
typography:
  display:
    fontFamily: "Roboto, Arial, sans-serif"
    fontSize: "clamp(2.15rem, 4vw, 3.6rem)"
    fontWeight: 700
    lineHeight: 1.02
    letterSpacing: "-0.04em"
  headline:
    fontFamily: "Roboto, Arial, sans-serif"
    fontSize: "clamp(1.45rem, 2.2vw, 2rem)"
    fontWeight: 700
    lineHeight: 1.08
    letterSpacing: "-0.04em"
  title:
    fontFamily: "Roboto, Arial, sans-serif"
    fontSize: "clamp(1.35rem, 2.5vw, 1.75rem)"
    fontWeight: 650
    lineHeight: 1.2
    letterSpacing: "-0.025em"
  body:
    fontFamily: "Roboto, Arial, sans-serif"
    fontSize: "1rem"
    fontWeight: 400
    lineHeight: 1.55
    letterSpacing: "normal"
  label:
    fontFamily: "Roboto, Arial, sans-serif"
    fontSize: "0.76rem"
    fontWeight: 700
    lineHeight: 1.2
    letterSpacing: "0.02em"
rounded:
  sm: "6px"
  md: "8px"
  lg: "10px"
spacing:
  xs: "6px"
  sm: "8px"
  md: "16px"
  lg: "24px"
  xl: "38px"
components:
  button-primary:
    backgroundColor: "{colors.accent}"
    textColor: "{colors.accent-contrast}"
    typography: "{typography.label}"
    rounded: "{rounded.sm}"
    padding: "0 22px"
    height: "48px"
  button-primary-hover:
    backgroundColor: "{colors.accent-hover}"
    textColor: "{colors.accent-contrast}"
  button-secondary:
    backgroundColor: "transparent"
    textColor: "{colors.text}"
    rounded: "{rounded.sm}"
    padding: "0 14px"
    height: "42px"
  button-secondary-hover:
    textColor: "{colors.accent}"
  button-danger:
    backgroundColor: "transparent"
    textColor: "{colors.danger-text}"
    rounded: "{rounded.sm}"
    padding: "0 14px"
    height: "42px"
  input-field:
    backgroundColor: "{colors.surface-raised}"
    textColor: "{colors.text}"
    rounded: "{rounded.sm}"
    padding: "0 16px"
    height: "46px"
  card-surface:
    backgroundColor: "{colors.surface-raised}"
    textColor: "{colors.text}"
    rounded: "{rounded.lg}"
    padding: "clamp(24px, 3vw, 38px)"
  chip-state:
    backgroundColor: "{colors.success-surface}"
    textColor: "{colors.success-text}"
    rounded: "{rounded.sm}"
    padding: "3px 10px"
  nav-link:
    backgroundColor: "transparent"
    textColor: "{colors.accent-contrast}"
    padding: "29px 0 25px"
---

# Design System: SimplyTest Academy

## Overview

**Creative North Star: "Das disziplinierte Planungsbrett"**

Der Schulungsplaner ist ein Arbeitsgerät, kein Schaufenster. Die Oberfläche
verhält sich wie ein sauber geführtes Planungsbrett: eine ruhige, leicht kühle
Fläche (`#e7ecf0`), auf der weiße Karten und harte Raster liegen. Nichts glänzt,
nichts wippt. Wer hier arbeitet, hat mehrere Termine, Qualifikationen und
Verfügbarkeiten gleichzeitig im Kopf — die Gestaltung nimmt Last ab, statt
Aufmerksamkeit zu fordern.

Farbe ist knapp und funktional. Das Blau (`#006fae`) markiert genau die Stellen,
an denen eine Entscheidung ansteht: die primäre Aktion, der aktive Tag, die
Kategorie, der Fokusring. Das Orange ist kein zweiter Akzent, sondern ein
Signaturton — es erscheint als 3–4 px starke Verlaufsnaht an Kopf, Fuß und
Anmeldekarte. Diese Naht ist das einzige dekorative Element im ganzen System
und trägt allein die Markenpräsenz.

Die dunkle Kopf- und Fußzone (`#171942`) klammert das Arbeitsfeld ein. Dazwischen
arbeitet die Seite mit einem breiten, zentrierten Maß (max. 1280 px) und
großzügigem vertikalen Atem, damit Tabellen, Kalenderraster und Kartenlisten
nebeneinander lesbar bleiben.

**Key Characteristics:**

- Ruhige Grundfläche, weiße erhobene Träger, dünne Trennlinien statt Schatten.
- Ein einziger Akzentton für Entscheidungen; Orange nur als Signaturnaht.
- Enge, negativ laufende Überschriften (`-0.04em`) gegen weich gesetzten Fließtext.
- Kompakte, gleich hohe Steuerelemente: 48 px primär, 46 px Feld, 42 px sekundär.
- Vollständiger Dark Mode über dieselben semantischen Variablen.
- Deutschsprachig durchgehend, Fachsprache aus `CONTEXT.md`.

## Colors

Eine kühle, zurückhaltende Palette: blaugraue Fläche, tiefes Marineblau als
Schrift- und Rahmenfarbe, ein einziges Arbeitsblau und ein Signaturorange.
Alle Farben liegen als semantische Custom Properties auf `:root` und werden im
Dark Mode geschlossen umdefiniert.

### Primary

- **Arbeitsblau** (`--accent`): Der einzige Ton für Handlungsfähigkeit — primäre
  Schaltfläche, aktiver Navigationsstrich, heutiger Kalendertag, Kategorie-Label,
  Eyebrow, Fokusring, Link im Kontobereich.
- **Arbeitsblau Gedrückt** (`--accent-hover`): Hover- und Aktivzustand der
  primären Schaltfläche und der Textlinks.
- **Kartenweiß** (`--accent-contrast`): Schrift auf gefüllten blauen Flächen.

### Secondary

- **Signaturorange** (`--brand-highlight`): Die bindende Markenfarbe der
  SimplyTest-CI. Sie ist als Token hinterlegt, aber derzeit an keiner Stelle
  der Oberfläche eingesetzt — die Marke trägt allein die Signaturnaht.
  Kein Akzent für Bedienelemente.
- **Nahtblau** (`--seam-blue`): Endpunkt derselben Naht, nie als Flächenfarbe.

### Neutral

- **Planungsgrau** (`--surface`): Die Grundfläche, auf der alles liegt.
- **Trägerweiß** (`--surface-raised`): Karten, Felder, Dialoge, Listenkörper.
- **Marineschrift** (`--text`): Fließtext, Überschriften und die dunkle Kopf-/Fußzone.
- **Gedämpftes Blaugrau** (`--muted`): Sekundärtext, Labels, Metadaten, Hilfstexte.
- **Haarlinie** (`--line`): Rahmen, Trenner und Rasterlinien — 1 px, nie stärker.

### Zustandsfarben

- **Bestätigungsgrün** (`--success-text` auf `--success-surface`): aktiver
  Kontozustand, Erfolgsmeldung, aktive Schulung.
- **Warnrot** (`--danger-text` auf `--danger-surface`): Fehlermeldung,
  stillgelegtes Konto, destruktive Aktion.
- **Achtsamkeitsgelb** (`--warning-text` auf `--warning-surface`): dringende
  Dashboard-Einträge, Terminwarnungen, der warnende Zustandsblock. Tritt nur
  als getönte Fläche mit eingefärbter Haarlinie auf, nie als dicke Seitenkante.

### Named Rules

**The One Decision Rule.** Arbeitsblau markiert ausschließlich das, worüber
gerade entschieden wird. Zwei blaue Flächen, die um dieselbe Aufmerksamkeit
konkurrieren, sind ein Fehler — nicht Geschmackssache.

**The Seam Rule.** Orange ist keine Bedienfarbe. Es erscheint als
`linear-gradient(90deg, #ea6839, #1c85c3)` mit 3–4 px Höhe an der Oberkante von
Kopfzeile, Fußzeile und Anmeldekarte. Sonst nirgends. Eine orange Schaltfläche
gibt es in diesem System nicht.

**The Token-Only Rule.** Jede Farbe in neuem CSS kommt aus einer Custom
Property. Ein Hex-Wert außerhalb des `:root`-Blocks und der Signaturnaht ist ein
Defekt: er überlebt den Dark Mode nicht.

## Typography

**Display Font:** Roboto (Fallback Arial, sans-serif)
**Body Font:** Roboto (Fallback Arial, sans-serif)
**Label/Mono Font:** `ui-monospace, SFMono-Regular, Menlo, monospace` — nur für
technische Kennungen (`SCH-001`), mit `letter-spacing: 0.05em`.

**Character:** Eine einzige Schriftfamilie, die ihre Hierarchie über Gewicht,
Laufweite und Größe erzeugt, nicht über Kontrast zweier Schriften. Große
Überschriften laufen deutlich negativ und stehen fast auf Zeilenhöhe 1 — das gibt
ihnen Masse, ohne laut zu werden. Der Fließtext läuft dagegen offen (1.55–1.65)
und ruhig.

### Hierarchy

- **Display** (700, `clamp(2.15rem, 4vw, 3.6rem)`, LH 1.02, LS -0.04em): Seitentitel
  im `.section-heading`. Maximal 13ch breit, damit der Umbruch gesetzt wirkt.
- **Headline** (700, `clamp(1.45rem, 2.2vw, 2rem)`, LH 1.08, LS -0.04em): Kartentitel,
  Kalenderüberschrift. Maximal 20ch.
- **Title** (650, `clamp(1.35rem, 2.5vw, 1.75rem)`, LS -0.025em): Kartenköpfe im
  Kontobereich, Termindetail.
- **Body** (400, 1rem, LH 1.55–1.65): Fließtext, maximal 54–58ch Zeilenlänge.
- **Label** (700, 0.72–0.78rem, LS 0.02–0.04em): Formularbeschriftungen,
  Tabellenköpfe, Kalenderwochentage, Eyebrow, Statusangaben. Immer in
  `--muted`, außer dem blauen Eyebrow.

### Named Rules

**The Measure Rule.** Kein Fließtext läuft breiter als 58ch, keine Überschrift
breiter als 20ch. Die Breite wird am Element begrenzt, nicht am Container.

**The Weight-Not-Caps Rule.** Hierarchie entsteht durch Gewicht und Größe.
Versalien sind der Tabellenkopfzeile vorbehalten; Schaltflächen und Labels
bleiben gemischt gesetzt.

## Layout

Ein zentriertes Einspaltenmaß von maximal **1280 px**. Ganzbreite Bänder
(Kopfzeile, Kalender, Dashboard, Fußzeile) ziehen ihren Innenabstand über
`padding-inline: max(24px, calc((100vw - 1280px) / 2))` — der Hintergrund läuft
durch, der Inhalt bleibt im Maß. Inhaltsseiten nutzen stattdessen
`width: min(1280px, calc(100% - 48px))`, Formularseiten das engere
`min(1120px, 100% - 3rem)`.

Vertikaler Rhythmus: Abschnitte atmen mit `clamp(64px, 6vw, 88px)`,
Überschriftenblöcke mit `clamp(36px, 4vw, 52px)`, Karten intern mit
`clamp(24px, 3vw, 38px)`. Der feine Rhythmus läuft auf einer 8-px-Basis
(6 / 8 / 16 / 24 / 38).

Raster: Katalogkarten und Profilkarten zweispaltig
(`repeat(2, minmax(0, 1fr))`, Lücke 16–20 px), der Monatskalender siebenspaltig
mit 1-px-Linien und mindestens 142 px Zellhöhe, Listenzeilen einspaltig mit
Trennlinie.

Breakpoints: **1200 px** (die vollständige Kopfzeile wechselt in ein
tastaturbedienbares Offenlegungsmenü), **860 px** (Trainerformular und
Kontozeile brechen auf), **680 px** (Kalender wechselt von Raster auf Liste,
Karten werden einspaltig, Fußzeile stapelt), **520 px** (Navigation und
Hilfsziele werden einspaltig, Dialoge werden vollflächig).
Es gibt keinen `min-width`-Einstieg: das System ist Desktop-first und faltet
nach unten.

### Named Rules

**The Full-Bleed Band Rule.** Ein Band, das eine eigene Hintergrundfarbe trägt,
läuft immer über die volle Breite; sein Inhalt wird über `padding-inline` ins
1280er-Maß gezogen — nie über einen zentrierten Wrapper mit sichtbarer Kante.

## Elevation & Depth

Das System ist bewusst **flach**. Tiefe entsteht durch Tonwert und Haarlinie:
weiße Träger auf blaugrauer Fläche, 1 px `--line` als Kante. Schatten sind
selten, weich und weit — sie heben nie, sie erden nur.

### Shadow Vocabulary

- **Kartenerdung** (`box-shadow: 0 8px 24px rgba(23, 25, 66, 0.06)`): Katalog-
  und Kontokarten. Im Dark Mode `rgba(0, 0, 0, 0.16)`.
- **Einstiegskarte** (`box-shadow: 0 18px 52px var(--shadow)`): nur die
  Anmelde-/Registrierkarte, die allein auf der Seite steht.
- **Kopfzeile** (`box-shadow: 0 8px 28px rgba(23, 25, 66, 0.18)`): trennt die
  dunkle Navigation vom Inhalt.
- **Dialog** (`box-shadow: 0 24px 60px var(--shadow)`): modale Ebene über dem
  abgedunkelten Hintergrund (`rgb(0 0 0 / 0.45)`).

### Named Rules

**The Flat-At-Rest Rule.** Flächen liegen im Ruhezustand flach. Hover hebt eine
Karte höchstens um 2 px und färbt ihre Kante blau ein — der Schatten wächst
dabei nicht mit.

## Shapes

Konsequent **rechteckig mit weich gebrochenen Ecken**. Es gibt genau drei
Radien: **6 px** für alles, was man bedient (Schaltfläche, Feld, Meldung),
**8 px** für Kalendereinträge, **10 px** (`--radius`) für tragende Flächen
(Karte, Dialog, Listenkörper, Zustandsblock). Kreisform ist der Tagesziffer im
Kalender vorbehalten.

Rahmen sind immer 1 px `--line`. Der einzige stärkere Strich im System ist die
2 px Unterkante des aktiven Navigationslinks. Dringlichkeit und Warnung werden
nicht über eine dicke Seitenkante ausgedrückt, sondern über eine getönte Fläche
mit eingefärbter Haarlinie.

### Named Rules

**The Three-Radii Rule.** 6 px bedienbar, 8 px Kalendereintrag, 10 px Träger.
Ein vierter Radius — insbesondere die Pille (`999px`) — gehört nicht in dieses
System.

## Components

### Buttons

- **Shape:** weich gebrochen (6 px), nie pillenförmig.
- **Primär** (`.primary-action`): gefülltes Arbeitsblau auf Weiß, mindestens
  48 px hoch, 22 px seitlich, 700, 0.9rem. Hover wechselt auf `--accent-hover`,
  Aktiv staucht auf `scale(0.98)`, Deaktiviert fällt auf 58 % Deckkraft.
- **Sekundär** (`.sekundaer-aktion`): transparent mit Haarlinie, 42 px hoch,
  14 px seitlich, 650, 0.78rem. Hover färbt Kante **und** Schrift blau.
- **Destruktiv** (`.danger-action`): sekundäre Form, Kante und Schrift in
  `--danger-text`, Hover legt `--danger-surface` unter.
- **Fokus:** `outline: 3px solid rgba(0, 132, 202, 0.45)` mit 4 px Versatz an der
  primären Aktion; Felder und sekundäre Aktionen nutzen stattdessen
  `box-shadow: 0 0 0 3px rgba(0, 132, 202, 0.16)`.

### Inputs / Fields

- **Style:** 46 px hoch, 16 px Innenabstand, 6 px Radius, `--surface-raised` mit
  1 px `--line`. Label darüber, 8 px Abstand, in `--muted`, 700, 0.76rem.
- **Focus:** Kante wird blau, dazu ein 3 px weicher blauer Hof; `outline: none`.
- **Disabled:** Fläche mischt sich Richtung `--surface` (`color-mix`), Schrift
  wird `--muted`, Cursor `not-allowed`.
- **Fehler:** Feldfehlertext direkt unter dem Feld in `--danger-text`, 0.85rem.

### Cards / Containers

- **Corner Style:** 10 px (`--radius`).
- **Background:** `--surface-raised` mit 1 px `--line`.
- **Shadow Strategy:** Kartenerdung (siehe Elevation), im Ruhezustand flach wirkend.
- **Internal Padding:** `clamp(24px, 3vw, 38px)`.
- **Hover:** `translateY(-2px)` über 240 ms `cubic-bezier(0.16, 1, 0.3, 1)`,
  Kante färbt sich blau ein.

### Chips

- **Zustandsschild** (`.zustand`): 6 px Radius, 0.8rem, 600, Innenabstand 3/10 px.
  Aktiv in `--success-surface` / `--success-text`, archiviert in gedämpftem
  Neutral. Ausschließlich für Zustände, nie als Filter oder Aktion.

### Navigation

- **Kopfzeile:** 78 px hoch, dunkles Marineblau mit Signaturnaht oben und
  radialem Blauschimmer rechts. Wortmarke links (142 px breit), dahinter ein
  senkrechter Trenner und das Wort „Academy".
- **Arbeitsbereiche:** Dashboard, Terminplaner und Schulungskatalog stehen als
  eigene Gruppe zusammen. Links laufen in 0.86rem und 76 % Weiß; Hover und
  Aktivzustand wechseln auf volles Weiß mit 2 px blauem Unterstrich.
- **Hilfsziele:** Die rollenabhängige Benutzerkontenverwaltung und
  Benachrichtigungen stehen rechts hinter einer Haarlinie. Verwaltung gehört
  nie in das persönliche Kontomenü.
- **Persönliches Konto:** Name und Rollen sind dauerhaft sichtbar. Das Menü
  enthält ausschließlich „Mein Profil" und „Abmelden".
- **Kompakt (≤1200 px):** Ein ortsfester Schalter „Menü" legt Arbeits- und
  Utility-Navigation untereinander im Dokumentfluss unter einer konstant 78 px
  hohen Kopfzeile offen. Zwischen
  **1201 und 1300 px** bleibt die vollständige Navigation sichtbar und rückt
  lediglich enger zusammen. Bei **680 px** werden die beiden Gruppen jeweils
  dreispaltig, bei **520 px** werden
  Arbeitsbereiche und Hilfsziele einspaltig. Alle Ziele bleiben mindestens
  44 px, auf kleinen Mobilgeräten 48 px hoch. Nach der Tastaturauswahl wandert
  der Fokus in den Inhalt; Katalog-Unterseiten weisen den Arbeitsbereich mit
  `aria-current="location"` aus.

### Tabellen

- **Verwaltungstabelle** (`.verwaltungstabelle`): randlos, Zellen 12/10 px,
  1 px `--line` als Zeilentrenner, Inhalt oben ausgerichtet. Kopfzeile in
  Versalien, 0.72rem, `letter-spacing: 0.04em`, in `--muted`. Zeilenaktionen
  sitzen rechts als kompakte sekundäre Schaltflächen (38 px statt 42 px),
  damit die Zeile nicht auseinanderläuft.

### Zustandsblock

- **`.state`:** 48 px Innenabstand, 10 px Radius, weiße Fläche mit Haarlinie.
  Trägt Lade-, Leer- und Fehlerzustände. Die Fehlervariante färbt Kante und Text
  in Warnrot; die Ladevariante zeigt pulsierende Skelettbalken
  (`animation: pulse 1.4s ease-in-out infinite`, 14 px hoch, 5 px Radius).

### Signaturnaht

Ein 3–4 px hoher `linear-gradient(90deg, #ea6839, #1c85c3)` an der Oberkante von
Kopfzeile, Fußzeile und Anmeldekarte. Sie ist das Markenzeichen des Systems und
erscheint nirgendwo sonst.

## Do's and Don'ts

### Do:

- **Do** jede Farbe aus einer Custom Property ziehen, damit der Dark Mode
  geschlossen bleibt.
- **Do** die drei Radien einhalten: 6 px bedienbar, 8 px Kalendereintrag,
  10 px Träger.
- **Do** Steuerelemente auf ihren festen Höhen halten: 48 px primär, 46 px Feld,
  42 px sekundär.
- **Do** Abstände aus der 8-px-Basis nehmen (6 / 8 / 16 / 24 / 38) und
  Abschnittsluft über `clamp()` skalieren.
- **Do** ganzbreite Bänder über `padding-inline: max(24px, calc((100vw - 1280px) / 2))`
  ins Maß ziehen.
- **Do** jedem interaktiven Element einen sichtbaren Fokuszustand geben —
  3 px blauer Ring oder Hof, nie `outline: none` ohne Ersatz.
- **Do** Tabellen mit `.verwaltungstabelle` auszeichnen; eine nackte `<table>`
  fällt aus dem System.

### Don't:

- **Don't** Pillenformen (`border-radius: 999px`) verwenden. Sie stammen aus
  einer abweichenden Schicht und gehören nicht zum System.
- **Don't** Orange als Bedienfarbe einsetzen — es ist die Naht, niemals
  Schaltfläche.
- **Don't** feste Hex-Werte außerhalb von `:root` und der Signaturnaht
  schreiben; sie brechen im Dark Mode.
- **Don't** `rem`-Abstände neben der px-Skala mischen. Das System rechnet in px.
- **Don't** `!important` verwenden. Wenn es nötig scheint, ist die Spezifität
  falsch aufgebaut.
- **Don't** Schatten zur Hierarchiebildung benutzen. Tiefe kommt aus Tonwert und
  Haarlinie.
- **Don't** einen zweiten Akzentton einführen. Eine Entscheidung, eine Farbe.
