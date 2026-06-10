# Recorda

> App de aprendizado para pessoas com dificuldade de fixação de conteúdo — especialmente perfis com TDAH — baseado em técnicas com comprovação científica.

---

## O que é o Recorda

Recorda é um app Android de estudo personalizado que combina geração de conteúdo por IA com métodos cognitivos validados pela literatura de aprendizagem. O objetivo é simples: o usuário informa o tema que quer aprender, e o app se encarrega do resto — gerar o material, montar a rotina de revisão e verificar periodicamente o quanto foi assimilado de verdade.

O foco inicial é em **flashcards gerados automaticamente via Gemini**, com expansão planejada para testes orais avaliados por IA, mapas mentais e rotinas de revisão espaçada.

---

## Fundamentos pedagógicos

O Recorda não é apenas um gerador de conteúdo — a mecânica do app é construída sobre técnicas com evidência científica sólida:

| Técnica | O que é | Por que está no Recorda |
|---|---|---|
| **Recuperação ativa** | Tentar lembrar a informação em vez de apenas relê-la | Flashcards e testes orais forçam evocação, não reconhecimento |
| **Repetição espaçada** | Revisar na frequência certa para o esquecimento | Algoritmo SM-2 / FSRS agenda revisões antes que o conteúdo se perca |
| **Chunking** | Agrupar informações relacionadas em unidades menores | O Gemini divide o tema em cards atômicos — um conceito por vez |
| **Codificação dual** | Combinar texto e imagem/áudio para fixação mais forte | Testes orais (fala + escuta) ampliam os canais de codificação |
| **Elaboração** | Conectar novo conteúdo ao que já se sabe | Prompts do Gemini incluem contexto e analogias, não apenas definições |
| **Externalização de estrutura** | Colocar o conhecimento "fora da cabeça" | Os cards e mapas mentais tornam a estrutura do tema visível e navegável |

Pessoas com TDAH frequentemente têm dificuldade com memória de trabalho e autorregulação do estudo. O Recorda endereça isso removendo atrito (basta digitar o tema), automatizando a agenda de revisão e tornando o progresso visível.

---

## Features atuais

- **Geração de flashcards via Gemini** — o usuário digita um tema e o app gera 5 flashcards no formato `Pergunta / Resposta` usando a API `generateContent`
- **Persistência offline-first** — flashcards salvos localmente via Room; temas ficam na fila (`PENDING`) se o dispositivo estiver sem internet e são sincronizados automaticamente quando a conexão retorna via WorkManager
- **Retry automático** — `GenerateContentWorker` com backoff exponencial reprocessa tópicos pendentes assim que há rede disponível
- **Validação de input** — detecção de tema vazio e duplicado antes de chamar a API
- **Swap Pattern** — `GeminiService`, `AnalyticsTracker` e `CrashReporter` são interfaces; debug usa implementações que só logam, release usa Firebase Analytics e Crashlytics
- **Observabilidade** — Timber (debug tree / Crashlytics tree), LeakCanary no debug, Chucker para inspeção de rede no debug
- **Dynamic feature** — módulo `:feature:review_session` como template para futuras features entregues sob demanda via Play Feature Delivery

---

## Roadmap

- [x] Algoritmo de repetição espaçada (SM-2 ou FSRS) para agendar revisões
- [x] Testes orais — `SpeechRecognizer` captura a resposta falada, Gemini avalia
- [x] `TextToSpeech` para leitura em voz alta dos cards
- [ ] Mapa mental gerado a partir dos flashcards de um tema
- [ ] Estatísticas de retenção por tema
- [ ] FSRS (substituir SM-2 pelo agendador mais moderno)

---

## Arquitetura

```
:app                    — telas, ViewModels, domínio, dados, DI, Application
:core:mvi               — framework MVI genérico (ScreenUiState / UiState / UiEvent / UiEffect)
:core:network           — Retrofit + OkHttp + Gson + ServiceExecutor + NetworkError
:core:ui                — ProcessContainer, LoadingScreen, tema compartilhado
:lint                   — regras Lint customizadas (NoAndroidLog, A11yHardcodedColor)
:macrobenchmark         — benchmarks de startup e renderização
:feature:review_session — módulo dinâmico (template para futuras features)
```

O `:app` segue arquitetura **MVI + Hilt + Clean Architecture** em camadas:

```
presentation/  → ViewModel (UiState / UiEvent / UiEffect) + Composables + Navigation
domain/        → modelos, repositórios (interfaces), use cases
data/          → Room (offline-first), Retrofit/Gemini, mappers, WorkManager
di/            → DataModule, DomainModule + módulos de swap debug/release
```

### Swap Pattern

Interfaces implementadas de forma diferente por variante de build:

