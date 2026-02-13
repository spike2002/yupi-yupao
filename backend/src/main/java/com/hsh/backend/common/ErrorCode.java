package com.hsh.backend.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NULL_ERROR(40001, "请求数据为空"),
    NOT_LOGIN(40002, "未登录"),
    NO_AUTH(40003, "没有授权"),
    SYSTEM_ERROR(50000, "系统内部错误");

    private final int code;

    private final String message;

}
