package PR4.server;

import io.rsocket.Payload;
import io.rsocket.util.DefaultPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class TaskHandlerTest {

    private TaskHandler taskHandler;

    @BeforeEach
    void setUp() {
        taskHandler = new TaskHandler();
    }

    @Test
    void testRequestResponse_validTaskId() {
        Mono<Payload> response = taskHandler.requestResponse(DefaultPayload.create("1"));

        StepVerifier.create(response)
                .expectNextMatches(payload -> payload.getDataUtf8().contains("Complete project"))
                .verifyComplete();
    }

    @Test
    void testRequestResponse_invalidTaskId() {
        Mono<Payload> response = taskHandler.requestResponse(DefaultPayload.create("999"));

        StepVerifier.create(response)
                .expectNextMatches(payload -> payload.getDataUtf8().contains("Task not found"))
                .verifyComplete();
    }

    @Test
    void testRequestStream() {
        Flux<Payload> stream = taskHandler.requestStream(DefaultPayload.create("stream"));

        StepVerifier.create(stream)
                .expectNextMatches(payload -> payload.getDataUtf8().contains("Complete project"))
                .expectNextMatches(payload -> payload.getDataUtf8().contains("Study for exams"))
                .verifyComplete();
    }

    /**
     * Задача добавлена
     */
    @Test
    void testFireAndForget() {
        TaskManager newTask = new TaskManager("3", "New Task", "New");
        Mono<Void> response = taskHandler.fireAndForget(DefaultPayload.create(newTask.toString()));

        StepVerifier.create(response)
                .verifyComplete();

        Mono<Payload> taskResponse = taskHandler.requestResponse(DefaultPayload.create("3"));
        StepVerifier.create(taskResponse)
                .expectNextMatches(payload -> payload.getDataUtf8().contains("New Task"))
                .verifyComplete();
    }

    /**
     * Задача обновлена
     */
    @Test
    void testRequestChannel() {
        Flux<Payload> payloads = Flux.just(
                DefaultPayload.create("3,New Task,In Progress"),
                DefaultPayload.create("2,Study for exams,Completed")
        );

        Flux<Payload> response = taskHandler.requestChannel(payloads);

        StepVerifier.create(response)
                .expectNextMatches(payload -> payload.getDataUtf8().contains("Updated task: 3"))
                .expectNextMatches(payload -> payload.getDataUtf8().contains("Updated task: 2"))
                .verifyComplete();

        Mono<Payload> task3Response = taskHandler.requestResponse(DefaultPayload.create("3"));
        Mono<Payload> task2Response = taskHandler.requestResponse(DefaultPayload.create("2"));

        StepVerifier.create(task3Response)
                .expectNextMatches(payload -> payload.getDataUtf8().contains("In Progress"))
                .verifyComplete();

        StepVerifier.create(task2Response)
                .expectNextMatches(payload -> payload.getDataUtf8().contains("Completed"))
                .verifyComplete();
    }
}

