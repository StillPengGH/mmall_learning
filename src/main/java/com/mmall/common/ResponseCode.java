package com.mmall.common;

/**
 * 响应状态码（枚举类）
 */
public enum ResponseCode {
    SUCCESS(0, "SUCCESS"), // 成功
    ERROR(1, "ERROR"),     // 失败
    ILLEGAL_ARGUMENT(2, "ILLEGAL_ARGUMENT"), // 参数错误
    NEED_LOGIN(10, "NEED_LOGIN"); // 未登录

    // 状态码
    private int code;
    // 描述
    private String desc;

    // 构造器
    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 开放get方法
    public int getCode() {
        return code;
    }
    public String getDesc() {
        return desc;
    }
}
