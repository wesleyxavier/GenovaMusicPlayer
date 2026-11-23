# Feature Specification: MVP Parte 2 — Playlists e Refinamentos de Navegação e Pastas

**Feature Branch**: `002-mvp-part2-playlists-and-refinements`

**Created**: 2026-11-22

**Status**: Draft

**Input**: User description: "a proxima fase constitui na melhoria: permitir ao clicar em uma musica ela ser executada; remover a opção de troca de audio, ele deve usar o que esta ativo; criar playlist; excluir informação de regra de hierarquia da tela, isto deve estar escondido, no máximo dar um aviso quando alguém tentar fazer."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Executar Música ao Clicar na Lista (Priority: P1)

Como ouvinte, quero tocar diretamente em qualquer música exibida na biblioteca ou em uma playlist para que sua reprodução inicie imediatamente, sem etapas intermediárias.

**Why this priority**: É a interação mais básica e esperada de um player de música.

**Independent Test**: Pode ser testada tocando em uma faixa na lista de músicas ou dentro de uma playlist e verificando que a reprodução inicia imediatamente e a faixa passa a ser indicada como ativa.

**Acceptance Scenarios**:

1. **Given** que o usuário visualiza uma lista de músicas (biblioteca ou playlist), **When** toca em uma faixa, **Then** a reprodução da faixa selecionada inicia imediatamente usando a lista atual como contexto da sessão.
2. **Given** que uma música já está tocando, **When** o usuário toca em outra música da lista, **Then** a nova faixa substitui a anterior e inicia a reprodução imediatamente.

---

### User Story 2 - Criar e Gerenciar Playlists Locais (Priority: P1)

Como ouvinte, quero criar, renomear e excluir playlists personalizadas, além de adicionar e remover músicas e reproduzi-las localmente, para organizar minhas faixas por momento ou preferência sem depender de internet.

**Why this priority**: É a entrega central da Parte 2 do MVP definida na Constituição.

**Independent Test**: Pode ser testada acessando a aba inferior "Playlists", criando uma playlist com nome válido, renomeando, adicionando faixas pelo menu `⋮` da biblioteca, removendo uma faixa, excluindo uma playlist e reproduzindo uma música ao clicar dentro da playlist.

**Acceptance Scenarios**:

1. **Given** que o usuário acessa a aba inferior "Playlists", **When** aciona "Criar playlist" e informa um nome válido não duplicado, **Then** a playlist é criada e persistida localmente, aparecendo na lista de playlists com contagem `0 músicas`.
2. **Given** que o usuário tenta criar ou renomear uma playlist com nome vazio ou já existente, **When** confirma a ação, **Then** o sistema recusa com mensagem clara em português.
3. **Given** que o usuário visualiza uma faixa na biblioteca e abre o menu `⋮`, **When** seleciona "Adicionar à playlist", **Then** pode escolher uma playlist existente (ou criar uma nova) e a faixa é incluída sem duplicação indesejada.
4. **Given** que o usuário abre uma playlist existente, **When** aciona renomear, remover uma música da playlist ou excluir a playlist (com confirmação), **Then** as alterações são persistidas localmente e refletidas na interface imediatamente.

---

### User Story 3 - Simplificar Tela "Tocando Agora" Usando Saída de Áudio Ativa (Priority: P2)

Como ouvinte, quero que o player utilize automaticamente a saída de áudio ativa do aparelho (alto-falante ou fone conectado) sem exibir botão ou cartão para troca manual de dispositivo, mantendo a tela "Tocando Agora" mais limpa.

**Why this priority**: Reduz ruído visual na tela de reprodução e elimina fluxo manual desnecessário.

**Independent Test**: Pode ser testada abrindo a tela "Tocando Agora" e verificando que não há opção/botão de troca de saída de áudio e que o som sai normalmente pelo dispositivo ativo no sistema.

**Acceptance Scenarios**:

1. **Given** que uma música está em reprodução, **When** o usuário abre a tela "Tocando Agora", **Then** nenhuma opção ou botão para trocar dispositivo de áudio é exibido na interface.
2. **Given** que o usuário alterna fones Bluetooth ou cabo no sistema operacional, **When** retoma a reprodução, **Then** o player utiliza automaticamente a saída de áudio ativa no sistema (mantendo a pausa automática ao desconectar fones).

