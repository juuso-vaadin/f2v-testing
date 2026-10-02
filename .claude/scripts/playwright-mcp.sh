#!/bin/bash
# Starts the Playwright MCP server used by the vaadin-devloop skill for browser verification.
# In Claude Code cloud sessions it reuses the preinstalled Chromium (no browser download);
# locally it falls back to whatever browser Playwright already has installed.
#
# The server is installed into its own directory under a lock rather than run through `npx`:
# at session start the SessionStart hook prefetches it while Claude Code launches this server,
# and two concurrent `npx` installs into the same npx cache leave a half-extracted
# playwright-core behind (MODULE_NOT_FOUND .../playwright-core/lib/coreBundle.js), so the server
# fails to connect for the whole session. `--install-only` installs and exits (used by the hook).
set -euo pipefail

VERSION="0.0.83"
PREFIX="${XDG_CACHE_HOME:-$HOME/.cache}/claude-playwright-mcp/$VERSION"
BIN="$PREFIX/node_modules/.bin/playwright-mcp"

install() {
  mkdir -p "$PREFIX"
  (
    flock 9
    # The marker is written only after a complete install, so an interrupted one is redone.
    if [ ! -f "$PREFIX/.installed" ] || [ ! -x "$BIN" ]; then
      rm -rf "$PREFIX/node_modules"
      npm install --prefix "$PREFIX" --no-save --no-audit --no-fund --loglevel=error \
        "@playwright/mcp@$VERSION" >&2
      touch "$PREFIX/.installed"
    fi
  ) 9>"$PREFIX.lock"
}

install
[ "${1:-}" = "--install-only" ] && exit 0

args=(--headless --isolated)
if [ "${CLAUDE_CODE_REMOTE:-}" = "true" ] && [ -x /opt/pw-browsers/chromium ]; then
  args+=(--browser chromium --executable-path /opt/pw-browsers/chromium --no-sandbox)
fi

exec "$BIN" "${args[@]}" "$@"
