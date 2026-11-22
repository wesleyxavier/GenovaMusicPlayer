---
description: "Task list for MVP Parte 1 — Biblioteca, Busca, Tocando Agora e Pastas"
---

# Tasks: MVP Parte 1 — Biblioteca, Busca, Tocando Agora e Pastas

**Input**: Design documents from `specs/001-mvp-part1-player/`
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/ui-and-viewmodel-contracts.md`, `quickstart.md`

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode rodar em paralelo (arquivos diferentes, sem dependência de tarefas incompletas)
- **[Story]**: User Story correspondente (`[US1]`, `[US2]`, `[US3]`)
- Todos os comandos de build/teste Android/Kotlin DEVEM ser executados via Docker (regra **Docker Isolation**).
- Commits, pushes e criação de PR exigem autorização explícita do usuário no momento da execução.

---

## 0. Branch

- [X] T001 Criar worktree e branch `feature/001-mvp-part1-player` a partir da branch base (skill `openspec-worktree`) em `worktrees/001-mvp-part1-player`

---

## Phase 1: Setup (Infraestrutura Base & Docker)

**Purpose**: Inicializar projeto Android Kotlin puro (zero dependências externas) e ambiente Docker de build/teste.

- [X] T002 Criar `Dockerfile` com JDK 17 e Android SDK (`compileSdk 34`, `build-tools 34.0.0`, `minSdk 26`) na raiz `Dockerfile`
- [X] T003 [P] Criar `settings.gradle.kts` e `build.gradle.kts` raiz sem dependências de terceiros em `settings.gradle.kts` e `build.gradle.kts`
- [X] T004 [P] Criar `app/build.gradle.kts` configurado apenas com Kotlin stdlib e `testImplementation(kotlin("test"))` (zero AndroidX/Material/Compose/RecyclerView/Media3) em `app/build.gradle.kts`
- [X] T005 [P] Criar `.gitignore` e `.graphifyignore` na raiz do repositório em `.gitignore` e `.graphifyignore`
- [X] T006 Criar `app/src/main/AndroidManifest.xml` declarando permissões (`READ_MEDIA_AUDIO`, `READ_EXTERNAL_STORAGE` maxSdk 32, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `POST_NOTIFICATIONS`), Activities e `PlaybackService` em `app/src/main/AndroidManifest.xml`

### 1.1 Commit Group 1
- [X] T007 Solicitar autorização e executar `git commit` + `git push` da estrutura inicial e Docker

---

## Phase 2: Foundational (Design System Nocturne Violet & Núcleo Compartilhado)

**Purpose**: Recursos visuais nativos do tema **Nocturne Violet** e modelos/singleton compartilhados necessários para todas as histórias.

**⚠️ CRITICAL**: Nenhuma User Story de UI deve iniciar antes desta fase.

- [X] T008 [P] Definir paleta de cores completa do `DESIGN.md` (`#13121B`, `#9D7BFF`, `#CEBDFF`, `#E5E0EE`, `#201E28`, `#2A2933`, `#35343E`, `#948E9F`, `#FFB4AB`, etc.) em `app/src/main/res/values/colors.xml`
- [X] T009 [P] Definir dimensões (`48dp` alvo mínimo, `64dp` play/pause, `68dp` linha de faixa, raios `8dp`/`16dp`/pill) e estilos base em `app/src/main/res/values/dimens.xml` e `app/src/main/res/values/styles.xml`
- [X] T010 [P] Definir todos os textos de interface, rótulos de acessibilidade (`contentDescription`) e mensagens de erro em português do Brasil em `app/src/main/res/values/strings.xml`
- [X] T011 [P] Adicionar fontes estáticas `Plus Jakarta Sans` e drawables nativos XML (pill buttons, cards arredondados, gradiente seekbar, ícones vetoriais) em `app/src/main/res/font/` e `app/src/main/res/drawable/`
- [X] T012 [P] Criar entidades puras `Track`, `LibraryFilter`, `PlayerStatus`, `RepeatMode`, `PlaybackSession` em `app/src/main/java/com/genova/musicplayer/domain/library/Track.kt`, `app/src/main/java/com/genova/musicplayer/domain/library/LibraryFilter.kt` e `app/src/main/java/com/genova/musicplayer/domain/player/PlaybackSession.kt`
- [X] T013 Criar singleton `PlayerStateStore` com listeners via callbacks (sem `LiveData` ou `Flow`) e regras explícitas de transição de estado em `app/src/main/java/com/genova/musicplayer/domain/player/PlayerStateStore.kt`
- [X] T014 [P] Criar testes unitários das transições de estado do player (`PlayerStatus`, `RepeatMode`, `Shuffle`) em `app/src/test/java/com/genova/musicplayer/domain/player/PlayerStateTransitionsTest.kt`

