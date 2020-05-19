package com.mmall.controller.common;

import com.mmall.common.Const;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Session有效期重置过滤器
 *
 * @author Still
 * @version 1.0
 * @date 2020/5/19 9:02
 */
public class SessionExpireFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        // 将ServletRequest强转为HttpServletRequest
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        // 通过HttpServletRequest获取Cookie中的LoginToken
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        // 如果loginToken不为空，重置登录token(sessionID)有效期（30分钟）
        if (StringUtils.isNotEmpty(loginToken)) {
            // 获取User信息
            String userJsonStr = RedisPoolUtil.get(loginToken);
            User user = JsonUtil.string2Obj(userJsonStr, User.class);
            // 用户信息不为空，重置session有效期
            if (user != null) {
                RedisPoolUtil.expire(loginToken, Const.RedisCacheExTime.REDIS_SESSION_EX_TIME);
            }
        }
        filterChain.doFilter(servletRequest,servletResponse);
    }

    @Override
    public void destroy() {

    }
}
