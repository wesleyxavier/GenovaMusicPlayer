---
description: "Task list for MVP Parte 2 — Playlists e Refinamentos"
---

# Tasks: MVP Parte 2 — Playlists e Refinamentos

**Input**: Design documents from `specs/002-mvp-part2-playlists-and-refinements/`
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/playlists-and-refinements-contracts.md`, `quickstart.md`

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode rodar em paralelo (arquivos diferentes, sem dependência de tarefas incompletas)
- **[Story]**: User Story correspondente (`[US1]`, `[US2]`, `[US3]`, `[US4]`)
- Todos os comandos de build/teste Android/Kotlin DEVEM ser executados via Docker (regra **Docker Isolation**).
- Commits, pushes e criação de PR exigem autorização explícita do usuário no momento da execução.

---

## 0. Branch

- [X] T001 Criar worktree e branch `feature/002-mvp-part2-playlists-and-refinements` a partir da branch base (`develop` ou `feature/001-mvp-part1-player`, skill `openspec-worktree`) em `worktrees/002-mvp-part2-playlists-and-refinements`

---

## Phase 1: Foundational (Textos & Navegação Compartilhada)

**Purpose**: Recursos comuns de string em PT-BR e suporte à terceira aba `"Playlists"` na barra inferior.

- [X] T002 Adicionar strings de playlists (criar, renomear, excluir, adicionar/remover música, erros de nome vazio/duplicado, aba `"Playlists"`) e `contentDescription` em `app/src/main/res/values/strings.xml`

---

## Phase 2: User Story 1 - Executar Música ao Clicar na Lista (Priority: P1) 🎯 MVP

**Goal**: Garantir que tocar em qualquer faixa na biblioteca dispare imediatamente a reprodução via `PlaybackService` usando a lista visível.

**Independent Test**: Tocar em qualquer faixa na tela `Músicas` e verificar início imediato da reprodução e destaque da faixa ativa.

- [X] T003 [US1] Garantir disparo imediato de `PlaybackService` (`ACTION_PLAY`) com a faixa selecionada e lista ativa ao clicar em item da `ListView` em `app/src/main/java/com/genova/musicplayer/ui/LibraryActivity.kt` e `app/src/main/java/com/genova/musicplayer/viewmodel/LibraryViewModel.kt`

---

## Phase 3: User Story 2 - Criar e Gerenciar Playlists Locais (Priority: P1) 🎯 MVP

**Goal**: Implementar lógica pura, persistência local, ViewModel e telas para criar, renomear, excluir playlists e adicionar/remover músicas.

**Independent Test**: Acessar aba `"Playlists"`, criar playlist, validar erro para nome vazio/duplicado, adicionar música pelo menu `⋮` da biblioteca, renomear, remover música, reproduzir ao clicar na faixa e excluir playlist com confirmação.

### Tests for User Story 2
- [X] T004 [P] [US2] Criar testes unitários em Kotlin puro cobrindo criação, nome vazio, nome duplicado (case-insensitive), renomeação, exclusão e adição/remoção de faixas sem duplicata em `app/src/test/java/com/genova/musicplayer/domain/playlist/PlaylistManagerTest.kt`

### Implementation for User Story 2
- [X] T005 [P] [US2] Implementar `Playlist` e `PlaylistValidationResult` em Kotlin puro em `app/src/main/java/com/genova/musicplayer/domain/playlist/Playlist.kt` e `app/src/main/java/com/genova/musicplayer/domain/playlist/PlaylistValidationResult.kt`
- [X] T006 [US2] Implementar `PlaylistManager` em Kotlin puro (regras de criar, renomear, excluir, adicionar sem duplicata e remover faixa) em `app/src/main/java/com/genova/musicplayer/domain/playlist/PlaylistManager.kt`
- [X] T007 [US2] Implementar `PlaylistPreferencesRepository` usando `SharedPreferences` + `org.json.JSONArray`/`JSONObject` em `app/src/main/java/com/genova/musicplayer/data/PlaylistPreferencesRepository.kt`
- [X] T008 [US2] Implementar `PlaylistsViewModel` em Kotlin comum com callbacks em `app/src/main/java/com/genova/musicplayer/viewmodel/PlaylistsViewModel.kt`
- [X] T009 [P] [US2] Criar layouts XML `activity_playlists.xml`, `activity_playlist_detail.xml` e `item_playlist.xml` no tema **Nocturne Violet** (alvos `>= 48dp` e `contentDescription`) em `app/src/main/res/layout/activity_playlists.xml`, `app/src/main/res/layout/activity_playlist_detail.xml` e `app/src/main/res/layout/item_playlist.xml`
- [X] T010 [US2] Atualizar barra de navegação inferior para 3 abas (`"Músicas"`, `"Playlists"`, `"Pastas"`) e incluir ação `"Adicionar à playlist"` no menu `⋮` da biblioteca em `app/src/main/res/layout/activity_library.xml`, `app/src/main/res/layout/activity_folders.xml` e `app/src/main/java/com/genova/musicplayer/ui/LibraryActivity.kt`
- [X] T011 [US2] Implementar `PlaylistListAdapter`, `PlaylistsActivity` e `PlaylistDetailActivity` (incluindo execução imediata ao clicar em música, renomear, remover música e excluir com confirmação) e registrar Activities em `app/src/main/java/com/genova/musicplayer/ui/PlaylistListAdapter.kt`, `app/src/main/java/com/genova/musicplayer/ui/PlaylistsActivity.kt`, `app/src/main/java/com/genova/musicplayer/ui/PlaylistDetailActivity.kt` e `app/src/main/AndroidManifest.xml`

### 3.1 Commit Group 1
- [ ] T012 Solicitar autorização e executar `git commit` + `git push` da implementação de Playlists (US1 + US2)

---

## Phase 4: User Story 3 - Simplificar Tela "Tocando Agora" Usando Saída Ativa (Priority: P2)

**Goal**: Remover da tela `"Tocando Agora"` o cartão e botão `"Trocar"` dispositivo de áudio, utilizando sempre a rota ativa do sistema.

**Independent Test**: Abrir `"Tocando Agora"` e confirmar ausência do cartão/botão `"Trocar"`, mantendo reprodução normal na saída ativa e pausa ao desconectar fones.

- [X] T013 [P] [US3] Remover cartão `"Dispositivo de Áudio"` e botão `"Trocar"` do layout em `app/src/main/res/layout/activity_now_playing.xml`
- [X] T014 [US3] Remover lógica/listeners de troca manual de saída de áudio mantendo uso automático da rota ativa em `app/src/main/java/com/genova/musicplayer/ui/NowPlayingActivity.kt`

---

## Phase 5: User Story 4 - Ocultar Regra de Hierarquia da Tela de Pastas (Priority: P2)

**Goal**: Remover o card informativo permanente `"Regra de Hierarquia"` da tela `"Pastas"`, exibindo apenas aviso em português quando o usuário tentar inclusão inválida.

**Independent Test**: Abrir `"Pastas"` e confirmar ausência de texto permanente sobre hierarquia; tentar adicionar subpasta de `/Music` e verificar aviso claro em português.

- [X] T015 [P] [US4] Remover o card informativo permanente `"Regra de Hierarquia"` e atualizar navegação inferior para 3 abas em `app/src/main/res/layout/activity_folders.xml`
- [X] T016 [US4] Garantir exibição do aviso em português apenas na tentativa inválida de hierarquia em `app/src/main/java/com/genova/musicplayer/ui/FoldersActivity.kt`

### 5.1 Commit Group 2
- [ ] T017 Solicitar autorização e executar `git commit` + `git push` dos refinamentos de UI (US3 + US4)

---

## Phase 6: Testes, Deploy no Celular & Pre-PR Checklist

**Purpose**: Validar testes unitários via Docker, gerar APK de debug para deploy direto no celular, atualizar `README.md`, sincronizar grafo e abrir PR.

- [X] T018 Executar suíte completa de testes unitários e build debug via Docker (`./gradlew testDebugUnitTest assembleDebug`) e disponibilizar `app/build/outputs/apk/debug/app-debug.apk` para deploy direto no celular
- [ ] T019 Executar validação manual no celular conforme `specs/002-mvp-part2-playlists-and-refinements/quickstart.md`
- [X] T020 [P] Atualizar `README.md` documentando as funcionalidades de playlists (Parte 2) e fluxos atualizados em `README.md`
- [X] T021 Executar `graphify update .` na raiz do projeto
- [ ] T022 Solicitar autorização explícita do usuário para `git commit` final + `git push` e criação do PR para a branch alvo

---

## Dependencies & Execution Order

- **Branch (`T001`)** → **Phase 1 (`T002`)**.
- **US1 (`T003`)**, **US2 (`T004–T012`)**, **US3 (`T013–T014`)** e **US4 (`T015–T016`)** dependem da base `001-mvp-part1-player` e de `T002`.
- **US3** e **US4** podem ser executadas em paralelo com **US2** (arquivos distintos, exceto `activity_folders.xml` na barra inferior já coordenado em `T010`/`T015`).
- **Phase 6 (`T018–T022`)** depende da conclusão de todas as histórias.
