package com.hsh.backend.model.vo;

import com.hsh.backend.model.entity.User;
import lombok.Data;

import java.util.Date;

@Data
public class TeamListVo {
    /**
     * id
     */
    private Long teamId;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 队长
     */
    private User createUser;

    /**
     * 队伍人数
     */
    private Integer hasJoinNum;

    /**
     * 登录用户是否加入，默认为true
     */
    private boolean hasJoin = true;

}
