# Configuration file for the Sphinx documentation builder.
#
# For the full list of built-in configuration values, see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

# -- Project information -----------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#project-information

project = 'Mein Projekt'
copyright = '2026, Philippe Joseph'
author = 'Philippe Joseph'

version = '0.1'
release = '0.1'

# -- General configuration ---------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#general-configuration

templates_path = ['_templates']

language = 'de'

# -- Options for HTML output -------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#options-for-html-output

# Corporate Design aus dem Paket st-sphinx-theme (Repo: ../st-sphinx-theme).
# Es erbt von sphinx_rtd_theme und bringt Farben, Roboto und die Chrome-
# Gestaltung mit -- die fruehere lokale _static/css/custom.css ist damit
# vollstaendig abgeloest und wurde entfernt.
#
# Das Theme liefert auch Styles fuer sphinx-needs, bindet sie aber nur ein,
# wenn sphinx_needs in extensions steht -- was hier der Fall ist, die Needs
# erscheinen also im Corporate Design.
import st_theme

extensions = [
    "sphinx_needs",
    "sphinxcontrib.plantuml",
]

html_theme = "st"
html_logo = st_theme.asset("img/st-logo.svg")
html_favicon = st_theme.asset("img/favicon.ico")

exclude_patterns = ["_build", "Thumbs.db", ".DS_Store"]

# ---------------------------------------------------------------------------
# Sphinx-Needs: eigene Need-Typen (analog zu "Trackern")
# ---------------------------------------------------------------------------
needs_types = [
    {"directive": "concept",  "title": "Concept",     "prefix": "CONCEPT_", "color": "#A9C7E4", "style": "node"},
    {"directive": "decision", "title": "Decision",    "prefix": "DEC_",     "color": "#C7B3E0", "style": "node"},
    {"directive": "req",     "title": "Requirement", "prefix": "REQ_",     "color": "#BFD8D2", "style": "node"},
    {"directive": "story",   "title": "Story",       "prefix": "STORY_",   "color": "#FEDCD2", "style": "node"},
    {"directive": "bug",     "title": "Bug",         "prefix": "BUG_",     "color": "#DF744A", "style": "node"},
    {"directive": "test",    "title": "Test",        "prefix": "TEST_",    "color": "#DCB239", "style": "node"},
]

# ---------------------------------------------------------------------------
# ID-Schema:  TYPE _ AREA _ [THEMA ...] _ NN
#   TYPE  = CONCEPT | DEC | REQ | STORY | BUG | TEST
#   AREA  = 3 Zeichen aus Großbuchstaben/Ziffern (Bereich, z. B. SIM, WIR)
#   THEMA = optionale, mehrteilige Gruppierung aus Großbuchstaben/Ziffern
#           (z. B. TAKT, RAUM, GELD)
#   NN    = 2-stellige, fortlaufende Nummer je Thema (führende Null erhält
#           die lexikalische = numerische Sortierung)
# Beispiele: REQ_SIM_01, REQ_SIM_TAKT_01, TEST_WIR_MARKT_02, CONCEPT_WIR_GELD_01
#
# Der Bereich im Schlüssel gibt jedem Bereich einen eigenen Nummernraum und
# vermeidet Merge-Kollisionen bei paralleler Arbeit in isolierten Worktrees.
# id_required schaltet Auto-IDs ab (IDs müssen stabil und explizit sein),
# id_regex macht schema-fremde IDs zum Build-Fehler.
# ---------------------------------------------------------------------------
needs_id_required = True
needs_id_regex = r"^(CONCEPT|DEC|REQ|STORY|BUG|TEST)_[A-Z0-9]{3}(_[A-Z0-9]+)*_[0-9]{2}$"

# ---------------------------------------------------------------------------
# Frei benennbare Link-Typen (Kern der Traceability)
#   Bug        --affects-->      Story
#   Story      --implements-->   Requirement
#   Test       --verifies-->     Requirement / Story
#   Requirement --derived_from--> Concept
#   beliebige Need --supersedes--> beliebige Need (gleicher Typ)
# Rückwärts-Links (incoming) berechnet Sphinx-Needs automatisch.

# "supersedes" hält Brüche fest, statt sie zu überschreiben: In der
# Konzeptphase werden Concepts regelmäßig ungültig, weil neue Anforderungen
# ihre Annahmen kippen. Das alte Concept bleibt mit Status "superseded"
# stehen und wird vom neuen per :supersedes: referenziert - die Begründung
# des Bruchs bleibt so nachvollziehbar. "rejected" bedeutet dagegen "war
# falsch", nicht "ist abgelöst".
#
# "concept" ist recherchiertes Hintergrundwissen (Definitionen, Formeln,
# Theoriebezüge) - noch keine testbare Anforderung. Requirements destillieren
# daraus konkrete, testbare Fähigkeiten und verlinken mit :derived_from: auf
# den Ursprung, damit die Kette Concept -> Requirement -> Story -> Test
# durchgängig bleibt.
# ---------------------------------------------------------------------------
needs_links = {
    "affects":      {"incoming": "affected by",   "outgoing": "affects"},
    "implements":   {"incoming": "implemented by", "outgoing": "implements"},
    "verifies":     {"incoming": "verified by",    "outgoing": "verifies"},
    "derived_from": {"incoming": "source for",     "outgoing": "derived from"},
    "supersedes":   {"incoming": "superseded by",  "outgoing": "supersedes"},
}

# ---------------------------------------------------------------------------
# Felder pro Need: Status-Lebenszyklus + eigene Zusatzfelder
#
# Status-Lebenszyklus (maturitätsbasiert, typübergreifend):
#   draft → review → approved → implemented / verified
#   (+ rejected = war falsch, + superseded = war richtig, ist abgelöst)
# Die Reihenfolge drückt Reife aus, nicht die Entstehungsreihenfolge:
# eine Story darf auf ein draft-Requirement zeigen, ein draft-Requirement
# darf ohne Stories existieren. Erst am Coding-Gate muss der Link stehen.
#
# Das enum auf dem Kernfeld "status" validiert jeden Statuswert
# (unbekannter Status -> Build-Fehler). priority/component/automated sind
# frei definierte Zusatzfelder.
# ---------------------------------------------------------------------------
needs_fields = {
    "status": {
        "schema": {
            "type": "string",
            "enum": [
                "draft", "review", "approved", "implemented", "verified",
                "rejected", "superseded",
            ],
        },
    },
    "priority":  {"schema": {"type": "string"}, "description": "Priorität, z. B. high/medium/low"},
    "component": {"schema": {"type": "string"}, "description": "Betroffene Komponente"},
    "automated": {"schema": {"type": "string"}, "description": "Testfall automatisiert (yes/no)"},
    "level": {
        "schema": {"type": "string", "enum": ["unit", "integration", "e2e", "manual"]},
        "description": "Verbindliche Nachweisebene, wenn eine bestimmte Ebene erforderlich ist",
    },
}

# Hinweis: Eine farbliche Reife-Darstellung im needflow-Graphen lässt sich
# später über needs_flow_configs ergänzen (benötigt PlantUML). Die Reife ist
# bereits über die needtable-Statusspalte sichtbar.

# needs.json-Export für schnelle Konsistenzchecks (ohne vollen HTML-Build)
needs_build_json = True
needs_json_remove_defaults = True
