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

public class TasksHandlerTest {
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
    public void shouldReturnTasksEndpoint() throws IOException, InterruptedException {
        Task task = new Task("task", "taskDescription", Duration.ofMinutes(5),
                LocalDateTime.of(2024, 3, 5, 0, 0));
        taskManager.createTask(task);
        int taskId = task.getId();
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());

        Task taskFromManager = tasksFromManager.get(0);
        assertEquals(taskId, taskFromManager.getId(), "incorrect task id");
        assertEquals(task.getName(), taskFromManager.getName(), "incorrect task name");
        assertEquals(task.getDescription(), taskFromManager.getDescription(), "incorrect task description");
        assertEquals(task.getDuration().toString(), taskFromManager.getDuration().toString(), "incorrect task description");
        assertEquals(task.getStartTime().toString(), taskFromManager.getStartTime().toString(), "incorrect task description");
    }

    @Test
    public void shouldReturnTasksIdEndpoint() throws IOException, InterruptedException {
        Task task = new Task("task", "taskDescription", Duration.ofMinutes(5),
                LocalDateTime.of(2024, 3, 5, 0, 0));
        task.setId(7);
        taskManager.createTask(task);
        int taskId = task.getId();
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task taskFromManager = gson.fromJson(response.body(), new TypeToken<Task>() {
        }.getType());

        assertEquals(taskId, taskFromManager.getId(), "incorrect task id");
        assertEquals(task.getName(), taskFromManager.getName(), "incorrect task name");
        assertEquals(task.getDescription(), taskFromManager.getDescription(), "incorrect task description");
        assertEquals(task.getDuration().toString(), taskFromManager.getDuration().toString(), "incorrect task duration");
        assertEquals(task.getStartTime().toString(), taskFromManager.getStartTime().toString(), "incorrect task start time");
    }

    @Test
    public void shouldReturn404ErrorWhenTaskNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "incorrect statusCode");

    }

    @Test
    public void shouldCreateTaskWhenPostRequest() throws IOException, InterruptedException {
        Task task = new Task("task", "taskDescription", Duration.ofMinutes(5),
                LocalDateTime.of(2024, 3, 5, 0, 0));
        String taskJson = gson.toJson(task);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = taskManager.getAllTasks();
        Task taskFromManager = tasksFromManager.get(0);

        assertNotNull(tasksFromManager, "tasks not return");
        assertEquals(1, tasksFromManager.size(), "incorrect num of tasks");
        assertEquals("task", taskFromManager.getName());
    }

    @Test
    public void shouldUpdateTaskWhenPostRequest() throws IOException, InterruptedException {
        Task task = new Task("task", "taskDescription", Duration.ofMinutes(5),
                LocalDateTime.of(2024, 3, 5, 0, 0));
        task.setId(5);
        taskManager.createTask(task);
        int taskId = task.getId();
        Task updateForTask = new Task("newName", "newDescription", Duration.ofMinutes(5),
                LocalDateTime.now());
        updateForTask.setId(5);
        String taskJson = gson.toJson(updateForTask);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Task taskFromManager = taskManager.getTaskById(5);

        assertEquals("newName", taskFromManager.getName(), "incorrect update task name");
        assertEquals("newDescription", taskFromManager.getDescription(), "incorrect update task description");
    }

    @Test
    public void shouldDeleteTaskIdWhenDeleteRequest() throws IOException, InterruptedException {
        Task task = new Task("task", "taskDescription", Duration.ofMinutes(5),
                LocalDateTime.of(2024, 3, 5, 0, 0));
        taskManager.createTask(task);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        assertEquals(1, taskManager.getAllTasks().size(), "incorrect tasks size before delete");

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(0, taskManager.getAllTasks().size(), "incorrect tasks size after delete");
    }

    @Test
    public void shouldDeleteTasksWhenDeleteRequest() throws IOException, InterruptedException {
        Task task = new Task("task", "taskDescription", Duration.ofMinutes(5),
                LocalDateTime.of(2024, 3, 3, 3, 3));
        Task task2 = new Task("task2", "taskDescription2", Duration.ofMinutes(11),
                LocalDateTime.of(2024, 4, 4, 4, 4));
        taskManager.createTask(task);
        taskManager.createTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        assertEquals(2, taskManager.getAllTasks().size(), "incorrect tasks size before delete");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(0, taskManager.getAllTasks().size(), "incorrect tasks size after delete");


    }

}