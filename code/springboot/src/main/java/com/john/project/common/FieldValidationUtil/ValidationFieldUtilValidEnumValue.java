package com.john.project.common.FieldValidationUtil;

import cn.hutool.core.util.EnumUtil;
import com.john.project.enums.OrganizeTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public abstract class ValidationFieldUtilValidEnumValue extends ValidationFieldUtilMustBeNull {

    public void checkValidOfOrganizeType(String organizeType) {
        if (StringUtils.isBlank(organizeType)) {
            return;
        }
        if (EnumUtil.getBy(OrganizeTypeEnum::getValue, organizeType) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect organizeType");
        }
    }

}
