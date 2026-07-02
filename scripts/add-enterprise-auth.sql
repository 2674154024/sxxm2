USE parttime_platform;

ALTER TABLE t_enterprise 
ADD COLUMN username VARCHAR(64) UNIQUE NOT NULL DEFAULT '' COMMENT '登录用户名' AFTER id,
ADD COLUMN password VARCHAR(256) NOT NULL DEFAULT '' COMMENT '登录密码(MD5加密)' AFTER username;

CREATE INDEX idx_enterprise_username ON t_enterprise(username);
