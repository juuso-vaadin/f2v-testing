# Project notes for Claude Code

This is a Vaadin 25.3 Flow (Java) application. It runs through the `vaadin-devloop` daemon
(`.vaadin/vaadin-dev`), not through Maven — see the `vaadin-devloop` skill.

## Figma-to-Vaadin workflow

The `figma-to-vaadin-orchestrator` skill and the skills it delegates to assume the app is
stopped and restarted with `appStartCommand` from `.figma-to-vaadin/state.json` whenever code
changes. In this project, the dev loop fills that role. It applies to all five skills:
`figma-to-vaadin-orchestrator`, `figma-to-aura-theme`, `figma-to-lumo-theme`,
`figma-to-vaadin` and `vaadin-visual-verification`.

- **Manifest values.** Record `"appStartCommand": ".vaadin/vaadin-dev start"` and
  `"appBaseUrl": "http://localhost:8080"`. If an existing manifest names a Maven command
  (`./mvnw spring-boot:run` or similar), correct it. Never start the app through Maven: the
  daemon owns the process, and a second one fights it for the port.
- **Starting.** Wherever a skill says to start the app, run `.vaadin/vaadin-dev status`, then
  `.vaadin/vaadin-dev start` if it reports `stopped`.
- **Restarting after a code change.** Wherever a skill says to stop and restart the app so that
  it serves current code, run `.vaadin/vaadin-dev apply` instead. Exit code `0` is
  authoritative evidence that the change is live, which is what the restart rule exists to
  guarantee. Treat any other exit code, or an `app log: N error(s)` line, as a failure to fix,
  not a reason to restart. Use `.vaadin/vaadin-dev restart` only when the dev loop says a
  restart is needed (for example after an `application.properties` change).
- **Build check at the end of theme configuration.** A successful `start` or `apply` confirms
  that the app builds and serves. No separate Maven build is needed.
- **Verification.** Keep one browser page open on the target route across applies, as the
  dev-loop skill describes, so CSS and theme pushes land in it. Reload the page after an apply
  that reports a restart.
