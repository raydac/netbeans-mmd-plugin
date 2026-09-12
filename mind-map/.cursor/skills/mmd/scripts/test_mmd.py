#!/usr/bin/env python3
"""Tests for the MMD skill scripts against mind-map-model fixtures."""

from __future__ import annotations

import json
import os
import subprocess
import sys
import tempfile
import unittest

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import crypto  # noqa: E402
import mmd  # noqa: E402

SCRIPTS = os.path.dirname(os.path.abspath(__file__))
MIND_MAP_ROOT = os.path.abspath(os.path.join(SCRIPTS, "..", "..", "..", ".."))
CANCER_RISK = os.path.join(
    MIND_MAP_ROOT,
    "mind-map-model",
    "src",
    "test",
    "resources",
    "com",
    "igormaznitsa",
    "mindmap",
    "model",
    "parser",
    "cancer_risk.mmd",
)


class HeaderTests(unittest.TestCase):
    def test_empty_rejected(self) -> None:
        with self.assertRaises(mmd.MmdError):
            mmd.parse_mmd("")

    def test_text_without_delimiter_rejected(self) -> None:
        with self.assertRaises(mmd.MmdError):
            mmd.parse_mmd("Who is here")

    def test_header_only_null_root(self) -> None:
        parsed = mmd.parse_mmd("Who is here\n-")
        self.assertIsNone(parsed.root)

    def test_hash_at_eof_without_title_null_root(self) -> None:
        parsed = mmd.parse_mmd("Who is here\n-\n#")
        self.assertIsNone(parsed.root)

    def test_hash_with_spaces_at_eof(self) -> None:
        parsed = mmd.parse_mmd("Who is here\n-\n#         ")
        self.assertIsNotNone(parsed.root)
        self.assertEqual("        ", parsed.root.text)

    def test_no_attributes(self) -> None:
        parsed = mmd.parse_mmd("lkf\n---\n# Hello")
        self.assertEqual("1.1", parsed.attributes["__version__"])
        self.assertEqual("Hello", parsed.root.text)

    def test_one_attribute_double_newline(self) -> None:
        parsed = mmd.parse_mmd("lkf\n> test=`Hi`\n\n---\n# Hello")
        self.assertEqual("Hi", parsed.attributes["test"])
        self.assertEqual("Hello", parsed.root.text)

    def test_one_attribute_single_newline(self) -> None:
        parsed = mmd.parse_mmd("lkf\n> test=`Hi`\n---\n# Hello")
        self.assertEqual("Hi", parsed.attributes["test"])

    def test_overridden_attributes(self) -> None:
        parsed = mmd.parse_mmd("lkf\n> test=`Hi`\n> test=`Lo`\n---\n# Hello")
        self.assertEqual("Lo", parsed.attributes["test"])

    def test_write_without_attributes(self) -> None:
        tree = mmd.MindMap()
        self.assertEqual(
            "[Scia Reto](https://sciareto.org) mind map   \n> __version__=`1.1`\n---\n",
            mmd.write_mmd(tree),
        )

    def test_write_with_attribute(self) -> None:
        tree = mmd.MindMap()
        tree.attributes["hello"] = "World"
        self.assertEqual(
            "[Scia Reto](https://sciareto.org) mind map   \n"
            "> __version__=`1.1`,hello=`World`\n---\n",
            mmd.write_mmd(tree),
        )


