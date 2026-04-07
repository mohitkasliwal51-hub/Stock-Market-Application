param(
  [int]$TimeoutSeconds = 300
)

$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$backendRoot = Join-Path $root 'backend'
$frontendRoot = Join-Path $root 'frontend'
$logRoot = Join-Path $root 'logs/live-verification'

$appPorts = @(8080, 4200, 8090, 8761, 8082, 8083, 8084, 8085, 8086, 8089)

New-Item -ItemType Directory -Force -Path $logRoot | Out-Null

function Stop-ExistingListeners {
  $pids = Get-NetTCPConnection -LocalPort $appPorts -State Listen -ErrorAction SilentlyContinue |
    Select-Object -ExpandProperty OwningProcess -Unique

  if ($pids) {
    Write-Host "Stopping existing listeners on app ports: $($pids -join ', ')"
    Stop-Process -Id $pids -Force
    Start-Sleep -Seconds 2
  }
}

function Start-ServiceProcess {
  param(
    [string]$Name,
    [string]$WorkingDirectory,
    [string[]]$Arguments
  )

  $stdout = Join-Path $logRoot "$Name.out.log"
  $stderr = Join-Path $logRoot "$Name.err.log"

  Start-Process -FilePath 'mvn.cmd' -WorkingDirectory $WorkingDirectory -ArgumentList $Arguments -WindowStyle Hidden -RedirectStandardOutput $stdout -RedirectStandardError $stderr | Out-Null
}

function Start-FrontendProcess {
  $stdout = Join-Path $logRoot 'frontend.out.log'
  $stderr = Join-Path $logRoot 'frontend.err.log'

  Start-Process -FilePath 'npm.cmd' -WorkingDirectory $frontendRoot -ArgumentList @('run', 'start') -WindowStyle Hidden -RedirectStandardOutput $stdout -RedirectStandardError $stderr | Out-Null
}

function Wait-ForHttp {
  param(
    [string]$Url,
    [string]$Name
  )

  $deadline = (Get-Date).AddSeconds($TimeoutSeconds)

  while ((Get-Date) -lt $deadline) {
    try {
      Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 5 | Out-Null
      Write-Host "$Name is ready: $Url"
      return
    }
    catch {
      Start-Sleep -Seconds 2
    }
  }

  throw "$Name did not become ready within $TimeoutSeconds seconds at $Url"
}

function Wait-ForPort {
  param(
    [int]$Port,
    [string]$Name
  )

  $deadline = (Get-Date).AddSeconds($TimeoutSeconds)

  while ((Get-Date) -lt $deadline) {
    $connection = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($connection) {
      Write-Host "$Name is ready on port $Port"
      return
    }

    Start-Sleep -Seconds 2
  }

  throw "$Name did not start listening on port $Port within $TimeoutSeconds seconds"
}

Write-Host 'Starting backend services...'
Stop-ExistingListeners

Start-ServiceProcess -Name 'config-service' -WorkingDirectory (Join-Path $backendRoot 'config-service') -Arguments @('-f', 'pom.xml', 'spring-boot:run')
Start-ServiceProcess -Name 'eureka-service' -WorkingDirectory (Join-Path $backendRoot 'eureka-service') -Arguments @('-f', 'pom.xml', 'spring-boot:run')

Write-Host 'Waiting for core infrastructure to become ready...'
Wait-ForHttp -Url 'http://localhost:8090/actuator/health' -Name 'Config service'
Wait-ForHttp -Url 'http://localhost:8761/actuator/health' -Name 'Eureka service'

Start-ServiceProcess -Name 'exchange-service' -WorkingDirectory (Join-Path $backendRoot 'exchange-service') -Arguments @('-f', 'pom.xml', 'spring-boot:run')
Start-ServiceProcess -Name 'sector-service' -WorkingDirectory (Join-Path $backendRoot 'sector-service') -Arguments @('-f', 'pom.xml', 'spring-boot:run')
Start-ServiceProcess -Name 'company-service' -WorkingDirectory (Join-Path $backendRoot 'company-service') -Arguments @('-f', 'pom.xml', 'spring-boot:run')
Start-ServiceProcess -Name 'user-service' -WorkingDirectory (Join-Path $backendRoot 'user-service') -Arguments @('-f', 'pom.xml', 'spring-boot:run')
Start-ServiceProcess -Name 'excel-service' -WorkingDirectory (Join-Path $backendRoot 'excel-service') -Arguments @('-f', 'pom.xml', 'spring-boot:run')
Start-ServiceProcess -Name 'market-service' -WorkingDirectory (Join-Path $backendRoot 'market-service') -Arguments @('-f', 'pom.xml', 'spring-boot:run')
Start-ServiceProcess -Name 'api-gateway-service' -WorkingDirectory (Join-Path $backendRoot 'api-gateway-service') -Arguments @('-f', 'pom.xml', 'spring-boot:run')

Write-Host 'Starting frontend...'
Start-FrontendProcess

Write-Host 'Waiting for services to become ready...'
Wait-ForPort -Port 8082 -Name 'Exchange service'
Wait-ForPort -Port 8083 -Name 'Sector service'
Wait-ForPort -Port 8084 -Name 'Company service'
Wait-ForPort -Port 8085 -Name 'Excel service'
Wait-ForPort -Port 8086 -Name 'User service'
Wait-ForPort -Port 8089 -Name 'Market service'
Wait-ForPort -Port 8080 -Name 'API gateway'
Wait-ForHttp -Url 'http://localhost:4200/' -Name 'Frontend'

Write-Host 'All services are running.'
