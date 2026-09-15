#!/usr/bin/env python3
"""Prüft freigegebene Requirements gegen Stories, Abnahmetests und Test-Evidenz."""

from __future__ import annotations

import re
import sys
from collections import defaultdict
from dataclasses import dataclass, field
from pathlib import Path


ACTIVE = {"approved", "implemented", "verified"}
ID_PATTERN = re.compile(r"\b(?:REQ|STORY|TEST)_[A-Z0-9]+(?:_[A-Z0-9]+)+\b")
DIRECTIVE_PATTERN = re.compile(r"^\.\. (concept|decision|req|story|bug|test)::\s*(.*)$")
OPTION_PATTERN = re.compile(r"^\s+:([a-z_]+):\s*(.*)$")
EVIDENCE_PATTERN = re.compile(r"verifies:\s*(.*)$")
TEST_PATTERN = re.compile(r"^(?:test|it)\s*\(|(?:public\s+)?void\s+\w+\s*\(")


@dataclass
class Need:
    kind: str
    options: dict[str, str] = field(default_factory=dict)

    @property
    def id(self) -> str:
        return self.options.get("id", "")

    def ids(self, option: str) -> set[str]:
        return set(ID_PATTERN.findall(self.options.get(option, "")))


def parse_needs(path: Path) -> list[Need]:
    needs: list[Need] = []
    current: Need | None = None
    current_option: str | None = None
    for line in path.read_text(encoding="utf-8").splitlines():
        directive = DIRECTIVE_PATTERN.match(line)
        if directive:
            if current:
                needs.append(current)
            current = Need(directive.group(1))
            current_option = None
            continue
        if not current:
            continue
        option = OPTION_PATTERN.match(line)
        if option:
            current_option = option.group(1)
            current.options[current_option] = option.group(2).strip()
        elif current_option and line.startswith("      ") and line.strip():
            current.options[current_option] += " " + line.strip()
        else:
            current_option = None
    if current:
        needs.append(current)
    return needs


def active(needs: list[Need], kind: str) -> list[Need]:
    return [need for need in needs if need.kind == kind and need.options.get("status") in ACTIVE]


def load_scopes(root: Path) -> list[tuple[Path, Path]]:
    scopes: list[tuple[Path, Path]] = []
    for number, raw in enumerate((root / "requirements-scope.txt").read_text(encoding="utf-8").splitlines(), 1):
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        parts = [part.strip() for part in line.split("|")]
        if len(parts) != 2:
            raise ValueError(f"requirements-scope.txt:{number}: erwartet 'requirements | umsetzung'")
        paths = tuple(root / part for part in parts)
        if not all(path.is_file() for path in paths):
            raise ValueError(f"requirements-scope.txt:{number}: Datei nicht gefunden")
        scopes.append(paths)  # type: ignore[arg-type]
    if not scopes:
        raise ValueError("requirements-scope.txt enthält keinen Scope")
    return scopes


def find_evidence(root: Path) -> dict[str, list[Path]]:
    found: dict[str, list[Path]] = defaultdict(list)
    test_files = list((root / "backend/src/test").rglob("*"))
    test_files += list((root / "frontend/src").rglob("*.spec.ts"))
    test_files += list((root / "frontend/tests").rglob("*"))
    for path in (path for path in test_files if path.is_file()):
        lines = path.read_text(encoding="utf-8").splitlines()
        for index, line in enumerate(lines):
            match = EVIDENCE_PATTERN.search(line)
            following = (candidate.strip() for candidate in lines[index + 1:index + 7])
            declaration = next((candidate for candidate in following
                                if candidate and not candidate.startswith("@")), "")
            if match and TEST_PATTERN.search(declaration):
                for test_id in ID_PATTERN.findall(match.group(1)):
                    found[test_id].append(path.relative_to(root))
    return found


def audit(root: Path) -> tuple[list[tuple[str, str, str, str, str]], list[str]]:
    evidence = find_evidence(root)
    rows: list[tuple[str, str, str, str, str]] = []
    issues: list[str] = []

    for requirements_path, acceptance_path in load_scopes(root):
        requirements = active(parse_needs(requirements_path), "req")
        acceptance = parse_needs(acceptance_path)
        stories = active(acceptance, "story")
        tests = active(acceptance, "test")
        stories_by_requirement: dict[str, list[Need]] = defaultdict(list)
        tests_by_target: dict[str, list[Need]] = defaultdict(list)

        for story in stories:
            if not story.ids("implements"):
                issues.append(f"{story.id}: Story ohne implements-Link")
            for requirement_id in story.ids("implements"):
                stories_by_requirement[requirement_id].append(story)
        for test in tests:
            if test.options.get("automated") != "yes":
                issues.append(f"{test.id}: :automated: muss yes sein")
            if not test.ids("verifies"):
                issues.append(f"{test.id}: Test ohne verifies-Link")
            for target_id in test.ids("verifies"):
                tests_by_target[target_id].append(test)

        for requirement in requirements:
            linked_stories = stories_by_requirement[requirement.id]
            linked_tests = {test.id: test for test in tests_by_target[requirement.id]}
            for story in linked_stories:
                linked_tests.update({test.id: test for test in tests_by_target[story.id]})
            if not linked_stories:
                issues.append(f"{requirement.id}: keine freigegebene Story")
            if not linked_tests:
                issues.append(f"{requirement.id}: kein freigegebener Abnahmetest")
                rows.append((requirement.id, "—", "—", "—", "OFFEN"))
                continue

            story_ids = ", ".join(story.id for story in linked_stories) or "—"
            for test in sorted(linked_tests.values(), key=lambda item: item.id):
                candidates = evidence.get(test.id, [])
                if test.options.get("level") == "e2e":
                    candidates = [item for item in candidates if "tests/e2e" in item.as_posix()]
                result = "E2E" if candidates and test.options.get("level") == "e2e" else (
                    "GETESTET" if candidates else "OFFEN"
                )
                evidence_text = ", ".join(sorted({item.as_posix() for item in candidates})) or "—"
                rows.append((requirement.id, story_ids, test.id, evidence_text, result))
                if not candidates:
                    issues.append(f"{test.id}: vollständiger Testnachweis fehlt")

    return rows, sorted(set(issues))


def main() -> int:
    root = Path(__file__).resolve().parents[1]
    try:
        rows, issues = audit(root)
    except (OSError, ValueError) as error:
        print(f"Requirements-Gate konnte nicht ausgeführt werden: {error}", file=sys.stderr)
        return 2

    print("| Requirement | Story | Abnahmetest | Evidenz | Ergebnis |")
    print("|---|---|---|---|---|")
    for row in rows:
        print("| " + " | ".join(row) + " |")
    present = len({row[2] for row in rows if row[4] != "OFFEN"})
    print(f"\nRequirements-Gate: {present} Nachweise vorhanden, {len(issues)} offene Punkte.")
    for issue in issues:
        print(f"- {issue}")
    return 1 if issues else 0


if __name__ == "__main__":
    raise SystemExit(main())
