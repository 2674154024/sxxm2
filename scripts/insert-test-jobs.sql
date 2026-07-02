USE parttime_platform;

INSERT INTO t_job (id, enterprise_id, job_title, job_type, industry_tag, salary_type, salary_amount, settlement_type, work_address, longitude, latitude, work_time, skill_require, recruit_num, status, is_insured, view_count, apply_count, created_at, updated_at) VALUES
('job-001', 'b75652a2-b41e-4196-822f-582d4fdf0f83', '茶颜悦色门店店员', 'parttime', 'tea', 1, 18.00, 1, '长沙市天心区黄兴南路步行商业街', 112.982200, 28.194200, '[{"day":"周一","start":"09:00","end":"18:00"},{"day":"周二","start":"09:00","end":"18:00"}]', '无要求', 5, 1, 1, 128, 23, NOW(), NOW()),
('job-002', 'b75652a2-b41e-4196-822f-582d4fdf0f83', '星巴克咖啡师', 'parttime', 'tea', 1, 22.00, 2, '长沙市芙蓉区IFS国金中心', 112.978900, 28.196500, '[{"day":"周三","start":"10:00","end":"19:00"}]', '英语基础', 3, 1, 1, 256, 45, NOW(), NOW()),
('job-003', 'b75652a2-b41e-4196-822f-582d4fdf0f83', '优衣库店员', 'parttime', 'retail', 1, 19.00, 3, '长沙市雨花区德思勤城市广场', 113.012300, 28.165400, '[{"day":"周四","start":"10:00","end":"20:00"}]', '无要求', 8, 1, 0, 89, 12, NOW(), NOW()),
('job-004', 'b75652a2-b41e-4196-822f-582d4fdf0f83', '家教老师（数学）', 'parttime', 'tutor', 1, 80.00, 1, '长沙市岳麓区银盆岭', 112.934500, 28.234500, '[{"day":"周六","start":"09:00","end":"11:00"}]', '本科以上', 5, 1, 0, 312, 67, NOW(), NOW()),
('job-005', 'b75652a2-b41e-4196-822f-582d4fdf0f83', '会展兼职礼仪', 'parttime', 'exhibition', 1, 200.00, 1, '长沙国际会展中心', 113.067800, 28.145600, '[{"day":"周日","start":"08:00","end":"18:00"}]', '形象好', 20, 1, 1, 523, 89, NOW(), NOW());

SELECT 'Test jobs inserted successfully' AS result;
