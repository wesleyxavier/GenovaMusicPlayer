# Research: MVP Parte 3 — Atalho "Tocando Agora", Mini-Player, Fila a Seguir e Evolução de Playlists

**Feature**: `004-part3-queue-miniplayer-playlists` | **Date**: 2026-11-23

## 1. Mini-Player Persistente sem AndroidX/Fragments

- **Decision**: Implementar o mini-player por meio de um layout XML incluível (`include_mini_player.xml`) combinado a um helper leve `MiniPlayerController` conectado ao `PlayerStateStore` via `addListener`/`removeListener` no `onResume`/`onPause` de cada `Activity` principal (`LibraryActivity`, `PlaylistsActivity`, `FoldersActivity` e `PlaylistDetailActivity`).
- **Rationale**: Como a arquitetura usa `Activity` clássica sem `FragmentActivity` ou `NavigationComponent` do AndroidX (Princípio I), incluir o mesmo componente XML + controller mantém consistência visual, código único sem duplicação e transição instantânea entre abas sem recriar lógica.
- **Alternatives considered**: Migrar todo o app para uma única `Activity` com `ViewFlipper` ou views programáticas — descartado por exigir refatoração invasiva de telas já estabilizadas e testadas nas Partes 1 e 2.

## 2. Subtela "Fila a Seguir" e Reordenação em `ListView` Nativa

- **Decision**: Implementar `QueueActivity` como tela dedicada (ou tema de sheet/modal em tela cheia com botão recolher `v` no topo) utilizando `ListView` + `QueueTrackAdapter` (padrão `ViewHolder`), oferecendo reordenação confiável e acessível por toque direto:
  - Modo de reordenação/alça na linha (`=`): toque na alça ou menu `⋮` permite mover para cima/baixo (ou arraste vertical simples via `OnTouchListener` quando iniciado na alça `=`, com fallback por botões/menu `"Mover para cima"` / `"Mover para baixo"` para acessibilidade TalkBack).
  - Clique no corpo do item da fila chama imediatamente `PlayerStateStore.playQueueIndex(index)` + `PlaybackService` (`ACTION_PLAY`).
- **Rationale**: `RecyclerView` e `ItemTouchHelper` são proibidos pela Constituição (`Princípio I`). Em `ListView` nativa, disponibilizar alça/ações explícitas de subir/descer e drag controlado na alça garante usabilidade ergonômica, acessibilidade (`>= 48dp` + TalkBack) e 100% de previsibilidade tanto manual quanto em testes E2E Appium.
- **Alternatives considered**: Apenas drag-and-drop customizado sem ações no menu — descartado porque usuários de TalkBack e testes automatizados precisam de controles acionáveis sem gestos complexos.

## 3. Regras Puras de Fila em `PlaybackQueueManager`

- **Decision**: Extrair toda a manipulação de lista/índice da fila para a classe Kotlin pura `PlaybackQueueManager` (`domain/player/`), invocada pelo `PlayerStateStore`.
- **Rationale**: Permite testar unitariamente na JVM todas as operações críticas: iniciar fila com origem, adicionar faixa no fim da fila, adicionar playlist inteira à fila, clicar em item específico da fila, remover item antes/no/após o índice ativo mantendo consistência do `currentIndex`, mover item (`fromIndex` → `toIndex`) ajustando `currentIndex`, limpar próximos itens e resolver próximo item considerando `repeatMode`, `isShuffleEnabled` e `autoPlayAfterQueue`.
- **Alternatives considered**: Manter manipulação ad-hoc dentro de `PlayerStateStore` ou `PlaybackService` — dificultaria testes unitários isolados e violaria a separação de domínio.

## 4. Evolução de Playlists mantendo Retrocompatibilidade de Persistência

- **Decision**: Estender `Playlist` com `isPinned: Boolean = false` e `coverStyle: Int = 0` (estilo/paleta local automática gerada a partir do nome ou capa da primeira faixa disponível), serializados em JSON opcional no `PlaylistPreferencesRepository` com valores default quando ausentes em JSONs legados da Parte 2.
- **Rationale**: Playlists já criadas pelo usuário na Parte 2 continuam carregando sem perda de dados ou migração destrutiva. A arte automática local usa gradientes/cores **Nocturne Violet** determinísticos combinados com a miniatura da primeira faixa resolvida via `AlbumArtCacheLoader`.
- **Alternatives considered**: Salvar imagens em arquivos externos no cache — desnecessário quando `AlbumArtCacheLoader` já resolve capas reais das faixas e cores determinísticas geram arte instantânea sem I/O extra.

## 5. Playlists Inteligentes Locais (`Mais Tocadas` e `Adicionadas Recentemente`)

- **Decision**: Criar `TrackPlayStatsRepository` (mapa `trackId -> playCount` em `SharedPreferences`) incrementado quando `PlaybackService` inicia/prepara com sucesso uma faixa, e uma classe pura `SmartPlaylistEngine` para calcular `"Mais Tocadas (Top 50)"` e `"Adicionadas Recentemente"` sobre as faixas autorizadas.
- **Rationale**: Mantém a experiência fiel ao design `colecao_de_playlists.png` 100% offline e testável na JVM, sem adicionar dependências ou banco SQLite externo.
- **Alternatives considered**: Omitir a seção de Playlists Inteligentes — deixaria a tela `Playlists` visualmente incompleta em relação à referência.
