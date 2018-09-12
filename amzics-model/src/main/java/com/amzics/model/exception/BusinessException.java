package com.amzics.model.exception;

import com.amzics.common.consts.RestResultStatus;

public class BusinessException extends RuntimeException{
    private int resultCode = RestResultStatus.ERROR;

    public int getResultCode() {
        return resultCode;
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, int resultCode) {
        super(message);
        this.resultCode = resultCode;
    }
}
