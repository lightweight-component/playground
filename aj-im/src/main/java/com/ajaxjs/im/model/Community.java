package com.ajaxjs.im.model;

import lombok.Data;

import java.util.Date;

@Data
public class Community {
    /**
     * 群组ID，主键，自动增长
     */
    private long id;

    /**
     * 名称，最大长度30
     */
    private String name;

    /**
     * 群主ID
     */
    private long ownerId;

    /**
     * 群logo，最大长度250
     */
    private String icon;

    /**
     * 群的类型
     */
    private int cate;

    /**
     * 群组备注，最大长度120
     */
    private String memo;

    /**
     * 创建时间
     */
    private Date createAt;
}
