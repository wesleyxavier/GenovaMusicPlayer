# Implementation Plan: MVP Parte 3 — Atalho "Tocando Agora", Mini-Player, Fila a Seguir e Evolução de Playlists

**Branch**: `004-part3-queue-miniplayer-playlists` | **Date**: 2026-11-23 | **Spec**: [spec.md](file:///c:/Users/User/Documents/TI/Lavi/Web/Genova/src/PROJETOS/GenovaMusicPlayer/specs/004-part3-queue-miniplayer-playlists/spec.md)

**Input**: Feature specification from `specs/004-part3-queue-miniplayer-playlists/spec.md` + Constituição `v1.2.0` + designs de referência em `docs/design/`.

## Summary

Implementar a Parte 3 do MVP conforme os designs de referência **Nocturne Violet** em `docs/design/`:
1. **Tela Principal (`LibraryActivity`)**: adicionar o chip/botão `"Tocando Agora"` para redirecionar à tela `"Reproduzindo agora"` (`NowPlayingActivity`) mantendo a execução direta ao tocar em qualquer faixa, e adicionar `"Adicionar à fila"` no menu `⋮` das faixas.
2. **Mini-Player Persistente (`MiniPlayerController` + `include_mini_player.xml`)**: barra flutuante reutilizável exibida acima das abas inferiores em `LibraryActivity`, `PlaylistsActivity` e `FoldersActivity` (e opcionalmente no rodapé de `PlaylistDetailActivity`) sempre que houver faixa ativa, com miniatura, título/artista, progresso linear, play/pause, próxima faixa e clique para abrir `NowPlayingActivity`.
3. **Subtela `"Fila a Seguir"` (`QueueActivity` + `PlaybackQueueManager`)**: ativar o botão `"Fila a Seguir"` em `NowPlayingActivity` para abrir a tela de fila com cartão `"Tocando Agora"`, lista numerada da fila, clique no item para tocar imediatamente, remoção, reordenação (botões subir/descer e alça de arraste/menu), adição de músicas ou playlists à fila, toggle `"Auto-play após o fim da fila"`, `"Limpar"` fila e `"Salvar Playlist"`.
4. **Evolução de Playlists (`PlaylistsActivity`, `PlaylistDetailActivity`, modais de criação e adição múltipla, `PlaylistManager`, `TrackPlayStatsRepository`)**: busca e ordenação na coleção, alternância grade/lista, fixação (`isPinned`), arte automática/cor derivada, limite de 60 caracteres com contador, seleção múltipla no modal `"Adicionar à Playlist"`, filtro interno e `"Ordem manual"` com reordenação persistida nos detalhes da playlist, `"Adicionar músicas"`, `"Adicionar à fila"` e seção `"Playlists Inteligentes do Sistema"` (`Mais Tocadas (Top 50)` e `Adicionadas Recentemente`).

Tudo em Kotlin puro e Views nativas (`android.*`), zero bibliotecas externas e builds/testes 100% em Docker.

## Technical Context

**Language/Version**: Kotlin (stdLib apenas), Java/JVM target 17, Android Gradle Plugin `8.4.2`
**Primary Dependencies**: Nenhuma biblioteca de terceiros (apenas Android SDK framework `android.*` + Kotlin stdlib)
**Storage**: `SharedPreferences` + `org.json.JSONArray`/`JSONObject` para playlists (estendendo `Playlist` com `isPinned`, `coverStyle` e limite 60 chars) e contagem local de reproduções (`TrackPlayStatsRepository`); estado da fila em memória no singleton `PlayerStateStore` (com persistência opcional de preferência `autoPlayAfterQueue` em `SharedPreferences`)
**Testing**: Testes unitários JVM (`kotlin("test")`) para `PlaybackQueueManager`, `PlaylistManager` (regras expandidas) e `SmartPlaylistEngine`, executados via Docker; validação E2E Appium em container via subagente `appium-test-hydrogen`
**Target Platform**: Android API 26+ até API 34 (`compileSdk 34`, `android.useAndroidX=false`)
**Project Type**: mobile-app (Android nativo)
**Performance Goals**: Abertura da lista com 2.000+ músicas fluida (`ListView`/`GridView` virtualizados + `ViewHolder` + `AlbumArtCacheLoader`), atualização do progresso do mini-player sem travar a UI thread
**Constraints**: Offline-only, zero dependências externas (sem `RecyclerView`, sem `AndroidX`, sem `Material`), UI e erros em PT-BR, alvos `>= 48dp`, TalkBack `contentDescription`, builds/testes exclusivamente em Docker
**Scale/Scope**: 1 nova `Activity` (`QueueActivity`), 1 componente reutilizável de mini-player (`MiniPlayerController` + XML), novos diálogos/bottom sheets nativos (`AlertDialog` / `Dialog` customizados com tema Nocturne Violet para `"Nova Playlist"`, `"Adicionar à Playlist"` multi-seleção e `"Adicionar à Fila"`), atualização de `LibraryActivity`, `NowPlayingActivity`, `PlaylistsActivity`, `PlaylistDetailActivity`, `FoldersActivity`, `PlayerStateStore`, `PlaybackService` e camada `domain/`

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Princípio I (Zero Dependências Externas):** Apenas SDK Android (`Dialog`, `ListView`, `GridView`, `ProgressBar`, `Switch`, `CheckBox`, `PopupMenu`) e Kotlin stdlib (`org.json`). Nenhuma biblioteca externa adicionada.
- [x] **Princípio II (MVVM Puro):** Toda regra de fila (`PlaybackQueueManager`), playlists (`PlaylistManager`) e playlists inteligentes (`SmartPlaylistEngine`) em classes Kotlin puras sem `android.*`. `PlayerStateStore` continua como singleton com callbacks.
- [x] **Princípio III (Escopo do MVP — Parte 3, v1.2.0):** Cobre exatamente chip `"Tocando Agora"`, mini-player, subtela `"Fila a Seguir"` e melhorias de playlists, mantendo fora do escopo perfil de usuário, favoritos e downloads online.
- [x] **Princípio IV (Pastas e Hierarquia):** Preservado integralmente; nenhuma varredura fora das pastas autorizadas.
- [x] **Princípio V (Reprodução e Áudio):** `PlaybackService` (`MediaPlayer` + `MediaSession` + `Notification.MediaStyle` + `AudioFocusRequest`) atualizado para respeitar inserções, remoções, saltos diretos na fila e `autoPlayAfterQueue` sem interromper a reprodução ativa.
- [x] **Princípio VI & VII (UI Nativa, Nocturne Violet e Simplicidade):** Layouts XML nativos alinhados aos PNGs em `docs/design/`, alvos `>= 48dp`, textos PT-BR, `contentDescription` para TalkBack.
- [x] **Docker Isolation & E2E:** Builds, testes unitários em Docker (`Dockerfile`) e E2E Appium via subagente `appium-test-hydrogen` (`docker-compose.e2e.yml`).

*Reavaliação pós-Phase 1:* Total aderência à Constituição `v1.2.0` sem exceções ou violações.

## Project Structure

### Documentation (this feature)

```text
specs/004-part3-queue-miniplayer-playlists/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── checklists/
│   └── requirements.md
├── contracts/
│   └── part3-ui-and-domain-contracts.md
└── tasks.md             # (/speckit-tasks)
```

### Source Code (repository root)

```text
app/src/
├── main/
│   ├── AndroidManifest.xml                      # Registrar QueueActivity
│   ├── res/
│   │   ├── layout/
│   │   │   ├── include_mini_player.xml          # Barra flutuante reutilizável do mini-player
│   │   │   ├── activity_queue.xml               # Subtela Fila a Seguir
│   │   │   ├── item_queue_track.xml             # Item numerado da fila com menu e alça de ordem
│   │   │   ├── dialog_create_playlist.xml       # Modal Nova Playlist (contador 60, capa/cor, pin)
│   │   │   ├── dialog_add_to_playlists.xml      # Modal Adicionar à Playlist (multi-seleção)
│   │   │   ├── dialog_add_to_queue.xml          # Modal Adicionar músicas/playlists à fila
│   │   │   ├── item_playlist_grid.xml           # Card de playlist para modo Grade
│   │   │   ├── activity_library.xml             # Chip "Tocando Agora" + include mini-player
│   │   │   ├── activity_now_playing.xml         # Ativar botão "Fila a Seguir" (remover "Em breve")
│   │   │   ├── activity_playlists.xml           # Busca, destaque criar, ordenação, grade/lista, inteligentes, mini-player
│   │   │   ├── activity_playlist_detail.xml     # Capa, resumo, Play/Aleatório, chips, filtro, ordem manual, mini-player
│   │   │   └── activity_folders.xml             # Include mini-player acima das abas
│   │   ├── drawable/                            # Ícones auxiliares (pin, grid, list, drag, play_circle, etc.) e fundos Nocturne Violet
│   │   └── values/strings.xml                   # Strings PT-BR e contentDescriptions da Parte 3
│   └── java/com/genova/musicplayer/
│       ├── domain/
│       │   ├── player/
│       │   │   ├── PlaybackSession.kt           # Origem da fila + autoPlayAfterQueue
│       │   │   ├── PlaybackQueueManager.kt      # Lógica pura da fila (adicionar, remover, reordenar, tocar índice, limpar, próximo)
│       │   │   └── PlayerStateStore.kt          # Integrar PlaybackQueueManager e callbacks
│       │   └── playlist/
│       │       ├── Playlist.kt                  # isPinned, coverStyle, max 60 chars
│       │       ├── PlaylistSortOrder.kt         # RECENT, ALPHABETICAL, MOST_TRACKS
│       │       ├── PlaylistManager.kt           # Limite 60 chars, togglePin, reorderTrack, addTracksBatch, sort/filter
│       │       └── SmartPlaylistEngine.kt       # Regras puras para Adicionadas Recentemente e Mais Tocadas (Top 50)
│       ├── data/
│       │   ├── PlaylistPreferencesRepository.kt # Serializar isPinned e coverStyle mantendo retrocompatibilidade
│       │   └── TrackPlayStatsRepository.kt      # Persistência local de contagem de reproduções e autoPlayAfterQueue
│       ├── viewmodel/
│       │   ├── QueueViewModel.kt                # Estado da tela Fila a Seguir
│       │   └── PlaylistsViewModel.kt            # Busca, ordenação, grade/lista, pin, reordenação, batch, inteligentes
│       ├── service/
│       │   └── PlaybackService.kt               # Suporte a queue actions, autoPlayAfterQueue e incremento de play stats
│       └── ui/
│           ├── MiniPlayerController.kt          # Vincula include_mini_player.xml ao PlayerStateStore nas Activities principais
│           ├── QueueActivity.kt                 # Subtela Fila a Seguir
│           ├── QueueTrackAdapter.kt             # Adapter virtualizado da fila (posição, remover, reordenar)
│           ├── LibraryActivity.kt               # Chip "Tocando Agora", menu "Adicionar à fila", modal multi-playlist, mini-player
│           ├── NowPlayingActivity.kt            # Botão "Fila a Seguir" abrindo QueueActivity
│           ├── PlaylistsActivity.kt             # UI evoluída (grade/lista, busca, ordenação, fixadas, inteligentes, criar)
│           ├── PlaylistListAdapter.kt           # Suporte a modo lista/grade, badge FIXADA, duração e Play rápido
│           ├── PlaylistDetailActivity.kt        # Capa, resumo, filtro, ordem manual, adicionar músicas, adicionar à fila
│           ├── PlaylistDialogs.kt               # Helpers reutilizáveis dos modais Nova Playlist e Adicionar à Playlist
│           └── FoldersActivity.kt               # Mini-player persistente acima das abas
└── test/java/com/genova/musicplayer/
    ├── domain/player/
    │   └── PlaybackQueueManagerTest.kt          # Testes unitários puros da fila
    └── domain/playlist/
        ├── PlaylistManagerTest.kt               # Expandir testes para pin, 60 chars, reordenação, batch, sort/filter
        └── SmartPlaylistEngineTest.kt           # Testes unitários puros de Mais Tocadas e Adicionadas Recentemente
```

**Structure Decision**: Extensão direta da arquitetura MVVM pura existente (`domain/` sem `android.*`, `data/`, `viewmodel/`, `ui/`, `service/`), reutilizando `ListView`/`GridView` + `BaseAdapter` e `Dialog` nativo do Android SDK para manter zero dependências externas.

## Complexity Tracking

Nenhuma violação à Constituição identificada.