| Interface | Debug | Release |
|---|---|---|
| `GeminiService` | `RetrofitGeminiService` | `RetrofitGeminiService` |
| `AnalyticsTracker` | `LoggingAnalyticsTracker` (Timber) | `FirebaseAnalyticsTracker` |
| `CrashReporter` | `LoggingCrashReporter` (Timber) | `CrashlyticsReporter` |

---

## Stack técnico

| Camada | Tecnologia |
|---|---|
| UI | Kotlin + Jetpack Compose + Material 3 |
| Navegação | Navigation Compose |
| DI | Hilt + KSP |
| Rede | Retrofit + OkHttp + Gson + Chucker (debug) |
| IA | Google Gemini API (`generateContent`) |
| Persistência | Room + WorkManager |
| Observabilidade | Timber + Firebase Crashlytics + Firebase Analytics + Firebase Performance + LeakCanary |
| Qualidade | detekt + ktlint + Android Lint + dependencyGuard + OWASP dependency-check |
| Hooks | lefthook (pre-commit: ktlint / detekt / testes; pre-push: lint) |
| CI/CD | GitHub Actions (em configuração) |

Stack escolhida para manter o projeto **sem custo recorrente** em uso pessoal: Gemini API tem tier gratuito generoso, Firebase tem plano Spark gratuito, e todo o processamento de áudio é on-device via APIs nativas do Android.

---

## Configuração local

### 1. Chave da API do Gemini

Gratuita — gere a sua em https://aistudio.google.com/apikey e adicione ao `local.properties` (não commitado):

```properties
gemini.api.key=SUA_CHAVE_AQUI
```

Exposta ao código via `BuildConfig.GEMINI_API_KEY`.

### 2. Chave NVD (opcional — apenas para scans OWASP)

```properties
nvd.apiKey=SUA_CHAVE_NVD
```

Gratuita em https://nvd.nist.gov/developers/request-an-api-key. Sem ela o scan OWASP ainda funciona, mas mais lento.

### 3. Build

```bash
./gradlew :app:assembleDebug
```

---

## Qualidade

```bash
# formatação
./gradlew ktlintCheck
# análise estática
./gradlew detekt
# lint Android
./gradlew :app:lintDebug
# garante que :core não depende de :app
./gradlew validateModuleGraph
# detecta mudanças no grafo de dependências
./gradlew dependencyGuard
# scan OWASP CVE (requer nvd.apiKey)
./gradlew dependencyCheckAggregate
```

---

## Testes E2E e módulos dinâmicos

Os testes instrumentados (incluindo os do `:feature:review_session`) rodam no **Firebase Test Lab via CI** (`.github/workflows/e2e.yml`), não localmente.

### Rodando localmente (opcional, para depuração)

`./gradlew :app:connectedDebugAndroidTest` **falha** com `ClassNotFoundException: ReviewSessionInitProvider`: essa task instala apenas `app-debug.apk` + o APK de testes, nunca o split do módulo dinâmico `:feature:review_session` — mesmo que o manifesto mesclado declare o provider. Para rodar localmente num emulador/dispositivo conectado, instale os APKs manualmente e dispare via `am instrument`:

```bash
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest :feature:review_session:assembleDebug

adb install-multiple -r -t \
  app/build/outputs/apk/debug/app-debug.apk \
  feature/review_session/build/outputs/apk/debug/review_session-debug.apk
adb install -r -t app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk

adb shell am instrument -w -r \
  com.samuelribeiro.recorda.test/com.samuelribeiro.recorda.HiltTestRunner
```

> Na primeira execução após uma instalação limpa, é possível ver uma falha isolada e não-determinística (ex.: timing de cold-start). Rode novamente antes de investigar — se a segunda execução passar 100%, foi flake.

### Build para o Test Lab

```bash
./gradlew bundleDebug assembleDebugAndroidTest
```

O Test Lab recebe o **App Bundle** (`.aab`), não o APK: `gcloud firebase test android run --app app-debug.aab` usa o bundletool para gerar o conjunto de APKs específico do dispositivo (base + splits de ABI/densidade).

### Regra para módulos dinâmicos

O conjunto de APKs gerado pelo bundletool para um dispositivo **não inclui módulos `on-demand`** — apenas o módulo base e seus splits. Para que um `:feature:*` dinâmico seja instalado e coberto pelo e2e, o `AndroidManifest.xml` do módulo precisa declarar:

```xml
<dist:module
    dist:instant="false"
    dist:title="@string/title_xxx">
    <dist:delivery>
        <dist:install-time />
    </dist:delivery>
    <dist:fusing dist:include="true" />
</dist:module>
```

- `<dist:install-time />` faz o módulo entrar nos splits gerados para o dispositivo (em vez de ficar disponível só sob demanda)
- `<dist:fusing dist:include="true" />` é **obrigatório em qualquer modo de entrega** — sem ele o build do bundle falha com `Module 'X' must specify its fusing configuration`

Use isso como modelo ao criar novos módulos dinâmicos que precisem de cobertura e2e.
