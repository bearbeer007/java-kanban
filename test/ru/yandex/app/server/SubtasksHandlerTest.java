package ru.yandex.app.server;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import ru.yandex.app.model.Epic;
import ru.yandex.app.model.Subtask;
import ru.yandex.app.model.TaskStatus;
import ru.yandex.app.server.HttpTaskServerTest;

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
    void postSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test addNewEpicForSubtask", "Test addNewEpicForSubtask description");
        final int epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Test addNewSubtask", "Test addNewSubtask description",epicId,LocalDateTime.of(2022, 03, 01, 10, 00),Duration.ofMinutes(60));
        String taskJson = gson.toJson(subtask);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(apiUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, response.statusCode());
            List<Subtask> subtasksFromManager = taskManager.getAllSubTasks();
            assertNotNull(subtasksFromManager, "Подзадачи не возвращаются");
            assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач");
            assertEquals("Test addNewSubtask", subtasksFromManager.get(0).getName(), "Некорректное имя подзадачи");
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");

    }

    @Test
    void updateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test addNewEpicForSubtask", "Test addNewEpicForSubtask description");
        final int epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Тест", "Тестовое описание",epicId, LocalDateTime.now(), Duration.ofMinutes(5));
        final int subtaskId = taskManager.addSubTask(subtask);

        Subtask subtaskForUpdate = taskManager.getSubTaskByTaskId(subtaskId);
        String newName = "New Name";
        String newDescription = "New Description";
        LocalDateTime newStartTime = LocalDateTime.now().plusHours(1);
        Duration newDuration = Duration.ofMinutes(60);
        subtaskForUpdate.setDescription(newDescription);
        subtaskForUpdate.setStatus(TaskStatus.DONE);
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
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, response.statusCode(), response.body());
            final Subtask updatedSubtask = taskManager.getSubTaskByTaskId(subtaskId);
            assertEquals(newName, updatedSubtask.getName(), "Имя задачи не совпадает");
            assertEquals(newDescription, updatedSubtask.getDescription(), "Описание задачи не совпадает");
            assertEquals(TaskStatus.DONE, updatedSubtask.getStatus(), "Статус задачи не совпадает");
            assertEquals(newStartTime, updatedSubtask.getStartTime(), "Дата начала задачи не совпадает");
            assertEquals(newDuration, updatedSubtask.getDuration(), "Продолжительность задачи не совпадает");
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
    }

    @Test
    void getSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test addNewEpicForSubtask", "Test addNewEpicForSubtask description");
        final int epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Тест", "Тестовое описание",epicId, LocalDateTime.now(), Duration.ofMinutes(5));
        subtask.setStartTime(LocalDateTime.of(2022, 03, 01, 10, 00));
        subtask.setDuration(Duration.ofMinutes(60));
        final int subtaskId = taskManager.addSubTask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(apiUrl + "/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Accept", "application/json")
                .GET()
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            final Subtask subtaskFromHttp = gson.fromJson(response.body(), new TypeToken<Subtask>() {
            }.getType());
            assertEquals(subtaskId, subtaskFromHttp.getTaskId(), "Некорректный id подзадачи");
            assertEquals("Test addNewSubtask", subtaskFromHttp.getName(), "Некорректное имя подзадачи");
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
    }

    @Test
    void getSubtaskByWrongId() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(apiUrl + "/5324542345234562");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Accept", "application/json")
                .GET()
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(404, response.statusCode());
            assertEquals("Некорректный идентификатор подзадачи", response.body(), "Не верная ошибка при неправильном ID подзадачи");
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
    }

    @Test
    void getSubtaskWrongEndPoint() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(apiUrl + "/123456789/test");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Accept", "application/json")
                .GET()
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(404, response.statusCode());
            assertEquals("Такого эндпоинта не существует", response.body(), "Не верная ошибка при неправильном ендпоинте");
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
    }

    @Test
    void getSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Test addNewEpicForSubtask", "Test addNewEpicForSubtask description");
        final int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("Test addNewSubtask", "Test addNewSubtask description",epicId,LocalDateTime.of(2022, 03, 01, 10, 00),Duration.ofMinutes(60));
        taskManager.addSubTask(subtask1);
        Subtask subtask2 = new Subtask("Test addNewSubtask", "Test addNewSubtask description",epicId,LocalDateTime.of(2022, 03, 01, 10, 00),Duration.ofMinutes(60));
        taskManager.addSubTask(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(apiUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Accept", "application/json")
                .GET()
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            final List<Subtask> subtasksFromHttp = gson.fromJson(response.body(), new TypeToken<List<Subtask>>() {
            }.getType());
            assertEquals(2, subtasksFromHttp.size(), "Некорректное количество подзадач");
            assertEquals(true, subtasksFromHttp.contains(subtask1), "Подзадача отсутствует в возвращаемом списке задач");
            assertEquals(true, subtasksFromHttp.contains(subtask2), "Подзадача отсутствует в возвращаемом списке задач");
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
    }

    @Test
    void addIntersectionsSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Test addNewEpicForSubtask", "Test addNewEpicForSubtask description");
        final int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("Test addNewSubtask", "Test addNewSubtask description",epicId,LocalDateTime.of(2022, 03, 01, 10, 00),Duration.ofMinutes(60));
        taskManager.addTask(subtask1);
        Subtask subtask2 = new Subtask("Test addNewSubtask", "Test addNewSubtask description",epicId,LocalDateTime.of(2022, 03, 01, 10, 00),Duration.ofMinutes(60));
        String taskJson = gson.toJson(subtask2);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(apiUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
         HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(406, response.statusCode());
            assertEquals("Задачи пересекаются!", response.body(), "Не верная ошибка при пересекающихся задачах");
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
    }

    @Test
    void deleteSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test addNewEpicForSubtask", "Test addNewEpicForSubtask description");
        final int epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Test addNewSubtask", "Test addNewSubtask description",epicId,LocalDateTime.of(2022, 03, 01, 10, 00),Duration.ofMinutes(60));
        final long subtaskId = taskManager.addSubTask(subtask);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(apiUrl + "/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Accept", "application/json")
                .DELETE()
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            List<Subtask> subtasksFromManager = taskManager.getAllSubTasks();
            assertNotNull(subtasksFromManager, "Задачи не возвращаются");
            assertEquals(0, subtasksFromManager.size(), "Некорректное количество задач");
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
    }
}