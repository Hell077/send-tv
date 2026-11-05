"""Catch accidental large files and common credential forms in staged blobs.

This is a fast guardrail, not a complete secret scanner or a security gate.
Never print the matching content.
"""

import re
import subprocess
import sys

SECRET = re.compile(
    rb"-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----"
    rb"|\bAKIA[A-Z0-9]{16}\b|\bgh[pousr]_[A-Za-z0-9]{36,}\b"
)


def main() -> int:
    names = subprocess.check_output(
        ["git", "diff", "--cached", "--name-only", "--diff-filter=ACMR", "-z"]
    ).split(b"\0")
    failed = False
    for raw_name in filter(None, names):
        name = raw_name.decode()
        blob = subprocess.check_output(["git", "show", ":" + name])
        if len(blob) > 2 * 1024 * 1024 or SECRET.search(blob):
            print(f"Review staged file for size or credentials: {name}")
            failed = True
    return int(failed)


if __name__ == "__main__":
    sys.exit(main())
