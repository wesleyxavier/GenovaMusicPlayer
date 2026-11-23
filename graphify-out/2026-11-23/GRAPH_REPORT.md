# Graph Report - 003-e2e-appium-and-apk-logo  (2026-11-22)

## Corpus Check
- 108 files · ~141,338 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1058 nodes · 1309 edges · 84 communities (81 shown, 3 thin omitted)
- Extraction: 99% EXTRACTED · 1% INFERRED · 0% AMBIGUOUS · INFERRED: 11 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `92869d74`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Tasks: [FEATURE NAME]
- SKILL.md
- SKILL.md
- SKILL.md
- Tasks: MVP Parte 1 — Biblioteca, Busca, Tocando Agora e Pastas
- Execution Steps
- Execution Steps
- Execution Steps
- common.ps1
- GenovaMusicPlayer — Instruções para Agentes de IA
- Core Principles
- 1. Entidades de Domínio (Kotlin Puro — sem `android.*`)
- Feature Specification: [FEATURE NAME]
- Feature Specification: MVP Parte 1 — Biblioteca, Busca, Tocando Agora e Pastas
- SKILL.md
- SKILL.md
- SKILL.md
- SKILL.md
- SKILL.md
- SKILL.md
- SKILL.md
- SKILL.md
- SKILL.md
- Core Principles
- Implementation Plan: [FEATURE]
- Implementation Plan: MVP Parte 1 — Biblioteca, Busca, Tocando Agora e Pastas
- Research & Technical Decisions: MVP Parte 1 — GenovaMusicPlayer
- SKILL.md
- SKILL.md
- SKILL.md
- create-new-feature.ps1
- 2. Contratos de Tela (Alinhados a Q1: B, Q2: B, Q3: B)
- Quickstart & Validation Guide: MVP Parte 1 — GenovaMusicPlayer
- SKILL.md
- SKILL.md
- SKILL.md
- SKILL.md
- SKILL.md
- SKILL.md
- SKILL.md
- SKILL.md
- SKILL.md
- SKILL.md
- SKILL.md
- SKILL.md
- Specification Quality Checklist: MVP Parte 1 — Biblioteca, Busca, Tocando Agora e Pastas
- [CHECKLIST TYPE] Checklist: [FEATURE NAME]
- gradlew
- LibraryActivity
- PlaybackService
- PlayerStateStore
- NowPlayingActivity
- FoldersViewModel
- FoldersActivity
- TrackListAdapter
- .addFolder
- FolderListAdapter
- FolderHierarchyValidatorTest
- .decodeSampledArt
- FolderPathNormalizer
- FolderPreferencesRepository
- AuthorizedFolder
- .render
- TrackFilterEngineTest
- Playlist
- PlaylistsViewModel
- Tasks: MVP Parte 2 — Playlists e Refinamentos
- User Scenarios & Testing *(mandatory)*
- Implementation Plan: MVP Parte 2 — Playlists e Refinamentos
- Data Model: MVP Parte 2 — Playlists e Refinamentos
- Research & Technical Decisions: MVP Parte 2 — Playlists e Refinamentos
- Specification Quality Checklist: MVP Parte 2 — Playlists e Refinamentos
- Quickstart & Validation Guide: MVP Parte 2 — Playlists e Refinamentos
- UI & Architecture Contracts: MVP Parte 2 — Playlists e Refinamentos
- Implementation Plan: Infraestrutura E2E Appium, Logo do APK e Diretriz GitHub MCP
- Research & Technical Decisions: 003-e2e-appium-and-apk-logo
- test_smoke_screens.py

## God Nodes (most connected - your core abstractions)
1. `PlaybackService` - 30 edges
2. `LibraryActivity` - 30 edges
3. `PlaylistDetailActivity` - 22 edges
4. `Playlist` - 21 edges
5. `PlayerStateStore` - 19 edges
6. `FoldersActivity` - 19 edges
7. `Track` - 18 edges
8. `PlaylistsViewModel` - 18 edges
9. `NowPlayingActivity` - 17 edges
10. `PlaylistsActivity` - 15 edges

## Surprising Connections (you probably didn't know these)
- `LibraryActivity` --references--> `FolderPreferencesRepository`  [EXTRACTED]
  app/src/main/java/com/genova/musicplayer/ui/LibraryActivity.kt → app/src/main/java/com/genova/musicplayer/data/FolderPreferencesRepository.kt
