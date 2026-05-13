@echo off
REM 下载最新的 fongmi-TV APK 构建制品
REM 用法: download-apk.bat

set REPO=taurusqh/fongmi-TV
set ARTIFACT_NAME=mobile-arm64-v8a
set DOWNLOAD_DIR=downloads

if not exist "%DOWNLOAD_DIR%" mkdir "%DOWNLOAD_DIR%"

echo 正在查找最新构建...

for /f "tokens=*" %%i in ('gh api repos/%REPO%/actions/artifacts --paginate --jq ".artifacts[] | select(.name == \"mobile-arm64-v8a\") | .id" ^| head /1') do set ARTIFACT_ID=%%i

if "%ARTIFACT_ID%"=="" (
    echo 错误: 未找到 artifact
    exit /b 1
)

echo 下载 artifact ID: %ARTIFACT_ID%

for /f "tokens=*" %%t in ('gh auth token') do set GH_TOKEN=%%t

powershell -Command "[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://api.github.com/repos/%REPO%/actions/artifacts/%ARTIFACT_ID%/zip' -Headers @{ 'Accept' = 'application/vnd.github+json'; 'Authorization' = 'Bearer %GH_TOKEN%' } -OutFile '%DOWNLOAD_DIR%\%ARTIFACT_NAME%.apk'"

if exist "%DOWNLOAD_DIR%\%ARTIFACT_NAME%.apk" (
    for %%A in ("%DOWNLOAD_DIR%\%ARTIFACT_NAME%.apk") do echo 下载完成: %%A ^(%%~zA bytes^)
) else (
    echo 下载失败
    exit /b 1
)