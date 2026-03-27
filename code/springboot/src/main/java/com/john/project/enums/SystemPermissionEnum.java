package com.john.project.enums;

import java.util.List;
import java.util.Optional;

import cn.hutool.core.util.EnumUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SystemPermissionEnum {

    SUPER_ADMIN("SUPER_ADMIN", true, false, List.of()),
    ORGANIZE_MANAGE("ORGANIZE_MANAGE", false, true, List.of()),
    ORGANIZE_VIEW("ORGANIZE_VIEW", false, true, List.of()),
    ANONYMOUS("ANONYMOUS", false, true, List.of(OrganizeTypeEnum.COUNTRY)),
    RESIDENT("RESIDENT", false, true, List.of(OrganizeTypeEnum.COUNTRY)),
    IMMIGRANT("IMMIGRANT", false, true, List.of(OrganizeTypeEnum.COUNTRY)),
    CITIZEN("CITIZEN", false, true, List.of(OrganizeTypeEnum.COUNTRY)),
    COMMUNITY_SUPERVISOR("COMMUNITY_SUPERVISOR", false, true, List.of()),
    NATIONAL_SUPERVISORY_COUNCIL("NATIONAL_SUPERVISORY_COUNCIL", false, true, List.of()),
    STATE_SUPERVISORY_COUNCIL("STATE_SUPERVISORY_COUNCIL", false, true, List.of()),
    ;

    private String value;
    private Boolean isSuperAdmin;
    private Boolean isOrganizeRole;
    private List<OrganizeTypeEnum> organizeType;

    public static SystemPermissionEnum parse(String value) {
        return Optional.ofNullable(EnumUtil.getBy(SystemPermissionEnum::getValue, value)).get();
    }
}