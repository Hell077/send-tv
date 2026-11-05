"""Guard translation coverage, format arguments and local documentation links."""
import re
import unittest
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class LocalizationTests(unittest.TestCase):
    def test_android_russian_has_all_strings_and_matching_format_arguments(self):
        def strings(locale):
            tree = ET.parse(ROOT / f"tv/src/main/res/values{locale}/strings.xml")
            return {node.attrib["name"]: node.text or "" for node in tree.findall("string")}
        english, russian = strings(""), strings("-ru")
        self.assertEqual(english.keys(), russian.keys())
        for name, text in english.items():
            self.assertTrue(russian[name].strip(), name)
            self.assertEqual(re.findall(r"%\d+\$[sd]", text), re.findall(r"%\d+\$[sd]", russian[name]), name)
        self.assertIn("не шифрует", russian["privacy"])
        self.assertIn("подтверждения на этом ТВ", russian["privacy"])

    def test_engineering_documents_have_russian_counterparts_and_valid_local_links(self):
        originals = [ROOT / "README.md", ROOT / "AGENTS.md"]
        for folder in ["architecture", "design", "docs", "governance", "product", "protocol", "roadmap", "website"]:
            originals.extend(path for path in (ROOT / folder).rglob("*.md")
                             if not path.is_relative_to(ROOT / "docs/ru")
                             and not path.is_relative_to(ROOT / "website/src")
                             and not path.is_relative_to(ROOT / "website/.astro"))
        for original in originals:
            translated = ROOT / "docs/ru" / original.relative_to(ROOT)
            self.assertTrue(translated.is_file(), str(original.relative_to(ROOT)))
            text = translated.read_text()
            self.assertRegex(text, r"[А-Яа-яЁё]", str(translated))
            for target in re.findall(r"\]\(([^\s)]+)\)", text):
                if "://" in target or target.startswith(("#", "mailto:")):
                    continue
                self.assertTrue((translated.parent / target.split("#")[0]).resolve().exists(), f"{translated}: {target}")


if __name__ == "__main__":
    unittest.main()
