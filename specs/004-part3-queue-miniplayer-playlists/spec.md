# Feature Specification: MVP Parte 3 — Atalho "Tocando Agora", Mini-Player, Fila a Seguir e Evolução de Playlists

**Feature Branch**: `004-part3-queue-miniplayer-playlists`

**Created**: 2026-11-23

**Status**: Draft

**Input**: User description: "na tela PRINCIPAL Vamos incluir o botão 'Tocando agora' que redireciona pro 'Reproduzindo agora'. Terá um mini player na tela principal. Vamos habilitar a função 'Fila a seguir'. Vamos melhorar a função de playlist. Designs de referência incluídos em docs/design/."

## Reference Designs

- [Biblioteca com Mini-Player e chip "Tocando Agora"](../../docs/design/biblioteca_com_mini_player_ativo.png)
- [Fila a Seguir](../../docs/design/fila_a_seguir.png)
- [Coleção de Playlists](../../docs/design/colecao_de_playlists.png)
- [Detalhes da Playlist](../../docs/design/detalhes_da_playlist.png)
- [Criar Nova Playlist](../../docs/design/criar_nova_playlist.png)
- [Adicionar à Playlist](../../docs/design/adicionar_playlist.png)

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Atalho "Tocando Agora" e Mini-Player Persistente na Tela Principal (Priority: P1)

Como ouvinte, quero ver um mini-player com a música atual e um botão "Tocando Agora" na tela principal de Músicas (além das demais abas principais), para voltar rapidamente à tela "Reproduzindo agora" ou pausar/avançar a música sem perder minha navegação enquanto escolho outras faixas ou playlists.

**Why this priority**: Resolve o fluxo central de navegação entre a biblioteca e a execução atual: o usuário inicia uma música tocando nela na lista, pode retornar para escolher ou enfileirar mais faixas e mantém visibilidade e acesso instantâneo à reprodução ativa.

**Independent Test**: Pode ser testada iniciando uma música na aba "Músicas", voltando para a lista, verificando a exibição do mini-player flutuante com progresso e controles rápidos, usando o botão/chip "Tocando Agora" e tocando no mini-player para reabrir a tela "Reproduzindo agora".

**Acceptance Scenarios**:

1. **Given** que o usuário está na tela principal "Músicas" e há uma música ativa (tocando ou pausada), **When** visualiza a barra de chips superior e o rodapé acima das abas, **Then** vê o botão/chip `"Tocando Agora"` e o mini-player flutuante exibindo miniatura da capa, título, artista, indicador de progresso, botão reproduzir/pausar e botão próxima faixa.
2. **Given** que o usuário toca no botão/chip `"Tocando Agora"` ou no corpo do mini-player com uma sessão ativa, **When** aciona o toque, **Then** o aplicativo redireciona imediatamente para a tela `"Reproduzindo agora"` sem interromper o áudio.
3. **Given** que nenhuma música foi iniciada ainda na sessão, **When** o usuário visualiza a tela principal, **Then** o mini-player permanece oculto e, caso toque no botão `"Tocando Agora"`, recebe orientação clara em português de que nenhuma música está em reprodução no momento.
4. **Given** que o mini-player está visível, **When** o usuário toca no botão reproduzir/pausar ou próxima faixa no próprio mini-player, **Then** a reprodução alterna ou avança imediatamente e o mini-player reflete a faixa e estado atualizados em todas as abas principais (`Músicas`, `Playlists`, `Pastas`).
5. **Given** que o usuário visualiza a lista de músicas na tela `Músicas`, **When** toca diretamente sobre 1 música com a fila vazia, **Then** a fila inicia contendo apenas essa música e sua reprodução começa imediatamente; **When** toca sobre 1 música com a fila já ativa, **Then** essa música é adicionada como última da fila e passa a reproduzir imediatamente.

---

### User Story 2 - Subtela "Fila a Seguir" Completa e Interativa (Priority: P1)

Como ouvinte, quero abrir a subtela "Fila a Seguir" a partir da tela "Reproduzindo agora" para acompanhar o que está tocando e o que virá a seguir, adicionar novas músicas ou playlists à fila, remover itens, alterar a ordem das faixas e clicar diretamente em qualquer música da fila para tocá-la na hora.

