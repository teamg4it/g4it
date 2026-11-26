# This file has been created (totally or partially) with the assistance of artificial intelligence tools.
# All content has been generated under the direct supervision of a named individual,
# and under the AI.Backbone Orchestrator Compliance framework

from pathlib import Path
import subprocess
import tempfile
import unittest


SCRIPT = Path(__file__).with_name("g4it48-live-e2e.sh").resolve()


class AccountConfigurationTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.root = Path(self.directory.name)
        self.config = self.root / "account.config"
        self.arguments = self.root / "curl-arguments.txt"
        self.bin = self.root / "bin"
        self.bin.mkdir()
        for name, body in {
            "curl": 'printf "%s\\n" "$@" > "$CURL_ARGUMENTS_FILE"\nexit 42\n',
            "jq": "cat >/dev/null\n",
        }.items():
            executable = self.bin / name
            executable.write_text("#!/bin/bash\n" + body)
            executable.chmod(0o700)

    def run_script(self, config, **overrides):
        if config is not None:
            self.config.write_bytes(config.encode("utf-8"))
        environment = {
            "PATH": f"{self.bin}:/usr/bin:/bin",
            "G4IT_E2E_ENV_FILE": str(self.config),
            "WORK": str(self.root / "work"),
            "CURL_ARGUMENTS_FILE": str(self.arguments),
            **overrides,
        }
        return subprocess.run(
            ["/bin/bash", str(SCRIPT)],
            cwd=self.root,
            env=environment,
            capture_output=True,
            text=True,
            timeout=10,
        )

    def assert_account_sent(self, result, user, value):
        self.assertEqual(42, result.returncode, result.stderr)
        arguments = self.arguments.read_text().splitlines()
        self.assertIn(f"username={user}", arguments)
        self.assertIn(f"password={value}", arguments)
        self.assertEqual("", result.stdout)
        self.assertEqual("", result.stderr)

    def test_loads_literal_quoted_values_with_crlf_and_no_final_newline(self):
        value = "$(printf must-stay-literal) # sample"
        marker = self.root / "unexpected-execution"
        result = self.run_script(
            "# account fixture\r\n"
            f"UNRELATED=$(touch {marker})\r\n"
            "export G4IT_E2E_USER = user@example.com # account\r\n"
            f"G4IT_E2E_PASSWORD='{value}'"
        )
        self.assert_account_sent(result, "user@example.com", value)
        self.assertFalse(marker.exists())

    def test_supports_double_quotes_comments_and_last_assignment(self):
        result = self.run_script(
            "G4IT_E2E_USER=unused@example.com\n"
            '  G4IT_E2E_USER="user@example.com" # selected account\n'
            'G4IT_E2E_PASSWORD="$(printf must-stay-literal) # value" # comment\n'
        )
        self.assert_account_sent(result, "user@example.com", "$(printf must-stay-literal) # value")

    def test_environment_values_take_precedence_and_do_not_require_a_file(self):
        result = self.run_script(
            None,
            G4IT_E2E_USER="user@example.com",
            G4IT_E2E_PASSWORD="supplied-value",
        )
        self.assert_account_sent(result, "user@example.com", "supplied-value")

    def test_loads_only_missing_environment_values(self):
        result = self.run_script(
            "G4IT_E2E_USER=unused@example.com\n"
            "G4IT_E2E_PASSWORD=literal#value # comment\n",
            G4IT_E2E_USER="user@example.com",
        )
        self.assert_account_sent(result, "user@example.com", "literal#value")

    def test_invalid_configuration_stops_before_network_or_work_directory(self):
        cases = [
            (None, "Cannot read E2E account configuration"),
            ("G4IT_E2E_USER=user@example.com\n", "G4IT_E2E_PASSWORD"),
            ("G4IT_E2E_PASSWORD='unclosed-value\n", "Invalid quoted value"),
        ]
        for config, message in cases:
            with self.subTest(message=message):
                result = self.run_script(config)
                self.assertNotEqual(0, result.returncode)
                self.assertIn(message, result.stderr)
                self.assertNotIn("unclosed-value", result.stderr)
                self.assertFalse(self.arguments.exists())
                self.assertFalse((self.root / "work").exists())


if __name__ == "__main__":
    unittest.main()