- `PlaylistDetailActivity` --references--> `FolderPreferencesRepository`  [EXTRACTED]
  app/src/main/java/com/genova/musicplayer/ui/PlaylistDetailActivity.kt → app/src/main/java/com/genova/musicplayer/data/FolderPreferencesRepository.kt
- `FoldersActivity` --references--> `MediaStoreTrackRepository`  [EXTRACTED]
  app/src/main/java/com/genova/musicplayer/ui/FoldersActivity.kt → app/src/main/java/com/genova/musicplayer/data/MediaStoreTrackRepository.kt
- `LibraryActivity` --references--> `MediaStoreTrackRepository`  [EXTRACTED]
  app/src/main/java/com/genova/musicplayer/ui/LibraryActivity.kt → app/src/main/java/com/genova/musicplayer/data/MediaStoreTrackRepository.kt
- `LibraryViewModel` --references--> `Track`  [EXTRACTED]
  app/src/main/java/com/genova/musicplayer/viewmodel/LibraryViewModel.kt → app/src/main/java/com/genova/musicplayer/domain/library/Track.kt

## Import Cycles
- None detected.

## Communities (84 total, 3 thin omitted)

### Community 0 - "Tasks: [FEATURE NAME]"
Cohesion: 0.07
Nodes (26): Dependencies & Execution Order, Format: `[ID] [P?] [Story] Description`, Implementation for User Story 1, Implementation for User Story 2, Implementation for User Story 3, Implementation Strategy, Incremental Delivery, MVP First (User Story 1 Only) (+18 more)

### Community 1 - "SKILL.md"
Cohesion: 0.08
Nodes (25): 1. Initialize Analysis Context, 2. Load Artifacts (Progressive Disclosure), 3. Build Semantic Models, 4. Detection Passes (Token-Efficient Analysis), 5. Severity Assignment, 6. Produce Compact Analysis Report, 7. Provide Next Actions, 8. Offer Remediation (+17 more)

### Community 2 - "SKILL.md"
Cohesion: 0.08
Nodes (25): 1. Initialize Analysis Context, 2. Load Artifacts (Progressive Disclosure), 3. Build Semantic Models, 4. Detection Passes (Token-Efficient Analysis), 5. Severity Assignment, 6. Produce Compact Analysis Report, 7. Provide Next Actions, 8. Offer Remediation (+17 more)

### Community 3 - "SKILL.md"
Cohesion: 0.08
Nodes (25): 1. Initialize Analysis Context, 2. Load Artifacts (Progressive Disclosure), 3. Build Semantic Models, 4. Detection Passes (Token-Efficient Analysis), 5. Severity Assignment, 6. Produce Compact Analysis Report, 7. Provide Next Actions, 8. Offer Remediation (+17 more)

### Community 4 - "Tasks: MVP Parte 1 — Biblioteca, Busca, Tocando Agora e Pastas"
Cohesion: 0.10
Nodes (20): 0. Branch, 1.1 Commit Group 1, 2.1 Commit Group 2, 3.1 Commit Group 3, 4.1 Commit Group 4, 5.1 Commit Group 5, Dependencies & Execution Order, Format: `[ID] [P?] [Story] Description` (+12 more)

### Community 5 - "Execution Steps"
Cohesion: 0.12
Nodes (15): 1. Initialize Convergence Context, 2. Load Artifacts (Progressive Disclosure), 3. Build the Intent Inventory, 4. Assess the Codebase and Classify Findings, 5. Assign Severity, 6. Present the In-Session Findings Summary, 7. Append Convergence Tasks (or report converged), 8. Provide Next Actions (Handoff) (+7 more)

### Community 6 - "Execution Steps"
Cohesion: 0.12
Nodes (15): 1. Initialize Convergence Context, 2. Load Artifacts (Progressive Disclosure), 3. Build the Intent Inventory, 4. Assess the Codebase and Classify Findings, 5. Assign Severity, 6. Present the In-Session Findings Summary, 7. Append Convergence Tasks (or report converged), 8. Provide Next Actions (Handoff) (+7 more)