### 2.1 Commit Group 2
- [X] T015 Solicitar autorização e executar `git commit` + `git push` da base visual e modelos centrais

---

## Phase 3: User Story 1 - Selecionar e Gerenciar Pastas de Música (Priority: P1) 🎯 MVP

**Goal**: Permitir escolher pastas via `ACTION_OPEN_DOCUMENT_TREE`, aplicar estritamente a Regra de Hierarquia em Kotlin puro e gerenciar a lista persistida na tela `Pastas`.

**Independent Test**: Abrir aba `Pastas`, adicionar pasta via seletor do sistema, validar recusa de subpasta, remoção automática de filhas ao adicionar pasta mãe, manutenção de irmãs (`/Music` vs `/Music2`) e exclusão com confirmação; validar os 7 casos unitários via Docker.

### Tests for User Story 1
- [X] T016 [P] [US1] Criar testes unitários obrigatórios em Kotlin puro cobrindo os 7 cenários (pasta igual, filha, mãe, irmã, prefixo de texto `/Music` vs `/Music2`, barra final e caminho vazio) em `app/src/test/java/com/genova/musicplayer/domain/folder/FolderHierarchyValidatorTest.kt`

### Implementation for User Story 1
- [X] T017 [P] [US1] Implementar `AuthorizedFolder` e `FolderAdditionResult` em `app/src/main/java/com/genova/musicplayer/domain/folder/AuthorizedFolder.kt`
- [X] T018 [P] [US1] Implementar `FolderPathNormalizer` (normalização de barras, espaços e quebra por segmentos completos) em `app/src/main/java/com/genova/musicplayer/domain/folder/FolderPathNormalizer.kt`
- [X] T019 [US1] Implementar `FolderHierarchyValidator` (rejeição de duplicata/descendente e remoção automática de filhas) em `app/src/main/java/com/genova/musicplayer/domain/folder/FolderHierarchyValidator.kt`
- [X] T020 [US1] Implementar `FolderPreferencesRepository` usando `SharedPreferences` + `org.json.JSONArray` (pastas autorizadas + timestamp da última varredura) e conversão de `treeUri` para caminho normalizado em `app/src/main/java/com/genova/musicplayer/data/FolderPreferencesRepository.kt`
- [X] T021 [US1] Implementar `FoldersViewModel` em Kotlin comum com estado de diagnóstico (`Status da Varredura`, `Uso Total de Áudio`), validação e remoção em `app/src/main/java/com/genova/musicplayer/viewmodel/FoldersViewModel.kt`
- [X] T022 [P] [US1] Criar layouts XML `activity_folders.xml` e `item_folder.xml` fiéis ao `gerenciador_de_pastas` Stitch (sem ícone de Perfil per `Q3: B`, CTA `+ Adicionar Pasta`, card `Regra de Hierarquia`, cards de diagnóstico, alvos `>= 48dp` e `contentDescription`) em `app/src/main/res/layout/activity_folders.xml` e `app/src/main/res/layout/item_folder.xml`
- [X] T023 [US1] Implementar `FolderListAdapter` (`BaseAdapter` + `ViewHolder`) e `FoldersActivity` integrando `ACTION_OPEN_DOCUMENT_TREE`, confirmação de remoção e navegação para `Músicas` em `app/src/main/java/com/genova/musicplayer/ui/FolderListAdapter.kt` e `app/src/main/java/com/genova/musicplayer/ui/FoldersActivity.kt`
- [X] T024 [US1] Gerar APK debug inicial via Docker (`./gradlew testDebugUnitTest assembleDebug`) em `app/build/outputs/apk/debug/app-debug.apk` para deploy e teste direto da tela `Pastas` no celular

