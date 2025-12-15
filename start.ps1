# Telecom Contract System Launcher

# Set output encoding to UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$Host.UI.RawUI.WindowTitle = "Telecom Contract System Launcher"

# Helper functions
function Print-Msg {
    param([string]$Text, [string]$Color = "White")
    Write-Host $Text -ForegroundColor $Color
}

function Print-Step {
    param([string]$Icon, [string]$Text, [string]$Status = "info")
    
    if ($Status -eq "info") { $c = "Cyan" }
    elseif ($Status -eq "success") { $c = "Green" }
    elseif ($Status -eq "warning") { $c = "Yellow" }
    elseif ($Status -eq "error") { $c = "Red" }
    else { $c = "Magenta" }
    
    Write-Host "$Icon " -NoNewline -ForegroundColor $c
    Write-Host $Text
}

# Function to kill process on port
function Kill-Port {
    param([int]$Port)
    Print-Step "[?]" "Checking port $Port..." "info"

    # Prefer netstat parsing (works without admin in most environments)
    $pids = @()
    try {
        $lines = netstat -ano | findstr ":$Port"
        if ($lines) {
            $pids = $lines | ForEach-Object { ($_ -split '\s+')[-1] } | Sort-Object -Unique
        }
    } catch {
        $pids = @()
    }

    # Fallback: Get-NetTCPConnection
    if (-not $pids -or $pids.Count -eq 0) {
        try {
            $conns = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue
            if ($conns) {
                $pids = $conns | Select-Object -ExpandProperty OwningProcess | Sort-Object -Unique
            }
        } catch {
            $pids = @()
        }
    }

    $pids = $pids | Where-Object { $_ -and $_ -ne "0" } | Sort-Object -Unique

    if ($pids -and $pids.Count -gt 0) {
        Print-Step "[!]" "Port $Port is in use by PID(s): $($pids -join ', '). Killing..." "warning"
        foreach ($pid in $pids) {
            try {
                taskkill /PID $pid /F | Out-Null
            } catch { }
        }
        Start-Sleep -Seconds 1
        Print-Msg "Port $Port should be free now." "Green"
    } else {
        Print-Msg "Port $Port is free." "Green"
    }
}

# Find a free port from a list (avoids admin/kill issues)
function Get-FreePort {
    param([int[]]$Candidates)
    foreach ($p in $Candidates) {
        $inUse = $false
        try {
            $lines = netstat -ano | findstr ":$p"
            if ($lines) { $inUse = $true }
        } catch { }
        if (-not $inUse) { return $p }
    }
    return $Candidates[0]
}

# Show Banner
Write-Host ""
Print-Msg "============================================================" "Cyan"
Print-Msg "|       Telecom Intelligent Contract System                |" "Yellow"
Print-Msg "============================================================" "Cyan"
Write-Host ""

# Set Project Root
$PROJECT_ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $PROJECT_ROOT
Print-Step "[*]" "Project Root: $PROJECT_ROOT" "info"
Write-Host ""

# Environment Check
Print-Step "[?]" "Checking environment..." "progress"

# Check Node
Write-Host "  [1/4] Node.js... " -NoNewline
if (Get-Command node -ErrorAction SilentlyContinue) {
    Print-Msg "OK" "Green"
} else {
    Print-Msg "NOT FOUND (Install from nodejs.org)" "Red"
}

# Check npm
Write-Host "  [2/4] npm...     " -NoNewline
if (Get-Command npm -ErrorAction SilentlyContinue) {
    Print-Msg "OK" "Green"
} else {
    Print-Msg "NOT FOUND" "Red"
}

# Check Java
Write-Host "  [3/4] Java...    " -NoNewline
if (Get-Command java -ErrorAction SilentlyContinue) {
    Print-Msg "OK" "Green"
} else {
    Print-Msg "NOT FOUND (Install JDK 17+)" "Red"
}

# Check Maven
Write-Host "  [4/4] Maven...   " -NoNewline
if (Get-Command mvn -ErrorAction SilentlyContinue) {
    Print-Msg "OK" "Green"
} elseif (Test-Path "$PROJECT_ROOT\backend\mvnw.cmd") {
    Print-Msg "OK (Wrapper)" "Yellow"
} else {
    Print-Msg "NOT FOUND" "Red"
}

