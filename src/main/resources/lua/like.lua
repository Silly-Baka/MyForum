--- 点赞的lua脚本
--- 1.1 点赞的key
local likeKey = KEYS[1]
--- 1.2 被赞总数的key
local likeTotalKey = KEYS[2]

--- 2.1 点赞的用户id
local userId = ARGV[1]

--- 如果已点赞 则取消点赞 并减少被点赞者的赞总数  返回 false
if(tonumber(redis.call('sismember',likeKey,userId)) == 1) then
    --- 取消点赞
    redis.call('srem',likeKey,userId)
    --- 减少赞总数
    redis.call('decr',likeTotalKey)

    return false
else
--- 如果未点赞 则点赞 并增加赞总数  返回true
    --- 点赞
    redis.call('sadd',likeKey,userId)
    --- 增加赞总数
    redis.call('incr',likeTotalKey)

    return true
end
