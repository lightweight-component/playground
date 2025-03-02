package com.ajaxjs.im.controller;

import org.springframework.web.socket.*;

import java.io.IOException;

public class ChatHandler implements WebSocketHandler {
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("Opened connection with id " + session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException {
        String payload = (String) message.getPayload();
        System.out.println("Received message: " + payload);
        session.sendMessage(new TextMessage("Echo: " + payload));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.out.println("Error in connection with id " + session.getId());
        exception.printStackTrace();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        System.out.println("Closed connection with id " + session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
