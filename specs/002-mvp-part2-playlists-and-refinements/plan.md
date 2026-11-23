# Implementation Plan: MVP Parte 2 — Playlists e Refinamentos

**Branch**: `002-mvp-part2-playlists-and-refinements` | **Date**: 2026-11-22 | **Spec**: [spec.md](file:///c:/Users/User/Documents/TI/Lavi/Web/Genova/src/PROJETOS/GenovaMusicPlayer/specs/002-mvp-part2-playlists-and-refinements/spec.md)

**Input**: Feature specification from `specs/002-mvp-part2-playlists-and-refinements/spec.md` + Constituição `v1.0.1`.

## Summary

Implementar o gerenciamento completo de playlists locais da Parte 2 do MVP (criar, renomear, excluir, adicionar e remover músicas) com nova aba inferior `"Playlists"`, somado aos três refinamentos solicitados: reprodução imediata ao clicar em qualquer música, remoção da opção de troca manual de saída de áudio em `"Tocando Agora"` (usando a rota ativa do sistema) e ocultação do card permanente da regra de hierarquia na tela `"Pastas"` (exibindo aviso apenas em tentativa inválida). Tudo em Kotlin puro e Views nativas sem dependências externas, seguindo **Nocturne Violet** e **Docker Isolation**.

## Technical Context

**Language/Version**: Kotlin (stdLib apenas), Java/JVM target 17, Android Gradle Plugin `8.4.2`
**Primary Dependencies**: Nenhuma biblioteca de terceiros (apenas Android SDK framework `android.*` + Kotlin stdlib)
**Storage**: `SharedPreferences` + `org.json.JSONArray`/`JSONObject` para playlists locais; `MediaStore` existente para resolver metadados das faixas
**Testing**: Testes unitários JVM (`kotlin("test")`) para `PlaylistManager` e regras puras, executados em Docker
**Target Platform**: Android API 26+ até API 34 (`compileSdk 34`, `android.useAndroidX=false`)
**Project Type**: mobile-app (Android nativo)
**Performance Goals**: Reprodução imediata ao toque (`< 1s` local), listas virtualizadas com `ListView` + `ViewHolder`
**Constraints**: Offline-only, zero dependências externas, UI e erros em PT-BR, alvos `>= 48dp`, TalkBack `contentDescription`, builds/testes exclusivamente em Docker
**Scale/Scope**: 2 novas telas/fluxos (`PlaylistsActivity`, `PlaylistDetailActivity`), atualização da barra inferior (3 abas) e ajustes pontuais em `LibraryActivity`, `NowPlayingActivity` e `FoldersActivity`

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Princípio I (Zero Dependências Externas):** Apenas SDK Android e Kotlin stdlib (`org.json` nativo para persistência de playlists).
- [x] **Princípio II (MVVM Puro):** `PlaylistManager` e entidades em Kotlin puro (`domain/playlist/`); `PlaylistsViewModel` em Kotlin comum com callbacks.
- [x] **Princípio III (Escopo do MVP — Parte 2):** Cobre exatamente playlists (criar, renomear, excluir, adicionar e remover músicas), sem adicionar itens fora do MVP.
- [x] **Princípio IV (Hierarquia de Pastas — v1.0.1):** Regra oculta na tela por padrão, exibindo aviso em PT-BR apenas em tentativa inválida.
- [x] **Princípio V (Reprodução e Áudio):** Remoção do botão `"Trocar"` mantém o uso da rota ativa, foco de áudio e pausa ao desconectar fones.
- [x] **Princípio VI & VII (UI Nativa e Simplicidade):** Views XML **Nocturne Violet**, alvos `>= 48dp`, PT-BR, TalkBack, sem abstrações desnecessárias.
- [x] **Docker Isolation:** Todos os comandos Gradle rodam no container Docker (`Dockerfile`).

*Reavaliação pós-Phase 1:* Total aderência à Constituição `v1.0.1` sem exceções ou violações.

## Project Structure

### Documentation (this feature)

```text
specs/002-mvp-part2-playlists-and-refinements/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── checklists/
│   └── requirements.md
├── contracts/
│   └── playlists-and-refinements-contracts.md
└── tasks.md             # (/speckit-tasks)
```

### Source Code (repository root)

```text
app/src/
├── main/
│   ├── res/layout/
│   │   ├── activity_playlists.xml
│   │   ├── activity_playlist_detail.xml
│   │   ├── item_playlist.xml
│   │   ├── activity_library.xml         # (atualizar bottom tabs + menu)
│   │   ├── activity_now_playing.xml     # (remover cartão/botão Trocar saída de áudio)
│   │   └── activity_folders.xml         # (remover card permanente Regra de Hierarquia + bottom tabs)
│   └── java/com/genova/musicplayer/
│       ├── domain/playlist/
│       │   ├── Playlist.kt
│       │   ├── PlaylistValidationResult.kt
│       │   └── PlaylistManager.kt
│       ├── data/
│       │   └── PlaylistPreferencesRepository.kt
│       ├── viewmodel/
│       │   └── PlaylistsViewModel.kt
│       └── ui/
│           ├── PlaylistsActivity.kt
│           ├── PlaylistListAdapter.kt
│           └── PlaylistDetailActivity.kt
└── test/java/com/genova/musicplayer/
    └── domain/playlist/
        └── PlaylistManagerTest.kt
```

**Structure Decision**: Extensão direta da estrutura modular definida em `001-mvp-part1-player`, mantendo `domain/playlist/` 100% independente de `android.*`.

## Complexity Tracking

Nenhuma violação à Constituição identificada.
