package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.List;

/**
 * 分片式Redis连接池
 *
 * @author Still
 * @version 1.0
 * @date 2020/5/21 10:36
 */
public class RedisShardedPool {
    // ShardedJedisPool 连接池
    // 定义为static是为了保证jedis连接池在tomcat启动的时候就要加载出来
    // 后面会写个静态代码块，初始化jedis连接池
    private static ShardedJedisPool pool;
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
    // Redis1的host
    private static String redis1Host = PropertiesUtil.getProperty("redis1.host");
    // Redis1的port
    private static Integer redis1Port = Integer.parseInt(PropertiesUtil.getProperty("redis1.port"));
    // Redis2的host
    private static String redis2Host = PropertiesUtil.getProperty("redis2.host");
    // Redis2的port
    private static Integer redis2Port = Integer.parseInt(PropertiesUtil.getProperty("redis2.port"));

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
        // 改造前：创建pool实例，其中第四个参数是超时时间，设置为2秒
        // pool = new JedisPool(poolConfig, redisHost, redisPort, 1000 * 2);

        // 改造后
        JedisShardInfo info1 = new JedisShardInfo(redis1Host,redis1Port,1000*2);
        JedisShardInfo info2 = new JedisShardInfo(redis2Host,redis2Port,1000*2);
        List<JedisShardInfo> jedisShardInfoList = new ArrayList<JedisShardInfo>(2);
        jedisShardInfoList.add(info1);
        jedisShardInfoList.add(info2);
        // 其中Hashing.MURMUR_HASH就是使用的一次性算法
        pool = new ShardedJedisPool(poolConfig,
                jedisShardInfoList,
                Hashing.MURMUR_HASH,
                Sharded.DEFAULT_KEY_TAG_PATTERN);
    }

    // 项目启动的时候就执行初始化连接池操作
    static {
        initPool();
    }

    // 对外开放的方法
    // 获取一个连接（jedis实例）
    public static ShardedJedis getResource() {
        return pool.getResource();
    }

    // 返回一个连接
    public static void returnResource(ShardedJedis jedis) {
        pool.returnResource(jedis);
    }

    // 返回一个Broken的连接(损坏的连接)
    public static void returnBrokenResource(ShardedJedis jedis) {
        pool.returnBrokenResource(jedis);
    }

    // 测试
    //public static void main(String[] args) {
    //    // 获取一个分片式Jedis实例
    //    ShardedJedis jedis = pool.getResource();
    //    // 向分布式redis服务器中循环写入数据key-value
    //    for (int i=0;i<10;i++){
    //        jedis.set("key"+i,"value"+i);
    //    }
    //    // 将Jedis实例返还
    //    pool.returnResource(jedis);
    //    System.out.println("end");
    //}
}
