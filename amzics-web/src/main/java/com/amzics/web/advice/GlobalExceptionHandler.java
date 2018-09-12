package com.amzics.web.advice;

import com.amzics.common.consts.RedisKey;
import com.amzics.common.consts.RequestKey;
import com.amzics.common.consts.RestResultStatus;
import com.amzics.common.consts.SessionKey;
import com.amzics.common.utils.JsonUtils;
import com.amzics.model.domain.SysUser;
import com.amzics.model.exception.BusinessException;
import com.amzics.model.pojo.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常捕获
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Value("${amzics.throw-exception-stack}")
    private Boolean throwExceptionStack;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public RestResult businessException(BusinessException ex) {
        return new RestResult().setMessage(ex.getMessage()).setStatus(ex.getResultCode());
    }

    /**
     * Bean 参数校验异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(value = {BindException.class})
    @ResponseBody
    public RestResult bindException(BindException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        List<String> errorMessages = bindingResult.getAllErrors().stream().map(e -> e.getDefaultMessage()).collect(Collectors.toList());
        RestResult result = new RestResult(2).setStatus(RestResultStatus.ERROR);
        result.setMessage(String.join(",", errorMessages));
        return result;
    }

    /**
     * 单个参数校验异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(value = {ConstraintViolationException.class})
    @ResponseBody
    public RestResult constraintViolationException(ConstraintViolationException ex) {
        List<String> errorMessages = ex.getConstraintViolations().stream().map(v -> v.getMessage()).collect(Collectors.toList());
        RestResult result = new RestResult(2).setStatus(RestResultStatus.ERROR);
        result.setMessage(String.join(",", errorMessages));
        return result;
    }

    /**
     * 意外异常,记录日志
     */
    @ExceptionHandler(value = {RuntimeException.class})
    @ResponseBody
    public RestResult exception(RuntimeException ex, HttpServletRequest request, HttpServletResponse response) {
        //region message
        Map<String, Object> requestInfo = new HashMap<>(4);
        //追踪码
        Long logCode = redisTemplate.opsForValue().increment(RedisKey.ERROR_LOG_CODE, 1);
        requestInfo.put("logCode", logCode);
        //url
        requestInfo.put("url", request.getRequestURI());
        //Parameters
        requestInfo.put("params", request.getParameterMap());
        //HttpHeader
        requestInfo.put("headers", Collections.list(request.getHeaderNames()).stream().collect(Collectors.toMap(n -> n, n -> request.getHeader(n))));
        //当前登陆用户
        SysUser user = (SysUser) request.getSession().getAttribute(SessionKey.USER);
        requestInfo.put("userId", null == user ? null : user.getId());
        //耗时
        Long requestStartTime = (Long) request.getAttribute(RequestKey.REQUEST_START_TIME);
        requestInfo.put("timespan", null == requestStartTime ? null : (System.currentTimeMillis() - requestStartTime.longValue() + " ms"));
        String message = JsonUtils.toJson(requestInfo);
        //endregion
        log.error(message, ex);
        //是否将异常堆栈信息返回给前端? 默认是关闭
        throwExceptionStack = null == throwExceptionStack ? false : throwExceptionStack;
        if (throwExceptionStack) {
                throw ex;
        }
        return new RestResult().setStatus(RestResultStatus.ERROR).setMessage(String.format("系统异常！追踪码：%s", logCode));
    }
}
