$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot

function Invoke-Step([scriptblock]$step) {
    & $step
    if ($LASTEXITCODE) { exit $LASTEXITCODE }
}

Set-Location $root
Invoke-Step { py -3.12 -m unittest scripts.test_check_requirements }
Invoke-Step { py -3.12 scripts/check_requirements.py }
Push-Location docs
Invoke-Step { .\make.bat html }
Pop-Location
Push-Location backend
Invoke-Step { .\mvnw.cmd test }
Pop-Location
Invoke-Step { npm.cmd test --prefix "$root\frontend" }
Invoke-Step { npm.cmd run build --prefix "$root\frontend" }
$env:CI = '1'
Invoke-Step { npm.cmd run test:e2e --prefix "$root\frontend" }
