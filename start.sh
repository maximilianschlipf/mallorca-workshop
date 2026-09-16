#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"

pids=()

cleanup() {
  ((${#pids[@]})) || return
  echo ""
  echo "Beende Prozesse..."
  for pid in "${pids[@]}"; do
    kill "$pid" 2>/dev/null || true
  done
  wait 2>/dev/null || true
}
trap cleanup EXIT
trap 'exit 130' INT TERM

kill_port_processes() {
  local port="$1" pid
  local -a port_pids=()
  while IFS= read -r pid; do
    [[ "$pid" =~ ^[0-9]+$ ]] && port_pids+=("$pid")
  done < <(lsof -nP -iTCP:"$port" -sTCP:LISTEN -t 2>/dev/null)
  ((${#port_pids[@]})) || return 0

  echo "Beende laufende Prozesse auf Port $port (${port_pids[*]})..."
  kill "${port_pids[@]}" 2>/dev/null || true
  for _ in {1..50}; do
    lsof -nP -iTCP:"$port" -sTCP:LISTEN -t >/dev/null 2>&1 || return 0
    sleep 0.1
  done

  port_pids=()
  while IFS= read -r pid; do
    [[ "$pid" =~ ^[0-9]+$ ]] && port_pids+=("$pid")
  done < <(lsof -nP -iTCP:"$port" -sTCP:LISTEN -t 2>/dev/null)
  echo "Erzwinge das Beenden auf Port $port (${port_pids[*]})..."
  kill -KILL "${port_pids[@]}" 2>/dev/null || true
  sleep 0.2
  if lsof -nP -iTCP:"$port" -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo "Port $port konnte nicht freigegeben werden."
    exit 1
  fi
}

wait_for_server() {
  local name="$1" url="$2" pid="$3" timeout="$4"
  local ende=$((SECONDS + timeout))
  while (( SECONDS < ende )); do
    if ! kill -0 "$pid" 2>/dev/null; then
      echo "$name konnte nicht gestartet werden."
      exit 1
    fi
    if curl -fsS "$url" >/dev/null 2>&1; then
      return
    fi
    sleep 1
  done
  echo "$name war nach ${timeout}s nicht bereit."
  exit 1
}

kill_port_processes 18081
kill_port_processes 15173

echo "Starte Backend (http://localhost:18081)..."
(cd "$BACKEND_DIR" && ./mvnw spring-boot:run) &
backend_pid=$!
pids+=("$backend_pid")

if [ ! -d "$FRONTEND_DIR/node_modules" ]; then
  echo "Installiere Frontend-Abhängigkeiten..."
  (cd "$FRONTEND_DIR" && npm ci)
fi

wait_for_server "Backend" "http://localhost:18081/api/health" "$backend_pid" 120

echo "Starte Frontend (http://localhost:15173)..."
(cd "$FRONTEND_DIR" && npm run dev) &
frontend_pid=$!
pids+=("$frontend_pid")

wait_for_server "Frontend" "http://localhost:15173" "$frontend_pid" 30
echo "Beide Server sind bereit. Mit Strg+C beenden."

while kill -0 "$backend_pid" 2>/dev/null && kill -0 "$frontend_pid" 2>/dev/null; do
  sleep 1
done
echo "Ein Serverprozess wurde unerwartet beendet."
exit 1