class TopicParseTests(unittest.TestCase):
    def test_solar_iteration_order(self) -> None:
        parsed = mmd.parse_mmd(
            "test\n---\n# Solar\n## Mercury\n## Venus\n## Earth\n### Moon\n"
            "## Mars\n### Phobos\n### Deimos"
        )
        names = []

        def walk(topic: mmd.Topic) -> None:
            names.append(topic.text)
            for child in topic.children:
                walk(child)

        walk(parsed.root)
        self.assertEqual(
            ["Solar", "Mercury", "Venus", "Earth", "Moon", "Mars", "Phobos", "Deimos"],
            names,
        )

    def test_two_level_iteration(self) -> None:
        parsed = mmd.parse_mmd(
            "---\n# root\n## child1\n### child1.1\n### child1.2\n"
            "## child2\n### child2.1\n### child2.2\n"
        )
        names = []

        def walk(topic: mmd.Topic) -> None:
            names.append(topic.text)
            for child in topic.children:
                walk(child)

        walk(parsed.root)
        self.assertEqual(
            ["root", "child1", "child1.1", "child1.2", "child2", "child2.1", "child2.2"],
            names,
        )

    def test_delimiter_only(self) -> None:
        parsed = mmd.parse_mmd("---\n")
        self.assertIsNone(parsed.root)

    def test_code_snippets_java_and_shell(self) -> None:
        parsed = mmd.parse_mmd(
            "---\n# root\n```Java\nSystem.exit(0);\n```\n```Shell\nexit\n```"
        )
        self.assertEqual("System.exit(0);\n", parsed.root.snippets["Java"])
        self.assertEqual("exit\n", parsed.root.snippets["Shell"])

    def test_note_and_link(self) -> None:
        parsed = mmd.parse_mmd(
            "---\n# Topic\n- NOTE\n<pre>Some\ntext</pre>\n"
            "- LINK\n<pre>http://www.google.com</pre>\n## Topic2"
        )
        self.assertEqual("Some\ntext", parsed.root.extras["NOTE"])
        self.assertEqual("http://www.google.com", parsed.root.extras["LINK"])
        self.assertEqual("Topic2", parsed.root.children[0].text)

    def test_note_preserves_crlf(self) -> None:
        parsed = mmd.parse_mmd(
            "x\n---\n# Topic\r\n- NOTE\r\n<pre>   Some   \r\n    text     \n    line  \r\n"
            "  end \r\n   </pre>\r\n- LINK\n<pre>http://www.google.com</pre>\n## Topic2"
        )
        self.assertEqual(
            "   Some   \r\n    text     \n    line  \r\n  end \r\n   ",
            parsed.root.extras["NOTE"],
        )

    def test_link_trims_spaces(self) -> None:
        parsed = mmd.parse_mmd(
            "---\n# Topic\n- LINK\n<pre>  http://www.google.com </pre>\n"
        )
        self.assertEqual("http://www.google.com", parsed.root.extras["LINK"])

    def test_attributes_with_backticks(self) -> None:
        parsed = mmd.parse_mmd(
            "---\n# Topic\n- NOTE\n<pre>Some\ntext</pre>\n"
            "- LINK\n<pre>http://www.google.com</pre>\n"
            "> attr1=`hello`,attr2=``wor`ld``"
        )
        self.assertEqual("hello", parsed.root.attributes["attr1"])
        self.assertEqual("wor`ld", parsed.root.attributes["attr2"])

    def test_br_in_titles(self) -> None:
        parsed = mmd.parse_mmd("---\n# Topic<br>Root\n ## Child<br/>Topic")
        self.assertEqual("Topic\nRoot", parsed.root.text)
        self.assertEqual("Child\nTopic", parsed.root.children[0].text)

    def test_snippet_indented_fence_stays_in_body(self) -> None:
        parsed = mmd.parse_mmd("---\n# Root\n```Java\nSystem.exit(0);\n ```\n```")
        self.assertEqual("System.exit(0);\n ```\n", parsed.root.snippets["Java"])

    def test_snippet_backticks_with_space_and_tick(self) -> None:
        parsed = mmd.parse_mmd("---\n# Root\n```Java\nSystem.exit(0);\n``` `\n```")
        self.assertEqual("System.exit(0);\n``` `\n", parsed.root.snippets["Java"])

    def test_snippet_many_backticks(self) -> None:
        parsed = mmd.parse_mmd("---\n# Root\n```Java\nSystem.exit(0);\n`````\n```")
        self.assertEqual("System.exit(0);\n`````\n", parsed.root.snippets["Java"])

    def test_snippet_three_backtick_and_char(self) -> None:
        parsed = mmd.parse_mmd("---\n# Root\n```Java\nSystem.exit(0);\n```a\n```")
        self.assertEqual("System.exit(0);\n```a\n", parsed.root.snippets["Java"])

    def test_snippet_char_and_three_backtick(self) -> None:
        parsed = mmd.parse_mmd("---\n# Root\n```Java\nSystem.exit(0);\na```\n```")
        self.assertEqual("System.exit(0);\na```\n", parsed.root.snippets["Java"])

    def test_empty_snippet(self) -> None:
        parsed = mmd.parse_mmd("---\n# Root\n```Java\n```")
        self.assertEqual("", parsed.root.snippets["Java"])

    def test_unclosed_snippet_header_only(self) -> None:
        parsed = mmd.parse_mmd("---\n# Root\n```Java\n")
        self.assertNotIn("Java", parsed.root.snippets)

    def test_unclosed_snippet_with_body(self) -> None:
        parsed = mmd.parse_mmd("---\n# Root\n```Java\nSystem.out.println();\n")
        self.assertNotIn("Java", parsed.root.snippets)

    def test_empty_middle_level(self) -> None:
        parsed = mmd.parse_mmd("---\n# \n## Child\n")
        self.assertEqual("", parsed.root.text)
        self.assertEqual("Child", parsed.root.children[0].text)

    def test_multi_levels(self) -> None:
        parsed = mmd.parse_mmd(
            "---\n# Level1\n## Level2.1\n### Level3.1\n## Level2.2\n"
            "### Level3.2\n#### Level4.2\n## Level2.3"
        )
        self.assertEqual("Level1", parsed.root.text)
        self.assertEqual(
            ["Level2.1", "Level2.2", "Level2.3"],
            [child.text for child in parsed.root.children],
        )
        self.assertEqual("Level4.2", parsed.root.children[1].children[0].children[0].text)


