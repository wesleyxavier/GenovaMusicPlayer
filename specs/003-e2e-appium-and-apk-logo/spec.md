# Feature Specification: Infraestrutura E2E Appium, Logo do APK e Diretriz GitHub MCP

**Feature Branch**: `003-e2e-appium-and-apk-logo`

**Created**: 2026-11-22

**Status**: Draft

**Input**: User description: "Incluir teste e2e com appium com docker-compose.e2e.yml executado sempre via subagentes appium-test-*; incluir dependências necessárias para o teste E2E; incluir instrução de uso do MCP do GitHub no AGENTS.md; incluir logo oficial no APK."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Ícone/Logo Oficial no APK (Priority: P1)

Como ouvinte, quero que o aplicativo instalado no meu aparelho exiba o logo oficial **Nocturne Violet** no launcher e nas configurações do sistema, para identificar o player facilmente.

**Why this priority**: Identidade visual básica do aplicativo instalável no dispositivo.

**Independent Test**: Gerar o APK de debug e inspecionar/instalar no dispositivo verificando o ícone oficial exibido no launcher.

**Acceptance Scenarios**:

1. **Given** o projeto compilado, **When** o APK é gerado e instalado no dispositivo Android, **Then** o ícone oficial fornecido pelo autor aparece no launcher do sistema em todas as densidades de tela.

---

### User Story 2 - Infraestrutura de Testes E2E com Appium em Docker (Priority: P1)

Como pessoa desenvolvedora, quero uma suíte E2E isolada em container (`docker-compose.e2e.yml` + `e2e/`) com todas as dependências necessárias e cenários básicos que capturem screenshots das telas, para validar o app em execução real via subagentes `appium-test-hydrogen` ou `appium-test-haiku`.

**Why this priority**: Garante validação visual e de fluxo real no dispositivo/emulador sem instalar Node/Python/Appium no host.

**Independent Test**: Validar a sintaxe do `docker-compose.e2e.yml`, o build da imagem do runner `e2e/` e a estrutura dos cenários em `e2e/` prontos para execução pelos subagentes `appium-test-*`.

**Acceptance Scenarios**:

1. **Given** um dispositivo Android conectado via ADB no host, **When** um subagente `appium-test-hydrogen` ou `appium-test-haiku` executa a suíte via `docker-compose.e2e.yml`, **Then** o container Appium se conecta ao dispositivo via ADB TCP e o runner `e2e/` executa os cenários salvando screenshots em `e2e/artifacts/screenshots/<cenario>/<tela>.png`.
2. **Given** as dependências do módulo `e2e/`, **When** o APK do aplicativo é compilado, **Then** nenhuma dependência de teste E2E é empacotada no APK final.

---

### User Story 3 - Diretriz de MCP do GitHub e Subagentes E2E no AGENTS.md (Priority: P2)

Como mantenedor do projeto, quero que `AGENTS.md` documente explicitamente o uso do MCP do GitHub para operações de PR e a obrigatoriedade de executar testes E2E pelos subagentes `appium-test-*`, para padronizar futuras sessões de agentes.

**Why this priority**: Alinha a documentação operacional à Constituição `v1.1.0`.

**Independent Test**: Ler `AGENTS.md` e confirmar as seções explícitas de MCP do GitHub e execução E2E via subagentes.

**Acceptance Scenarios**:

1. **Given** o arquivo `AGENTS.md`, **When** um agente inicia uma sessão no repositório, **Then** encontra a instrução de usar o MCP do GitHub para PRs e os subagentes `appium-test-hydrogen` / `appium-test-haiku` com `docker-compose.e2e.yml` para testes E2E.

### Edge Cases

- **ADB desconectado ou sem emulador:** O runner/subagente interrompe com mensagem clara sem alterar código do app.
- **Dependências E2E isoladas:** O `app/build.gradle.kts` permanece 100% livre de bibliotecas externas.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O aplicativo MUST incluir o logo oficial fornecido pelo autor nos recursos de ícone do APK (`mipmap-*`) e referenciado no manifesto.
- **FR-002**: O projeto MUST definir `docker-compose.e2e.yml` com serviço Appium e serviço runner `e2e/` contendo todas as dependências necessárias para automação Android em container.
- **FR-003**: O módulo `e2e/` MUST conter cenários iniciais que validem as telas principais e salvem screenshots em `e2e/artifacts/screenshots/<cenario>/<tela>.png`.
- **FR-004**: O arquivo `AGENTS.md` MUST instruir o uso do MCP do GitHub para operações de PR/repositório e a execução obrigatória de testes E2E via `appium-test-hydrogen` ou `appium-test-haiku`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: `./gradlew testDebugUnitTest assembleDebug` continua passando 100% em Docker com o novo ícone do APK incluído e zero dependências externas no app.
- **SC-002**: `docker compose -f docker-compose.e2e.yml config` valida sem erros e a imagem do runner `e2e/` constrói com todas as dependências Appium instaladas.
- **SC-003**: `AGENTS.md` e `README.md` refletem integralmente a Constituição `v1.1.0`.

## Assumptions

- O host Windows possui `adb` disponível para conexão com o dispositivo físico e encaminhamento via ADB TCP para o container Appium.
