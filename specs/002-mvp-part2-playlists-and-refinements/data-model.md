# Data Model: MVP Parte 2 — Playlists e Refinamentos

**Feature**: `002-mvp-part2-playlists-and-refinements` | **Date**: 2026-11-22

## 1. Entidades de Domínio (Kotlin Puro — sem `android.*`)

### `Playlist`
- `id: String` — Identificador único local (ex.: timestamp + contador ou UUID gerado em Kotlin/Java).
- `name: String` — Nome exibido da playlist (já normalizado com `trim()`).
- `createdAtEpochMs: Long` — Data de criação.
- `trackIds: List<Long>` — Lista ordenada dos IDs de `Track` incluídos na playlist.

### `PlaylistValidationResult` (Sealed Class)
- `Success(val playlists: List<Playlist>, val affectedPlaylist: Playlist)` — Operação concluída.
- `EmptyName` — Nome vazio ou composto apenas por espaços.
- `DuplicateName(val normalizedName: String)` — Já existe outra playlist com o mesmo nome (comparação `trim().lowercase()`).
- `PlaylistNotFound(val playlistId: String)` — Identificador não encontrado.

## 2. Regras de Negócio (`PlaylistManager`)

1. **Criar (`createPlaylist(existing, rawName, nowMs)`):**
   - Normalizar `name = rawName.trim()`. Se `name.isEmpty()` → `EmptyName`.
   - Se existir playlist com `it.name.lowercase() == name.lowercase()` → `DuplicateName(name)`.
   - Criar `Playlist` com `trackIds = emptyList()` e retornar `Success`.
2. **Renomear (`renamePlaylist(existing, playlistId, rawNewName)`):**
   - Mesmas validações de nome vazio e duplicidade (ignorando a própria playlist `playlistId`).
3. **Excluir (`deletePlaylist(existing, playlistId)`):**
   - Remove a playlist correspondente e retorna lista atualizada.
4. **Adicionar faixa (`addTrackToPlaylist(existing, playlistId, trackId)`):**
   - Se `trackId` já estiver em `trackIds`, mantém sem duplicar; caso contrário, anexa ao final.
5. **Remover faixa (`removeTrackFromPlaylist(existing, playlistId, trackId)`):**
   - Remove `trackId` de `trackIds` preservando a ordem das demais faixas.

## 3. Persistência Local

- `SharedPreferences` (`"genova_playlists_prefs"`):
  - `"playlists_json"`: `JSONArray` onde cada item é um `JSONObject` com `id`, `name`, `createdAtEpochMs` e `trackIds` (`JSONArray` de `Long`).
