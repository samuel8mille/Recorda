# Changelog

All notable changes to this project will be documented in this file.
## [Unreleased]

### Bug Fixes

- **test**: Inicializar WorkManager antes do launch da activity nos testes E2E
- **cd**: Derivar versão do app do CHANGELOG.md em vez de tag inexistente
- **e2e**: Instalar APK do feature dinâmico no Test Lab e desabilitar config cache no OWASP
- **e2e**: Localizar APK do feature dinâmico dinamicamente e desabilitar parallel no OWASP
- **e2e**: Usar app bundle (.aab) para incluir o feature dinâmico no Test Lab
- **e2e**: Mudar review_session para entrega install-time
- **e2e**: Adicionar fusing config exigida pelo bundletool no módulo review_session

### Documentation

- **readme**: Documentar build e2e via AAB e regra de módulos dinâmicos

### Testing

- **integration+e2e**: Adicionar testes de DAO e fluxo de tópicos

### Debug

- **e2e**: Coletar logcat do dispositivo no Test Lab quando o job falhar
## [0.0.1] - 2026-06-09

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
