package PR4.server;

import org.h2.util.Task;

public class TaskManager extends Task {
    private final String id;
    private final String description;
    private final String status;

    public TaskManager(String id, String description, String status) {
        this.id = id;
        this.description = description;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Task{id='" + id + "', description='" + description + "', status='" + status + "'}";
    }

    public static TaskManager fromString(String str) {
        String[] parts = str.split(",");
        return new TaskManager(parts[0], parts[1], parts[2]);
    }

    @Override
    public void call() throws Exception {

    }
}

