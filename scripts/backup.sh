#!/bin/bash

set -e

BACKUP_DIR="/data/backup"
MYSQL_HOST="localhost"
MYSQL_PORT="3306"
MYSQL_USER="root"
MYSQL_PASSWORD=""
MYSQL_DATABASE="parttime_platform"

OSS_ENDPOINT=""
OSS_BUCKET=""
OSS_ACCESS_KEY_ID=""
OSS_ACCESS_KEY_SECRET=""

RETENTION_DAYS=30

ALERT_WEBHOOK=""
ALERT_EMAIL=""

LOG_FILE="${BACKUP_DIR}/backup.log"

info() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] INFO: $1" >> "${LOG_FILE}"
}

error() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: $1" >> "${LOG_FILE}"
}

send_alert() {
    local subject="$1"
    local message="$2"
    
    if [ -n "${ALERT_WEBHOOK}" ]; then
        curl -s -X POST "${ALERT_WEBHOOK}" \
            -H "Content-Type: application/json" \
            -d "{\"subject\": \"${subject}\", \"message\": \"${message}\"}" || true
    fi
    
    if [ -n "${ALERT_EMAIL}" ]; then
        echo "${message}" | mail -s "${subject}" "${ALERT_EMAIL}" || true
    fi
}

init() {
    mkdir -p "${BACKUP_DIR}/full"
    mkdir -p "${BACKUP_DIR}/binlog"
    
    if [ ! -f "/usr/local/bin/ossutil" ]; then
        error "ossutil not found, please install it first"
        exit 1
    fi
}

full_backup() {
    info "Starting full MySQL backup..."
    
    local timestamp=$(date '+%Y%m%d_%H%M%S')
    local backup_file="${BACKUP_DIR}/full/mysql_full_${timestamp}.sql.gz"
    local binlog_file="${BACKUP_DIR}/full/binlog_position_${timestamp}.txt"
    
    if ! mysqldump -h "${MYSQL_HOST}" -P "${MYSQL_PORT}" -u "${MYSQL_USER}" -p"${MYSQL_PASSWORD}" \
        --single-transaction --master-data=2 --flush-logs \
        "${MYSQL_DATABASE}" | gzip > "${backup_file}"; then
        error "Full backup failed"
        send_alert "MySQL全量备份失败" "全量备份失败，请检查数据库状态"
        return 1
    fi
    
    mysql -h "${MYSQL_HOST}" -P "${MYSQL_PORT}" -u "${MYSQL_USER}" -p"${MYSQL_PASSWORD}" \
        -e "SHOW MASTER STATUS" > "${binlog_file}"
    
    info "Full backup completed: ${backup_file}"
    
    if ! /usr/local/bin/ossutil cp "${backup_file}" "oss://${OSS_BUCKET}/backup/full/"; then
        error "Failed to upload full backup to OSS"
        send_alert "MySQL全量备份上传失败" "备份文件上传OSS失败"
        return 1
    fi
    
    if ! /usr/local/bin/ossutil cp "${binlog_file}" "oss://${OSS_BUCKET}/backup/full/"; then
        error "Failed to upload binlog position to OSS"
        send_alert "MySQL全量备份binlog位置上传失败" "binlog位置文件上传OSS失败"
        return 1
    fi
    
    info "Full backup uploaded to OSS"
    return 0
}

binlog_backup() {
    info "Starting binlog incremental backup..."
    
    local timestamp=$(date '+%Y%m%d_%H%M%S')
    local binlog_dir=$(mysql -h "${MYSQL_HOST}" -P "${MYSQL_PORT}" -u "${MYSQL_USER}" -p"${MYSQL_PASSWORD}" \
        -e "SHOW VARIABLES LIKE 'log_bin_basename'" 2>/dev/null | grep -v Variable_name | awk '{print $1}')
    
    if [ -z "${binlog_dir}" ]; then
        error "Binlog directory not found"
        send_alert "Binlog备份失败" "无法获取binlog目录，请检查MySQL配置"
        return 1
    fi
    
    local binlog_parent=$(dirname "${binlog_dir}")
    local latest_binlog=$(ls -t "${binlog_parent}"/*.00000* 2>/dev/null | head -1)
    
    if [ -z "${latest_binlog}" ]; then
        error "No binlog files found"
        send_alert "Binlog备份失败" "未找到binlog文件"
        return 1
    fi
    
    local binlog_filename=$(basename "${latest_binlog}")
    local backup_file="${BACKUP_DIR}/binlog/${binlog_filename}_${timestamp}.gz"
    
    if ! gzip -c "${latest_binlog}" > "${backup_file}"; then
        error "Binlog backup failed"
        send_alert "Binlog备份失败" "binlog压缩备份失败"
        return 1
    fi
    
    info "Binlog backup completed: ${backup_file}"
    
    if ! /usr/local/bin/ossutil cp "${backup_file}" "oss://${OSS_BUCKET}/backup/binlog/"; then
        error "Failed to upload binlog to OSS"
        send_alert "Binlog上传失败" "binlog文件上传OSS失败"
        return 1
    fi
    
    info "Binlog uploaded to OSS"
    return 0
}

cleanup_local() {
    info "Cleaning up local backups older than ${RETENTION_DAYS} days..."
    
    find "${BACKUP_DIR}/full" -type f -mtime +${RETENTION_DAYS} -delete
    find "${BACKUP_DIR}/binlog" -type f -mtime +${RETENTION_DAYS} -delete
    
    info "Local cleanup completed"
}

cleanup_oss() {
    info "Cleaning up OSS backups older than ${RETENTION_DAYS} days..."
    
    local cutoff_date=$(date -d "-${RETENTION_DAYS} days" '+%Y-%m-%d')
    
    /usr/local/bin/ossutil ls "oss://${OSS_BUCKET}/backup/full/" | grep -v "Directory" | \
        while read -r line; do
            local file_date=$(echo "${line}" | awk '{print $2}' | cut -d'/' -f6 | cut -d'_' -f3 | cut -c1-8)
            if [ "${file_date}" \< "${cutoff_date}" ]; then
                local file_path=$(echo "${line}" | awk '{print $4}')
                /usr/local/bin/ossutil rm "${file_path}" || true
            fi
        done
    
    /usr/local/bin/ossutil ls "oss://${OSS_BUCKET}/backup/binlog/" | grep -v "Directory" | \
        while read -r line; do
            local file_date=$(echo "${line}" | awk '{print $2}' | cut -d'/' -f6 | cut -d'_' -f2 | cut -c1-8)
            if [ "${file_date}" \< "${cutoff_date}" ]; then
                local file_path=$(echo "${line}" | awk '{print $4}')
                /usr/local/bin/ossutil rm "${file_path}" || true
            fi
        done
    
    info "OSS cleanup completed"
}

main() {
    init
    
    case "${1:-}" in
        full)
            full_backup
            ;;
        binlog)
            binlog_backup
            ;;
        cleanup)
            cleanup_local
            cleanup_oss
            ;;
        *)
            echo "Usage: $0 {full|binlog|cleanup}"
            exit 1
            ;;
    esac
}

main "$@"