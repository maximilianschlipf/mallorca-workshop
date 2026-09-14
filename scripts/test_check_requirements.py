import tempfile
import unittest
from pathlib import Path

from scripts.check_requirements import audit


REQUIREMENTS = """\
.. req:: Beispiel
   :id: REQ_TEST_01
   :status: approved
"""

ACCEPTANCE = """\
.. story:: Beispiel umsetzen
   :id: STORY_TEST_01
   :status: approved
   :implements: REQ_TEST_01

.. test:: Beispiel im Browser
   :id: TEST_TEST_01
   :status: approved
   :automated: yes
   :level: e2e
   :verifies: STORY_TEST_01
"""


class RequirementsGateTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        (self.root / "docs").mkdir()
        (self.root / "frontend/tests/e2e").mkdir(parents=True)
        (self.root / "frontend/src").mkdir(parents=True)
        (self.root / "backend/src/test").mkdir(parents=True)
        (self.root / "docs/requirements.rst").write_text(REQUIREMENTS, encoding="utf-8")
        (self.root / "docs/acceptance.rst").write_text(ACCEPTANCE, encoding="utf-8")
        (self.root / "requirements-scope.txt").write_text(
            "docs/requirements.rst | docs/acceptance.rst\n", encoding="utf-8"
        )
        (self.root / "requirements-manual-evidence.md").write_text("", encoding="utf-8")

    def tearDown(self):
        self.temp.cleanup()

    def test_missing_evidence_fails(self):
        _, issues = audit(self.root)
        self.assertIn("TEST_TEST_01: vollständiger Testnachweis fehlt", issues)

    def test_e2e_evidence_must_be_in_e2e_directory(self):
        marker = "// verifies: TEST_TEST_01\n"
        (self.root / "frontend/src/example.spec.ts").write_text(marker, encoding="utf-8")
        _, issues = audit(self.root)
        self.assertIn("TEST_TEST_01: vollständiger Testnachweis fehlt", issues)

        (self.root / "frontend/tests/e2e/example.spec.ts").write_text(marker, encoding="utf-8")
        _, issues = audit(self.root)
        self.assertEqual([], issues)


if __name__ == "__main__":
    unittest.main()
