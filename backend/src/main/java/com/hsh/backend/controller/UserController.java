package com.hsh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hsh.backend.common.BaseResponse;
import com.hsh.backend.common.ResultUtils;
import com.hsh.backend.model.request.UserLoginRequest;
import com.hsh.backend.model.request.UserRegisterRequest;
import com.hsh.backend.model.entity.User;
import com.hsh.backend.model.request.UserUpdateRequest;
import com.hsh.backend.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@Tag(name = "UserController")
@RestController
@RequestMapping("/user")
public class UserController {

    final
    UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册
     *
     * @param UserRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest UserRegisterRequest) {
        long res = userService.userRegister(UserRegisterRequest);
        return ResultUtils.success(res, "用户注册成功");
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest UserLoginRequest, HttpServletRequest request) {
        User user = userService.userLogin(UserLoginRequest, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Long> userLogout(HttpServletRequest request) {
        long res = userService.userLogout(request);
        return ResultUtils.success(res);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrent(HttpServletRequest request) {
        User current = userService.getCurrent(request);
        return ResultUtils.success(current);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String userAccount, HttpServletRequest request) {
        List<User> list = userService.searchUsers(userAccount, request);
        return ResultUtils.success(list);
    }

    @PostMapping("/delet")
    public BaseResponse<Long> deleteUser(@RequestBody Long id, HttpServletRequest request) {
        Long res = userService.deleteUser(id, request);
        return ResultUtils.success(res);
    }

    @PostMapping("/add/tags")
    public BaseResponse<Long> addTags(@RequestBody List<String> tags, HttpServletRequest request) {
        Long res = userService.addTags(tags, request);
        return ResultUtils.success(res);
    }

    @PostMapping("/delete/tags")
    public BaseResponse<Long> deleteTags(@RequestBody List<String> tags, HttpServletRequest request) {
        Long res = userService.deleteTags(tags, request);
        return ResultUtils.success(res);
    }

    @PostMapping("update")
    // todo session在前端是怎么保存的，在后端是怎么保存的，request里面都存的什么
    // todo 更新完了之后前端存的用户信息是不是还是浏览器缓存的用户信息，该怎么解决
    public BaseResponse<User> updateUserByMe(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        User user = userService.updateUserByMe(userUpdateRequest, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/update/admin")
    public BaseResponse<User> updateAdminByAdmin(@RequestBody UserUpdateRequest userUpdateRequest, @RequestParam Long id, HttpServletRequest request) {
        User user = userService.updateUserByAdmin(userUpdateRequest, id, request);
        return ResultUtils.success(user);
    }
//    @GetMapping("/test")
//    public String test(){
//        log.error("测试");
//        System.out.println("hello");
//        return "12345";
//    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommend(long pageSize,long pageNum,HttpServletRequest request){
        Page<User> userPage=userService.recommend(pageSize,pageNum,request);
        return ResultUtils.success(userPage);
    }
}
