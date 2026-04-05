param(
    [switch]$UseDevProfile = $true
)

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendRoot = Join-Path $repoRoot 'backend'
$powershellExe = (Get-Command powershell).Source

$companyProfile = $null
if ($UseDevProfile) {
    $companyProfile = 'dev'
}

function Start-NewTerminal {
    param(
        [string]$Title,
        [string]$WorkingDirectory,
        [string]$Command
    )

    $arguments = @(
        '-NoExit'
        '-Command'
        "`$host.UI.RawUI.WindowTitle = '$Title'; Set-Location -LiteralPath '$WorkingDirectory'; $Command"
    )

    Start-Process -FilePath $powershellExe -ArgumentList $arguments
}

function Get-MavenRunCommand {
    param(
        [string]$PomPath,
        [string]$Profile
    )

    if ($Profile) {
        return "mvn -f '$PomPath' spring-boot:run '-Dspring-boot.run.profiles=$Profile'"
    }

    return "mvn -f '$PomPath' spring-boot:run"
}

$serviceCommands = @(
    @{ Title = 'Eureka Service';     WorkingDirectory = (Join-Path $backendRoot 'eureka-service');     Command = (Get-MavenRunCommand -PomPath (Join-Path $backendRoot 'eureka-service\pom.xml') -Profile $null) },
    @{ Title = 'Config Service';      WorkingDirectory = (Join-Path $backendRoot 'config-service');      Command = (Get-MavenRunCommand -PomPath (Join-Path $backendRoot 'config-service\pom.xml') -Profile $null) },
    @{ Title = 'Company Service';     WorkingDirectory = (Join-Path $backendRoot 'company-service');     Command = (Get-MavenRunCommand -PomPath (Join-Path $backendRoot 'company-service\pom.xml') -Profile $companyProfile) },
    @{ Title = 'User Service';        WorkingDirectory = (Join-Path $backendRoot 'user-service');        Command = (Get-MavenRunCommand -PomPath (Join-Path $backendRoot 'user-service\pom.xml') -Profile $null) },
    @{ Title = 'Exchange Service';    WorkingDirectory = (Join-Path $backendRoot 'exchange-service');    Command = (Get-MavenRunCommand -PomPath (Join-Path $backendRoot 'exchange-service\pom.xml') -Profile $null) },
    @{ Title = 'Sector Service';      WorkingDirectory = (Join-Path $backendRoot 'sector-service');      Command = (Get-MavenRunCommand -PomPath (Join-Path $backendRoot 'sector-service\pom.xml') -Profile $null) },
    @{ Title = 'Excel Service';       WorkingDirectory = (Join-Path $backendRoot 'excel-service');       Command = (Get-MavenRunCommand -PomPath (Join-Path $backendRoot 'excel-service\pom.xml') -Profile $null) },
    @{ Title = 'API Gateway Service'; WorkingDirectory = (Join-Path $backendRoot 'api-gateway-service'); Command = (Get-MavenRunCommand -PomPath (Join-Path $backendRoot 'api-gateway-service\pom.xml') -Profile $null) },
    @{ Title = 'Frontend';            WorkingDirectory = (Join-Path $repoRoot 'frontend');                Command = 'npm start' }
)

foreach ($service in $serviceCommands) {
    Start-NewTerminal -Title $service.Title -WorkingDirectory $service.WorkingDirectory -Command $service.Command
}