### 3.1 Commit Group 3
- [X] T025 Solicitar autorização e executar `git commit` + `git push` da User Story 1

---

## Phase 4: User Story 2 - Navegar, Filtrar e Buscar na Biblioteca de Músicas (Priority: P1)

**Goal**: Exibir músicas apenas das pastas autorizadas em lista virtualizada com busca em tempo real, chips `"Todas"` e `"Recentes"` (`Q2: B`), botão `"Aleatório"`, capas em cache assíncrono e menu `⋮` com `"Ver detalhes do arquivo"`.

**Independent Test**: Com pastas autorizadas populadas, abrir `Músicas`, buscar por título/artista em tempo real, alternar `"Todas"` e `"Recentes"`, abrir `"Ver detalhes do arquivo"` em `⋮` e tocar em faixa ou `"Aleatório"`.

### Tests for User Story 2
- [X] T026 [P] [US2] Criar testes unitários para busca instantânea por título/artista e ordenação `"Todas"` / `"Recentes"` em `app/src/test/java/com/genova/musicplayer/domain/library/TrackFilterEngineTest.kt`

### Implementation for User Story 2
- [X] T027 [P] [US2] Implementar `TrackFilterEngine` em Kotlin puro (busca case-insensitive/normalizada e filtros `ALL`/`RECENT`) em `app/src/main/java/com/genova/musicplayer/domain/library/TrackFilterEngine.kt`
- [X] T028 [US2] Implementar `MediaStoreTrackRepository` filtrando `MediaStore.Audio.Media.EXTERNAL_CONTENT_URI` exclusivamente pelos caminhos autorizados em `ExecutorService` em `app/src/main/java/com/genova/musicplayer/data/MediaStoreTrackRepository.kt`
- [X] T029 [P] [US2] Implementar `AlbumArtCacheLoader` com `android.util.LruCache<String, Bitmap>`, `BitmapFactory.Options.inSampleSize` e `ExecutorService` + `Handler` (zero decodificação na main thread) em `app/src/main/java/com/genova/musicplayer/data/AlbumArtCacheLoader.kt`
- [X] T030 [US2] Implementar `LibraryViewModel` em Kotlin comum coordenando carga do `MediaStore`, filtro em tempo real, estatísticas (`{N} músicas`) e disparo de reprodução direta/aleatória em `app/src/main/java/com/genova/musicplayer/viewmodel/LibraryViewModel.kt`
- [X] T031 [P] [US2] Criar layouts XML `activity_library.xml` e `item_track.xml` fiéis ao `biblioteca_busca` Stitch (sem ícone de Perfil per `Q3: B`, apenas chips `"Todas"` e `"Recentes"` per `Q2: B`, sem mini-player per `Q1: B`, alvos `>= 48dp` e `contentDescription`) em `app/src/main/res/layout/activity_library.xml` e `app/src/main/res/layout/item_track.xml`
- [X] T032 [US2] Implementar `TrackListAdapter` (`ListView` + `BaseAdapter` + `ViewHolder` com indicador animado de faixa ativa) e `LibraryActivity` (incluindo estado vazio e modal `"Ver detalhes do arquivo"` no menu `⋮`) em `app/src/main/java/com/genova/musicplayer/ui/TrackListAdapter.kt` e `app/src/main/java/com/genova/musicplayer/ui/LibraryActivity.kt`

### 4.1 Commit Group 4
- [X] T033 Solicitar autorização e executar `git commit` + `git push` da User Story 2

---

## Phase 5: User Story 3 - Controlar Reprodução na Tela "Tocando Agora" e em Segundo Plano (Priority: P1)

**Goal**: Garantir reprodução contínua em segundo plano (`PlaybackService` + `MediaPlayer` + `MediaSession` + `Notification.MediaStyle` + foco de áudio + pausa ao desconectar fones) e controles ergonômicos na metade inferior da tela `Tocando Agora`.

