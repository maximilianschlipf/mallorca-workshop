$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$processes = @()

function Stop-Port([int]$port) {
    Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty OwningProcess -Unique |
        ForEach-Object { Stop-Process -Id $_ -Force }
}

function Wait-Server([string]$name, [string]$url, $process, [int]$timeout) {
    $end = (Get-Date).AddSeconds($timeout)
    while ((Get-Date) -lt $end) {
        if ($process.HasExited) { throw "$name konnte nicht gestartet werden." }
        try {
            Invoke-WebRequest $url -UseBasicParsing -TimeoutSec 2 | Out-Null
            return
        } catch {
            Start-Sleep -Seconds 1
        }
    }
    throw "$name war nach ${timeout}s nicht bereit."
}

try {
    Stop-Port 18081
    Stop-Port 15173

    Write-Host 'Starte Backend (http://localhost:18081)...'
    $backend = Start-Process -FilePath "$root\backend\mvnw.cmd" -ArgumentList 'spring-boot:run' -WorkingDirectory "$root\backend" -NoNewWindow -PassThru
    $processes += $backend

    if (-not (Test-Path "$root\frontend\node_modules")) {
        Write-Host 'Installiere Frontend-Abhängigkeiten...'
        & npm.cmd ci --prefix "$root\frontend"
        if ($LASTEXITCODE) { throw "npm ci ist fehlgeschlagen ($LASTEXITCODE)." }
    }

    Wait-Server 'Backend' 'http://localhost:18081/api/health' $backend 120

    Write-Host 'Starte Frontend (http://localhost:15173)...'
    $frontend = Start-Process -FilePath 'npm.cmd' -ArgumentList 'run', 'dev' -WorkingDirectory "$root\frontend" -NoNewWindow -PassThru
    $processes += $frontend
    Wait-Server 'Frontend' 'http://localhost:15173' $frontend 30

    Write-Host 'Beide Server sind bereit. Mit Strg+C beenden.'
    while (-not $backend.HasExited -and -not $frontend.HasExited) { Start-Sleep -Seconds 1 }
    throw 'Ein Serverprozess wurde unerwartet beendet.'
} finally {
    $processes | Where-Object { $_ -and -not $_.HasExited } | Stop-Process -Force
}
