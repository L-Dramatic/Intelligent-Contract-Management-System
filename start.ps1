# Telecom Contract System Launcher
# Encoding: UTF-8 with BOM

# Set output encoding to UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
$Host.UI.RawUI.WindowTitle = "Telecom Contract System Launcher"

# Helper functions
function Print-Msg {
    param(
        [Parameter(Position=0)]
        [string]$Text,
        [Parameter(Position=1)]
        [string]$Color = "White"
    )
    Write-Host $Text -ForegroundColor $Color
}

function Print-Step {
    param(
        [Parameter(Position=0)]
        [string]$Icon,
        [Parameter(Position=1)]
        [string]$Text,
        [Parameter(Position=2)]
        [string]$Status = "info"
    )
    
    switch ($Status) {
        "info"     { $c = "Cyan" }
        "success"  { $c = "Green" }
        "warning"  { $c = "Yellow" }
        "error"    { $c = "Red" }
        default    { $c = "Magenta" }
    }
    
    Write-Host "$Icon " -NoNewline -ForegroundColor $c
    Write-Host $Text
}

# Function to kill process on port
function Kill-Port {
    param([int]$Port)
    Print-Step "[?]" "Checking port $Port..." "info"

    $pids = @()
    try {
        $lines = netstat -ano | findstr ":$Port"
        if ($lines) {
            $pids = $lines | ForEach-Object { ($_ -split '\s+')[-1] } | Sort-Object -Unique
        }
    } catch {
        $pids = @()
    }

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
        Print-Step "[!]" "Port $Port in use by PID(s): $($pids -join ', '). Killing..." "warning"
        foreach ($pid in $pids) {
            try { taskkill /PID $pid /F | Out-Null } catch { }
        }
        Start-Sleep -Seconds 1
        Write-Host "Port $Port should be free now." -ForegroundColor Green
    } else {
        Write-Host "Port $Port is free." -ForegroundColor Green
    }
}

# Find a free port
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

# Check Python venv
function Get-PythonVenvPath {
    param([string]$AiServicePath)
    
    $venvPaths = @(
        (Join-Path $AiServicePath "venv\Scripts\python.exe"),
        (Join-Path $AiServicePath ".venv\Scripts\python.exe")
    )
    
    foreach ($path in $venvPaths) {
        if (Test-Path $path) { return $path }
    }
    return $null
}

# Show Banner
Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "|       Telecom Intelligent Contract System                |" -ForegroundColor Yellow
Write-Host "|       Dian Xin Zhi Hui He Tong Guan Li Xi Tong           |" -ForegroundColor Yellow
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

# Set Project Root
$PROJECT_ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $PROJECT_ROOT
Print-Step "[*]" "Project Root: $PROJECT_ROOT" "info"

# Configuration
$MAVEN_LOCAL_REPO = Join-Path $PROJECT_ROOT "backend\.m2_repo"
# Use MAVEN_OPTS environment variable instead of command-line args (more reliable)
$env:MAVEN_OPTS = "-Dmaven.repo.local=$MAVEN_LOCAL_REPO"
Print-Step "[*]" "Maven Repo:   $MAVEN_LOCAL_REPO (Saves C: space)" "info"
Write-Host ""

# Environment Check
Print-Step "[?]" "Checking environment..." "info"

# Check Node
Write-Host "  [1/5] Node.js... " -NoNewline
if (Get-Command node -ErrorAction SilentlyContinue) {
    $nodeVersion = node --version
    Write-Host "OK ($nodeVersion)" -ForegroundColor Green
} else {
    Write-Host "NOT FOUND" -ForegroundColor Red
}

# Check npm
Write-Host "  [2/5] npm...     " -NoNewline
if (Get-Command npm -ErrorAction SilentlyContinue) {
    $npmVersion = npm --version
    Write-Host "OK (v$npmVersion)" -ForegroundColor Green
} else {
    Write-Host "NOT FOUND" -ForegroundColor Red
}

# Check Java
Write-Host "  [3/5] Java...    " -NoNewline
if (Get-Command java -ErrorAction SilentlyContinue) {
    Write-Host "OK" -ForegroundColor Green
} else {
    Write-Host "NOT FOUND (Install JDK 17+)" -ForegroundColor Red
}

