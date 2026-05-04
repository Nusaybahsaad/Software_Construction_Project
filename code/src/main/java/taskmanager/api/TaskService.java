package taskmanager.api;

import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import taskmanager.model.Task;

/**
 * Service for task operations.
 */
public interface TaskService {

    Mono<Void> addTask(Task task);

    Mono<Void> removeTask(String taskId);

    Mono<Task> findTaskById(String taskId);

    Flux<Task> findAllTasks();

    Mono<List<Task>> findAllTasksAsList();
}