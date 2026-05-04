package taskmanager.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import taskmanager.api.SchedulePlanner;
import taskmanager.api.TaskManager;
import taskmanager.api.TaskService;
import taskmanager.exception.InvalidTaskException;
import taskmanager.exception.TaskNotFoundException;
import taskmanager.impl.DefaultTaskManager;
import taskmanager.model.ScheduleRecommendation;
import taskmanager.model.Task;
import taskmanager.model.WeatherForecast;

/**
 * A Swing-based UI for the TaskManager application. 
 * It displays tasks in a table and allows users to add, edit, delete tasks, 
 * fetch weather info, and get recommendations.
 * @param  Uses Reactor's Schedulers to run blocking operations off the EDT, 
 * and SwingUtilities.invokeLater to update the UI on the EDT.
 * @throws Shows dialogs for user input errors and service exceptions.   
 */

public class SmartTaskManagerFrame extends JFrame {

    /** Formatter used for parsing and displaying date-time fields. */
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final TaskManager taskManager;
    private final TaskService taskService;  // to be initialized from taskManager.impl
    private final SchedulePlanner schedulePlanner;

    private final JTable taskTable;
    private final DefaultTableModel tableModel;
    private final JButton updateWeatherButton;
    private final JLabel statusLabel;

    // Added buttons for Add, Edit, Delete, and Recommendations
    private final JButton addButton;
    private final JButton editButton;
    private final JButton deleteButton;
    private final JButton recommendButton;

