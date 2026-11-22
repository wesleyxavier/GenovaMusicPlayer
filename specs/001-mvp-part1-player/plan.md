# Implementation Plan: MVP Parte 1 — Biblioteca, Busca, Tocando Agora e Pastas

**Branch**: `001-mvp-part1-player` | **Date**: 2026-11-21 | **Spec**: [spec.md](file:///c:/Users/User/Documents/TI/Lavi/Web/Genova/src/PROJETOS/GenovaMusicPlayer/specs/001-mvp-part1-player/spec.md)

**Input**: Feature specification from `specs/001-mvp-part1-player/spec.md` + Design System `nocturne_violet/DESIGN.md` e telas Stitch (`biblioteca_busca`, `tocando_agora`, `gerenciador_de_pastas`).

## Summary

Construir a Parte 1 do MVP do **GenovaMusicPlayer** em Kotlin puro para Android usando exclusivamente o framework Android padrão (`View`s XML, `ListView` com `ViewHolder`, `MediaStore`, `ACTION_OPEN_DOCUMENT_TREE`, `MediaPlayer`, `MediaSession`, `Notification.MediaStyle` e `ForegroundService`), seguindo a arquitetura MVVM sem bibliotecas externas, o design system **Nocturne Violet** e todas as restrições da Constituição.

## Technical Context

**Language/Version**: Kotlin (stdLib apenas), Java/JVM target 17, Android Gradle Plugin
**Primary Dependencies**: Nenhuma biblioteca de terceiros (apenas Android SDK framework `android.*` + Kotlin stdlib; zero AndroidX, Material, Compose, RecyclerView, Media3 ou Coroutines)
**Storage**: `MediaStore` (leitura de faixas locais filtrada por pastas), `SharedPreferences` + `org.json.JSONArray` (persistência de pastas autorizadas e timestamp de varredura), `android.util.LruCache` (cache em memória de capas redimensionadas)
**Testing**: Testes unitários JVM (Kotlin puro para regras de pastas, busca, filtros e estado do player), executados em container Docker
**Target Platform**: Android API 26+ (Android 8.0+) até API 34 (`compileSdk 34`)
**Project Type**: mobile-app (Android nativo)
**Performance Goals**: Abertura fluida da lista com pelo menos 2.000 músicas (benchmark obrigatório antes da conclusão), virtualização via `ListView` + `ViewHolder`, zero I/O ou decodificação de bitmap na main thread
**Constraints**: Offline-only, zero dependências externas, textos e erros em PT-BR, alvos de toque `>= 48dp`, controles principais na metade inferior em `Tocando Agora`, compatibilidade total com TalkBack, builds/testes exclusivamente em Docker
**Scale/Scope**: 3 telas da Parte 1 (`Biblioteca & Busca`, `Tocando Agora`, `Gerenciador de Pastas`) + `PlaybackService` em primeiro plano + notificação de mídia

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Princípio I (Zero Dependências Externas):** Apenas SDK Android padrão e Kotlin stdlib. Nenhuma dependência AndroidX, Material, Compose, RecyclerView ou Media3 no `build.gradle.kts`.
- [x] **Princípio II (MVVM Puro + Singleton):** `LibraryViewModel` e `FoldersViewModel` são classes Kotlin comuns; lógica de pastas em classes Kotlin puras; `PlayerStateStore` é um singleton observado por callbacks.
- [x] **Princípio III (Escopo Rígido do MVP):** Apenas recursos da Parte 1 implementados. Conforme decisão `Q1: B`, `"Fila a Seguir"` e `"Ver Letra"` permanecem apenas como placeholders visuais inativos (`"Em breve"`) sem lógica de fila ou leitura de letras, e não há mini-player.
- [x] **Princípio IV (Biblioteca e Hierarquia de Pastas):** `ACTION_OPEN_DOCUMENT_TREE` + `MediaStore` filtrado + `FolderHierarchyValidator` em Kotlin puro com testes unitários para os 7 cenários obrigatórios.
- [x] **Princípio V (Reprodução e Áudio):** `MediaPlayer` com máquina de estados explícita, `MediaSession`, `Notification.MediaStyle`, `ForegroundService`, foco de áudio e pausa ao desconectar fones.
- [x] **Princípio VI (Interface e Acessibilidade):** Views do framework XML seguindo `nocturne_violet/DESIGN.md` e telas Stitch, PT-BR, alvos `>= 48dp`, controles inferiores e `contentDescription`.
- [x] **Princípio VII (Simplicidade):** `ListView` nativo, `ExecutorService` + `Handler`, sem camadas extras desnecessárias.
- [x] **Docker Isolation:** Todos os comandos Gradle (`testDebugUnitTest`, `assembleDebug`) rodam dentro de container Docker.

*Reavaliação pós-Phase 1:* Nenhum desvio ou violação introduzida nos artefatos [research.md](file:///c:/Users/User/Documents/TI/Lavi/Web/Genova/src/PROJETOS/GenovaMusicPlayer/specs/001-mvp-part1-player/research.md), [data-model.md](file:///c:/Users/User/Documents/TI/Lavi/Web/Genova/src/PROJETOS/GenovaMusicPlayer/specs/001-mvp-part1-player/data-model.md) e [ui-and-viewmodel-contracts.md](file:///c:/Users/User/Documents/TI/Lavi/Web/Genova/src/PROJETOS/GenovaMusicPlayer/specs/001-mvp-part1-player/contracts/ui-and-viewmodel-contracts.md).

## Project Structure

### Documentation (this feature)

```text
specs/001-mvp-part1-player/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── checklists/
│   └── requirements.md
├── contracts/
│   └── ui-and-viewmodel-contracts.md
└── tasks.md             # (/speckit-tasks)
```

### Source Code (repository root)

```text
Dockerfile
build.gradle.kts
settings.gradle.kts
app/
├── build.gradle.kts
└── src/
    ├── main/
    │   ├── AndroidManifest.xml
    │   ├── res/
    │   │   ├── drawable/          # Formas pill, cards, gradientes e ícones vetoriais
    │   │   ├── font/              # Plus Jakarta Sans
    │   │   ├── layout/            # activity_library.xml, activity_now_playing.xml, activity_folders.xml, item_track.xml, item_folder.xml
    │   │   └── values/            # colors.xml (Nocturne Violet), dimens.xml, strings.xml (PT-BR), styles.xml
    │   └── java/com/genova/musicplayer/
    │       ├── domain/
    │       │   ├── folder/        # FolderPathNormalizer.kt, FolderHierarchyValidator.kt, AuthorizedFolder.kt
    │       │   ├── library/       # Track.kt, LibraryFilter.kt, TrackFilterEngine.kt
    │       │   └── player/        # PlayerStatus.kt, RepeatMode.kt, PlaybackSession.kt, PlayerStateStore.kt
    │       ├── data/
    │       │   ├── FolderPreferencesRepository.kt
    │       │   ├── MediaStoreTrackRepository.kt
    │       │   └── AlbumArtCacheLoader.kt
    │       ├── service/
    │       │   └── PlaybackService.kt
    │       ├── viewmodel/
    │       │   ├── LibraryViewModel.kt
    │       │   └── FoldersViewModel.kt
    │       └── ui/
    │           ├── LibraryActivity.kt
    │           ├── TrackListAdapter.kt
    │           ├── NowPlayingActivity.kt
    │           ├── FoldersActivity.kt
    │           └── FolderListAdapter.kt
    └── test/java/com/genova/musicplayer/
        └── domain/
            ├── folder/            # FolderHierarchyValidatorTest.kt (7 cenários obrigatórios)
            ├── library/           # TrackFilterEngineTest.kt
            └── player/            # PlayerStateTransitionsTest.kt
```

**Structure Decision**: Projeto Android único (`app/`) com separação clara entre `domain/` (Kotlin puro testável sem Android), `data/` (`MediaStore`, `SharedPreferences`, `LruCache`), `service/` (`PlaybackService` em primeiro plano), `viewmodel/` (classes Kotlin comuns) e `ui/` (`Activity`s e `BaseAdapter`s com `ViewHolder`).

## Complexity Tracking

Nenhuma violação à Constituição identificada.
