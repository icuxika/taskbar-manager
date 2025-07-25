$rootPath = Split-Path -Parent $PSScriptRoot
$headerPath = Join-Path -Path $rootPath -ChildPath "ffm.h"
$includesPath = Join-Path -Path $rootPath -ChildPath "includes.txt"

$outputPath = ".\composeApp\src\desktopMain\java"
$targetPackage = "com.icuxika.taskbar_manager.jextract.win32"

Write-Host "jextract:" -ForegroundColor Blue
Write-Host "    run_extract.ps1: $PSScriptRoot" -ForegroundColor Blue
Write-Host "    ffm.h: $headerPath" -ForegroundColor Blue
Write-Host "    includes.txt: $includesPath" -ForegroundColor Blue

try {
    $jextractPath = Get-Command jextract -ErrorAction Stop
    & $jextractPath --output $outputPath -t $targetPackage -l user32 "@$includesPath" $headerPath
    Write-Host "jextract 已完成代码生成" -ForegroundColor Green
}
catch {
    throw "jextract 不存在于 PATH 中"
}