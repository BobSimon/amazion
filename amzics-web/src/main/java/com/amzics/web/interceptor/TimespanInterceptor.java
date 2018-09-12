package com.amzics.web.interceptor;

import com.amzics.common.consts.RequestKey;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 计算耗时
 **/
public class TimespanInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute(RequestKey.REQUEST_START_TIME, System.currentTimeMillis());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        Long currentTimeMillis = (Long) request.getAttribute(RequestKey.REQUEST_START_TIME);
        long timespan = System.currentTimeMillis() - currentTimeMillis.longValue();
    }
}