class WriteTests(unittest.TestCase):
    def test_one_level(self) -> None:
        tree = mmd.MindMap()
        tree.root = mmd.Topic("Level1")
        self.assertTrue(mmd.write_mmd(tree).endswith("\n# Level1\n"))

    def test_one_level_with_link(self) -> None:
        tree = mmd.MindMap()
        tree.root = mmd.Topic("Level1")
        tree.root.extras["LINK"] = "http://wwww.igormaznitsa.com"
        written = mmd.write_mmd(tree)
        self.assertIn(
            "\n# Level1\n- LINK\n<pre>http://wwww.igormaznitsa.com</pre>\n",
            written,
        )

    def test_link_and_snippets(self) -> None:
        tree = mmd.MindMap()
        tree.root = mmd.Topic("Level1")
        tree.root.extras["LINK"] = "http://wwww.igormaznitsa.com"
        tree.root.snippets["Java"] = "System.exit();"
        tree.root.snippets["Shell"] = "exit"
        written = mmd.write_mmd(tree)
        self.assertIn(
            "\n# Level1\n- LINK\n<pre>http://wwww.igormaznitsa.com</pre>\n"
            "```Java\nSystem.exit();\n```\n```Shell\nexit\n```\n",
            written,
        )

    def test_attribute_with_backtick(self) -> None:
        tree = mmd.MindMap()
        tree.root = mmd.Topic("Level1")
        tree.root.attributes["hello"] = "wor`ld"
        tree.root.extras["LINK"] = "http://wwww.igormaznitsa.com"
        written = mmd.write_mmd(tree)
        self.assertIn(
            "\n# Level1\n> hello=``wor`ld``\n\n- LINK\n"
            "<pre>http://wwww.igormaznitsa.com</pre>\n",
            written,
        )

    def test_special_chars_in_title(self) -> None:
        tree = mmd.MindMap()
        tree.root = mmd.Topic("<Level1>\nNextText")
        written = mmd.write_mmd(tree)
        self.assertTrue(written.endswith("\n# \\<Level1\\><br/>NextText\n"))

    def test_two_levels(self) -> None:
        tree = mmd.MindMap()
        tree.root = mmd.Topic("Level1")
        mmd.Topic("Level2", tree.root)
        written = mmd.write_mmd(tree)
        self.assertTrue(written.endswith("\n# Level1\n\n## Level2\n"))

    def test_three_levels_escaped_dot(self) -> None:
        tree = mmd.MindMap()
        tree.root = mmd.Topic("Level1")
        level2 = mmd.Topic("Level2", tree.root)
        mmd.Topic("Level3", level2)
        mmd.Topic("Level2.1", tree.root)
        written = mmd.write_mmd(tree)
        self.assertTrue(written.endswith("\n# Level1\n\n## Level2\n\n### Level3\n\n## Level2\\.1\n"))

    def test_hash_title_round_trip(self) -> None:
        tree = mmd.MindMap()
        tree.root = mmd.Topic("")
        mmd.Topic("#NewTopic", tree.root)
        packed = mmd.write_mmd(tree)
        parsed = mmd.parse_mmd(packed)
        self.assertEqual("#NewTopic", parsed.root.children[0].text)

    def test_encrypted_note_round_trip(self) -> None:
        tree = mmd.MindMap()
        tree.root = mmd.Topic("`Root\ntopic`")
        tree.root.extras["NOTE"] = "Encrypted world"
        tree.root.attributes["extras.note.encrypted"] = "true"
        tree.root.attributes["extras.note.encrypted.hint"] = "tip"
        written = mmd.write_mmd(tree)
        self.assertIn(
            "> extras.note.encrypted=`true`,extras.note.encrypted.hint=`tip`",
            written,
        )
        parsed = mmd.parse_mmd(written)
        self.assertEqual("true", parsed.root.attributes["extras.note.encrypted"])
        self.assertEqual("tip", parsed.root.attributes["extras.note.encrypted.hint"])
        self.assertEqual("Encrypted world", parsed.root.extras["NOTE"])

    def test_note_with_ticks_round_trip(self) -> None:
        tree = mmd.MindMap()
        tree.root = mmd.Topic("`Root\ntopic`")
        tree.root.extras["NOTE"] = "Hello world \n <br>```Some```"
        written = mmd.write_mmd(tree)
        expected_tail = (
            "\n# \\`Root<br/>topic\\`\n- NOTE\n<pre>Hello world \n"
            " &lt;br&gt;```Some```</pre>\n"
        )
        self.assertTrue(written.endswith(expected_tail), written)
        parsed = mmd.parse_mmd(written)
        self.assertEqual("`Root\ntopic`", parsed.root.text)
        self.assertEqual("Hello world \n <br>```Some```", parsed.root.extras["NOTE"])


