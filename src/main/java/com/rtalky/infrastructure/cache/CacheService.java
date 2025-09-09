package com.rtalky.infrastructure.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口
 * 提供统一的缓存操作API
 */
public interface CacheService {

    /**
     * 设置缓存值
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    void set(String key, Object value, long timeout, TimeUnit unit);

    /**
     * 设置缓存值（永不过期）
     *
     * @param key 缓存键
     * @param value 缓存值
     */
    void set(String key, Object value);

    /**
     * 获取缓存值
     *
     * @param key 缓存键
     * @return 缓存值
     */
    Object get(String key);

    /**
     * 获取缓存值并转换为指定类型
     *
     * @param key 缓存键
     * @param clazz 目标类型
     * @return 缓存值
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 删除缓存
     *
     * @param key 缓存键
     * @return 是否删除成功
     */
    boolean delete(String key);

    /**
     * 批量删除缓存
     *
     * @param keys 缓存键集合
     * @return 成功删除的数量
     */
    long delete(Set<String> keys);

    /**
     * 检查缓存是否存在
     *
     * @param key 缓存键
     * @return 是否存在
     */
    boolean hasKey(String key);

    /**
     * 设置过期时间
     *
     * @param key 缓存键
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否设置成功
     */
    boolean expire(String key, long timeout, TimeUnit unit);

    /**
     * 获取剩余过期时间
     *
     * @param key 缓存键
     * @param unit 时间单位
     * @return 剩余时间
     */
    Long getExpire(String key, TimeUnit unit);

    /**
     * 递增
     *
     * @param key 缓存键
     * @param delta 递增因子
     * @return 递增后的值
     */
    long increment(String key, long delta);

    /**
     * 递减
     *
     * @param key 缓存键
     * @param delta 递减因子
     * @return 递减后的值
     */
    long decrement(String key, long delta);

    /**
     * 获取哈希表中的所有字段和值
     *
     * @param key 缓存键
     * @return 字段-值映射
     */
    Map<Object, Object> hashGetAll(String key);

    /**
     * 获取哈希表中指定字段的值
     *
     * @param key 缓存键
     * @param field 字段名
     * @return 字段值
     */
    Object hashGet(String key, String field);

    /**
     * 设置哈希表中的字段值
     *
     * @param key 缓存键
     * @param field 字段名
     * @param value 字段值
     */
    void hashSet(String key, String field, Object value);

    /**
     * 删除哈希表中的字段
     *
     * @param key 缓存键
     * @param fields 字段名
     * @return 删除的字段数量
     */
    long hashDelete(String key, Object... fields);

    /**
     * 检查哈希表中是否存在字段
     *
     * @param key 缓存键
     * @param field 字段名
     * @return 是否存在
     */
    boolean hashHasKey(String key, String field);

    /**
     * 向列表左侧添加元素
     *
     * @param key 缓存键
     * @param value 元素值
     * @return 列表长度
     */
    long listLeftPush(String key, Object value);

    /**
     * 向列表右侧添加元素
     *
     * @param key 缓存键
     * @param value 元素值
     * @return 列表长度
     */
    long listRightPush(String key, Object value);

    /**
     * 从列表左侧弹出元素
     *
     * @param key 缓存键
     * @return 元素值
     */
    Object listLeftPop(String key);

    /**
     * 从列表右侧弹出元素
     *
     * @param key 缓存键
     * @return 元素值
     */
    Object listRightPop(String key);

    /**
     * 获取列表范围
     *
     * @param key 缓存键
     * @param start 起始索引
     * @param end 结束索引
     * @return 元素列表
     */
    List<Object> listRange(String key, long start, long end);

    /**
     * 向集合添加元素
     *
     * @param key 缓存键
     * @param values 元素值
     * @return 成功添加的数量
     */
    long setAdd(String key, Object... values);

    /**
     * 从集合移除元素
     *
     * @param key 缓存键
     * @param values 元素值
     * @return 成功移除的数量
     */
    long setRemove(String key, Object... values);

    /**
     * 获取集合所有元素
     *
     * @param key 缓存键
     * @return 元素集合
     */
    Set<Object> setMembers(String key);

    /**
     * 检查集合是否包含元素
     *
     * @param key 缓存键
     * @param value 元素值
     * @return 是否包含
     */
    boolean setIsMember(String key, Object value);

    /**
     * 获取集合大小
     *
     * @param key 缓存键
     * @return 集合大小
     */
    long setSize(String key);

    /**
     * 获取匹配的键
     *
     * @param pattern 匹配模式
     * @return 键集合
     */
    Set<String> keys(String pattern);
}
