package com.hsh.backend.model.vo;

import com.hsh.backend.model.entity.User;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TeamDetailVo {

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
     * 队伍用户列表
     */
    List<User> userList;

}
