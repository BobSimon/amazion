package com.amzics.model.pojo;


import java.io.Serializable;
import java.util.HashMap;

/**
 * 返回结果对象
 */
public class RestResult extends HashMap<String, Object> implements Serializable {
    public RestResult() {
    }

    public RestResult(int initialCapacity) {
        super(initialCapacity);
    }

    public RestResult(Integer status, String message, Object data) {
        super(4);
        this.setStatus(status);
        this.setMessage(message);
        this.setData(data);
    }

    /**
     * status
     */
    public RestResult setStatus(Integer status) {
        if (null != status) {
            this.put("status", status);
        }
        return this;
    }

    /**
     * message
     */
    public RestResult setMessage(String message) {
        if (null != message) {
            this.put("msg", message);
        }
        return this;
    }

    /**
     * date
     */
    public RestResult setData(Object data) {
        if (null != data) {
            this.put("data", data);
        }
        return this;
    }
}
