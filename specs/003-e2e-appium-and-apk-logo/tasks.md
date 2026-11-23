# Tasks: Infraestrutura E2E Appium, Logo do APK e Diretriz GitHub MCP

**Input**: Design documents from `specs/003-e2e-appium-and-apk-logo/`

## 0. Branch

- [X] T001 Criar worktree e branch `feature/003-e2e-appium-and-apk-logo` a partir de `develop` em `worktrees/003-e2e-appium-and-apk-logo`

## Phase 1: Logo Oficial do APK (US1)

- [X] T002 [P] [US1] Gerar `ic_launcher.png` e `ic_launcher_round.png` nas densidades `mipmap-mdpi` a `mipmap-xxxhdpi` a partir do logo oficial em `app/src/main/res/mipmap-*/`
- [X] T003 [US1] Configurar `android:icon="@mipmap/ic_launcher"` e `android:roundIcon="@mipmap/ic_launcher_round"` em `app/src/main/AndroidManifest.xml`

## Phase 2: Infraestrutura E2E Appium em Docker (US2)

- [X] T004 [P] [US2] Criar `e2e/requirements.txt`, `e2e/Dockerfile` e `e2e/pytest.ini` com dependências Appium isoladas do APK em `e2e/`
- [X] T005 [P] [US2] Criar `docker-compose.e2e.yml` com serviço `appium` e serviço `e2e-runner` conectado via ADB TCP ao host em `docker-compose.e2e.yml`
- [X] T006 [US2] Criar `e2e/conftest.py` e `e2e/tests/test_smoke_screens.py` capturando screenshots em `e2e/artifacts/screenshots/<cenario>/<tela>.png` em `e2e/`

## Phase 3: Documentação Operacional (US3) & Validação

- [X] T007 [P] [US3] Atualizar `AGENTS.md` com seção de MCP do GitHub e obrigatoriedade dos subagentes `appium-test-hydrogen` / `appium-test-haiku` para E2E em `AGENTS.md`
- [X] T008 [P] Atualizar `README.md` com instruções do `docker-compose.e2e.yml` em `README.md`
- [X] T009 Validar `./gradlew testDebugUnitTest assembleDebug --no-daemon` em Docker e `docker compose -f docker-compose.e2e.yml config`
- [X] T010 Executar `graphify update .`, commit, push, criar/mergear PR e dar `git pull` em `develop`
