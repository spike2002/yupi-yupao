package com.hsh.backend.service.impl;

import java.lang.reflect.Type;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hsh.backend.common.ErrorCode;
import com.hsh.backend.model.request.UserLoginRequest;
import com.hsh.backend.model.request.UserRegisterRequest;
import com.hsh.backend.model.entity.User;
import com.hsh.backend.exception.BusinessException;
import com.hsh.backend.mapper.UserMapper;
import com.hsh.backend.model.request.UserUpdateRequest;
import com.hsh.backend.service.UserService;
import com.hsh.backend.utils.MinDistance;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hsh.backend.constant.UserConstant.ADMIN_ROLE;
import static com.hsh.backend.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author 24496
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2026-01-16 08:46:17
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private static final String SALT = "hsh";
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final Gson gson = new Gson();
//构造函数和@RequiredArgsConstructor冲突，因为@RequiredArgsConstructor注解会扫描final字段并自动生成构造函数，相同的构造函数会造成冲突
//    public UserServiceImpl(UserMapper userMapper) {
//        this.userMapper = userMapper;
//    }

    @Override
    public long userRegister(UserRegisterRequest UserRegisterRequest) {
        if (UserRegisterRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        String userAccount = UserRegisterRequest.getUserAccount();
        String userPassword = UserRegisterRequest.getUserPassword();
        String checkPassword = UserRegisterRequest.getCheckPassword();
        String planetCode = UserRegisterRequest.getPlanetCode();
        //校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "注册数据含有空值");
        }
        if (userAccount.length() < 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度小于3");
        }
        if (userPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度小于6");
        }
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球码长度大于5");
        }
        //正则表达式校验用户名只能包含大小写字母、数字和下划线
        if (!userAccount.matches("^[a-zA-Z0-9_]+$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号含有违禁符号");
        }
        if (!checkPassword.equals(userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入密码不相同");
        }
        //用户账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号重复");
        }
        //校验星球号码不能重复
        //注意：需新建 queryWrapper 或 clear 之前的条件，否则是叠加查询
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planet_code", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球码重复");
        }
        //加密用户密码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes(StandardCharsets.UTF_8));
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return user.getUserId();
    }

    @Override
    public User userLogin(UserLoginRequest UserLoginRequest, HttpServletRequest request) {
        if (UserLoginRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        String userAccount = UserLoginRequest.getUserAccount();
        String userPassword = UserLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "账号或密码为空");
        }
        if (userAccount.length() < 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (!userAccount.matches("^[a-zA-Z0-9_]+$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号含有违禁符号");
        }
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes(StandardCharsets.UTF_8));
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        //账号密码不匹配
        if (user == null) {
            log.info("登录失败,账号密码不匹配");
            return null;
        }
        User safeUser = getSafeUser(user);
        request.getSession().setAttribute(USER_LOGIN_STATE, user.getUserId());
        return safeUser;
    }

    @Override
    public long userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public User getCurrent(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Long userId = (Long) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return getSafeUser(userMapper.selectById(userId));
    }

    @Override
    public List<User> searchUsers(String userAccount, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "用户未登录或无相关权限");
        }
        if (userAccount == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("user_account", userAccount);
        List<User> list = userMapper.selectList(queryWrapper);
        List<User> res = list.stream().map(user -> getSafeUser(user)).toList();
        return res;
    }

    @Override
    public List<User> searchUsersByTags(List<String> tags, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "用户未登录或无权限");
        }
        if (tags == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        for (String tag : tags) {
            userQueryWrapper.like("tags", tag);
        }
        //其实写到这里也可以了，这样就是模糊查询，比如搜java会出来javascript，下面的话就是完全匹配查询
        List<User> userList = userMapper.selectList(userQueryWrapper);
        List<User> resList = userList.stream().filter(user -> {
            HashSet<String> tempSet = gson.fromJson(user.getTags(), new TypeToken<Set<String>>() {
            }.getType());
            return tempSet.containsAll(tags);
        }).map(this::getSafeUser).toList();
        return resList;
    }

    /**
     * 根据用户tag相似度返回匹配用户
     *
     * @param num
     * @param request
     * @return
     */
    @Override
    public List<User> match(Integer num, HttpServletRequest request) {
        if (num == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (num < 1 || num > 10) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "匹配用户数量不符合要求");
        }
