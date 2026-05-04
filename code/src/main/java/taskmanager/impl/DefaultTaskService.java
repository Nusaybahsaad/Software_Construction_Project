package taskmanager.impl;

import java.util.ArrayList;
import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import taskmanager.api.TaskService;
import taskmanager.exception.InvalidTaskException;
import taskmanager.exception.TaskNotFoundException;
import taskmanager.model.Task;

/**
 * Implementation of TaskService that stores tasks in memory using an ArrayList.
 * This class handles CRUD (Create, Read, Update, Delete) operations reactively.
 */
public class DefaultTaskService implements TaskService {

    /** The in-memory list where all tasks are stored during runtime. */
    private final List<Task> tasks = new ArrayList<>();

    /**
     * Adds a new task to the list.
     * <p><b>Precondition:</b> Task object and its title must not be null or empty.</p>
     * @param task The task to be added.
     * @return A Mono<Void> that signals completion or an error if the task is invalid.
     */
    @Override
    public Mono<Void> addTask(Task task) {
        return Mono.fromRunnable(() -> {
            if (task == null || task.getTitle() == null || task.getTitle().isEmpty()) {
                throw new InvalidTaskException("Invalid task data: Title is required.");
            }
            tasks.add(task);
        });
    }

    /**
     * Removes a task from the list by its ID.
     * @param taskId The unique identifier of the task.
     * @return A Mono<Void> that signals completion or TaskNotFoundException if ID doesn't exist.
     */
    @Override
    public Mono<Void> removeTask(String taskId) {
        return Mono.fromRunnable(() -> {
            boolean removed = tasks.removeIf(t -> t.getId().equals(taskId));
            if (!removed) {
                throw new TaskNotFoundException(taskId);
            }
        });
    }

    /**
     * Finds a specific task by its ID using Java Streams.
     * @param taskId The unique identifier.
     * @return A Mono containing the Task if found, or an error signal if not.
     */
    @Override
    public Mono<Task> findTaskById(String taskId) {
        return Mono.fromCallable(() ->
            tasks.stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new TaskNotFoundException(taskId))
        );
    }

    /**
     * Returns all stored tasks as a Flux stream.
     * @return A Flux emitting all tasks one by one.
     */
    @Override
    public Flux<Task> findAllTasks() {
        return Flux.fromIterable(tasks);
    }

    /**
     * Collects all tasks into a single list wrapped in a Mono.
     * @return A Mono containing the full list of tasks.
     */
    @Override
    public Mono<List<Task>> findAllTasksAsList() {
        return findAllTasks().collectList();
    }
}