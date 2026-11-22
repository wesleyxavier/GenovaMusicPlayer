# UI & Architecture Contracts: MVP Parte 1 — GenovaMusicPlayer

**Feature**: `001-mvp-part1-player` | **Date**: 2026-11-21

## 1. Contrato Visual (Design System Nocturne Violet + Stitch)

Baseado em `nocturne_violet/DESIGN.md` e nas telas HTML de referência:

- **Paleta Principal:**
  - `surface` / `background`: `#13121B`
  - `surface_container_lowest`: `#0E0D16`
  - `surface_container_low`: `#1C1A24`
  - `surface_container`: `#201E28`
  - `surface_container_high`: `#2A2933`
  - `surface_container_highest`: `#35343E`
  - `primary`: `#CEBDFF`
  - `primary_container`: `#9D7BFF`
  - `on_primary`: `#390094`
  - `on_primary_container`: `#320085`
  - `on_surface`: `#E5E0EE`
  - `on_surface_variant`: `#CBC3D5`
  - `outline`: `#948E9F`
  - `error`: `#FFB4AB`
- **Tipografia:** `Plus Jakarta Sans` (`res/font/`), sem uso de Material Components.
- **Ergonomia e Acessibilidade:**
  - Todo controle interativo possui área de toque mínima de `48dp x 48dp`.
  - Todo controle interativo possui `android:contentDescription` em português do Brasil.
  - Botão Play/Pause principal em `Tocando Agora`: `64dp x 64dp`, posicionado na metade inferior da tela.

## 2. Contratos de Tela (Alinhados a Q1: B, Q2: B, Q3: B)

### Tela 1: `LibraryActivity` / `LibraryFragment` (`biblioteca_busca`)
- **Barra Superior:** Logo + Título `"Músicas"` + ícone de Lupa (`48dp`, foca campo de busca). Ícone de Perfil removido (`Q3: B`).
- **Campo de Busca:** `EditText` altura `52dp`, placeholder `"Pesquisar músicas, artistas..."`, botão limpar (`X`) visível quando há texto; filtro em tempo real.
- **Chips de Filtro (`Q2: B`):** Apenas `"Todas"` e `"Recentes"` (altura `36dp–38dp`).
- **Cabeçalho de Estatísticas + CTA:** `"{N} músicas"` / `"Memória interna • Áudio HD"` + botão pill `"Aleatório"`.
- **Lista Virtualizada (`ListView`):** Linhas de `68dp` com miniatura `48dp` (canto `8dp`), indicador animado na faixa em reprodução, título, artista, duração e botão `⋮` (`48dp`).
- **Menu de Contexto `⋮` (`Q2: B`):** Apenas `"Ver detalhes do arquivo"` (exibe modal simples com formato, taxa de amostragem, tamanho e caminho). Sem `"Reproduzir a seguir"` (`Q1: B`).
- **Navegação Inferior:** Abas `"Músicas"` (ativa) e `"Pastas"`. Sem mini-player flutuante (`Q1: B`).

### Tela 2: `NowPlayingActivity` (`tocando_agora`)
- **Barra Superior:** Botão Voltar (`←`, `48dp`), título `"Reproduzindo Agora"`, botão `⋮` (`48dp`, abre detalhes técnicos de áudio e caminho). Ícone de Perfil removido (`Q3: B`).
- **Área Central:** Capa do álbum (`max 320dp`, `1:1`, raio `16dp`) com brilho lavanda sutil atrás.
- **Metadados e Ações:** Badge `"Hi-Fi Sem Perdas"` (quando aplicável), título, artistas e botão `"Compartilhar"` (`48dp`, `Intent.ACTION_SEND` com URI da faixa). Sem botão Favoritar (`Q2: B`).
- **Seekbar e Tempos:** `SeekBar` interativa (altura de toque `>= 24dp–48dp`) + `02:14` / `03:48`.
- **Controles Principais (Metade Inferior):** Shuffle (`48dp` + ponto indicador), Anterior (`48dp`), Play/Pause (`64dp`), Próxima (`48dp`), Repetir (`48dp` + ponto indicador).
- **Cartão de Saída de Áudio:** Mostra rota atual e botão `"Trocar"` (abre seletor de saída de mídia / configurações Bluetooth do sistema).
- **Rodapé (`Q1: B`):** Botões `"Fila a Seguir"` e `"Ver Letra"` exibidos como placeholders visuais inativos com indicação `"Em breve"` (sem lógica de fila ou leitura de letras).

### Tela 3: `FoldersActivity` (`gerenciador_de_pastas`)
- **Barra Superior:** Logo + Título `"Pastas"` + ícone de Lupa (leva para a aba `"Músicas"` com foco na busca). Ícone de Perfil removido (`Q3: B`).
- **Cabeçalho + CTA:** `"Diretórios Locais"`, badge `"Sincronizado"`, descrição do escopo e botão `"+ Adicionar Pasta (Seletor do Sistema)"` (`56dp` de altura) invocando `ACTION_OPEN_DOCUMENT_TREE`.
- **Card Regra de Hierarquia:** Explicação permanente de que pastas pai englobam subpastas e subpastas redundantes são bloqueadas.
- **Cards de Diagnóstico:** `"Status da Varredura"` (`"há X minutos"`) e `"Uso Total de Áudio"` (`"X GB / N faixas indexadas"`).
- **Lista de Pastas:** Ícone, nome, badge `"{N} músicas"`, caminho normalizado, data de inclusão e botão Lixeira (`48dp`, com diálogo de confirmação antes de remover).
- **Navegação Inferior:** Abas `"Músicas"` e `"Pastas"` (ativa).

## 3. Contratos Internos (MVVM Puro + Singleton)

- **`PlayerStateStore` (Singleton):**
  - `fun getState(): PlaybackSession`
  - `fun addListener(listener: (PlaybackSession) -> Unit)`
  - `fun removeListener(listener: (PlaybackSession) -> Unit)`
- **`PlaybackService` (`Service` em primeiro plano):**
  - Actions via `Intent`: `ACTION_PLAY`, `ACTION_PAUSE`, `ACTION_RESUME`, `ACTION_NEXT`, `ACTION_PREVIOUS`, `ACTION_SEEK`, `ACTION_TOGGLE_SHUFFLE`, `ACTION_CYCLE_REPEAT`, `ACTION_STOP`.
- **`LibraryViewModel` & `FoldersViewModel` (Classes Kotlin puras sem AndroidX):**
  - Expõem callbacks `onStateChanged: ((UiState) -> Unit)?` e métodos síncronos/assíncronos de intenção do usuário (`onSearchQueryChanged`, `onFilterSelected`, `onFolderPicked`, `onRemoveFolderConfirmed`, `onShuffleAllClicked`, `onTrackClicked`).
