import com.google.gson.reflect.TypeToken;
import http.HttpTaskServerTest;
import org.junit.jupiter.api.Test;
import tasks.Status;
import tasks.Task;

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

class TasksHandlerTest extends HttpTaskServerTest {
    String apiUrl = "http://localhost:8080/api/v1/tasks";

    TasksHandlerTest() throws IOException {
    }

    @Test
    void postTask() {
        Task task = new Task("Тест", "Тестовое описание", LocalDateTime.now(), Duration.ofMinutes(5));
        String taskJson = gson.toJson(task);
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
            List<Task> tasksFromManager = taskManager.getTasks();
            assertNotNull(tasksFromManager, "Задачи не возвращаются");
            assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
            assertEquals("Тест", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
        } catch (IOException | InterruptedException e) {
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void updateTask() {
        Task task = new Task("Тест", "Тестовое описание", LocalDateTime.now(), Duration.ofMinutes(5));
        final Long taskId = taskManager.addTask(task);

        Task taskForUpdate = taskManager.getTask(taskId);
        String newName = "New Name";
        String newDescription = "New Description";
        Status newStatus = Status.DONE;
        LocalDateTime newStartTime = LocalDateTime.now().plusHours(1);
        Duration newDuration = Duration.ofMinutes(60);
        taskForUpdate.setName(newName);
        taskForUpdate.setDescription(newDescription);
        taskForUpdate.setStatus(newStatus);
        taskForUpdate.setStartTime(newStartTime);
        taskForUpdate.setDuration(newDuration);
        String taskJson = gson.toJson(taskForUpdate);
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
            final Task updatedTask = taskManager.getTask(taskId);
            assertEquals(newName, updatedTask.getName(), "Имя задачи не совпадает");
            assertEquals(newDescription, updatedTask.getDescription(), "Описание задачи не совпадает");
            assertEquals(newStatus, updatedTask.getStatus(), "Статус задачи не совпадает");
            assertEquals(newStartTime, updatedTask.getStartTime(), "Дата начала задачи не совпадает");
            assertEquals(newDuration, updatedTask.getDuration(), "Продолжительность задачи не совпадает");
        } catch (IOException | InterruptedException e) {
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void getTaskById() {
        Task task = new Task("Тест", "Тестовое описание", LocalDateTime.now(), Duration.ofMinutes(5));
        Long taskId = taskManager.addTask(task);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(apiUrl + "/" + taskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            final Task taskFromHttp = gson.fromJson(response.body(), new TypeToken<Task>() {
            }.getType());
            assertEquals(taskId, taskFromHttp.getId(), "Некорректный id задачи");
            assertEquals("Тест", taskFromHttp.getName(), "Некорректное имя задачи");
        } catch (IOException | InterruptedException e) {
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void getTaskByWrongId() {
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
            assertEquals("Некорректный идентификатор задачи", response.body(), "Не верная ошибка при неправильном ID задачи");
        } catch (IOException | InterruptedException e) {
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void getTaskWrongEndPoint() {
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
    void getTasks() {
        Task task1 = new Task("Тест1", "Тестовое описание1", LocalDateTime.now(), Duration.ofMinutes(5));
        taskManager.addTask(task1);
        Task task2 = new Task("Тест2", "Тестовое описание2", LocalDateTime.now().plusHours(1), Duration.ofMinutes(5));
        taskManager.addTask(task2);

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
            final List<Task> tasksFromHttp = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
            }.getType());
            assertEquals(2, tasksFromHttp.size(), "Некорректное количество задач");
            assertEquals(true, tasksFromHttp.contains(task1), "Задача отсутствует в возвращаемом списке задач");
            assertEquals(true, tasksFromHttp.contains(task2), "Задача отсутствует в возвращаемом списке задач");
        } catch (IOException | InterruptedException e) {
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void addIntersectionsTasks() {
        Task task1 = new Task("Тест1", "Тестовое описание1", LocalDateTime.now(), Duration.ofMinutes(50));
        taskManager.addTask(task1);
        Task task2 = new Task("Тест2", "Тестовое описание2", LocalDateTime.now(), Duration.ofMinutes(50));

        String taskJson = gson.toJson(task2);
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
    void deleteTask() {
        Task task = new Task("Тест", "Тестовое описание", LocalDateTime.now(), Duration.ofMinutes(5));
        Long taskId = taskManager.addTask(task);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(apiUrl + "/" + taskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Accept", "application/json")
                .DELETE()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            List<Task> tasksFromManager = taskManager.getTasks();
            assertNotNull(tasksFromManager, "Задачи не возвращаются");
            assertEquals(0, tasksFromManager.size(), "Некорректное количество задач");
        } catch (IOException | InterruptedException e) {
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }
}