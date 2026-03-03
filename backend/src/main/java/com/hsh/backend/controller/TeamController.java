package com.hsh.backend.controller;

import com.hsh.backend.common.BaseResponse;
import com.hsh.backend.common.ResultUtils;
import com.hsh.backend.model.request.TeamAddRequest;
import com.hsh.backend.model.request.TeamUpdateRequest;
import com.hsh.backend.service.TeamService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/team")
@Tag(name = "TeamController")
@Slf4j
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        Long res= teamService.addTeam(teamAddRequest,request);
        return ResultUtils.success(res);
    }

    @PostMapping("/update")
    public BaseResponse<Long> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest,HttpServletRequest request){
        Long res=teamService.updateTeam(teamUpdateRequest,request);
        return ResultUtils.success(res);
    }
}
