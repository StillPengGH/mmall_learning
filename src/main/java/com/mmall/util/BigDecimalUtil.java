package com.mmall.util;

import java.math.BigDecimal;

/**
 * 处理浮点数丢失精度工具类
 *
 * @author Still
 * @version 1.0
 * @date 2020/3/11 13:33
 */
public class BigDecimalUtil {

    private BigDecimalUtil() {

    }

    // 加法
    public static BigDecimal add(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2);
    }

    // 减法
    public static BigDecimal sub(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2);
    }

    // 乘法
    public static BigDecimal mul(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2);
    }

    // 除法
    public static BigDecimal div(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        // 四舍五入，保留小数点后两位
        return b1.divide(b2,2,BigDecimal.ROUND_HALF_UP);
    }

}