**Why this priority**: É o núcleo funcional pedido para a Parte 3, transformando a sessão estática em uma fila de execução dinâmica controlável pelo usuário.

**Independent Test**: Pode ser testada tocando uma música, abrindo "Fila a Seguir" em "Reproduzindo agora", adicionando faixas individuais e uma playlist à fila, reordenando itens, removendo um item, tocando em uma música da fila para executá-la e salvando a fila como playlist.

**Acceptance Scenarios**:

1. **Given** que o usuário está na tela `"Reproduzindo agora"`, **When** toca no botão `"Fila a Seguir"`, **Then** abre a subtela `"Fila a Seguir"` exibindo cabeçalho com contagem total, botão `"Limpar"`, origem da reprodução, cartão `"Tocando Agora"` com progresso e play/pause, e a lista `"A Seguir na Fila"` com contagem restante e duração total.
2. **Given** que a fila contém músicas listadas na subtela `"Fila a Seguir"` (incluindo músicas tocadas anteriormente, a faixa atual destacada e as próximas na fila), **When** o usuário toca diretamente sobre qualquer música da lista, **Then** a reprodução da música clicada inicia imediatamente e o destaque da fila atualiza para ela.
3. **Given** que o usuário deseja incluir mais conteúdo na execução atual, **When** aciona `"+ Adicionar"` na tela `"Fila a Seguir"` (ou usa `"Adicionar à fila"` no menu de opções de uma música ou dentro de uma playlist), **Then** pode adicionar músicas individuais ou uma playlist inteira à fila sem interromper a faixa que está tocando.
4. **Given** que há itens na fila, **When** o usuário arrasta a alça de reordenação (`=`) de um item ou escolhe `"Remover da fila"` no menu `⋮`, **Then** a nova ordem e a remoção passam a valer imediatamente para a sequência de reprodução e para os contadores restantes.
5. **Given** que o usuário aciona `"Limpar"` na tela `"Fila a Seguir"`, **When** confirma a ação, **Then** os próximos itens da fila são removidos mantendo a faixa atual controlada sem travamento.
6. **Given** que há músicas na fila atual, **When** o usuário aciona `"Salvar Playlist"`, **Then** informa um nome válido e cria uma nova playlist local contendo as faixas da fila.
7. **Given** que a fila chega à última música com repetição desligada, **When** a opção `"Auto-play após o fim da fila"` está ativada, **Then** o player continua a reprodução com faixas da biblioteca; quando desativada, a reprodução encerra ao fim da fila.

---

### User Story 3 - Visão Geral e Criação Aprimorada de Playlists (Priority: P2)

Como ouvinte, quero buscar, ordenar, alternar entre grade e lista, fixar playlists favoritas no topo e criar novas playlists com opção de capa/arte automática e fixação, para organizar e acessar minhas coleções com rapidez e fidelidade visual.

**Why this priority**: Eleva a tela "Playlists" da Parte 2 para o nível visual e funcional do design de referência, facilitando o acesso rápido antes mesmo de abrir uma playlist.

**Independent Test**: Pode ser testada acessando a aba "Playlists", criando uma playlist com toggle de fixação e contagem de caracteres, filtrando pela barra de busca, alternando ordenação e modo grade/lista, e iniciando reprodução pelo botão Play direto do card.

**Acceptance Scenarios**:

1. **Given** que o usuário acessa a aba `"Playlists"`, **When** visualiza a tela, **Then** vê barra de busca para filtrar coleções, cartão de destaque `"Criar Nova Playlist"`, seção `"Minhas Playlists"` com contagem, seletor de ordenação (`Mais recentes`, `Alfabética`, `Mais faixas`), alternância entre visualização em grade e em lista, e o mini-player ativo quando houver reprodução.
2. **Given** que o usuário aciona criar playlist, **When** o modal `"Nova Playlist"` abre, **Then** vê campo de nome com contador de até 60 caracteres, opção de gerar arte automática local a partir do nome/faixas (ou escolher cor/ícone/capa local) e toggle `"Fixar no topo da biblioteca"`.
3. **Given** que existem playlists fixadas e não fixadas, **When** a lista de playlists é exibida, **Then** playlists fixadas aparecem primeiro com selo `"FIXADA"` e cada playlist exibe nome, contagem de músicas, duração total formatada e botão de reprodução rápida.
4. **Given** que o usuário toca no botão Play direto de um card/item de playlist (não vazia), **When** aciona o toque, **Then** inicia imediatamente a reprodução da playlist na fila sem precisar entrar na tela de detalhes.

