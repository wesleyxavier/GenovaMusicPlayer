# Quickstart & Validation Guide: MVP Parte 3

**Feature**: `004-part3-queue-miniplayer-playlists` | **Date**: 2026-11-23

## 1. Pré-requisitos e Build via Docker

1. Construir a imagem Docker (caso ainda não construída):
   ```powershell
   docker build -t genova-music-player .
   ```
2. Executar testes unitários (incluindo `PlaybackQueueManagerTest`, `PlaylistManagerTest` e `SmartPlaylistEngineTest`):
   ```powershell
   docker run --rm -v "${PWD}:/workspace" -w /workspace genova-music-player ./gradlew testDebugUnitTest --no-daemon
   ```
3. Gerar APK de debug:
   ```powershell
   docker run --rm -v "${PWD}:/workspace" -w /workspace genova-music-player ./gradlew assembleDebug --no-daemon
   ```

## 2. Cenários de Validação Manual / E2E

1. **Botão `"Tocando Agora"` e Mini-Player na Tela Principal:**
   - Abrir a aba `Músicas` sem reprodução ativa -> mini-player oculto; tocar em `"Tocando Agora"` exibe toast informativo.
   - Tocar em uma música na lista -> inicia reprodução imediatamente; voltar para `Músicas` -> mini-player aparece acima da barra inferior com capa, título, artista, progresso, Play/Pause e Próxima; tocar em `"Tocando Agora"` ou no corpo do mini-player abre `NowPlayingActivity`.
   - Navegar para `Playlists` e `Pastas` -> mini-player permanece visível e sincronizado.
2. **Subtela `"Fila a Seguir"`:**
   - Em `NowPlayingActivity`, tocar em `"Fila a Seguir"` -> abre `QueueActivity`.
   - Tocar em `"+ Adicionar"` -> adicionar músicas e uma playlist inteira à fila; validar contadores atualizados.
   - Reordenar um item e remover outro item -> validar atualização imediata.
   - Clicar em um item da fila -> reprodução muda imediatamente para a música clicada.
   - Testar `"Salvar Playlist"` e `"Limpar"` fila.
3. **Evolução de Playlists:**
   - Na aba `Playlists`, testar busca, ordenação, alternância Grade/Lista e modal `"Nova Playlist"` (contador 60 caracteres + `"Fixar no topo"`).
   - No menu `⋮` de uma faixa na biblioteca, testar `"Adicionar à playlist"` marcando múltiplas playlists e confirmando em `"Concluir"`.
   - Em `PlaylistDetailActivity`, testar filtro interno, `"+ Adicionar músicas"`, `"Ordem manual"` (reordenação persistida ao reabrir) e `"Adicionar à fila"`.
   - Validar `"Playlists Inteligentes do Sistema"` (`Mais Tocadas (Top 50)` e `Adicionadas Recentemente`).
4. **Testes E2E Appium (via subagente `appium-test-hydrogen`):**
   ```powershell
   docker compose -f docker-compose.e2e.yml up --build --abort-on-container-exit
   ```
