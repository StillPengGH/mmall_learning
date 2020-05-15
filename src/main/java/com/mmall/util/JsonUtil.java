package com.mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Json序列化和反序列化工具类
 *
 * @author Still
 * @version 1.0
 * @date 2020/5/13 15:27
 */

@Slf4j
public class JsonUtil {
    // jackson提供的一个Object映射对象
    private static ObjectMapper objectMapper = new ObjectMapper();

    // tomcat启动即执行的操作：初始化objectMapper一些属性
    static {
        // 属性一：对象的多所有字段全部列入，进行序列化
        objectMapper.setSerializationInclusion(Inclusion.ALWAYS);
        // 属性二：取消将Date转为TimeStamps
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        // 属性三：忽略空Bean转JSON的错误
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
        // 属性四：统一序列化后Date的格式：yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.DEFAULT_FORMAT));
        // 属性五：针对反序列化，忽略json字符串中存在，但是在java对象中不存该属性的情况，防止错误
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // 方法一：对象转字符串
    public static <T> String obj2String(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            // obj是String则直接返回，不是则使用objectMapper对其进行序列化操作
            return obj instanceof String ? (String) obj : objectMapper.writeValueAsString(obj);
        } catch (IOException e) {
            // 使用logback对错误进行输出
            log.warn("Parse Object to String Error", e);
            return null;
        }
    }

    // 方法二：对象转字符串（漂亮的、格式化好的）
    public static <T> String obj2StringPretty(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper
                    .writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (IOException e) {
            log.warn("Parse Object to Pretty String Error", e);
            return null;
        }
    }

    /**
     * 方法三：字符串转Obj
     * 其中<T> ：代表将方法设置为泛型方法
     * 第二个T：代表返回值类型
     * Class<T>：即限制Class的类型
     * str:要转的字符串
     * clazz：要转的类型（class是关键字，所以使用clazz）
     */
    public static <T> T string2Obj(String str, Class<T> clazz) {
        if (StringUtils.isEmpty(str) || clazz == null) {
            return null;
        }
        try {
            // 如果想要转的类型是是String类型直接返回，否则进行string to obj
            return clazz.equals(String.class) ? (T) str : objectMapper.readValue(str, clazz);
        } catch (IOException e) {
            log.warn("Parse String to Object Error", e);
            return null;
        }
    }

    /**
     * 反序列化（适用于复杂对象的反序列化）
     *
     * @param str           json字符串
     * @param typeReference 反序列化的类型：例如:Map<User,Category>、List<User>...
     * @return
     */
    public static <T> T string2Obj(String str, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(str) || typeReference == null) {
            return null;
        }
        try {
            return (T) (typeReference.getType().equals(String.class) ? str : objectMapper.readValue(str, typeReference));
        } catch (IOException e) {
            log.warn("Parse String to Object Error", e);
            return null;
        }

    }

    /**
     * 反序列化（适用于复杂集合类型的反序列化）
     *
     * @param str             json字符串
     * @param collectionClass 集合类型.class
     * @param elementsClass   集合内元素.class
     * @return
     */
    public static <T> T string2Obj(String str, Class<?> collectionClass, Class<?> elementsClass) {
        JavaType javaType = objectMapper
                .getTypeFactory()
                .constructParametricType(collectionClass, elementsClass);
        try {
            return objectMapper.readValue(str, javaType);
        } catch (Exception e) {
            log.warn("Parse String to Object Error", e);
            return null;
        }
    }
}
