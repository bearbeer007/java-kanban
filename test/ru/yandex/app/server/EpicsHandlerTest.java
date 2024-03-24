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

public class EpicsHandlerTest {
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
    public void shouldReturnEpicEndpoint() throws IOException, InterruptedException {
        Epic epic = new Epic("epic", "epicDescription");
        taskManager.createEpic(epic);
        int epicId = epic.getId();
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Epic> epicsFromManager = gson.fromJson(response.body(), new TypeToken<List<Epic>>() {
        }.getType());

        Epic epicFromManager = epicsFromManager.get(0);
        assertEquals(epicId, epicFromManager.getId(), "incorrect epic id");
        assertEquals(epic.getName(), epicFromManager.getName(), "incorrect epic name");
        assertEquals(epic.getDescription(), epicFromManager.getDescription(), "incorrect epic description");
    }

    @Test
    public void shouldReturnEpicsIdEndpoint() throws IOException, InterruptedException {
        Epic epic = new Epic("epic", "epicDescription");
        taskManager.createEpic(epic);
        int epicId = epic.getId();
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Epic epicFromManager = gson.fromJson(response.body(), new TypeToken<Epic>() {
        }.getType());

        assertEquals(epicId, epicFromManager.getId(), "incorrect epic id");
        assertEquals(epic.getName(), epicFromManager.getName(), "incorrect epic name");
        assertEquals(epic.getDescription(), epicFromManager.getDescription(), "incorrect epic description");
    }

    @Test
    public void shouldReturnEpicSubtasksIdEndpoint() throws IOException, InterruptedException {
        Epic epic = new Epic("epic", "epicDescription");
        Subtask subtask = new Subtask("subtask", "subtaskDescription", epic.getId(),
                LocalDateTime.of(2024, 3, 3, 3, 3), Duration.ofMinutes(10));
        Subtask subtask2 = new Subtask("subtask2", "subtaskDescription2", epic.getId(),
                LocalDateTime.of(2024, 4, 4, 4, 4), Duration.ofMinutes(10));
        taskManager.createEpic(epic);
        taskManager.createSubtask(subtask);
        taskManager.createSubtask(subtask2);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId() + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Integer> subtasksListFromManager = gson.fromJson(response.body(), new TypeToken<List<Integer>>() {
        }.getType());


        assertEquals(epic.getSubtaskList().toString(), subtasksListFromManager.toString(), "incorrect return subtasks list of epic");
    }

    @Test
    public void shouldReturn404ErrorWhenEpicsNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "incorrect statusCode");
    }

    @Test
    public void shouldCreateSubtaskWhenPostRequest() throws IOException, InterruptedException {
        Epic epic = new Epic("epic", "epicDescription");
        String epicJson = gson.toJson(epic);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        assertEquals(0, taskManager.getAllEpics().size(), "incorrect epics size before create");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = taskManager.getAllEpics();
        Epic epicFromManager = epicsFromManager.get(0);

        assertNotNull(epicFromManager, "epics not return");
        assertEquals(1, epicsFromManager.size(), "incorrect num of epics");
        assertEquals("epic", epicFromManager.getName());
    }

    @Test
    public void shouldDeleteEpicIdWhenDeleteRequest() throws IOException, InterruptedException {
        Epic epic = new Epic("epic", "epicDescription");
        taskManager.createEpic(epic);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        assertEquals(1, taskManager.getAllEpics().size(), "incorrect epics size before delete");

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(0, taskManager.getAllEpics().size(), "incorrect epics size after delete");
    }

    @Test
    public void shouldDeleteEpicsWhenDeleteRequest() throws IOException, InterruptedException {
        Epic epic = new Epic("epic", "epicDescription");
        Epic epic2 = new Epic("epic2", "epicDescription2");
        taskManager.createEpic(epic);
        taskManager.createEpic(epic2);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        assertEquals(2, taskManager.getAllEpics().size(), "incorrect epics size before delete");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(0, taskManager.getAllEpics().size(), "incorrect epics size after delete");
    }

}