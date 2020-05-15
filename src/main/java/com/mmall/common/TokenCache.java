package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Token缓存类
 */
@Slf4j
public class TokenCache {

    // 缓存中key的前缀
    public static final String TOKEN_PREFIX = "token_";

    // 创建本地缓存
    private static LoadingCache<String,String> localCache = CacheBuilder.newBuilder()
            .initialCapacity(1000)
            .maximumSize(10000) // 缓存最大空间为10兆
            .expireAfterAccess(12, TimeUnit.HOURS) // 有效期12小时
            .build(new CacheLoader<String, String>() {
                // 默认的数据加载实现，当调用get取值的时候，如果key没有对应的值，就调用这个方法进行加载
                @Override
                public String load(String s) throws Exception {
                    return "null";
                }
            });

    // 设置缓存
    public static void setKey(String key,String value){
       localCache.put(key,value);
    }

    // 获取缓存
    public static String getKey(String key){
        String value = null;
        try{
            value = localCache.get(key);
            if("null".equals(value)){
                return null;
            }
            return value;
        }catch (Exception e){
            // 记录错误日志
            log.error("localCache get error",e);
        }
        return null;
    }
}
