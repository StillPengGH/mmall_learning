package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Redisson初始化类
 *
 * @author Still
 * @version 1.0
 * @date 2020/7/8 16:01
 */
@Component
@Slf4j
public class RedissonManager {
    // Redisson的config
    private Config redissonConfig = new Config();

    private Redisson redisson = null;

    // 对外开放redisson
    public Redisson getRedisson() {
        return redisson;
    }

    // Redis1的host
    private static String redis1Host = PropertiesUtil.getProperty("redis1.host");
    // Redis1的port
    private static Integer redis1Port = Integer.parseInt(PropertiesUtil.getProperty("redis1.port"));
    // Redis2的host
    private static String redis2Host = PropertiesUtil.getProperty("redis2.host");
    // Redis2的port
    private static Integer redis2Port = Integer.parseInt(PropertiesUtil.getProperty("redis2.port"));

    // RedissonManager在构造器完成之后执行init方法
    // 和写在静态代码块中一个效果
    @PostConstruct
    private void init() {
        try {
            // Redisson这个版本不支持一致性算法
            // 加载单服务
            redissonConfig.useSingleServer().setAddress(
                    new StringBuffer().append(redis1Host).append(":").append(redis1Port).toString());
            // 创建redisson示例
            redisson = (Redisson) Redisson.create(redissonConfig);
            log.info("初始化Redisson结束");
        } catch (Exception e) {
            log.error("Redisson init error", e);
        }
    }
}
