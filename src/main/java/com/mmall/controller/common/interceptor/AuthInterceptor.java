package com.mmall.controller.common.interceptor;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.schema.Server;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * 登录权限统一判断拦截器
 *
 * @author Still
 * @version 1.0
 * @date 2020/5/26 9:40
 */
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {
    /**
     * Controller处理之前调用
     */
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest,
                             HttpServletResponse httpServletResponse,
                             Object o) throws Exception {
        // 将参数o强转为HandlerMethod
        HandlerMethod handlerMethod = (HandlerMethod) o;
        // 通过handlerMethod对象获取方法名和类名
        String methodName = handlerMethod.getMethod().getName(); // login
        String className = handlerMethod.getBean().getClass().getSimpleName(); // 不带包名的类名
        //String className = handlerMethod.getBean().getClass().getName(); // 带包名的类名

        // 组装日志信息，并打印，日志内容：参数的key和value
        StringBuffer requestParamBuffer = new StringBuffer();
        Map paramMap = httpServletRequest.getParameterMap();
        Iterator it = paramMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String mapKey = (String) entry.getKey();
            String mapValue = StringUtils.EMPTY;
            // request的paramMap，里面的value返回的是一个String数组
            Object obj = entry.getValue();
            if (obj instanceof String[]) {
                String[] strArr = (String[]) obj;
                mapValue = Arrays.toString(strArr);
            }
            requestParamBuffer.append(mapKey).append("=").append(mapValue);
        }

        // 判断是否是登录请求，如果是直接取消拦截，进入controller
        if (StringUtils.equals(className, "UserManageController") && StringUtils.equals(methodName, "login")) {
            log.info("权限拦截器拦截到请求，className:{},methodName:{}", className, methodName);
            // 如果是登录请求，不打印参数，因为参数有密码，会引起信息泄露
            return true;
        }

        // 输入拦截日志
        log.info("权限拦截器拦截到请求，className:{},methodName:{},params:{}",
                className,methodName,requestParamBuffer.toString());

        // 获取用户信息
        User user = null;
        // 在cookie中获取登录token
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isNotEmpty(loginToken)) {
            // 在redis中获取用户信息，并反序列化为User对象
            String userJsonStr = RedisShardedPoolUtil.get(loginToken);
            user = JsonUtil.string2Obj(userJsonStr, User.class);
        }

        // 如果用户为空或用户权限非管理员
        if (user == null || (user.getRole().intValue() != Const.Role.ROLE_ADMIN)) {
            // 重置response
            // 必须添加reset，不然报错：getWriter() has already been called for this response
            httpServletResponse.reset();
            // 因为脱离了springMVC的返回流程，所以重置编码
            httpServletResponse.setCharacterEncoding("UTF-8");
            // 设置返回值类型
            httpServletResponse.setContentType("application/json;charset=UTF-8");

            // 拿到输出对象
            PrintWriter out = httpServletResponse.getWriter();
            if (user == null) {
                // 判断如果是产品管理中的富文本上传请求，封装特定返回值（Map）
                if (StringUtils.equals(className, "ProductManageController")
                        && StringUtils.equals(methodName, "richTextImgUpload")) {
                    Map resMap = Maps.newHashMap();
                    resMap.put("success",false);
                    resMap.put("msg","请登录管理员");
                    out.print(JsonUtil.obj2String(resMap));
                } else {
                    out.print(JsonUtil.obj2String(ServerResponse.createByErrorMessage("拦截器拦截，用户未登录")));
                }
            } else {
                if (StringUtils.equals(className, "ProductManageController")
                        && StringUtils.equals(methodName, "richTextImgUpload")) {
                    Map resMap = Maps.newHashMap();
                    resMap.put("success",false);
                    resMap.put("msg","无权限操作");
                    out.print(JsonUtil.obj2String(resMap));
                } else {
                    out.print(JsonUtil.obj2String(ServerResponse.createByErrorMessage("拦截器拦截，无权限访问")));
                }
            }

            // 将输出流中数据清空
            out.flush();
            // 关闭输出流
            out.close();

            // 返回false，不进入controller
            return false;
        }

        // 如果用户不为空且为管理员，进入controller
        return true;
    }

    /**
     * Controller处理之后调用
     */
    @Override
    public void postHandle(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse,
                           Object o,
                           ModelAndView modelAndView) throws Exception {
        log.info("postHandle");
    }

    /**
     * 所有处理完成后调用（例：非前后端分离项目，View层之后）
     */
    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                Object o, Exception e) throws Exception {
        log.info("afterCompletion");
    }
}
