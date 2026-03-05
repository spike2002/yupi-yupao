package com.hsh.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hsh.backend.common.BaseResponse;
import com.hsh.backend.common.ResultUtils;
import com.hsh.backend.mapper.UserTeamMapper;
import com.hsh.backend.model.dto.TeamQuery;
import com.hsh.backend.model.entity.User;
import com.hsh.backend.model.entity.UserTeam;
import com.hsh.backend.model.request.TeamAddRequest;
import com.hsh.backend.model.request.TeamUpdateRequest;
import com.hsh.backend.model.vo.TeamDetailVo;
import com.hsh.backend.model.vo.TeamListVo;
import com.hsh.backend.service.TeamService;
import com.hsh.backend.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/list")
    public BaseResponse<List<TeamListVo>> listTeams(@RequestBody TeamQuery teamQuery, HttpServletRequest request) {
        List<TeamListVo> list = teamService.listTeams(teamQuery, request);
        return ResultUtils.success(list);
    }

    @GetMapping("/list/page")
    public BaseResponse<Page<TeamListVo>> listTeamsByPage(@RequestBody TeamQuery teamQuery, HttpServletRequest request, @RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        Page<TeamListVo> page = teamService.listTeamsByPage(teamQuery, request, pageNum, pageSize);
        return ResultUtils.success(page);
    }

    @GetMapping("/list/mycreate")
    public BaseResponse<List<TeamListVo>> listMyCreateTeams(@RequestBody TeamQuery teamQuery, HttpServletRequest request) {
        List<TeamListVo> list = teamService.listMyCreateTeams(teamQuery, request);
        return ResultUtils.success(list);
    }

    @GetMapping("/list/myjoin")
    public BaseResponse<List<TeamListVo>> listMyJoinTeams(@RequestBody TeamQuery teamQuery, HttpServletRequest request) {
        List<TeamListVo> list = teamService.listMyJoinTeams(teamQuery, request);
        return ResultUtils.success(list);
    }

    @GetMapping("/getdetail")
    public BaseResponse<TeamDetailVo> getTeamDetailById(Long teamId, HttpServletRequest request) {
        TeamDetailVo teamDetailVo=teamService.getTeamDetailById(teamId,request);
        return ResultUtils.success(teamDetailVo);
    }
}
