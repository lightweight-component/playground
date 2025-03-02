package com.ajaxjs.im.model;

import lombok.Data;

import java.util.Date;

/**
 * 好友和群都存在这个表里面 可根据具体业务做拆分
 */
@Data
public class Contact {
    /**
     * 唯一标识ID，主键，自动增长
     */
    private long id;

    /**
     * 记录拥有者的用户ID
     */
    private long ownerId;

    /**
     * 对端信息的用户或群组ID
     */
    private long dstObj;

    /**
     * 类型，表示联系人的分类或属性
     */
    private int cate;

    /**
     * 备注信息，关于联系人的描述
     */
    private String memo;

    /**
     * 创建时间，记录何时创建这个联系人
     */
    private Date createAt;
}
