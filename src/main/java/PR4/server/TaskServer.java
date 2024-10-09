package PR4.server;

import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.netty.server.TcpServerTransport;

public class TaskServer {
    public static void main(String[] args) {
        RSocketServer.create(SocketAcceptor.with(new TaskHandler()))
                .bind(TcpServerTransport.create(7000))
                .block()
                .onClose()
                .block();
    }
}
