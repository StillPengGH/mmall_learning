package com.mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Still
 * @version 1.0
 * @date 2020/5/15 15:17
 */

@Slf4j
public class CookieUtil {
    // 写在哪个域名下，这里.yaoerba.com是一级域名下
    // www.yaoerbacom.com|user.yaoerba.com|product.yaoerba.com都可以看到写的cookie
    private final static String COOKIE_DOMAIN = ".yaoerba.com";
    // 服务端要“种”到客户端浏览器上的名字
    private final static String COOKIE_NAME = "yeb_login_token";

    /**
     * 向客户端写Cookie用来记录登录的token
     * @param response 因为是写登录的Cookie，写到客户端，所有使用Http的Response响应对象
     * @param token 就是登录的JSESSIONID
     */
    public static void writeLoginToken(HttpServletResponse response,String token){
        // cookie中存储的名字是yeb_login_token,值就是登录时的sessionId
        Cookie cookie = new Cookie(COOKIE_NAME,token);
        cookie.setDomain(COOKIE_DOMAIN);
        //设置指定目录才能获取到cookie，这里我们设置在根目录
        cookie.setPath("/");
        //设置cookie有效期为1年
        //设置为-1，则代表永久
        //不设置，cookie不会写入硬盘，而是写在内存，只在当前页面有效
        cookie.setMaxAge(60*60*24*365);
        log.info("write cookieName:{} cookieValue:{}",cookie.getName(),cookie.getValue());
        //通过响应对象将cookie写到客户端浏览器
        response.addCookie(cookie);

    }

    /**
     * 读取存储在Cookie里的登录token
     * @param request 读取肯定用到的是，Http的Request对象
     */
    public static String readLoginToken(HttpServletRequest request){
        // 从request中读取cookie返回的是一个数组
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(Cookie cookie:cookies){
                log.info("read cookieName:{} cookieValue:{}",cookie.getName(),cookie.getValue());
                // 如果在客户端的cookies数组中，获取到了name为yeb_login_token，返回value值
                // 即当时登录时的JSESSIONID
                if(StringUtils.equals(cookie.getName(),COOKIE_NAME)){
                    log.info("return cookieName:{},cookieValue:{}",cookie.getName(),cookie.getValue());
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 退出登录时，清除cookie中登录的token
     * @param request
     * @param response
     */
    public static void delLoginToken(HttpServletRequest request,HttpServletResponse response){
        // 从request中读取cookies
        Cookie[] cookies = request.getCookies();
        // 如果存在则将该cookie的有效期设置为0，有效期为0的cookie就是删除该cookie
        // 使用response对象对其返回到客户端
        if(cookies != null){
            for (Cookie cookie:cookies){
                if(StringUtils.equals(cookie.getName(),COOKIE_NAME)){
                    cookie.setDomain(COOKIE_DOMAIN);
                    cookie.setPath("/");
                    // 设置为0,代表删除cookie
                    cookie.setMaxAge(0);
                    log.info("delete cookieName:{} cookieValue:{}",cookie.getName(),cookie.getValue());
                    response.addCookie(cookie);
                    return;
                }
            }
        }
    }
}
