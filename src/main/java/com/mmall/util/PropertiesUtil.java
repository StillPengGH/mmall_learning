package com.mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

@Slf4j
public class PropertiesUtil {

    // Properties对象
    private static Properties props;

    // 静态代码块，只执行一次，初始化一些静态变量
    static {
        // 配置文件名称
        String fileName = "mmall.properties";
        props = new Properties();
        // 读取配置文件
        try {
            props.load(new InputStreamReader(
                    PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName),
                    "UTF-8"));
        } catch (IOException e) {
            log.error("配置文件读取异常", e);
        }
    }

    // 通过配置文件里的可key获取value
    public static String getProperty(String key){
        String value = props.getProperty(key.trim());
        if(StringUtils.isBlank(value)){
            return null;
        }
        return value.trim();
    }

    // 重载方法，可以为配置文件中的key设置默认值，灵活。
    public static String getProperty(String key,String defaultValue){
        String value = props.getProperty(key.trim());
        if(StringUtils.isBlank(value)){
            value = defaultValue;
        }
        return value.trim();
    }
}
