package com.amzics.web.controller;

import com.amzics.common.consts.RestResultStatus;
import com.amzics.common.consts.SessionKey;
import com.amzics.model.domain.SysUser;
import com.amzics.model.pojo.RestResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;

public class BaseController {
    //region result

    /**
     * 响应结果
     */
    protected RestResult result(int status,String msg,Object data){
        return new RestResult().setStatus(status).setMessage(msg).setData(data);
    }

    //region sucess
    protected RestResult sucess(String msg,Object data){
        return this.result(RestResultStatus.SUCCESS,msg,data);
    }
    protected RestResult sucess(String msg){
        return this.result(RestResultStatus.SUCCESS,msg,null);
    }
    protected RestResult sucess(Object data){
        return this.result(RestResultStatus.SUCCESS,null,data);
    }
    //endregion

    //region error
    protected RestResult error(String msg,Object data){
        return this.result(RestResultStatus.ERROR,msg,data);
    }
    protected RestResult error(String msg){
        return this.result(RestResultStatus.ERROR,msg,null);
    }
    protected RestResult error(Object data){
        return this.result(RestResultStatus.ERROR,null,data);
    }
    //endregion

    //region download
    protected ResponseEntity<byte[]> download(String downloadName, byte[] buffer){
        //解决中文乱码问题
        String fileName = null;
        try {
            fileName = new String(downloadName.getBytes("utf-8"), "iso-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", fileName);
        ResponseEntity<byte[]> entity = new ResponseEntity<byte[]>(buffer, headers, HttpStatus.OK);
        return entity;
    }
    //endregion

    //endregion

    //region http
    protected HttpServletRequest getRequest(){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (null== requestAttributes){
            return null;
        }
        return requestAttributes.getRequest();
    }
    protected HttpSession getSession(){
        return getRequest().getSession();
    }
    protected HttpServletResponse getResponse(){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (null== requestAttributes){
            return null;
        }
        return requestAttributes.getResponse();
    }
    //endregion

    //region ext
    protected SysUser getUser(){
        return (SysUser) getSession().getAttribute(SessionKey.USER);
    }
    //endregion
}
