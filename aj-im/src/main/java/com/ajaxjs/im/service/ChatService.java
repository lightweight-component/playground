package com.ajaxjs.im.service;


import com.ajaxjs.im.common.CommandConstants;
import com.ajaxjs.im.model.Message;
import com.ajaxjs.im.model.Node;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.websocket.*;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@Slf4j
public class ChatService {
    /**
     * 用户和 Node 映射关系表
     */
    private Map<Long, Node> clientMap = new ConcurrentHashMap<>();

    // 读写锁
    /**
     * 读写锁
     */
    private ReentrantReadWriteLock rwLocker = new ReentrantReadWriteLock();

    // 后端调度逻辑处理
    public void dispatch(byte[] data) {
//        ObjectMapper mapper = new ObjectMapper();

        try {
            Message msg = mapper.readValue(data, Message.class);
            switch (msg.getCmd()) {
                case CommandConstants.CMD_SINGLE_MSG:
                    sendMsg(msg.getDstid(), data);
                    break;
                case CommandConstants.CMD_ROOM_MSG:
                    // 遍历clientMap并发送消息
                    break;
                case CommandConstants.CMD_HEART:
                    // 检测客户端的心跳
                    break;
            }
        } catch (IOException e) {
            log.error("Error deserializing message", e);
        }
    }

    /**
     * 添加新的群ID到用户的 groupset 中
     *
     * @param userId
     * @param gid
     */
    public void addGroupId(Long userId, Long gid) {
        rwLocker.writeLock().lock();

        try {
            Node node = clientMap.get(userId);
            if (node != null)
                node.getGroupSets().add(gid);
        } finally {
            rwLocker.writeLock().unlock();
        }
    }

    /**
     * 发送逻辑
     *
     * @param node
     */
    public void sendProc(Node node) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    byte[] data = node.getDataQueue().poll(1, TimeUnit.SECONDS);

                    if (data != null)
                        node.getConn().getBasicRemote().sendText(new String(data));
                } catch (IOException | InterruptedException e) {
                    log.error("Error sending message", e);
                }
            }
        });
    }

    /**
     * 接收逻辑
     * 需要使用 Java WebSocket API 处理接收逻辑
     *
     * @param node
     */
    public void recvProc(Node node) {
        Session session = node.getConn();

        while (true) {
            try {
                // 接收消息
                String data = session.getBasicRemote().receiveText();
                // 分发消息处理
                dispatch(data);
                // 进一步处理接收到的数据
                System.out.printf("recv<=%s", data);
            } catch (IOException e) {
                // 打印异常信息
                e.printStackTrace();
                break; // 如果出现异常，退出循环
            }
        }
    }

    /**
     * 发送消息,发送到消息的管道
     *
     * @param userId
     * @param msg
     */
    public void sendMsg(Long userId, byte[] msg) {
        rwLocker.readLock().lock();

        try {
            Node node = clientMap.get(userId);
            if (node != null)
                node.getDataQueue().offer(msg);
        } finally {
            rwLocker.readLock().unlock();
        }
    }

    /**
     * 校验token是否合法
     *
     * @param userId
     * @param token
     * @return
     */
    public boolean checkToken(Long userId, String token) {
        // 需要实现UserService和Find方法
        return false;
    }
}
