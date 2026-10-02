#!/bin/bash
# Starts the Playwright MCP server used by the vaadin-devloop skill for browser verification.
# In Claude Code cloud sessions it reuses the preinstalled Chromium (no browser download);
# locally it falls back to whatever browser Playwright already has installed.
set -euo pipefail

args=(--headless --isolated)
if [ "${CLAUDE_CODE_REMOTE:-}" = "true" ] && [ -x /opt/pw-browsers/chromium ]; then
  args+=(--browser chromium --executable-path /opt/pw-browsers/chromium --no-sandbox)
fi

exec npx -y @playwright/mcp@0.0.83 "${args[@]}" "$@"
