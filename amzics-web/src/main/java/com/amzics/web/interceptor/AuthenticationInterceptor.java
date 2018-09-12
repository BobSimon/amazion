package com.amzics.web.interceptor;

import com.amzics.common.consts.CookieKey;
import com.amzics.common.consts.RestResultStatus;
import com.amzics.common.consts.SessionKey;
import com.amzics.common.utils.EncryptUtils;
import com.amzics.common.utils.JsonUtils;
import com.amzics.model.annotation.Authen;
import com.amzics.model.domain.SysUser;
import com.amzics.model.exception.BusinessException;
import com.amzics.model.pojo.RestResult;
import com.amzics.service.SysUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.mail.Session;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Optional;

/**
 * 认证拦截器
 */
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private SysUserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //不是处理方法
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;

        //有@Authen注解的,并且当前没有登录的
        boolean isNeedLogin = (handlerMethod.hasMethodAnnotation(Authen.class)
                || handlerMethod.getMethod().getDeclaringClass().isAnnotationPresent(Authen.class))
                && null == getLoginedUser(request,response);

        if (isNeedLogin) {
            //未登录,直接返回异常信息
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter writer = null;
            try {
                writer = response.getWriter();
                String json = JsonUtils.toJson(new RestResult().setStatus(RestResultStatus.UNLOING).setMessage("您还未登录!"));
                writer.write(json);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                writer.close();
            }
            return false;
        }
        //没有@Authen注解或者已经登录了,不作拦截
        return true;
    }

    /**
     * 获取已经登录的用户
     */
    private SysUser getLoginedUser(HttpServletRequest request,HttpServletResponse response) {
        SysUser user = (SysUser) request.getSession().getAttribute(SessionKey.USER);
        //如果session里面没有,试试看从cookie里面拿
        if (null == user) {
            Optional<Cookie> cookieOptional = Arrays.stream(request.getCookies()).filter(c -> CookieKey.REMEBER_ME.equals(c.getName())).findAny();
            if(cookieOptional.isPresent()){
                //cookie里面有rememberMe,直接登录
                user= JsonUtils.as(EncryptUtils.aesDecrypt(cookieOptional.get().getValue()), SysUser.class);
                try {
                    user = userService.login(user);
                    request.getSession().setAttribute(SessionKey.USER,user);
                }catch (BusinessException e){
                    //这里登录不上,不能报错,但是要移除cookie
                    user = null;
                    Cookie cookie = cookieOptional.get();
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
        return user;
    }
}
