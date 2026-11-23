# Data Model: MVP Parte 3 — Fila a Seguir, Mini-Player e Playlists Evoluídas

**Feature**: `004-part3-queue-miniplayer-playlists` | **Date**: 2026-11-23

## 1. Entities (`domain/` — Kotlin puro)

### `PlaybackSession` (atualização em `domain/player/PlaybackSession.kt`)
- `currentTrack: Track?` — faixa ativa atual
- `activeList: List<Track>` — lista ordenada da fila de reprodução atual
- `currentIndex: Int` — índice da faixa ativa em `activeList` (`-1` quando vazia)
- `queueOriginLabel: String` — origem da reprodução (ex.: `"Biblioteca"`, `"Favoritas do Black"`, `"Fila personalizada"`)
- `status: PlayerStatus` — `IDLE`, `PREPARING`, `PLAYING`, `PAUSED`, `COMPLETED`, `ERROR`
- `positionMs: Long` — posição atual em ms
- `durationMs: Long` — duração total da faixa atual em ms
- `isShuffleEnabled: Boolean` — modo aleatório ativo
- `repeatMode: RepeatMode` — `OFF`, `REPEAT_ALL`, `REPEAT_ONE`
- `autoPlayAfterQueue: Boolean` — continua tocando faixas da biblioteca ao terminar a fila (quando `repeatMode == OFF`)
- `audioOutputName: String` — nome informativo da rota ativa
- `errorMessage: String?` — erro em PT-BR quando houver

### `PlaybackQueueManager` (`domain/player/PlaybackQueueManager.kt`)
- Responsável por operações puras e imutáveis sobre `(activeList, currentIndex)`:
  - `replaceQueue(tracks, startIndex, originLabel)`
  - `enqueueAsLastAndPlay(session, track, fallbackOrigin)` — se a fila estiver vazia, cria fila de 1 faixa; se já ativa, anexa ao fim da fila e seleciona o novo último índice para reprodução imediata
  - `appendTracks(session, tracksToAdd, fallbackOrigin)` — adiciona ao fim da fila; se fila vazia, inicia no índice `0`
  - `removeAt(session, index)` — remove item ajustando `currentIndex`:
    - Se `index < currentIndex`: decrementa `currentIndex` em 1
    - Se `index == currentIndex`: avança para o item que assumiu a posição (ou anterior/vazio se último)
    - Se `index > currentIndex`: mantém `currentIndex`
  - `moveItem(session, fromIndex, toIndex)` — reordena item ajustando `currentIndex` para acompanhar a faixa em execução
  - `selectIndex(session, index)` — muda faixa ativa para `activeList[index]`
  - `clearUpcoming(session)` — remove itens após `currentIndex` (ou limpa fila se ociosa)

### `Playlist` (atualização em `domain/playlist/Playlist.kt`)
- `id: String` — identificador único
- `name: String` — nome normalizado (1 a 60 caracteres após `trim()`)
- `createdAtEpochMs: Long` — timestamp de criação
- `trackIds: List<Long>` — lista ordenada de IDs de faixas
- `isPinned: Boolean = false` — fixada no topo da biblioteca de playlists
- `coverStyle: Int = 0` — índice de estilo visual/gradiente local (0..5) para arte automática quando sem capa customizada

### `PlaylistSortOrder` (`domain/playlist/PlaylistSortOrder.kt`)
- `RECENT` — mais recentes primeiro (`createdAtEpochMs` decrescente)
- `ALPHABETICAL` — A–Z (`name` case-insensitive)
- `MOST_TRACKS` — maior quantidade de faixas primeiro
- Regra transversal: playlists com `isPinned == true` sempre precedem as não fixadas dentro de qualquer ordenação.

### `SmartPlaylistEngine` (`domain/playlist/SmartPlaylistEngine.kt`)
- `buildMostPlayed(tracks: List<Track>, playCounts: Map<Long, Int>, limit: Int = 50): List<Track>`
- `buildRecentlyAdded(tracks: List<Track>, limit: Int = 50): List<Track>`

## 2. Persistence (`data/`)

- **`PlaylistPreferencesRepository`**:
  - Continua usando `SharedPreferences` (`genova_playlists`) com array JSON.
  - Novos campos opcionais por item JSON: `"isPinned"` (default `false`) e `"coverStyle"` (default `0`).
- **`TrackPlayStatsRepository`**:
  - Novo repositório em `SharedPreferences` (`genova_playback_stats`) guardando:
    - Contagem por faixa (`JSONObject` `trackId -> count`)
    - Preferência booleana `auto_play_after_queue` (default `true`)
    - Preferência de modo de visualização de playlists `playlist_view_grid` (default `true`) e ordenação `playlist_sort_order`
