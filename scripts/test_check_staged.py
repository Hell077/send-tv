import unittest

from check_staged import SECRET


class SecretGuardTest(unittest.TestCase):
    def test_rejects_common_secret_forms(self):
        for value in [
            b"-----BEGIN " + b"PRIVATE KEY-----",
            b"AKIA" + b"A" * 16,
            b"ghp_" + b"a" * 36,
        ]:
            self.assertIsNotNone(SECRET.search(value))

    def test_allows_redacted_examples_and_protocol_prose(self):
        for value in [b"Bearer <token>", b"ghp_REDACTED", b"No secrets in logs"]:
            self.assertIsNone(SECRET.search(value))


if __name__ == "__main__":
    unittest.main()