Write-Host ""

# Menu
Print-Step "[*]" "Select mode:" "info"
Print-Msg "  [1] Start All (Recommended)"
Print-Msg "  [2] Backend Only"
Print-Msg "  [3] Frontend Only"
Print-Msg "  [4] Install Dependencies"
Print-Msg "  [0] Exit"
Print-Msg "  [5] Stop Services (kill ports 8080/5173)"
Write-Host ""

$choice = Read-Host "Enter [0-5]"

if ($choice -eq "1") {
    # Start All
    Print-Step "[+]" "Starting services..." "progress"
    
    # 1. Pick backend port (prefer 8080; if occupied, use 8081/8082...)
    $backendPort = Get-FreePort @(8080, 8081, 8082, 8083, 8090)
    if ($backendPort -ne 8080) {
        Print-Step "[!]" "Port 8080 is busy. Will start backend on $backendPort instead." "warning"
    }
    
    # 2. Start Backend in a new window
    $backendPath = Join-Path $PROJECT_ROOT "backend"
    # Use APP_PORT env var to avoid Maven argument parsing issues on Windows/PowerShell
    $cmd = "mvn spring-boot:run"
    if (Test-Path (Join-Path $backendPath "mvnw.cmd")) { $cmd = ".\mvnw.cmd spring-boot:run" }
    
    Print-Step "[>]" "Starting Backend (in new window)..." "info"
    # Removed the problematic WindowTitle command inside the string
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "`$env:APP_PORT='$backendPort'; cd '$backendPath'; echo 'Starting Backend...'; $cmd"
    
    Print-Step "[.]" "Waiting for backend to initialize (15s)..." "info"
    Start-Sleep -Seconds 15
    
    # 3. Start Frontend in CURRENT window
    $frontendPath = Join-Path $PROJECT_ROOT "frontend"
    Set-Location $frontendPath
    
    Print-Step "[>]" "Starting Frontend..." "info"
    if (-not (Test-Path "node_modules")) {
        Print-Msg "node_modules not found. Installing dependencies..." "Yellow"
        npm install
    }

    # Pass backend URL to Vite proxy (so frontend uses the same backend port)
    $env:VITE_BACKEND_URL = "http://localhost:$backendPort"
    
    npm run dev

} elseif ($choice -eq "2") {
    # Backend Only
    $backendPort = Get-FreePort @(8080, 8081, 8082, 8083, 8090)
    $backendPath = Join-Path $PROJECT_ROOT "backend"
    Set-Location $backendPath
    $env:APP_PORT = "$backendPort"
    if (Test-Path "mvnw.cmd") { .\mvnw.cmd spring-boot:run } else { mvn spring-boot:run }

} elseif ($choice -eq "3") {
    # Frontend Only
    $frontendPath = Join-Path $PROJECT_ROOT "frontend"
    Set-Location $frontendPath
    if (-not (Test-Path "node_modules")) { npm install }
    npm run dev

} elseif ($choice -eq "4") {
    # Install
    Print-Step "[+]" "Installing..." "progress"
    
    # Frontend
    Print-Step "[>]" "Frontend dependencies..." "progress"
    Set-Location (Join-Path $PROJECT_ROOT "frontend")
    npm install
    
    # Backend
    Print-Step "[>]" "Backend compilation..." "progress"
    Set-Location (Join-Path $PROJECT_ROOT "backend")
    if (Test-Path "mvnw.cmd") { .\mvnw.cmd compile -DskipTests } else { mvn compile -DskipTests }
    
    Print-Step "[V]" "Installed! Now select [1] to start." "success"

} elseif ($choice -eq "0") {
    exit 0
} elseif ($choice -eq "5") {
    Print-Step "[+]" "Stopping services (ports 8080/5173)..." "progress"
    Kill-Port 8080
    Kill-Port 5173
    Print-Step "[V]" "Done." "success"
} else {
    Print-Msg "Invalid option" "Red"
}

Write-Host ""
# Read-Host "Press Enter to exit"
