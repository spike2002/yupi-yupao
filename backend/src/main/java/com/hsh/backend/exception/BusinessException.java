package com.hsh.backend.exception;

import com.hsh.backend.common.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    /**
     * 异常码
     */
    private final int code;

    /**
     * 异常描述
     */
    private final String description;

    //手写构造函数，标准化异常类型，不能自定义异常
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = null;
    }

    public BusinessException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }
}
