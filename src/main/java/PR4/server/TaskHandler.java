package PR4.server;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.util.DefaultPayload;
import org.h2.util.Task;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class TaskHandler implements RSocket {
    private final Map<String, Task> taskRepo = new HashMap<>();

    public TaskHandler() {
        taskRepo.put("1", new TaskManager("1", "Complete project", "New"));
        taskRepo.put("2", new TaskManager("2", "Study for exams", "In Progress"));
    }

    @Override
    public Mono<Payload> requestResponse(Payload payload) {
        String taskId = payload.getDataUtf8();
        Task task = taskRepo.getOrDefault(taskId, new TaskManager("0", "Task not found", "Unknown"));
        return Mono.just(DefaultPayload.create(task.toString()));
    }

    @Override
    public Flux<Payload> requestStream(Payload payload) {
        return Flux.fromIterable(taskRepo.values())
                .map(task -> DefaultPayload.create(task.toString()))
                .delayElements(Duration.ofSeconds(1));
    }

    @Override
    public Mono<Void> fireAndForget(Payload payload) {
        TaskManager task = TaskManager.fromString(payload.getDataUtf8());
        taskRepo.put(task.getId(), task);
        return Mono.empty();
    }

    @Override
    public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
        return Flux.from(payloads)
                .map(Payload::getDataUtf8)
                .map(TaskManager::fromString)
                .doOnNext(task -> taskRepo.put(task.getId(), task))
                .map(task -> DefaultPayload.create("Updated task: " + task.getId()));
    }
}
