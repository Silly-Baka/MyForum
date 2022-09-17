-------关注的lua脚本
---
--- 当前用户关注列表的key
local currentUserFollowKey = KEYS[1]
--- 被关注列表的key
local followedByKey = KEYS[2]
--- 当前用户id
local currentUserId = ARGV[1]
--- 被关注用户的id
local targetUserId = ARGV[2]
--- 如果已关注
if(tonumber(redis.call('sismember',followedByKey,currentUserId)) == 1) then
--- 取消关注
--- 减少目标用户被关注列表中的 当前用户
    redis.call('srem',followedByKey,currentUserId)
--- 减少当前用户关注列表中的 目标用户
    redis.call('srem',currentUserFollowKey,targetUserId)
--- 返回 false
    return false
else
--- 如果未关注
--- 关注
--- 增加目标用户被关注列表 当前用户
    redis.call('sadd',followedByKey,currentUserId)
--- 增加当前用户关注列表 目标用户
    redis.call('sadd',currentUserFollowKey,targetUserId)
--- 返回true
    return true
end