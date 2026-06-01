$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

function Invoke-Step {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Title,

        [Parameter(Mandatory = $true)]
        [scriptblock] $Command
    )

    Write-Host ""
    Write-Host "==> $Title" -ForegroundColor Cyan
    & $Command
}

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

if (-not (Test-Path ".\logs")) {
    New-Item -ItemType Directory -Path ".\logs" | Out-Null
}

Invoke-Step "Build Maven project" {
    .\mvnw.cmd clean package -DskipTests
}

Invoke-Step "Build Docker images" {
    docker-compose build
}

Invoke-Step "Start Docker containers" {
    docker-compose up -d
}

Invoke-Step "Follow application logs" {
    docker-compose logs -f --tail=200 app
}
