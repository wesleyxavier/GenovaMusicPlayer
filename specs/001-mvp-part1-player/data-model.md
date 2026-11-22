# Data Model: MVP Parte 1 — GenovaMusicPlayer

**Feature**: `001-mvp-part1-player` | **Date**: 2026-11-21

## 1. Entidades de Domínio (Kotlin Puro — sem `android.*`)

### `AuthorizedFolder`
- `normalizedPath: String` — Caminho normalizado por segmentos (ex.: `/storage/emulated/0/Music`). Chave única.
- `treeUriString: String` — URI persistida retornada pelo seletor do sistema.
- `displayName: String` — Nome amigável da pasta (último segmento).
- `addedAtEpochMs: Long` — Timestamp de inclusão para exibição ("Adicionado em ...").
- `trackCount: Int` — Quantidade de faixas indexadas nessa pasta.
- `totalSizeBytes: Long` — Soma do tamanho dos arquivos de áudio indexados nessa pasta.

### `FolderAdditionResult` (Sealed Class)
- `Added(val folders: List<AuthorizedFolder>, val removedChildren: List<AuthorizedFolder>)` — Inclusão aceita; lista opcional de subpastas filhas removidas automaticamente.
- `RejectedDuplicate(val existingPath: String)` — Caminho igual já incluído.
- `RejectedCoveredByParent(val parentPath: String)` — Tentativa de incluir pasta descendente de uma já existente.
- `RejectedInvalidPath` — Caminho vazio ou inválido após normalização.

### `Track`
- `id: Long` — Identificador único no `MediaStore`.
- `contentUri: String` — URI de conteúdo para reprodução e leitura de capa.
- `title: String` — Título da faixa.
- `artist: String` — Artista(s).
- `album: String` — Nome do álbum.
- `durationMs: Long` — Duração total em milissegundos.
- `dateAddedEpochSec: Long` — Data de inclusão no dispositivo (usada no filtro `"Recentes"`).
- `filePath: String` — Caminho normalizado do arquivo no armazenamento.
- `sizeBytes: Long` — Tamanho do arquivo em bytes.
- `mimeType: String` — Tipo MIME / formato (ex.: `audio/flac`, `audio/mpeg`).
- `bitrateKbps: Int?` — Taxa de bits quando disponível.
- `sampleRateHz: Int?` — Taxa de amostragem quando disponível.
- `isHiFi: Boolean` — Derivado de formato sem perdas (`FLAC`, `WAV`, `ALAC`) ou `>= 320 kbps` para badge `"Hi-Fi Sem Perdas"`.

### `LibraryFilter` (Enum)
- `ALL` (`"Todas"`)
- `RECENT` (`"Recentes"` — ordenado por `dateAddedEpochSec` decrescente)

### `RepeatMode` (Enum)
- `OFF` — Não repetir
- `REPEAT_ALL` — Repetir todas as faixas da sessão
- `REPEAT_ONE` — Repetir faixa atual

### `PlayerStatus` (Enum)
- `IDLE`
- `PREPARING`
- `PLAYING`
- `PAUSED`
- `COMPLETED`
- `ERROR`

### `PlaybackSession`
- `currentTrack: Track?` — Faixa ativa no momento.
- `activeList: List<Track>` — Lista base da sessão atual (visível/filtrada ou acervo completo no shuffle).
- `currentIndex: Int` — Índice da faixa atual em `activeList` (`-1` se vazio).
- `status: PlayerStatus` — Estado explícito do reprodutor.
- `positionMs: Long` — Posição atual em milissegundos.
- `durationMs: Long` — Duração total em milissegundos.
- `isShuffleEnabled: Boolean` — Modo aleatório ativo/inativo.
- `repeatMode: RepeatMode` — Modo de repetição atual.
- `audioOutputName: String` — Nome da rota de áudio ativa (ex.: `"Alto-falante"` ou nome do fone Bluetooth).
- `errorMessage: String?` — Mensagem em português em caso de falha.

## 2. Regras de Validação e Transições de Estado

### Hierarquia de Pastas (`FolderHierarchyValidator`)
1. Normalizar entrada: `trim()`, substituir `\` por `/`, colapsar `//` em `/`, remover `/` final (exceto raiz `/`). Se vazio → `RejectedInvalidPath`.
2. Quebrar em segmentos não vazios (`segments`).
3. Para cada pasta existente `existing`:
   - Se `newSegments == existingSegments` → `RejectedDuplicate(existing.normalizedPath)`.
   - Se `existingSegments` é prefixo completo de `newSegments` → `RejectedCoveredByParent(existing.normalizedPath)`.
   - Se `newSegments` é prefixo completo de `existingSegments` → marcar `existing` em `removedChildren`.
4. Remover `removedChildren`, adicionar nova pasta e retornar `Added(...)`.

### Máquina de Estados do Player (`PlayerStateStore` + `PlaybackService`)
- `IDLE` → `play(track, list)` → `PREPARING` → (`onPrepared`) → `PLAYING`.
- `PLAYING` → `pause()` / perda de foco / fone desconectado → `PAUSED`.
- `PAUSED` → `resume()` (após recuperar/obter foco) → `PLAYING`.
- `PLAYING` / `PAUSED` → `seekTo(ms)` → mantém estado atual com nova posição.
- `PLAYING` → `onCompletion` → avança conforme `repeatMode` (`REPEAT_ONE` reinicia faixa, `REPEAT_ALL` vai para próxima/início, `OFF` vai para próxima ou `COMPLETED` ao fim).
- Qualquer estado → `onError` → `ERROR` com mensagem em português e liberação segura do `MediaPlayer`.

## 3. Persistência Local (Sem Bibliotecas Externas)

- `SharedPreferences` (`"genova_music_prefs"`):
  - `"authorized_folders"`: JSON string via `org.json.JSONArray` contendo `normalizedPath`, `treeUriString`, `displayName`, `addedAtEpochMs`.
  - `"last_scan_epoch_ms"`: `Long` para exibir `"há X minutos"` no card de diagnóstico da tela Pastas.
