# Feature Specification: MVP Parte 1 — Biblioteca, Busca, Tocando Agora e Pastas

**Feature Branch**: `001-mvp-part1-player`

**Created**: 2026-11-21

**Status**: Draft

**Input**: User description: "Detalhamento completo das telas da Parte 1 do MVP: Biblioteca & Busca (SCREEN_6), Tocando Agora (SCREEN_4) e Gerenciador de Pastas (SCREEN_2), alinhado à Constituição do Player de Música Android."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Selecionar e Gerenciar Pastas de Música (Priority: P1)

Como ouvinte, quero escolher explicitamente quais pastas do meu aparelho contêm minhas músicas e gerenciar essa lista com segurança, para que apenas meu acervo autorizado apareça no player sem varrer todo o armazenamento.

**Why this priority**: Sem pastas autorizadas não há músicas na biblioteca; é o ponto de entrada de dados e garante a regra obrigatória de privacidade e hierarquia de diretórios.

**Independent Test**: Pode ser testada abrindo a aba "Pastas", adicionando uma ou mais pastas pelo seletor do sistema, verificando o bloqueio de subpastas redundantes, a remoção automática de filhas ao adicionar uma pasta mãe e a exclusão de uma pasta da lista.

**Acceptance Scenarios**:

1. **Given** que o usuário está na tela "Pastas", **When** toca em "+ Adicionar Pasta (Seletor do Sistema)" e escolhe uma pasta válida sem relação de ancestral/descendente com as já incluídas, **Then** a pasta é persistida, seu caminho normalizado, contagem de músicas, tipo de conteúdo e data de inclusão são exibidos, e o diagnóstico atualiza o total de músicas e espaço ocupado.
2. **Given** que a pasta `/Music` já está incluída, **When** o usuário tenta adicionar `/Music/Rock`, **Then** o sistema recusa a adição com mensagem clara em português informando que a pasta já está coberta pela pasta pai.
3. **Given** que `/Music/Rock` já está incluída, **When** o usuário adiciona `/Music`, **Then** `/Music/Rock` é removida automaticamente da lista e apenas `/Music` permanece monitorada.
4. **Given** que `/Music` está incluída, **When** o usuário adiciona `/Music2`, **Then** ambas permanecem na lista por serem pastas irmãs (comparação por segmentos completos).
5. **Given** que uma pasta monitorada está na lista, **When** o usuário toca no botão Lixeira e confirma a remoção, **Then** a pasta é removida da persistência local e suas músicas deixam de aparecer na biblioteca.

---

### User Story 2 - Navegar, Filtrar e Buscar na Biblioteca de Músicas (Priority: P1)

Como ouvinte, quero visualizar todas as músicas das pastas autorizadas, filtrar rapidamente por categorias, buscar por título ou artista em tempo real e iniciar a reprodução (direta ou aleatória), para encontrar e tocar qualquer faixa rapidamente.

**Why this priority**: É a tela principal de uso diário do aplicativo e o ponto de disparo da reprodução.

**Independent Test**: Pode ser testada com pastas autorizadas contendo músicas locais, digitando na barra de pesquisa, alternando chips de filtro, tocando em uma faixa ou no botão "Aleatório" e abrindo o menu de opções de uma faixa.

**Acceptance Scenarios**:

1. **Given** que existem músicas indexadas nas pastas autorizadas, **When** o usuário acessa a aba "Músicas", **Then** visualiza o cabeçalho de estatísticas (total de músicas, origem e qualidade), o botão "Aleatório", os chips de filtro e a lista virtualizada com miniatura da capa, título, artista e duração.
2. **Given** que a lista de músicas está visível, **When** o usuário toca na lupa ou digita na barra "Pesquisar músicas, artistas...", **Then** a lista é filtrada em tempo real pelos metadados locais correspondentes.
3. **Given** que o usuário visualiza a lista, **When** seleciona um chip de categoria ("Todas" ou "Recentes"), **Then** a lista exibe todas as faixas ou as adicionadas/modificadas mais recentemente, conforme o filtro selecionado.
4. **Given** que o usuário toca no botão "Aleatório" ou em qualquer faixa da lista, **When** a reprodução inicia, **Then** o serviço de reprodução em segundo plano é iniciado, a faixa ativa exibe indicador visual de barras sonoras animadas na lista e os controles ficam disponíveis na notificação de mídia do sistema.
5. **Given** que o usuário toca no botão "Mais opções" (⋮) de uma faixa, **When** o menu de contexto abre, **Then** exibe a ação "Ver detalhes do arquivo" (formato, taxa de amostragem, tamanho e caminho normalizado).

