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
 * Service implementation for managing the lifecycle of tasks in memory.
 * This class handles CRUD operations using reactive programming.
 */
public class DefaultTaskService implements TaskService {

    /** The in-memory list where all tasks are stored during runtime. */
    private final List<Task> tasks = new ArrayList<>();

    /**
     * Adds a new task to the internal list.
     * 
     * Preconditions: The task object and its title must not be null or empty.
     * Postconditions: The task is added to the list, and the Mono completes
     * successfully.
     * Side-effects: Mutates the internal 'tasks' list by adding a new task.
     * 
     * @param task The task to be added.
     * @return A Mono signaling completion.
     * @throws InvalidTaskException if the precondition is violated.
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
     * Removes a task from the list based on its unique ID.
     * 
     * Preconditions: The taskId must exist in the system.
     * Postconditions: The task is removed from the storage list.
     * Side-effects: Mutates the internal 'tasks' list by removing an item.
     * 
     * @param taskId The ID of the task to delete.
     * @return A Mono signaling completion.
     * @throws TaskNotFoundException if the ID does not match any existing task.
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
     * Searches for a task by its ID.
     * 
     * Preconditions: The taskId must not be null.
     * Postconditions: Returns a Mono containing the requested task.
     * Side-effects: None (Read-only operation).
     * 
     * @param taskId The unique identifier.
     * @return A Mono containing the found task.
     * @throws TaskNotFoundException if no task is found with the given ID.
     */
    @Override
    public Mono<Task> findTaskById(String taskId) {
        return Mono.fromCallable(() -> tasks.stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new TaskNotFoundException(taskId)));
    }

    /**
     * Postconditions: Returns a Flux stream emitting all current tasks.
     * Side-effects: None.
     * 
     * @return A Flux of all tasks.
     */
    @Override
    public Flux<Task> findAllTasks() {
        return Flux.fromIterable(tasks);
    }

    /**
     * Postconditions: Collects all tasks and returns them as a list.
     * Side-effects: None.
     * 
     * @return A Mono containing the full list of tasks.
     */
    @Override
    public Mono<List<Task>> findAllTasksAsList() {
        return findAllTasks().collectList();
    }
}