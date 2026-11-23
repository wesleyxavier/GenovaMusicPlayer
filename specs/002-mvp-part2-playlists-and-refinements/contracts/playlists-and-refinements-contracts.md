# UI & Architecture Contracts: MVP Parte 2 — Playlists e Refinamentos

**Feature**: `002-mvp-part2-playlists-and-refinements` | **Date**: 2026-11-22

## 1. Refinamentos nas Telas Existentes

- **Execução direta ao clicar (`LibraryActivity` & `PlaylistDetailActivity`):**
  - Tocar em qualquer linha da lista de músicas dispara imediatamente `PlaybackService` (`ACTION_PLAY`) com a faixa selecionada e a lista exibida no momento.
- **Tela `Tocando Agora` (`activity_now_playing.xml` / `NowPlayingActivity.kt`):**
  - **Removido:** cartão `"Dispositivo de Áudio"` e botão `"Trocar"`. O áudio utiliza automaticamente a rota ativa do sistema.
- **Tela `Pastas` (`activity_folders.xml` / `FoldersActivity.kt`):**
  - **Removido:** card informativo permanente `"Regra de Hierarquia"`.
  - **Mantido:** aviso em português exibido somente quando o usuário tentar adicionar pasta já coberta por uma pasta pai.

## 2. Novas Telas e Contratos de Playlists

- **Barra de Navegação Inferior (3 abas):**
  - `"Músicas"` · `"Playlists"` · `"Pastas"` (cada item com área de toque `>= 48dp` e `contentDescription` em PT-BR).
- **Tela `PlaylistsActivity` (`activity_playlists.xml`):**
  - Barra superior com título `"Playlists"`, botão `"+ Criar playlist"` (`>= 48dp`), lista virtualizada (`ListView` + `ViewHolder`) exibindo nome da playlist, contagem (`"{N} músicas"`) e menu `⋮` (`"Renomear"`, `"Excluir"` com confirmação).
- **Tela `PlaylistDetailActivity` (`activity_playlist_detail.xml`):**
  - Barra superior com voltar (`←`), nome da playlist, botão `"Aleatório"` e lista de faixas da playlist.
  - Clicar em uma faixa reproduz imediatamente usando a playlist como lista da sessão; menu `⋮` da faixa permite `"Remover da playlist"` e `"Ver detalhes do arquivo"`.
- **Menu `⋮` da Biblioteca (`LibraryActivity`):**
  - Passa a incluir `"Adicionar à playlist"` (além de `"Ver detalhes do arquivo"`), abrindo diálogo para selecionar playlist existente ou criar nova na hora.

## 3. Contratos Internos

- **`PlaylistsViewModel` (Kotlin comum com callbacks):**
  - `onStateChanged: ((PlaylistsUiState) -> Unit)?`
  - Métodos: `loadPlaylists()`, `createPlaylist(name)`, `renamePlaylist(id, newName)`, `deletePlaylist(id)`, `addTrackToPlaylist(playlistId, trackId)`, `removeTrackFromPlaylist(playlistId, trackId)`.
- **`PlaylistPreferencesRepository`:**
  - `loadAll(): List<Playlist>`
  - `saveAll(playlists: List<Playlist>)`