### Community 7 - "Execution Steps"
Cohesion: 0.12
Nodes (15): 1. Initialize Convergence Context, 2. Load Artifacts (Progressive Disclosure), 3. Build the Intent Inventory, 4. Assess the Codebase and Classify Findings, 5. Assign Severity, 6. Present the In-Session Findings Summary, 7. Append Convergence Tasks (or report converged), 8. Provide Next Actions (Handoff) (+7 more)

### Community 8 - "common.ps1"
Cohesion: 0.23
Nodes (13): Find-SpecifyRoot(), Format-SpecKitCommand(), Get-CurrentBranch(), Get-FeaturePathsEnv(), Get-InvokeSeparator(), Get-NormalizedPriority(), Get-Python3Command(), Get-RepoRoot() (+5 more)

### Community 9 - "GenovaMusicPlayer — Instruções para Agentes de IA"
Cohesion: 0.12
Nodes (15): Comandos, Convenções, Fluxo de especificação, GenovaMusicPlayer — Instruções para Agentes de IA, Propósito, Regras de operação, Restrições, Stack (+7 more)

### Community 10 - "Core Principles"
Cohesion: 0.14
Nodes (13): Core Principles, GenovaMusicPlayer (Player de Música Android) Constitution, Governance, Histórico de Emendas, I. Plataforma e Zero Dependências Externas (NON-NEGOTIABLE), II. Arquitetura MVVM Pura sem Bibliotecas, III. Escopo Rígido do MVP, IV. Biblioteca de Músicas e Regra de Hierarquia de Pastas (+5 more)

### Community 11 - "1. Entidades de Domínio (Kotlin Puro — sem `android.*`)"
Cohesion: 0.14
Nodes (13): 1. Entidades de Domínio (Kotlin Puro — sem `android.*`), 2. Regras de Validação e Transições de Estado, 3. Persistência Local (Sem Bibliotecas Externas), `AuthorizedFolder`, Data Model: MVP Parte 1 — GenovaMusicPlayer, `FolderAdditionResult` (Sealed Class), Hierarquia de Pastas (`FolderHierarchyValidator`), `LibraryFilter` (Enum) (+5 more)

### Community 12 - "Feature Specification: [FEATURE NAME]"
Cohesion: 0.15
Nodes (12): Assumptions, Edge Cases, Feature Specification: [FEATURE NAME], Functional Requirements, Key Entities *(include if feature involves data)*, Measurable Outcomes, Requirements *(mandatory)*, Success Criteria *(mandatory)* (+4 more)

### Community 13 - "Feature Specification: MVP Parte 1 — Biblioteca, Busca, Tocando Agora e Pastas"
Cohesion: 0.15
Nodes (12): Assumptions, Edge Cases, Feature Specification: MVP Parte 1 — Biblioteca, Busca, Tocando Agora e Pastas, Functional Requirements, Key Entities, Measurable Outcomes, Requirements *(mandatory)*, Success Criteria *(mandatory)* (+4 more)

### Community 14 - "SKILL.md"
Cohesion: 0.18
Nodes (10): Completion Report, Done When, Key rules, Mandatory Post-Execution Hooks, Outline, Phase 0: Outline & Research, Phase 1: Design & Contracts, Phases (+2 more)

### Community 15 - "SKILL.md"
Cohesion: 0.18
Nodes (10): Completion Report, Done When, For AI Generation, Mandatory Post-Execution Hooks, Outline, Pre-Execution Checks, Quick Guidelines, Section Requirements (+2 more)

### Community 16 - "SKILL.md"
Cohesion: 0.18
Nodes (10): Checklist Format (REQUIRED), Completion Report, Done When, Mandatory Post-Execution Hooks, Outline, Phase Structure, Pre-Execution Checks, Task Generation Rules (+2 more)

### Community 17 - "SKILL.md"
Cohesion: 0.18
Nodes (10): Completion Report, Done When, Key rules, Mandatory Post-Execution Hooks, Outline, Phase 0: Outline & Research, Phase 1: Design & Contracts, Phases (+2 more)

### Community 18 - "SKILL.md"
Cohesion: 0.18
Nodes (10): Completion Report, Done When, For AI Generation, Mandatory Post-Execution Hooks, Outline, Pre-Execution Checks, Quick Guidelines, Section Requirements (+2 more)

