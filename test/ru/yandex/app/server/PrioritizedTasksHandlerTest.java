import com.google.gson.reflect.TypeToken;
import http.HttpTaskServerTest;
import org.junit.jupiter.api.Test;
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

class PrioritizedHandlerTest extends HttpTaskServerTest {
    String apiUrl = "http://localhost:8080/api/v1/prioritized";

    PrioritizedHandlerTest() throws IOException {
    }

    @Test
    void getPrioritized() {
        Task task1 = new Task("Тест1", "Тестовое описание1", LocalDateTime.now(), Duration.ofMinutes(5));
        Long taskId1 = taskManager.addTask(task1);
        Task task2 = new Task("Тест2", "Тестовое описание2", LocalDateTime.now().plusHours(1), Duration.ofMinutes(5));
        Long taskId2 = taskManager.addTask(task2);
        taskManager.getTask(taskId1);
        taskManager.getTask(taskId2);

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
    void getPrioritizedWrongEndPoint() {
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
}