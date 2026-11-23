# Quickstart & Validation Guide: MVP Parte 2 — Playlists e Refinamentos

**Feature**: `002-mvp-part2-playlists-and-refinements` | **Date**: 2026-11-22

## 1. Testes Unitários JVM (Via Docker)

```powershell
docker run --rm -v "${PWD}:/workspace" -w /workspace genova-music-player ./gradlew testDebugUnitTest --no-daemon
```

### Cenários Obrigatórios Validados (`PlaylistManagerTest`)
- Criar playlist com nome válido (com `trim()`).
- Recusar nome vazio ou apenas espaços (`EmptyName`).
- Recusar nome duplicado ignorando maiúsculas/minúsculas (`DuplicateName`).
- Renomear playlist validando duplicidade e nome vazio.
- Excluir playlist mantendo as demais intactas.
- Adicionar faixa sem duplicar caso já exista na playlist e remover faixa preservando ordem.

## 2. Build APK Debug (Via Docker)

```powershell
docker run --rm -v "${PWD}:/workspace" -w /workspace genova-music-player ./gradlew assembleDebug --no-daemon
```

## 3. Validação Manual no Dispositivo

- [ ] Tocar em qualquer música na biblioteca ou playlist inicia a reprodução imediatamente.
- [ ] Tela `"Tocando Agora"` não exibe opção `"Trocar"` de dispositivo de áudio e toca normalmente na saída ativa (alto-falante ou fone).
- [ ] Tela `"Pastas"` não exibe card permanente da regra de hierarquia; ao tentar adicionar subpasta de `/Music`, exibe aviso claro em português.
- [ ] Aba `"Playlists"` permite criar, renomear, excluir (com confirmação), adicionar músicas via `⋮` na biblioteca e remover músicas da playlist, persistindo após reiniciar o app.
