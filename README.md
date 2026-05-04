# Software_Construction_Project
Smart Task Manager 
This professional API-based application manages tasks by integrating real-time weather data. Below is the technical documentation on how to set up and interact with the system.

1. How to Run the App
Step 1: Open the project folder in your IDE (like VS Code or IntelliJ).  

Step 2: Make sure you have Java 17 and Maven installed to download the libraries (Project Reactor and JSON).

Step 3: Go to the file named MainApp.java (located in the taskmanager package).  

Step 4: Run the main method. The Smart Task Manager window will appear on your screen.
2. Where to Put the API Key
The application requires a key from OpenWeatherMap for live updates.

Open the MainApp.java file.

Locate the withWeatherApiKey method.

Replace the placeholder with your actual key:

Java
TaskManager tm = TaskManager.builder()
        .withWeatherApiKey("your API key") // Replace with your key
        .build();

3. Code Example: Using TaskManager
// 1. Setup the manager
TaskManager tm = TaskManager.builder()
        .withWeatherApiKey("your_key")
        .build();

// 2. Create a task that depends on the weather (like a Football Match)
Task myTask = new Task("001", "Football Match", LocalDateTime.now().plusDays(1), true);

// 3. Add it to the system
tm.addTask(myTask);

// 4. Get smart recommendations based on the weather in your city
tm.getSmartRecommendations("Makkah")
    .subscribe(result -> {
        System.out.println("Weather Advice: " + result.get(0).recommendation());
    });

    