### Community 19 - "SKILL.md"
Cohesion: 0.18
Nodes (10): Checklist Format (REQUIRED), Completion Report, Done When, Mandatory Post-Execution Hooks, Outline, Phase Structure, Pre-Execution Checks, Task Generation Rules (+2 more)

### Community 20 - "SKILL.md"
Cohesion: 0.18
Nodes (10): Completion Report, Done When, Key rules, Mandatory Post-Execution Hooks, Outline, Phase 0: Outline & Research, Phase 1: Design & Contracts, Phases (+2 more)

### Community 21 - "SKILL.md"
Cohesion: 0.18
Nodes (10): Completion Report, Done When, For AI Generation, Mandatory Post-Execution Hooks, Outline, Pre-Execution Checks, Quick Guidelines, Section Requirements (+2 more)

### Community 22 - "SKILL.md"
Cohesion: 0.18
Nodes (10): Checklist Format (REQUIRED), Completion Report, Done When, Mandatory Post-Execution Hooks, Outline, Phase Structure, Pre-Execution Checks, Task Generation Rules (+2 more)

### Community 23 - "Core Principles"
Cohesion: 0.18
Nodes (10): Core Principles, Governance, [PRINCIPLE_1_NAME], [PRINCIPLE_2_NAME], [PRINCIPLE_3_NAME], [PRINCIPLE_4_NAME], [PRINCIPLE_5_NAME], [PROJECT_NAME] Constitution (+2 more)

### Community 24 - "Implementation Plan: [FEATURE]"
Cohesion: 0.22
Nodes (8): Complexity Tracking, Constitution Check, Documentation (this feature), Implementation Plan: [FEATURE], Project Structure, Source Code (repository root), Summary, Technical Context

### Community 25 - "Implementation Plan: MVP Parte 1 — Biblioteca, Busca, Tocando Agora e Pastas"
Cohesion: 0.22
Nodes (8): Complexity Tracking, Constitution Check, Documentation (this feature), Implementation Plan: MVP Parte 1 — Biblioteca, Busca, Tocando Agora e Pastas, Project Structure, Source Code (repository root), Summary, Technical Context

### Community 26 - "Research & Technical Decisions: MVP Parte 1 — GenovaMusicPlayer"
Cohesion: 0.22
Nodes (8): 1. Lista Virtualizada sem AndroidX / RecyclerView, 2. Seleção de Pastas (`ACTION_OPEN_DOCUMENT_TREE`) e Filtro no `MediaStore`, 3. Regra de Hierarquia e Normalização em Kotlin Puro, 4. Carregamento Sob Demanda e Cache de Capas, 5. Máquina de Estados do `MediaPlayer`, `MediaSession` e Áudio em Segundo Plano, 6. Design System Nocturne Violet sem Bibliotecas UI, 7. Ambiente de Build e Testes (Docker Isolation), Research & Technical Decisions: MVP Parte 1 — GenovaMusicPlayer

### Community 27 - "SKILL.md"
Cohesion: 0.25
Nodes (7): Anti-Examples: What NOT To Do, Checklist Purpose: "Unit Tests for English", Example Checklist Types & Sample Items, Execution Steps, Post-Execution Checks, Pre-Execution Checks, User Input

### Community 28 - "SKILL.md"
Cohesion: 0.25
Nodes (7): Anti-Examples: What NOT To Do, Checklist Purpose: "Unit Tests for English", Example Checklist Types & Sample Items, Execution Steps, Post-Execution Checks, Pre-Execution Checks, User Input

### Community 29 - "SKILL.md"
Cohesion: 0.25
Nodes (7): Anti-Examples: What NOT To Do, Checklist Purpose: "Unit Tests for English", Example Checklist Types & Sample Items, Execution Steps, Post-Execution Checks, Pre-Execution Checks, User Input

### Community 30 - "create-new-feature.ps1"
Cohesion: 0.39
Nodes (4): ConvertTo-AsciiLower(), ConvertTo-CleanBranchName(), ConvertTo-UnicodeWords(), Get-BranchName()

### Community 31 - "2. Contratos de Tela (Alinhados a Q1: B, Q2: B, Q3: B)"
Cohesion: 0.25
Nodes (7): 1. Contrato Visual (Design System Nocturne Violet + Stitch), 2. Contratos de Tela (Alinhados a Q1: B, Q2: B, Q3: B), 3. Contratos Internos (MVVM Puro + Singleton), Tela 1: `LibraryActivity` / `LibraryFragment` (`biblioteca_busca`), Tela 2: `NowPlayingActivity` (`tocando_agora`), Tela 3: `FoldersActivity` (`gerenciador_de_pastas`), UI & Architecture Contracts: MVP Parte 1 — GenovaMusicPlayer

