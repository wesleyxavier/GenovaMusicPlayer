<!--
Sync Impact Report
- Version change: 1.0.0 -> 1.0.1
- Modified principles: IV. Biblioteca de Músicas e Regra de Hierarquia de Pastas (clarificação de exibição na UI)
- Added sections: None
- Removed sections: None
- Follow-up TODOs: None
-->
# GenovaMusicPlayer (Player de Música Android) Constitution

## Core Principles

### I. Plataforma e Zero Dependências Externas (NON-NEGOTIABLE)

O aplicativo DEVE ser exclusivamente Android, escrito em Kotlin, utilizando apenas o framework Android padrão e a biblioteca padrão do Kotlin. Nenhuma biblioteca de terceiros é permitida, incluindo AndroidX, Material Components, Jetpack Compose, RecyclerView ou Media3.

- Qualquer exceção a esta regra exige aprovação explícita do autor registrada nesta constituição, contendo justificativa, tamanho estimado e licença.
- O aplicativo NÃO DEVE possuir backend, conta de usuário, streaming ou sincronização na nuvem.

### II. Arquitetura MVVM Pura sem Bibliotecas

O projeto DEVE seguir o padrão MVVM sem uso de bibliotecas externas: o `ViewModel` DEVE ser uma classe Kotlin comum, sem herdar de componentes do AndroidX.

- As camadas DEVEM ser estritamente separadas em: `Model/Repository` (`MediaStore`, pastas e playlists), `ViewModel` (estado da tela) e `View` (`Activity` e `View`, responsáveis apenas por exibir estado e repassar eventos).
- Toda a lógica de pastas e de playlists DEVE residir em classes Kotlin puras, sem dependência do framework Android.
- O estado do player DEVE ser mantido em um objeto singleton e observado pelas telas por meio de callbacks, sem uso de `LiveData` ou `Flow`.

### III. Escopo Rígido do MVP

O desenvolvimento DEVE respeitar estritamente o escopo definido para o MVP:

- **Parte 1:** lista de músicas do aparelho, busca, tela "tocando agora", reprodução em segundo plano com controles na notificação e seleção de pastas.
- **Parte 2:** playlists (criar, renomear, excluir, adicionar e remover músicas).
- **Fora do MVP:** fila de reprodução, mini-player, letras, equalizador, Chromecast, temas e download de metadados online. Qualquer funcionalidade fora do MVP exige nova especificação aprovada antes de ser implementada.

### IV. Biblioteca de Músicas e Regra de Hierarquia de Pastas

A seleção de músicas DEVE respeitar a privacidade e a hierarquia de diretórios do usuário:

- As pastas DEVEM ser escolhidas exclusivamente via seletor do sistema (`ACTION_OPEN_DOCUMENT_TREE`); é proibida a varredura de todo o armazenamento.
- A lista de pastas selecionadas DEVE ser persistida e a leitura via `MediaStore` DEVE ser filtrada pelos caminhos dessas pastas.
- **Regra de hierarquia obrigatória:** nunca manter duas pastas em relação de ancestral e descendente:
  - Ao tentar adicionar uma pasta contida em outra já incluída, a operação DEVE ser recusada com mensagem clara em português.
  - Ao adicionar uma pasta que contém outras já incluídas, as pastas filhas DEVEM ser removidas automaticamente.
  - A regra de hierarquia NÃO DEVE ser exibida permanentemente na tela; ela atua como validação interna, exibindo aviso em português apenas quando o usuário tentar realizar uma operação inválida.
- Caminhos DEVEM ser normalizados e comparados por segmentos completos (ex.: `/Music2` NÃO é subpasta de `/Music`).
- A validação de pastas NÃO DEVE depender do Android e DEVE possuir testes unitários cobrindo obrigatoriamente: pasta igual, filha, mãe, irmã, prefixo de texto, barra final e caminho vazio.

### V. Reprodução em Segundo Plano e Controle de Áudio

A reprodução de áudio DEVE ser robusta e integrada ao sistema operacional:

