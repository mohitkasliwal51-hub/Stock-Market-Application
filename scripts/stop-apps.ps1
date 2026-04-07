$ErrorActionPreference = 'SilentlyContinue'

$ports = 8080, 4200, 8090, 8761, 8082, 8083, 8084, 8085, 8086, 8087, 8088, 8089, 8091
$pids = Get-NetTCPConnection -LocalPort $ports -State Listen -ErrorAction SilentlyContinue |
  Select-Object -ExpandProperty OwningProcess -Unique

if ($pids) {
  Stop-Process -Id $pids -Force -ErrorAction SilentlyContinue
}
