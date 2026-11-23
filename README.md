# GenovaMusicPlayer

Player de música nativo Android em Kotlin puro, offline-only, sem dependências externas.

## Objetivo
Oferecer navegação e reprodução local de músicas a partir de pastas explicitamente autorizadas pelo usuário, com busca em tempo real, controles em segundo plano (notificação e foco de áudio) e design fiel à referência **Nocturne Violet**.

## Stack
- Kotlin + Framework Android padrão (`compileSdk 34`, `minSdk 26`, `android.useAndroidX=false`)
- Arquitetura MVVM pura (sem AndroidX, Material, Compose, RecyclerView ou Media3)
- Build e testes via Docker (`Dockerfile`)

## Funcionalidades (MVP Parte 1, Parte 2 & Parte 3)
- **Pastas (`FoldersActivity`):** seleção explícita via `ACTION_OPEN_DOCUMENT_TREE`, persistência local (`SharedPreferences` + `org.json.JSONArray`), diagnósticos de varredura/uso e aplicação estrita da Regra de Hierarquia (`FolderHierarchyValidator`, exibindo aviso somente em tentativa inválida).
- **Biblioteca & Busca (`LibraryActivity`):** lista virtualizada (`ListView` + `BaseAdapter` + `ViewHolder`), busca em tempo real por título/artista, filtros `"Todas"` e `"Recentes"`, chip `"Tocando Agora"`, botão `"Aleatório"`, execução imediata ao clicar em 1 música (inicia fila única se vazia, ou adiciona como última e reproduz se já houver fila ativa), carregamento assíncrono de capas em `LruCache` (`AlbumArtCacheLoader`), `"Adicionar à fila"`, `"Adicionar à playlist"` (seleção múltipla) e detalhes do arquivo no menu `⋮`.
- **Mini-Player Persistente (`MiniPlayerController`):** barra flutuante exibida acima da navegação inferior nas abas principais com capa, título/artista, progresso, Play/Pause, Próxima faixa e acesso direto a `"Reproduzindo Agora"`.
- **Fila a Seguir (`QueueActivity` + `PlaybackQueueManager`):** subtela aberta a partir de `"Reproduzindo Agora"` exibindo cartão ativo e fila completa (incluindo tocadas antes e próximas), adição de músicas e playlists, remoção via menu `⋮`, reordenação por arraste na alça `=`, clique direto para tocar qualquer item da fila, `"Limpar"`, `"Salvar Playlist"` e toggle `"Auto-play após o fim da fila"`.
- **Playlists (`PlaylistsActivity` & `PlaylistDetailActivity`):** criação com contador de 60 caracteres, arte automática e fixação no topo (`isPinned`), busca, ordenação (`Mais recentes`, `Alfabética`, `Mais faixas`), visualização Grade/Lista, seleção múltipla ao adicionar faixa, filtro interno, `"Ordem manual"` persistida, `"Adicionar músicas"`, `"Adicionar à fila"` e `"Playlists Inteligentes do Sistema"` (`Mais Tocadas (Top 50)` e `Adicionadas Recentemente`).
- **Tocando Agora & Segundo Plano (`NowPlayingActivity` + `PlaybackService`):** `MediaPlayer` com máquina de estados explícita (`PlayerStateStore`), `MediaSession`, `Notification.MediaStyle`, gerenciamento de `AudioFocusRequest`, pausa automática ao desconectar fones (`ACTION_AUDIO_BECOMING_NOISY`), saída automática pelo dispositivo ativo do sistema, seekbar interativa, shuffle, repeat, atalho para `"Fila a Seguir"` e compartilhamento.

## Como executar
1. Construir a imagem Docker:
   `docker build -t genova-music-player .`
2. Rodar testes unitários:
   `docker run --rm -v "${PWD}:/workspace" -w /workspace genova-music-player ./gradlew testDebugUnitTest --no-daemon`
3. Gerar APK de debug:
   `docker run --rm -v "${PWD}:/workspace" -w /workspace genova-music-player ./gradlew assembleDebug --no-daemon`
4. Instalar `app/build/outputs/apk/debug/app-debug.apk` no dispositivo Android.
5. Rodar testes E2E com Appium em container (via subagente `appium-test-hydrogen` ou `appium-test-haiku`):
   `docker compose -f docker-compose.e2e.yml up --build --abort-on-container-exit`

## Regras de domínio
- Pastas selecionadas via `ACTION_OPEN_DOCUMENT_TREE`, sem varredura global do armazenamento.
- Regra de hierarquia: nunca manter pastas em relação ancestral/descendente (filha de pasta já incluída é recusada; pasta mãe remove filhas automaticamente).
- Reprodução com `MediaPlayer`, `MediaSession`, `Notification.MediaStyle`, `ForegroundService` e pausa automática ao perder foco ou desconectar fones.

## Design de Referência (Nocturne Violet)
- [Biblioteca & Busca](docs/design/biblioteca_busca.png)
- [Biblioteca com Mini-Player Ativo](docs/design/biblioteca_com_mini_player_ativo.png)
- [Tocando Agora](docs/design/tocando_agora.png)
- [Fila a Seguir](docs/design/fila_a_seguir.png)
- [Coleção de Playlists](docs/design/colecao_de_playlists.png)
- [Detalhes da Playlist](docs/design/detalhes_da_playlist.png)
- [Criar Nova Playlist](docs/design/criar_nova_playlist.png)
- [Adicionar à Playlist](docs/design/adicionar_playlist.png)
- [Gerenciador de Pastas](docs/design/gerenciador_de_pastas.png)
