package com.mmall.util;

import com.mmall.common.RedisPool;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

/**
 * Jedis各种操作的API封装
 *
 * @author Still
 * @version 1.0
 * @date 2020/5/12 14:33
 */
@Slf4j
public class RedisPoolUtil {

    // 添加key-value
    public static String set(String key, String value) {
        Jedis jedis = null;
        String result = null;

        try {
            jedis = RedisPool.getResource();
            result = jedis.set(key, value);
        } catch (Exception e) {
            log.error("set key:{} value:{} error", key, value, e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }

        RedisPool.returnResource(jedis);
        return result;
    }

    // 根据key获取value值
    public static String get(String key) {
        Jedis jedis = null;
        String result = null;

        try {
            jedis = RedisPool.getResource();
            result = jedis.get(key);
        } catch (Exception e) {
            log.error("get key:{} error", key, e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }

        RedisPool.returnResource(jedis);
        return result;
    }

    // set添加过期时间（单位是秒）
    public static String setEx(String key, String value, int exTime) {
        Jedis jedis = null;
        String result = null;
        try {
            jedis = RedisPool.getResource();
            result = jedis.setex(key, exTime, value);
        } catch (Exception e) {
            log.error("setEx key:{} value:{} error", key, value, e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    // 重新设置指定key的有效期
    public static Long expire(String key, int exTime) {
        Jedis jedis = null;
        Long result = null;
        try {
            jedis = RedisPool.getResource();
            result = jedis.expire(key, exTime);
        } catch (Exception e) {
            log.error("expire key:{} error", key, e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    // 根据key进行删除操作
    public static Long del(String key) {
        Jedis jedis = null;
        Long result = null;
        try {
            jedis = RedisPool.getResource();
            result = jedis.del(key);
        } catch (Exception e) {
            log.error("del key:{} error", key, e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }

        RedisPool.returnResource(jedis);
        return result;
    }
}