---

### User Story 4 - Detalhes da Playlist Aprimorado e Adição em Múltiplas Playlists (Priority: P2)

Como ouvinte, quero filtrar faixas dentro de uma playlist, alternar e ajustar a ordem manual das músicas, enviar a playlist inteira para a fila e adicionar uma música a várias playlists de uma só vez, para montar minhas seleções sem trabalho repetitivo.

**Why this priority**: Completa o ciclo produtivo entre playlists e fila de reprodução, evitando adicionar música em uma playlist por vez e permitindo curadoria real da ordem das faixas.

**Independent Test**: Pode ser testada abrindo o menu de opções de uma faixa na biblioteca, selecionando múltiplas playlists no modal "Adicionar à Playlist" e confirmando; depois abrindo uma playlist, filtrando suas faixas, ativando "Ordem manual" para reordenar, e acionando "Adicionar à fila".

**Acceptance Scenarios**:

1. **Given** que o usuário aciona `"Adicionar à playlist"` no menu `⋮` de uma música, **When** o modal `"Adicionar à Playlist"` abre, **Then** visualiza o resumo da faixa selecionada, atalho `"+ Criar Nova Playlist"` (já criando com a faixa), campo `"Buscar playlist existente..."` e checkboxes para marcar ou desmarcar múltiplas playlists antes de tocar em `"Concluir"`.
2. **Given** que o usuário abre uma playlist em `"Detalhes da Playlist"`, **When** visualiza a tela, **Then** vê capa/arte da playlist, título, resumo (`N faixas • duração total • formatos`), botões principais `Play` e `Aleatório`, ações `"+ Adicionar músicas"`, `"Ordem manual"` e `"Adicionar à fila"`, além do campo `"Filtrar faixas na playlist..."`.
3. **Given** que o usuário ativa `"Ordem manual"` nos detalhes da playlist, **When** move uma faixa para cima/baixo ou pela alça de reordenação, **Then** a nova ordem das faixas é persistida localmente na playlist.
4. **Given** que o usuário toca em `"+ Adicionar músicas"` ou `"Adicionar à fila"` nos detalhes da playlist, **When** seleciona músicas da biblioteca para incluir na playlist ou aciona enviar para a fila, **Then** as faixas selecionadas são incluídas na playlist (sem duplicidade) ou adicionadas à fila de reprodução atual com feedback claro em português.

---

### User Story 5 - Playlists Inteligentes Locais do Sistema (Priority: P3)

Como ouvinte, quero acessar atalhos automáticos locais de "Adicionadas Recentemente" e "Mais Tocadas" na aba Playlists, para ouvir rapidamente minhas novidades e faixas mais reproduzidas sem precisar montá-las manualmente.

**Why this priority**: Completa a seção inferior presente no design de referência `colecao_de_playlists.png` de forma 100% offline, porém é incremental em relação à fila e gestão principal de playlists.

**Independent Test**: Pode ser testada reproduzindo algumas músicas localmente e verificando na aba "Playlists" a seção "Playlists Inteligentes do Sistema" com "Mais Tocadas (Top 50)" e "Adicionadas Recentemente" atualizando contagens e abrindo/reproduzindo normalmente.

**Acceptance Scenarios**:

1. **Given** que existem faixas nas pastas autorizadas, **When** o usuário visualiza a seção `"Playlists Inteligentes do Sistema"` na aba `"Playlists"`, **Then** vê `"Mais Tocadas (Top 50)"` (baseada em contagem local de reproduções) e `"Adicionadas Recentemente"` (ordenadas pela data mais recente no aparelho), podendo abrir ou reproduzir diretamente cada uma delas.

