local user_key = KEYS[1]

-- Arguments
local refill_rate_per_minute = tonumber(ARGV[1])
local bucket_capacity = tonumber(ARGV[2])
local current_time = tonumber(ARGV[3])
local requested_tokens = 1 -- Each request costs 1 token

local current_state = redis.call('HMGET', user_key, 'tokens', 'timestamp')
local last_tokens = tonumber(current_state[1])
local last_refill_time = tonumber(current_state[2])

-- If the user is new, initialize their bucket state
if last_tokens == nil then
    last_tokens = bucket_capacity
    last_refill_time = current_time
end

local elapsed_time = current_time - last_refill_time
if elapsed_time > 0 then
    -- Convert refill rate from per-minute to per-second
    local refill_rate_per_second = refill_rate_per_minute / 60
    local tokens_to_add = math.floor(elapsed_time * refill_rate_per_second)
    if tokens_to_add > 0 then
        last_tokens = math.min(last_tokens + tokens_to_add, bucket_capacity)
        last_refill_time = current_time
    end
end

local allowed = 0 -- 0 means deny
-- If there are enough tokens, consume one and allow the request
if last_tokens >= requested_tokens then
    last_tokens = last_tokens - requested_tokens
    allowed = 1 -- 1 means allow
end

redis.call('HMSET', user_key, 'tokens', last_tokens, 'timestamp', last_refill_time)
redis.call('EXPIRE', user_key, 120) -- Expires after 2 minutes of inactivity

-- Return 1 if allowed, 0 if denied
return allowed