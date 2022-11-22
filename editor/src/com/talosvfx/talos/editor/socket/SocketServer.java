package com.talosvfx.talos.editor.socket;

import com.talosvfx.talos.TalosMain;
import org.eclipse.jetty.websocket.api.Session;
import spark.Spark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * SocketServer is class which handles initialization, message broadcasting and basic configuration of Talos editor server.
 * Talos editor server is routine to communicate with Talos runtime to patch small changes without having to export the whole
 * project.
 */
public class SocketServer {

    private final static int DEFAULT_PORT = 42069;

    public static int SERVER_PORT;

    private static SocketServer instance;

    public static ArrayList<Session> currentConnectedUsers = new ArrayList<>();

    private SocketServer() {
        SERVER_PORT = TalosMain.Instance().Prefs().getInteger("serverPort", DEFAULT_PORT);
        Spark.initExceptionHandler(new Consumer<Exception>() {
            @Override
            public void accept(Exception e) {
                e.printStackTrace();
            }
        });

        Spark.port(SERVER_PORT);
        Spark.webSocket("/talos", ServerHandler.class);
        Spark.init();
    }

    public static void dispose () {
        if (instance != null) {
            Spark.stop();
        }
    }

    public static SocketServer getInstance() {
        if (instance == null) {
            instance = new SocketServer();
        }
        return instance;
    }

    public static void broadcastPatch(String patch) {
        currentConnectedUsers.stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(patch);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void restartServer() {
        Spark.stop();
        Spark.awaitStop();

        Spark.port(SERVER_PORT);
        Spark.webSocket("/talos", ServerHandler.class);
        Spark.init();
    }
}

