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
        (self.root / "implemented-requirements.txt").write_text(
            "docs/requirements.rst | docs/acceptance.rst\n", encoding="utf-8"
        )
    def tearDown(self):
        self.temp.cleanup()

    def test_missing_evidence_fails(self):
        _, issues = audit(self.root)
        self.assertIn("TEST_TEST_01: vollständiger Testnachweis fehlt", issues)

    def test_manual_tests_are_rejected(self):
        path = self.root / "docs/acceptance.rst"
        path.write_text(ACCEPTANCE.replace(":automated: yes", ":automated: no"), encoding="utf-8")
        _, issues = audit(self.root)
        self.assertIn("TEST_TEST_01: :automated: muss yes sein", issues)

    def test_e2e_evidence_must_be_in_e2e_directory(self):
        marker = "// verifies: TEST_TEST_01\n"
        test = marker + 'test("Beispiel", () => {})\n'
        (self.root / "frontend/src/example.spec.ts").write_text(test, encoding="utf-8")
        _, issues = audit(self.root)
        self.assertIn("TEST_TEST_01: vollständiger Testnachweis fehlt", issues)

        (self.root / "frontend/tests/e2e/example.spec.ts").write_text(test, encoding="utf-8")
        _, issues = audit(self.root)
        self.assertEqual([], issues)

    def test_marker_must_precede_an_executable_test(self):
        (self.root / "frontend/tests/e2e/example.spec.ts").write_text(
            "// verifies: TEST_TEST_01\nasync function helper() {}\n", encoding="utf-8"
        )
        _, issues = audit(self.root)
        self.assertIn("TEST_TEST_01: vollständiger Testnachweis fehlt", issues)

    def test_java_helper_without_test_annotation_is_rejected(self):
        (self.root / "backend/src/test/ExampleTest.java").write_text(
            "// verifies: TEST_TEST_01\nvoid helper() {}\n", encoding="utf-8"
        )
        _, issues = audit(self.root)
        self.assertIn("TEST_TEST_01: vollständiger Testnachweis fehlt", issues)

    def test_annotated_java_test_is_accepted(self):
        path = self.root / "docs/acceptance.rst"
        path.write_text(ACCEPTANCE.replace(":level: e2e", ":level: integration"), encoding="utf-8")
        (self.root / "backend/src/test/ExampleTest.java").write_text(
            "// verifies: TEST_TEST_01\n@Test\nvoid scenario() {}\n", encoding="utf-8"
        )
        _, issues = audit(self.root)
        self.assertEqual([], issues)


if __name__ == "__main__":
    unittest.main()
