package com.ajaxjs.im.model;

import lombok.Data;

@Data
public class Message {
    /**
     * 消息ID
     */
    private long id;

    /**
     * 发送者的用户ID
     */
    private long userid;

    /**
     * 消息类型，例如群聊还是私聊
     */
    private int cmd;

    /**
     * 对端用户ID或群ID
     */
    private long dstid;

    /**
     * 消息展示样式
     */
    private int media;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息预览图片的URL
     */
    private String pic;

    /**
     * 相关服务的URL
     */
    private String url;

    /**
     * 消息的简单描述
     */
    private String memo;

    /**
     * 与数字相关的其他信息，例如金额
     */
    private int amount;
}
