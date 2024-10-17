package PR4.server;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketConnector;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class TaskClient {
    public static void main(String[] args) {
        RSocket socket = RSocketConnector.create()
                .connect(TcpClientTransport.create("localhost", 7001))
                .block();

        // Request-Response: Получить задачу по ID
        assert socket != null;
        Mono<String> response = socket.requestResponse(DefaultPayload.create("1"))
                .map(Payload::getDataUtf8);

        response.subscribe(System.out::println);

        // Request-Stream: Получить поток задач
        Flux<String> taskStream = socket.requestStream(DefaultPayload.create("stream"))
                .map(Payload::getDataUtf8);

        taskStream.subscribe(System.out::println);

        // Fire-and-Forget: Создать задачу
        TaskManager newTaskManager = new TaskManager("3", "New Task", "New");
        socket.fireAndForget(DefaultPayload.create(newTaskManager.toString())).subscribe();

        // Channel: Обновление задач
        Flux<String> channelResponse = socket.requestChannel(
                Flux.just(
                        DefaultPayload.create("3,Update Task,In Progress"),
                        DefaultPayload.create("2,Study for exams,Completed")
                )
        ).map(Payload::getDataUtf8);

        channelResponse.subscribe(System.out::println);

        socket.onClose().block();
    }
}
