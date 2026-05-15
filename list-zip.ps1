Add-Type -AssemblyName System.IO.Compression.FileSystem
$z = [System.IO.Compression.ZipFile]::OpenRead('D:/Android/TVBox/fongmi-TV/downloads/mobile-arm64-v8a.zip')
$z.Entries | ForEach-Object { $_.Name }
$z.Dispose()