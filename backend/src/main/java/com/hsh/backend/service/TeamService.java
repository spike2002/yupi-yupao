package com.hsh.backend.service;

import com.hsh.backend.model.entity.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hsh.backend.model.request.TeamAddRequest;
import com.hsh.backend.model.request.TeamUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author 24496
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2026-02-12 10:33:38
*/
public interface TeamService extends IService<Team> {

    Long addTeam(TeamAddRequest teamAddRequest, HttpServletRequest request);

    Long updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest request);
}
