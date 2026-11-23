# Research & Technical Decisions: MVP Parte 2 — Playlists e Refinamentos

**Feature**: `002-mvp-part2-playlists-and-refinements` | **Date**: 2026-11-22

## 1. Domínio de Playlists em Kotlin Puro

- **Decision**: Implementar `Playlist`, `PlaylistValidationResult` e `PlaylistManager` no pacote `domain/playlist/` em Kotlin puro (sem `android.*`), centralizando criação, renomeação, exclusão, adição (evitando duplicata da mesma faixa por `trackId`) e remoção de faixas.
- **Rationale**: Atende diretamente ao Princípio II e Princípio VII da Constituição, permitindo testes unitários JVM rápidos e determinísticos.
- **Alternatives considered**: Validar apenas na `Activity` ou `SharedPreferences` direto (rejeitado por acoplar regra de negócio à camada Android).

## 2. Persistência Local de Playlists sem Bibliotecas Externas

- **Decision**: Persistir playlists em `SharedPreferences` (`"genova_playlists_prefs"`) usando `org.json.JSONArray` e `org.json.JSONObject` (presentes nativamente no Android SDK), armazenando `id`, `name`, `createdAtEpochMs` e lista ordenada de `trackIds` (`Long`), resolvendo os metadados completos contra as faixas autorizadas do `MediaStore` ao carregar.
- **Rationale**: Zero dependências externas (sem Room, SQLite helper complexo ou Gson), leitura rápida e resiliente caso uma faixa seja removida do aparelho (faixas ausentes no `MediaStore` são ignoradas graciosamente).
- **Alternatives considered**: `SQLiteOpenHelper` manual (mais verboso sem ganho concreto no MVP para listas locais de playlists, contrariando Princípio VII — Simplicidade).

## 3. Navegação Inferior e Pontos de Entrada de Playlists

- **Decision**: Expandir a barra de navegação inferior nas telas principais para 3 abas (`"Músicas"`, `"Playlists"`, `"Pastas"`), manter `PlaylistsActivity` com lista de playlists + CTA `"+ Criar playlist"` e `PlaylistDetailActivity` para listar/reproduzir/remover faixas da playlist selecionada, além da ação `"Adicionar à playlist"` no menu `⋮` de cada faixa na biblioteca.
- **Rationale**: Mantém simetria visual com o design **Nocturne Violet**, alvos `>= 48dp` e acesso direto sem menus escondidos.
- **Alternatives considered**: Exibir playlists apenas em diálogo dentro de `"Músicas"` (dificulta visualização e gerenciamento completo do CRUD da Parte 2).

## 4. Saída de Áudio Ativa sem Troca Manual

- **Decision**: Remover o cartão `"Dispositivo de Áudio"` / botão `"Trocar"` de `activity_now_playing.xml` e `NowPlayingActivity.kt`. O `MediaPlayer` e o `AudioManager` continuam reproduzindo pela rota ativa do sistema operacional e pausando em `ACTION_AUDIO_BECOMING_NOISY` / perda de foco.
- **Rationale**: Reduz ruído visual em `"Tocando Agora"` e atende exatamente ao pedido de usar o dispositivo ativo sem troca manual.
- **Alternatives considered**: Manter texto informativo estático do dispositivo sem botão (desnecessário frente à remoção solicitada).

## 5. Ocultação da Regra de Hierarquia na Tela de Pastas

- **Decision**: Remover o card informativo permanente `"Regra de Hierarquia"` de `activity_folders.xml`. A validação em `FolderHierarchyValidator` permanece idêntica e, ao tentar adicionar uma pasta descendente de outra já incluída, `FoldersActivity` exibe um aviso claro em português (`AlertDialog` ou banner/mensagem de erro).
- **Rationale**: Alinha a interface à emenda constitucional `v1.0.1`.
- **Alternatives considered**: Exibir texto colapsável ("Saiba mais") na tela (rejeitado por manter referência permanente na tela).
