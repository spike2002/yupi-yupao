package com.hsh.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hsh.backend.common.ErrorCode;
import com.hsh.backend.exception.BusinessException;
import com.hsh.backend.mapper.UserTeamMapper;
import com.hsh.backend.model.entity.Team;
import com.hsh.backend.model.entity.User;
import com.hsh.backend.model.entity.UserTeam;
import com.hsh.backend.model.enums.TeamStatusEnum;
import com.hsh.backend.model.request.TeamAddRequest;
import com.hsh.backend.model.request.TeamUpdateRequest;
import com.hsh.backend.service.TeamService;
import com.hsh.backend.mapper.TeamMapper;
import com.hsh.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.hsh.backend.model.enums.TeamStatusEnum.PRIVATE;
import static com.hsh.backend.model.enums.TeamStatusEnum.getTeamStatusEnumByCode;

/**
 * @author 24496
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2026-02-12 10:33:38
 */
@Service
@Slf4j
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    private final UserService userService;
    private final TeamMapper teamMapper;
    private final UserTeamMapper userTeamMapper;
    private final RedissonClient redissonClient;

    public TeamServiceImpl(UserService userService, TeamMapper teamMapper, UserTeamMapper userTeamMapper, RedissonClient redissonClient) {
        this.userService = userService;
        this.teamMapper = teamMapper;
        this.userTeamMapper = userTeamMapper;
        this.redissonClient = redissonClient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addTeam(TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        //参数校验
        //1.用户未登录不能创建队伍
        User loginUser = userService.getCurrent(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //2.队伍名称<20
        String name = team.getName();
        if (Strings.isBlank(name) || name.length() >= 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名称应小于20");
        }
        //3.队伍描述<512
        String description = team.getDescription();
        if (Strings.isBlank(description) || description.length() >= 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述应小于512");
        }
        //4.1<=队伍人数<=10
        //防止出现NPE
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum > 10 || maxNum < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数应大于等于1小于等于10");
        }
        //5.若队伍status为空设为0
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum teamStatus = getTeamStatusEnumByCode(status);
        if (teamStatus == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态异常");
        }
        //6.若队伍status不为空为1，密码要小于32位
        if (teamStatus == PRIVATE) {
            String password = team.getPassword();
            if (Strings.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍密码不合规");
            }
        }
        //7.超时时间>当前时间
        Date expireTime = team.getExpireTime();
        log.info("expireTime {}", expireTime);
        if (expireTime == null || expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间错误");
        }
        RLock rLock = redissonClient.getLock(String.format("yupao:addTeam:userId:%s", loginUser.getUserId()));
        try {
            if (!rLock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "正在创建中，请勿频繁点击");
            }
            //8.每个用户最多创建20个队伍
            QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", loginUser.getUserId());
            Long count = teamMapper.selectCount(queryWrapper);
            if (count >= 20) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户创建队伍超过20个");
            }
            //todo 这块有一个幂等性的问题
            //9.team表插入数据
            int insert = teamMapper.insert(team);
            Long teamId = team.getTeamId();
            if (insert != 1) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "team表插入错误");
            }
            //10.user_team表插入数据
            UserTeam userTeam = new UserTeam();
            userTeam.setUserId(loginUser.getUserId());
            userTeam.setTeamId(teamId);
            userTeam.setJoinTime(new Date());
            int insert1 = userTeamMapper.insert(userTeam);
            if (insert1 != 1) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "user_team表插入错误");
            }
            //sleep5秒
//            Thread.sleep(5000);
            return teamId;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
    }

    @Override
    public Long updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = teamMapper.selectById(teamUpdateRequest.getTeamId());
        //参数校验
        //1.用户未登录不能创建队伍
        User loginUser = userService.getCurrent(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //2.用户非管理员或者创建者
        if (!userService.isAdmin(request) || !loginUser.getUserId().equals(oldTeam.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH,"用户非管理员或者创建者");
        }
        //2.队伍名称<20
        String name = teamUpdateRequest.getName();
        if (Strings.isBlank(name) || name.length() >= 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名称应小于20");
        }
        //3.队伍描述<512
        String description = teamUpdateRequest.getDescription();
        if (Strings.isBlank(description) || description.length() >= 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述应小于512");
        }
        //4.1<=队伍人数<=10
        //防止出现NPE
        int maxNum = Optional.ofNullable(teamUpdateRequest.getMaxNum()).orElse(0);
        if (maxNum > 10 || maxNum < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数应大于等于1小于等于10");
        }
        //5.若队伍status为空设为0
        int status = Optional.ofNullable(teamUpdateRequest.getStatus()).orElse(0);
        TeamStatusEnum teamStatus = getTeamStatusEnumByCode(status);
        if (teamStatus == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态异常");
        }
        //6.若队伍status不为空为1，密码要小于32位
        if (teamStatus == PRIVATE) {
            String password = teamUpdateRequest.getPassword();
            if (Strings.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍密码不合规");
            }
        }
        //7.超时时间>当前时间
        Date expireTime = teamUpdateRequest.getExpireTime();
        log.info("expireTime {}", expireTime);
        if (expireTime == null || expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间错误");
        }
        Team newTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest,newTeam);
        int res = teamMapper.updateById(newTeam);
        return (long) res;
    }
}