### Community 32 - "Quickstart & Validation Guide: MVP Parte 1 — GenovaMusicPlayer"
Cohesion: 0.25
Nodes (7): 1. Testes Unitários JVM (Kotlin Puro — Via Docker), 2. Build do APK Debug (Via Docker), 3. Benchmark Obrigatório (2.000+ Músicas), 4. Checklist de Validação Manual End-to-End, Cenários Obrigatórios Validados, Pré-requisitos, Quickstart & Validation Guide: MVP Parte 1 — GenovaMusicPlayer

### Community 33 - "SKILL.md"
Cohesion: 0.29
Nodes (6): Completion Report, Done When, Mandatory Post-Execution Hooks, Outline, Pre-Execution Checks, User Input

### Community 34 - "SKILL.md"
Cohesion: 0.29
Nodes (6): Completion Report, Done When, Mandatory Post-Execution Hooks, Outline, Pre-Execution Checks, User Input

### Community 35 - "SKILL.md"
Cohesion: 0.29
Nodes (6): Completion Report, Done When, Mandatory Post-Execution Hooks, Outline, Pre-Execution Checks, User Input

### Community 36 - "SKILL.md"
Cohesion: 0.29
Nodes (6): Completion Report, Done When, Mandatory Post-Execution Hooks, Outline, Pre-Execution Checks, User Input

### Community 37 - "SKILL.md"
Cohesion: 0.29
Nodes (6): Completion Report, Done When, Mandatory Post-Execution Hooks, Outline, Pre-Execution Checks, User Input

### Community 38 - "SKILL.md"
Cohesion: 0.29
Nodes (6): Completion Report, Done When, Mandatory Post-Execution Hooks, Outline, Pre-Execution Checks, User Input

### Community 39 - "SKILL.md"
Cohesion: 0.33
Nodes (5): Outline, Post-Execution Checks, Pre-Execution Checks, Scope Guard, User Input

### Community 40 - "SKILL.md"
Cohesion: 0.33
Nodes (5): Deprecation Notice, Outline, Post-Execution Checks, Pre-Execution Checks, User Input

### Community 41 - "SKILL.md"
Cohesion: 0.33
Nodes (5): Outline, Post-Execution Checks, Pre-Execution Checks, Scope Guard, User Input

### Community 42 - "SKILL.md"
Cohesion: 0.33
Nodes (5): Deprecation Notice, Outline, Post-Execution Checks, Pre-Execution Checks, User Input

### Community 43 - "SKILL.md"
Cohesion: 0.33
Nodes (5): Outline, Post-Execution Checks, Pre-Execution Checks, Scope Guard, User Input

### Community 44 - "SKILL.md"
Cohesion: 0.33
Nodes (5): Deprecation Notice, Outline, Post-Execution Checks, Pre-Execution Checks, User Input

### Community 45 - "Specification Quality Checklist: MVP Parte 1 — Biblioteca, Busca, Tocando Agora e Pastas"
Cohesion: 0.33
Nodes (5): Content Quality, Feature Readiness, Notes, Requirement Completeness, Specification Quality Checklist: MVP Parte 1 — Biblioteca, Busca, Tocando Agora e Pastas

### Community 46 - "[CHECKLIST TYPE] Checklist: [FEATURE NAME]"
Cohesion: 0.40
Nodes (4): [Category 1], [Category 2], [CHECKLIST TYPE] Checklist: [FEATURE NAME], Notes

### Community 47 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 55 - "LibraryActivity"
Cohesion: 0.06
Nodes (25): LibraryFilter, Activity, Boolean, Bundle, Button, ImageButton, Int, Intent (+17 more)

### Community 56 - "PlaybackService"
Cohesion: 0.11
Nodes (15): Boolean, Int, Intent, Long, String, PlaybackService, AudioFocusRequest, AudioManager (+7 more)

### Community 57 - "PlayerStateStore"
Cohesion: 0.08
Nodes (11): PlaybackSession, PlayerStatus, RepeatMode, Boolean, Int, List, Long, String (+3 more)