# Check Maven
Write-Host "  [4/5] Maven...   " -NoNewline
if (Get-Command mvn -ErrorAction SilentlyContinue) {
    Write-Host "OK" -ForegroundColor Green
} elseif (Test-Path "$PROJECT_ROOT\backend\mvnw.cmd") {
    Write-Host "OK (Wrapper)" -ForegroundColor Yellow
} else {
    Write-Host "NOT FOUND" -ForegroundColor Red
}

# Check Python
Write-Host "  [5/5] Python...  " -NoNewline
$aiServicePath = Join-Path $PROJECT_ROOT "ai-service"
$pythonVenv = Get-PythonVenvPath $aiServicePath
$pythonCmd = "python"

if ($pythonVenv) {
    try {
        $pyVersion = & $pythonVenv --version 2>&1
        Write-Host "OK ($pyVersion - venv)" -ForegroundColor Green
        $pythonCmd = $pythonVenv
    } catch {
        Write-Host "OK (venv found)" -ForegroundColor Yellow
        $pythonCmd = $pythonVenv
    }
} elseif (Get-Command python -ErrorAction SilentlyContinue) {
    $pyVersion = python --version 2>&1
    Write-Host "OK ($pyVersion - system)" -ForegroundColor Yellow
} else {
    Write-Host "NOT FOUND (AI service disabled)" -ForegroundColor Yellow
    $pythonCmd = $null
}

Write-Host ""

# Menu
Print-Step "[*]" "Select startup mode:" "info"
Write-Host "  [1] Start All (Backend + Frontend + AI)" -ForegroundColor White
Write-Host "  [2] Start Backend + Frontend" -ForegroundColor White
Write-Host "  [3] Backend Only" -ForegroundColor White
Write-Host "  [4] Frontend Only" -ForegroundColor White
Write-Host "  [5] AI Service Only" -ForegroundColor White
Write-Host "  [6] Install Dependencies" -ForegroundColor White
Write-Host "  [7] Stop All Services (8080/5173/8765)" -ForegroundColor White
Write-Host "  [0] Exit" -ForegroundColor White
Write-Host ""

$choice = Read-Host "Enter [0-7]"

