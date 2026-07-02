@echo off
cd /d D:\sxxm2\backend

set ENV_SET=set MYSQL_ROOT_PASSWORD=parttime@2024&set REDIS_PASSWORD=parttime@2024&set RABBITMQ_USER=admin&set RABBITMQ_PASSWORD=parttime@2024&set JWT_SECRET=parttime-platform-jwt-secret-2024&set AES_SECRET_KEY=cGFydHRpbWUyMDI0cGFydHRpbWUyMDI0cGFydHRpbWU=

echo Starting user-service...
start "user-service" /min cmd /c "%ENV_SET%&java -jar user-service\target\user-service-1.0.0-SNAPSHOT.jar > D:\sxxm2\logs\user-service.log 2>&1"

echo Starting job-service...
start "job-service" /min cmd /c "%ENV_SET%&java -jar job-service\target\job-service-1.0.0-SNAPSHOT.jar > D:\sxxm2\logs\job-service.log 2>&1"

echo Starting admin-service...
start "admin-service" /min cmd /c "%ENV_SET%&java -jar admin-service\target\admin-service-1.0.0-SNAPSHOT.jar > D:\sxxm2\logs\admin-service.log 2>&1"

echo Waiting 3 seconds...
timeout /t 3 /nobreak >nul

echo Starting gateway...
start "gateway" /min cmd /c "%ENV_SET%&java -jar gateway\target\gateway-1.0.0-SNAPSHOT.jar > D:\sxxm2\logs\gateway.log 2>&1"

echo All backend services started.