---

### User Story 3 - Controlar Reprodução na Tela "Tocando Agora" e em Segundo Plano (Priority: P1)

Como ouvinte, quero controlar a faixa em reprodução com uma mão na tela "Tocando Agora" e continuar ouvindo e controlando a música com a tela desligada ou fora do app, para ter experiência contínua de player de música.

**Why this priority**: Completa o ciclo essencial do player na Parte 1 (reprodução ativa, ergonomia e continuidade em segundo plano).

**Independent Test**: Pode ser testada iniciando uma música, abrindo "Tocando Agora", testando play/pause, próxima, anterior, seek, shuffle, repeat, minimizando para a lista, bloqueando a tela e verificando os controles na notificação e pausa ao desconectar fones.

**Acceptance Scenarios**:

1. **Given** que uma música está tocando, **When** o usuário abre "Tocando Agora", **Then** vê a barra superior (voltar ←, título "Reproduzindo...", menu ⋮ com detalhes técnicos), capa do álbum com badge de qualidade (ex.: "Hi-Fi Sem Perdas"), título, artistas, botão compartilhar, barra de progresso interativa com tempos decorrido/total, painel de controles na metade inferior (shuffle, anterior, play/pause 64dp, próxima, repetir) e cartão de saída de áudio.
2. **Given** que o usuário está em "Tocando Agora", **When** toca em voltar (←), **Then** retorna à lista de músicas sem interromper a reprodução.
3. **Given** que uma música está tocando, **When** o usuário arrasta a barra de progresso ou alterna shuffle/repetir (Não repetir / Repetir todas / Repetir uma), **Then** a posição e o modo da sessão atualizam imediatamente, mantendo o comportamento também quando a tela está fechada ou bloqueada.
4. **Given** que a reprodução está ativa, **When** os fones de ouvido são desconectados ou outro aplicativo assume o foco de áudio, **Then** a reprodução pausa automaticamente.
5. **Given** que o usuário toca no botão "Trocar" do cartão "Dispositivo de Áudio", **When** o seletor nativo do sistema abre, **Then** o usuário pode alternar a rota de áudio (ex.: Bluetooth ou Alto-falante).
6. **Given** que o usuário visualiza o rodapé da tela "Tocando Agora", **When** observa os botões "Fila a Seguir" e "Ver Letra", **Then** eles são exibidos apenas como elementos visuais inativos com indicação "Em breve" (sem implementar lógica de fila de reprodução ou leitura de letras na Parte 1, preservando o layout de referência sem violar o escopo do MVP).

### Edge Cases

