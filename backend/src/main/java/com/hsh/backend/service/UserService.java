package com.hsh.backend.service;

import com.hsh.backend.model.request.UserLoginRequest;
import com.hsh.backend.model.request.UserRegisterRequest;
import com.hsh.backend.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @author 24496
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2026-01-16 08:46:17
 */
public interface UserService {

    long userRegister(UserRegisterRequest UserRegisterRequest);

    User userLogin(UserLoginRequest UserLoginRequest, HttpServletRequest request);

    long userLogout(HttpServletRequest request);

    User getCurrent(HttpServletRequest request);

    List<User> searchUsers(String userAccount, HttpServletRequest request);

    Long deleteUser(Long id, HttpServletRequest request);

    Long addTags(List<String> tags, HttpServletRequest request);
}
