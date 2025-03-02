package com.ajaxjs.im.model;

import lombok.Data;
import javax.websocket.*;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 */
@Data
public class Node {
    private Session conn;

    /**
     *  阻塞队列
     */
    private BlockingQueue<byte[]> dataQueue;
    private Set<Object> groupSets;

    public Node(Session conn) {
        this.conn = conn;
        this.dataQueue = new LinkedBlockingQueue<>(50);
        this.groupSets = ConcurrentHashMap.newKeySet();
    }
}
