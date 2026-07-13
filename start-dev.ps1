# MSME Platform - Development Startup Script (Windows)
# Usage: .\start-dev.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " IDBI MSME Platform - Dev Environment" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if Java 21 is available
$javaVersion = java -version 2>&1 | Select-String "version"
if ($javaVersion -notmatch "21") {
    Write-Host "WARNING: Java 21 recommended. Current: $javaVersion" -ForegroundColor Yellow
}

# Check if Node.js is available
$nodeVersion = node --version 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Node.js not found. Please install Node.js 20+." -ForegroundColor Red
    exit 1
}
Write-Host "Node.js: $nodeVersion" -ForegroundColor Green

# Check if firebase-service-account.json exists
$saPath = "backend\src\main\resources\firebase-service-account.json"
if (-not (Test-Path $saPath)) {
    Write-Host ""
    Write-Host "WARNING: $saPath not found!" -ForegroundColor Yellow
    Write-Host "  1. Go to Firebase Console > Project Settings > Service Accounts" -ForegroundColor Yellow
    Write-Host "  2. Click 'Generate new private key'" -ForegroundColor Yellow
    Write-Host "  3. Save as: $saPath" -ForegroundColor Yellow
    Write-Host ""
}

Write-Host ""
Write-Host "Starting Backend (port 8080)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\backend'; & '.tools\apache-maven-3.9.9\bin\mvn.cmd' spring-boot:run '-Dspring-boot.run.profiles=dev'" -WindowStyle Normal

Start-Sleep -Seconds 3

Write-Host "Starting Frontend (port 5173)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\frontend'; npm run dev" -WindowStyle Normal

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Services Starting:" -ForegroundColor Cyan
Write-Host "   Backend:  http://localhost:8080/api" -ForegroundColor White
Write-Host "   Frontend: http://localhost:5173" -ForegroundColor White
Write-Host "   Swagger:  http://localhost:8080/api/swagger-ui.html" -ForegroundColor White
Write-Host "   Health:   http://localhost:8080/api/actuator/health" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
