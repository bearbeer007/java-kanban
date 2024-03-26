import com.google.gson.reflect.TypeToken;
import http.HttpTaskServerTest;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SubtasksHandlerTest extends HttpTaskServerTest {
    String apiUrl = "http://localhost:8080/api/v1/subtasks";

    SubtasksHandlerTest() throws IOException {
    }

    @Test
    void postSubtask() {
        Epic epic = new Epic("Test addNewEpicForSubtask", "Test addNewEpicForSubtask description");
        final long epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Test addNewSubtask", "Test addNewSubtask description");
        subtask.setStartTime(LocalDateTime.of(2022, 03, 01, 10, 00));
        subtask.setDuration(Duration.ofMinutes(60));
        subtask.setEpicId(epicId);

        String taskJson = gson.toJson(subtask);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(apiUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, response.statusCode());
            List<Subtask> subtasksFromManager = taskManager.getSubtasks();
            assertNotNull(subtasksFromManager, "Подзадачи не возвращаются");
            assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач");
            assertEquals("Test addNewSubtask", subtasksFromManager.get(0).getName(), "Некорректное имя подзадачи");
        } catch (IOException | InterruptedException e) {
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void updateSubtask() {
        Epic epic = new Epic("Test addNewEpicForSubtask", "Test addNewEpicForSubtask description");
        final long epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Тест", "Тестовое описание", LocalDateTime.now(), Duration.ofMinutes(5));
        final Long subtaskId = taskManager.addSubtask(subtask, epicId);

        Subtask subtaskForUpdate = taskManager.getSubtask(subtaskId);
        String newName = "New Name";
        String newDescription = "New Description";
        Status newStatus = Status.DONE;
        LocalDateTime newStartTime = LocalDateTime.now().plusHours(1);
        Duration newDuration = Duration.ofMinutes(60);
        subtaskForUpdate.setName(newName);
        subtaskForUpdate.setDescription(newDescription);
        subtaskForUpdate.setStatus(newStatus);
        subtaskForUpdate.setStartTime(newStartTime);
        subtaskForUpdate.setDuration(newDuration);
        String taskJson = gson.toJson(subtaskForUpdate);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(apiUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, response.statusCode(), response.body());
            final Subtask updatedSubtask = taskManager.getSubtask(subtaskId);
            assertEquals(newName, updatedSubtask.getName(), "Имя задачи не совпадает");
            assertEquals(newDescription, updatedSubtask.getDescription(), "Описание задачи не совпадает");
            assertEquals(newStatus, updatedSubtask.getStatus(), "Статус задачи не совпадает");
            assertEquals(newStartTime, updatedSubtask.getStartTime(), "Дата начала задачи не совпадает");
            assertEquals(newDuration, updatedSubtask.getDuration(), "Продолжительность задачи не совпадает");
        } catch (IOException | InterruptedException e) {
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void getSubtaskById() {
        Epic epic = new Epic("Test addNewEpicForSubtask", "Test addNewEpicForSubtask description");
        final long epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Test addNewSubtask", "Test addNewSubtask description");
        subtask.setStartTime(LocalDateTime.of(2022, 03, 01, 10, 00));
        subtask.setDuration(Duration.ofMinutes(60));
        final long subtaskId = taskManager.addSubtask(subtask, epicId);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(apiUrl + "/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            final Subtask subtaskFromHttp = gson.fromJson(response.body(), new TypeToken<Subtask>() {
            }.getType());
            assertEquals(subtaskId, subtaskFromHttp.getId(), "Некорректный id подзадачи");
            assertEquals("Test addNewSubtask", subtaskFromHttp.getName(), "Некорректное имя подзадачи");
        } catch (IOException | InterruptedException e) {
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void getSubtaskByWrongId() {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(apiUrl + "/5324542345234562");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(404, response.statusCode());
            assertEquals("Некорректный идентификатор подзадачи", response.body(), "Не верная ошибка при неправильном ID подзадачи");
        } catch (IOException | InterruptedException e) {
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void getSubtaskWrongEndPoint() {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(apiUrl + "/123456789/test");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(404, response.statusCode());
            assertEquals("Такого эндпоинта не существует", response.body(), "Не верная ошибка при неправильном ендпоинте");
        } catch (IOException | InterruptedException e) {
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void getSubtasks() {
        Epic epic = new Epic("Test addNewEpicForSubtask", "Test addNewEpicForSubtask description");
        final long epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("Test addNewSubtask1", "Test addNewSubtask description1");
        subtask1.setStartTime(LocalDateTime.of(2022, 03, 01, 10, 00));
        subtask1.setDuration(Duration.ofMinutes(60));
        taskManager.addSubtask(subtask1, epicId);
        Subtask subtask2 = new Subtask("Test addNewSubtask2", "Test addNewSubtask description2");
        subtask2.setStartTime(LocalDateTime.of(2022, 04, 01, 10, 00));
        subtask2.setDuration(Duration.ofMinutes(60));
        taskManager.addSubtask(subtask2, epicId);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(apiUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            final List<Subtask> subtasksFromHttp = gson.fromJson(response.body(), new TypeToken<List<Subtask>>() {
            }.getType());
            assertEquals(2, subtasksFromHttp.size(), "Некорректное количество подзадач");
            assertEquals(true, subtasksFromHttp.contains(subtask1), "Подзадача отсутствует в возвращаемом списке задач");
            assertEquals(true, subtasksFromHttp.contains(subtask2), "Подзадача отсутствует в возвращаемом списке задач");
        } catch (IOException | InterruptedException e) {
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void addIntersectionsSubtasks() {
        Epic epic = new Epic("Test addNewEpicForSubtask", "Test addNewEpicForSubtask description");
        final long epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("Тест1", "Тестовое описание1", LocalDateTime.now(), Duration.ofMinutes(50));
        subtask1.setEpicId(epicId);
        taskManager.addTask(subtask1);
        Subtask subtask2 = new Subtask("Тест2", "Тестовое описание2", LocalDateTime.now(), Duration.ofMinutes(50));
        subtask2.setEpicId(epicId);
        String taskJson = gson.toJson(subtask2);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(apiUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(406, response.statusCode());
            assertEquals("Задачи пересекаются!", response.body(), "Не верная ошибка при пересекающихся задачах");
        } catch (IOException | InterruptedException e) {
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void deleteSubtask() {
        Epic epic = new Epic("Test addNewEpicForSubtask", "Test addNewEpicForSubtask description");
        final long epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Test addNewSubtask", "Test addNewSubtask description");
        subtask.setStartTime(LocalDateTime.of(2022, 03, 01, 10, 00));
        subtask.setDuration(Duration.ofMinutes(60));
        final long subtaskId = taskManager.addSubtask(subtask, epicId);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(apiUrl + "/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Accept", "application/json")
                .DELETE()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            List<Subtask> subtasksFromManager = taskManager.getSubtasks();
            assertNotNull(subtasksFromManager, "Задачи не возвращаются");
            assertEquals(0, subtasksFromManager.size(), "Некорректное количество задач");
        } catch (IOException | InterruptedException e) {
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }
}