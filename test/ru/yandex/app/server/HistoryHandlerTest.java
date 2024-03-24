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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryHandlerTest {
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

    public void shouldReturnHistory() throws IOException, InterruptedException {
        Task task = new Task("task", "taskDescription", Duration.ofMinutes(5),
                LocalDateTime.of(2024, 3, 5, 0, 0));
        taskManager.createTask(task);
        taskManager.getTaskById(1);
        List<Task> testTasks = List.of(task);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> historyFromManager = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());

        assertEquals(testTasks, historyFromManager);
    }
}