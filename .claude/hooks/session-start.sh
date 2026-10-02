#!/bin/bash
# SessionStart hook for Claude Code cloud sessions (claude.ai/code).
#
# Cloud sessions do not install the plugins that .claude/settings.json enables, so this hook
# installs them. A plugin installed after Claude Code has started only becomes fully active in
# the next session, so the hook also tells Claude where the plugin skills are, making them usable
# right away. Put the plugin install in the cloud environment's setup script (runs before Claude
# Code starts) to have the plugins load natively from the first turn; see README.md.
set -euo pipefail

if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

cd "${CLAUDE_PROJECT_DIR:-$(pwd)}"

MARKETPLACE_REPO="vaadin/agent-marketplace"
PLUGINS=(vaadin-skills@vaadin-marketplace vaadin-agent-tools@vaadin-marketplace)
PLUGIN_CACHE="$HOME/.claude/plugins/cache/vaadin-marketplace"

log() { echo "[session-start] $*" >&2; }

# 0. JDK 25: pom.xml compiles with --release 25, the cloud image ships JDK 21.
JDK_HOME="/usr/lib/jvm/java-25-openjdk-amd64"
if [ ! -x "$JDK_HOME/bin/javac" ]; then
  export DEBIAN_FRONTEND=noninteractive
  apt-get install -y -q openjdk-25-jdk-headless >&2 \
    || { apt-get update -q >&2 && apt-get install -y -q openjdk-25-jdk-headless >&2; } \
    || log "JDK 25 install failed"
fi
if [ -x "$JDK_HOME/bin/javac" ]; then
  export JAVA_HOME="$JDK_HOME" PATH="$JDK_HOME/bin:$PATH"
  if [ -n "${CLAUDE_ENV_FILE:-}" ]; then
    echo "export JAVA_HOME=\"$JDK_HOME\" PATH=\"$JDK_HOME/bin:\$PATH\"" >> "$CLAUDE_ENV_FILE"
  fi
fi

# 1. Vaadin plugins (skills + hooks from vaadin/agent-marketplace).
if ! claude plugin marketplace list 2>/dev/null | grep -q vaadin-marketplace; then
  claude plugin marketplace add "$MARKETPLACE_REPO" >&2 || log "marketplace add failed"
fi
installed="$(claude plugin list 2>/dev/null || true)"
for plugin in "${PLUGINS[@]}"; do
  if ! grep -q "$plugin" <<<"$installed"; then
    claude plugin install "$plugin" --scope project >&2 || log "install of $plugin failed"
  fi
done

# 2. Warm the npm cache for the Playwright MCP server so it starts quickly (see .mcp.json).
npx -y @playwright/mcp@0.0.83 --help >/dev/null 2>&1 || log "could not prefetch @playwright/mcp"

# 3. Resolve Maven dependencies so `.vaadin/vaadin-dev start` and tests do not stall on downloads.
./mvnw -q -B dependency:resolve >&2 || log "maven dependency resolution failed"

# 4. Tell Claude where the plugin skills live, for sessions where the plugins were installed by
#    this hook (and are therefore not yet loaded as skills).
skills=""
for skill in "$PLUGIN_CACHE"/*/*/skills/*/SKILL.md; do
  [ -f "$skill" ] || continue
  name="$(sed -n 's/^name:[[:space:]]*//p' "$skill" | head -1)"
  # First line of the description, also when it is a folded (>) or literal (|) YAML block.
  desc="$(awk '/^description:/ { sub(/^description:[[:space:]]*/, ""); if ($0 !~ /^[>|]-?$/) { print; exit } next_line = 1; next }
               next_line { sub(/^[[:space:]]+/, ""); print; exit }' "$skill" | cut -c1-220)"
  skills+="- ${name:-$(basename "$(dirname "$skill")")}: ${skill} — ${desc}"$'\n'
done

if [ -n "$skills" ]; then
  context="Vaadin plugin skills are installed in this cloud session. If they are not listed among your available skills (they were installed after startup), use them anyway: when a task matches one, Read its SKILL.md at the path below and follow it, resolving relative references against that skill's directory. The vaadin-agent-tools CLI is at ${PLUGIN_CACHE}/vaadin-agent-tools/*/bin/vaadin-agent-tools.
${skills}"
  jq -n --arg ctx "$context" \
    '{hookSpecificOutput: {hookEventName: "SessionStart", additionalContext: $ctx}}'
fi
