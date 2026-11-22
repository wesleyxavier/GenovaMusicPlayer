# Quickstart & Validation Guide: MVP Parte 1 — GenovaMusicPlayer

**Feature**: `001-mvp-part1-player` | **Date**: 2026-11-21

## Pré-requisitos

- Docker Desktop ativo (regra global **Docker Isolation**: zero JDK/Android SDK instalado no host).
- Dispositivo Android físico ou emulador (API 26+) com músicas locais em pelo menos duas pastas (ex.: `/Music` e `/Download/Audio`) para validação manual e benchmark de 2.000 músicas.

## 1. Testes Unitários JVM (Kotlin Puro — Via Docker)

Executar a suíte de testes unitários (sem dependência de Android) dentro do container Gradle/Android:

```powershell
docker run --rm -v "${PWD}:/workspace" -w /workspace <imagem-android-sdk> ./gradlew testDebugUnitTest
```

### Cenários Obrigatórios Validados
1. **Hierarquia de pastas (`FolderHierarchyValidatorTest`):**
   - Pasta igual (`/Music` + `/Music`) → recusada.
   - Pasta filha (`/Music` existente + `/Music/Rock`) → recusada com mensagem em PT-BR.
   - Pasta mãe (`/Music/Rock` existente + `/Music`) → aceita e remove `/Music/Rock`.
   - Pasta irmã (`/Music` + `/Podcasts`) → ambas mantidas.
   - Prefixo de texto (`/Music` + `/Music2`) → ambas mantidas.
   - Barra final (`/Music/` vs `/Music`) → normalizadas como iguais.
   - Caminho vazio (`""` ou `"   "`) → recusado.
2. **Busca, filtros e máquina de estados do player:**
   - Filtro em tempo real por título/artista e ordenação `"Recentes"`.
   - Transições explícitas de `PlayerStatus`, `RepeatMode` e `Shuffle`.

## 2. Build do APK Debug (Via Docker)

```powershell
docker run --rm -v "${PWD}:/workspace" -w /workspace <imagem-android-sdk> ./gradlew assembleDebug
```

## 3. Benchmark Obrigatório (2.000+ Músicas)

1. Popular uma pasta autorizada com pelo menos **2.000 arquivos de áudio**.
2. Instalar o APK gerado (`app/build/outputs/apk/debug/app-debug.apk`) no dispositivo de teste.
3. Selecionar a pasta via `"+ Adicionar Pasta (Seletor do Sistema)"`.
4. Medir o tempo de abertura da tela `"Músicas"` até a renderização inicial dos itens visíveis e validar rolagem sem travamentos na main thread (StrictMode / log de tempo de carregamento).

## 4. Checklist de Validação Manual End-to-End

- [ ] Selecionar pasta no seletor do sistema, fechar e reabrir o app: lista persiste.
- [ ] Tentar adicionar subpasta e pasta mãe: comportamento conforme Regra de Hierarquia.
- [ ] Iniciar música ou `"Aleatório"`, bloquear a tela e testar Play/Pause, Próxima e Anterior pela notificação.
- [ ] Desconectar fones de ouvido durante a reprodução: música pausa automaticamente.
- [ ] Testar navegação completa com TalkBack ativado verificando `contentDescription` em todos os botões.
