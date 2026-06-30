CREATE DATABASE IF NOT EXISTS parttime_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE parttime_platform;

CREATE TABLE IF NOT EXISTS t_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operator_id VARCHAR(64) NOT NULL,
    module VARCHAR(64) NOT NULL,
    action VARCHAR(64) NOT NULL,
    request_params TEXT,
    ip_address VARCHAR(64),
    client_type VARCHAR(32),
    trace_id VARCHAR(64) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_operator_id (operator_id),
    INDEX idx_module (module),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_student (
    id VARCHAR(64) PRIMARY KEY,
    real_name VARCHAR(64),
    student_no VARCHAR(64),
    school_id BIGINT,
    id_card_encrypt TEXT,
    phone_encrypt TEXT,
    avatar_url VARCHAR(256),
    verify_status TINYINT DEFAULT 0 COMMENT '0未认证 1审核中 2已认证 3认证失败',
    credit_score INT DEFAULT 100,
    available_time JSON,
    skill_tags TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_verify_status (verify_status),
    INDEX idx_credit_score (credit_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_enterprise (
    id VARCHAR(64) PRIMARY KEY,
    enterprise_name VARCHAR(256) NOT NULL,
    credit_code VARCHAR(64),
    business_license_url VARCHAR(256),
    legal_person VARCHAR(64),
    contact_name VARCHAR(64),
    contact_phone_encrypt TEXT,
    verify_status TINYINT DEFAULT 0 COMMENT '0未认证 1审核中 2已认证',
    is_certified TINYINT DEFAULT 0,
    credit_score INT DEFAULT 100,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_verify_status (verify_status),
    INDEX idx_credit_score (credit_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_job (
    id VARCHAR(64) PRIMARY KEY,
    enterprise_id VARCHAR(64) NOT NULL,
    job_title VARCHAR(256) NOT NULL,
    job_type VARCHAR(64),
    industry_tag VARCHAR(64),
    salary_type TINYINT DEFAULT 1 COMMENT '1时薪 2日薪',
    salary_amount DECIMAL(10,2) NOT NULL,
    settlement_type TINYINT DEFAULT 1 COMMENT '1日结 2周结',
    work_address VARCHAR(512),
    longitude DECIMAL(12,8),
    latitude DECIMAL(12,8),
    work_time JSON,
    skill_require TEXT,
    recruit_num INT DEFAULT 1,
    status TINYINT DEFAULT 0 COMMENT '0待审核 1已发布 2已下架',
    is_insured TINYINT DEFAULT 0,
    view_count INT DEFAULT 0,
    apply_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_enterprise_id (enterprise_id),
    INDEX idx_status (status),
    INDEX idx_industry_tag (industry_tag),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_job_apply (
    id VARCHAR(64) PRIMARY KEY,
    student_id VARCHAR(64) NOT NULL,
    job_id VARCHAR(64) NOT NULL,
    apply_status TINYINT DEFAULT 0 COMMENT '0已投递 1待面试 2已录用 3已拒绝',
    interview_time DATETIME,
    sign_status TINYINT DEFAULT 0 COMMENT '0未签约 1待签署 2已签署',
    agreement_id VARCHAR(64),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_student_id (student_id),
    INDEX idx_job_id (job_id),
    INDEX idx_apply_status (apply_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_clock_record (
    id VARCHAR(64) PRIMARY KEY,
    student_id VARCHAR(64) NOT NULL,
    job_id VARCHAR(64) NOT NULL,
    clock_type TINYINT NOT NULL COMMENT '1签到 2签退',
    longitude DECIMAL(12,8),
    latitude DECIMAL(12,8),
    clock_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_abnormal TINYINT DEFAULT 0,
    abnormal_reason VARCHAR(512),
    work_date DATE NOT NULL,
    INDEX idx_student_id (student_id),
    INDEX idx_job_id (job_id),
    INDEX idx_work_date (work_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_salary_flow (
    id VARCHAR(64) PRIMARY KEY,
    student_id VARCHAR(64) NOT NULL,
    job_id VARCHAR(64) NOT NULL,
    enterprise_id VARCHAR(64) NOT NULL,
    work_date DATE NOT NULL,
    work_hours DECIMAL(5,2) NOT NULL,
    hourly_wage DECIMAL(10,2) NOT NULL,
    gross_amount DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2) DEFAULT 0,
    net_amount DECIMAL(10,2) NOT NULL,
    settlement_status TINYINT DEFAULT 0 COMMENT '0待确认 1待发放 2发放中 3已到账 4已驳回',
    flow_id VARCHAR(64) UNIQUE,
    agreement_id VARCHAR(64),
    invoice_id VARCHAR(64),
    trace_id VARCHAR(64),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_student_id (student_id),
    INDEX idx_enterprise_id (enterprise_id),
    INDEX idx_settlement_status (settlement_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_salary_escrow (
    id VARCHAR(64) PRIMARY KEY,
    enterprise_id VARCHAR(64) NOT NULL,
    job_id VARCHAR(64),
    total_amount DECIMAL(12,2) NOT NULL,
    paid_amount DECIMAL(12,2) DEFAULT 0,
    freeze_amount DECIMAL(12,2) DEFAULT 0,
    available_amount DECIMAL(12,2) NOT NULL,
    status TINYINT DEFAULT 0 COMMENT '0待支付 1已托管',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_enterprise_id (enterprise_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_complaint (
    id VARCHAR(64) PRIMARY KEY,
    complainant_id VARCHAR(64) NOT NULL,
    defendant_id VARCHAR(64) NOT NULL,
    defendant_type TINYINT NOT NULL COMMENT '1学生 2企业',
    job_id VARCHAR(64),
    complaint_type VARCHAR(64),
    complaint_content TEXT,
    evidence_urls TEXT,
    status TINYINT DEFAULT 0 COMMENT '0待审核 1处理中 2已解决 3已驳回',
    handle_result VARCHAR(512),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_complainant_id (complainant_id),
    INDEX idx_defendant_id (defendant_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_notification (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    user_type TINYINT NOT NULL COMMENT '1学生 2企业 3管理员',
    notification_type VARCHAR(64),
    title VARCHAR(256),
    content TEXT,
    is_read TINYINT DEFAULT 0,
    link_url VARCHAR(512),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_admin (
    id VARCHAR(64) PRIMARY KEY,
    username VARCHAR(64) UNIQUE NOT NULL,
    password VARCHAR(256) NOT NULL,
    real_name VARCHAR(64),
    role_type TINYINT NOT NULL COMMENT '1审核 2风控 3运营 4财务 5超级',
    status TINYINT DEFAULT 1,
    last_login_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_role_type (role_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_job_match_log (
    id VARCHAR(64) PRIMARY KEY,
    student_id VARCHAR(64) NOT NULL,
    job_id VARCHAR(64) NOT NULL,
    match_score INT NOT NULL,
    skill_score INT,
    time_score INT,
    distance_score INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_student_id (student_id),
    INDEX idx_job_id (job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_message (
    id VARCHAR(64) PRIMARY KEY,
    from_id VARCHAR(64) NOT NULL,
    to_id VARCHAR(64) NOT NULL,
    content TEXT,
    message_type VARCHAR(32) DEFAULT 'text',
    timestamp BIGINT NOT NULL,
    is_read TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_from_id (from_id),
    INDEX idx_to_id (to_id),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(128) UNIQUE NOT NULL,
    config_value TEXT,
    config_desc VARCHAR(512),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;