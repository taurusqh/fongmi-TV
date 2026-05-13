@echo off
chcp 65001 >nul 2>&1

set REPO=taurusqh/fongmi-TV
set ARTIFACT_NAME=mobile-arm64-v8a
set DOWNLOAD_DIR=downloads

if not exist "%DOWNLOAD_DIR%" mkdir "%DOWNLOAD_DIR%"

echo fongmi-TV APK 下载工具
echo.

:check_status
echo 检查构建状态...
gh run list --repo %REPO% --limit 1

for /f "tokens=1,2,3" %%a in ('gh run list --repo %REPO% --limit 1 --json status,conclusion --jq ".[] | \"(.status) (.conclusion)\""') do (
    set STATUS=%%a
    set CONCLUSION=%%b
)

echo Status: %STATUS%, Conclusion: %CONCLUSION%
echo.

if "%STATUS%"=="completed" (
    echo 构建已完成
    goto :download
)

echo 构建未完成，2分钟后重试...
timeout /t 120 /nobreak
goto :check_status

:download
echo 查找制品...
for /f "tokens=*" %%i in ('gh api repos/%REPO%/actions/artifacts --paginate --jq ".artifacts[] | select(.name == \"%ARTIFACT_NAME%\") | .id"') do set ARTIFACT_ID=%%i

if not defined ARTIFACT_ID (
    echo 未找到制品
    exit /b 1
)

echo 下载 artifact !ARTIFACT_ID! ...

set GH_TOKEN=
for /f "tokens=*" %%t in ('gh auth token') do set GH_TOKEN=%%t

powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://api.github.com/repos/%REPO%/actions/artifacts/%ARTIFACT_ID%/zip' -Headers @{'Accept'='application/vnd.github+json';'Authorization'='Bearer %GH_TOKEN%'} -OutFile '%DOWNLOAD_DIR%\%ARTIFACT_NAME%.zip'"

echo 解压...
powershell -Command "Expand-Archive -Path '%DOWNLOAD_DIR%\%ARTIFACT_NAME%.zip' -DestinationPath '%DOWNLOAD_DIR%\temp' -Force"

for %%f in ("%DOWNLOAD_DIR%\temp\*.apk") do (
    copy "%%f" "%DOWNLOAD_DIR%\mobile-arm64_v8a.apk"
)

del "%DOWNLOAD_DIR%\%ARTIFACT_NAME%.zip" 2>nul
rmdir /s /q "%DOWNLOAD_DIR%\temp" 2>nul

echo.
echo 下载完成: %DOWNLOAD_DIR%\mobile-arm64_v8a.apk
echo.
echo 安装到手机？(输入 ok 安装)

set /p answer=
if /i "%answer%"=="ok" (
    echo 安装中...
    adb install -r "%DOWNLOAD_DIR%\mobile-arm64_v8a.apk"
) else (
    echo 跳过
)