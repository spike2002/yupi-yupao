package com.hsh.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hsh.backend.common.BaseResponse;
import com.hsh.backend.common.ResultUtils;
import com.hsh.backend.mapper.UserTeamMapper;
import com.hsh.backend.model.dto.TeamQuery;
import com.hsh.backend.model.entity.User;
import com.hsh.backend.model.entity.UserTeam;
import com.hsh.backend.model.request.TeamAddRequest;
import com.hsh.backend.model.request.TeamUpdateRequest;
import com.hsh.backend.model.vo.TeamListVo;
import com.hsh.backend.service.TeamService;
import com.hsh.backend.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/team")
@Tag(name = "TeamController")
@Slf4j
public class TeamController {

    private final TeamService teamService;
    private final UserService userService;
    private final UserTeamMapper userTeamMapper;

    public TeamController(TeamService teamService, UserService userService, UserTeamMapper userTeamMapper) {
        this.teamService = teamService;
        this.userService = userService;
        this.userTeamMapper = userTeamMapper;
    }

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        Long res = teamService.addTeam(teamAddRequest, request);
        return ResultUtils.success(res);
    }

    @PostMapping("/update")
    public BaseResponse<Long> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        Long res = teamService.updateTeam(teamUpdateRequest, request);
        return ResultUtils.success(res);
    }

    @PostMapping("/list")
    public BaseResponse<List<TeamListVo>> listTeams(@RequestBody TeamQuery teamQuery, HttpServletRequest request) {
        //Controller 层实现插入hasJoin
        List<TeamListVo> list = teamService.listTeams(teamQuery, request);
        if(CollectionUtils.isEmpty(list)){
            return ResultUtils.success(list);
        }
        User loginUser = userService.getCurrent(request);
        Long userId = loginUser.getUserId();
//        list.forEach(teamVo -> teamVo.getTeamId());
        List<Long> teamIdList = list.stream().map(teamListVo -> teamListVo.getTeamId()).toList();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("user_id", userId);
        userTeamQueryWrapper.in("team_id", teamIdList);
        List<UserTeam> userTeams = userTeamMapper.selectList(userTeamQueryWrapper);
        Set<Long> hasJoinIdList = userTeams.stream().map(userTeam -> userTeam.getTeamId()).collect(Collectors.toSet());
        list.forEach(teamListVo -> {
            boolean hasJoin = hasJoinIdList.contains(teamListVo.getTeamId());
            teamListVo.setHasJoin(hasJoin);
        });
        return ResultUtils.success(list);
    }
}
