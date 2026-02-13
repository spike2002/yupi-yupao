package com.hsh.backend.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {

    private int code;

    private T data;

    private String message;

    private String description;

    public BaseResponse(int code, T data, String message) {
//        this.code = code;
//        this.data = data;
//        this.message = message;
//        this.description = " ";
        this(code, data, message, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }

}
