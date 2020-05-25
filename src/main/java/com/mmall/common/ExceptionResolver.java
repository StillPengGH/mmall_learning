package com.mmall.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 全局异常处理
 *
 * @author Still
 * @version 1.0
 * @date 2020/5/25 14:38
 */
@Slf4j
@Component
public class ExceptionResolver implements HandlerExceptionResolver {

    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest,
                                         HttpServletResponse httpServletResponse,
                                         Object o, Exception e) {
        // 在后台输入错误堆栈(哪个请求异常，e为具体异常信息)
        log.error("{} Exception",httpServletRequest.getRequestURI(),e);
        // 将ModelAndView转为Json ModelAndView
        ModelAndView modelAndView = new ModelAndView(new MappingJacksonJsonView());
        // 如果jackson使用的是2.0以上版本，要是用MappingJackson2JsonView
        modelAndView.addObject("status",ResponseCode.ERROR.getCode());
        modelAndView.addObject("msg","接口异常，详情请看服务器端日志的异常信息。");
        modelAndView.addObject("data",e.toString()); // e.toString()拿到的是简短的异常描述
        return modelAndView;
    }
}