class EscapeTests(unittest.TestCase):
    def test_unescape_markdown(self) -> None:
        self.assertEqual("Hello\nWorld", mmd.unescape_markdown("Hello<br>World"))
        self.assertEqual("<>\n", mmd.unescape_markdown("\\<\\><br>"))
        self.assertEqual(
            "\\`*_{}[]()#<>+-.!\n",
            mmd.unescape_markdown(
                "\\\\\\`\\*\\_\\{\\}\\[\\]\\(\\)\\#\\<\\>\\+\\-\\.\\!<br/>"
            ),
        )

    def test_escape_markdown(self) -> None:
        self.assertEqual("Hello<br/>World", mmd.escape_markdown("Hello\nWorld"))
        self.assertEqual(
            "\\\\\\`\\*\\_\\{\\}\\[\\]\\(\\)\\#\\<\\>\\+\\-\\.\\!<br/>",
            mmd.escape_markdown("\\`*_{}[]()#<>+-.!\n"),
        )


class CancerRiskTests(unittest.TestCase):
    def test_round_trip_tree(self) -> None:
        self.assertTrue(os.path.isfile(CANCER_RISK), CANCER_RISK)
        with open(CANCER_RISK, "r", encoding="utf-8", newline="") as handle:
            original = handle.read()
        parsed = mmd.parse_mmd(original)
        self.assertIsNotNone(parsed.root)
        self.assertEqual("Causes of Cancer", parsed.root.text)
        self.assertEqual(2, len(parsed.root.children))
        self.assertEqual("Environmental Risk Factors", parsed.root.children[0].text)
        self.assertEqual(
            'System.out.println("Hello world!")\n',
            parsed.root.children[0].snippets["Java"],
        )
        self.assertIn("leftSide", parsed.root.children[1].attributes)
        rewritten = mmd.write_mmd(parsed)
        again = mmd.parse_mmd(rewritten)
        self.assertEqual(mmd.comparable_tree(parsed), mmd.comparable_tree(again))
        self.assertEqual([], mmd.validate_mmd(original))
        self.assertEqual([], mmd.validate_mmd(rewritten))


class CliTests(unittest.TestCase):
    def test_parse_write_validate_scripts(self) -> None:
        sample = os.path.join(SCRIPTS, "_sample.mmd")
        tree_path = os.path.join(SCRIPTS, "_sample.json")
        out_path = os.path.join(SCRIPTS, "_sample_out.mmd")
        try:
            with open(sample, "w", encoding="utf-8", newline="\n") as handle:
                handle.write("---\n# Root\n## Child\n")
            parse = subprocess.run(
                [sys.executable, os.path.join(SCRIPTS, "parse.py"), sample],
                check=True,
                capture_output=True,
                text=True,
            )
            with open(tree_path, "w", encoding="utf-8") as handle:
                handle.write(parse.stdout)
            subprocess.run(
                [
                    sys.executable,
                    os.path.join(SCRIPTS, "write.py"),
                    "--in",
                    tree_path,
                    "--out",
                    out_path,
                ],
                check=True,
                capture_output=True,
                text=True,
            )
            validate = subprocess.run(
                [sys.executable, os.path.join(SCRIPTS, "validate.py"), out_path],
                check=True,
                capture_output=True,
                text=True,
            )
            self.assertEqual("OK\n", validate.stdout)
        finally:
            for path in (sample, tree_path, out_path):
                if os.path.isfile(path):
                    os.remove(path)


