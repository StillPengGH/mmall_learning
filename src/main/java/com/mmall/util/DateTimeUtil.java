package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * @author Still
 * @version 1.0
 * @date 2020/2/28 10:46
 */
public class DateTimeUtil {

    // 默认转换格式
    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 字符串转指定格式时间
     * @param dateTimeStr 时间字符串
     * @param formatStr 转换格式字符串
     * @return Date
     */
    public static Date strToDate(String dateTimeStr, String formatStr){
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(formatStr);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }

    /**
     * 时间类型转String
     * @param date 时间
     * @param formatStr 格式
     * @return String
     */
    public static String dateToStr(Date date, String formatStr){
        if(date == null){
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(formatStr);
    }

    // 重载方法
    public static Date strToDate(String dateTimeStr){
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DEFAULT_FORMAT);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }

    public static String dateToStr(Date date){
        if(date == null){
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(DEFAULT_FORMAT);
    }
}
