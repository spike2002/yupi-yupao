package com.hsh.backend.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum TeamStatusEnum {

    PUBLIC(0, "公开"),

    PRIVATE(1, "私有");

    public static TeamStatusEnum getTeamStatusEnumByCode(Integer code) {
        if (code == null) {
            return null;
        }
        TeamStatusEnum[] teamStatusEnums = TeamStatusEnum.values();
        for (TeamStatusEnum teamStatusEnum : teamStatusEnums) {
            if (teamStatusEnum.getCode() == code) {
                return teamStatusEnum;
            }
        }
        return null;
    }

    private int code;
    private String message;

}