JAVA_NOTE_CIPHERTEXT = (
    "gJLc5oWXTyeLeu24WhyqdlDFoGMgvuTvTzOx4hdCRx8JYjXMUoziQFR+fyiO3/rt"
    "Riiy2BVXTM04CUbp8dkb5A=="
)


class CryptoTests(unittest.TestCase):
    def test_encrypt_matches_java_vector(self) -> None:
        self.assertEqual(
            JAVA_NOTE_CIPHERTEXT,
            crypto.encrypt("hello", "Hello Crypto-World"),
        )

    def test_decrypt_java_vector(self) -> None:
        self.assertEqual(
            "Hello Crypto-World",
            crypto.decrypt("hello", JAVA_NOTE_CIPHERTEXT),
        )

    def test_decrypt_wrong_password(self) -> None:
        with self.assertRaises(crypto.DecryptError):
            crypto.decrypt("hello1", JAVA_NOTE_CIPHERTEXT)
        with self.assertRaises(crypto.DecryptError):
            crypto.decrypt("", JAVA_NOTE_CIPHERTEXT)

    def test_decrypt_empty_ciphertext(self) -> None:
        with self.assertRaises(crypto.DecryptError):
            crypto.decrypt("hello", "")

    def test_encrypt_empty_password_is_identity(self) -> None:
        self.assertEqual("plain note", crypto.encrypt("", "plain note"))

    def test_encrypt_decrypt_unicode_round_trip(self) -> None:
        text = "Привет 🔐\nsecond line"
        self.assertEqual(text, crypto.decrypt("пароль", crypto.encrypt("пароль", text)))

    def test_list_encrypted_notes(self) -> None:
        tree = mmd.MindMap()
        tree.root = mmd.Topic("Root")
        secret = mmd.Topic("Secret", tree.root)
        secret.extras["NOTE"] = JAVA_NOTE_CIPHERTEXT
        secret.attributes["extras.note.encrypted"] = "true"
        secret.attributes["extras.note.encrypted.hint"] = "tip"
        mmd.Topic("Open", tree.root).extras["NOTE"] = "visible"
        notes = crypto.find_encrypted_notes(tree.root)
        self.assertEqual(1, len(notes))
        self.assertEqual(["Root", "Secret"], notes[0]["path"])
        self.assertEqual("tip", notes[0]["hint"])
        self.assertEqual(JAVA_NOTE_CIPHERTEXT, notes[0]["ciphertext"])

    def test_cli_decrypt_password_stdin(self) -> None:
        result = subprocess.run(
            [
                sys.executable,
                os.path.join(SCRIPTS, "crypto.py"),
                "decrypt",
                "--password-stdin",
                "--text",
                JAVA_NOTE_CIPHERTEXT,
            ],
            input="hello\n",
            capture_output=True,
            text=True,
            check=False,
        )
        self.assertEqual(0, result.returncode)
        self.assertEqual("Hello Crypto-World", result.stdout)

    def test_cli_decrypt_wrong_password(self) -> None:
        result = subprocess.run(
            [
                sys.executable,
                os.path.join(SCRIPTS, "crypto.py"),
                "decrypt",
                "--password-stdin",
                "--text",
                JAVA_NOTE_CIPHERTEXT,
            ],
            input="nope\n",
            capture_output=True,
            text=True,
            check=False,
        )
        self.assertEqual(1, result.returncode)
        self.assertIn("wrong password", result.stderr)

    def test_cli_list(self) -> None:
        packed = mmd.write_mmd(
            mmd.parse_mmd(
                "[Scia Reto](https://sciareto.org) mind map   \n"
                "> __version__=`1.1`\n---\n# Secret\n"
                "> extras.note.encrypted=`true`,extras.note.encrypted.hint=`tip`\n\n"
                "- NOTE\n<pre>%s</pre>\n" % JAVA_NOTE_CIPHERTEXT
            )
        )
        handle = tempfile.NamedTemporaryFile("w", encoding="utf-8", suffix=".mmd", delete=False)
        try:
            handle.write(packed)
            handle.close()
            result = subprocess.run(
                [sys.executable, os.path.join(SCRIPTS, "crypto.py"), "list", handle.name],
                capture_output=True,
                text=True,
                check=False,
            )
        finally:
            os.remove(handle.name)
        self.assertEqual(0, result.returncode)
        payload = json.loads(result.stdout)
        self.assertEqual("tip", payload[0]["hint"])
        self.assertEqual(JAVA_NOTE_CIPHERTEXT, payload[0]["ciphertext"])


if __name__ == "__main__":
    unittest.main()