**Independent Test**: Iniciar reprodução, abrir `Tocando Agora`, testar play/pause, anterior, próxima, seek, shuffle, repeat, `"Compartilhar"`, `"Trocar"` saída de áudio, verificar placeholders `"Em breve"` para `"Fila a Seguir"`/`"Ver Letra"` (`Q1: B`), voltar à lista sem interromper áudio, bloquear tela e testar controles na notificação e pausa ao desconectar fones.

- [X] T034 [US3] Implementar `PlaybackService` (`ForegroundService`) encapsulando `MediaPlayer` com máquina de estados explícita, `MediaSession`, `Notification.MediaStyle`, gerenciamento de `AudioFocusRequest` e `BroadcastReceiver` para `ACTION_AUDIO_BECOMING_NOISY` em `app/src/main/java/com/genova/musicplayer/service/PlaybackService.kt`
- [X] T035 [P] [US3] Criar layout XML `activity_now_playing.xml` fiel ao `tocando_agora` Stitch (sem ícone de Perfil per `Q3: B`, sem botão Favoritar per `Q2: B`, botões `"Fila a Seguir"` e `"Ver Letra"` inativos com `"Em breve"` per `Q1: B`, botão Play/Pause `64dp` na metade inferior e alvos `>= 48dp`) em `app/src/main/res/layout/activity_now_playing.xml`
- [X] T036 [US3] Implementar `NowPlayingActivity` conectada ao `PlayerStateStore` via callbacks, `SeekBar` interativa, `Intent.ACTION_SEND` para compartilhar, seletor de saída de áudio (`"Trocar"`), modal de detalhes técnicos no menu `⋮` e suporte a capa expandida em `app/src/main/java/com/genova/musicplayer/ui/NowPlayingActivity.kt`

### 5.1 Commit Group 5
- [X] T037 Solicitar autorização e executar `git commit` + `git push` da User Story 3

---

## Phase 6: Testes, Deploy no Celular, Benchmark & Pre-PR Checklist

**Purpose**: Validar suíte unitária no Docker, gerar APK final para deploy direto no celular, medir abertura com 2.000+ músicas, atualizar documentação e preparar PR.

- [X] T038 Executar suíte completa de testes unitários e build debug final via Docker (`./gradlew testDebugUnitTest assembleDebug`) e disponibilizar `app/build/outputs/apk/debug/app-debug.apk` para deploy direto no celular
- [ ] T039 Executar deploy no celular, checklist manual end-to-end e benchmark de abertura da lista com pelo menos 2.000 músicas conforme `specs/001-mvp-part1-player/quickstart.md`, registrando o resultado medido
- [X] T040 [P] Atualizar `README.md` com instruções de arquitetura, build via Docker e funcionalidades da Parte 1 em `README.md`
- [X] T041 Executar `graphify update .` na raiz do projeto para sincronizar o grafo de conhecimento
- [X] T042 Solicitar autorização explícita do usuário para `git commit` final + `git push` e criação do PR para a branch alvo

---

## Dependencies & Execution Order

- **Branch (`T001`)** → **Phase 1 Setup (`T002–T007`)** → **Phase 2 Foundational (`T008–T015`)**.
- **Phase 3 (US1 - Pastas)**, **Phase 4 (US2 - Biblioteca)** e **Phase 5 (US3 - Tocando Agora & Service)** dependem da **Phase 2**.
  - US1 e US2 podem avançar em paralelo após Phase 2 (US2 consome `FolderPreferencesRepository` de US1 apenas na integração final).
  - US3 depende do `PlayerStateStore` (Phase 2) e integra-se aos disparos de reprodução de US2.
- **Phase 6 (Testes, Benchmark & Pre-PR)** depende da conclusão de US1, US2 e US3.

## Implementation Strategy

- **MVP Incremental:** Entregar primeiro `Phase 1 + Phase 2 + US1` (gestão de pastas validada por testes unitários), em seguida `US2` (lista virtualizada + busca + capas em cache) e por fim `US3` (`PlaybackService` + `NowPlayingActivity`), validando o benchmark de 2.000+ músicas antes de encerrar.
