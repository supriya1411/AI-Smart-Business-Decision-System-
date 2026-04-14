# ── AI Smart Business Decision System — Local Startup ──
# No Docker required! Uses embedded SQLite database.

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " AI Smart Business Decision System"      -ForegroundColor Cyan
Write-Host " Local Startup (No Docker Required)"     -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Ensure data directory exists for SQLite DB
$dataDir = Join-Path $PSScriptRoot "data"
if (-not (Test-Path $dataDir)) {
    New-Item -ItemType Directory -Path $dataDir | Out-Null
    Write-Host "[+] Created data/ directory" -ForegroundColor Green
}

# Start Spring Boot application
Write-Host "[*] Starting Spring Boot application on http://localhost:8080 ..." -ForegroundColor Yellow
Write-Host "[*] Hibernate will auto-create tables in data/ai_business.db" -ForegroundColor Yellow
Write-Host ""
mvn spring-boot:run
