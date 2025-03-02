package com.ajaxjs.im.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 添加新的成员
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AddNewMember extends PageArg {
    /**
     * 用户 id
     */
    private long userId;

    private String dstName;
}
