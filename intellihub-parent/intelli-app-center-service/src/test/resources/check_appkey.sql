-- 检查 AppKey 是否存在于数据库中
-- AppKey: IH4315340gtRfKPamTHVU4GE

-- 1. 查询应用信息
SELECT 
    id AS '应用ID',
    tenant_id AS '租户ID',
    name AS '应用名称',
    code AS '应用编码',
    app_key AS 'AppKey',
    app_secret AS 'AppSecret',
    status AS '状态',
    expire_time AS '过期时间',
    created_at AS '创建时间',
    updated_at AS '更新时间'
FROM app_info
WHERE app_key = 'IH4315340gtRfKPamTHVU4GE';

-- 2. 查询所有应用（如果上面查询无结果）
SELECT 
    id,
    name,
    app_key,
    app_secret,
    status,
    created_at
FROM app_info
ORDER BY created_at DESC
LIMIT 10;

-- 3. 检查应用的订阅关系
SELECT 
    s.id AS '订阅ID',
    s.app_id AS '应用ID',
    s.api_id AS 'API ID',
    s.api_path AS 'API路径',
    s.status AS '状态',
    s.subscribed_at AS '订阅时间',
    a.name AS '应用名称'
FROM app_api_subscription s
LEFT JOIN app_info a ON s.app_id = a.id
WHERE a.app_key = 'IH4315340gtRfKPamTHVU4GE';

-- 4. 统计信息
SELECT 
    COUNT(*) AS '应用总数',
    SUM(CASE WHEN status = 'active' THEN 1 ELSE 0 END) AS '活跃应用数',
    SUM(CASE WHEN app_key IS NOT NULL THEN 1 ELSE 0 END) AS '有AppKey的应用数'
FROM app_info;
