# Script to add layout="tabular" to all p:panelGrid components
# This ensures backward compatibility with PrimeFaces 14.0.0

$rootPath = "D:\Dev\fmis\src\main\webapp"
$xhtmlFiles = Get-ChildItem -Path $rootPath -Filter "*.xhtml" -Recurse
$filesModified = 0
$totalReplacements = 0

Write-Host "Starting to update panelGrid components..." -ForegroundColor Cyan
Write-Host "Found $($xhtmlFiles.Count) XHTML files to process`n" -ForegroundColor Yellow

foreach ($file in $xhtmlFiles) {
    $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
    $originalContent = $content
    $fileReplacements = 0

    # Pattern to match p:panelGrid tags that don't already have layout attribute
    # This regex looks for <p:panelGrid followed by attributes that don't include layout=
    $pattern = '<p:panelGrid\s+(?![^>]*layout\s*=)([^>]*?)>'

    $matches = [regex]::Matches($content, $pattern)

    if ($matches.Count -gt 0) {
        # Replace each match by adding layout="tabular"
        $content = [regex]::Replace($content, $pattern, '<p:panelGrid layout="tabular" $1>')

        $fileReplacements = $matches.Count

        # Save the file with UTF8 encoding
        Set-Content -Path $file.FullName -Value $content -Encoding UTF8 -NoNewline

        $filesModified++
        $totalReplacements += $fileReplacements

        Write-Host "Updated: $($file.FullName)" -ForegroundColor Green
        Write-Host "  -> Added layout attribute to $fileReplacements panelGrid(s)" -ForegroundColor Gray
    }
}

Write-Host "`n======================================" -ForegroundColor Cyan
Write-Host "Update Complete!" -ForegroundColor Green
Write-Host "Files modified: $filesModified" -ForegroundColor Yellow
Write-Host "Total panelGrid components updated: $totalReplacements" -ForegroundColor Yellow
Write-Host "======================================`n" -ForegroundColor Cyan
