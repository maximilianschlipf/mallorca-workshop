#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

python3 -m unittest scripts.test_check_requirements
python3 scripts/check_requirements.py
make -C docs html
(cd backend && ./mvnw test)
(cd frontend && npm test)
(cd frontend && npm run build)
(cd frontend && CI=1 npm run test:e2e)
