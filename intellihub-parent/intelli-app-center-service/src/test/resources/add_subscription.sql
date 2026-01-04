-- 为应用添加 API 订阅
-- AppKey: IH4315340gtRfKPamTHVU4GE
-- API: GET /open/user/{userId}

-- 1. 查询应用 ID
SELECT 
    id AS app_id,
    name AS app_name,
    app_key
FROM app_info
WHERE app_key = 'IH4315340gtRfKPamTHVU4GE';

-- 2. 查询 API ID
SELECT 
    id AS api_id,
    name AS api_name,
    path,
    method
FROM open_api
WHERE path = '/open/user/{userId}'
  AND method = 'GET';

-- 3. 添加订阅关系（请先执行上面两个查询，获取 app_id 和 api_id）
-- 将下面的 'YOUR_APP_ID' 和 'YOUR_API_ID' 替换为实际的 ID

INSERT INTO app_api_subscription (
    id,
    app_id,
    api_id,
    api_path,
    status,
    subscribed_at,
    created_at,
    updated_at,
    deleted
) VALUES (
    REPLACE(UUID(), '-', ''),  -- 生成唯一 ID
    'YOUR_APP_ID',              -- 替换为实际的应用 ID
    'YOUR_API_ID',              -- 替换为实际的 API ID（例如：2007696330425032706）
    '/open/user/{userId}',      -- API 路径
    'active',                   -- 状态：active
    NOW(),                      -- 订阅时间
    NOW(),                      -- 创建时间
    NOW(),                      -- 更新时间
    0                           -- 未删除
);

-- 4. 验证订阅是否添加成功
SELECT 
    s.id,
    s.app_id,
    s.api_id,
    s.api_path,
    s.status,
    s.subscribed_at,
    a.name AS app_name,
    a.app_key
FROM app_api_subscription s
LEFT JOIN app_info a ON s.app_id = a.id
WHERE a.app_key = 'IH4315340gtRfKPamTHVU4GE'
  AND s.status = 'active';
