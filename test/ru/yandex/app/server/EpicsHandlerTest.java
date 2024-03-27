package ru.yandex.app.server;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import ru.yandex.app.model.Epic;
import ru.yandex.app.model.Subtask;

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

class EpicsHandlerTest extends HttpTaskServerTest {
    String apiUrl = "http://localhost:8080/api/v1/epics";

    EpicsHandlerTest() throws IOException {
    }

    @Test
    void postEpic() {
        Epic epic = new Epic("Test addNewEpic", "Test addNewEpic description");
        String taskJson = gson.toJson(epic);
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
            List<Epic> epicsFromManager = taskManager.getAllEpics();
            assertNotNull(epicsFromManager, "Эпики не возвращаются");
            assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
            assertEquals("Test addNewEpic", epicsFromManager.get(0).getName(), "Некорректное имя эпика");
        } catch (IOException | InterruptedException e) {
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void updateEpic() {
        Epic epic = new Epic("Тест", "Тестовое описание");
        final int epicId = taskManager.addEpic(epic);

        Epic epicForUpdate = taskManager.getEpicByTaskId(epicId);
        String newName = "New Name";
        String newDescription = "New Description";
        epicForUpdate.setDescription(newDescription);
        String taskJson = gson.toJson(epicForUpdate);
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
            final Epic updatedEpic = taskManager.getEpicByTaskId(epicId);
            assertEquals(newName, updatedEpic.getName(), "Имя задачи не совпадает");
            assertEquals(newDescription, updatedEpic.getDescription(), "Описание задачи не совпадает");
        } catch (IOException | InterruptedException e) {
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void getEpicById() {
        Epic epic = new Epic("Test addNewEpic", "Test addNewEpic description");
        final long epicId = taskManager.addEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(apiUrl + "/" + epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());

            final Epic epicFromHttp = gson.fromJson(response.body(), new TypeToken<Epic>() {
            }.getType());
            assertEquals(epicId, epicFromHttp.getTaskId(), "Некорректный id эпика");
            assertEquals("Test addNewEpic", epicFromHttp.getName(), "Некорректное имя эпика");
        } catch (IOException | InterruptedException e) {
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void getEpicByWrongId() {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(apiUrl + "/123456789");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(404, response.statusCode());
            assertEquals("Некорректный идентификатор эпика", response.body(), "Не верная ошибка при неправильном ID эпика");
        } catch (IOException | InterruptedException e) {
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void getEpicWrongEndPoint() {
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
    void getEpics() {
        Epic epic1 = new Epic("Test addNewEpic1", "Test addNewEpic1 description");
        taskManager.addEpic(epic1);
        Epic epic2 = new Epic("Test addNewEpic2", "Test addNewEpic2 description");
        taskManager.addEpic(epic2);

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
            final List<Epic> epicsFromHttp = gson.fromJson(response.body(), new TypeToken<List<Epic>>() {
            }.getType());
            assertEquals(2, epicsFromHttp.size(), "Некорректное количество задач");
            assertEquals(true, epicsFromHttp.contains(epic1), "Задача отсутствует в возвращаемом списке задач");
            assertEquals(true, epicsFromHttp.contains(epic2), "Задача отсутствует в возвращаемом списке задач");
        } catch (IOException | InterruptedException e) {
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void getEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Test addNewEpicForSubtask", "Test addNewEpicForSubtask description");
        final int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("Test addNewSubtask1", "Test addNewSubtask description1",epicId,LocalDateTime.of(2022, 03, 01, 10, 00),Duration.ofMinutes(60));
        taskManager.addSubTask(subtask1);
        Subtask subtask2 = new Subtask("Test addNewSubtask2", "Test addNewSubtask description1",epicId,LocalDateTime.of(2022, 03, 01, 10, 00),Duration.ofMinutes(60));
        taskManager.addSubTask(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(apiUrl + "/" + epicId + "/subtasks");
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
    void deleteEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test addNewEpicForSubtask", "Test addNewEpicForSubtask description");
        final long epicId = taskManager.addEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(apiUrl + "/" + epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Accept", "application/json")
                .DELETE()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            List<Epic> epicsFromManager = taskManager.getAllEpics();
            assertNotNull(epicsFromManager, "Эпики не возвращаются");
            assertEquals(0, epicsFromManager.size(), "Некорректное количество эпиков");
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");

    }
}