# My Application README

- [ ] TODO Replace or update this README with instructions relevant to your application

## Project Structure

This project has the following structure:

```
src
├── main/java
│   └── [application package]
│       ├── base
│       │   └── ui
│       │       ├── MainLayout.java
│       │       └── ViewTitle.java
│       ├── examplefeature
│       │   ├── ui
│       │   │   └── TaskListView.java
│       │   ├── Task.java
│       │   ├── TaskRepository.java
│       │   └── TaskService.java                
│       └── Application.java     
├── main/resources
│   ├── META-INF
│   │   └── resources
│   │       ├── icons
│   │       │   └── clipboard-check.svg
│   │       ├── styles.css
│   │       └── view-title.css
│   └── application.properties 
└── test/java
    └── [application package]
        └── examplefeature
            ├── ui
            │   └── TaskListViewTest.java
            └── TaskServiceTest.java                 
```

The main entry point into the application is `Application.java`. This class contains the `main()` method that starts up 
the Spring Boot application.

The project follows a *feature-based package structure*, organizing code by *functional units* rather than traditional 
architectural layers. It includes two feature packages: `base` and `examplefeature`.

* The `base` package contains classes meant for reuse across different features, either through composition or 
  inheritance. You can use them as-is, tweak them to your needs, or remove them.
* The `examplefeature` package is an example feature package that demonstrates the structure. It represents a 
  *self-contained unit of functionality*, including UI components, business logic, data access, and an integration test.
  Once you create your own features, *you'll remove this package*.


## Starting in Development Mode

To start the application in development mode, import it into your IDE and run the `Application` class. 
You can also start the application from the command line by running: 

```bash
./mvnw
```

## Building for Production

To build the application in production mode, run:

```bash
./mvnw package
```

To build a Docker image, run:

```bash
docker build -t my-application:latest .
```

If you use commercial components, pass the license key as a build secret:

```bash
docker build --secret id=proKey,src=$HOME/.vaadin/proKey .
```

## Agentic Development (Claude Code on the web)

The repository carries everything a Claude Code cloud session (claude.ai/code) needs:

| What | Where | Loaded in cloud sessions by |
|------|-------|-----------------------------|
| `vaadin-devloop` skill + `vaadin-dev` CLI | `.claude/skills/`, `.agents/skills/`, `.vaadin/` | committed files |
| Vaadin docs MCP (`https://mcp.vaadin.com/docs`) | `.mcp.json` | committed files |
| Playwright MCP (browser verification, uses the preinstalled Chromium) | `.mcp.json`, `.claude/scripts/playwright-mcp.sh` | committed files |
| `vaadin-skills` and `vaadin-agent-tools` plugins | `.claude/settings.json` (`enabledPlugins`) | `.claude/hooks/session-start.sh` |
| JDK 25 (the cloud image ships JDK 21), Maven dependencies | — | `.claude/hooks/session-start.sh` |

Cloud sessions do not install the plugins that `enabledPlugins` lists, so the SessionStart hook installs
them. A plugin installed after Claude Code has started is only fully active from the next session on,
so the hook also hands Claude the paths of the plugin skills. To have the plugins (including the
`vaadin-agent-tools` post-edit theme check) load natively from the first turn, add this to the cloud
environment's **setup script** (environment menu in the session title bar → Edit), which runs before
Claude Code starts:

```bash
apt-get install -y openjdk-25-jdk-headless || (apt-get update && apt-get install -y openjdk-25-jdk-headless)
claude plugin marketplace add vaadin/agent-marketplace
claude plugin install vaadin-skills@vaadin-marketplace
claude plugin install vaadin-agent-tools@vaadin-marketplace
```

## Next Steps

The [Building Apps](https://vaadin.com/docs/v25/building-apps) guides contain hands-on advice for adding features to 
your application.
