package taskmanager.api;


import java.time.LocalDateTime;

public class MainApp {

    public static void main(String[] args) {
        // Build the TaskManager (students will implement DefaultTaskManager)
        TaskManager tm = TaskManager.builder()
                .withWeatherApiKey("YOUR_API_KEY_HERE")
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
    }
}