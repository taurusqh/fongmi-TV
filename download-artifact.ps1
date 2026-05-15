$headers = @{'Authorization' = 'Bearer ' + (gh auth token); 'Accept' = 'application/vnd.github+json'}
$response = Invoke-WebRequest -Uri 'https://api.github.com/repos/taurusqh/fongmi-TV/actions/artifacts/6990855556/zip' -Headers $headers -UseBasicParsing
[System.IO.File]::WriteAllBytes('D:/Android/TVBox/fongmi-TV/downloads/mobile-arm64-v8a.zip', $response.Content)