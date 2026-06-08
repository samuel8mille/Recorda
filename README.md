# Recorda

App de aprendizado para pessoas com dificuldade de fixação de conteúdo (em
especial perfis com TDAH), baseado em técnicas de estudo com comprovação
científica para revisão periódica: recuperação ativa, repetição espaçada,
chunking, codificação dual, elaboração e externalização de estrutura.

O usuário informa o tema que quer aprender; o app busca material em fontes
confiáveis, gera conteúdo de revisão (cards, perguntas, jogos) e monta uma
rotina de estudo personalizada — verificando periodicamente, inclusive por
meio de testes orais avaliados por IA, o quanto foi de fato assimilado.

## Stack inicial

- Kotlin + Jetpack Compose + Material 3
- Gradle version catalog (`gradle/libs.versions.toml`)
- Retrofit + OkHttp + Gson + Coroutines — para chamar a API do Gemini
  (geração de conteúdo e busca de fontes), atrás de uma abstração própria
- `SpeechRecognizer` / `TextToSpeech` nativos do Android — testes orais e
  lembretes falados (on-device, sem custo de API)
- Algoritmo de repetição espaçada — a definir entre SM-2 (mais simples,
  bom para começar) e FSRS (mais moderno)

Combinação escolhida para manter o projeto **sem custo recorrente**, focado
em uso pessoal — ver `App_Aprendizado_TDAH_Conceito_Inicial.docx` em
Documentos para o detalhamento de cada ferramenta e seus trade-offs.

## Configuração local

A chave da API do Gemini é gratuita e não é commitada. Gere a sua em
https://aistudio.google.com/apikey e cole em `local.properties`:

```
gemini.api.key=SUA_CHAVE_AQUI
```

Ela é exposta ao código via `BuildConfig.GEMINI_API_KEY` (lida no
`app/build.gradle.kts`, mesmo padrão de segredos usado no UrlShortener).

Projeto recém-criado — esqueleto mínimo para começar a evoluir a partir daqui.