- **Nenhuma pasta selecionada:** A aba "Músicas" deve exibir estado vazio claro orientando o usuário a adicionar uma pasta na aba "Pastas".
- **Pasta selecionada sem arquivos de áudio válidos:** A pasta permanece listada com contagem `0 músicas` e o diagnóstico reflete os totais sem falhar.
- **Caminho vazio, barra final ou prefixo de texto semelhante:** A validação de hierarquia normaliza barras finais, ignora caminhos vazios e compara segmentos completos (ex.: `/Music` vs `/Music2`).
- **Perda de permissão ou remoção externa de pasta/arquivo:** Ao tentar reproduzir ou listar arquivo inexistente, o sistema trata graciosamente sem travar e informa em português.
- **Interrupções de áudio (chamada telefônica, outro app, desconexão de fone):** Pausa automática garantida e liberação correta do foco de áudio.
- **Acessibilidade e ergonomia:** Todos os botões possuem descrição de conteúdo para leitor de tela (TalkBack) e alvos de toque de pelo menos 48dp.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema MUST permitir adicionar pastas exclusivamente pelo seletor de pastas do sistema operacional, persistindo a lista localmente sem varredura global do armazenamento.
- **FR-002**: O sistema MUST aplicar a regra de hierarquia de pastas: recusar com mensagem clara em português a inclusão de pasta descendente de outra já incluída e remover automaticamente pastas filhas ao incluir uma pasta ancestral, comparando caminhos normalizados por segmentos completos.
- **FR-003**: O sistema MUST permitir remover uma pasta da lista mediante confirmação, retirando suas músicas da biblioteca exibida.
- **FR-004**: O sistema MUST exibir na tela "Pastas" diagnóstico com momento da última varredura, espaço total ocupado e quantidade de faixas indexadas, além de ícone, nome, contagem de músicas, caminho normalizado e data de inclusão de cada pasta.
- **FR-005**: O sistema MUST listar na tela "Músicas" apenas faixas provenientes das pastas autorizadas, exibindo cabeçalho de estatísticas, capa sob demanda em lista virtualizada, indicador animado na faixa em reprodução, título, artista e duração.
- **FR-006**: O sistema MUST fornecer busca em tempo real por título e artista sobre o acervo local autorizado e botão "Aleatório" para iniciar reprodução aleatória imediata.
- **FR-007**: O sistema MUST manter a reprodução ativa em segundo plano com controles sincronizados na notificação de mídia do sistema (reproduzir/pausar, próxima, anterior) e respeitar foco de áudio e desconexão de fones.
- **FR-008**: A tela "Tocando Agora" MUST exibir barra superior (voltar, status, menu de detalhes técnicos), capa com badge de qualidade, identificação da faixa, barra de progresso interativa com marcadores de tempo, controles principais na metade inferior (shuffle, anterior, play/pause, próxima, repetir) e cartão de saída de áudio com atalho ao seletor nativo.
- **FR-009**: Toda a interface e mensagens de erro MUST estar em português do Brasil, seguir o design de referência do YouTube Music, respeitar alvos mínimos de toque de 48dp e possuir descrições de acessibilidade para TalkBack em todos os controles.
- **FR-010**: O sistema MUST suportar navegação entre as abas inferiores "Músicas" e "Pastas" mantendo a reprodução ininterrupta.

### Key Entities

- **AuthorizedFolder**: Representa uma pasta selecionada pelo usuário, com caminho normalizado, nome exibido, data de inclusão, contagem de faixas e tamanho total estimado.
- **Track**: Representa uma faixa de áudio local autorizada, com título, artista(s), álbum, duração, referência de capa, qualidade/formato de áudio e caminho/URI local.
- **PlaybackSession**: Estado global da sessão de reprodução atual, contendo faixa ativa, estado (tocando/pausado/parado), posição atual, modo shuffle e modo repeat.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A tela de lista de músicas abre e exibe uma biblioteca com pelo menos 2.000 músicas mantendo rolagem fluida e sem bloqueio da interface (tempo de abertura medido e validado antes da conclusão da etapa).
- **SC-002**: 100% dos cenários de validação de hierarquia de pastas (igual, filha, mãe, irmã, prefixo de texto, barra final e caminho vazio) passam em testes automatizados independentes da interface.
- **SC-003**: Usuários conseguem iniciar, pausar, avançar, retroceder, buscar posição e alternar shuffle/repeat tanto na tela "Tocando Agora" quanto com a tela bloqueada pela notificação do sistema.
- **SC-004**: 100% dos controles interativos das três telas possuem alvo de toque mínimo de 48dp e descrição de conteúdo verificável pelo leitor de tela.

## Assumptions

- Todas as músicas reproduzidas estão armazenadas localmente no dispositivo dentro das pastas autorizadas pelo usuário.
- O design visual de referência do YouTube Music fornecido pelo autor é a fonte de verdade para cores (incluindo destaque lavanda), espaçamentos e hierarquia visual.
- Detalhes técnicos de áudio no menu (⋮) da tela "Tocando Agora" (formato, taxa de amostragem, caminho do arquivo) e compartilhamento via menu nativo do sistema não conflitam com a proibição de download online nem de backend.
