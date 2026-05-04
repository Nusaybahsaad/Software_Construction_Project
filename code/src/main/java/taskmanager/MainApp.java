package taskmanager;
import java.time.LocalDateTime;

import taskmanager.api.TaskManager;
import taskmanager.model.Task;
import taskmanager.ui.SmartTaskManagerFrame;

public class MainApp {

    public static void main(String[] args) {
        // Build the TaskManager (students will implement DefaultTaskManager)
        TaskManager tm = TaskManager.builder()
                .withWeatherApiKey("37af483a0484d5697761139b6b392727")
                .build();

        // Add a couple of test tasks
        Task task1 = new Task(
                "task-001",
                "Morning run",
                LocalDateTime.now().plusHours(2),
                true
        );
        Task task2 = new Task(
                "task-002",
                "Coding session",
                LocalDateTime.now().plusHours(4),
                false
        );

        tm.addTask(task1);
        tm.addTask(task2);

        System.out.println("Tasks loaded: " + tm.getTasks().size());

        // Wire this to the Swing UI
        SmartTaskManagerFrame frame = new SmartTaskManagerFrame(tm);
        javax.swing.SwingUtilities.invokeLater(() -> frame.setVisible(true));

        try {
                Thread.currentThread().join();
                
        } catch (InterruptedException e) {
                System.out.println("Main thread interrupted, exiting.");
}
    }
}