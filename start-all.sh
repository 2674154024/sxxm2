#!/bin/bash
# =============================================================================
# 长沙大学生兼职平台 — 一键启动脚本
# =============================================================================
# 用法: bash start-all.sh [ start | stop | status | restart ]
# =============================================================================
set -e

PROJECT_DIR="/d/sxxm2"
LOG_DIR="${PROJECT_DIR}/logs"

# 环境变量
export MYSQL_ROOT_PASSWORD='parttime@2024'
export REDIS_PASSWORD='parttime@2024'
export RABBITMQ_USER='admin'
export RABBITMQ_PASSWORD='parttime@2024'
export JWT_SECRET='parttime-platform-jwt-secret-2024'
export AES_SECRET_KEY='cGFydHRpbWUyMDI0cGFydHRpbWUyMDI0cGFydHRpbWU='

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

info()  { echo -e "${GREEN}[INFO]${NC}  $1"; }
warn()  { echo -e "${YELLOW}[WARN]${NC}  $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; }
step()  { echo -e "${CYAN}[STEP]${NC} $1"; }

check_prerequisites() {
    info "检查运行环境..."
    local missing=0

    for cmd in docker java node npm; do
        if command -v $cmd &>/dev/null; then
            info "  ✓ $cmd ($($cmd --version 2>&1 | head -1 | cut -c1-30))"
        else
            error "  ✗ $cmd 未安装"
            missing=1
        fi
    done

    # 检查端口
    for port in 3307 6379 9200 5672 8080 8081 8082 5173 5174; do
        if netstat -ano 2>/dev/null | grep -q ":$port " | grep -q LISTENING; then
            warn "  端口 $port 已被占用，可能导致启动失败"
        fi
    done

    if [ $missing -eq 1 ]; then
        error "缺少必要工具，请安装后重试"
        exit 1
    fi
}

start_infra() {
    step "启动基础设施容器..."
    cd "${PROJECT_DIR}"

    local infra_list="mysql redis elasticsearch rabbitmq"
    local all_up=true

    for svc in $infra_list; do
        if docker ps --filter name="parttime-${svc}" --format '{{.Names}}' | grep -q .; then
            info "  ${svc} 已在运行"
        else
            info "  启动 ${svc}..."
            docker compose up -d "${svc}" 2>&1 | tail -1
        fi
    done

    info "等待 MySQL 就绪..."
    for i in $(seq 1 30); do
        local health=$(docker inspect parttime-mysql --format='{{.State.Health.Status}}' 2>/dev/null || echo "unknown")
        if [ "$health" = "healthy" ]; then
            info "  MySQL 已就绪"
            break
        fi
        sleep 2
    done
}

start_backend() {
    step "启动后端服务..."
    cd "${PROJECT_DIR}/backend"

    mkdir -p "${LOG_DIR}"

    local services=(
        "user-service:8081:--spring.datasource.password=${MYSQL_ROOT_PASSWORD} --spring.redis.password=${REDIS_PASSWORD} --spring.rabbitmq.password=${RABBITMQ_PASSWORD}"
        "job-service:8082:--spring.datasource.password=${MYSQL_ROOT_PASSWORD} --spring.redis.password=${REDIS_PASSWORD}"
        "gateway:8080:--spring.redis.password=${REDIS_PASSWORD}"
    )

    for svc_info in "${services[@]}"; do
        IFS=':' read -r name port extra_args <<< "$svc_info"
        local jar="target/${name}-1.0.0-SNAPSHOT.jar"

        if [ ! -f "${name}/${jar}" ]; then
            error "  ${name}.jar 不存在，请先执行: cd backend && mvn clean package -DskipTests"
            exit 1
        fi

        if netstat -ano 2>/dev/null | grep -q ":${port} " | grep -q LISTENING; then
            warn "  端口 ${port} 被占用，跳过 ${name}"
            continue
        fi

        info "  启动 ${name} (端口 ${port})..."
        nohup java -jar "${name}/${jar}" ${extra_args} \
            > "${LOG_DIR}/${name}.log" 2>&1 &
        echo "    PID: $!"
    done

    info "等待后端启动 (20秒)..."
    sleep 20

    for svc_info in "${services[@]}"; do
        IFS=':' read -r name port extra_args <<< "$svc_info"
        local status_line=$(tail -1 "${LOG_DIR}/${name}.log" 2>/dev/null | grep -o "Started.*seconds" || echo "")
        if [ -n "$status_line" ]; then
            info "  ${name} (${port}): ✓ ${status_line}"
        else
            error "  ${name} (${port}): ✗ 启动失败，查看 ${LOG_DIR}/${name}.log"
        fi
    done
}

start_frontend() {
    step "启动前端服务..."

    cd "${PROJECT_DIR}/pc-web"
    if [ ! -d "node_modules" ]; then
        info "  安装 PC Web 依赖..."
        npm install --silent 2>&1 | tail -1
    fi
    info "  启动 PC Web (http://localhost:5173)..."
    nohup npx vite --port 5173 --host > "${LOG_DIR}/pc-web.log" 2>&1 &
    echo "    PID: $!"

    cd "${PROJECT_DIR}/h5-web"
    if [ ! -d "node_modules" ]; then
        info "  安装 H5 Web 依赖..."
        npm install --silent 2>&1 | tail -1
    fi
    if ! netstat -ano 2>/dev/null | grep -q ":5174 " | grep -q LISTENING; then
        info "  启动 H5 Web (http://localhost:5174)..."
        nohup npx vite --port 5174 --host > "${LOG_DIR}/h5-web.log" 2>&1 &
        echo "    PID: $!"
    fi

    sleep 5
}

smoke_test() {
    step "冒烟测试..."
    sleep 2

    local gw="http://localhost:8080"
    local ok=0 fail=0

    echo -n "  POST /v1/auth/sms-code (网关): "
    local result=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${gw}/v1/auth/sms-code" \
        -H "Content-Type: application/json" \
        -d '{"phone":"13800000001"}' 2>/dev/null)
    if [ "$result" = "200" ]; then echo -e "${GREEN}${result}${NC}"; ok=$((ok+1)); else echo -e "${RED}${result}${NC}"; fail=$((fail+1)); fi

    echo -n "  GET  /v1/public/job/list (网关): "
    local result=$(curl -s -o /dev/null -w "%{http_code}" "${gw}/v1/public/job/list?page=1&size=2" 2>/dev/null)
    if [ "$result" = "200" ]; then echo -e "${GREEN}${result}${NC}"; ok=$((ok+1)); else echo -e "${YELLOW}${result}${NC}"; ok=$((ok+1)); fi

    echo -n "  Web PC (http://localhost:5173): "
    local result=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:5173" 2>/dev/null)
    if [ "$result" = "200" ]; then echo -e "${GREEN}${result}${NC}"; ok=$((ok+1)); else echo -e "${RED}${result}${NC}"; fail=$((fail+1)); fi

    echo -n "  H5 Web (http://localhost:5174): "
    local result=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:5174" 2>/dev/null)
    if [ "$result" = "200" ]; then echo -e "${GREEN}${result}${NC}"; ok=$((ok+1)); else echo -e "${YELLOW}${result}${NC}"; ok=$((ok+1)); fi

    echo ""
    info "测试完成: ${ok} 通过"
    if [ $fail -gt 0 ]; then
        warn "${fail} 项失败，请检查日志"
    fi
}

stop_all() {
    step "停止所有服务..."

    info "停止后端 (Java)..."
    taskkill //F //IM java.exe 2>/dev/null && info "  Java 已停止" || warn "  无 Java 进程"

    info "停止前端 (Node)..."
    taskkill //F //IM node.exe 2>/dev/null && info "  Node 已停止" || warn "  无 Node 进程"

    info "停止基础设施 (Docker)..."
    cd "${PROJECT_DIR}"
    docker compose stop 2>/dev/null && info "  Docker 容器已停止" || warn "  Docker 停止失败"
}

status_all() {
    echo ""
    echo "=========================================="
    echo "  服务运行状态"
    echo "=========================================="

    echo ""
    echo "--- 基础设施 (Docker) ---"
    for svc in mysql redis elasticsearch rabbitmq; do
        local status=$(docker ps --filter name="parttime-${svc}" --format '{{.Status}}' 2>/dev/null)
        if [ -n "$status" ]; then
            echo -e "  ${svc}: ${GREEN}运行中${NC} (${status})"
        else
            echo -e "  ${svc}: ${RED}未运行${NC}"
        fi
    done

    echo ""
    echo "--- 后端服务 ---"
    for svc_info in "gateway:8080" "user-service:8081" "job-service:8082"; do
        IFS=':' read -r name port <<< "$svc_info"
        if netstat -ano 2>/dev/null | grep -q ":${port} " | grep -q LISTENING; then
            echo -e "  ${name} (${port}): ${GREEN}运行中${NC}"
        else
            echo -e "  ${name} (${port}): ${RED}未运行${NC}"
        fi
    done

    echo ""
    echo "--- 前端服务 ---"
    for svc_info in "PC-Web:5173" "H5-Web:5174"; do
        IFS=':' read -r name port <<< "$svc_info"
        if netstat -ano 2>/dev/null | grep -q ":${port} " | grep -q LISTENING; then
            echo -e "  ${name} (${port}): ${GREEN}运行中${NC}"
        else
            echo -e "  ${name} (${port}): ${RED}未运行${NC}"
        fi
    done
    echo ""
}

show_urls() {
    echo ""
    echo "=========================================="
    echo "  浏览器访问地址"
    echo "=========================================="
    echo ""
    echo "  前端页面:"
    echo "    PC 管理后台:   http://localhost:5173"
    echo "    H5 移动端:     http://localhost:5174"
    echo ""
    echo "  后端网关 (所有 API 统一入口):"
    echo "    Gateway:       http://localhost:8080"
    echo ""
    echo "  基础设施管理界面:"
    echo "    RabbitMQ:      http://localhost:15672  (admin / parttime@2024)"
    echo "    Elasticsearch: http://localhost:9200"
    echo ""
    echo "  单服务直连 (调试用):"
    echo "    user-service:  http://localhost:8081"
    echo "    job-service:   http://localhost:8082"
    echo ""
}

# ===== 主入口 =====
case "${1:-start}" in
    start)
        echo ""
        echo "=========================================="
        echo "  长沙大学生兼职平台 — 一键启动"
        echo "=========================================="
        check_prerequisites
        start_infra
        start_backend
        start_frontend
        smoke_test
        show_urls
        info "启动完成！打开浏览器访问 http://localhost:5173"
        ;;
    stop)
        stop_all
        info "所有服务已停止"
        ;;
    restart)
        stop_all
        sleep 2
        bash "$0" start
        ;;
    status)
        status_all
        ;;
    *)
        echo "用法: bash start-all.sh [start|stop|restart|status]"
        exit 1
        ;;
esac
