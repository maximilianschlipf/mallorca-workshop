# Runbook

## Tagesstart

1. Backend mit Port 18081 starten.
2. Frontend mit Port 15173 starten.
3. API kurz pruefen:

```bash
curl -i http://localhost:18081/api/schulungen
```

4. Frontend im Browser oeffnen und Katalogsicht pruefen.

## Seed-Reset

Es wird eine H2 In-Memory-Datenbank verwendet.

Regel:

- Ein Neustart des Backends stellt den sauberen Seed-Zustand wieder her.

Vorgehen:

1. Backend-Prozess stoppen.
2. Backend erneut starten.
3. API erneut mit curl pruefen.

## Troubleshooting

### Port belegt

- Backend oder Frontend auf freie Ports starten (siehe setup.md).

### API nicht erreichbar

- Pruefen, ob Backend wirklich laeuft.
- Logs auf Startfehler pruefen.

### Leere Liste in API

- Seed-Import beim Start pruefen.
- JSON-Dateien auf gueltiges Format pruefen.