- O player DEVE usar `MediaPlayer`, com todas as transições de estado tratadas explicitamente.
- A sessão de mídia DEVE ser exposta via `MediaSession` e a notificação DEVE usar `Notification.MediaStyle`.
- A reprodução em segundo plano DEVE ocorrer por meio de um `Service` em primeiro plano (Foreground Service).
- O foco de áudio DEVE ser solicitado e liberado corretamente; pausar ao perder o foco ou ao desconectar fones de ouvido é obrigatório.
- Os controles de pausar, retomar, próxima, anterior, seek, shuffle e repeat DEVEM funcionar com e sem a tela aberta.

### VI. Interface Nativa, Acessível e Fiel ao Design

A interface DEVE priorizar fidelidade visual, ergonomia e acessibilidade utilizando apenas Views nativas:

- A lista de músicas e a tela "tocando agora" DEVEM seguir o design de referência do YouTube Music fornecido pelo autor; em caso de divergência, o design vence.
- A UI DEVE ser construída exclusivamente com `View`s do framework Android, sem Jetpack Compose.
- Todos os textos exibidos na interface DEVEM estar em português do Brasil.
- Uso com uma mão: alvos de toque DEVEM ter no mínimo `48dp` e os controles principais DEVEM permanecer na metade inferior da tela.
- Acessibilidade: compatibilidade obrigatória com TalkBack, exigindo descrição de conteúdo (`contentDescription`) em todos os controles.

### VII. Simplicidade e Ausência de Código Morto

O código DEVE manter apenas o necessário para cumprir a especificação vigente:

- Deve-se preferir sempre a solução mais simples que atenda à especificação, sem adicionar camadas, padrões ou abstrações sem necessidade concreta no MVP.
- Código morto e funcionalidades não especificadas DEVEM ser removidos imediatamente, nunca mantidos "por precaução".

## Padrões de Desempenho

- Listas DEVEM ser virtualizadas, criando e vinculando apenas os itens visíveis na tela.
- Capas de álbuns DEVEM ser carregadas sob demanda, redimensionadas antes da exibição e armazenadas em cache de memória.
- Leitura de arquivos e decodificação de imagens NUNCA DEVEM ocorrer na thread principal.
- Antes de considerar qualquer etapa concluída, DEVE-SE medir o tempo de abertura da lista com pelo menos 2.000 músicas.

## Qualidade, Testes e Fluxo de Trabalho

- Regras de negócio (pastas, busca, estado da fila e validação de playlists) DEVEM ser isoladas da interface e cobertas por testes unitários.
- Toda mudança de comportamento DEVE começar por especificação no Spec Kit, com critérios de aceitação definidos antes da implementação.
- Nomes de classes, funções e variáveis DEVEM ser escritos em inglês; textos de interface e mensagens de erro DEVEM ser escritos em português do Brasil.

## Governance

- Esta constituição prevalece sobre quaisquer outras práticas ou solicitações ad-hoc no projeto.
- **Emendas:** esta constituição só pode ser alterada por decisão explícita do autor (Wesley Xavier De Oliveira Monteiro), com data e motivo obrigatoriamente registrados na seção de histórico ao final deste arquivo.
- **Resolução de conflitos:** em caso de conflito entre um pedido novo e esta constituição, o conflito DEVE ser apontado explicitamente antes de qualquer código ser escrito.
- **Versionamento semântico:** `MAJOR` para remoção ou redefinição incompatível de princípios; `MINOR` para adição de princípio/seção ou expansão material de regras; `PATCH` para esclarecimentos, correções de texto ou refinamentos sem impacto semântico.
- **Conformidade:** toda especificação, plano técnico e revisão de código DEVE verificar explicitamente a aderência a esta constituição.

### Histórico de Emendas

- **2026-11-21 (v1.0.0):** Ratificação inicial da constituição do projeto pelo autor Wesley Xavier De Oliveira Monteiro.
- **2026-11-22 (v1.0.1):** Esclarecimento no Princípio IV pelo autor: a regra de hierarquia de pastas permanece oculta na UI por padrão, exibindo aviso apenas quando houver tentativa inválida.

**Version**: 1.0.1 | **Ratified**: 2026-11-21 | **Last Amended**: 2026-11-22
