package com.ajaxjs.im.model;

import lombok.Data;

import java.util.Date;

@Data
public abstract class PageArg {
    /**
     * 从哪页开始
     */
    private int pageFrom;

    /**
     * 每页大小
     */
    private int pageSize;

    /**
     * 关键词
     */
    private String kWord;

    /**
     * 升序字段
     */
    private String asc;

    /**
     * 降序字段
     */
    private String desc;

    /**
     * 名称
     */
    private String name;

    /**
     * 用户ID
     */
    private long userId; // 使用Java的long表示64位整数

    /**
     * 目标ID
     */
    private long dstId; // 使用Java的long表示64位整数

    /**
     * 时间点1
     */
    private Date dateFrom;

    /**
     * 时间点2
     */
    private Date dateTo;

    /**
     * 总数量
     */
    private long total;
}
