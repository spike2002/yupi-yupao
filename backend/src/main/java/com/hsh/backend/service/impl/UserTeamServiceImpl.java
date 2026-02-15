package com.hsh.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hsh.backend.model.entity.UserTeam;
import com.hsh.backend.service.UserTeamService;
import com.hsh.backend.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author 24496
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2026-02-12 10:33:51
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