### Community 58 - "NowPlayingActivity"
Cohesion: 0.14
Nodes (11): Activity, Bundle, ImageButton, ImageView, Int, Long, String, TextView (+3 more)

### Community 59 - "FoldersViewModel"
Cohesion: 0.20
Nodes (11): Added, FolderFeedback, FoldersUiState, FoldersViewModel, List, Long, String, Unit (+3 more)

### Community 60 - "FoldersActivity"
Cohesion: 0.07
Nodes (24): FolderPreferencesRepository, List, Long, String, AuthorizedFolder, FolderListAdapter, BaseAdapter, Int (+16 more)

### Community 61 - "TrackListAdapter"
Cohesion: 0.05
Nodes (30): List, MediaStoreTrackRepository, deriveHiFi(), Boolean, Int, String, Track, List (+22 more)

### Community 62 - ".addFolder"
Cohesion: 0.22
Nodes (10): Added, FolderAdditionResult, RejectedCoveredByParent, RejectedDuplicate, RejectedInvalidPath, FolderHierarchyValidator, Boolean, List (+2 more)

### Community 63 - "FolderListAdapter"
Cohesion: 0.17
Nodes (11): Assumptions, Edge Cases, Feature Specification: Infraestrutura E2E Appium, Logo do APK e Diretriz GitHub MCP, Functional Requirements, Measurable Outcomes, Requirements *(mandatory)*, Success Criteria *(mandatory)*, User Scenarios & Testing *(mandatory)* (+3 more)

### Community 65 - ".decodeSampledArt"
Cohesion: 0.33
Nodes (6): AlbumArtCacheLoader, ImageView, Int, String, Bitmap, Context

### Community 66 - "FolderPathNormalizer"
Cohesion: 0.39
Nodes (4): FolderPathNormalizer, Boolean, List, String

### Community 67 - "FolderPreferencesRepository"
Cohesion: 0.33
Nodes (5): 0. Branch, Phase 1: Logo Oficial do APK (US1), Phase 2: Infraestrutura E2E Appium em Docker (US2), Phase 3: Documentação Operacional (US3) & Validação, Tasks: Infraestrutura E2E Appium, Logo do APK e Diretriz GitHub MCP

### Community 68 - "AuthorizedFolder"
Cohesion: 0.40
Nodes (4): Content Quality, Feature Readiness, Requirement Completeness, Specification Quality Checklist: Infraestrutura E2E Appium, Logo do APK e Diretriz GitHub MCP

### Community 69 - ".render"
Cohesion: 0.67
Nodes (3): _build_options(), driver(), UiAutomator2Options

### Community 71 - "Playlist"
Cohesion: 0.19
Nodes (8): BaseAdapter, Int, List, Long, View, ViewGroup, PlaylistListAdapter, ViewHolder

### Community 72 - "PlaylistsViewModel"
Cohesion: 0.05
Nodes (32): List, PlaylistPreferencesRepository, Playlist, List, Long, String, PlaylistManager, DuplicateName (+24 more)

### Community 73 - "Tasks: MVP Parte 2 — Playlists e Refinamentos"
Cohesion: 0.13
Nodes (14): 0. Branch, 3.1 Commit Group 1, 5.1 Commit Group 2, Dependencies & Execution Order, Format: `[ID] [P?] [Story] Description`, Implementation for User Story 2, Phase 1: Foundational (Textos & Navegação Compartilhada), Phase 2: User Story 1 - Executar Música ao Clicar na Lista (Priority: P1) 🎯 MVP (+6 more)

### Community 74 - "User Scenarios & Testing *(mandatory)*"
Cohesion: 0.14
Nodes (13): Assumptions, Edge Cases, Feature Specification: MVP Parte 2 — Playlists e Refinamentos de Navegação e Pastas, Functional Requirements, Key Entities, Measurable Outcomes, Requirements *(mandatory)*, Success Criteria *(mandatory)* (+5 more)

### Community 75 - "Implementation Plan: MVP Parte 2 — Playlists e Refinamentos"
Cohesion: 0.22
Nodes (8): Complexity Tracking, Constitution Check, Documentation (this feature), Implementation Plan: MVP Parte 2 — Playlists e Refinamentos, Project Structure, Source Code (repository root), Summary, Technical Context