    private final String[] columnNames = {"ID", "Title", "Due Time", "Weather Sensitive", "Status"};
    /**
     * Initializes the frame with the given TaskManager, sets up the UI components, and wires event handlers.
     * @param taskManager The TaskManager instance to use for data operations. Must be an
     * instance of DefaultTaskManager to access the TaskService for add/edit/delete operations.
     */
    public SmartTaskManagerFrame(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.taskService = ((DefaultTaskManager) taskManager).getTaskService();
        this.schedulePlanner = taskManager.getPlanner();
        

        setTitle("Smart Task Manager (Swing)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 400);

        tableModel = new DefaultTableModel(columnNames, 0);
        taskTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(taskTable);

        updateWeatherButton = new JButton("Update Weather for Selected Task");
        updateWeatherButton.setEnabled(false);

        // Initialize new buttons
        addButton       = new JButton(" Add");
        editButton      = new JButton(" Edit");
        deleteButton    = new JButton(" Delete");
        recommendButton = new JButton(" Recommendations");

        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        recommendButton.setEnabled(false);

        statusLabel = new JLabel("Ready");

        // Button panel holds all buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(updateWeatherButton);
        buttonPanel.add(recommendButton);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.NORTH);

        // Initialization: load tasks
        loadTasks();

        // Wiring: select row → enable weather button
        taskTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = taskTable.getSelectedRow();
            updateWeatherButton.setEnabled(selectedRow >= 0);
            editButton.setEnabled(selectedRow >= 0);
            deleteButton.setEnabled(selectedRow >= 0);
            recommendButton.setEnabled(selectedRow >= 0);
        });

        // “Update Weather” clicked
        updateWeatherButton.addActionListener(e -> {
            int selectedRow = taskTable.getSelectedRow();
            if (selectedRow < 0) return;

            String taskId = (String) tableModel.getValueAt(selectedRow, 0);
            updateWeatherForTask(taskId);
        });
        // "Add" clicked
        addButton.addActionListener(e -> showAddTaskDialog());
 
        // "Edit" clicked
        editButton.addActionListener(e -> {
            int selectedRow = taskTable.getSelectedRow();
            if (selectedRow < 0) return;
            String taskId = (String) tableModel.getValueAt(selectedRow, 0);
            showEditTaskDialog(taskId);
        });
 
        // "Delete" clicked
        deleteButton.addActionListener(e -> {
            int selectedRow = taskTable.getSelectedRow();
            if (selectedRow < 0) return;
            String taskId = (String) tableModel.getValueAt(selectedRow, 0);
            deleteTask(taskId);
        });
 
        // "Recommendations" clicked — uses getSmartRecommendations() from DefaultTaskManager
        recommendButton.addActionListener(e -> showRecommendations());
    }

    // ... Keep your existing loadTasks, populateTable, and updateWeatherForTask methods ...
    private void loadTasks() {
        Mono.just(taskManager.getTasks())
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(tasks -> SwingUtilities.invokeLater(() -> populateTable(tasks)))
                .subscribe();
    }


        private void populateTable(List<Task> tasks) {
        tableModel.setRowCount(0);
        for (Task t : tasks) {
            tableModel.addRow(new Object[]{t.getId(), t.getTitle(), t.getDueDateTime(), t.isWeatherSensitive(), "N/A"});
        }
    }
    /**
     * Fetches weather for the task's location (fixed as "Jeddah" here) and updates the "Status" column based on precipitation probability.
     * @param taskId The ID of the task to update weather for.  
     */
    private void updateWeatherForTask(String taskId) {
        Mono<WeatherForecast> forecastMono = taskManager.fetchWeather("Jeddah");  // fixed city

        forecastMono
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(forecast -> SwingUtilities.invokeLater(() -> {
                    // Simple weather‑aware status logic
                    String status = forecast.getPrecipitationProbability() > 0.6
                            ? "RISKY (rain)"
                            : "SAFE";

                    updateTaskStatusInTable(taskId, status);
                    statusLabel.setText("Weather updated for task: " + taskId);
                }))
                .doOnError(error -> SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Weather fetch failed: " + error.getMessage());
                }))
                .subscribe();
    }
    /**
     * Updates the "Status" column for the task with the given ID in the table.
     * @param taskId The ID of the task to update.
     * @param status The new status string to display.
     */
    private void updateTaskStatusInTable(String taskId, String status) {
        int rowCount = tableModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            String idInTable = (String) tableModel.getValueAt(i, 0);
            if (idInTable.equals(taskId)) {
                tableModel.setValueAt(status, i, 4);
                break;
            }
        }
    }

    /**
     * Opens a dialog to collect new task data, then adds it via TaskService.
     *
     * @param title The title of the new task.
     * @param due The due date and time of the new task.
     * @param isWeatherSensitive Whether the new task is weather sensitive.
     */
    private void showAddTaskDialog() {
        JTextField titleField   = new JTextField(20);
        JTextField dueDateField = new JTextField(
                LocalDateTime.now().plusDays(1).format(DATE_FMT), 20);
        JTextField descField    = new JTextField(20);
        JCheckBox  sensitiveBox = new JCheckBox("Weather Sensitive?");
 
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.GridLayout(0, 2, 6, 6));
        panel.add(new JLabel("Title *:"));
        panel.add(titleField);
        panel.add(new JLabel("Due (yyyy-MM-dd HH:mm) *:"));
        panel.add(dueDateField);
        panel.add(new JLabel(""));
        panel.add(sensitiveBox);
 
        int result = JOptionPane.showConfirmDialog(this, panel,
                "Add New Task", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;
 
        String title = titleField.getText().trim();
        try {
            if (title.isEmpty()) {
                showWarning("Task title cannot be empty.");
                return;
            }
        } catch (Exception e ) {
            showWarning("Task title cannot be empty.");
            return;
        }
 
        LocalDateTime due;
        try {
            due = LocalDateTime.parse(dueDateField.getText().trim(), DATE_FMT);
        } catch (DateTimeParseException ex) {
            showWarning("Invalid date format. Use: yyyy-MM-dd HH:mm");
            return;
        }
 
        String id    = "task-" + System.currentTimeMillis();
        Task newTask = new Task(id, title, due, sensitiveBox.isSelected());
        newTask.setDescription(descField.getText().trim());
 
        // Uses DefaultTaskService.addTask() which throws InvalidTaskException on bad input
        taskService.addTask(newTask)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(v -> SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Task '" + title + "' added.");
                    loadTasks();
                }))
                .doOnError(err -> SwingUtilities.invokeLater(() -> {
                    try {
                        throw err;
                    } catch (InvalidTaskException ex) {
                        showError("Invalid task: " + ex.getMessage());
                    } catch (Throwable ex) {
                        showError("Failed to add task: " + ex.getMessage());
                    }
                }))
                .subscribe();
    }
 
    /**
     * Opens a dialog pre-filled with the selected task's data for editing.
     *
     * @param  taskId must match an existing task.
     * @param  On success, task fields are updated and table refreshed.
     * @throws  Shows error dialog on TaskNotFoundException.
     *
     * @param taskId The ID of the task to edit.
     */
    private void showEditTaskDialog(String taskId) {
        // Uses DefaultTaskService.findTaskById() which throws TaskNotFoundException if missing
        taskService.findTaskById(taskId)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(task -> SwingUtilities.invokeLater(() -> {
 
                    JTextField titleField   = new JTextField(task.getTitle(), 20);
                    JTextField dueDateField = new JTextField(
                            task.getDueDateTime() != null
                                    ? task.getDueDateTime().format(DATE_FMT) : "", 20);
                    JTextField descField    = new JTextField(
                            task.getDescription() != null ? task.getDescription() : "", 20);
                    JCheckBox  sensitiveBox = new JCheckBox("Weather Sensitive?",
                            task.isWeatherSensitive());
 
                    JPanel panel = new JPanel();
                    panel.setLayout(new java.awt.GridLayout(0, 2, 6, 6));
                    panel.add(new JLabel("Title *:"));
                    panel.add(titleField);
                    panel.add(new JLabel("Due (yyyy-MM-dd HH:mm) *:"));
                    panel.add(dueDateField);
                    panel.add(new JLabel(""));
                    panel.add(sensitiveBox);
 
                    int result = JOptionPane.showConfirmDialog(this, panel,
                            "Edit Task", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (result != JOptionPane.OK_OPTION) return;
 
                    String newTitle = titleField.getText().trim();
                    if (newTitle.isEmpty()) { showWarning("Title cannot be empty."); return; }
 
                    LocalDateTime newDue;
                    try {
                        newDue = LocalDateTime.parse(dueDateField.getText().trim(), DATE_FMT);
                    } catch (DateTimeParseException ex) {
                        showWarning("Invalid date format. Use: yyyy-MM-dd HH:mm");
                        return;
                    }
 
                    // Apply edits directly on the task object (Task has setters)
                    task.setTitle(newTitle);
                    task.setDueDateTime(newDue);
                    task.setDescription(descField.getText().trim());
                    task.setWeatherSensitive(sensitiveBox.isSelected());
 
                    statusLabel.setText("Task '" + newTitle + "' updated.");
                    loadTasks();
                }))
                .doOnError(err -> SwingUtilities.invokeLater(() -> {
                    try {
                        throw err;
                    } catch (TaskNotFoundException ex) {
                        showError("Task not found: " + taskId);
                    } catch (Throwable ex) {
                        showError("Failed to load task: " + ex.getMessage());
                    }
                }))
                .subscribe();
    }
 
    /**
     * Asks for confirmation then deletes the task with the given ID.
     *
     * @param  taskId must match an existing task.
     * @param  Task removed and table refreshed on success.
     * @exception  Shows error dialog on TaskNotFoundException. 
     *
     * @param taskId The ID of the task to delete.
     */
    private void deleteTask(String taskId) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete task: " + taskId + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
 
        // Uses DefaultTaskService.removeTask() which throws TaskNotFoundException if missing
        taskService.removeTask(taskId)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(v -> SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Task " + taskId + " deleted.");
                    loadTasks();
                }))
                .doOnError(err -> SwingUtilities.invokeLater(() -> {
                    try {
                        throw err;
                    } catch (TaskNotFoundException ex) {
                        showError("Task not found: " + taskId);
                    } catch (Throwable ex) {
                        showError("Delete failed: " + ex.getMessage());
                    }
                }))
                .subscribe();
    }
 
    /**
     * Uses DefaultTaskManager.getSmartRecommendations() — the ready-made reactive
     * pipeline that fetches weather then calls DefaultSchedulePlanner.suggestSchedule().
     * @param  A task must be selected in the table.
     * @throws  Shows error dialog on failure.
     */
    private void showRecommendations() {
        statusLabel.setText("Generating recommendations...");
 
        // Reuses the existing pipeline in DefaultTaskManager instead of rebuilding it
        ((DefaultTaskManager) taskManager).getSmartRecommendations("Jeddah")
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(recommendations -> SwingUtilities.invokeLater(() -> {
                    if (recommendations.isEmpty()) {
                        JOptionPane.showMessageDialog(this,
                                "No tasks to recommend for.",
                                "Recommendations", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
 
                    StringBuilder sb = new StringBuilder();
                    for (ScheduleRecommendation rec : recommendations) {
                        sb.append("Task: ").append(rec.task().getTitle()).append("\n");
                        sb.append("   ").append(rec.recommendation()).append("\n\n");
                    }
 
                    JTextArea textArea = new JTextArea(sb.toString());
                    textArea.setEditable(false);
                    textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
                    JScrollPane scroll = new JScrollPane(textArea);
                    scroll.setPreferredSize(new Dimension(450, 280));
 
                    JOptionPane.showMessageDialog(this, scroll,
                            "Weather-Aware Recommendations", JOptionPane.INFORMATION_MESSAGE);
 
                    statusLabel.setText("Recommendations ready.");
                }))
                .doOnError(error -> SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Recommendations failed: " + error.getMessage());
                    showError("Could not generate recommendations: " + error.getMessage());
                }))
                .subscribe();
    }
 
    /**
     * Shows an error dialog and updates the status label.
     *
     * @param message The error message to display.
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        statusLabel.setText("Error: " + message);
    }
 
    /**
     * Shows a warning dialog for invalid user input.
     *
     * @param message The warning message to display.
     */
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }
}
