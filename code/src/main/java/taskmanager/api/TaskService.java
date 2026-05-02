package taskmanager.api;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

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