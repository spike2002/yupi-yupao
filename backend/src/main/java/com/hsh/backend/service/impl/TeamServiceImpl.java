package com.hsh.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hsh.backend.model.entity.Team;
import com.hsh.backend.service.TeamService;
import com.hsh.backend.mapper.TeamMapper;
import org.springframework.stereotype.Service;

/**
* @author 24496
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2026-02-12 10:33:38
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

}