---

### User Story 4 - Ocultar Regra de Hierarquia da Tela de Pastas (Priority: P2)

Como ouvinte, quero que a tela de gerenciamento de pastas não exiba explicações permanentes sobre a regra de hierarquia, mostrando apenas um aviso claro caso eu tente adicionar uma pasta coberta por outra.

**Why this priority**: Alinha a interface à emenda constitucional `v1.0.1`, mantendo a tela de pastas mais limpa e objetiva.

**Independent Test**: Pode ser testada abrindo a tela "Pastas" e verificando a ausência do card informativo permanente da regra de hierarquia, e em seguida tentando adicionar uma subpasta de uma pasta já incluída para confirmar o aviso em português.

**Acceptance Scenarios**:

1. **Given** que o usuário abre a tela "Pastas", **When** visualiza o cabeçalho e a lista de pastas, **Then** não há card ou texto explicativo permanente sobre a regra de hierarquia na tela.
2. **Given** que `/Music` já está incluída, **When** o usuário tenta adicionar `/Music/Rock`, **Then** o sistema exibe um aviso claro em português explicando que a pasta já está incluída dentro de `/Music` e não a duplica.

### Edge Cases

- **Nome de playlist com espaços extras ou vazio:** Espaços nas extremidades são removidos antes da validação; nomes vazios são recusados com mensagem em português.
- **Faixa removida do aparelho ou de pasta autorizada:** Ao abrir uma playlist, faixas cujo arquivo ou pasta não está mais disponível são tratadas graciosamente sem travar o aplicativo.
- **Troca de saída de áudio externa:** Mesmo sem botão de troca no app, desconectar fones continua pausando a reprodução conforme a Constituição.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema MUST iniciar imediatamente a reprodução da faixa ao usuário tocar sobre qualquer item de música na biblioteca ou em uma playlist.
- **FR-002**: O sistema MUST remover da tela "Tocando Agora" qualquer opção ou botão de troca de dispositivo de áudio, utilizando automaticamente a rota de áudio ativa no sistema.
- **FR-003**: O sistema MUST permitir criar, renomear e excluir playlists locais (com confirmação na exclusão), além de adicionar e remover músicas, com validação de nome (não vazio e sem duplicidade, ignorando diferenças apenas de maiúsculas/minúsculas ou espaços nas pontas) em lógica isolada da interface.
- **FR-004**: O sistema MUST expor a aba inferior `"Playlists"` na navegação principal e a ação `"Adicionar à playlist"` no menu `⋮` das faixas na biblioteca, persistindo localmente as playlists e suas associações sem depender de rede ou backend.
- **FR-005**: O sistema MUST remover o card/texto informativo permanente sobre regra de hierarquia da tela "Pastas", exibindo aviso em português apenas quando o usuário tentar adicionar uma pasta descendente de outra já incluída.

### Key Entities

- **Playlist**: Coleção local nomeada pelo usuário contendo identificador único, nome normalizado, data de criação e lista ordenada de faixas associadas.
- **PlaylistValidationResult**: Resultado da validação pura de criação/edição de playlist (sucesso, nome vazio ou nome duplicado).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Ao tocar em qualquer música na lista, a reprodução inicia em menos de 1 segundo em condições normais de uso local.
- **SC-002**: 100% das regras de validação de playlist (criação com nome válido, nome vazio, nome duplicado e adição de faixa) passam em testes unitários independentes da interface.
- **SC-003**: A tela "Tocando Agora" não apresenta nenhum controle de troca de saída de áudio e a tela "Pastas" não apresenta texto permanente de regra de hierarquia, exibindo aviso somente em tentativa inválida.

## Assumptions

- Esta especificação evolui e refina o comportamento das telas especificadas em `001-mvp-part1-player`, mantendo todas as restrições da Constituição `v1.0.1` (zero bibliotecas externas, MVVM puro sem AndroidX, Kotlin stdlib, PT-BR na UI e acessibilidade TalkBack com alvos >= 48dp).
- O escopo completo de playlists da Parte 2 na Constituição inclui criar, renomear, excluir, adicionar e remover músicas; a confirmação de inclusão parcial vs completa será fechada na clarificação.
