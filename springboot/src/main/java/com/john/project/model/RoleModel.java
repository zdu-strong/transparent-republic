package com.john.project.model;

import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class RoleModel {

    private String id;

    private String name;

    private Date createDate;

    private Date updateDate;

    private List<PermissionRelationModel> permissionList;

}
