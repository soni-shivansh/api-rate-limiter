local tokens_key = KEYS[1]
local timestamp_key = KEYS[2]

-- Arguments that will be passed
local refill_rate = tonumber(ARGV[1])
local bucket_capacity = tonumber(ARGV[2])
local current_time = tonumber(ARGV[3])
local requested_tokens = 1 -- Each request costs 1 token

-- Get the current values from Redis
local last_tokens = tonumber(redis.call('get', tokens_key))
local last_refill_time = tonumber(redis.call('get', timestamp_key))

-- If the user is new, initialize their bucket
if last_tokens == nil then
    last_tokens = bucket_capacity
    last_refill_time = current_time
end

-- Calculate elapsed time and tokens to add
local elapsed_time = current_time - last_refill_time
if elapsed_time > 0 then
    local tokens_to_add = math.floor(elapsed_time * refill_rate / 60)
    -- Add new tokens, but not more than the bucket capacity
    last_tokens = math.min(last_tokens + tokens_to_add, bucket_capacity)
    -- Update the last refill time
    last_refill_time = current_time
end

local allowed = 0 -- 0 means deny
-- If there are enough tokens, consume one and allow the request
if last_tokens >= requested_tokens then
    last_tokens = last_tokens - requested_tokens
    allowed = 1 -- 1 means allow
end

-- Save the new state back to Redis and set an expiration time (e.g., 2 minutes)
redis.call('set', tokens_key, last_tokens, 'EX', 120)
redis.call('set', timestamp_key, last_refill_time, 'EX', 120)

-- Return 1 if allowed, 0 if denied
return allowed