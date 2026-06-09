# Changelog

All notable changes to this project will be documented in this file.
## [Unreleased]

### Bug Fixes

- Increase OkHttp read/write timeout to 60s for Gemini API

### CI/CD

- Mark sonarcloud and dependency-submission as continue-on-error
- Remove continue-on-error now that SonarCloud and Dependency Graph are configured
- Add workflow_dispatch trigger for manual runs
- Skip jacoco-report step on workflow_dispatch (action only supports push/PR)

### Chores

- Initial Recorda project skeleton
- Wire up tooling chosen for the free/personal-use stack

### Documentation

- Add comprehensive README with app concept, pedagogy, architecture and stack

### Features

- Replicate UrlShortener architecture and ship TopicScreen vertical slice
- **m8**: Wire real Firebase config and fix WorkManager on-demand init
- **m9**: Wire quality tooling — ktlint, detekt, dependencyGuard, lefthook
- M10 — CI/CD GitHub Actions workflows
- M10 — alinhar workflows com UrlShortener
- **m11**: Add 90% coverage gate to pre-commit hook
- **m12**: Flashcard review session screen
- **m13**: Botão Revisar + SM-2 scheduling + extração do prompt
- **m14**: Due-date filtering, Tudo em dia screen + changelog config
- **m15**: Exclusão de tópico com confirmação
- **m16**: TTS na sessão de revisão

### Refactoring

- **ui**: Substituir lista expandida por cards na tela de tópicos

### Style

- Activate OutdatedDocumentation detekt rule and fix KDoc gaps
