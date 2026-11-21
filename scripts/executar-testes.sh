#!/usr/bin/env sh
# Roda toda a suíte (com cobertura) e gera docs/evidencias/RESULTADOS_TESTES.md
set -e
cd "$(dirname "$0")/.."
./mvnw -B clean verify -Pcobertura
java scripts/RelatorioTestes.java