### Community 76 - "Data Model: MVP Parte 2 — Playlists e Refinamentos"
Cohesion: 0.29
Nodes (6): 1. Entidades de Domínio (Kotlin Puro — sem `android.*`), 2. Regras de Negócio (`PlaylistManager`), 3. Persistência Local, Data Model: MVP Parte 2 — Playlists e Refinamentos, `Playlist`, `PlaylistValidationResult` (Sealed Class)

### Community 77 - "Research & Technical Decisions: MVP Parte 2 — Playlists e Refinamentos"
Cohesion: 0.29
Nodes (6): 1. Domínio de Playlists em Kotlin Puro, 2. Persistência Local de Playlists sem Bibliotecas Externas, 3. Navegação Inferior e Pontos de Entrada de Playlists, 4. Saída de Áudio Ativa sem Troca Manual, 5. Ocultação da Regra de Hierarquia na Tela de Pastas, Research & Technical Decisions: MVP Parte 2 — Playlists e Refinamentos

### Community 78 - "Specification Quality Checklist: MVP Parte 2 — Playlists e Refinamentos"
Cohesion: 0.33
Nodes (5): Content Quality, Feature Readiness, Notes, Requirement Completeness, Specification Quality Checklist: MVP Parte 2 — Playlists e Refinamentos

### Community 79 - "Quickstart & Validation Guide: MVP Parte 2 — Playlists e Refinamentos"
Cohesion: 0.33
Nodes (5): 1. Testes Unitários JVM (Via Docker), 2. Build APK Debug (Via Docker), 3. Validação Manual no Dispositivo, Cenários Obrigatórios Validados (`PlaylistManagerTest`), Quickstart & Validation Guide: MVP Parte 2 — Playlists e Refinamentos

### Community 80 - "UI & Architecture Contracts: MVP Parte 2 — Playlists e Refinamentos"
Cohesion: 0.40
Nodes (4): 1. Refinamentos nas Telas Existentes, 2. Novas Telas e Contratos de Playlists, 3. Contratos Internos, UI & Architecture Contracts: MVP Parte 2 — Playlists e Refinamentos

### Community 81 - "Implementation Plan: Infraestrutura E2E Appium, Logo do APK e Diretriz GitHub MCP"
Cohesion: 0.50
Nodes (3): Constitution Check, Implementation Plan: Infraestrutura E2E Appium, Logo do APK e Diretriz GitHub MCP, Summary

### Community 82 - "Research & Technical Decisions: 003-e2e-appium-and-apk-logo"
Cohesion: 0.50
Nodes (3): 1. Stack E2E Isolada em `docker-compose.e2e.yml`, 2. Geração de Ícones do APK (`mipmap-*`), Research & Technical Decisions: 003-e2e-appium-and-apk-logo

## Knowledge Gaps
- **461 isolated node(s):** `PlayerStatus`, `User Input`, `Pre-Execution Checks`, `Goal`, `Operating Constraints` (+456 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **3 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Track` connect `TrackListAdapter` to `PlaybackService`, `PlayerStateStore`, `FoldersViewModel`, `LibraryActivity`?**
  _High betweenness centrality (0.072) - this node is a cross-community bridge._
- **Why does `PlaylistDetailActivity` connect `TrackListAdapter` to `PlaylistsViewModel`, `FoldersActivity`?**
  _High betweenness centrality (0.042) - this node is a cross-community bridge._
- **Why does `PlaylistsViewModel` connect `PlaylistsViewModel` to `TrackListAdapter`, `LibraryActivity`?**
  _High betweenness centrality (0.029) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `Playlist` (e.g. with `.`addTrackToPlaylist avoids duplicates and removeTrackFromPlaylist preserves order`()` and `.`createPlaylist rejects duplicate name ignoring case and whitespace`()`) actually correct?**
  _`Playlist` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `PlayerStatus`, `User Input`, `Pre-Execution Checks` to the rest of the system?**
  _461 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Tasks: [FEATURE NAME]` be split into smaller, more focused modules?**
  _Cohesion score 0.07407407407407407 - nodes in this community are weakly interconnected._
- **Should `SKILL.md` be split into smaller, more focused modules?**
  _Cohesion score 0.07692307692307693 - nodes in this community are weakly interconnected._