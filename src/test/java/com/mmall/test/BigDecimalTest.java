package com.mmall.test;

import org.junit.Test;

import java.math.BigDecimal;

/**
 * 浮点数丢失精度测试
 * @author Still
 * @version 1.0
 * @date 2020/3/11 13:24
 */
public class BigDecimalTest {
    /**
     * 输出结果：丢失精度
     * 0.060000000000000005
     * 0.5800000000000001
     * 401.49999999999994
     * 1.2329999999999999
     */
    @Test
    public void test1() {
        System.out.println(0.05 + 0.01);
        System.out.println(1.0 - 0.42);
        System.out.println(4.015 * 100);
        System.out.println(123.3 / 100);
    }

    /**
     * 输出结果：依然丢失精度
     * 0.06000000000000000298372437868010820238851010799407958984375
     */
    @Test
    public void test2() {
        BigDecimal b1 = new BigDecimal(0.05);
        BigDecimal b2 = new BigDecimal(0.01);
        System.out.println(b1.add(b2));
    }

    /**
     * 输出结果：符合预期
     * 0.06
     */
    @Test
    public void test3(){
        BigDecimal b1 = new BigDecimal("0.05");
        BigDecimal b2 = new BigDecimal("0.01");
        System.out.println(b1.add(b2));
    }
}
