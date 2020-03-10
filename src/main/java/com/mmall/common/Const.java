package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * 常量类
 */
public class Const {
    // 当前登录的用户（用于作为缓存中的key）
    public static final String CURRENT_USER = "currentUser";
    // 用于注册时，校验用户名和邮箱的常量，判断type是否为用户名或邮箱
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";
    // 产品排序
    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }
    // 用户权限
    public interface Role{
        int ROLE_CUSTOMER = 0; // 普通用户
        int ROLE_ADMIN = 1; // 管理员
    }

    // 产品状态码
    public enum ProductStatusEnum {
        ON_SALE(1,"在线");
        private String value;
        private int code;
        ProductStatusEnum(int code,String value){
            this.code = code;
            this.value = value;
        }
        public String getValue() {
            return value;
        }
        public int getCode() {
            return code;
        }
    }
}
