param(
    $Repo = "taurusqh/fongmi-TV",
    $ArtifactName = "mobile-arm64-v8a",
    $DownloadDir = "./downloads",
    $MaxAttempts = 5,
    $IntervalSeconds = 120
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $DownloadDir)) {
    New-Item -ItemType Directory -Path $DownloadDir | Out-Null
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " fongmi-TV APK Download Tool" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

function Get-LatestRun {
    $runs = gh api repos/$Repo/actions/runs --paginate | ConvertFrom-Json
    return $runs.workflow_runs[0]
}

function Get-ArtifactId {
    $artifacts = gh api repos/$Repo/actions/artifacts --paginate | ConvertFrom-Json
    $artifact = $artifacts.artifacts | Where-Object { $_.name -eq $ArtifactName } | Select-Object -First 1
    return $artifact.id
}

function Download-Artifact {
    param([string]$Id, [string]$OutputPath)
    $token = (gh auth token) | Out-String | ForEach-Object { $_.Trim() }
    $uri = "https://api.github.com/repos/$Repo/actions/artifacts/$Id/zip"
    Write-Host "Downloading..."
    $headers = @{ 'Accept' = 'application/vnd.github+json'; 'Authorization' = "Bearer $token" }
    Invoke-WebRequest -Uri $uri -Headers $headers -OutFile $OutputPath -UseBasicParsing
}

function Install-Apk {
    param([string]$ApkPath)
    Write-Host ""
    Write-Host "Installing to device..."
    $result = adb install -r $ApkPath 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Install SUCCESS!" -ForegroundColor Green
    } else {
        Write-Host "Install FAILED: $result" -ForegroundColor Red
    }
}

Write-Host "[1/3] Checking build status..." -ForegroundColor Cyan
$run = Get-LatestRun
Write-Host "  Build # $($run.run_number) - $($run.status) / $($run.conclusion)" -ForegroundColor White

if ($run.status -ne "completed") {
    Write-Host ""
    Write-Host "Build in progress, polling..." -ForegroundColor Yellow
    for ($i = 1; $i -le $MaxAttempts; $i++) {
        Write-Host "[Attempt $i/$MaxAttempts] Waiting ${IntervalSeconds}s..." -ForegroundColor Cyan
        Start-Sleep -Seconds $IntervalSeconds
        $run = Get-LatestRun
        Write-Host "  Status: $($run.status) / $($run.conclusion)" -ForegroundColor White
        if ($run.status -eq "completed") {
            Write-Host "Build completed!" -ForegroundColor Green
            break
        }
    }
    if ($run.status -ne "completed") {
        Write-Host "ERROR: Max attempts reached" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "Build already completed" -ForegroundColor Green
}

Write-Host ""
Write-Host "[2/3] Downloading artifact..." -ForegroundColor Cyan
$artifactId = Get-ArtifactId
if (-not $artifactId) {
    Write-Host "ERROR: Artifact not found" -ForegroundColor Red
    exit 1
}
Write-Host "  Artifact ID: $artifactId" -ForegroundColor White

$zipPath = Join-Path $DownloadDir "$ArtifactName.zip"
Download-Artifact -Id $artifactId -OutputPath $zipPath
Write-Host "  Downloaded: $zipPath" -ForegroundColor Green

Write-Host ""
Write-Host "[3/3] Extracting APK..." -ForegroundColor Cyan
$extractDir = Join-Path $DownloadDir "temp"
if (Test-Path $extractDir) { Remove-Item $extractDir -Recurse -Force }
Expand-Archive -Path $zipPath -DestinationPath $extractDir -Force

$apkFiles = Get-ChildItem -Path $extractDir -Filter "*.apk"
if ($apkFiles.Count -eq 0) {
    Write-Host "ERROR: No APK found" -ForegroundColor Red
    exit 1
}

$apk = $apkFiles[0]
$finalPath = Join-Path $DownloadDir $apk.Name
Copy-Item $apk.FullName -Destination $finalPath -Force
Remove-Item $zipPath -Force -ErrorAction SilentlyContinue
Remove-Item $extractDir -Recurse -Force -ErrorAction SilentlyContinue

$sizeMB = [math]::Round($apk.Length / 1MB, 2)

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Download Complete" -ForegroundColor Green
Write-Host "  Path: $finalPath" -ForegroundColor White
Write-Host "  Size: ${sizeMB} MB" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan

Write-Host ""
Write-Host "Install to device? (ok/y/yes to install)" -ForegroundColor Yellow
$response = Read-Host

if ($response -match '^(ok|y|yes)$') {
    Install-Apk -ApkPath $finalPath
} else {
    Write-Host "Skipped"
}
