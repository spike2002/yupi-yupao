package com.hsh.backend.common;

public class ResultUtils {

    public static  <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    public static  <T> BaseResponse<T> success(T data,String description) {
        return new BaseResponse<>(0, data, "ok",description);
    }

    public static  <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }
}
