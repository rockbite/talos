package com.talosvfx.talos.editor.socket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

@WebSocket
public class ServerHandler {

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        SocketServer.currentConnectedUsers.add(user);
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        SocketServer.currentConnectedUsers.remove(user);
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {

    }

    @OnWebSocketError
    public void onError (Session session, Throwable throwable) {
        throwable.printStackTrace();
    }

}
