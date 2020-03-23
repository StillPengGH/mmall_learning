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
    // 购物车相关
    public interface Cart{
        int CHECKED = 1; // 购物车选中状态
        int UN_CHECKED = 0; // 购物车中未选中状态
        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
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
    // 订单状态枚举
    public enum OrderStatusEnum{
        CANCELED(0,"已取消"),
        NO_PAY(10,"未支付"),
        PAID(20,"已支付"),
        SHIPPED(40,"已发货"),
        ORDER_SUCCESS(50,"订单完成"),
        ORDER_CLOSED(60,"订单关闭");
        OrderStatusEnum(int code,String value){
            this.code = code;
            this.value = value;
        }
        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }
    // 支付宝回到交易状态码
    public interface AlipayCallBack{
        // 等待买家付款
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        // 交易成功
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";
        // 给支付宝响应正确/错误
        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }

    // 支付平台
    public enum PayPlatformEnum{
        ALIPAY(1,"支付宝");
        PayPlatformEnum(int code,String value){
            this.code = code;
            this.value = value;
        }
        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }
}
