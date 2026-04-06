param(
  [string]$TestList = "GatewaySmokeTest,GatewayPerformanceTest,GatewaySecurityTest,GatewayContractTest,GatewayIntegrationTest,EndToEndServiceWorkflowTest"
)

$mvnArgs = @("-Dtest=$TestList", "test")
& mvn @mvnArgs
exit $LASTEXITCODE
