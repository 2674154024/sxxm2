# 启动后端服务脚本
# 使用 -Djwt.secret JVM 参数确保 JWT 密钥可靠传递

$backendDir = "d:\sxxm2\backend"
$logDir = "d:\sxxm2\logs"

# 确保日志目录存在
if (!(Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir -Force
}

# 设置环境变量（用于数据库、Redis等连接）
$env:MYSQL_ROOT_PASSWORD = "parttime@2024"
$env:REDIS_PASSWORD = "parttime@2024"
$env:RABBITMQ_USER = "admin"
$env:RABBITMQ_PASSWORD = "parttime@2024"
$env:JWT_SECRET = "parttime-platform-jwt-secret-2024"
$env:AES_SECRET_KEY = "cGFydHRpbWUyMDI0cGFydHRpbWUyMDI0cGFydHRpbWU="

# JVM 参数
$jvmArgs = "-Djwt.secret=parttime-platform-jwt-secret-2024"

Write-Host "Starting user-service..."
Start-Process -FilePath "java" `
    -ArgumentList "$jvmArgs", "-jar", "$backendDir\user-service\target\user-service-1.0.0-SNAPSHOT.jar" `
    -RedirectStandardOutput "$logDir\user-service.log" `
    -RedirectStandardError "$logDir\user-service-err.log" `
    -WindowStyle Minimized

Write-Host "Starting job-service..."
Start-Process -FilePath "java" `
    -ArgumentList "$jvmArgs", "-jar", "$backendDir\job-service\target\job-service-1.0.0-SNAPSHOT.jar" `
    -RedirectStandardOutput "$logDir\job-service.log" `
    -RedirectStandardError "$logDir\job-service-err.log" `
    -WindowStyle Minimized

Write-Host "Starting admin-service..."
Start-Process -FilePath "java" `
    -ArgumentList "$jvmArgs", "-jar", "$backendDir\admin-service\target\admin-service-1.0.0-SNAPSHOT.jar" `
    -RedirectStandardOutput "$logDir\admin-service.log" `
    -RedirectStandardError "$logDir\admin-service-err.log" `
    -WindowStyle Minimized

Write-Host "Waiting 5 seconds for services to start..."
Start-Sleep -Seconds 5

Write-Host "Starting gateway..."
Start-Process -FilePath "java" `
    -ArgumentList "$jvmArgs", "-jar", "$backendDir\gateway\target\gateway-1.0.0-SNAPSHOT.jar" `
    -RedirectStandardOutput "$logDir\gateway.log" `
    -RedirectStandardError "$logDir\gateway-err.log" `
    -WindowStyle Minimized

Write-Host "All backend services started."
Write-Host "Waiting 15 seconds for all services to be ready..."
Start-Sleep -Seconds 15
Write-Host "Done. Check logs in $logDir"
