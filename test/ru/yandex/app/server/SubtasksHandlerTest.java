import adapters.DurationAdapter;
import adapters.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import managers.FileBackedTaskManager;
import managers.TaskManager;
import models.Epic;
import models.Subtask;
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

public class SubtasksHandlerTest {
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
    public void shouldReturnSubtaskEndpoint() throws IOException, InterruptedException {
        Epic epic = new Epic("epic", "epicDescription");
        Subtask subtask = new Subtask("subtask", "subtaskDescription", epic.getId(), LocalDateTime.of(2024, 3, 5, 20, 0), Duration.ofMinutes(10));
        taskManager.createEpic(epic);
        taskManager.createSubtask(subtask);
        int subtaskId = subtask.getId();
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Subtask> subtasksFromManager = gson.fromJson(response.body(), new TypeToken<List<Subtask>>() {
        }.getType());

        Subtask subtaskFromManager = subtasksFromManager.get(0);
        assertEquals(subtaskId, subtaskFromManager.getId(), "incorrect subtask id");
        assertEquals(subtask.getName(), subtaskFromManager.getName(), "incorrect subtask name");
        assertEquals(subtask.getDescription(), subtaskFromManager.getDescription(), "incorrect subtask description");
        assertEquals(subtask.getDuration().toString(), subtaskFromManager.getDuration().toString(), "incorrect subtask description");
        assertEquals(subtask.getStartTime().toString(), subtaskFromManager.getStartTime().toString(), "incorrect subtask description");
    }

    @Test
    public void shouldReturnSubtasksIdEndpoint() throws IOException, InterruptedException {
        Epic epic = new Epic("epic", "epicDescription");
        Subtask subtask = new Subtask("subtask", "subtaskDescription", epic.getId(), LocalDateTime.of(2024, 3, 5, 20, 0), Duration.ofMinutes(10));
        taskManager.createEpic(epic);
        taskManager.createSubtask(subtask);
        int subtaskId = subtask.getId();
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Subtask subtaskFromManager = gson.fromJson(response.body(), new TypeToken<Subtask>() {
        }.getType());

        assertEquals(subtaskId, subtaskFromManager.getId(), "incorrect subtask id");
        assertEquals(subtask.getName(), subtaskFromManager.getName(), "incorrect subtask name");
        assertEquals(subtask.getDescription(), subtaskFromManager.getDescription(), "incorrect subtask description");
        assertEquals(subtask.getDuration().toString(), subtaskFromManager.getDuration().toString(), "incorrect subtask duration");
        assertEquals(subtask.getStartTime().toString(), subtaskFromManager.getStartTime().toString(), "incorrect subtask start time");
    }

    @Test
    public void shouldReturn404ErrorWhenSubtaskNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "incorrect statusCode");

    }

    @Test
    public void shouldCreateSubtaskWhenPostRequest() throws IOException, InterruptedException {
        Epic epic = new Epic("epic", "epicDescription");
        Subtask subtask = new Subtask("subtask", "subtaskDescription", epic.getId(), LocalDateTime.of(2024, 3, 5, 20, 0), Duration.ofMinutes(10));
        taskManager.createEpic(epic);

        String subtaskJson = gson.toJson(subtask);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        assertEquals(0, taskManager.getAllSubtasks().size(), "incorrect subtasks size before create");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Subtask> subtasksFromManager = taskManager.getAllSubtasks();
        Subtask subtaskFromManager = subtasksFromManager.get(0);

        assertNotNull(subtasksFromManager, "subtasks not return");
        assertEquals(1, subtasksFromManager.size(), "incorrect num of subtasks");
        assertEquals("subtask", subtaskFromManager.getName());
    }

    @Test
    public void shouldUpdateSubtaskWhenPostRequest() throws IOException, InterruptedException {
        Epic epic = new Epic("epic", "epicDescription");
        Subtask subtask = new Subtask("subtask", "subtaskDescription", epic.getId(),
                LocalDateTime.of(2024, 3, 3, 3, 3), Duration.ofMinutes(10));
        taskManager.createEpic(epic);
        subtask.setId(5);
        taskManager.createSubtask(subtask);
        int subtaskId = subtask.getId();
        Task updateForSubtask = new Subtask("newName", "newDescription", epic.getId(),
                LocalDateTime.of(2024, 4, 4, 4, 4), Duration.ofMinutes(10));
        updateForSubtask.setId(5);
        String subtaskJson = gson.toJson(updateForSubtask);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Subtask subtaskFromManager = taskManager.getSubtaskById(5);

        assertEquals("newName", subtaskFromManager.getName(), "incorrect update subtask name");
        assertEquals("newDescription", subtaskFromManager.getDescription(), "incorrect update subtask description");
    }

    @Test
    public void shouldDeleteSubtaskIdWhenDeleteRequest() throws IOException, InterruptedException {
        Epic epic = new Epic("epic", "epicDescription");
        Subtask subtask = new Subtask("subtask", "subtaskDescription", epic.getId(),
                LocalDateTime.of(2024, 3, 3, 3, 3), Duration.ofMinutes(10));
        taskManager.createEpic(epic);
        taskManager.createSubtask(subtask);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        assertEquals(1, taskManager.getAllSubtasks().size(), "incorrect subtasks size before delete");

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(0, taskManager.getAllSubtasks().size(), "incorrect subtasks size after delete");
    }

    @Test
    public void shouldDeleteSubtasksWhenDeleteRequest() throws IOException, InterruptedException {
        Epic epic = new Epic("epic", "epicDescription");
        Subtask subtask = new Subtask("subtask", "subtaskDescription", epic.getId(),
                LocalDateTime.of(2024, 3, 3, 3, 3), Duration.ofMinutes(10));
        Subtask subtask2 = new Subtask("subtask2", "subtaskDescription2", epic.getId(),
                LocalDateTime.of(2024, 4, 4, 4, 4), Duration.ofMinutes(10));
        taskManager.createEpic(epic);
        taskManager.createSubtask(subtask);
        taskManager.createSubtask(subtask2);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        assertEquals(2, taskManager.getAllSubtasks().size(), "incorrect subtasks size before delete");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(0, taskManager.getAllSubtasks().size(), "incorrect subtasks size after delete");
    }
}