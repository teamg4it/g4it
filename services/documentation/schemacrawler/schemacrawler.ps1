param(
    [Parameter(Mandatory = $true)][string]$file
)

if (-not (Test-Path $file)) {
    throw "Could not find file '$file'"
}

$data = Get-Content $file | ConvertFrom-Json
$db = Get-Content 'db.properties.json' | ConvertFrom-Json
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$outDir = "out/$timestamp"
$weight = 0

foreach ($group in $data.tableGroups) {
    if (-not $($group.name)) {
        Write-Error "Skipping current table group as it has no name"
        continue
    }

    if (-not $($group.description)) {
        Write-Error "Skipping table group '$($group.name)' as it has no description"
        continue
    }

    if (-not $($group.tables)) {
        Write-Error "Skipping table group '$($group.name)' as it has no table"
        continue
    }

    Write-Host "Group name: $($group.name)"
    Write-Host "Content: `n - $($group.tables -join "`n - ")"
    
    # Regex to match the given table names: ^public.(table1|table2|...)$
    $group | Add-Member 'regex' "^$($db.schema)\.($($group.tables -join "|"))$"
    Write-Host "Matching RegEx: $($group.regex)"

    $dstDir = "$outDir/$($group.name)".toLower() -replace ' ', '_'
    $dstFile = "$dstDir/_index.md"

    if (-Not (Test-Path "$dstDir")) {
        New-Item -ItemType directory "$dstDir" | Out-Null
    }

    Write-Host "Generating '$dstFile'..."

    # Remove single quotes from group name and description
    $group.description = $($group.description) -replace "`'", ""
    $group.name = $($group.name) -replace "`'", ""
    
    # increase the weight to ensure proper page sorting in hugo
    $weight++

    # Hugo doc header
    "---`ntitle: '$($group.name)'`ndescription: '$($group.description)'`nweight: $weight`n---" | Add-Content $dstFile

    # Actual schemacrawler command
    podman run `
        -v '.:/home/schcrwlr/share' `
        --rm -it 'schemacrawler/schemacrawler' '/opt/schemacrawler/bin/schemacrawler.sh' `
        --server 'postgresql' `
        --host $($db.host) `
        --port $($db.port) `
        --database "$($db.database)?sslmode=disable" `
        --schemas $($db.schema) `
        --user $($db.user) `
        --password $($db.password) `
        --info-level 'standard' `
        --command 'script' `
        --script-language 'python' `
        --script '/home/schcrwlr/share/markdown.py' `
        --table-types 'TABLE' `
        --grep-tables $($group.regex) `
        --title $($group.name) `
    | Add-Content $dstFile


    if (-Not (Test-Path "$dstFile")) {
        Write-Error "Could not generate file '$dstFile'"
        Remove-Item $dstDir -Recurse -Force -ErrorAction SilentlyContinue
        continue
    }

    Write-Host "'$dstFile' has been generated successfully`n"
}

Write-Host "Done."