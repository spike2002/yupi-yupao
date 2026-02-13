package com.hsh.backend.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class UserRegisterRequest {
    /**
     * 用户账号
     */
    @NonNull
    private String userAccount;
    /**
     * 用户密码
     */
    @NonNull
    private String userPassword;
    /**
     * 二次确认密码
     */
    @NonNull
    private String checkPassword;
    /**
     * 星球码
     */
    @NonNull
    private String planetCode;
}
