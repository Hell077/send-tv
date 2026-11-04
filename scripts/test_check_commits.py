import unittest

from check_commits import valid


class CommitSubjectTest(unittest.TestCase):
    def test_accepts_project_scopes_and_breaking_changes(self):
        self.assertTrue(valid("feat(tv): add receiver shell"))
        self.assertTrue(valid("fix(protocol)!: reject unsafe requests"))
        self.assertTrue(valid("feat(tv): x"))

    def test_rejects_wrong_scope_missing_scope_period_and_long_subject(self):
        for subject in [
            "add receiver shell", "feat: add receiver shell",
            "feat(android): add shell", "feat(tv): add shell.",
            "feat(tv): " + "a" * 70, "feat(tv): ", "feat(tv): .",
        ]:
            with self.subTest(subject=subject):
                self.assertFalse(valid(subject))


if __name__ == "__main__":
    unittest.main()
