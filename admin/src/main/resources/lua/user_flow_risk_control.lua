-- 设置用户访问频率限制的参数
local username = KEYS[1]  -- 获取 key
local timeWindow = tonumber(ARGV[1]) -- 获取参数：时间窗口，单位：秒

-- 构造 Redis 中存储用户访问次数的键名
local accessKey = "short-link:user-flow-risk-control:" .. username -- 拼接字符串，组成 accessKey

-- 原子递增访问次数，并获取递增后的值
local currentAccessCount = redis.call("INCR", accessKey)

-- 设置键的过期时间
if currentAccessCount == 1 then -- 如果是第一次访问（防止后台管理限流无限刷新）
    redis.call("EXPIRE", accessKey, timeWindow) -- 设置过期时间
end

-- 返回当前访问次数
return currentAccessCount
