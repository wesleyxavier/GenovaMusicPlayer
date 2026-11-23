# UI & Architecture Contracts: MVP Parte 3

**Feature**: `004-part3-queue-miniplayer-playlists` | **Date**: 2026-11-23

## 1. Tela Principal (`LibraryActivity` / `activity_library.xml`)

- **Chip/Botão `"Tocando Agora"`**:
  - Posicionado na barra de chips ao lado de `"Todas"` e `"Recentes"` (referência `docs/design/biblioteca_com_mini_player_ativo.png`).
  - Altura mínima de toque `>= 48dp` (ou wrapper acessível `>= 48dp`), `contentDescription` em PT-BR.
  - Ação ao clicar: se `PlayerStateStore.getState().currentTrack != null`, abre `NowPlayingActivity`; caso contrário exibe `Toast` `"Nenhuma música em reprodução no momento"`.
- **Menu `⋮` da faixa na lista**:
  - Passa a exibir `"Adicionar à fila"`, `"Adicionar à playlist"` e `"Ver detalhes do arquivo"`.
  - `"Adicionar à playlist"` abre o novo modal multi-seleção (`PlaylistDialogs.showAddToPlaylistsDialog`).
  - `"Adicionar à fila"` enfileira a faixa no `PlayerStateStore` (e inicia reprodução via `PlaybackService` se o player estiver `IDLE`) com toast `"Adicionada à fila"`.

## 2. Mini-Player Persistente (`include_mini_player.xml` / `MiniPlayerController.kt`)

- Exibido acima da barra de navegação inferior em `LibraryActivity`, `PlaylistsActivity`, `FoldersActivity` (e no rodapé de `PlaylistDetailActivity`) quando `session.currentTrack != null`. Oculto (`View.GONE`) quando `currentTrack == null`.
- Elementos obrigatórios (referência `biblioteca_com_mini_player_ativo.png` sem ícone de coração/favoritar):
  - Barra de progresso linear fina (`2dp`) refletindo `positionMs / durationMs`.
  - Miniatura da capa (`40dp`), título (1 linha, ellipsize), artista (1 linha, ellipsize).
  - Botão Play/Pause (`>= 48dp`, alternando ícone `ic_play`/`ic_pause`).
  - Botão Próxima (`>= 48dp`, `ic_skip_next`).
  - Toque no card do mini-player abre `NowPlayingActivity`.

## 3. Tela `"Reproduzindo Agora"` (`NowPlayingActivity`) & Subtela `"Fila a Seguir"` (`QueueActivity`)

- **`NowPlayingActivity`**:
  - Substitui o placeholder de toast do botão `btn_queue_placeholder` (renomeado ou reutilizado como `btn_open_queue`, removendo rótulo `"Em breve"`) para abrir `QueueActivity`.
- **`QueueActivity` (`activity_queue.xml` / `item_queue_track.xml`)**:
  - Referência visual: `docs/design/fila_a_seguir.png` (sem ícone de perfil/favoritar).
  - Cabeçalho: botão recolher/voltar (`v`), título `"Fila A Seguir"`, badge com contagem total da fila e botão `"Limpar"`.
  - Origem da reprodução (`queueOriginLabel`) + indicador `"Aleatório ON/OFF"`.
  - Cartão `"Tocando Agora"` com capa, título, artista, badge qualidade (`Hi-Fi`/`FLAC`/bitrate), tempo `mm:ss / mm:ss`, barra de progresso e botão Play/Pause (`>= 48dp`).
  - Seção `"A Seguir na Fila"`: resumo (`"{N} faixas restantes • {duração}"`), botão `"+ Adicionar"` (abre modal para escolher música da biblioteca ou playlist inteira para adicionar à fila) e switch `"Auto-play após o fim da fila"`.
  - Lista virtualizada (`ListView` + `QueueTrackAdapter`):
    - Exibe toda a fila (`activeList`, incluindo faixas tocadas anteriormente e próximas) com número da posição (`1..N`), capa, título (destaque lavanda e fundo elevado na faixa ativa), artista • formato • duração.
    - Clique no item: dispara reprodução imediata daquele item (`QueueViewModel.playQueueItem(position)` -> `PlaybackService`).
    - Arraste vertical na alça de reordenação (`=`): reordena o item ao vivo na fila (`PlayerStateStore.moveQueueItem`).
    - Menu `⋮` do item: `"Remover da fila"`.
  - Rodapé de ações: `"Salvar Playlist"` (abre diálogo `"Nova Playlist"` preenchendo com as faixas da fila), botões rápidos `"Aleatório"` e `"Repetir"`.

## 4. Módulo de Playlists Evoluído (`PlaylistsActivity`, `PlaylistDetailActivity`, `PlaylistDialogs`)

- **`PlaylistsActivity` (`colecao_de_playlists.png`)**:
  - Campo `"Filtrar faixas ou coleções..."` filtrando playlists e atalhos em tempo real.
  - Card destaque `"Criar Nova Playlist"` com botão `"Criar →"`.
  - Cabeçalho `"Minhas Playlists"` com contagem, seletor de ordenação (`Mais recentes`, `Alfabética`, `Mais faixas`) e toggle Grade (`GridView`) / Lista (`ListView`).
  - Card/item exibe capa (da 1ª faixa ou arte automática Nocturne Violet), selo `"★ FIXADA"` quando `isPinned`, nome, contagem + duração (`"{N} músicas • {duração}"`), botão Play rápido e menu `⋮` (`"Adicionar à fila"`, `"Fixar/Desafixar"`, `"Renomear"`, `"Excluir"`).
  - Seção `"Playlists Inteligentes do Sistema"`: `"Mais Tocadas (Top 50)"` e `"Adicionadas Recentemente"` com botão Play rápido e abertura de detalhes.
- **Modal `"Nova Playlist"` (`criar_nova_playlist.png`)**:
  - Campo de nome com `maxLength="60"` e contador `"N/60"`, seletor de estilo de arte automática, toggle `"Fixar no topo da biblioteca"`, botões `"Cancelar"` e `"+ Criar Playlist"`.
- **Modal `"Adicionar à Playlist"` (`adicionar_playlist.png`)**:
  - Resumo da faixa no topo, botão `"+ Criar Nova Playlist"`, busca `"Buscar playlist existente..."`, lista com checkboxes para seleção múltipla e botão `"Concluir"`.
- **`PlaylistDetailActivity` (`detalhes_da_playlist.png`)**:
  - Cabeçalho com voltar, menu `⋮` (compartilhar lista/títulos, renomear, fixar/desafixar, excluir), capa da playlist, selo `"PLAYLIST PESSOAL"`, título, resumo (`"{N} faixas • {duração} • {formatos}"`), botões `Play` e `Aleatório`.
  - Barra de ações: `"+ Adicionar músicas"` (dialog multi-seleção da biblioteca), `"Ordem manual"` (toggle modo reordenação na lista), `"Adicionar à fila"`.
  - Campo `"Filtrar faixas na playlist..."`.
  - Lista de faixas com suporte a mover para cima/baixo em `"Ordem manual"` e menu `⋮` (`"Adicionar à fila"`, `"Remover da playlist"`, `"Ver detalhes do arquivo"`).
