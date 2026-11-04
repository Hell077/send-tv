"""Validate commit subjects against governance/git.md (also usable as a hook)."""

import pathlib
import re
import subprocess
import sys

SUBJECT = re.compile(
    r"(?:feat|fix|docs|design|refactor|perf|test|build|ci|chore|revert)"
    r"\((?:tv|web|cli|protocol|security|site|docs|design|repo)\)!?: \S(?:.*[^.])?$"
)


def valid(subject: str) -> bool:
    return (
        len(subject) <= 72
        and not subject.endswith(".")
        and SUBJECT.fullmatch(subject) is not None
    )


def main() -> int:
    if len(sys.argv) == 3 and sys.argv[1] == "--message-file":
        subjects = pathlib.Path(sys.argv[2]).read_text().splitlines()[:1]
    elif len(sys.argv) == 2:
        subjects = subprocess.check_output(
            ["git", "log", "--format=%s", sys.argv[1]], text=True
        ).splitlines()
    else:
        print("Usage: check_commits.py <revision-range> | --message-file <path>")
        return 2
    if not subjects:
        print("No commit subjects found")
        return 1
    invalid = [subject for subject in subjects if not valid(subject)]
    for subject in invalid:
        print(f"Invalid Conventional Commit subject: {subject}")
    return int(bool(invalid))


if __name__ == "__main__":
    sys.exit(main())