//        User loginUser = getCurrent(request);
//        List<String> loginUserTags = gson.fromJson(loginUser.getTags(), new TypeToken<List<String>>() {
//        }.getType());
//        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
////        userQueryWrapper.select("user_id", "tags");
//        userQueryWrapper.isNotNull("tags");
//        List<User> userList = userMapper.selectList(userQueryWrapper);
//        List<Pair<User, Integer>> list = new ArrayList<>();
//        for (User user : userList) {
//            List<String> tags = gson.fromJson(user.getTags(), new TypeToken<List<String>>() {
//            }.getType());
//            int distance = MinDistance.getMinDistance(tags, loginUserTags);
//            list.add(Pair.of(user, distance));
//        }
//        //返回编辑距离从小到大排序的结果
//        List<User> resList = list.stream().sorted((a, b) -> {
//            return a.getSecond() - b.getSecond();
//        }).map(Pair::getFirst).map(this::getSafeUser).limit(num).toList();
//        return resList;
        //大根堆优化版本
        User loginUser = getCurrent(request);
        Type type = new TypeToken<List<String>>() {
        }.getType();
        //防止每个循环都新建一个实例
        List<String> loginUserTags = gson.fromJson(loginUser.getTags(), type);
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.isNotNull("tags");
        //空值保护，不然""空字符串gson序列化的时候会序列化成null，后续MinDistance.getMinDistance()会报NPE
        userQueryWrapper.ne("tags","");
        //避免查到用户本人
        userQueryWrapper.ne("user_id", loginUser.getUserId());
        List<User> userList = userMapper.selectList(userQueryWrapper);
        PriorityQueue<Pair<User, Integer>> maxHeap = new PriorityQueue<>((a, b) -> {
            return b.getSecond() - a.getSecond();
        });
        for (User user : userList) {
            List<String> tags = gson.fromJson(user.getTags(), type);
            int distance = MinDistance.getMinDistance(tags, loginUserTags);
            if (maxHeap.size() <= 10) {
                maxHeap.offer(Pair.of(user, distance));
            } else {
                if (distance < maxHeap.peek().getSecond()) {
                    maxHeap.poll();
                    maxHeap.offer(Pair.of(user, distance));
                }
            }
        }
        //返回编辑距离从小到大排序的结果
        List<User> list = maxHeap.stream().sorted((a, b) -> {
            return a.getSecond() - b.getSecond();
        }).limit(num).map(Pair::getFirst).map(this::getSafeUser).toList();
        return list;
    }

    @Override
    public Long deleteUser(Long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long res = (long) userMapper.deleteById(id);
        return res;
    }

    @Override
    public Long addTags(List<String> tags, HttpServletRequest request) {
        if (request == null || tags == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginuser = this.getCurrent(request);
        String tagstr = loginuser.getTags();
        Set<String> tempUserTagsSet = gson.fromJson(tagstr, new TypeToken<Set<String>>() {
        }.getType());
        tempUserTagsSet = Optional.ofNullable(tempUserTagsSet).orElse(new HashSet<>());
        tempUserTagsSet.addAll(tags);
        loginuser.setTags(gson.toJson(tempUserTagsSet));
        return (long) userMapper.updateById(loginuser);
    }

    @Override
    public Long deleteTags(List<String> tags, HttpServletRequest request) {
        if (request == null || tags == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginuser = this.getCurrent(request);
        String tagstr = loginuser.getTags();
        Set<String> tempUserTagsSet = gson.fromJson(tagstr, new TypeToken<Set<String>>() {
        }.getType());
        tempUserTagsSet = Optional.ofNullable(tempUserTagsSet).orElse(new HashSet<>());
        for (String tag : tags) {
            tempUserTagsSet.remove(tag);
        }
        loginuser.setTags(gson.toJson(tempUserTagsSet));
        return (long) userMapper.updateById(loginuser);
    }

    @Override
    public User updateUserByMe(UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        if (userUpdateRequest == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User toUpdate = new User();
        Long userId = (Long) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userId == null || userId < 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        toUpdate.setUserId(userId);
        toUpdate.setUserName(userUpdateRequest.getUserName());
        toUpdate.setUserAccount(userUpdateRequest.getUserAccount());
        toUpdate.setPhone(userUpdateRequest.getPhone());
        toUpdate.setEmail(userUpdateRequest.getEmail());
        int rows = userMapper.updateById(toUpdate);
        if (rows <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改失败");
        }
        return getSafeUser(userMapper.selectById(userId));
    }

    @Override
    public User updateUserByAdmin(UserUpdateRequest userUpdateRequest, Long id, HttpServletRequest request) {
        if (userUpdateRequest == null || id == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User toUpdate = new User();
        Long userId = (Long) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userId == null || userId < 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        toUpdate.setUserId(id);
        toUpdate.setUserName(userUpdateRequest.getUserName());
        toUpdate.setUserAccount(userUpdateRequest.getUserAccount());
        toUpdate.setPhone(userUpdateRequest.getPhone());
        toUpdate.setEmail(userUpdateRequest.getEmail());
        int rows = userMapper.updateById(toUpdate);
        if (rows <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改失败");
        }
        return getSafeUser(userMapper.selectById(id));
    }

    @Override
    public Page<User> recommend(Long pageSize, Long pageNum, HttpServletRequest request) {
        if (pageSize == null || pageNum == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = this.getCurrent(request);
        String recommendKey = String.format("yupi:yupao:user:recommend:%s", loginUser.getUserId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Page<User> userPage = (Page<User>) valueOperations.get(recommendKey);
        if (userPage != null) {
            return userPage;
        } else {
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userPage = userMapper.selectPage(new Page<>(pageNum, pageSize), userQueryWrapper);
            List<User> safeUsers = userPage.getRecords().stream().map(this::getSafeUser).toList();
            userPage.setRecords(safeUsers);
            try {
                valueOperations.set(recommendKey, userPage, 30, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.error("set redis key error,key={}", recommendKey, e);
            }
            return userPage;
        }
    }

    private boolean isAdmin(HttpServletRequest request) {
        User user = this.getCurrent(request);
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    private boolean isAdmin(User user) {
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    private User getSafeUser(User user) {
        if (user == null) {
            return null;
        }
        User safeUser = new User();
        safeUser.setUserId(user.getUserId());
        safeUser.setUserName(user.getUserName());
        safeUser.setUserAccount(user.getUserAccount());
        safeUser.setAvatarUrl(user.getAvatarUrl());
        safeUser.setGender(user.getGender());
        safeUser.setPhone(user.getPhone());
        safeUser.setEmail(user.getEmail());
        safeUser.setUserStatus(user.getUserStatus());
        safeUser.setUserRole(user.getUserRole());
        safeUser.setPlanetCode(user.getPlanetCode());
        safeUser.setTags(user.getTags());
        return safeUser;
    }
}

