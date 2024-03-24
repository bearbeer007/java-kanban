import adapters.DurationAdapter;
import adapters.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import managers.FileBackedTaskManager;
import managers.TaskManager;
import models.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PrioritizedTasksHandlerTest {
    File tmpFile;
    TaskManager taskManager;
    HttpTaskServer taskServer;
    Gson gson = new GsonBuilder().registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateAdapter())
            .create();

    {
        try {
            tmpFile = File.createTempFile("data", ".csv");
            taskManager = new FileBackedTaskManager(tmpFile);
            taskServer = new HttpTaskServer(taskManager);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    public void setUp() {
        taskManager.deleteAllTasks();
        taskManager.deleteAllSubtasks();
        taskManager.deleteAllEpics();
        Task.setCount(0);
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void shouldReturnPrioritizedTasks() throws IOException, InterruptedException {
        Task task1 = new Task("task1", "taskDescription1", Duration.ofMinutes(5),
                LocalDateTime.of(2024, 3, 5, 0, 0));
        Task task2 = new Task("task1", "taskDescription1",
                Duration.ofMinutes(10), LocalDateTime.of(2024, 1, 5, 0, 0));
        Task task3 = new Task("task1", "taskDescription1",
                Duration.ofMinutes(15), LocalDateTime.of(2024, 2, 5, 0, 0));
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createTask(task3);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Set<Task> tasksFromManager = gson.fromJson(response.body(), new TypeToken<Set<Task>>() {
        }.getType());

        assertNotNull(tasksFromManager, "prioritized task is empty");
        assertEquals(3, tasksFromManager.size(), "incorrect size of prioritized tasks");
    }
}