if ($choice -eq "1") {
    # Start All
    Print-Step "[+]" "Starting all services..." "info"
    
    # --- AI Service ---
    $aiServicePath = Join-Path $PROJECT_ROOT "ai-service"
    $aiPort = 8765
    
    if (Test-Path $aiServicePath) {
        $aiPort = Get-FreePort @(8765, 8766, 8767)
        if ($aiPort -ne 8765) {
            Print-Step "[!]" "Port 8765 busy. AI service on $aiPort." "warning"
        }
        
        Print-Step "[>]" "Starting AI Service (port $aiPort)..." "info"
        
        # Determine Python command and Venv activation
        $startCmd = ""
        $pythonVenv = Get-PythonVenvPath $aiServicePath
        
        if ($pythonVenv) {
            # Use venv
            $venvActivate = Join-Path (Split-Path $pythonVenv) "Activate.ps1"
            $startCmd = "cd '$aiServicePath'; & '$venvActivate'; Write-Host 'AI Service starting...' -ForegroundColor Cyan; python -m uvicorn app.main:app --host 0.0.0.0 --port $aiPort --reload"
        } else {
            # Use system python
            $startCmd = "cd '$aiServicePath'; Write-Host 'AI Service starting...' -ForegroundColor Cyan; python -m uvicorn app.main:app --host 0.0.0.0 --port $aiPort --reload"
        }
        
        Start-Process powershell -ArgumentList "-NoExit", "-Command", $startCmd
        
        Print-Step "[.]" "Waiting for AI service (10s)..." "info"
        Start-Sleep -Seconds 10
    }
    
    # --- Backend ---
    $backendPort = Get-FreePort @(8080, 8081, 8082, 8083)
    if ($backendPort -ne 8080) {
        Print-Step "[!]" "Port 8080 busy. Backend on $backendPort." "warning"
    }
    
    $backendPath = Join-Path $PROJECT_ROOT "backend"
    
    # Construct Maven command
    $mvnCmd = "mvn spring-boot:run"
    if (Test-Path (Join-Path $backendPath "mvnw.cmd")) { $mvnCmd = ".\mvnw.cmd spring-boot:run" }
    
    Print-Step "[>]" "Starting Backend (port $backendPort)..." "info"
    Print-Msg "    Linking to AI Service at: http://localhost:$aiPort" "Gray"
    
    # Use environment variable (Spring Boot auto-converts ai.service.base-url to AI_SERVICE_BASE_URL)
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "`$env:APP_PORT='$backendPort'; `$env:AI_SERVICE_BASE_URL='http://localhost:$aiPort'; `$env:MAVEN_OPTS='-Dmaven.repo.local=$MAVEN_LOCAL_REPO'; cd '$backendPath'; Write-Host 'Backend starting...' -ForegroundColor Cyan; $mvnCmd"
    
    Print-Step "[.]" "Waiting for backend (15s)..." "info"
    Start-Sleep -Seconds 15
    
    # --- Frontend ---
    $frontendPath = Join-Path $PROJECT_ROOT "frontend"
    Set-Location $frontendPath
    
    Print-Step "[>]" "Starting Frontend..." "info"
    if (-not (Test-Path "node_modules")) {
        Write-Host "Installing dependencies..." -ForegroundColor Yellow
        npm install
    }

    $env:VITE_BACKEND_URL = "http://localhost:$backendPort"
    
    Write-Host ""
    Print-Step "[V]" "Services started!" "success"
    Write-Host "  - Frontend:    http://localhost:5173" -ForegroundColor Cyan
    Write-Host "  - Backend:     http://localhost:$backendPort" -ForegroundColor Cyan
    Write-Host "  - AI Service:  http://localhost:$aiPort" -ForegroundColor Cyan
    Write-Host ""
    
    npm run dev

} elseif ($choice -eq "2") {
    # Backend + Frontend
    Print-Step "[+]" "Starting Backend + Frontend..." "info"
    
    $backendPort = Get-FreePort @(8080, 8081, 8082, 8083)
    if ($backendPort -ne 8080) {
        Print-Step "[!]" "Port 8080 busy. Backend on $backendPort." "warning"
    }
    
    $backendPath = Join-Path $PROJECT_ROOT "backend"
    $mvnCmd = "mvn spring-boot:run"
    if (Test-Path (Join-Path $backendPath "mvnw.cmd")) { $mvnCmd = ".\mvnw.cmd spring-boot:run" }
    
    Print-Step "[>]" "Starting Backend (port $backendPort)..." "info"
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "`$env:APP_PORT='$backendPort'; `$env:MAVEN_OPTS='-Dmaven.repo.local=$MAVEN_LOCAL_REPO'; cd '$backendPath'; Write-Host 'Backend starting...' -ForegroundColor Cyan; $mvnCmd"
    
    Print-Step "[.]" "Waiting for backend (15s)..." "info"
    Start-Sleep -Seconds 15
    
    $frontendPath = Join-Path $PROJECT_ROOT "frontend"
    Set-Location $frontendPath
    
    Print-Step "[>]" "Starting Frontend..." "info"
    if (-not (Test-Path "node_modules")) {
        Write-Host "Installing dependencies..." -ForegroundColor Yellow
        npm install
    }

    $env:VITE_BACKEND_URL = "http://localhost:$backendPort"
    
    Write-Host ""
    Print-Step "[V]" "Services started!" "success"
    Write-Host "  - Frontend:    http://localhost:5173" -ForegroundColor Cyan
    Write-Host "  - Backend:     http://localhost:$backendPort" -ForegroundColor Cyan
    Write-Host ""
    
    npm run dev

} elseif ($choice -eq "3") {
    # Backend Only
    $backendPort = Get-FreePort @(8080, 8081, 8082, 8083)
    if ($backendPort -ne 8080) {
        Print-Step "[!]" "Port 8080 busy. Backend on $backendPort." "warning"
    }
    
    $backendPath = Join-Path $PROJECT_ROOT "backend"
    Set-Location $backendPath
    $env:APP_PORT = "$backendPort"
    
    Print-Step "[>]" "Starting Backend on port $backendPort..." "info"
    Write-Host "  API Docs: http://localhost:$backendPort/doc.html" -ForegroundColor Cyan
    Write-Host ""
    
    $env:MAVEN_OPTS = "-Dmaven.repo.local=$MAVEN_LOCAL_REPO"
    if (Test-Path "mvnw.cmd") { 
        .\mvnw.cmd spring-boot:run
    } else { 
        mvn spring-boot:run
    }

} elseif ($choice -eq "4") {
    # Frontend Only
    $frontendPath = Join-Path $PROJECT_ROOT "frontend"
    Set-Location $frontendPath
    
    Print-Step "[>]" "Starting Frontend..." "info"
    if (-not (Test-Path "node_modules")) { 
        Write-Host "Installing dependencies..." -ForegroundColor Yellow
        npm install 
    }
    
    Write-Host "  Frontend: http://localhost:5173" -ForegroundColor Cyan
    Write-Host ""
    
    npm run dev

} elseif ($choice -eq "5") {
    # AI Service Only
    $aiServicePath = Join-Path $PROJECT_ROOT "ai-service"
    
    if (-not (Test-Path $aiServicePath)) {
        Print-Step "[!]" "AI service directory not found!" "error"
        exit 1
    }
    
    Set-Location $aiServicePath
    $pythonVenv = Get-PythonVenvPath $aiServicePath
    
    $aiPort = Get-FreePort @(8765, 8766, 8767)
    if ($aiPort -ne 8765) {
        Print-Step "[!]" "Port 8765 busy. AI service on $aiPort." "warning"
    }
    
    Print-Step "[>]" "Starting AI Service on port $aiPort..." "info"
    Write-Host "  AI Service:  http://localhost:$aiPort" -ForegroundColor Cyan
    Write-Host "  API Docs:    http://localhost:$aiPort/docs" -ForegroundColor Cyan
    Write-Host ""
    
    if ($pythonVenv) {
        $venvActivate = Join-Path (Split-Path $pythonVenv) "Activate.ps1"
        & $venvActivate
        Print-Step "[V]" "Virtual environment activated" "success"
    } else {
        Print-Step "[!]" "No venv found, using system Python" "warning"
    }
    
    python -m uvicorn app.main:app --host 0.0.0.0 --port $aiPort --reload

} elseif ($choice -eq "6") {
    # Install Dependencies
    Print-Step "[+]" "Installing all dependencies..." "info"
    
    # Frontend
    Print-Step "[>]" "Installing Frontend dependencies..." "info"
    Set-Location (Join-Path $PROJECT_ROOT "frontend")
    npm install
    Print-Step "[V]" "Frontend dependencies installed" "success"
    
    # Backend
    Print-Step "[>]" "Compiling Backend..." "info"
    Print-Msg "    Using local repo: $MAVEN_LOCAL_REPO" "Gray"
    Set-Location (Join-Path $PROJECT_ROOT "backend")
    
    $env:MAVEN_OPTS = "-Dmaven.repo.local=$MAVEN_LOCAL_REPO"
    if (Test-Path "mvnw.cmd") { 
        .\mvnw.cmd compile -DskipTests
    } else { 
        mvn compile -DskipTests
    }
    Print-Step "[V]" "Backend compiled" "success"
    
    # AI Service
    $aiServicePath = Join-Path $PROJECT_ROOT "ai-service"
    if (Test-Path $aiServicePath) {
        Print-Step "[>]" "Setting up AI Service..." "info"
        Set-Location $aiServicePath
        
        $pythonVenv = Get-PythonVenvPath $aiServicePath
        
        if (-not $pythonVenv) {
            Print-Step "[.]" "Creating Python virtual environment..." "info"
            python -m venv venv
            $pythonVenv = Join-Path $aiServicePath "venv\Scripts\python.exe"
        }
        
        if ($pythonVenv -and (Test-Path $pythonVenv)) {
            Print-Step "[.]" "Installing Python dependencies..." "info"
            $pipPath = Join-Path (Split-Path $pythonVenv) "pip.exe"
            & $pipPath install -r requirements.txt
            Print-Step "[V]" "AI Service dependencies installed" "success"
        } else {
            Print-Step "[!]" "Could not setup Python venv" "warning"
        }
    }
    
    Write-Host ""
    Print-Step "[V]" "All dependencies installed! Now select [1] to start." "success"

} elseif ($choice -eq "0") {
    exit 0

} elseif ($choice -eq "7") {
    Print-Step "[+]" "Stopping all services (8080/5173/8765)..." "info"
    Kill-Port 8080
    Kill-Port 5173
    Kill-Port 8765
    Print-Step "[V]" "All services stopped." "success"

} else {
    Write-Host "Invalid option" -ForegroundColor Red
}

Write-Host ""
