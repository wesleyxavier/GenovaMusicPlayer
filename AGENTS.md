# GenovaMusicPlayer — Instruções para Agentes de IA

> Documentação para humanos: [README.md](README.md).
> Campos "A definir" devem ser preenchidos pelo dono do projeto. Não invente valores.

## Propósito
Player de música nativo Android em Kotlin puro, offline-only, com seleção explícita de pastas, busca local, reprodução em segundo plano e arquitetura MVVM sem bibliotecas externas.

## Stack
- Mobile (Android/Kotlin) — apenas framework Android padrão (`android.*`) e Kotlin stdlib (`compileSdk 34`, `minSdk 26`, `android.useAndroidX=false`).
- Build e testes via Docker (`gradle:8.7-jdk21` + Android SDK 34).

## Comandos
| Ação | Comando |
| ---- | ------- |
| Build | `docker build -t genova-music-player .` e `docker run --rm -v "${PWD}:/workspace" -w /workspace genova-music-player ./gradlew assembleDebug --no-daemon` |
| Testes | `docker run --rm -v "${PWD}:/workspace" -w /workspace genova-music-player ./gradlew testDebugUnitTest --no-daemon` |
| Testes E2E | `docker compose -f docker-compose.e2e.yml up --build --abort-on-container-exit` (executar **sempre** via subagente `appium-test-hydrogen` ou `appium-test-haiku`) |
| Executar | Instalar `app/build/outputs/apk/debug/app-debug.apk` no dispositivo Android |

## Regras de operação
1. **Regra Zero:** nunca presuma. Requisito ambíguo vira pergunta curta ao usuário.
2. **Autorização:** nunca `commit`, `push` ou criar PR sem "sim" explícito do usuário na conversa.
3. **Nunca sobrescreva** arquivo existente sem perguntar.
4. **Docker Isolation:** Todo build/teste roda em container; nada de toolchain local.
5. **Testes E2E Appium:** DEVEM ser sempre executados exclusivamente por um dos subagentes dedicados (`appium-test-hydrogen` em `.agents/agents/appium-test-hydrogen.md` / `.github/agents/appium-test-hydrogen.agent.md` ou `appium-test-haiku` em `.github/agents/appium-test-haiku.agent.md`) usando `docker-compose.e2e.yml`.
6. **GitHub MCP:** Operações de Pull Request e repositório remoto no GitHub DEVEM priorizar o **MCP do GitHub** quando disponível na sessão.
7. **graphify:** com `graphify-out/graph.json` presente, consulte `graphify query "<pergunta>"` antes de varrer arquivos. Após alterar código, rode `graphify update .`.

## Fluxo de especificação
- **Spec Kit** (`.specify/` e `specs/`), regido por `.specify/memory/constitution.md`.
- **Worktree (`openspec-worktree`)**: todo `tasks.md` deve iniciar com `## 0. Branch` criando worktree em `worktrees/{branch}/` (branches `feature/`, `bugfix/` ou `hotfix/`), validando a branch base (`develop` ou feature anterior) antes de criar e removendo o worktree após o merge.

## Convenções
- Arquitetura MVVM pura (sem AndroidX, LiveData ou Flow); estado global do player em singleton com callbacks.
- Lógica de pastas e filtros em classes Kotlin puras (sem dependência de `android.*`).
- Identificadores de código (classes, funções, variáveis) em inglês; textos de UI e mensagens de erro em português do Brasil.

## Restrições
- Zero bibliotecas de terceiros (sem AndroidX, Material Components, Compose, RecyclerView, Media3 ou Coroutines).
- Sem backend, conta de usuário, streaming ou sincronização na nuvem.
- Apenas pastas selecionadas via `ACTION_OPEN_DOCUMENT_TREE`, respeitando a regra de hierarquia.
