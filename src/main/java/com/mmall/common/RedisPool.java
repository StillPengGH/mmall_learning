package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis 连接池
 *
 * @author Still
 * @version 1.0
 * @date 2020/5/12 10:11
 */
public class RedisPool {
    // jedis 连接池
    // 定义为static是为了保证jedis连接池在tomcat启动的时候就要加载出来
    // 后面会写个静态代码块，初始化jedis连接池
    private static JedisPool pool;
    // 控制jedis连接池里面和redis-server的最大连接数
    private static Integer maxTotal = Integer.parseInt(
            PropertiesUtil.getProperty("redis.max.total", "20"));
    // 在jedis连接池最多有多个少状态是空闲的的jedis实例，
    private static Integer maxIdle = Integer.parseInt(
            PropertiesUtil.getProperty("redis.max.idle", "20"));
    // 在jedis pool中最小的空闲状态额jedis实例个数
    private static Integer minIdle = Integer.parseInt(
            PropertiesUtil.getProperty("redis.min.idle", "20"));
    // 在向jedispool中借（取）一个jedis实例，是否要进行验证操作，如果赋值true，则拿到的jedis实例一定是可用的
    private static Boolean testOnBorrow = Boolean.parseBoolean(
            PropertiesUtil.getProperty("redis.test.borrow", "true"));
    // 在还jedis实例的时候，是否进行验证操作，如果true，则还回到jedispool里的jedis实例一定是可用的
    private static Boolean testOnReturn = Boolean.parseBoolean(
            PropertiesUtil.getProperty("redis.test.return", "true"));
    // Redis的host
    private static String redisHost = PropertiesUtil.getProperty("redis.host");
    // Redis的port
    private static Integer redisPort = Integer.parseInt(PropertiesUtil.getProperty("redis.port"));

    // 连接池初始化配置，私有静态方法，防止外部调用
    // 只在内部调用一次即可初始化jedis连接池
    private static void initPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setTestOnBorrow(testOnBorrow);
        poolConfig.setTestOnReturn(testOnReturn);
        // 连接耗尽时，是否阻塞，false会抛出异常，true阻塞直到超时。默认为true
        poolConfig.setBlockWhenExhausted(true);
        // 创建pool实例，其中第四个参数是超时时间，设置为2秒
        pool = new JedisPool(poolConfig, redisHost, redisPort, 1000 * 2);
    }

    // 项目启动的时候就执行初始化连接池操作
    static {
        initPool();
    }

    // 对外开放的方法
    // 获取一个连接（jedis实例）
    public static Jedis getResource() {
        return pool.getResource();
    }

    // 返回一个连接
    public static void returnResource(Jedis jedis) {
        pool.returnResource(jedis);
    }

    // 返回一个Broken的连接(损坏的连接)
    public static void returnBrokenResource(Jedis jedis) {
        pool.returnBrokenResource(jedis);
    }

}
