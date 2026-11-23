# Implementation Plan: Infraestrutura E2E Appium, Logo do APK e Diretriz GitHub MCP

**Branch**: `003-e2e-appium-and-apk-logo` | **Date**: 2026-11-22 | **Spec**: [spec.md](file:///c:/Users/User/Documents/TI/Lavi/Web/Genova/src/PROJETOS/GenovaMusicPlayer/specs/003-e2e-appium-and-apk-logo/spec.md)

## Summary

Adicionar o logo oficial do APK em todas as densidades `mipmap-*` + `AndroidManifest.xml`, estruturar a suíte E2E Appium isolada em `docker-compose.e2e.yml` + `e2e/` (com dependências Python/Appium fora do APK) e atualizar `AGENTS.md` / `README.md` com as diretrizes da Constituição `v1.1.0` (subagentes `appium-test-*` e MCP do GitHub).

## Constitution Check

- [x] **Princípio I (Zero Dependências Externas no APK + Exceção E2E):** APK continua com zero dependências externas; dependências Appium ficam isoladas em `e2e/requirements.txt` e `docker-compose.e2e.yml`.
- [x] **Princípio VI (Logo oficial):** Ícone do APK adicionado em `mipmap-*`.
- [x] **Qualidade & Fluxo (v1.1.0):** Testes E2E via `docker-compose.e2e.yml` executados exclusivamente pelos subagentes `appium-test-hydrogen` ou `appium-test-haiku`; `AGENTS.md` atualizado com instrução do MCP do GitHub.
- [x] **Docker Isolation:** Build, testes unitários e E2E rodam em containers Docker.
