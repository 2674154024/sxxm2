#!/bin/bash
# Start all core backend services for local development
set -e

export MYSQL_ROOT_PASSWORD='parttime@2024'
export REDIS_PASSWORD='parttime@2024'
export RABBITMQ_USER='admin'
export RABBITMQ_PASSWORD='parttime@2024'
export JWT_SECRET='parttime-platform-jwt-secret-2024'
export AES_SECRET_KEY='cGFydHRpbWUyMDI0cGFydHRpbWUyMDI0cGFydHRpbWU='

cd /d/sxxm2/backend
rm -f /d/sxxm2/logs/*.log
mkdir -p /d/sxxm2/logs

echo "=== Starting Backend Services ==="

echo "[1/3] user-service (8081)..."
nohup java -jar user-service/target/user-service-1.0.0-SNAPSHOT.jar \
    --spring.datasource.password="${MYSQL_ROOT_PASSWORD}" \
    --spring.redis.password="${REDIS_PASSWORD}" \
    --spring.rabbitmq.password="${RABBITMQ_PASSWORD}" \
    > /d/sxxm2/logs/user-service.log 2>&1 &

echo "[2/3] job-service (8082)..."
nohup java -jar job-service/target/job-service-1.0.0-SNAPSHOT.jar \
    --spring.datasource.password="${MYSQL_ROOT_PASSWORD}" \
    --spring.redis.password="${REDIS_PASSWORD}" \
    > /d/sxxm2/logs/job-service.log 2>&1 &

echo "[3/3] gateway (8080)..."
nohup java -jar gateway/target/gateway-1.0.0-SNAPSHOT.jar \
    --spring.redis.password="${REDIS_PASSWORD}" \
    > /d/sxxm2/logs/gateway.log 2>&1 &

echo "Waiting for startup..."
sleep 20

echo ""
echo "=== Health Check ==="
for svc in gateway:8080 user-service:8081 job-service:8082; do
    name="${svc%%:*}"
    port="${svc##*:}"
    status=$(tail -1 /d/sxxm2/logs/$name.log 2>/dev/null | grep -o "Started.*seconds" || echo "NOT STARTED")
    echo "  $name (:$port): $status"
done

echo ""
echo "=== Smoke Test (via Gateway :8080) ==="
result=$(curl -s -X POST http://localhost:8080/v1/auth/sms-code \
    -H "Content-Type: application/json" \
    -d '{"phone":"13800000001"}' 2>/dev/null)
echo "  POST /v1/auth/sms-code: $(echo $result | head -c 60)"

result=$(curl -s -X POST http://localhost:8080/v1/auth/sms-code \
    -H "Content-Type: application/json" \
    -d '{"phone":"13800000001"}' 2>/dev/null)
echo "  Rate-limit test: $(echo $result | head -c 60)"

result=$(curl -s "http://localhost:8080/v1/public/job/list?page=1&size=2" 2>/dev/null)
echo "  GET /v1/public/job/list: $(echo $result | head -c 60)"

echo ""
echo "=== Done ==="
echo "  Gateway: http://localhost:8080 (all routes)"
echo "  PC Web:  cd pc-web && npm run dev  (http://localhost:5173)"
echo "  H5 Web:  cd h5-web && npm run dev  (http://localhost:5174)"
