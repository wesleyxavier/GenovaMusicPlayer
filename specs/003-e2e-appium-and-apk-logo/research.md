# Research & Technical Decisions: 003-e2e-appium-and-apk-logo

**Feature**: `003-e2e-appium-and-apk-logo` | **Date**: 2026-11-22

## 1. Stack E2E Isolada em `docker-compose.e2e.yml`

- **Decision**: Usar imagem oficial `appium/appium:latest` para o servidor Appium (com driver `uiautomator2`) e um container Python (`python:3.12-slim`) em `e2e/Dockerfile` com `Appium-Python-Client==4.2.1`, `selenium==4.25.0` e `pytest==8.3.3` em `e2e/requirements.txt`.
- **Rationale**: Leve, rápido para construir, 100% isolado do APK (`app/`) e compatível com `host.docker.internal:5037` / ADB TCP para alcançar o dispositivo conectado ao host Windows.
- **Alternatives considered**: Node.js + WebdriverIO (maior footprint de `node_modules` na imagem).

## 2. Geração de Ícones do APK (`mipmap-*`)

- **Decision**: Gerar os PNGs redimensionados a partir do logo enviado pelo autor para `mipmap-mdpi` (48px), `mipmap-hdpi` (72px), `mipmap-xhdpi` (96px), `mipmap-xxhdpi` (144px) e `mipmap-xxxhdpi` (192px) com nomes `ic_launcher.png` e `ic_launcher_round.png`, configurando `android:icon` e `android:roundIcon` no `AndroidManifest.xml`.
- **Rationale**: Compatível com `minSdk 26` / `compileSdk 34` sem exigir bibliotecas externas.
