package com.hsh.backend.exception;

import com.hsh.backend.common.BaseResponse;
import com.hsh.backend.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandle(BusinessException e) {
        log.error("业务逻辑中断{}，原因是:{}", e.getCode(), e.getMessage());
        return new BaseResponse<>(e.getCode(), null, e.getMessage(), e.getDescription());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> businessExceptionHandle(RuntimeException e) {
        log.error("RuntimeException: " + e);
        return new BaseResponse<>(ErrorCode.SYSTEM_ERROR);
    }
}
