# GenovaMusicPlayer

Player de música nativo Android em Kotlin puro, offline-only, sem dependências externas.

## Objetivo
Oferecer navegação e reprodução local de músicas a partir de pastas explicitamente autorizadas pelo usuário, com busca em tempo real, controles em segundo plano (notificação e foco de áudio) e design fiel à referência **Nocturne Violet**.

## Stack
- Kotlin + Framework Android padrão (`compileSdk 34`, `minSdk 26`, `android.useAndroidX=false`)
- Arquitetura MVVM pura (sem AndroidX, Material, Compose, RecyclerView ou Media3)
- Build e testes via Docker (`Dockerfile`)

## Funcionalidades (MVP Parte 1)
- **Pastas (`FoldersActivity`):** seleção explícita via `ACTION_OPEN_DOCUMENT_TREE`, persistência local (`SharedPreferences` + `org.json.JSONArray`), diagnósticos de varredura/uso e aplicação estrita da Regra de Hierarquia (`FolderHierarchyValidator`).
- **Biblioteca & Busca (`LibraryActivity`):** lista virtualizada (`ListView` + `BaseAdapter` + `ViewHolder`), busca em tempo real por título/artista, filtros `"Todas"` e `"Recentes"`, botão `"Aleatório"`, carregamento assíncrono de capas em `LruCache` (`AlbumArtCacheLoader`) e detalhes do arquivo no menu `⋮`.
- **Tocando Agora & Segundo Plano (`NowPlayingActivity` + `PlaybackService`):** `MediaPlayer` com máquina de estados explícita (`PlayerStateStore`), `MediaSession`, `Notification.MediaStyle`, gerenciamento de `AudioFocusRequest`, pausa automática ao desconectar fones (`ACTION_AUDIO_BECOMING_NOISY`), seekbar interativa, shuffle, repeat e compartilhamento.

## Como executar
1. Construir a imagem Docker:
   `docker build -t genova-music-player .`
2. Rodar testes unitários:
   `docker run --rm -v "${PWD}:/workspace" -w /workspace genova-music-player ./gradlew testDebugUnitTest --no-daemon`
3. Gerar APK de debug:
   `docker run --rm -v "${PWD}:/workspace" -w /workspace genova-music-player ./gradlew assembleDebug --no-daemon`
4. Instalar `app/build/outputs/apk/debug/app-debug.apk` no dispositivo Android.

## Regras de domínio
- Pastas selecionadas via `ACTION_OPEN_DOCUMENT_TREE`, sem varredura global do armazenamento.
- Regra de hierarquia: nunca manter pastas em relação ancestral/descendente (filha de pasta já incluída é recusada; pasta mãe remove filhas automaticamente).
- Reprodução com `MediaPlayer`, `MediaSession`, `Notification.MediaStyle`, `ForegroundService` e pausa automática ao perder foco ou desconectar fones.
