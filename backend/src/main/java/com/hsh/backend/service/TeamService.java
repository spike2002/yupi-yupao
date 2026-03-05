package com.hsh.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hsh.backend.model.dto.TeamQuery;
import com.hsh.backend.model.entity.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hsh.backend.model.request.TeamAddRequest;
import com.hsh.backend.model.request.TeamUpdateRequest;
import com.hsh.backend.model.vo.TeamDetailVo;
import com.hsh.backend.model.vo.TeamListVo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author 24496
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2026-02-12 10:33:38
*/
public interface TeamService extends IService<Team> {

    Long addTeam(TeamAddRequest teamAddRequest, HttpServletRequest request);

    Long updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest request);

    List<TeamListVo> listTeams(TeamQuery teamQuery, HttpServletRequest request);

    Page<TeamListVo> listTeamsByPage(TeamQuery teamQuery, HttpServletRequest request, Integer pageNum, Integer pageSize);

    List<TeamListVo> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request);

    List<TeamListVo> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request);

    TeamDetailVo getTeamDetailById(Long teamId, HttpServletRequest request);
}