### Edge Cases

- **Nenhuma música em reprodução ao clicar em `"Tocando Agora"` ou abrir `"Fila a Seguir"`:** Exibe mensagem clara em português sem abrir tela quebrada ou vazia com erro.
- **Fila vazia após remover todos os próximos itens:** Mantém a música atual tocando normalmente e exibe estado vazio claro na lista `"A Seguir na Fila"` com botão `"+ Adicionar"`.
- **Remover a música atualmente em reprodução pela fila:** Se o item removido for o ativo, avança graciosamente para o próximo item válido ou encerra/pausa caso não haja mais itens.
- **Reordenação enquanto o modo Aleatório está ativo:** A alteração manual de ordem na fila atualiza a lista ordenada da sessão de forma previsível e consistente.
- **Adicionar playlist vazia à fila:** Recusa graciosamente com aviso em português informando que a playlist não possui faixas disponíveis.
- **Busca interna na playlist ativa durante `"Ordem manual"`:** A reordenação manual opera apenas quando a lista não está filtrada por texto (ou orienta limpar o filtro antes de reordenar), evitando índices inconsistentes.
- **Nome de playlist com mais de 60 caracteres:** O campo limita a entrada em 60 caracteres com contador visível (`N/60`) e mantém validação contra nome vazio ou duplicado.
- **Faixas removidas do armazenamento:** Tanto a fila quanto as playlists ignoram com segurança faixas cujo arquivo local deixou de existir, sem travar o app.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: A tela principal `"Músicas"` MUST exibir o botão/chip `"Tocando Agora"` que redireciona para a tela `"Reproduzindo agora"` quando houver faixa ativa na sessão, exibindo aviso claro em português caso nenhuma faixa tenha sido iniciada.
- **FR-002**: O sistema MUST exibir um mini-player flutuante persistente na tela principal `"Músicas"` e nas demais abas principais (`Playlists` e `Pastas`) sempre que houver uma faixa ativa na sessão, contendo miniatura da capa, título, artista, indicador visual de progresso, botão reproduzir/pausar e botão próxima faixa, abrindo `"Reproduzindo agora"` ao tocar no card.
- **FR-003**: Na tela `"Músicas"`, tocar em 1 música com a fila vazia MUST criar uma fila contendo apenas aquela música e iniciá-la; caso já exista fila ativa, MUST adicioná-la como última da fila e iniciar sua reprodução imediatamente (enquanto `"Aleatório"` carrega a lista visível). Na tela `"Detalhes da Playlist"` e na subtela `"Fila a Seguir"`, tocar em uma música MUST iniciar sua reprodução imediatamente.
- **FR-004**: Na tela `"Reproduzindo agora"`, o botão `"Fila a Seguir"` MUST abrir a subtela `"Fila a Seguir"` exibindo contagem total, origem da reprodução, estado do modo aleatório, cartão da faixa atual com progresso e controles, e a lista `"A Seguir na Fila"` com contagem e duração restante.
- **FR-005**: Na subtela `"Fila a Seguir"`, o usuário MUST poder: clicar em qualquer item da fila para iniciar imediatamente sua reprodução, adicionar músicas individuais ou playlists inteiras à fila, remover itens da fila, reordenar itens da fila, limpar os próximos itens da fila, alternar `"Auto-play após o fim da fila"`, alternar `Aleatório`/`Repetir` e salvar a fila atual como uma nova playlist local.
- **FR-006**: O menu de opções (`⋮`) das faixas na biblioteca e os detalhes de playlist MUST oferecer `"Adicionar à fila"` (adicionando a música ou toda a playlist à fila de reprodução atual, ou iniciando a reprodução caso nenhuma sessão esteja ativa).
- **FR-007**: A tela `"Playlists"` MUST suportar busca por nome em tempo real, cartão de destaque `"Criar Nova Playlist"`, ordenação (`Mais recentes`, `Alfabética`, `Mais faixas`), alternância entre visualização em Grade e em Lista, exibição de playlists fixadas no topo com destaque `"FIXADA"`, contagem de faixas com duração total e botão de reprodução rápida no card/item.
- **FR-008**: O fluxo `"Nova Playlist"` MUST permitir informar nome (máximo de 60 caracteres com contador), definir arte automática local ou opção de capa e marcar `"Fixar no topo da biblioteca"`, mantendo a validação de nome não vazio e não duplicado.
- **FR-009**: O fluxo `"Adicionar à Playlist"` MUST permitir buscar playlists existentes, criar uma nova playlist já com a música selecionada e marcar/desmarcar múltiplas playlists simultaneamente antes de confirmar em `"Concluir"`.
- **FR-010**: A tela `"Detalhes da Playlist"` MUST exibir cabeçalho com capa/arte, resumo (`N faixas • duração • formatos`), botões `Play` e `Aleatório`, ações `"+ Adicionar músicas"`, `"Ordem manual"` (para reordenar e persistir a ordem das faixas) e `"Adicionar à fila"`, além de campo de filtro interno por título/artista.
- **FR-011**: Toda a nova interface e mensagens de erro/feedback MUST estar em português do Brasil, seguir o Design System **Nocturne Violet** e os designs de referência em `docs/design/`, respeitar alvos mínimos de toque de `48dp` e possuir descrições de acessibilidade (`contentDescription`) para TalkBack.

