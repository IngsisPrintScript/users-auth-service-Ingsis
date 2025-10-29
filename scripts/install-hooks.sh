#!/usr/bin/env bash
set -euo pipefail

if ! command -v git >/dev/null 2>&1; then
	echo "git not found in PATH. Please install git and try again." >&2
	exit 1
fi

git config core.hooksPath .githooks
chmod +x .githooks/pre-commit .githooks/pre-push || true

echo "[hooks] Installed git hooks from .githooks"
echo "Run 'git config --get core.hooksPath' to verify. To uninstall: 'git config --unset core.hooksPath'"