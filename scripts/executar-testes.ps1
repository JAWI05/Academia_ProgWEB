# Roda toda a suíte (com cobertura) e gera docs\evidencias\RESULTADOS_TESTES.md
# Uso (PowerShell, na raiz do projeto):  .\scripts\executar-testes.ps1
$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")
.\mvnw.cmd -B clean verify -Pcobertura
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
java scripts\RelatorioTestes.java
