# Setup

## Voraussetzungen

- Java 21
- Node.js 22 LTS
- npm
- Maven Wrapper (im spaeteren backend/ erwartet)
- VS Code mit GitHub Copilot

## Standard-Ports

- Backend: 18081
- Frontend (Vite): 15173

Hinweis: Ports koennen bei Bedarf ueberschrieben werden.

## Start (2 Kommandos)

Aus dem Repo-Root (copy-paste faehig):

```bash
(cd backend && ./mvnw spring-boot:run)
```

```bash
(cd frontend && npm install && npm run dev -- --port 15173)
```

### Terminal 1: Backend

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=18081
```

### Terminal 2: Frontend

```bash
cd frontend
npm install
npm run dev -- --port 15173
```

## Port-Override Beispiele

Backend:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=19081
```

Frontend:

```bash
npm run dev -- --port 16173
```

## Erwartung nach Start

- API ist unter http://localhost:18081/api/schulungen erreichbar
- Frontend ist unter http://localhost:15173 erreichbar