### Key Entities

- **PlaybackQueue / PlaybackSession**: Estado global da sessão de reprodução contendo a lista ordenada de faixas da fila, o índice da faixa atual, a origem da reprodução (ex.: biblioteca ou nome da playlist), estado (`PLAYING`, `PAUSED`, etc.), posição/duração, modos `shuffle`/`repeat` e configuração de `autoPlayAfterQueue`.
- **Playlist**: Coleção local persistida contendo identificador único, nome (até 60 caracteres), data de criação, lista ordenada de identificadores de faixas, indicador `isPinned` (fixada no topo) e referência/estilo de capa local (`coverStyle` / `coverUri`).
- **TrackPlayStats**: Registro local offline de contagem de reproduções por faixa para alimentar a lista inteligente `"Mais Tocadas (Top 50)"`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: O usuário consegue alternar entre a tela `"Músicas"` e `"Reproduzindo agora"` em 1 toque pelo chip `"Tocando Agora"` ou pelo mini-player, mantendo a reprodução sem interrupções audíveis.
- **SC-002**: 100% das operações de fila de reprodução (adicionar música, adicionar playlist, tocar item específico da fila, remover item, reordenar itens, limpar fila e comportamento de auto-play ao fim da fila) são cobertas por testes unitários independentes da interface e passam sem falhas.
- **SC-003**: 100% das novas regras de playlist (limite de 60 caracteres, fixação no topo, reordenação manual de faixas, adição em lote de músicas e ordenação/filtro) passam em testes unitários independentes da interface.
- **SC-004**: O fluxo ponta a ponta (iniciar reprodução pela biblioteca, usar o mini-player, abrir `"Fila a Seguir"` por `"Reproduzindo agora"`, manipular a fila e navegar nas playlists aprimoradas) passa na validação E2E em container e mantém tempo de abertura de lista com 2.000+ músicas dentro do padrão de fluidez do projeto.

## Assumptions

- Os ícones de **Perfil/Conta**, **Favoritar (coração)** e **Download online** presentes em alguns mockups do Stitch permanecem omitidos ou substituídos por controles ativos autorizados (ex.: `Próxima faixa` no mini-player e na fila), em conformidade com o Princípio I e III da Constituição `v1.2.0` (app 100% offline, sem conta de usuário e sem módulo de favoritos).
- O botão `"Salvar Playlist"` na subtela `"Fila a Seguir"` reutiliza a validação padrão de nomes de `PlaylistManager` (não vazio, único e até 60 caracteres).
- A lista inteligente `"Mais Tocadas (Top 50)"` contabiliza reproduções locais registradas a partir da implantação desta fase (iniciando vazia ou com as faixas reproduzidas localmente), enquanto `"Adicionadas Recentemente"` utiliza a data de adição da faixa no armazenamento local (`MediaStore`).
- O trabalho será desenvolvido em worktree e branch própria `feature/004-part3-queue-miniplayer-playlists` criada a partir de `develop`.
