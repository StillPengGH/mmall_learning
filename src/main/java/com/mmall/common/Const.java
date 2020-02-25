package com.mmall.common;

/**
 * 常量类
 */
public class Const {
    // 当前登录的用户（用于作为缓存中的key）
    public static final String CURRENT_USER = "currentUser";
    // 用于注册时，校验用户名和邮箱的常量，判断type是否为用户名或邮箱
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";

    // 用户权限
    public interface Role{
        int ROLE_CUSTOMER = 0; // 普通用户
        int ROLE_ADMIN = 1; // 管理员
    }
}
