package com.rtalky.infrastructure.cache.impl;

import com.rtalky.infrastructure.cache.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存服务实现
 */
@Slf4j
@Service
public class RedisCacheService implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ValueOperations<String, Object> valueOperations;

    public RedisCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.valueOperations = redisTemplate.opsForValue();
    }

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            valueOperations.set(key, value, timeout, unit);
            log.debug("设置缓存: key={}, timeout={} {}", key, timeout, unit);
        } catch (Exception e) {
            log.error("设置缓存失败: key={}", key, e);
            throw new RuntimeException("缓存设置失败", e);
        }
    }

    @Override
    public void set(String key, Object value) {
        try {
            valueOperations.set(key, value);
            log.debug("设置缓存(永不过期): key={}", key);
        } catch (Exception e) {
            log.error("设置缓存失败: key={}", key, e);
            throw new RuntimeException("缓存设置失败", e);
        }
    }

    @Override
    public Object get(String key) {
        try {
            Object value = valueOperations.get(key);
            log.debug("获取缓存: key={}, value={}", key, value);
            return value;
        } catch (Exception e) {
            log.error("获取缓存失败: key={}", key, e);
            throw new RuntimeException("缓存获取失败", e);
        }
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = valueOperations.get(key);
            if (value != null && clazz.isInstance(value)) {
                log.debug("获取缓存: key={}, value={}", key, value);
                return clazz.cast(value);
            }
            return null;
        } catch (Exception e) {
            log.error("获取缓存失败: key={}", key, e);
            throw new RuntimeException("缓存获取失败", e);
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            boolean result = redisTemplate.delete(key);
            log.debug("删除缓存: key={}, result={}", key, result);
            return result;
        } catch (Exception e) {
            log.error("删除缓存失败: key={}", key, e);
            throw new RuntimeException("缓存删除失败", e);
        }
    }

    @Override
    public long delete(Set<String> keys) {
        try {
            Long count = redisTemplate.delete(keys);
            long result = count != null ? count : 0;
            log.debug("批量删除缓存: keys={}, count={}", keys, result);
            return result;
        } catch (Exception e) {
            log.error("批量删除缓存失败: keys={}", keys, e);
            throw new RuntimeException("缓存批量删除失败", e);
        }
    }

    @Override
    public boolean hasKey(String key) {
        try {
            Boolean result = redisTemplate.hasKey(key);
            return result != null && result;
        } catch (Exception e) {
            log.error("检查缓存是否存在失败: key={}", key, e);
            throw new RuntimeException("缓存检查失败", e);
        }
    }

    @Override
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            Boolean result = redisTemplate.expire(key, timeout, unit);
            return result != null && result;
        } catch (Exception e) {
            log.error("设置缓存过期时间失败: key={}", key, e);
            throw new RuntimeException("缓存过期设置失败", e);
        }
    }

    @Override
    public Long getExpire(String key, TimeUnit unit) {
        try {
            return redisTemplate.getExpire(key, unit);
        } catch (Exception e) {
            log.error("获取缓存过期时间失败: key={}", key, e);
            throw new RuntimeException("缓存过期时间获取失败", e);
        }
    }

    @Override
    public long increment(String key, long delta) {
        try {
            Long result = valueOperations.increment(key, delta);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("缓存递增失败: key={}, delta={}", key, delta, e);
            throw new RuntimeException("缓存递增失败", e);
        }
    }

    @Override
    public long decrement(String key, long delta) {
        try {
            Long result = valueOperations.decrement(key, delta);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("缓存递减失败: key={}, delta={}", key, delta, e);
            throw new RuntimeException("缓存递减失败", e);
        }
    }

    @Override
    public Map<Object, Object> hashGetAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("获取哈希表所有字段失败: key={}", key, e);
            throw new RuntimeException("哈希表获取失败", e);
        }
    }

    @Override
    public Object hashGet(String key, String field) {
        try {
            return redisTemplate.opsForHash().get(key, field);
        } catch (Exception e) {
            log.error("获取哈希表字段失败: key={}, field={}", key, field, e);
            throw new RuntimeException("哈希表字段获取失败", e);
        }
    }

    @Override
    public void hashSet(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
        } catch (Exception e) {
            log.error("设置哈希表字段失败: key={}, field={}", key, field, e);
            throw new RuntimeException("哈希表字段设置失败", e);
        }
    }

    @Override
    public long hashDelete(String key, Object... fields) {
        try {
            return redisTemplate.opsForHash().delete(key, fields);
        } catch (Exception e) {
            log.error("删除哈希表字段失败: key={}, fields={}", key, Arrays.toString(fields), e);
            throw new RuntimeException("哈希表字段删除失败", e);
        }
    }

    @Override
    public boolean hashHasKey(String key, String field) {
        try {
            return redisTemplate.opsForHash().hasKey(key, field);
        } catch (Exception e) {
            log.error("检查哈希表字段是否存在失败: key={}, field={}", key, field, e);
            throw new RuntimeException("哈希表字段检查失败", e);
        }
    }

    @Override
    public long listLeftPush(String key, Object value) {
        try {
            Long result = redisTemplate.opsForList().leftPush(key, value);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("列表左推失败: key={}", key, e);
            throw new RuntimeException("列表操作失败", e);
        }
    }

    @Override
    public long listRightPush(String key, Object value) {
        try {
            Long result = redisTemplate.opsForList().rightPush(key, value);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("列表右推失败: key={}", key, e);
            throw new RuntimeException("列表操作失败", e);
        }
    }

    @Override
    public Object listLeftPop(String key) {
        try {
            return redisTemplate.opsForList().leftPop(key);
        } catch (Exception e) {
            log.error("列表左弹失败: key={}", key, e);
            throw new RuntimeException("列表操作失败", e);
        }
    }

    @Override
    public Object listRightPop(String key) {
        try {
            return redisTemplate.opsForList().rightPop(key);
        } catch (Exception e) {
            log.error("列表右弹失败: key={}", key, e);
            throw new RuntimeException("列表操作失败", e);
        }
    }

    @Override
    public List<Object> listRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("获取列表范围失败: key={}, start={}, end={}", key, start, end, e);
            throw new RuntimeException("列表操作失败", e);
        }
    }

    @Override
    public long setAdd(String key, Object... values) {
        try {
            Long result = redisTemplate.opsForSet().add(key, values);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("集合添加失败: key={}, values={}", key, Arrays.toString(values), e);
            throw new RuntimeException("集合操作失败", e);
        }
    }

    @Override
    public long setRemove(String key, Object... values) {
        try {
            Long result = redisTemplate.opsForSet().remove(key, values);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("集合移除失败: key={}, values={}", key, Arrays.toString(values), e);
            throw new RuntimeException("集合操作失败", e);
        }
    }

    @Override
    public Set<Object> setMembers(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("获取集合成员失败: key={}", key, e);
            throw new RuntimeException("集合操作失败", e);
        }
    }

    @Override
    public boolean setIsMember(String key, Object value) {
        try {
            Boolean result = redisTemplate.opsForSet().isMember(key, value);
            return result != null && result;
        } catch (Exception e) {
            log.error("检查集合成员失败: key={}, value={}", key, value, e);
            throw new RuntimeException("集合操作失败", e);
        }
    }

    @Override
    public long setSize(String key) {
        try {
            Long result = redisTemplate.opsForSet().size(key);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("获取集合大小失败: key={}", key, e);
            throw new RuntimeException("集合操作失败", e);
        }
    }

    @Override
    public Set<String> keys(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception e) {
            log.error("获取匹配键失败: pattern={}", pattern, e);
            throw new RuntimeException("键匹配失败", e);
        }
    }
}
