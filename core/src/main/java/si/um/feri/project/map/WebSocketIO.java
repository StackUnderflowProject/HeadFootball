package si.um.feri.project.map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class WebSocketIO {
    private static Socket socket;
    private Emitter.Listener onMessageReceived;

    public WebSocketIO(Emitter.Listener onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public void connect(String url) {
        try {
            socket = IO.socket(url);
            socket.on(Socket.EVENT_CONNECT, onConnect);
            socket.on("update-match", onMessageReceived);
            socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            socket.connect();
        } catch (Exception e) {
            System.out.println("Failed to create socket :(");
        }
    }

    public void dispose() {
        if (socket != null) socket.disconnect();
    }

    private final Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("Connected to the server!");
        }
    };

    private final Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("Disconnected from server.");
        }
    };

    private final Emitter.Listener onError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (args.length > 0) {
                Exception error = (Exception) args[0];
                System.out.println("Socket.IO error: " + error.getMessage());
            } else {
                System.out.println("Socket.IO error occurred.");
            }
        }
    };

    private final Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (args.length > 0) {
                Exception error = (Exception) args[0];
                System.out.println("Socket.IO connect error: " + error.getMessage());
            } else {
                System.out.println("Socket.IO connect error occurred.");
            }
        }
    };
}
