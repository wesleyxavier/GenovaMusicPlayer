---
description: "Task list for MVP Parte 3 Ã¢â‚¬â€ Atalho Tocando Agora, Mini-Player, Fila a Seguir e EvoluÃƒÂ§ÃƒÂ£o de Playlists"
---

# Tasks: MVP Parte 3 Ã¢â‚¬â€ Atalho "Tocando Agora", Mini-Player, Fila a Seguir e EvoluÃƒÂ§ÃƒÂ£o de Playlists

**Input**: Design documents from `specs/004-part3-queue-miniplayer-playlists/`
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/part3-ui-and-domain-contracts.md`, `quickstart.md`

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Pode rodar em paralelo (arquivos diferentes, sem dependÃƒÂªncia de tarefas incompletas)
- **[Story]**: User Story correspondente (`[US1]`, `[US2]`, `[US3]`, `[US4]`, `[US5]`)
- Todos os comandos de build/teste Android/Kotlin DEVEM ser executados via Docker (regra **Docker Isolation**).
- Testes E2E Appium DEVEM ser executados exclusivamente via subagente `appium-test-hydrogen` (ou `appium-test-haiku`).
- Commits, pushes e criaÃƒÂ§ÃƒÂ£o de PR exigem autorizaÃƒÂ§ÃƒÂ£o explÃƒÂ­cita do usuÃƒÂ¡rio no momento da execuÃƒÂ§ÃƒÂ£o.

---

## 0. Branch

- [X] T001 Criar worktree e branch `feature/004-part3-queue-miniplayer-playlists` a partir da branch `develop` (skill `openspec-worktree`) em `worktrees/004-part3-queue-miniplayer-playlists`

---

## Phase 1: Foundational (Recursos Visuais, Strings PT-BR & Estado Base)

**Purpose**: Infraestrutura compartilhada de strings, ÃƒÂ­cones vetoriais Nocturne Violet e atualizaÃƒÂ§ÃƒÂ£o dos modelos de sessÃƒÂ£o e estatÃƒÂ­sticas necessÃƒÂ¡rios para todas as histÃƒÂ³rias.

- [X] T002 [P] Adicionar strings em PT-BR e `contentDescription` (chip `"Tocando Agora"`, `"Fila A Seguir"`, `"A Seguir na Fila"`, `"Auto-play apÃƒÂ³s o fim da fila"`, `"Limpar"`, `"Salvar Playlist"`, `"Ordem manual"`, `"Adicionar ÃƒÂ  fila"`, `"Fixar no topo da biblioteca"`, `"FIXADA"`, contador `"/60"`, filtros e ordenaÃƒÂ§ÃƒÂ£o) em `app/src/main/res/values/strings.xml`
- [X] T003 [P] Adicionar ÃƒÂ­cones vetoriais e drawables auxiliares Nocturne Violet (`ic_drag_handle.xml`, `ic_pin.xml`, `ic_grid_view.xml`, `ic_list_view.xml`, `ic_playlist_add.xml`, `ic_keyboard_arrow_down.xml`, `bg_mini_player.xml`) em `app/src/main/res/drawable/`
- [X] T004 Estender `PlaybackSession` com `queueOriginLabel: String = "Biblioteca"` e `autoPlayAfterQueue: Boolean = true` em `app/src/main/java/com/genova/musicplayer/domain/player/PlaybackSession.kt`
- [X] T005 [P] Implementar `TrackPlayStatsRepository` usando `SharedPreferences` (`genova_playback_stats`) para persistir contagem local de reproduÃƒÂ§ÃƒÂµes por `trackId`, `auto_play_after_queue`, `playlist_view_grid` e `playlist_sort_order` em `app/src/main/java/com/genova/musicplayer/data/TrackPlayStatsRepository.kt`

**Checkpoint**: Base de recursos, sessÃƒÂ£o e repositÃƒÂ³rio local pronta; histÃƒÂ³rias de usuÃƒÂ¡rio podem avanÃƒÂ§ar em paralelo respeitando dependÃƒÂªncias explÃƒÂ­citas.

---

## Phase 2: User Story 1 - Atalho "Tocando Agora" e Mini-Player Persistente (Priority: P1) Ã°Å¸Å½Â¯ MVP

**Goal**: Incluir o chip/botÃƒÂ£o `"Tocando Agora"` na tela principal e exibir o mini-player flutuante persistente com progresso, play/pause e prÃƒÂ³xima faixa nas telas principais de navegaÃƒÂ§ÃƒÂ£o, mantendo execuÃƒÂ§ÃƒÂ£o direta ao clicar em qualquer mÃƒÂºsica.

**Independent Test**: Iniciar uma mÃƒÂºsica na aba `MÃƒÂºsicas`, voltar ÃƒÂ  lista, validar o mini-player visÃƒÂ­vel acima das abas com progresso/controles funcionando, navegar para `Playlists` e `Pastas` mantendo o mini-player visÃƒÂ­vel, e tocar no chip `"Tocando Agora"` ou no mini-player para abrir `NowPlayingActivity`.

- [X] T006 [P] [US1] Criar layout XML reutilizÃƒÂ¡vel do mini-player flutuante (`40dp` capa, tÃƒÂ­tulo, artista, barra de progresso `2dp`, botÃƒÂµes Play/Pause e PrÃƒÂ³xima com alvo `>= 48dp` e `contentDescription`, alinhado a `docs/design/biblioteca_com_mini_player_ativo.png`) em `app/src/main/res/layout/include_mini_player.xml`
- [X] T007 [US1] Implementar `MiniPlayerController` para vincular `include_mini_player.xml` ao `PlayerStateStore` via callbacks (ocultar quando `currentTrack == null`, atualizar capa/progresso/botÃƒÂµes, disparar `PlaybackService` e abrir `NowPlayingActivity` ao tocar no card) em `app/src/main/java/com/genova/musicplayer/ui/MiniPlayerController.kt`
- [X] T008 [US1] Adicionar chip/botÃƒÂ£o `"Tocando Agora"` (`>= 48dp` de alvo tÃƒÂ¡til acessÃƒÂ­vel e `contentDescription`) e `<include layout="@layout/include_mini_player" />` acima da barra inferior em `app/src/main/res/layout/activity_library.xml`
- [X] T009 [P] [US1] Incluir `<include layout="@layout/include_mini_player" />` acima da barra inferior em `app/src/main/res/layout/activity_playlists.xml`, `app/src/main/res/layout/activity_folders.xml` e no rodapÃƒÂ© de `app/src/main/res/layout/activity_playlist_detail.xml`
- [X] T010 [US1] Conectar clique do chip `"Tocando Agora"` (abrindo `NowPlayingActivity` ou exibindo aviso se nenhuma faixa ativa), `MiniPlayerController` e origem `"Biblioteca"` ao iniciar reproduÃƒÂ§ÃƒÂ£o em `app/src/main/java/com/genova/musicplayer/ui/LibraryActivity.kt` e `app/src/main/java/com/genova/musicplayer/ui/FoldersActivity.kt`

**Checkpoint**: UsuÃƒÂ¡rio navega entre `MÃƒÂºsicas`, `Playlists`, `Pastas` e `Reproduzindo agora` usando o chip `"Tocando Agora"` e o mini-player sem interromper o ÃƒÂ¡udio.

---

## Phase 3: User Story 2 - Subtela "Fila a Seguir" Completa e Interativa (Priority: P1) Ã°Å¸Å½Â¯ MVP

**Goal**: Habilitar o botÃƒÂ£o `"Fila a Seguir"` em `NowPlayingActivity` abrindo a subtela `QueueActivity` com visualizaÃƒÂ§ÃƒÂ£o da fila, clique para executar qualquer item, adiÃƒÂ§ÃƒÂ£o de mÃƒÂºsicas e playlists ÃƒÂ  fila, remoÃƒÂ§ÃƒÂ£o, reordenaÃƒÂ§ÃƒÂ£o, limpar fila, salvar fila como playlist e controle de auto-play.

**Independent Test**: Abrir `NowPlayingActivity`, tocar em `"Fila a Seguir"`, adicionar mÃƒÂºsicas individuais e uma playlist ÃƒÂ  fila, reordenar itens, remover um item, tocar sobre uma faixa da fila para tocÃƒÂ¡-la imediatamente, alternar `"Auto-play apÃƒÂ³s o fim da fila"`, salvar fila como playlist e limpar fila.

### Tests for User Story 2

- [X] T011 [P] [US2] Criar testes unitÃƒÂ¡rios em Kotlin puro cobrindo `replaceQueue`, `appendTracks`, `removeAt` (antes/no/apÃƒÂ³s `currentIndex`), `moveItem` (ajustando `currentIndex`), `selectIndex`, `clearUpcoming` e resoluÃƒÂ§ÃƒÂ£o de prÃƒÂ³xima faixa com `autoPlayAfterQueue` em `app/src/test/java/com/genova/musicplayer/domain/player/PlaybackQueueManagerTest.kt`

### Implementation for User Story 2

- [X] T012 [P] [US2] Implementar `PlaybackQueueManager` em Kotlin puro (operaÃƒÂ§ÃƒÂµes imutÃƒÂ¡veis de fila e ajuste consistente de `currentIndex`) em `app/src/main/java/com/genova/musicplayer/domain/player/PlaybackQueueManager.kt`
- [X] T013 [US2] Integrar `PlaybackQueueManager` no `PlayerStateStore` (`appendToQueue`, `removeFromQueue`, `moveQueueItem`, `playQueueIndex`, `clearUpcomingQueue`, `setAutoPlayAfterQueue`) e atualizar `resolveNextIndex` em `app/src/main/java/com/genova/musicplayer/domain/player/PlayerStateStore.kt`
- [X] T014 [US2] Atualizar `PlaybackService` para suportar continuaÃƒÂ§ÃƒÂ£o apÃƒÂ³s tÃƒÂ©rmino da fila quando `autoPlayAfterQueue == true`, registrar incremento de reproduÃƒÂ§ÃƒÂµes em `TrackPlayStatsRepository` ao preparar faixa com sucesso e sincronizar estado de fila/notificaÃƒÂ§ÃƒÂ£o em `app/src/main/java/com/genova/musicplayer/service/PlaybackService.kt`
- [X] T015 [P] [US2] Implementar `QueueViewModel` em Kotlin comum com callbacks coordenando `PlayerStateStore`, lista filtrada da biblioteca/playlists para adiÃƒÂ§ÃƒÂ£o na fila e aÃƒÂ§ÃƒÂ£o `"Salvar Playlist"` em `app/src/main/java/com/genova/musicplayer/viewmodel/QueueViewModel.kt`
- [X] T016 [P] [US2] Criar layouts XML `activity_queue.xml`, `item_queue_track.xml` e `dialog_add_to_queue.xml` fiÃƒÂ©is a `docs/design/fila_a_seguir.png` (alvos `>= 48dp`, sem perfil/favoritar, posiÃƒÂ§ÃƒÂ£o numÃƒÂ©rica, qualidade, alÃƒÂ§a `=` e menu `Ã¢â€¹Â®`) em `app/src/main/res/layout/activity_queue.xml`, `app/src/main/res/layout/item_queue_track.xml` e `app/src/main/res/layout/dialog_add_to_queue.xml`
- [X] T017 [US2] Implementar `QueueTrackAdapter` (virtualizado com `ViewHolder`, destaque lavanda na faixa ativa, menu `Ã¢â€¹Â®` com tocar/mover para cima/mover para baixo/remover e interaÃƒÂ§ÃƒÂ£o na alÃƒÂ§a de reordenaÃƒÂ§ÃƒÂ£o) em `app/src/main/java/com/genova/musicplayer/ui/QueueTrackAdapter.kt`
- [X] T018 [US2] Implementar `QueueActivity` (botÃƒÂ£o recolher, `"Limpar"`, cartÃƒÂ£o `"Tocando Agora"` com progresso e play/pause, `"+ Adicionar"` mÃƒÂºsicas/playlists, switch `"Auto-play apÃƒÂ³s o fim da fila"`, clique na lista para tocar mÃƒÂºsica selecionada, `"Salvar Playlist"`, controles rÃƒÂ¡pidos `AleatÃƒÂ³rio`/`Repetir`) e registrar em `app/src/main/java/com/genova/musicplayer/ui/QueueActivity.kt` e `app/src/main/AndroidManifest.xml`
- [X] T019 [US2] Ativar botÃƒÂ£o `"Fila a Seguir"` (substituindo placeholder `"Em breve"` por abertura de `QueueActivity`) em `app/src/main/res/layout/activity_now_playing.xml` e `app/src/main/java/com/genova/musicplayer/ui/NowPlayingActivity.kt`, e adicionar `"Adicionar ÃƒÂ  fila"` no menu `Ã¢â€¹Â®` da biblioteca em `app/src/main/java/com/genova/musicplayer/ui/LibraryActivity.kt`

### 3.1 Commit Group 1

- [X] T020 Solicitar autorizaÃƒÂ§ÃƒÂ£o explÃƒÂ­cita do usuÃƒÂ¡rio e executar `git commit` + `git push` das entregas P1 (Chip `"Tocando Agora"`, Mini-Player e Subtela `"Fila a Seguir"`)

**Checkpoint**: Todo o fluxo de reproduÃƒÂ§ÃƒÂ£o, mini-player e fila dinÃƒÂ¢mica estÃƒÂ¡ funcional e testado.

---

## Phase 4: User Story 3 - VisÃƒÂ£o Geral e CriaÃƒÂ§ÃƒÂ£o Aprimorada de Playlists (Priority: P2)

**Goal**: Evoluir a tela `PlaylistsActivity` e o modal `"Nova Playlist"` de acordo com `docs/design/colecao_de_playlists.png` e `docs/design/criar_nova_playlist.png` (busca, card destaque criar, contador 60 caracteres, fixar no topo, arte automÃƒÂ¡tica, ordenaÃƒÂ§ÃƒÂ£o, alternÃƒÂ¢ncia grade/lista e Play rÃƒÂ¡pido).

**Independent Test**: Abrir aba `Playlists`, criar playlist com 60 caracteres mÃƒÂ¡x. e toggle `"Fixar no topo da biblioteca"`, testar busca, alterar ordenaÃƒÂ§ÃƒÂ£o (`Mais recentes`, `AlfabÃƒÂ©tica`, `Mais faixas`), alternar entre Grade e Lista, e acionar Play direto no card.

### Tests for User Story 3

- [X] T021 [P] [US3] Expandir `PlaylistManagerTest` cobrindo limite mÃƒÂ¡ximo de 60 caracteres no nome, `isPinned` (fixadas sempre no topo), `coverStyle`, ordenaÃƒÂ§ÃƒÂ£o (`RECENT`, `ALPHABETICAL`, `MOST_TRACKS`) e filtro por texto em `app/src/test/java/com/genova/musicplayer/domain/playlist/PlaylistManagerTest.kt`

### Implementation for User Story 3

- [X] T022 [P] [US3] Atualizar `Playlist` com `isPinned: Boolean = false` e `coverStyle: Int = 0` e criar `PlaylistSortOrder` (`RECENT`, `ALPHABETICAL`, `MOST_TRACKS`) em `app/src/main/java/com/genova/musicplayer/domain/playlist/Playlist.kt` e `app/src/main/java/com/genova/musicplayer/domain/playlist/PlaylistSortOrder.kt`
- [X] T023 [US3] Evoluir `PlaylistManager` impondo limite de 60 caracteres apÃƒÂ³s `trim()`, suporte a `isPinned`/`coverStyle` na criaÃƒÂ§ÃƒÂ£o, `togglePin`, ordenaÃƒÂ§ÃƒÂ£o com fixadas primeiro e filtragem em `app/src/main/java/com/genova/musicplayer/domain/playlist/PlaylistManager.kt`
- [X] T024 [US3] Atualizar `PlaylistPreferencesRepository` para persistir e ler `isPinned` e `coverStyle` mantendo retrocompatibilidade com playlists salvas sem esses campos em `app/src/main/java/com/genova/musicplayer/data/PlaylistPreferencesRepository.kt`
- [X] T025 [P] [US3] Criar layouts XML `dialog_create_playlist.xml` (capa/arte automÃƒÂ¡tica, input com `maxLength="60"` + contador `N/60`, switch `"Fixar no topo da biblioteca"`, botÃƒÂµes `"Cancelar"` e `"+ Criar Playlist"`) e `item_playlist_grid.xml` (card grade com selo `"FIXADA"`, arte, nome, contagem/duraÃƒÂ§ÃƒÂ£o e botÃƒÂ£o Play `>= 48dp`) em `app/src/main/res/layout/dialog_create_playlist.xml` e `app/src/main/res/layout/item_playlist_grid.xml`
- [X] T026 [US3] Atualizar `activity_playlists.xml` e `item_playlist.xml` conforme `docs/design/colecao_de_playlists.png` (busca `"Filtrar faixas ou coleÃƒÂ§ÃƒÂµes..."`, card destaque `"Criar Nova Playlist"`, seletor de ordenaÃƒÂ§ÃƒÂ£o, toggle Grade/Lista, `GridView`/`ListView` de playlists e seÃƒÂ§ÃƒÂ£o de Playlists Inteligentes) em `app/src/main/res/layout/activity_playlists.xml` e `app/src/main/res/layout/item_playlist.xml`
- [X] T027 [US3] Atualizar `PlaylistsViewModel`, `PlaylistDialogs`, `PlaylistListAdapter` e `PlaylistsActivity` com busca, ordenaÃƒÂ§ÃƒÂ£o, alternÃƒÂ¢ncia Grade/Lista, modal `"Nova Playlist"`, selo `"FIXADA"`, Play rÃƒÂ¡pido da playlist, menu `Ã¢â€¹Â®` (`"Adicionar ÃƒÂ  fila"`, `"Fixar/Desafixar"`, `"Renomear"`, `"Excluir"`) e `MiniPlayerController` em `app/src/main/java/com/genova/musicplayer/viewmodel/PlaylistsViewModel.kt`, `app/src/main/java/com/genova/musicplayer/ui/PlaylistDialogs.kt`, `app/src/main/java/com/genova/musicplayer/ui/PlaylistListAdapter.kt` e `app/src/main/java/com/genova/musicplayer/ui/PlaylistsActivity.kt`

**Checkpoint**: Aba `Playlists` reflete o design de referÃƒÂªncia com criaÃƒÂ§ÃƒÂ£o avanÃƒÂ§ada, fixaÃƒÂ§ÃƒÂ£o, ordenaÃƒÂ§ÃƒÂ£o, grade/lista e reproduÃƒÂ§ÃƒÂ£o rÃƒÂ¡pida.

---

## Phase 5: User Story 4 - Detalhes da Playlist Aprimorado e AdiÃƒÂ§ÃƒÂ£o em MÃƒÂºltiplas Playlists (Priority: P2)

**Goal**: Evoluir `PlaylistDetailActivity` conforme `docs/design/detalhes_da_playlist.png` (cabeÃƒÂ§alho rico, Play/AleatÃƒÂ³rio, `"+ Adicionar mÃƒÂºsicas"`, `"Ordem manual"` com persistÃƒÂªncia, `"Adicionar ÃƒÂ  fila"`, filtro interno) e implementar o modal multi-seleÃƒÂ§ÃƒÂ£o `"Adicionar ÃƒÂ  Playlist"` conforme `docs/design/adicionar_playlist.png`.

**Independent Test**: Abrir menu `Ã¢â€¹Â®` de uma faixa na biblioteca, selecionar 2 playlists no modal `"Adicionar ÃƒÂ  Playlist"` e confirmar em `"Concluir"`; abrir uma playlist, filtrar faixas, ativar `"Ordem manual"`, reordenar faixas, reabrir a playlist confirmando ordem salva e acionar `"Adicionar ÃƒÂ  fila"`.

### Tests for User Story 4

- [X] T028 [P] [US4] Adicionar testes unitÃƒÂ¡rios em `PlaylistManagerTest` para `reorderTrack` (mover faixa na playlist), `addTracksToPlaylist` (adiÃƒÂ§ÃƒÂ£o em lote sem duplicidade) e `setTrackInPlaylists` (seleÃƒÂ§ÃƒÂ£o mÃƒÂºltipla de playlists) em `app/src/test/java/com/genova/musicplayer/domain/playlist/PlaylistManagerTest.kt`

### Implementation for User Story 4

- [X] T029 [US4] Implementar `reorderTrack`, `addTracksToPlaylist` e `updateTrackMemberships` em `app/src/main/java/com/genova/musicplayer/domain/playlist/PlaylistManager.kt` e expor os novos comandos em `app/src/main/java/com/genova/musicplayer/viewmodel/PlaylistsViewModel.kt`
- [X] T030 [P] [US4] Criar layout XML `dialog_add_to_playlists.xml` fiel a `docs/design/adicionar_playlist.png` (card da faixa com badge Hi-Fi, atalho `"+ Criar Nova Playlist"`, busca `"Buscar playlist existente..."`, lista com checkboxes para multi-seleÃƒÂ§ÃƒÂ£o e rodapÃƒÂ© com contador e botÃƒÂ£o `"Concluir"`) em `app/src/main/res/layout/dialog_add_to_playlists.xml`
- [X] T031 [US4] Implementar modal multi-seleÃƒÂ§ÃƒÂ£o `"Adicionar ÃƒÂ  Playlist"` em `app/src/main/java/com/genova/musicplayer/ui/PlaylistDialogs.kt` e conectÃƒÂ¡-lo ao menu `Ã¢â€¹Â®` de `app/src/main/java/com/genova/musicplayer/ui/LibraryActivity.kt`
- [X] T032 [US4] Atualizar `activity_playlist_detail.xml` e `PlaylistDetailActivity` conforme `docs/design/detalhes_da_playlist.png` (capa/arte, badge `"PLAYLIST PESSOAL"`, resumo de faixas/duraÃƒÂ§ÃƒÂ£o/formatos, botÃƒÂµes `Play` e `AleatÃƒÂ³rio`, chips `"+ Adicionar mÃƒÂºsicas"`, `"Ordem manual"` e `"Adicionar ÃƒÂ  fila"`, campo `"Filtrar faixas na playlist..."`, reordenaÃƒÂ§ÃƒÂ£o persistida, compartilhamento de tÃƒÂ­tulos e `MiniPlayerController`) em `app/src/main/res/layout/activity_playlist_detail.xml` e `app/src/main/java/com/genova/musicplayer/ui/PlaylistDetailActivity.kt`

**Checkpoint**: Multi-seleÃƒÂ§ÃƒÂ£o de playlists e curadoria completa nos detalhes da playlist (incluindo ordem manual e envio para fila) funcionando de ponta a ponta.

---

## Phase 6: User Story 5 - Playlists Inteligentes Locais do Sistema (Priority: P3)

**Goal**: Exibir na aba `Playlists` a seÃƒÂ§ÃƒÂ£o `"Playlists Inteligentes do Sistema"` com `"Mais Tocadas (Top 50)"` e `"Adicionadas Recentemente"` 100% offline.

**Independent Test**: Reproduzir faixas na biblioteca, abrir aba `Playlists`, verificar `"Mais Tocadas (Top 50)"` e `"Adicionadas Recentemente"` com contagens corretas e iniciar reproduÃƒÂ§ÃƒÂ£o ou abrir visualizaÃƒÂ§ÃƒÂ£o das faixas ao tocar nelas.

### Tests for User Story 5

- [X] T033 [P] [US5] Criar testes unitÃƒÂ¡rios em Kotlin puro cobrindo ordenaÃƒÂ§ÃƒÂ£o por contagem local de reproduÃƒÂ§ÃƒÂµes, limite Top 50 e ordenaÃƒÂ§ÃƒÂ£o por data recente em `app/src/test/java/com/genova/musicplayer/domain/playlist/SmartPlaylistEngineTest.kt`

### Implementation for User Story 5

- [X] T034 [P] [US5] Implementar `SmartPlaylistEngine` em Kotlin puro (`buildMostPlayed`, `buildRecentlyAdded`) em `app/src/main/java/com/genova/musicplayer/domain/playlist/SmartPlaylistEngine.kt`
- [X] T035 [US5] Integrar `"Mais Tocadas (Top 50)"` e `"Adicionadas Recentemente"` em `PlaylistsActivity` e suportar abertura de playlist inteligente em modo somente leitura (reproduzir, aleatÃƒÂ³rio, adicionar ÃƒÂ  fila, sem renomear/excluir) em `app/src/main/java/com/genova/musicplayer/ui/PlaylistsActivity.kt` e `app/src/main/java/com/genova/musicplayer/ui/PlaylistDetailActivity.kt`

### 6.1 Commit Group 2

- [X] T036 Solicitar autorizaÃƒÂ§ÃƒÂ£o explÃƒÂ­cita do usuÃƒÂ¡rio e executar `git commit` + `git push` das entregas de Playlists EvoluÃƒÂ­das e Playlists Inteligentes (US3 + US4 + US5)

**Checkpoint**: Todas as histÃƒÂ³rias P1, P2 e P3 implementadas e alinhadas aos designs de referÃƒÂªncia em `docs/design/`.

---

## Phase 7: Testes, E2E Appium & Pre-PR Checklist

**Purpose**: Validar unidade, performance com 2.000+ faixas, E2E Appium em container via subagente dedicado, documentaÃƒÂ§ÃƒÂ£o, grafo e abertura de PR.

- [X] T037 Executar suÃƒÂ­te completa de testes unitÃƒÂ¡rios e build debug via Docker (`./gradlew testDebugUnitTest assembleDebug --no-daemon`) e confirmar zero erros
- [X] T038 Atualizar e executar testes E2E Appium cobrindo chip `"Tocando Agora"`, mini-player, subtela `"Fila a Seguir"` e playlists aprimoradas utilizando **exclusivamente** o subagente `appium-test-hydrogen` com `docker-compose.e2e.yml` em `e2e/`
- [X] T039 [P] Atualizar `README.md` documentando as funcionalidades da Parte 3 e linkando as novas imagens de referÃƒÂªncia em `docs/design/` (`biblioteca_com_mini_player_ativo.png`, `fila_a_seguir.png`, `colecao_de_playlists.png`, `detalhes_da_playlist.png`, `criar_nova_playlist.png`, `adicionar_playlist.png`) em `README.md`
- [X] T040 Executar `graphify update .` na raiz do projeto para sincronizar `graphify-out/`
- [X] T041 Solicitar autorizaÃƒÂ§ÃƒÂ£o explÃƒÂ­cita do usuÃƒÂ¡rio para `git commit` final + `git push` e criaÃƒÂ§ÃƒÂ£o do PR para a branch `develop` via MCP do GitHub

---

## Dependencies & Execution Order

- **Branch (`T001`)** Ã¢â€ â€™ **Phase 1 (`T002Ã¢â‚¬â€œT005`)** Ã¢â€ â€™ bloqueia todas as User Stories.
- **US1 (`T006Ã¢â‚¬â€œT010`)** e **US2 (`T011Ã¢â‚¬â€œT019`)** podem avanÃƒÂ§ar apÃƒÂ³s Phase 1; `T019` integra `QueueActivity` em `NowPlayingActivity` e `"Adicionar ÃƒÂ  fila"` em `LibraryActivity`.
- **US3 (`T021Ã¢â‚¬â€œT027`)** depende de Phase 1 e reutiliza `MiniPlayerController` (`T007`).
- **US4 (`T028Ã¢â‚¬â€œT032`)** depende da evoluÃƒÂ§ÃƒÂ£o de `Playlist`/`PlaylistManager` em `T022Ã¢â‚¬â€œT024` e integra `"Adicionar ÃƒÂ  fila"` com `PlayerStateStore` (`T013`).
- **US5 (`T033Ã¢â‚¬â€œT035`)** depende de `TrackPlayStatsRepository` (`T005`) e do hook de incremento em `PlaybackService` (`T014`).
- **Phase 7 (`T037Ã¢â‚¬â€œT041`)** depende da conclusÃƒÂ£o de todas as histÃƒÂ³rias.

## Parallel Example

```text
# Phase 1 em paralelo:
T002 (strings.xml), T003 (drawables), T004 (PlaybackSession.kt), T005 (TrackPlayStatsRepository.kt)

# Phase 3 (US2) testes e modelos em paralelo:
T011 (PlaybackQueueManagerTest.kt), T012 (PlaybackQueueManager.kt), T015 (QueueViewModel.kt), T016 (layouts da fila)
```
